/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;


import java.lang.reflect.Method;
import java.util.ArrayList;

import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.Invocation;

/**
 *
 *
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @version $Revision: 1.1.1.1 $
 *
 *   <p><b>Revisions:</b>
 *
 *   <p><b>20010831 Bill Burke:</b>
 *   <ul>
 *   <li> First import of sources
 *   </ul>
 */

public class HARMIClient 
   implements HARMIProxy, java.lang.reflect.InvocationHandler, java.io.Serializable
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected String key = null;
   protected ArrayList targets = null;
   protected LoadBalancePolicy loadBalancePolicy;
   protected transient long currentViewId = 0;
   protected transient Object local = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public HARMIClient() {}
   
   public HARMIClient(ArrayList targets, LoadBalancePolicy policy, String key)
   {
      this.targets = targets;
      this.loadBalancePolicy = policy;
      this.loadBalancePolicy.init(this);      
      this.key = key;
   }
   
   public HARMIClient(ArrayList targets,
                       LoadBalancePolicy policy,
                       String key,
                       Object local)
   {
      this.targets = targets;
      this.loadBalancePolicy = policy;
      this.loadBalancePolicy.init(this);      
      this.key = key;
      this.local = local;
   }
   
   // Public --------------------------------------------------------
   
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


   public Method findLocalMethod(Method method, Object[] args) throws Exception
   {
      return method;
   }
   
   
   public Object invokeRemote(Object proxy, Method method, Object[] args) throws Throwable
   {
      HARMIServer target = (HARMIServer)getRemoteTarget();      
      while (target != null)
      {
         try
         {
            MarshalledInvocation mi = new MarshalledInvocation(null, method, args, null, null, null);
            mi.setObjectName (""); //FIXME: Fake value! Bill's optimisations regarding MI make the hypothesis
                                   // that ObjectName is always here otherwise the writeExternal code of MI
                                   // "out.writeInt(payload.size() - 3);" is wrong
            HARMIResponse rsp = target.invoke(currentViewId, mi);
            if (rsp.newReplicants != null)
            {
               // System.out.println("new set of replicants" + rsp.newReplicants + " : view : " + rsp.currentViewId); 
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
         // If we reach here, this means that we must fail-over
         remoteTargetHasFailed(target);
         target = (HARMIServer)getRemoteTarget();
      }
      // if we get here this means list was exhausted
      throw new java.rmi.RemoteException("Service unavailable.");
      
   }

   // HARMIProxy implementation ----------------------------------------------
   
   public boolean isLocal()
   {
      return local != null;
   }
   
   // InvocationHandler implementation ----------------------------------------------   
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // The isLocal call is handled by the proxy
      //
      if (method.getName().equals("isLocal") && (args == null || args.length == 0))
      {
         return method.invoke(this, args);
      }
      
      // we try to optimize the call locally first
      //
      if (local != null)
      {
         try
         {
            Method localMethod = findLocalMethod(method, args);
            return localMethod.invoke(local, args);
         }
         catch (java.lang.reflect.InvocationTargetException ite)
         {
            throw ite.getTargetException();
         }
      }
      else
      {
         return invokeRemote(null, method, args);
      }
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
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

   // Private -------------------------------------------------------
   
   private void readObject (java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException
   {
      this.key = (String)stream.readUTF();
      this.targets = (ArrayList)stream.readObject();
      this.loadBalancePolicy = (LoadBalancePolicy)stream.readObject();      
      HARMIServer server = (HARMIServer)HARMIServer.rmiServers.get(key);
      
      this.loadBalancePolicy.init(this);
      
      if (server != null)
      {
         synchronized (targets)
         {
            try
            {
               targets = (ArrayList)server.getReplicants();
               local = server.getLocal();
            }
            catch (Exception ignored)
            {}
         }
      }
   }
   private void writeObject (java.io.ObjectOutputStream stream) throws java.io.IOException
   {
      stream.writeUTF(key);
      stream.writeObject(targets);
      stream.writeObject(loadBalancePolicy);
   }
   
   // Inner classes -------------------------------------------------  
   
}
