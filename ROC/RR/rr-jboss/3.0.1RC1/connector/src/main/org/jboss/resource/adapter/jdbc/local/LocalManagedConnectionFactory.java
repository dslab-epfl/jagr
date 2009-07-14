
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.adapter.jdbc.local;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import org.jboss.logging.Logger;
import java.io.Serializable;

/**
 * LocalManagedConnectionFactory.java
 *
 *
 * Created: Fri Apr 19 13:33:08 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class LocalManagedConnectionFactory 
   implements ManagedConnectionFactory, Serializable
{
   private static final Logger log = Logger.getLogger(LocalManagedConnectionFactory.class);
   
   private String driverClass;
   private boolean driverLoaded;
   private String connectionURL;
   private String userName;
   private String password;
   

   private int transactionIsolation = -1;
   


   public LocalManagedConnectionFactory ()
   {
      
   }
   // implementation of javax.resource.spi.ManagedConnectionFactory interface

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnectionFactory method
      return null;
   }

   /**
    *
    * @param param1 <description>
    * @exception javax.resource.ResourceException <description>
    */
   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnectionFactory method
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      return new LocalDataSource(this, cm);
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("NYI");
      //return createConnectionFactory(new DefaultConnectionManager());
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      boolean debug = log.isDebugEnabled();
      Properties props = getConnectionProperties(subject, cri);
      // Some friendly drivers (Oracle, you guessed right) modify the props you supply.
      // Since we use our copy to identify compatibility in matchManagedConnection, we need
      // a pristine copy for our own use.  So give the friendly driver a copy.
      Properties copy = new Properties();
      copy.putAll(props);
      if (debug) {
         log.debug("Using properties: "  + props);
      }
      
      try 
      {
         String url = internalGetConnectionURL();
         checkDriver(url);


         Connection con = DriverManager.getConnection(url, copy);
         if (con == null) 
         {
            throw new ResourceException("Wrong driver class for this connection URL");
         } // end of if ()

         return new LocalManagedConnection(this, con, props, transactionIsolation);
      }
      catch (SQLException e)
      {
         // use our ResourceException to properly display linked exception in stack trace
         throw new org.jboss.resource.ResourceException("Could not create connection", e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public ManagedConnection matchManagedConnections(Set mcs, Subject subject, ConnectionRequestInfo cri) 
      throws ResourceException
   {
      Properties newProps = getConnectionProperties(subject, cri);
      for (Iterator i = mcs.iterator(); i.hasNext(); )
      {
         Object o = i.next();
         if (o instanceof LocalManagedConnection) 
         {
            LocalManagedConnection mc = (LocalManagedConnection)o;
            if (mc.getProps().equals(newProps)) 
            {
               return mc;
            } // end of if ()
            
         } // end of if ()
      } // end of for ()
      return null;
   }

   /**
    *
    * @return hashcode computed according to recommendations in Effective Java.
    */
   public int hashCode()
   {
      int result = 17;
      result = result * 37 + ((connectionURL == null)? 0: connectionURL.hashCode());
      result = result * 37 + ((driverClass == null)? 0: driverClass.hashCode());
      result = result * 37 + ((userName == null)? 0: userName.hashCode());
      result = result * 37 + ((password == null)? 0: password.hashCode());
      result = result * 37 + transactionIsolation;
      return result;
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    */
   public boolean equals(Object other)
   {
      if (this == other) 
      {
         return true;
      } // end of if ()
      if (getClass() != other.getClass()) 
      {
         return false;
      } // end of if ()
      LocalManagedConnectionFactory otherMcf = (LocalManagedConnectionFactory)other;
      return this.connectionURL.equals(otherMcf.connectionURL)
         && this.driverClass.equals(otherMcf.driverClass)
         && ((this.userName == null) ? otherMcf.userName == null: 
                                       this.userName.equals(otherMcf.userName))
         && ((this.password == null) ? otherMcf.password == null: 
                                       this.password.equals(otherMcf.password))
         && this.transactionIsolation == otherMcf.transactionIsolation;

   }

   //-----------Property setting code

   /**
    * Get the value of ConnectionURL.
    * @return value of ConnectionURL.
    */
   public String getConnectionURL() {
      return connectionURL;
   }
   
   /**
    * Set the value of ConnectionURL.
    * @param v  Value to assign to ConnectionURL.
    */
   public void setConnectionURL(final String  connectionURL) {
      this.connectionURL = connectionURL;
   }

   /**
    * Get the DriverClass value.
    * @return the DriverClass value.
    */
   public String getDriverClass()
   {
      return driverClass;
   }

   /**
    * Set the DriverClass value.
    * @param newDriverClass The new DriverClass value.
    */
   public void setDriverClass(final String driverClass)
   {
      this.driverClass = driverClass;
      driverLoaded = false;
   }

   /**
    * Get the value of userName.
    * @return value of userName.
    */
   public String getUserName() {
      return userName;
   }
   
   /**
    * Set the value of userName.
    * @param v  Value to assign to userName.
    */
   public void setUserName(final String  userName) {
      this.userName = userName;
   }
   
   /**
    * Get the value of password.
    * @return value of password.
    */
   public String getPassword() {
      return password;
   }
   
   /**
    * Set the value of password.
    * @param v  Value to assign to password.
    */
   public void setPassword(final String  password) {
      this.password = password;
   }
   
   /**
    * Gets the TransactionIsolation attribute of the JDBCManagedConnectionFactory
    * object
    *
    * @return   The TransactionIsolation value
    */
   public String getTransactionIsolation()
   {
      switch (this.transactionIsolation)
      {
         case Connection.TRANSACTION_NONE:
            return "TRANSACTION_NONE";
         case Connection.TRANSACTION_READ_COMMITTED:
            return "TRANSACTION_READ_COMMITTED";
         case Connection.TRANSACTION_READ_UNCOMMITTED:
            return "TRANSACTION_READ_UNCOMMITTED";
         case Connection.TRANSACTION_REPEATABLE_READ:
            return "TRANSACTION_REPEATABLE_READ";
         case Connection.TRANSACTION_SERIALIZABLE:
            return "TRANSACTION_SERIALIZABLE";
         case -1:
            return "DEFAULT";
         default:
            return Integer.toString(transactionIsolation);
      }
   }


   /**
    * Sets the TransactionIsolation attribute of the JDBCManagedConnectionFactory
    * object
    *
    * @param transactionIsolation  The new TransactionIsolation value
    */
   public void setTransactionIsolation(String transactionIsolation)
   {
      if (transactionIsolation.equals("TRANSACTION_NONE"))
      {
         this.transactionIsolation = Connection.TRANSACTION_NONE;
      }
      else if (transactionIsolation.equals("TRANSACTION_READ_COMMITTED"))
      {
         this.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
      }
      else if (transactionIsolation.equals("TRANSACTION_READ_UNCOMMITTED"))
      {
         this.transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;
      }
      else if (transactionIsolation.equals("TRANSACTION_REPEATABLE_READ"))
      {
         this.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ;
      }
      else if (transactionIsolation.equals("TRANSACTION_SERIALIZABLE"))
      {
         this.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE;
      }
      else
      {
         try
         {
            this.transactionIsolation = Integer.parseInt(transactionIsolation);
         }
         catch (NumberFormatException nfe)
         {
            throw new IllegalArgumentException("Setting Isolation level to unknown state: " + transactionIsolation);
         }
      }
   }

   //package access

   Properties getConnectionProperties(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      if (cri != null && cri.getClass() != LocalConnectionRequestInfo.class) 
      {
         throw new ResourceException("Wrong kind of ConnectionRequestInfo: " + cri.getClass());
      } // end of if ()
      
      Properties props = new Properties();
      if (subject != null) 
      {
         for (Iterator i = subject.getPrivateCredentials().iterator(); i.hasNext(); )
         {
            Object o = i.next();
            if (o instanceof PasswordCredential && ((PasswordCredential)o).getManagedConnectionFactory() == this) 
            {
               PasswordCredential cred = (PasswordCredential)o;
               props.setProperty("user", (cred.getUserName() == null)? "": cred.getUserName());
               props.setProperty("password", new String(cred.getPassword()));
               return props;
            } // end of if ()
         } // end of for ()
         throw new ResourceException("No matching credentials in Subject!");
      } // end of if ()
      LocalConnectionRequestInfo lcri = (LocalConnectionRequestInfo)cri;
      if (lcri != null) 
      {
         props.setProperty("user", (lcri.getUserName() == null)? "": lcri.getUserName());
         props.setProperty("password", (lcri.getPassword() == null)? "": lcri.getPassword());
	 return props;
      } // end of if ()
      if (userName != null)
      {
         props.setProperty("user", userName);
         props.setProperty("password", (password == null)? "": password);
      }
      return props;
   }
   
   //protected access

   /**
    * Check the driver for the given URL.  If it is not registered already
    * then register it.
    *
    * @param url   The JDBC URL which we need a driver for.
    */
   protected void checkDriver(final String url) throws ResourceException
   {
      // don't bother if it is loaded already
      if (driverLoaded) 
      {
         return;
      }
      log.debug("Checking driver for URL: " + url);
         
      if (driverClass == null) 
      {
         throw new ResourceException("No Driver class specified!");
      }

      // Check if the driver is already loaded, if not then try to load it
     
      if (isDriverLoadedForURL(url)) 
      {
         driverLoaded = true;
         return;
      } // end of if ()

      try 
      {
         //try to load the class... this should register with DriverManager.
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(driverClass);
         if (isDriverLoadedForURL(url)) 
         {
            driverLoaded = true;
            //return immediately, some drivers (Cloudscape) do not let you create an instance.
            return;
         } // end of if ()
         //We loaded the class, but either it didn't register 
         //and is not spec compliant, or is the wrong class.
         Driver driver = (Driver)clazz.newInstance();
         DriverManager.registerDriver(driver);
         if (isDriverLoadedForURL(url)) 
         {
            driverLoaded = true;
            return;
         } // end of if ()
         //We can even instantiate one, it must be the wrong class for the URL.
      }
      catch (Exception e) 
      {
         throw new org.jboss.resource.ResourceException
            ("Failed to register driver for: " + driverClass, e);
      }   

      throw new ResourceException("Apparently wrong driver class specified for URL: class: " + driverClass + ", url: " + url);
   }

   private boolean isDriverLoadedForURL(String url)
   {
      try 
      {
         DriverManager.getDriver(url);
         log.debug("Driver already registered for url: " + url);
         return true;
      }
      catch (Exception e)
      {
         log.debug("Driver not yet registered for url: " + url);
         return false;
      } // end of try-catch
   }
      

   protected String internalGetConnectionURL()
   {
      return connectionURL;
   }
}// LocalManagedConnectionFactory
