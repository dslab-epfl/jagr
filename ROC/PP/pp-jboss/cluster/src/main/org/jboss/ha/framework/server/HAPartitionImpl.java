/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.StringRefAddr;
import javax.naming.InitialContext;
import javax.management.MBeanServer;

import org.javagroups.blocks.MethodCall;
import org.javagroups.MergeView;

import org.jboss.invocation.MarshalledValueInputStream;
import org.jboss.invocation.MarshalledValueOutputStream;

import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.DistributedMap;
import org.jboss.ha.framework.interfaces.DistributedState;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAPartition.HAPartitionStateTransfer;
import org.jboss.ha.framework.interfaces.HAPartition.HAMembershipListener;

import org.jboss.naming.NonSerializableFactory;
import org.jboss.logging.Logger;

/**
 * This class is an abstraction class for a JavaGroups RPCDispatch and JChannel.
 * It is a default implementation of HAPartition for the <a href="http://www.javagroups.com/">JavaGroups</A> framework
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b><br>
 */

public class HAPartitionImpl 
   extends org.javagroups.blocks.RpcDispatcher 
   implements org.javagroups.MessageListener, org.javagroups.MembershipListener, HAPartition
{
   // Constants -----------------------------------------------------
   
   final org.javagroups.blocks.MethodLookup method_lookup_clos=new org.javagroups.blocks.MethodLookupClos();
   
   // Attributes ----------------------------------------------------
   
   protected HashMap rpcHandlers = new HashMap();
   protected HashMap stateHandlers = new HashMap();
   protected ArrayList listeners = new ArrayList();
   protected Vector members = null;
   protected Vector otherMembers = null;
   
   protected String partitionName;
   protected String nodeName;
   protected int timeout = 60000;
   
   protected org.javagroups.JChannel channel;
   
   protected DistributedReplicantManagerImpl replicantManager;
   protected DistributedStateImpl dsManager;
   
   protected Logger log;
   
   protected long currentViewId = -1;
   
   protected MBeanServer server;
   
   // Static --------------------------------------------------------
   
   /**
    * Creates an object from a byte buffer
    */
   public static Object objectFromByteBuffer (byte[] buffer) throws Exception
   {
      if(buffer == null) 
         return null;

      ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
      MarshalledValueInputStream mvis = new MarshalledValueInputStream(bais);
      return mvis.readObject();
   }
   
   /**
    * Serializes an object into a byte buffer.
    * The object has to implement interface Serializable or Externalizable
    */
   public static byte[] objectToByteBuffer (Object obj) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      MarshalledValueOutputStream mvos = new MarshalledValueOutputStream(baos);
      mvos.writeObject(obj);
      mvos.flush();
      return baos.toByteArray();
   }

    // Constructors --------------------------------------------------
       
   public HAPartitionImpl(String partitionName, org.javagroups.JChannel channel, boolean deadlock_detection, MBeanServer server) throws Exception
   {
      this(partitionName, channel, deadlock_detection);
      this.server = server;
   }
   
   public HAPartitionImpl(String partitionName, org.javagroups.JChannel channel, boolean deadlock_detection) throws Exception
   {
      super(channel, null, null, new Object(), false); // init RpcDispatcher with a fake target object
      this.log = Logger.getLogger(HAPartitionImpl.class.getName() + "." + partitionName);
      this.channel = channel;
      this.partitionName = partitionName;
   }
   
    // Public --------------------------------------------------------
   
   public void init() throws Exception
   {
      log.info("Initializing");
      
      // Subscribe to dHA events comming generated by the org.javagroups. protocol stack
      //
      log.debug("setMembershipListener");
      setMembershipListener(this);
      log.debug("setMessageListener");
      setMessageListener(this);
      
      // Create the DRM and link it to this HAPartition
      //
      log.debug("create replicant manager");
      this.replicantManager = new DistributedReplicantManagerImpl(this, this.server);
      log.debug("init replicant manager");
      this.replicantManager.init();
      log.debug("bind replicant manager");
      
      // Create the DS and link it to this HAPartition
      //
      log.debug("create distributed state");
      this.dsManager = new DistributedStateImpl(this, this.server);
      log.debug("init distributed state service");
      this.dsManager.init();
      log.debug("bind distributed state service");

      
      // Bind ourself in the public JNDI space
      //
      Context ctx = new InitialContext();
      this.bind("/HAPartition/" + partitionName, this, HAPartitionImpl.class, ctx);
      
      log.debug("done initing.");
   }
   
   public void startPartition() throws Exception
   {
      // get current JG group properties
      //
      log.debug("get nodeName");
      this.nodeName = channel.getLocalAddress().toString();
      log.debug("Get current members");
      org.javagroups.View view = channel.getView();
      this.members = (Vector)view.getMembers().clone();
      log.info("Number of cluster members: " + members.size());
      for(int m = 0; m > members.size(); m ++)
      {
         Object node = members.get(m);
         log.debug(node);
      }
      // Keep a list of other members only for "exclude-self" RPC calls
      //
      this.otherMembers = (Vector)view.getMembers().clone();
      this.otherMembers.remove (channel.getLocalAddress());
      log.info ("Other members: " + this.otherMembers.size ());
      
      // Update the initial view id
      //
      this.currentViewId = view.getVid().getId();

      // We must now syncrhonize new state transfer subscriber
      //
      boolean rc = channel.getState(null, 8000);
      if (rc)
         log.debug("State was retrieved successfully");
      else
         log.debug("State could not be retrieved, (must be first member of group)");
      
      // We start now able to start our DRM and DS
      //
      this.replicantManager.start();
      this.dsManager.start();
   }
   
   public void closePartition() throws Exception
   {
      log.info("Closing partition " + partitionName);
      // Stop the DRM and DS services
      //
      try {
         this.replicantManager.stop();
      }
      catch (Exception e) {
         log.error("operation failed", e);
      }
      
      try {
         this.dsManager.stop();
      }
      catch (Exception e) {
         log.error("operation failed", e);
      }
      
      try {
         channel.close();
      }
      catch (Exception e) {
         log.error("operation failed", e);
      }

      String boundName = "/HAPartition/" + partitionName;

      InitialContext ctx = new InitialContext();
      try {
         
         ctx.unbind(boundName);
      }
      finally {
         ctx.close();
      }
      NonSerializableFactory.unbind (boundName);
      
      log.info("Partition " + partitionName + " closed.");
   }
   
   // org.javagroups.MessageListener implementation ----------------------------------------------

   // MessageListener methods
   //
   public byte[] getState()
   {
      boolean debug = log.isDebugEnabled();
      
      log.debug("getState called.");
      try
      {
         // we now get the sub-state of each HAPartitionStateTransfer subscribers and
         // build a "macro" state
         //
         HashMap state = new HashMap();
         java.util.Iterator keys = stateHandlers.keySet().iterator();
         while (keys.hasNext())
         {
            String key = (String)keys.next();
            HAPartition.HAPartitionStateTransfer subscriber = (HAPartition.HAPartitionStateTransfer)stateHandlers.get(key);
            if (debug)
               log.debug("getState for " + key);
            state.put(key, subscriber.getCurrentState());
         }
         return objectToByteBuffer(state);
      }
      catch (Exception ex)
      {
         log.error("GetState failed", ex);
      }
      return null;
   }
   
   public void setState(byte[] obj)
   {
      try
      {
         log.debug("setState called");
         if (obj == null)
         {
            log.debug("state is null");
            return;
         }
         
         HashMap state = (HashMap)objectFromByteBuffer(obj);
         java.util.Iterator keys = state.keySet().iterator();
         while (keys.hasNext())
         {
            String key = (String)keys.next();
            log.debug("setState for " + key);
            Object someState = state.get(key);
            HAPartition.HAPartitionStateTransfer subscriber = (HAPartition.HAPartitionStateTransfer)stateHandlers.get(key);
            if (subscriber != null)
            {
               subscriber.setCurrentState((java.io.Serializable)someState);
            }
            else
            {
               log.debug("There is no stateHandler for: " + key);
            }
         }
      }
      catch (Exception ex)
      {
         log.error("setState failed", ex);
      }
   }
   
   public void receive(org.javagroups.Message msg)
   { /* complete */}
   
   // org.javagroups.MembershipListener implementation ----------------------------------------------
   
   public void suspect(org.javagroups.Address suspected_mbr) { log.info("Suspected member: " + suspected_mbr); /* complete */  }
   
   public void block() {}
   
   public void viewAccepted(org.javagroups.View newView)
   {
      boolean debug = log.isDebugEnabled();
      
      try
      {
         // we update the view id
         //
         this.currentViewId = newView.getVid().getId();
         if (debug)
            log.info("New cluster view: " + currentViewId + " (" + newView.getMembers () + ")");
         
         // Keep a list of other members only for "exclude-self" RPC calls
         //
         this.otherMembers = (Vector)newView.getMembers().clone();
         this.otherMembers.remove (channel.getLocalAddress());
         
         if (this.members == null)
         {
            // Initial viewAccepted
            //
            this.members = (Vector)newView.getMembers().clone();
            log.debug("ViewAccepted: initial members set");
            return;
         }
         Vector oldMembers = this.members;
         Vector allMembers = newView.getMembers();
         if (debug)
            log.debug("membership changed from " + this.members.size() + " to " + allMembers.size());
         Vector deadMembers = getDeadMembers(oldMembers, allMembers);
         Vector newMembers = getNewMembers(oldMembers, allMembers);
         this.members = (Vector)allMembers.clone();
         
         
         // Broadcast the new view to the view change listeners
         //
         synchronized(this.listeners)
         {
            // if the new view occurs because of a merge, we first inform listeners of the merge
            //
            boolean isAMerge = (newView instanceof MergeView);
            Vector originatingGroups = null;
            if (isAMerge)
               originatingGroups = ((MergeView)newView).getSubgroups ();
            
            for (int i = 0; i < listeners.size(); i++)
            {
               try
               {
                  HAPartition.HAMembershipListener aListener = (HAPartition.HAMembershipListener)listeners.get(i);
                  if (isAMerge && (aListener instanceof HAPartition.HAMembershipExtendedListener) )
                     ((HAPartition.HAMembershipExtendedListener)aListener).membershipChangedDuringMerge (
                        deadMembers, newMembers, allMembers, originatingGroups);
                  else
                     aListener.membershipChanged(deadMembers, newMembers, allMembers);
               }
               catch (Exception e)
               {
                  // a problem in a listener should not prevent other members to receive the new view
                  log.error("a problem in a listener should not prevent other members to receive the new view", e);
               }
            }
         }
      }
      catch (Exception ex)
      {
         log.error("ViewAccepted failed", ex);
      }
   }
   
   // HAPartition implementation ----------------------------------------------
   
   public String getNodeName()
   {
      return nodeName;
   }
   
   public String getPartitionName()
   {
      return partitionName;
   }
   
   public DistributedReplicantManager getDistributedReplicantManager()
   {
      return replicantManager;
   }
   
   public DistributedState getDistributedStateService()
   {
      return this.dsManager;
   }
   public DistributedMap getDistributedMapService()
   {
      return this.dsManager;
   }

   public long getCurrentViewId()
   {
      return this.currentViewId;
   }
   
   public Vector getCurrentView()
   {
      // we don't directly return this.members because we want to 
      // hide JG objects
      //
      Vector result = new Vector (this.members.size ());
      if (this.members != null)
      {
         for (int i = 0; i < this.members.size (); i++)
            result.add (this.members.elementAt (i).toString ());
      }
      
      return result;      
   }

   // ***************************
   // ***************************
   // RPC multicast communication
   // ***************************
   // ***************************
   //
   public void registerRPCHandler(String objName, Object subscriber)
   {
      rpcHandlers.put(objName, subscriber);
   }
   
   public void unregisterRPCHandler(String objName, Object subscriber)
   {
      rpcHandlers.remove(objName);
   }
      
   /**
    * This function is an abstraction of RpcDispatcher.
    */
   public ArrayList callMethodOnCluster(String objName, String methodName, Object[] args, boolean excludeSelf) throws Exception
   {
      ArrayList rtn = new ArrayList();
      MethodCall m = new MethodCall(objName + "." + methodName, args);
      org.javagroups.util.RspList rsp = null;
      
      if (excludeSelf)
      {
         rsp = this.callRemoteMethods(this.otherMembers, m, org.javagroups.blocks.GroupRequest.GET_ALL, timeout);
      }
      else
         rsp = this.callRemoteMethods(null, m, org.javagroups.blocks.GroupRequest.GET_ALL, timeout);
         
      if (rsp != null)
      {
         for (int i = 0; i < rsp.size(); i++)
         {
            Object item = rsp.elementAt(i);
            if (item instanceof org.javagroups.util.Rsp)
            {
               item = ((org.javagroups.util.Rsp)item).getValue();
            }
            rtn.add(item);
         }
      }

      return rtn;
   }
   
   /**
    * This function is an abstraction of RpcDispatcher for asynchronous messages
    */
   public void callAsynchMethodOnCluster(String objName, String methodName, Object[] args, boolean excludeSelf) throws Exception
   {
      MethodCall m = new MethodCall(objName + "." + methodName, args);
      if (excludeSelf)
         this.callRemoteMethods(this.otherMembers, m, org.javagroups.blocks.GroupRequest.GET_NONE, timeout);
      else
         this.callRemoteMethods(null, m, org.javagroups.blocks.GroupRequest.GET_NONE, timeout);
   }
   
   // *************************
   // *************************
   // State transfer management
   // *************************
   // *************************
   //      
   public void subscribeToStateTransferEvents(String objectName, HAPartitionStateTransfer subscriber)
   {
      stateHandlers.put(objectName, subscriber);
   }
   
   public void unsubscribeFromStateTransferEvents(String objectName, HAPartitionStateTransfer subscriber)
   {
      stateHandlers.remove(objectName);
   }
   
   // *************************
   // *************************
   // Group Membership listeners
   // *************************
   // *************************
   //   
   public void registerMembershipListener(HAMembershipListener listener)
   {
      synchronized(this.listeners)
      {
         this.listeners.add(listener);
      }
   }
   
   public void unregisterMembershipListener(HAMembershipListener listener)
   {
      synchronized(this.listeners)
      {
         this.listeners.remove(listener);
      }
   }
   
   // org.javagroups.RpcDispatcher overrides ---------------------------------------------------
   
   /**
    * Message contains MethodCall. Execute it against *this* object and return result.
    * Use MethodCall.Invoke() to do this. Return result.
    *
    * This overrides RpcDispatcher.Handle so that we can dispatch to many different objects.
    * @param req The org.javagroups. representation of the method invocation
    * @return The serializable return value from the invocation
    */
   public Object handle(org.javagroups.Message req)
   {
      Object body = null;
      Object retval = null;
      MethodCall  method_call = null;
      
      if (log.isDebugEnabled())
         log.debug("Partition " + partitionName + " received msg");
      if(req == null || req.getBuffer() == null)
      {
         log.warn("RpcProtocol.Handle(): message or message buffer is null !");
         return null;
      }
      
      try
      {
         body=org.javagroups.util.Util.objectFromByteBuffer(req.getBuffer());
      }
      catch(Exception e)
      {
         log.warn("RpcProtocol.Handle(): " + e);
         return null;
      }
      
      if(body == null || !(body instanceof MethodCall))
      {
         log.warn("RpcProtocol.Handle(): message does not contain a MethodCall object !");
         return null;
      }
      
      // get method call informations
      //
      method_call=(MethodCall)body;
      String methodName = method_call.getName();      
      
      if (log.isDebugEnabled()) log.debug("pre methodName: " + methodName);
      
      int idx = methodName.lastIndexOf('.');
      String handlerName = methodName.substring(0, idx);
      String newMethodName = methodName.substring(idx + 1);
      
      if (log.isDebugEnabled()) 
      {
         log.debug("handlerName: " + handlerName + " methodName: " + newMethodName);
         log.debug("Handle: " + methodName);
      }
      
      // prepare method call
      //
      method_call.setName(newMethodName);
      Object handler = rpcHandlers.get(handlerName);
      
      // Invoke it
      //
      retval=method_call.invoke(handler, method_lookup_clos);
      
      return retval;
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * Helper method that binds the partition in the JNDI tree.
    * @param jndiName Name under which the object must be bound
    * @param who Object to bind in JNDI
    * @param classType Class type under which should appear the bound object
    * @param ctx Naming context under which we bind the object
    * @throws Exception Thrown if a naming exception occurs during binding
    */   
   protected void bind(String jndiName, Object who, Class classType, Context ctx) throws Exception
   {
      // Ah ! This service isn't serializable, so we use a helper class
      //
      NonSerializableFactory.bind(jndiName, who);
      javax.naming.Name n = ctx.getNameParser("").parse(jndiName);
      while (n.size () > 1)
      {
         String ctxName = n.get (0);
         try
         {
            ctx = (Context)ctx.lookup (ctxName);
         }
         catch (javax.naming.NameNotFoundException e)
         {
            log.debug ("creating Subcontext" + ctxName);
            ctx = ctx.createSubcontext (ctxName);
         }
         n = n.getSuffix (1);
      }

      // The helper class NonSerializableFactory uses address type nns, we go on to
      // use the helper class to bind the service object in JNDI
      //
      StringRefAddr addr = new StringRefAddr("nns", jndiName);
      javax.naming.Reference ref = new javax.naming.Reference(classType.getName (), addr, NonSerializableFactory.class.getName (), null);
      ctx.rebind (n.get (0), ref);
   }
   
   /**
    * Helper method that returns a vector of dead members from two input vectors: new and old vectors of two views.
    * @param oldMembers Vector of old members
    * @param newMembers Vector of new members
    * @return Vector of members that have died between the two views
    */   
   protected Vector getDeadMembers(Vector oldMembers, Vector newMembers)
   {
      boolean debug = log.isDebugEnabled();
      
      Vector dead = new Vector();
      for (int i=0; i<oldMembers.size ();i++)
      {
         if (debug) 
            log.debug("is node " + oldMembers.elementAt(i).toString() + "dead?");
         if (!newMembers.contains(oldMembers.elementAt (i)))
         {
            if (debug) 
               log.debug("node " + oldMembers.elementAt(i).toString()  + "is dead");
            dead.add(oldMembers.elementAt (i));
         }
         else
         {
            if (debug) 
               log.debug("node " + oldMembers.elementAt(i).toString()  + "is NOT dead");
         }
      }
      
      return dead;
   }
   
   /**
    * Helper method that returns a vector of new members from two input vectors: new and old vectors of two views.
    * @param oldMembers Vector of old members
    * @param allMembers Vector of new members
    * @return Vector of members that have joined the partition between the two views
    */   
   protected Vector getNewMembers(Vector oldMembers, Vector allMembers)
   {
      Vector newMembers = new Vector();
      for (int i=0; i<allMembers.size();i++)
         if (!oldMembers.contains (allMembers.elementAt (i)))
            newMembers.add (allMembers.elementAt (i));
      return newMembers;
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}