/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.jrmp.interfaces;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.rmi.ServerException;
import java.rmi.NoSuchObjectException;
import java.rmi.MarshalledObject;

import java.util.ArrayList;
import java.util.HashSet;

import javax.transaction.SystemException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerInterceptor;

import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;

/**
*
* JRMPInvokerProxy, local to the proxy and is capable of delegating to local and JRMP implementations
* 
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.4 $
*
* <p><b>2001/11/19: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
* <p><b>2002/04/08: Sacha Labourey</b>
* <ol>
*   <li>Pass a value with the invocation that allows any server side interceptor to know
 *      when a call result from a failover (and the number of tries)</li>
* </ol>
*/
public class JRMPInvokerProxyHA extends JRMPInvokerProxy
   implements Externalizable
{
   // Public --------------------------------------------------------
   protected ArrayList targets = null;
   protected LoadBalancePolicy loadBalancePolicy;
   protected transient long currentViewId = 0;
   
   public static final HashSet colocation = new HashSet();
   
   public JRMPInvokerProxyHA()
   {
   }
   
   public JRMPInvokerProxyHA(ArrayList targets, LoadBalancePolicy policy)
   {
      this.targets = targets;
      this.loadBalancePolicy = policy;
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
      }
   }
   
   public Object getRemoteTarget()
   {
      //      System.out.println("number of targets: " + targets.size());
      if (targets.size() == 0)
      {
         return null;
      }
      synchronized (targets)
      {
         return loadBalancePolicy.chooseTarget(targets);
      }
   }
   
   public void remoteTargetHasFailed(Object target)
   {
      removeDeadTarget(target);
   }
   
   
   protected void removeDeadTarget(Object target)
   {
      //System.out.println("Size before : " + Integer.toString(targets.length));
      if (targets != null)
      {
         synchronized(targets)
         {
            //System.out.println("removeDeadTarget has been called");
            int length = targets.size();
            for (int i=0; i<length; ++i)
            {
               if (targets.get(i) == target)
               {
                  targets.remove(i);
                  return;
               }
            }
         }
      }
      // nothing found
   }
   
   /**
   * Returns wether we are local to the originating container or not. 
   */
   public boolean isLocal(Invocation invocation) 
   {
      return colocation.contains(invocation.getObjectName());
   }
   
   /**
   * The invocation on the delegate, calls the right invoker.  Remote if we are remote, local if we
   * are local. 
   */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // we give the opportunity, to any server interceptor, to know if this a
      // first invocation to a node or if it is a failovered call
      //
      int failoverCounter = 0;
      invocation.setValue ("FAILOVER_COUNTER", new Integer(failoverCounter), invocation.AS_IS);

      // ROC PINPOINT EMK BEGIN
      invocation.setValue( "roc.requestinfo",
			   roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo(),
			   invocation.AS_IS );
      // ROC PINPOINT EMK END

      // optimize if calling another bean in same EJB-application
      if (isLocal(invocation)) {
         return InvokerInterceptor.getLocal().invoke(invocation);
      }
      else 
      {
         // We are going to go through a Remote invocation, switch to a Marshalled Invocation
         MarshalledInvocation mi = new MarshalledInvocation(invocation);
         
         // Set the transaction propagation context
         mi.setTransactionPropagationContext(getTransactionPropagationContext());
         mi.setValue("CLUSTER_VIEW_ID", new Long(currentViewId));
         Invoker target = (Invoker)getRemoteTarget();
         while (target != null)
         {
            try
            {
               HARMIResponse rsp = (HARMIResponse)((MarshalledObject)target.invoke(mi)).get();
               
	       // ROC PINPOINT EMK BEGIN
	       roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo().setSeqNum( rsp.request_seqnum );
	       // ROC PINPOINT EMK END
	       
	       if (rsp.newReplicants != null)
               {
                  //               System.out.println("new set of replicants");
                  setTargets(rsp.newReplicants);
                  currentViewId = rsp.currentViewId;
               }
               return rsp.response;
            }
            catch (java.rmi.ConnectException ce)
            {
            }
            catch (java.rmi.ConnectIOException cioe)
            {
            }
            catch (java.rmi.NoSuchObjectException nsoe)
            {
            }
            catch (java.rmi.UnmarshalException ue ) 
            {
            }
            catch (java.rmi.UnknownHostException uhe)
            {
            }
            catch (org.jboss.ha.framework.interfaces.GenericClusteringException gce)
            {
               // this is a generic clustering exception that contain the
               // completion status: usefull to determine if we are authorized
               // to re-issue a query to another node
               //               
               if (gce.getCompletionStatus () != gce.COMPLETED_NO)
                  throw new java.rmi.RemoteException (gce.getMessage ());
            }
            // If we reach here, this means that we must fail-over
            remoteTargetHasFailed(target);
            target = (Invoker)getRemoteTarget();
            
            failoverCounter++;
            mi.setValue ("FAILOVER_COUNTER", new Integer(failoverCounter), invocation.AS_IS);
         }
         // if we get here this means list was exhausted
         throw new java.rmi.RemoteException("Service unavailable.");
      }
   }
   
   /**
   *  Externalize this instance.
   *
   *  If this instance lives in a different VM than its container
   *  invoker, the remote interface of the container invoker is
   *  not externalized.
   */
   public void writeExternal(final ObjectOutput out)
   throws IOException
   { 
      out.writeObject(targets);
      out.writeObject(loadBalancePolicy);
   }
   
   /**
   *  Un-externalize this instance.
   *
   *  We check timestamps of the interfaces to see if the instance is in the original VM of creation
   */
   public void readExternal(final ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      targets = (ArrayList)in.readObject();
      this.loadBalancePolicy = (LoadBalancePolicy)in.readObject();      
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
