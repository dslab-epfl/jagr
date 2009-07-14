/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.http.interfaces;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.rmi.ServerException;
import java.util.ArrayList;

import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;

/** The client side Http invoker proxy that posts an invocation to the
 InvokerServlet using the HttpURLConnection created from a target url.
 This proxy handles failover using its associated LoadBalancePolicy and
 current list of URL strings. The candidate URLs are updated dynamically
 after an invocation if the cluster partitation view has changed.

* @author Scott.Stark@jboss.org
* @version $Revision: 1.1.1.1 $
*/
public class HttpInvokerProxyHA
   implements Invoker, Externalizable
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(HttpInvokerProxyHA.class);

   /** Serial Version Identifier. */
   //static final long serialVersionUID = -8249272784108192267L;
   // Attributes ----------------------------------------------------

   // URL to the remote JMX node invoker
   protected ArrayList targets = null;
   protected LoadBalancePolicy loadBalancePolicy;
   protected transient long currentViewId = 0;
   protected transient boolean trace = false;

   // Constructors --------------------------------------------------
   public HttpInvokerProxyHA()
   {
      // For externalization to work
   }

   /**
    * @param targets, the list of URLs through which clients should contact the
    * InvokerServlet.
    * @param policy, the policy for choosing among targets
   */
   public HttpInvokerProxyHA(ArrayList targets, LoadBalancePolicy policy)
   {
      this.targets = targets;
      this.loadBalancePolicy = policy;
      this.trace = log.isTraceEnabled();
      if( trace )
         log.trace("Init, targets: "+targets+", policy="+loadBalancePolicy);
   }

   // Public --------------------------------------------------------

   public String getServerHostName() throws Exception
   {
      return null;
   }
   public ArrayList getTargets()
   {
      return targets;
   }

   public void setTargets(ArrayList newTargets)
   {
      synchronized(targets)
      {
         targets.clear();
         targets.addAll(newTargets);
         if( trace )
            log.trace("Updated targets: "+targets);
      }
   }

   public Object getRemoteTarget()
   {
      Object target = null;
      if( targets.size() > 0 )
      {
         synchronized (targets)
         {
            target = loadBalancePolicy.chooseTarget(targets);
         }
      }
      if( trace )
         log.trace("Choose remoteTarget: "+target);
      return target;
   }

   public void remoteTargetHasFailed(Object target)
   {
      removeDeadTarget(target);
   }   

   protected void removeDeadTarget(Object target)
   {
      if (targets != null)
      {
         boolean removed = targets.remove(target);
         if( trace )
         {
            log.trace("removeDeadTarget("+target+"), removed="+removed
               +", targets.size="+targets.size());
         }
      }
   }

   /** This method builds a MarshalledInvocation from the invocation passed
    in and then does a post to the target URL.
   */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // we give the opportunity, to any server interceptor, to know if this a
      // first invocation to a node or if it is a failovered call
      //
      int failoverCounter = 0;
      
      // We are going to go through a Remote invocation, switch to a Marshalled Invocation
      MarshalledInvocation mi = new MarshalledInvocation(invocation);         
      mi.setValue("CLUSTER_VIEW_ID", new Long(currentViewId));
      String target = (String) getRemoteTarget();
      URL externalURL = Util.resolveURL(target);
      while( externalURL != null )
      {
         invocation.setValue ("FAILOVER_COUNTER", new Integer(failoverCounter), invocation.AS_IS);
         try
         {
            if( trace )
               log.trace("Invoking on target="+externalURL);
            Object rtn = Util.invoke(externalURL, mi);
            HARMIResponse rsp = (HARMIResponse) rtn;
            if (rsp.newReplicants != null)
            {
               setTargets(rsp.newReplicants);
               currentViewId = rsp.currentViewId;
            }
            return rsp.response;
         }
         catch(GenericClusteringException e)
         {
            // this is a generic clustering exception that contain the
            // completion status: usefull to determine if we are authorized
            // to re-issue a query to another node
            //               
            if( e.getCompletionStatus() != e.COMPLETED_NO )
               throw new ServerException("Cannot proceed beyond target="+externalURL, e);
         }
         catch(Throwable e)
         {
            if( trace )
               log.trace("Invoke failed, target="+externalURL, e);
         }
         // If we reach here, this means that we must fail-over
         remoteTargetHasFailed(target);
         target = (String) getRemoteTarget();
         externalURL = Util.resolveURL(target);
         failoverCounter ++;
      }
      // if we get here this means list was exhausted
      throw new java.rmi.RemoteException("Service unavailable.");
   }

   /** Externalize this instance.
   */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   { 
      out.writeObject(targets);
      out.writeObject(loadBalancePolicy);
   }

   /** Un-externalize this instance.
   */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      this.targets = (ArrayList)in.readObject();
      this.loadBalancePolicy = (LoadBalancePolicy) in.readObject();
      this.trace = log.isTraceEnabled();
      if( trace )
         log.trace("Init, targets: "+targets+", policy="+loadBalancePolicy);
   }
}
