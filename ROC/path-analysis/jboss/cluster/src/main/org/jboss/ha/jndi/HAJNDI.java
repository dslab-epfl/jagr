/*
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */

package org.jboss.ha.jndi;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnp.interfaces.NamingContext;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAPartition.HAPartitionStateTransfer;

/** 
 *   This class extends the JNP JNDI implementation.
 *   binds and unbinds will be distributed to all members of the cluster
 *   that are running HAJNDI.
 *   lookups will look for Names in HAJNDI then delegate to the local InitialContext
 *
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b><br>
 */

public class HAJNDI extends org.jnp.server.NamingServer implements HAPartitionStateTransfer, Serializable, org.jnp.interfaces.Naming
{
   // Attributes --------------------------------------------------------
   
   private HAPartition partition;
   private org.jboss.logging.Logger log;
   
   // Constructor --------------------------------------------------------
   
   public HAJNDI(HAPartition partition)
      throws NamingException
   {
      super();
      log = org.jboss.logging.Logger.getLogger(HAJNDI.class);
      this.partition = partition;
   }
   
   // Public --------------------------------------------------------

   public void init() throws Exception
   {
      log.debug("subscribeToStateTransferEvents");
      partition.subscribeToStateTransferEvents("HAJNDI", this);
      log.debug("registerRPCHandler");
      partition.registerRPCHandler("HAJNDI", this);
   }

   // HAPartition.HAPartitionStateTransfer Implementation --------------------------------------------------------
   
   public Serializable getCurrentState()
   {
      log.trace("getCurrentState called");
      return table;
   }

   public void setCurrentState(Serializable newState)
   {
      log.trace("setCurrentState called");
      table.clear();
      table.putAll((java.util.Hashtable)newState);
   }

   // Naming implementation -----------------------------------------
   

   public synchronized void _bind(Name name, Object obj, String className)
      throws NamingException
   {
      log.trace("_bind");
      super.bind(name, obj, className);
   }
   public synchronized void bind(Name name, Object obj, String className)
      throws NamingException
   {
      log.trace("bind");
      super.bind(name, obj, className);
      // if we get here, this means we can do it on every node.
      Object[] args = new Object[3];
      args[0] = name;
      args[1] = obj;
      args[2] = className;
      try
      {
         partition.callMethodOnCluster("HAJNDI", "_bind", args, true);
      }
      catch (Exception ignored) {
         // FIXME is this right?
      }
   }

   public synchronized void _rebind(Name name, Object obj, String className)
      throws NamingException
   {
      log.trace("_rebind");
      super.rebind(name, obj, className);
   }
   public synchronized void rebind(Name name, Object obj, String className)
      throws NamingException
   {
      log.trace("rebind");
      super.rebind(name, obj, className);
      
      // if we get here, this means we can do it on every node.
      Object[] args = new Object[3];
      args[0] = name;
      args[1] = obj;
      args[2] = className;
      try
      {
         partition.callMethodOnCluster("HAJNDI", "_rebind", args, true);
      }
      catch (Exception ignored)
      {
         // FIXME is this right?
      }
   }
   
   public synchronized void _unbind(Name name)
      throws NamingException
   {
      log.debug("_unbind");
      super.unbind(name);
   }
   public synchronized void unbind(Name name)
      throws NamingException
   {
      log.trace("unbind");
      super.unbind(name);
      
      // if we get here, this means we can do it on every node.
      Object[] args = new Object[1];
      args[0] = name;
      try
      {
         partition.callMethodOnCluster("HAJNDI", "_unbind", args, true);
      }
      catch (Exception ignored)
      {
         // FIXME is this right?
      }
   }

   public Object lookup(Name name)
      throws NamingException
   {
      log.trace("lookup");
      Object result = null;
      try
      {
         result = super.lookup(name);
      }
      catch (NameNotFoundException ex)
      {
         try
         {
            // not found in global jndi, look in local.
            result = lookupLocally(name);
         }
         catch (NameNotFoundException nnfe)
         {
            // if we get here, this means we can do it on every node.
            Object[] args = new Object[1];
            args[0] = name;
            ArrayList rsp = null;
            try
            {
               log.trace("calling lookupLocally");
               rsp = partition.callMethodOnCluster("HAJNDI", "lookupLocally", args, true);
            }
            catch (Exception ignored)
            {
            }

            if (rsp == null || rsp.size() == 0) throw new NameNotFoundException();
            for (int i = 0; i < rsp.size(); i++)
            {
               result = rsp.get(i);
               if( result != null )
                  log.trace("_lookupLocally returned: " + result.getClass().getName());
               if (!(result instanceof Exception))
                  return result;
            }
            throw nnfe;
         }
      }
      return result;
   }

   public Object _lookupLocally(Name name)
   {
      log.trace("_lookupLocally");
      try
      {
         return lookupLocally(name);
      }
      catch (NamingException ex)
      {
         log.trace("_lookupLocally returning NameNotFoundException");
         return ex;
      }
      catch (Exception rex)
      {
         return rex;
      }
   }
   public Object lookupLocally(Name name) throws NamingException
   {
      log.debug("lookupLocally");
      // FIXME: This is a really big hack here
      // We cannot do InitialContext().lookup(name) because
      // we get ClassNotFound errors and ClassLinkage errors.
      // So, what we do is cheat and get the static localServer variable
      try
      {
         if (NamingContext.localServer != null)
         {
            return NamingContext.localServer.lookup(name);
         }
         else
         {
            InitialContext ctx = new InitialContext();
            return ctx.lookup(name);
         }
      }
      catch (NamingException nex)
      {
         log.trace("lookupLocally failed for " + name.toString(), nex);
         throw nex;
      }
      catch (java.rmi.RemoteException rmex)
      {
         throw new NamingException("unknown remote exception");
      }
      catch (RuntimeException rex)
      {
         log.trace("lookupLocally failed for " + name.toString(), rex);
         throw rex;
      }
   }

   protected ArrayList enum2list (javax.naming.NamingEnumeration en)
   {
      ArrayList rtn = new ArrayList();
      try
      {
         while (en.hasMore())
         {
            rtn.add(en.next());
         }
         en.close();
      }
      catch (NamingException ignored) {}
      return rtn;
   }
   
   public Collection list(Name name)
      throws NamingException
   {
      log.trace("list");
      Collection result = null;
      try
      {
         result = super.list(name);
      }
      catch (NameNotFoundException ex)
      {
         // not found in global jndi, look in local.
         result =  enum2list(new InitialContext().list(name));
      }
      return result;
   }
    
   public Collection listBindings(Name name)
      throws NamingException
   {
      log.trace("listBindings");
      Collection result = null;
      try
      {
         result = super.listBindings(name);
      }
      catch (NameNotFoundException ex)
      {
         // not found in global jndi, look in local.
         result =  enum2list(new InitialContext().listBindings(name));
      }
      return result;
   }
   
}
