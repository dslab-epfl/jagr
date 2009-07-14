/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy;

import java.util.*;
import java.net.*;
import java.io.*;
import org.jboss.RR.FailureReport;

import java.io.Externalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;

       //// MIKECHEN: BEGIN ////
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
       //// MIKECHEN: END   ////
import roc.config.ROCConfig;

/**
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.12 $
 *
 * <p><b>2001/11/19: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public class ClientContainer
   implements Externalizable, InvocationHandler
{
      
   // the "static" information that gets attached to every invocation
   public InvocationContext context;
   
   // The first interceptor in the chain
   public Interceptor next;
   
   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};

   public ClientContainer()
   {
      // For externalization to work
   }
   
   public ClientContainer(InvocationContext context) 
   {
      this.context = context;
   }
   
   public Object invoke(final Object proxy,
      final Method m,
      Object[] args)
   throws Throwable
   {
      // Normalize args to always be an array
      // Isn't this a bug in the proxy call??
      if (args == null)
         args = EMPTY_ARGS;
        
      //Create the invocation object
      Invocation invocation = new Invocation();
      
      // Contextual information for the interceptors
      invocation.setInvocationContext(context);
      
      invocation.setObjectName(context.getObjectName());
      invocation.setMethod(m);
      invocation.setArguments(args);
      
      // send the invocation down the client interceptor chain
      //return next.invoke(invocation);

      //// ROC PINPOINT MIKECHEN EMK BEGIN ////
      java.util.Map PP_originInfo = null;
      java.util.Map PP_attributes = null;

      if( ROCConfig.ENABLE_PINPOINT &&
	  ROCConfig.ENABLE_PINPOINT_TRACING_EJB ) {

	  PP_originInfo = new java.util.HashMap( roc.pinpoint.tracing.java.EnvironmentDetails.GetDetails() );
	  // todo: may have to adjust "name", taking only substring(indexof(".")).trim()
	  PP_originInfo.put( "name", (String)context.getValue( org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME));
	  PP_originInfo.put( "methodName", m.getName() );
	  PP_originInfo.put( "type", "EJB" );
	  // TODO: add method arguments, and other info, to originInfo
	  
	  roc.pinpoint.tracing.RequestInfo PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo(); 
	  PP_reqInfo.incrementSeqNum();
	  
	  PP_attributes = new java.util.HashMap();
	  PP_attributes.put( "observationLocation",
			     "org.jboss.proxy.ClientContainer" );
	  PP_attributes.put( "stage", "METHODCALLBEGIN" );
	  
	  roc.pinpoint.tracing.Observation PP_obs = 
	      new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE,
						    PP_reqInfo,
						    PP_originInfo, 
						    null, 
						    PP_attributes );
	  
	  roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );
      }
      //// ROC PINPOINT MIKECHEN EMK END ////


       //// MIKECHEN: BEGIN ////
      //System.out.println("getObjectName: " + invocation.getObjectName());
      //System.out.println("getMethodName: " + invocation.getMethod().getName());
      //12:54:03,087 INFO  [STDOUT] getObjectName: 1668941772
      //12:54:03,087 INFO  [STDOUT] getMethodName: getDetails

       //System.out.println(context.getObjectName());
       //System.out.println(context.context);
       //21:25:04,119 INFO  [STDOUT] -1116137989
       //21:25:04,119 INFO  [STDOUT] {-1616783654=org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxy@7cbde6, 905134987=-1116137989, 293367873=TheShoppingClientController, 824509681=org.jboss.proxy.ejb.EJBMetaDataImpl@66c114}
      //return next.invoke(invocation);
      
       try {
	   // ROC PINPOINT EMK BEGIN 
	   if( ROCConfig.ENABLE_PINPOINT &&
	       ROCConfig.ENABLE_PINPOINT_TRACING_EJB &&
	       ROCConfig.ENABLE_PINPOINT_FAULT_INJECTION ) {
	       // fault injection
	       int fault = roc.pinpoint.injection.FaultGenerator.CheckFaultTriggers( PP_originInfo );
	       if( fault == roc.pinpoint.injection.FaultTrigger.FT_NOFAULT ) {
		   // invoke normally . don't inject any fault
		   return next.invoke(invocation);
	       } else if( roc.pinpoint.injection.FaultGenerator.isAutomatableFault( fault )) {
		   roc.pinpoint.injection.FaultGenerator.GenerateFault( fault );
		   // never reached --
		   return null;
	       }
	       else if( fault == roc.pinpoint.injection.FaultTrigger.FT_THROWEXPECTEDEXCEPTION ) {
		   Class[] exceptions = m.getExceptionTypes();
		   // todo: what's the best thing to do? we'll just use the
		   //  first exception now.
		   if( exceptions.length > 0 ) {
		       throw (Throwable)(exceptions[0].newInstance());
		   }
		   else {
		       // if there's no throws exceptions, don't throw anything.
		       return null;
		   }
	       }
	       else if( fault == roc.pinpoint.injection.FaultTrigger.FT_NULLCALL ) {
		   // do nothing. it's a null call.
		   return null;
	       }
	       else {
		   // never reached -- error in Pinpoint.
		   // just call original code
		   return next.invoke(invocation);
	       }
	   }
	   // ROC PINPOINT EMK END
	   else {
	       // original code
	       return next.invoke(invocation);
	   }
       }
       catch (Throwable e) {

	   //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK BEGIN ////
	   if( ROCConfig.ENABLE_PINPOINT &&
	       ROCConfig.ENABLE_PINPOINT_TRACING_EJB ) {
	       //      report details of the exception, this is a likely failure
	       roc.pinpoint.tracing.RequestInfo PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
	       PP_reqInfo.incrementSeqNum();
	       
	       java.util.Map PP_rawDetails = new java.util.HashMap();
	       StackTraceElement[] PP_ste = e.getStackTrace();
	       java.util.List PP_stacktrace = new java.util.ArrayList( PP_ste.length );
	       for( int PP_i=0; PP_i < PP_ste.length; PP_i++ ) {
		   // todo, later, we might want a more structure storage 
		   //       of the details in PP_ste[ PP_i ], rather than
		   //       just a toString() dump.
		   PP_stacktrace.add( PP_ste[ PP_i ].toString() );
	       }
	       PP_rawDetails.put( "exception", e.toString() );
	       PP_rawDetails.put( "stacktrace", PP_stacktrace );
	       
	       if( PP_attributes.containsKey( "stage" ))
		   PP_attributes.remove( "stage" );
	       
	       roc.pinpoint.tracing.Observation PP_obs = 
		   new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_ERROR,
							 PP_reqInfo,
							 PP_originInfo, 
							 PP_rawDetails, 
							 PP_attributes );
	       roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );
	   }
	   //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK END ////

	   	    //// BEGIN: MIKECHEN
	    StringBuffer str = new StringBuffer("handleException: ");
	    try {
		InitialContext ic = new InitialContext();
		String jndiName = (String) context.getValue(org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME);
		String className = jndiName;//ic.lookup(jndiName).getClass().getName();
		String methodName = invocation.getMethod().getName();
	   
		//// read in the stack trace
		java.io.StringWriter sw = new java.io.StringWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(sw);
		e.printStackTrace(pw);
		System.err.println("=============================================================");
		System.err.println("================  BEGIN STACKTRACE ==========================");
		e.printStackTrace();
		System.err.println("================  END   STACKTRACE ==========================");
		System.err.println("=============================================================");
		String stacktrace = new String(sw.getBuffer());
		java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(stacktrace));
	   
		String line = null;
		String prevLine = null;
		String caller = null;
		String callee = null;
		String callerMethod = null;
		String calleeMethod = null;
	   

		callee = className;
		calleeMethod = methodName;
		// skip to the first Container
		line = br.readLine();
		
		while (line != null) {
		    //if (line.indexOf("org.jboss.ejb.Container.invoke") != -1) 
		    if (line.indexOf("org.jboss.proxy.ClientContainer.invoke") != -1) 
			break;
		    line = br.readLine();
		}
		

		while (line != null) {
		    //// caller is the previous bean
		    if (line.indexOf("sun.reflect.") != -1) {
			if (prevLine != null) {
			    prevLine = prevLine.substring("at ".length());
			    prevLine = prevLine.substring(0, prevLine.indexOf("("));
			    // find the ejb and the method
			    caller    = prevLine.substring(0, prevLine.lastIndexOf("."));
			    callerMethod = prevLine.substring(prevLine.lastIndexOf(".")+1);
			    break;
			}
		    }
		    //// caller is the previous Servlet
		    else if (line.indexOf("javax.servlet.http.HttpServlet.service") != -1) {
			if (prevLine != null) {
			    prevLine = prevLine.substring("at ".length());
			    prevLine = prevLine.substring(0, prevLine.indexOf("("));
			    // find the servlet and the method
			    caller    = prevLine.substring(0, prevLine.lastIndexOf("."));
			    callerMethod = prevLine.substring(prevLine.lastIndexOf(".")+1);
			    break;
			}
		    }
		    prevLine = line;
		    line = br.readLine();
		}



		System.err.println(caller + "." + callerMethod + " --> " + callee + "." + calleeMethod);
	   
		if ((caller != null) && (callee != null)) 
                {
                    // START STEVE ZHANG

                    // Use the short name
                    int idx1 = 1 + callee.lastIndexOf(".");
                    int idx2 = 1 + caller.lastIndexOf(".");

                    // send directly to brain
                    try
                    {  
                        // temperarily hardcoded to be localhost
                        InetSocketAddress sockAddr = new InetSocketAddress(InetAddress.getLocalHost(), 
									   2374);
                        DatagramSocket socket = new DatagramSocket();
			// all node names should be trimmed
                        FailureReport report = new FailureReport((callee.substring(idx1)).trim(), 
                                                                 (caller.substring(idx2)).trim(), 
								 new Date());
                        ByteArrayOutputStream bArray_out = new ByteArrayOutputStream();
                        ObjectOutputStream obj_out = new ObjectOutputStream(bArray_out);
                        obj_out.writeObject(report);
                        DatagramPacket packet = new DatagramPacket(bArray_out.toByteArray(), 
                                                                   bArray_out.size(), 
                                                                   sockAddr);
                        socket.send(packet);
                        System.out.println("ClientContainer: FailureReport message sent to TheBrain!");
                    }
                    catch (SocketException sockExp)
                    {
                        System.err.println("ClientContainer: failed to bind to a UDP port!");
                    }
                    catch (IOException ioExp)
                    {
                        System.err.println("ClientContainer: Error sending FailureReport to TheBrain!");
                    }
                    // END STEVE ZHANG
		    // FOR NOW We will do both (report to brain as well to FailureMonitor)
                    
	 	    // System.err.println("======= invoking FailureMonitor ====== ");
	 	    //// report to the FaultMonitor MBean
		    //InitialContext ic = new InitialContext();
		    //RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx:localhost:rmi");
		    // String serverName = java.net.InetAddress.getLocalHost().getHostName();
		    // RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx:" + serverName + ":rmi");
	       
		    // ObjectName name = new ObjectName("jboss:type=FailureMonitor");
		    //MBeanInfo info = server.getMBeanInfo(name);
		    //ObjectInstance instance = server.getObjectInstance(name);
		    //String[] sig = {"org.jboss.ejb.Container", "org.jboss.ejb.Container"};
		    // String[] sig = {"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.Throwable"};
		    // Object[] opArgs = {caller, callerMethod, callee, calleeMethod, e};
		    // Object result = server.invoke(name, "reportFailure", opArgs, sig);
		    //System.out.println("JNDIView.list(true) output:\n"+result);
		    //Thread.sleep(10000);
                    
		}
	    }
	    catch (Exception oe) {
		oe.printStackTrace();
	    }
	    
	    //// END: MIKECHEN /////

	    

	   throw e;

       }
       ////MIKECHEN: END ////
       finally {
	   //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK BEGIN ////
	   if( ROCConfig.ENABLE_PINPOINT &&
	       ROCConfig.ENABLE_PINPOINT_TRACING_EJB ) {
	       try {
		   roc.pinpoint.tracing.RequestInfo PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
		   PP_reqInfo.incrementSeqNum();
		   PP_attributes.put( "stage", "METHODCALLEND" );
		   roc.pinpoint.tracing.Observation PP_obs = 
		       new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE,
							     PP_reqInfo,
							     PP_originInfo, 
							     null, 
							     PP_attributes );
		   roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );
	       }
	       catch( Exception e ) {
		   e.printStackTrace();
	       }
	   }
	   //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK END ////
       }
       
   }
   
   public Interceptor setNext(Interceptor interceptor) 
   {
      next = interceptor;
      
      return interceptor;
   }
   
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(next);
      out.writeObject(context);
   }

   /**
   * Externalization support.
   *
   * @param in
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      next = (Interceptor) in.readObject();
      context = (InvocationContext) in.readObject();

   }
}
 
