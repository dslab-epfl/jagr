
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.connectionmanager;



import java.io.Serializable;
import java.lang.SecurityException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.management.j2ee.JCAConnectionFactory;
import org.jboss.management.j2ee.JCAManagedConnectionFactory;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityDomain;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.jmx.JMXExceptionDecoder;
import org.jboss.util.jmx.MBeanServerLocator;


/**
 * The BaseConnectionManager2 is an abstract base class for JBoss ConnectionManager 
 * implementations.  It includes functionality to obtain managed connections from 
 * a ManagedConnectionPool mbean, find the Subject from a SubjectSecurityDomain, 
 * and interact with the CachedConnectionManager for connections held over 
 * transaction and method boundaries.  Important mbean references are to a 
 * ManagedConnectionPool supplier (typically a JBossManagedConnectionPool), and a 
 * RARDeployment representing the ManagedConnectionFactory.
 *
 *
 * Created: Wed Jan  2 12:16:09 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:E.Guib@ceyoniq.com">Erwin Guib</a>
 * @version $$
 * @jmx:mbean name="jboss.jca:service=BaseConnectionManager"
 *            extends="org.jboss.system.ServiceMBean"
 */

public abstract class BaseConnectionManager2 
   extends ServiceMBeanSupport 
   implements BaseConnectionManager2MBean, ConnectionCacheListener
{

   /** Note that this copy has a trailing / unlike the original in 
    * JaasSecurityManagerService.
    */
   private static final String SECURITY_MGR_PATH = "java:/jaas/";

   public static final String STOPPING_NOTIFICATION = "jboss.jca.connectionmanagerstopping";

   private ObjectName managedConnectionPoolName;

   private ManagedConnectionPool poolingStrategy;

   private ObjectName managedConnectionFactoryName;

   private ManagedConnectionFactory mcf;

   protected String mcfJndiName;

   //private ObjectName securityDomainName; //for the future??
   private String securityDomainJndiName;
   private SubjectSecurityManager /*SecurityDomain*/ securityDomain;

   private ObjectName jaasSecurityManagerService;


   private ObjectName ccmName;
   private CachedConnectionManager ccm;

   // JSR-77 Managed Object
   ObjectName jcaConnectionFactory;
   ObjectName jcaManagedConnectionFactory;

   private final Map managedConnectionToListenerMap = Collections.synchronizedMap(new HashMap());

   protected final Logger log = Logger.getLogger(getClass());
   /**
    * Default BaseConnectionManager2 managed constructor for use by subclass mbeans.
    *
    */
   public BaseConnectionManager2()
   {
   }



   /**
    * Creates a new <code>BaseConnectionManager2</code> instance.
    * for TESTING ONLY! not a managed operation.
    * @param mcf a <code>ManagedConnectionFactory</code> value
    * @param ccm a <code>CachedConnectionManager</code> value
    * @param poolingStrategy a <code>ManagedConnectionPool</code> value
    */
   public BaseConnectionManager2(ManagedConnectionFactory mcf,
                                 CachedConnectionManager ccm,
                                 ManagedConnectionPool poolingStrategy)
   {
      this.mcf = mcf;
      this.ccm = ccm;
      this.poolingStrategy = poolingStrategy;
   }


   /**
    * ManagedConnectionFactoryName holds the ObjectName of the mbean that 
    * represents the ManagedConnectionFactory.  Normally this can be an 
    * embedded mbean in a depends element rather than a separate mbean 
    * reference.
    * @return the ManagedConnectionFactoryName value.
    * @jmx:managed-attribute
    */
   public ObjectName getManagedConnectionFactoryName()
   {
      return managedConnectionFactoryName;
   }

   /**
    * Set the ManagedConnectionFactoryName value.
    * @param newManagedConnectionFactoryName The new ManagedConnectionFactoryName value.
    * @jmx:managed-attribute
    */
   public void setManagedConnectionFactoryName(ObjectName newManagedConnectionFactoryName)
   {
      this.managedConnectionFactoryName = newManagedConnectionFactoryName;
   }

   
   /**
    * The ManagedConnectionPool holds the ObjectName of the mbean representing
    * the pool for this connection manager.  Normally it will be an embedded
    * mbean in a depends tag rather than an ObjectName reference to the mbean.
    * @return the ManagedConnectionPool value.
    * @jmx:managed-attribute
    */
   public ObjectName getManagedConnectionPool()
   {
      return managedConnectionPoolName;
   }

   /**
    * Set the ManagedConnectionPool value.
    * @param newManagedConnectionPool The new ManagedConnectionPool value.
    * @jmx:managed-attribute
    */
   public void setManagedConnectionPool(ObjectName newManagedConnectionPool)
   {
      this.managedConnectionPoolName = newManagedConnectionPool;
   }

   


   /**
    * The CachecConnectionManager holds the ObjectName of the 
    * CachedConnectionManager mbean used by this ConnectionManager.
    * Normally this will be a depends tag with the ObjectName of the 
    * unique CachedConnectionManager for the server.
    *
    * @param ccmName an <code>ObjectName</code> value
    * @jmx:managed-attribute
    */
   public void setCachedConnectionManager(ObjectName ccmName)
   {
      this.ccmName = ccmName;
   }

   /**
    * Describe <code>getCachedConnectionManager</code> method here.
    *
    * @return an <code>ObjectName</code> value
    * @jmx:managed-attribute
    */
   public ObjectName getCachedConnectionManager()
   {
      return ccmName;
   }




   /**
    *  The SecurityDomainJndiName holds the jndi name of the security domain 
    * configured for the ManagedConnectionFactory this ConnectionManager 
    * manages.  It is normally of the form java:/jaas/firebirdRealm,
    * where firebirdRealm is the name found in auth.conf or equivalent file.
    *
    * @param name an <code>String</code> value
    * @jmx:managed-attribute
    */
   public void setSecurityDomainJndiName(String securityDomainJndiName)
   {
      if (securityDomainJndiName != null 
          && securityDomainJndiName.startsWith(SECURITY_MGR_PATH))
      {
         securityDomainJndiName = securityDomainJndiName.substring(SECURITY_MGR_PATH.length());
         log.warn("WARNING: UPDATE YOUR SecurityDomainJndiName! REMOVE " + SECURITY_MGR_PATH);
      } // end of if ()
      this.securityDomainJndiName = securityDomainJndiName;
   }
   /**
    * Get the SecurityDomainJndiName value.
    * @return the SecurityDomainJndiName value.
    * @jmx:managed-attribute
    */
   public String getSecurityDomainJndiName()
   {
      return securityDomainJndiName;
   }

   /**
    * Get the JaasSecurityManagerService value.
    * @return the JaasSecurityManagerService value.
    * @jmx:managed-attribute
    */
   public ObjectName getJaasSecurityManagerService()
   {
      return jaasSecurityManagerService;
   }

   /**
    * Set the JaasSecurityManagerService value.
    * @param newJaasSecurityManagerService The new JaasSecurityManagerService value.
    * @jmx:managed-attribute
    */
   public void setJaasSecurityManagerService(final ObjectName jaasSecurityManagerService)
   {
      this.jaasSecurityManagerService = jaasSecurityManagerService;
   }

   
   
   /**
    * ManagedConnectionFactory is an internal attribute that holds the 
    * ManagedConnectionFactory instance managed by this ConnectionManager.
    *
    * @return value of managedConnectionFactory
    *
    * @jmx.managed-attribute access="READ"
    */
   public ManagedConnectionFactory getManagedConnectionFactory()
   {
      return mcf;
   }
   



   /**
    * Describe <code>getInstance</code> method here.
    *
    * @return a <code>BaseConnectionManager2</code> value
    *
    * @jmx.managed-attribute access="READ"
    */
   public BaseConnectionManager2 getInstance()
   {
      return this;
   }

   //ServiceMBeanSupport
   
   public String getName()
   {
      return "BaseConnectionManager";
   }
   
   protected void createService() throws Exception
   {
      // Create JSR-77 JCAConnectionFactory Managed Object
      jcaConnectionFactory = JCAConnectionFactory.create(getServer(),
                                                         getServiceName().getKeyProperty("name") + 
                                                         "-" + 
                                                         getServiceName().getKeyProperty("service"),
                                                         getServiceName());

      // Create JSR-77 JCAManagedConnectionFactory Managed Object
      jcaManagedConnectionFactory = JCAManagedConnectionFactory.create(
                                                       getServer(),
                                                       managedConnectionFactoryName.getKeyProperty("name") +
                                                       "-" +
                                                       managedConnectionFactoryName.getKeyProperty("service"),
                                                       jcaConnectionFactory);
   }

   protected void startService() throws Exception
   {
      super.startService();
      try 
      {
         ccm = (CachedConnectionManager)getServer().getAttribute(ccmName, "Instance");
      
      }
      catch (Exception e)
      {
         JMXExceptionDecoder.rethrow(e);
      } // end of try-catch
      
      if (ccm == null) 
      {
         throw new DeploymentException("cached ConnectionManager not found: " + ccmName);
      } // end of if ()

      if (securityDomainJndiName != null && jaasSecurityManagerService == null) 
      {
         throw new DeploymentException("You must supply both securityDomainJndiName and jaasSecurityManagerService to use container managed security");
      } // end of if ()
      

      if (securityDomainJndiName != null) 
      {
         securityDomain = (SubjectSecurityManager)new InitialContext().lookup(SECURITY_MGR_PATH + securityDomainJndiName);
      } // end of if ()
      
      if (managedConnectionPoolName == null) 
      {
         throw new DeploymentException("managedConnectionPool not set!");
      } // end of if ()
      try 
      {
         poolingStrategy = (ManagedConnectionPool)getServer().getAttribute(
            managedConnectionPoolName, 
            "ManagedConnectionPool");
      }
      catch (Exception e)
      {
         JMXExceptionDecoder.rethrow(e);
      } // end of try-catch
      

      if (managedConnectionFactoryName == null) 
      {
         throw new DeploymentException("ManagedConnectionFactory not set!");         
      } // end of if ()
      try 
      {
         mcf = (ManagedConnectionFactory)getServer().invoke(
            managedConnectionFactoryName, 
            "startManagedConnectionFactory",
            new Object[] {new ConnectionManagerProxy(this, this.serviceName)},
            new String[] {ConnectionManager.class.getName()});

         // save the JNDI Name for this connector
         // we need it later to check if the connector gets called from a
         // bean with shared or unshared resource-ref
         mcfJndiName = (String)getServer().getAttribute(managedConnectionFactoryName, 
               "JndiName");
      }
      catch (Exception e)
      {
         JMXExceptionDecoder.rethrow(e);
      } // end of try-catch
      

      poolingStrategy.setManagedConnectionFactory(mcf);
   }

   protected void stopService()
      throws Exception
   {
      //notify the login modules the mcf is going away, they need to look it up again later.
     sendNotification(new Notification(STOPPING_NOTIFICATION, 
                                       getServiceName(), 
                                       getNextNotificationSequenceNumber()));
      if (jaasSecurityManagerService != null && securityDomainJndiName != null) 
      {
         getServer().invoke(jaasSecurityManagerService,
                            "flushAuthenticationCache",
                            new Object[] {securityDomainJndiName},
                            new String[] {String.class.getName()});
         
      } // end of if ()
      
      shutdown();
      mcf = null;
      try 
      {
         getServer().invoke(managedConnectionFactoryName, 
                            "stopManagedConnectionFactory",
                            new Object[] {},
                            new String[] {});
      }
      catch (Exception e)
      {
         log.error("Could not stop ManagedConnectionFactory", e);  
      } // end of try-catch
      poolingStrategy = null;
      securityDomain = null;
      ccm = null;
      super.stopService();
   }
   
   protected void destroyService() 
   {
      // Destroy JSR-77 JCAManagedConnectionFactory Managed Object
      JCAManagedConnectionFactory.destroy(getServer(),
                                          managedConnectionFactoryName.getKeyProperty("name") +
                                          "-" +
                                          managedConnectionFactoryName.getKeyProperty("service")
                                          );
      
      // Destroy JSR-77 JCAConnectionFactory Managed Object
      JCAConnectionFactory.destroy(getServer(),
                                   getServiceName().getKeyProperty("name") +
                                   "-" +
                                   getServiceName().getKeyProperty("service")
                                   );
   }

   /**
    * Describe <code>getManagedConnection</code> method here.
    * Public for use in testing pooling functionality by itself.
    * called by both allocateConnection and reconnect.
    * @param subject a <code>Subject</code> value
    * @param cri a <code>ConnectionRequestInfo</code> value
    * @return a <code>ManagedConnection</code> value
    * @exception ResourceException if an error occurs
    */
   public ManagedConnection getManagedConnection(Subject subject, ConnectionRequestInfo cri) 
      throws ResourceException
   {
      ManagedConnection mc = poolingStrategy.getConnection(subject, cri);
      if (getConnectionEventListener(mc) == null) 
      {
         //it is a new ManagedConnection, so no one else can be getting it.
         managedConnectionToListenerMap.put(mc, registerConnectionEventListener(mc));
         if (log.isTraceEnabled()) 
         {
            log.trace("registering ConnectionEventListener for ManagedConnection: " + mc);
         } // end of if ()
         
      } // end of if ()
      return mc;
   }

   public void returnManagedConnection(ManagedConnection mc, boolean kill)
   {
      if (kill) 
      {
         managedConnectionToListenerMap.remove(mc);
         if (log.isTraceEnabled()) 
         {
            log.trace("killing ManagedConnection, removing ConnectionListener: " + mc);
         } // end of if ()
        
      } // end of if ()
      try 
      {
         poolingStrategy.returnConnection(mc, kill);
      }
      catch (ResourceException re)
      {
         log.warn("resourceException returning connection: " + mc, re);
      } // end of try-catch
   }

   public int getConnectionCount()
   {
      return poolingStrategy.getConnectionCount();
   }

   public void shutdown()
   {
      log.info("shutting down pool: " + this);      
      managedConnectionToListenerMap.clear();
      poolingStrategy.shutdown();
   }
   // implementation of javax.resource.spi.ConnectionManager interface

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Object allocateConnection(ManagedConnectionFactory mcf, 
                                    ConnectionRequestInfo cri) 
      throws ResourceException
   {
      //it is an explicit spec requirement that equals be used for matching rather than ==.
      if (!this.mcf.equals(mcf)) 
      {
         throw new ResourceException("Wrong ManagedConnectionFactory sent to allocateConnection!");
      } // end of if ()
      Subject subject = getSubject();
      ManagedConnection mc = getManagedConnection(subject, cri);
      //WRONG METHOD NAME!!
      managedConnectionReconnected(mc);

      Object connection = mc.getConnection(subject, cri);
      registerAssociation(mc, connection);
      if (ccm != null) 
      {
         ccm.registerConnection(this, new ConnectionRecord(mc, connection, cri));
      } // end of if ()
      return connection;
   }

   protected abstract ConnectionListener registerConnectionEventListener(ManagedConnection mc) 
      throws ResourceException;


   //ConnectionCacheListener implementation
   public void transactionStarted(Collection conns) throws SystemException
   {
      //reimplement in subclasses
   }

   /**
    * Describe <code>reconnect</code> method here.
    *
    * @param conns a <code>Collection</code> value
    * @exception ResourceException if an error occurs
    * @todo decide if the warning situation should throw an exception
    */
   public void reconnect(Collection conns, Set unsharableResources) throws ResourceException
   {
      // if we have an unshareable connection the association was not removed
      // nothing to do
      if(unsharableResources.contains(mcfJndiName))
      {
         log.trace("reconnect for unshareable connection: nothing to do");
         return;
      }
      
      Map criToMCMap = new HashMap();
      for (Iterator i = conns.iterator(); i.hasNext(); )
      {
         ConnectionRecord cr = (ConnectionRecord)i.next();
         if (cr.mc != null) 
         {
            //This might well be an error.
            log.warn("reconnecting a connection handle that still has a managedConnection! " + cr.mc + " " + cr.connection);
            if (managedConnectionToListenerMap.containsKey(cr.mc)) 
            {
               throw new IllegalArgumentException("reconnect(ConnectionRecord cr) called with a non null ManagedConnection that was not killed!");
            } // end of if ()
         }
         ManagedConnection mc = (ManagedConnection)criToMCMap.get(cr.cri);
         if (mc == null) 
         {
            mc = getManagedConnection(getSubject(), cr.cri);
            criToMCMap.put(cr.cri, mc);
            //only call once per managed connection, when we get it.
            managedConnectionReconnected(mc);
         } // end of if ()
         
         mc.associateConnection(cr.connection);
         registerAssociation(mc, cr.connection);
         cr.setManagedConnection(mc);
      } // end of for () 
      criToMCMap.clear();//not needed logically, might help the gc.     
   }


   public void disconnect(Collection crs, Set unsharableResources) throws ResourceException

   {
      // if we have an unshareable connection do not remove the association
      // nothing to do
      if(unsharableResources.contains(mcfJndiName))
      {
         log.trace("disconnect for unshareable connection: nothing to do");
         return;
      }

      Set mcs = new HashSet();
      for (Iterator i = crs.iterator(); i.hasNext(); )
      {
         ConnectionRecord cr = (ConnectionRecord)i.next();
         ManagedConnection mc = cr.mc;
         cr.setManagedConnection(null);
         unregisterAssociation(mc, cr.connection);
         if (!mcs.contains(mc)) 
         {
            mcs.add(mc);  
         } // end of if ()
      } // end of for () 
      for (Iterator i = mcs.iterator(); i.hasNext(); )
      {
         managedConnectionDisconnected((ManagedConnection)i.next());
      } // end of for ()
      
   }

   // implementation of javax.management.NotificationBroadcaster interface


   /**
    *
    * @return <description>
    */
   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // TODO: implement this javax.management.NotificationBroadcaster method
      return super.getNotificationInfo();
   }

   //protected methods

   protected ConnectionListener getConnectionEventListener(ManagedConnection mc)
   {
      return (ConnectionListener)managedConnectionToListenerMap.get(mc);
   }

   //does NOT put the mc back in the pool if no more handles. Doing so would introduce a race condition
   //whereby the mc got back in the pool while still enlisted in the tx. 
   //The mc could be checked out again and used before the delist occured.
   protected void unregisterAssociation(ManagedConnection mc, Object c) throws ResourceException
   {
      boolean flag = false;
      ConnectionListener cli = getConnectionEventListener(mc);
      if (cli == null) 
      {
         throw  new IllegalArgumentException("disconnect(ManagedConnection mc: " + mc + ", Object c: " + c + ") called with unknown managed connection");
      } // end of if ()
      cli.unregisterConnection(c);
      if (cli.isManagedConnectionFree()) 
      {
         mc.cleanup();
      } // end of if ()
   }

   protected final CachedConnectionManager getCcm()
   {
      return ccm;
   }

   //reimplement in subclasses to e.g. enlist in current tx.
   protected void managedConnectionReconnected(ManagedConnection mc) throws ResourceException
   {
     
   }

   //reimplement in subclasses to e.g. enlist in current tx.
   protected void managedConnectionDisconnected(ManagedConnection mc) throws ResourceException
   {
     
   }


   private void registerAssociation(ManagedConnection mc, Object c) throws ResourceException
   {
      ConnectionListener cli = getConnectionEventListener(mc);
      cli.registerConnection(c);
   }

   private Subject getSubject()
   {
      Subject subject = null;
      if (securityDomain != null) 
      {
         Principal principal = SecurityAssociation.getPrincipal();
         Object credential = SecurityAssociation.getCredential();
         if (securityDomain.isValid(principal, credential)) 
         {
            subject = securityDomain.getActiveSubject();
         } // end of if ()
         else
         {
            throw new SecurityException("Invalid authentication attempt, principal=" + principal);
         } // end of else
         
      } // end of if ()
      if (log.isTraceEnabled()) 
      {
         log.trace("subject: " + subject);
      } // end of if ()
      return subject;
   }


   //ConnectionListener

   interface ConnectionListener extends ConnectionEventListener
   {

      ManagedConnection getManagedConnection();

      void registerConnection(Object handle);

      void unregisterConnection(Object handle);

      boolean isManagedConnectionFree();

   }

   protected abstract class BaseConnectionEventListener implements ConnectionListener
   {

      private final ManagedConnection mc;
 
      private int handleCount = 0;
      private final List handles = new LinkedList();

      protected final Logger log = Logger.getLogger(getClass());


      protected BaseConnectionEventListener(ManagedConnection mc)
      {
         this.mc = mc;
      }

      public ManagedConnection getManagedConnection()
      {
         return mc;
      }

      public synchronized void registerConnection(Object handle)
      {
         handleCount++;
         handles.add(handle);
      }

      public synchronized void unregisterConnection(Object handle)
      {
         handles.remove(handle);
         handleCount--;
         if (log.isTraceEnabled()) 
         {
            log.trace("unregisterConnection: " + handleCount);
         } // end of if ()
      }

      public boolean isManagedConnectionFree()
      {
         return handleCount == 0;
      }

      protected void unregisterConnections()
      {
         for (Iterator i = handles.iterator(); i.hasNext(); )
         {
            getCcm().unregisterConnection(BaseConnectionManager2.this, i.next());
         }
         handles.clear();
         handleCount = 0;
      }

   }

   public static class ConnectionManagerProxy
      implements ConnectionManager, Serializable
   {

      private transient BaseConnectionManager2 realCm;
      private final ObjectName cmName;

      ConnectionManagerProxy(final BaseConnectionManager2 realCm, final ObjectName cmName)
      {
         this.realCm = realCm;
         this.cmName = cmName;
      }

      // implementation of javax.resource.spi.ConnectionManager interface

      /**
       *
       * @param mcf <description>
       * @param cri <description>
       * @return <description>
       * @exception javax.resource.ResourceException <description>
       */
      public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri) throws ResourceException
      {
         return getCM().allocateConnection(mcf, cri);
      }

      private BaseConnectionManager2 getCM() throws ResourceException
      {
         if (realCm == null) 
         {
            try 
            {
               realCm = (BaseConnectionManager2)MBeanServerLocator.locate().getAttribute(
                  cmName, 
                  "Instance");

            }
            catch (Throwable t)
            {
               Throwable t2 = JMXExceptionDecoder.decode(t);
               //log.info("Problem locating real ConnectionManager: ", t2);
               throw new ResourceException("Problem locating real ConnectionManager: " + t2);
            } // end of try-catch
         } // end of if ()
         return realCm;  
      }
   }

}// BaseConnectionManager2
