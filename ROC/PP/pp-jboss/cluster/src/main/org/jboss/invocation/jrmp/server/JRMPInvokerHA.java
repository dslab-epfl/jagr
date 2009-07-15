/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation.jrmp.server;

import java.io.Serializable;
import java.rmi.ServerException;
import java.rmi.MarshalledObject;

import javax.naming.InitialContext;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerHA;
import org.jboss.logging.Logger;
import org.jboss.system.Registry;

import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.GenericClusteringException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The JRMPInvokerHA is an HA-RMI implementation that can generate Invocations from RMI/JRMP 
 * into the JMX base
 *
 * @author <a href="mailto:bill@burkecentral.com>Bill Burke</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.3 $
 */
public class JRMPInvokerHA
   extends JRMPInvoker
   implements InvokerHA
{
   protected HashMap beanMap = new HashMap();

   public JRMPInvokerHA()
   {
      super();
   }
   
   public void start()
      throws Exception
   {
      if (getState() != STOPPED && getState() != FAILED)
         return;
      
      state = STARTING;
      log.info("Starting");

      loadCustomSocketFactories();

      if (log.isDebugEnabled())
      {
         log.debug("RMI Port='" +  (rmiPort == ANONYMOUS_PORT ?
            "Anonymous" : Integer.toString(rmiPort)+"'"));
         log.debug("Client SocketFactory='" + (clientSocketFactory == null ?
            "Default" : clientSocketFactory.toString()+"'"));
         log.debug("Server SocketFactory='" + (serverSocketFactory == null ?
            "Default" : serverSocketFactory.toString()+"'"));
         log.debug("Server SocketAddr='" + (serverAddress == null ?
            "Default" : serverAddress+"'"));
         log.debug("SecurityDomain='" + (sslDomain == null ?
            "None" : sslDomain+"'"));
      }

      try
      {
         exportCI();
         Registry.bind(serviceName, this);
      }
      catch (Exception e)
      {
         state = FAILED;
         log.error("Failed", e);
         throw new ServerException("Could not bind HA JRMP invoker", e);
      }
      
      state = STARTED;
      log.info("Started");
   }

   public void stop()
   {
      if (getState() != STARTED)
         return;
      
      state = STOPPING;
      log.info("Stopping");
      
      try
      {
         unexportCI();
      }
      catch (Throwable e)
      {  
         state = FAILED;
         log.error("Failed", e);
         return;
      }
      state = STOPPED;
      log.info("Stopped");
   }
   
   public void destroy()
   {
   }

   public void registerBean(ObjectName beanName, HATarget target)
      throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());
      if (log.isDebugEnabled())
      {
         log.debug("BEAN NAME IS "+beanName);
      }

      if (beanMap.containsKey(hash))
      {
         // FIXME [oleg] In theory this is possible!
         throw new IllegalStateException("Trying to register bean with the existing hashCode");
      }
      beanMap.put(hash, target);
      JRMPInvokerProxyHA.colocation.add(hash);
   }

   public Invoker createProxy(ObjectName targetName, LoadBalancePolicy policy)
      throws Exception
   {
      Integer hash = new Integer(targetName.hashCode());
      HATarget target = (HATarget)beanMap.get(hash);
      if (target == null)
      {
         throw new IllegalStateException("The bean hashCode not found");
      }
      return new JRMPInvokerProxyHA(target.getReplicants(), target.getCurrentViewId (), policy);
   }
   public Invoker createProxy(String beanName, LoadBalancePolicy policy) throws Exception
   {
      ObjectName targetName = new ObjectName(beanName);
      return createProxy(targetName, policy);
   }

   public void unregisterBean(ObjectName beanName) throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());
      beanMap.remove(hash);
      JRMPInvokerProxyHA.colocation.remove(hash);
   }

   /** Return the exported RemoteStub as the HATarget stub
    */
   public Serializable getStub()
   {
      return super.invokerStub;
   }

   /**
    * Invoke a Remote interface method.
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

      try
      {
	  // ROC PINPOINT EMK BEGIN              --- 6 LINES
	 if( roc.config.ROCConfig.ENABLE_PINPOINT &&
	     roc.config.ROCConfig.ENABLE_PINPOINT_TRACING_RMI ) {
	     roc.pinpoint.tracing.RequestInfo rocRequestInfo = 
		 (roc.pinpoint.tracing.RequestInfo)
		 invocation.getValue( "roc.requestid" );
	     roc.pinpoint.tracing.ThreadedRequestTracer.setRequestInfo( rocRequestInfo );
	 }
	  // ROC PINPOINT EMK END

         // Deserialize the transaction if it is there
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         invocation.setTransaction(importTPC(mi.getTransactionPropagationContext()));

         // Extract the ObjectName, the rest is still marshalled
         Integer beanNameHash = (Integer) invocation.getObjectName();
         ObjectName mbean = (ObjectName) Registry.lookup(beanNameHash);

         long clientViewId = ((Long)invocation.getValue("CLUSTER_VIEW_ID")).longValue();

         // The cl on the thread should be set in another interceptor
         Object rtn = server.invoke(mbean,
                                    "",
                                    new Object[] { invocation },
                                    Invocation.INVOKE_SIGNATURE);

         // Update the targets list if the client view is out of date         
         HARMIResponse rsp = new HARMIResponse();


	 // ROC PINPOINT EMK BEGIN              --- 4 LINES
	 if( roc.config.ROCConfig.ENABLE_PINPOINT &&
	     roc.config.ROCConfig.ENABLE_PINPOINT_TRACING_RMI ) {
	     rsp.request_seqnum = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo().getSeqNum();
	 }
	 // ROC PINPOINT EMK END

         HATarget target = (HATarget) beanMap.get(beanNameHash);
         if (target == null)
         {
            throw new IllegalStateException("The bean hashCode not found");
         }
         if (clientViewId != target.getCurrentViewId())
         {
            rsp.newReplicants = new ArrayList(target.getReplicants());
            rsp.currentViewId = target.getCurrentViewId();
         }
         rsp.response = rtn;
         return new MarshalledObject(rsp);
      }
      catch (InstanceNotFoundException infe)
      {
         throw new GenericClusteringException (GenericClusteringException.COMPLETED_NO, infe.getMessage ());
      }
      catch (ReflectionException ie)
      {
         throw new GenericClusteringException (GenericClusteringException.COMPLETED_NO, ie.getMessage ());
      }
      catch (MBeanException e)
      {
         throw e.getTargetException();
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
}
