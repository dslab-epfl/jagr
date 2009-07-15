/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.jrmp.interfaces;






import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.MarshalledObject;
import java.rmi.NoSuchObjectException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.transaction.SystemException;
import javax.transaction.TransactionRolledbackException;
import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.FamilyClusterInfo;
import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.InvokerProxyHA;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.PayloadKey;
import org.jboss.invocation.local.LocalInvoker;
import org.jboss.tm.TransactionPropagationContextFactory;

/**
*
* JRMPInvokerProxy, local to the proxy and is capable of delegating to local and JRMP implementations
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.3 $
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
* <p><b>2002/08/24: Sacha Labourey</b>
* <ol>
*   <li>Externalized the repository of known targets so that all identical proxies
*      reference the same (up-to-date) data: avoid replication of identical data and
       converge quicker in case of a node dies (and we a collection or proxies for example)</li>
* </ol>
*/
public class JRMPInvokerProxyHA
   extends JRMPInvokerProxy
   implements InvokerProxyHA, Externalizable
{
   // Public --------------------------------------------------------
   //protected ArrayList targets = null;
   protected LoadBalancePolicy loadBalancePolicy;
   protected String proxyFamilyName = null;

   FamilyClusterInfo familyClusterInfo = null;
   //protected transient long currentViewId = 0;

   public static final HashSet colocation = new HashSet();
   public static final java.util.WeakHashMap txFailoverAuthorizations = new java.util.WeakHashMap ();

   public JRMPInvokerProxyHA() {}

   public JRMPInvokerProxyHA(ArrayList targets, LoadBalancePolicy policy,
                             String proxyFamilyName, long viewId)
   {
      this.familyClusterInfo = ClusteringTargetsRepository.initTarget (proxyFamilyName, targets, viewId);
      this.loadBalancePolicy = policy;
      this.proxyFamilyName = proxyFamilyName;
   }

   public void updateClusterInfo (ArrayList targets, long viewId)
   {
      if (familyClusterInfo != null)
         this.familyClusterInfo.updateClusterInfo (targets, viewId);
   }

   public Object getRemoteTarget()
   {
      return loadBalancePolicy.chooseTarget(this.familyClusterInfo);
   }

   public void remoteTargetHasFailed(Object target)
   {
      removeDeadTarget(target);
   }

   protected void removeDeadTarget(Object target)
   {
      //System.out.println("Removing a dead target: Size before : " + Integer.toString(this.familyClusterInfo.getTargets ().size()));
      if (this.familyClusterInfo != null)
         this.familyClusterInfo.removeDeadTarget (target);
   }

   protected int totalNumberOfTargets ()
   {
      if (this.familyClusterInfo != null)
         return this.familyClusterInfo.getTargets ().size ();
      else
         return 0;
   }

   protected void resetView ()
   {
      this.familyClusterInfo.resetView ();
   }

   /**
   * Returns wether we are local to the originating container or not.
   */
   public boolean isLocal(Invocation invocation)
   {
      return colocation.contains(invocation.getObjectName());
   }
   
   public boolean txContextAllowsFailover (Invocation invocation)
   {
      javax.transaction.Transaction tx = invocation.getTransaction();
      if (tx != null)
      {
         synchronized (tx)
         {
            return ! txFailoverAuthorizations.containsKey (tx);               
         }
      }
      else
      {
         return true;
      }
   }
   
   public void invocationHasReachedAServer (Invocation invocation)
   {
      javax.transaction.Transaction tx = invocation.getTransaction();
      if (tx != null)
      {
         synchronized (tx)
         {
            txFailoverAuthorizations.put (tx, null);               
         }
      }
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
      invocation.setValue ("FAILOVER_COUNTER", new Integer(failoverCounter), PayloadKey.AS_IS);


      // ROC PINPOINT EMK BEGIN            ---  5 LINES
      if( roc.config.ROCConfig.ENABLE_PINPOINT &&
	  roc.config.ROCConfig.ENABLE_PINPOINT_TRACING_RMI ) {
	  invocation.setValue( "roc.requestinfo",
			       roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo(),
			       PayloadKey.AS_IS );
      }
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
         mi.setValue("CLUSTER_VIEW_ID", new Long(this.familyClusterInfo.getCurrentViewId ()));
         Invoker target = (Invoker)getRemoteTarget();
         
         boolean failoverAuthorized = true;
         
         while (target != null && failoverAuthorized)
         {                        
            boolean definitivlyRemoveNodeOnFailure = true;
            try
            {
		Object rtnObj = null;

		// BEGIN ROC PINPOINT EMK BEGIN
		if( roc.config.ROCConfig.ENABLE_PINPOINT && 
		    roc.config.ROCConfig.ENABLE_PINPOINT_TRACING_RMI ) {
		    try {
			rtnObj = target.invoke(mi);
		    }
		    catch( java.rmi.RemoteException re ) {
			roc.jboss.pinpoint.PinpointWrappedJRMPException ppe;
			if( re instanceof roc.jboss.pinpoint.PinpointWrappedJRMPException ) {
			    ppe = (roc.jboss.pinpoint.PinpointWrappedJRMPException)re;
			}
			else {
			    ppe = (roc.jboss.pinpoint.PinpointWrappedJRMPException)re.getCause();
			}

			roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo().setSeqNum( ppe.request_seqnum );
			if( ppe.cause instanceof Exception ) {
			    throw (Exception)ppe.cause;
			}
			else if( ppe.cause instanceof Error ) {
			    throw (Error)ppe.cause;
			}			
		    }
		    catch( Exception e ) {
			System.err.println( "ACK ACK ACK!!! We just lost a request trace, because a call threw an exception and wasn't wrapped in a PinpointWrappedJRMPException!!!. The exception is " + e.getMessage() + " and the request trace info is " + roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo().getRequestId() );
			throw e;
		    }
		}
		else {
		    // END ROC PINPOINT EMK END
		    // original code
		    rtnObj = target.invoke( mi );
		}

               HARMIResponse rsp = null;
               if (rtnObj instanceof MarshalledObject)
               {
                  rsp = (HARMIResponse)((MarshalledObject)rtnObj).get();
               }
               else
               {
                  rsp = (HARMIResponse)rtnObj;
               }

	       // ROC PINPOINT EMK BEGIN           ---  4 LINES
	       if( roc.config.ROCConfig.ENABLE_PINPOINT &&
		   roc.config.ROCConfig.ENABLE_PINPOINT_TRACING_RMI ) {
		   roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo().setSeqNum( rsp.request_seqnum );
	       }
	       // ROC PINPOINT EMK END


               if (rsp.newReplicants != null)
               {
                  // System.out.println("new set of replicants" + rsp.newReplicants.size () + " : view : " + rsp.currentViewId + " : Previous : " + this.familyClusterInfo.getCurrentViewId () + " (me = " + this + ")");
                  //for (int aa = 0; aa<rsp.newReplicants.size(); aa++) System.out.println("Member: " + aa + " : " + rsp.newReplicants.get(aa));
                  updateClusterInfo (rsp.newReplicants, rsp.currentViewId);
               }
               //else System.out.println("Static set of replicants: " + this.familyClusterInfo.getCurrentViewId () + " (me = " + this + ")");
               
               invocationHasReachedAServer (invocation);

               return rsp.response;
            }
            catch (java.net.ConnectException nce)
            {
            }
            catch (java.net.UnknownHostException uhe)
            {
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
            catch (GenericClusteringException gce)
            {
               // this is a generic clustering exception that contain the
               // completion status: usefull to determine if we are authorized
               // to re-issue a query to another node
               //
               if (gce.getCompletionStatus () == GenericClusteringException.COMPLETED_NO)
               {
                  // we don't want to remove the node from the list of failed
                  // node UNLESS there is a risk to indefinitively loop
                  //
                  if (totalNumberOfTargets() >= failoverCounter)
                  {
                     if (!gce.isDefinitive ())
                        definitivlyRemoveNodeOnFailure = false;
                  }
               }
               else
               {
                  invocationHasReachedAServer (invocation);
                  throw new java.rmi.RemoteException (gce.getMessage ());
               }
            }
            catch (ServerException ex)
            {
               //Why do NoSuchObjectExceptions get ignored for a retry here
               //unlike in the non-HA case?
               invocationHasReachedAServer (invocation);
               if (ex.detail instanceof TransactionRolledbackException)
               {                  
                  throw (TransactionRolledbackException) ex.detail;
               }
               throw ex;
            }
            catch (Exception whatever)
            {
               invocationHasReachedAServer (invocation);
               throw whatever;
            }
            
            // If we reach here, this means that we must fail-over
            remoteTargetHasFailed(target);
            if (!definitivlyRemoveNodeOnFailure)
            {
               resetView ();
            }

            failoverAuthorized = txContextAllowsFailover (invocation);            
            target = (Invoker)getRemoteTarget();            

            failoverCounter++;
            mi.setValue ("FAILOVER_COUNTER", new Integer(failoverCounter), PayloadKey.AS_IS);
         }
         // if we get here this means list was exhausted
         if (failoverAuthorized)
            throw new java.rmi.RemoteException("Service unavailable.");
         else
            throw new java.rmi.RemoteException("Service unavailable (failover not possible inside a user transaction).");
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
      ArrayList targets = this.familyClusterInfo.getTargets();
      long vid = this.familyClusterInfo.getCurrentViewId ();
      targets.trimToSize();
      out.writeObject(targets);
      out.writeObject(this.loadBalancePolicy);
      out.writeObject (this.proxyFamilyName);
      out.writeLong (vid);
   }

   /**
   *  Un-externalize this instance.
   *
   *  We check timestamps of the interfaces to see if the instance is in the original VM of creation
   */
   public void readExternal(final ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      ArrayList targets = (ArrayList)in.readObject();
      this.loadBalancePolicy = (LoadBalancePolicy)in.readObject();
      this.proxyFamilyName = (String)in.readObject();
      long vid = in.readLong ();

      // keep a reference on our family object
      //
      this.familyClusterInfo = ClusteringTargetsRepository.initTarget (this.proxyFamilyName, targets, vid);
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
