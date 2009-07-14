/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy;


import java.io.Externalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;

/**
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.8 $
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


      //// ROC PINPOINT MIKECHEN EMK BEGIN ////
      java.util.Map PP_originInfo = new java.util.HashMap( roc.pinpoint.tracing.java.EnvironmentDetails.GetDetails() );
      PP_originInfo.put( "name", (String)context.getValue( org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME));
      PP_originInfo.put( "methodName", m.getName() );
      PP_originInfo.put( "type", "EJB" );
      // TODO: add method arguments, and other info, to originInfo
      // TODO: generic info, like JVM, OS version, don't need to be reported every time...

      roc.pinpoint.tracing.RequestInfo PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo(); 
      PP_reqInfo.incrementSeqNum();

      java.util.Map PP_attributes = new java.util.HashMap();
      PP_attributes.put( "observationLocation",
			 "org.jboss.proxy.ClientContainer" );
      PP_attributes.put( "stage", "METHODCALLBEGIN" );
      
      roc.pinpoint.tracing.Observation PP_obs = 
	  new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE,
						PP_reqInfo,
						PP_originInfo, 
						null, 
						PP_attributes );
						
      roc.pinpoint.tracing.jboss.JMSObservationPublisher.GlobalSend( PP_obs );
      //// ROC PINPOINT MIKECHEN EMK END ////


      Object result = null;
      try {
	  // ROC PINPOINT EMK BEGIN 
	  // fault injection
	  int fault = roc.pinpoint.injection.FaultGenerator.CheckFaultTriggers( PP_originInfo );
	  if( fault == roc.pinpoint.injection.FaultTrigger.FT_NOFAULT ) {
	      // invoke normally . don't inject any fault
	      result = next.invoke(invocation);
	  } else if( roc.pinpoint.injection.FaultGenerator.isAutomatableFault( fault )) {
	      roc.pinpoint.injection.FaultGenerator.GenerateFault( fault );
	  }
	  else if( fault == roc.pinpoint.injection.FaultTrigger.FT_THROWEXPECTEDEXCEPTION ) {
	      Class[] exceptions = m.getExceptionTypes();
	      // todo: what's the best thing to do? we'll just use the
	      //  first exception now.
	      if( exceptions.length > 0 ) {
		  throw (Throwable)(exceptions[0].newInstance());
	      }
	      // if there's no throws exceptions, don't throw anything.
	  }
	  else if( fault == roc.pinpoint.injection.FaultTrigger.FT_NULLCALL ) {
	      // do nothing. it's a null call.
	  }
	  // ROC PINPOINT EMK END
	  // [original code]  result = next.invoke(invocation);
      }
      catch (Exception e) {
	  e.printStackTrace();
	  
	  //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK BEGIN ////
	  //      report details of the exception, this is a likely failure
	  PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
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

	  PP_obs = 
	      new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_ERROR,
						    PP_reqInfo,
						    PP_originInfo, 
						    PP_rawDetails, 
						    PP_attributes );
	  roc.pinpoint.tracing.jboss.JMSObservationPublisher.GlobalSend( PP_obs );
	  //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK END ////

	  throw e;
      }
      finally {
	  //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK BEGIN ////
	  try {
	      PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
	      PP_reqInfo.incrementSeqNum();
	      PP_attributes.put( "stage", "METHODCALLEND" );
	      PP_obs = 
		  new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE,
							PP_reqInfo,
							PP_originInfo, 
							null, 
							PP_attributes );
	      roc.pinpoint.tracing.jboss.JMSObservationPublisher.GlobalSend( PP_obs );
	  }
	  catch( Exception e ) {
	      e.printStackTrace();
	  }
	  //// ROC PINPOINT PATH-ANALYSIS MIKECHEN EMK END ////
      }


      return result;
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
 
