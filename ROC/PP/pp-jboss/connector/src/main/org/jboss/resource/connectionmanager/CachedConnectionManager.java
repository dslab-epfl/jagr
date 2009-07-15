
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.connectionmanager;



import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.resource.ResourceException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import org.jboss.logging.Logger;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.jboss.system.Service;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.ejb.EnterpriseContext;  //another UserTx.



/**
 * The CachedConnectionManager mbean manages associations between meta-aware objects 
 * (those accessed through interceptor chains) and connection handles, and between
 *  user transactions and connection handles.  Normally there should only be one 
 * such mbean.  It is called by CachedConnectionInterceptor, UserTransaction, 
 * and all BaseConnectionManager2 instances.
 *
 *
 * Created: Sat Jan  5 18:50:27 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:E.Guib@ceyoniq.com">Erwin Guib</a>
 * @version
 * @jmx:mbean name="jboss.jca:service=CachedConnectionManager"
 *            extends="org.jboss.system.Service"
 */

public class CachedConnectionManager 
   extends ServiceMBeanSupport 
   implements ServerVMClientUserTransaction.UserTransactionStartedListener,
              CachedConnectionManagerMBean
{
   //ThreadLocal that holds current calling meta-programming aware object. This is used 
   //in case someone caches a connection between invocations.  

   private final ThreadLocal currentObjects = new ThreadLocal(); 

   private final Map objectToConnectionManagerMap = new HashMap();

   protected final Logger log = Logger.getLogger(getClass());



   /**
    * Default CachedConnectionManager managed constructor for mbeans. 
    * Remember that this mbean should be a singleton.
    * @jmx.managed-constructor
    */
   public CachedConnectionManager ()
   {
   }

   /**
    * The Instance attribute simply holds the current instance,
    * which is normally the only instance of CachedConnectionManager.
    *
    * @return a <code>CachedConnectionManager</code> value
    * @jmx.managed-attribute access="READ"
    */
   public CachedConnectionManager getInstance()
   {
      return this;
   }


   protected void startService() throws Exception
   {
      ServerVMClientUserTransaction.getSingleton().registerTxStartedListener(this);
      EnterpriseContext.setUserTransactionStartedListener(this);
   }

   protected void stopService()
      throws Exception
   {
      ServerVMClientUserTransaction.getSingleton().unregisterTxStartedListener(this);
      EnterpriseContext.setUserTransactionStartedListener(null);
   }
   
   //Object registration for meta-aware objects (i.e. this is called by interceptors)
   /**
    * Describe <code>pushMetaAwareObject</code> method here.
    * PUBLIC for TESTING PURPOSES ONLY!
    * @param key an <code>Object</code> value
    * @exception ResourceException if an error occurs
    */
   public void pushMetaAwareObject(final Object rawKey, Set unsharableResources)
      throws ResourceException
   {
      LinkedList stack = (LinkedList)currentObjects.get();
      if (stack == null) 
      {
         if (log.isTraceEnabled()) 
         {
            log.trace("new stack for key: " + rawKey);
         } // end of if ()
         stack = new LinkedList();
         currentObjects.set(stack);  
      } // end of if ()
      else
      {
         if (log.isTraceEnabled()) 
         {
            log.trace("old stack for key: " + rawKey);
         } // end of if ()
         /* Remove attempt to recycle connections while we call another ejb.  Users should
            close the handle if they need to reuse them. 
         if (!stack.isEmpty()) 
         {
            Object oldKey = stack.getLast();
            disconnect(oldKey, unsharableResources);
         } // end of if ()
         */
      } // end of else
      //check for reentrancy, reconnect if not reentrant.
      //wrap key to be based on == rather than equals
      Object key = new IdentityWrapper(rawKey);
      if (!stack.contains(key))
      {
	 reconnect(key, unsharableResources);
      }
      stack.addLast(key);
   }

   /**
    * Describe <code>popMetaAwareObject</code> method here.
    * PUBLIC for TESTING PURPOSES ONLY!
    *
    * @exception ResourceException if an error occurs
    */
   public void popMetaAwareObject(Set unsharableResources)
      throws ResourceException
   {
      LinkedList stack = (LinkedList)currentObjects.get();
      Object oldKey = stack.removeLast();
      if (log.isTraceEnabled()) 
      {
         log.trace("popped object: " + oldKey);
      }
      disconnect(oldKey, unsharableResources);
      /*Remove attempt to recycle connections on a call to another ejb.
      if (!stack.isEmpty()) 
      {
         Object key = stack.getLast();
         reconnect(key, unsharableResources);
      } // end of if ()
      */
      //Should we remove it if empty??
   }

   Object peekMetaAwareObject()
   {
      LinkedList stack = (LinkedList)currentObjects.get();
      if (stack == null) 
      {
         return null;
      } // end of if ()
      if (!stack.isEmpty()) 
      {
         return stack.getLast();
      } // end of if ()
      else
      {
         return null;
      } // end of else
   }

   //ConnectionRegistration -- called by ConnectionCacheListeners (normally ConnectionManagers)

   void registerConnection(ConnectionCacheListener cm, ConnectionRecord cr)
   {
      Object key = peekMetaAwareObject();
      if (log.isTraceEnabled()) 
      {
         log.trace("registering connection from " + cm + ", connection record: " + cr + ", key: " + key);
      } // end of if ()
      if (key == null) 
      {
         return; //not participating properly in this management scheme.
      } // end of if ()
      
      Map cmToConnectionsMap = null;
      synchronized (objectToConnectionManagerMap)
      {
         cmToConnectionsMap = (Map)objectToConnectionManagerMap.get(key);
         if (cmToConnectionsMap == null) 
         {
            cmToConnectionsMap = new HashMap();
            objectToConnectionManagerMap.put(key, cmToConnectionsMap);
         } // end of if ()
      }
      //We should be the only thread looking at this object...
      //may still need to synch to avoid "double checked locking" problem.
      synchronized (cmToConnectionsMap)
      {
         Collection conns = (Collection)cmToConnectionsMap.get(cm);
         if (conns == null) 
         {
            conns = new ArrayList();
            cmToConnectionsMap.put(cm, conns);
         } // end of if ()
         conns.add(cr);
      }
   }


   void unregisterConnection(ConnectionCacheListener cm, Object c)
   {
      Object key = peekMetaAwareObject();
      if (log.isTraceEnabled()) 
      {
         log.trace("unregistering connection from " + cm + ", object: " + c + ", key: " + key);
      } // end of if ()
      if (key == null) 
      {
         return; //not participating properly in this management scheme.
      } // end of if ()
      
      Map cmToConnectionsMap = null;
      synchronized (objectToConnectionManagerMap)
      {
         cmToConnectionsMap = (Map)objectToConnectionManagerMap.get(key);
         if (cmToConnectionsMap == null) 
         {
            return; //??? shouldn't happen
         } // end of if ()
      }
      //We should be the only thread looking at this object...
      //may still need to synch to avoid "double checked locking" problem.
      synchronized (cmToConnectionsMap)
      {
         Collection conns = (Collection)cmToConnectionsMap.get(cm);
         if (conns == null) 
         {
            return;//???shouldn't happen.
         } // end of if ()
         for (Iterator i = conns.iterator(); i.hasNext(); )
         {
            if (((ConnectionRecord)i.next()).connection == c) 
            {
               i.remove();
               //cleanup anything that's empty
               if (conns.size() == 0) 
               {
                  cmToConnectionsMap.remove(cm);
                  if (cmToConnectionsMap.size() == 0) 
                  {
                     synchronized(objectToConnectionManagerMap)
                     {
                        objectToConnectionManagerMap.remove(key);
                     }
                     
                  } // end of if ()
                  
               } // end of if ()
               
               return;
            } // end of if ()
            
         } // end of for ()
         
      }
   }

   //called by UserTransaction after starting a transaction
   public void userTransactionStarted()
      throws SystemException
   {
      Object key = peekMetaAwareObject();
      if (log.isTraceEnabled()) 
      {
         log.trace("user tx started, key: " + key);
      } // end of if ()
      if (key == null) 
      {
         return; //not participating properly in this management scheme.
      } // end of if ()
      
      Map cmToConnectionsMap = null;
      synchronized (objectToConnectionManagerMap)
      {
         cmToConnectionsMap = (Map)objectToConnectionManagerMap.get(key);
         if (cmToConnectionsMap == null) 
         {
            return; //??? shouldn't happen
         } // end of if ()
      }
      //We should be the only thread looking at this object...
      //may still need to synch to avoid "double checked locking" problem.
      synchronized (cmToConnectionsMap)
      {
         for (Iterator i = cmToConnectionsMap.keySet().iterator(); i.hasNext(); )
         {
            ConnectionCacheListener cm = (ConnectionCacheListener)i.next();
            Collection conns = (Collection)cmToConnectionsMap.get(cm);
            cm.transactionStarted(conns);
         } // end of for ()
         
      }

   }

   private void reconnect(Object key, Set unsharableResources)
      throws ResourceException
   //TODOappropriate cleanup???
   {
      Map cmToConnectionsMap = null;
      synchronized (objectToConnectionManagerMap)
      {
         cmToConnectionsMap = (Map)objectToConnectionManagerMap.get(key);
         if (cmToConnectionsMap == null) 
         {
            return; //??? shouldn't happen
         } // end of if ()
      }
      //We should be the only thread looking at this object...
      //may still need to synch to avoid "double checked locking" problem.
      synchronized (cmToConnectionsMap)
      {
         for (Iterator i = cmToConnectionsMap.keySet().iterator(); i.hasNext(); )
         {
            ConnectionCacheListener cm = (ConnectionCacheListener)i.next();
            Collection conns = (Collection)cmToConnectionsMap.get(cm);
            cm.reconnect(conns, unsharableResources);
         } // end of for ()
         
      }
   }

   private void disconnect(Object key, Set unsharableResources)
      throws ResourceException
   //TODOappropriate cleanup???
   {
      Map cmToConnectionsMap = null;
      synchronized (objectToConnectionManagerMap)
      {
         cmToConnectionsMap = (Map)objectToConnectionManagerMap.get(key);
         if (cmToConnectionsMap == null) 
         {
            return; //??? shouldn't happen
         } // end of if ()
      }
      //We should be the only thread looking at this object...
      //may still need to synch to avoid "double checked locking" problem.
      synchronized (cmToConnectionsMap)
      {
         for (Iterator i = cmToConnectionsMap.keySet().iterator(); i.hasNext(); )
         {
            ConnectionCacheListener cm = (ConnectionCacheListener)i.next();
            Collection conns = (Collection)cmToConnectionsMap.get(cm);
            cm.disconnect(conns, unsharableResources);
         } // end of for ()
         
      }
   }
   //shutdown method for ConnectionManager

   /**
    * Describe <code>unregisterConnectionCacheListener</code> method here.
    * This is a shutdown method called by a connection manager.  It will remove all reference
    * to that connection manager from the cache, so cached connections from that manager
    * will never be recoverable.
    * Possibly this method should not exist.
    * @param cm a <code>ConnectionCacheListener</code> value
    */
   void unregisterConnectionCacheListener(ConnectionCacheListener cm)
   {
      if (log.isTraceEnabled()) 
      {
         log.trace("unregisterConnectionCacheListener: " + cm);
      } // end of if ()
      synchronized (objectToConnectionManagerMap)
      {
         for (Iterator i = objectToConnectionManagerMap.values().iterator(); i.hasNext(); )
         {
            Map cmToConnectionsMap = (Map)i.next();
            if (cmToConnectionsMap != null)
            {
               cmToConnectionsMap.remove(cm);
            } // end of if ()
            
         } // end of for ()
         
      }
   }

   /**
    * The class <code>IdentityWrapper</code> wraps objects so they may be used in hashmaps
    * based on their object identity rather than equals implementation. Used for keys.
    *
    */
   private final static class IdentityWrapper
   {
     
      private final Object o;

      IdentityWrapper(final Object o)
      {
         this.o = o;
      }

      public boolean equals(Object other)
      {
         return (other instanceof IdentityWrapper) && o == ((IdentityWrapper)other).o;
      }

      public int hashCode()
      {
         return System.identityHashCode(o);
      }
   }

}// CachedConnectionManager
