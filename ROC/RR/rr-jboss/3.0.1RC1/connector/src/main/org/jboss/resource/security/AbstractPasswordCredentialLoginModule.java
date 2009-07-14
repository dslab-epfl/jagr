
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.security;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.security.auth.spi.AbstractServerLoginModule;
import org.jboss.util.jmx.MBeanServerLocator;


/**
 * AbstractPasswordCredentialLoginModule.java
 *
 *
 * Created: Sun Apr 28 21:30:58 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public abstract class AbstractPasswordCredentialLoginModule extends AbstractServerLoginModule 
 implements NotificationListener {
   private MBeanServer server;
   private ObjectName managedConnectionFactoryName;
   private ManagedConnectionFactory mcf;
   private static final Logger log = Logger.getLogger(AbstractPasswordCredentialLoginModule.class);

   public AbstractPasswordCredentialLoginModule() 
   {      
   }
   
   public void initialize(Subject subject, CallbackHandler handler, Map sharedState, Map options)
   {
      super.initialize(subject, handler, sharedState, options);
      try 
      {
         managedConnectionFactoryName = new ObjectName((String) options.get("managedConnectionFactoryName"));
      }
      catch (MalformedObjectNameException mone)
      {
         throw new IllegalArgumentException("Malformed ObjectName: " + (String) options.get("managedConnectionFactoryName"));
         
      } // end of try-catch
      
      if( managedConnectionFactoryName == null )
      {
         throw new IllegalArgumentException("Must supply a managedConnectionFactoryName!");
      }
      server = MBeanServerLocator.locate();
      getMcf();
   }

   /**
    * Describe <code>login</code> method here.
    * I don't understand why this is necessary....
    * @return a <code>boolean</code> value
    * @exception LoginException if an error occurs
    */
   public boolean login() throws LoginException
   {
      if (mcf == null) 
      {
         return false;
      } // end of if ()
      return super.login();
   }

   protected ManagedConnectionFactory getMcf()
   {
      if (mcf == null) 
      {
         try 
         {
            mcf = (ManagedConnectionFactory)server.getAttribute(
               managedConnectionFactoryName,
               "ManagedConnectionFactory");
            NotificationFilterSupport nf = new NotificationFilterSupport();
            nf.enableType(BaseConnectionManager2.STOPPING_NOTIFICATION);
            server.addNotificationListener(managedConnectionFactoryName, 
                                           this,
                                           nf,
                                           new Object()); 
         }
         catch (Exception e)
         {
            log.error("The ConnectionManager mbean: " + managedConnectionFactoryName + " specified in a ConfiguredIdentityLoginModule could not be found.  ConnectionFactory will be unusable!");
            throw new IllegalArgumentException("Managed Connection Factory not found: " + managedConnectionFactoryName);
         } // end of try-catch
         if (log.isTraceEnabled()) 
         {
            log.trace("mcfname: " + managedConnectionFactoryName);
         } // end of if ()
      } // end of if ()
      
      return mcf;
   }
   // implementation of javax.management.NotificationListener interface

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    */
   public void handleNotification(Notification notification, Object handback)
   {
      if (notification.getSource().equals(managedConnectionFactoryName)) 
      {
         removeCredentials();
         mcf = null;
         try 
         {
            server.removeNotificationListener(managedConnectionFactoryName, this); 
         }
         catch (Exception e)
         {
            log.error("The ConnectionManager mbean: " + managedConnectionFactoryName + " could not have the login module " + this + " removed.  This may result in a memory leak!");
         } // end of try-catch
      } // end of if ()
   }



   public boolean logout() throws LoginException
   {
      removeCredentials();
      return super.logout();
   }

   /**
    * The removeCredentials method removes whatever is necessary to 
    * make super.login()
    *
    */
   protected void removeCredentials()
   {
      sharedState.remove("javax.security.auth.login.name");
      sharedState.remove("javax.security.auth.login.password");
      Set credentials = subject.getPrivateCredentials();
      for (Iterator i = credentials.iterator(); i.hasNext(); )
      {
         Object o = i.next();
         if (o instanceof PasswordCredential && ((PasswordCredential)o).getManagedConnectionFactory() == mcf) 
         {
            i.remove();
         } // end of if ()
         
      } // end of for ()
   }
   


}// AbstractPasswordCredentialLoginModule
