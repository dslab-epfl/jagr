/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.jboss.resource.adapter.jdbc.xa;

import java.io.PrintWriter;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jdbc.JDBCConnectionRequestInfo;
//for transaction isolation constants

import org.jboss.resource.adapter.jdbc.JDBCDataSource;

/**
 * ManagedConnectionFactory implementation for XADataSource connections. You
 * give it an XADataSource, user, and password and it generated connections.
 * Matches connections based on JDBC user and XADataSource. Currently supports
 * managed mode only. <p>
 *
 * In an environment where you invoke this class directly, the preferred way to
 * configure it is to call setUserName, setPassword, and setXADataSource with a
 * configured XADataSource. In an environment where this is deployed as a RAR
 * into a server and configured via a deployment descriptor, you'll need to use
 * setXADataSourceClass and setXADataSourceProperties or setXADataSourceJNDIName
 * instead of setXADataSource. The XADataSourceProperties value should be a
 * string in the format <tt>name=value;name=value;name=value...</tt> where the
 * names and values are properties of your XADataSource implementation. For
 * example:</p> <pre>
 *
 * xaDataSourceJNDIName java:jdbc/SomeXADataSource - or - xaDataSourceClass
 * com.dbproduct.XADataSourceImpl xaDataSourceProperties url=jdbc:dbproduct:config;port=9999
 * </pre>
 *
 * @author    Aaron Mulder <ammulder@alumni.princeton.edu>
 * @author    Larry Sanderson <larrys@mrstock.com>
 * @version   $Revision: 1.1.1.1 $
 */
public class XAManagedConnectionFactory implements ManagedConnectionFactory
{

   private Logger log = Logger.getLogger(XAManagedConnectionFactory.class);

   private transient XADataSource xads;
   private String username;
   private String password;
   private String xaDataSourceClass;
   private String xaDataSourceProperties;
   private String xaDataSourceName;

   private int transactionIsolation = -1;

   /**
    * Constructor for the XAManagedConnectionFactory object
    */
   public XAManagedConnectionFactory()
   {
   }

   /*
    * We ignore this and use log4j
    */
   /**
    * Sets the LogWriter attribute of the XAManagedConnectionFactory object
    *
    * @param writer                                The new LogWriter value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public void setLogWriter(PrintWriter writer)
          throws javax.resource.ResourceException
   {
   }

   /**
    * Sets the UserName attribute of the XAManagedConnectionFactory object
    *
    * @param username  The new UserName value
    */
   public void setUserName(String username)
   {
      this.username = username;
   }

   /**
    * Sets the Password attribute of the XAManagedConnectionFactory object
    *
    * @param password  The new Password value
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * Sets the XADataSource attribute of the XAManagedConnectionFactory object
    *
    * @param xads  The new XADataSource value
    */
   public void setXADataSource(XADataSource xads)
   {
      this.xads = xads;
   }

   /**
    * Sets the XADataSourceClass attribute of the XAManagedConnectionFactory
    * object
    *
    * @param className  The new XADataSourceClass value
    */
   public void setXADataSourceClass(String className)
   {
      xaDataSourceClass = className;
   }

   /**
    * Sets the XADataSourceProperties attribute of the XAManagedConnectionFactory
    * object
    *
    * @param props  The new XADataSourceProperties value
    */
   public void setXADataSourceProperties(String props)
   {
      xaDataSourceProperties = props;
   }

   /*
    * As far as I know, there is no way to use this
    * since RawXADataSourceLoader is gone
    */
   /**
    * Sets the XADataSourceJNDIName attribute of the XAManagedConnectionFactory
    * object
    *
    * @param name  The new XADataSourceJNDIName value
    */
   public void setXADataSourceJNDIName(String name)
   {
      xaDataSourceName = name;
   }

   /**
    * Sets the TransactionIsolation attribute of the XAManagedConnectionFactory
    * object
    *
    * @param transactionIsolation  The new TransactionIsolation value
    */
   public void setTransactionIsolation(String transactionIsolation)
   {
      log.trace("TransactionIsolation set:" + transactionIsolation);
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

   /**
    * Gets the LogWriter attribute of the XAManagedConnectionFactory object
    *
    * @return                                      The LogWriter value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public PrintWriter getLogWriter()
          throws javax.resource.ResourceException
   {
      return null;
   }

   /**
    * Gets the UserName attribute of the XAManagedConnectionFactory object
    *
    * @return   The UserName value
    */
   public String getUserName()
   {
      return username;
   }

   /**
    * Gets the Password attribute of the XAManagedConnectionFactory object
    *
    * @return   The Password value
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * Gets the XADataSource attribute of the XAManagedConnectionFactory object
    *
    * @return   The XADataSource value
    */
   public XADataSource getXADataSource()
   {
      return xads;
   }

   /**
    * Gets the XADataSourceClass attribute of the XAManagedConnectionFactory
    * object
    *
    * @return   The XADataSourceClass value
    */
   public String getXADataSourceClass()
   {
      return xaDataSourceClass;
   }

   /**
    * Gets the XADataSourceProperties attribute of the XAManagedConnectionFactory
    * object
    *
    * @return   The XADataSourceProperties value
    */
   public String getXADataSourceProperties()
   {
      return xaDataSourceProperties;
   }

   /**
    * Gets the XADataSourceJNDIName attribute of the XAManagedConnectionFactory
    * object
    *
    * @return   The XADataSourceJNDIName value
    */
   public String getXADataSourceJNDIName()
   {
      return xaDataSourceName;
   }

   /**
    * Gets the TransactionIsolation attribute of the XAManagedConnectionFactory
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
    * #Description of the Method
    *
    * @param mgr                                   Description of Parameter
    * @return                                      Description of the Returned
    *      Value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public Object createConnectionFactory(ConnectionManager mgr)
          throws javax.resource.ResourceException
   {
      DataSource ds = new JDBCDataSource(mgr, this);
      return ds;
   }

   /**
    * #Description of the Method
    *
    * @return                                      Description of the Returned
    *      Value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public Object createConnectionFactory()
          throws javax.resource.ResourceException
   {
      throw new java.lang.UnsupportedOperationException("Must be used in managed mode");
   }

   /**
    * #Description of the Method
    *
    * @param sub                                   Description of Parameter
    * @param info                                  Description of Parameter
    * @return                                      Description of the Returned
    *      Value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public ManagedConnection createManagedConnection(Subject sub, ConnectionRequestInfo info)
          throws javax.resource.ResourceException
   {
      // Set user and password to default
      String user = username;
      String pw = password;

      // Check passed Subject and ConnectionRequestInfo for user/password overrides
      if (sub != null)
      {
         Set creds = sub.getPrivateCredentials(javax.resource.spi.security.PasswordCredential.class);
         for (Iterator it = creds.iterator(); it.hasNext(); )
         {
            PasswordCredential pc = (PasswordCredential)it.next();
            user = pc.getUserName();
            pw = new String(pc.getPassword());
            break;
         }
      }
      else
      {
         if (info != null)
         {
            JDBCConnectionRequestInfo jdbcInfo = (JDBCConnectionRequestInfo)info;
            user = jdbcInfo.user;
            pw = jdbcInfo.password;
         }
      }

      // Create the connection
      try
      {
         XAConnection con = getXADS().getXAConnection(user, pw);
         ManagedConnection mc = new XAManagedConnection(xads, con, user, transactionIsolation);
         return mc;
      }
      catch (SQLException e)
      {
         throw new ResourceException("Unable to create DB XAConnection: " + e);
      }
   }

   /**
    * #Description of the Method
    *
    * @param cons                                  Description of Parameter
    * @param sub                                   Description of Parameter
    * @param info                                  Description of Parameter
    * @return                                      Description of the Returned
    *      Value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public ManagedConnection matchManagedConnections(Set cons, Subject sub, ConnectionRequestInfo info)
          throws javax.resource.ResourceException
   {
      // Set user and password to default
      String user = username;
      String pw = password;

      // Check passed Subject and ConnectionRequestInfo for user/password overrides
      if (sub != null)
      {
         Set creds = sub.getPrivateCredentials(javax.resource.spi.security.PasswordCredential.class);
         for (Iterator it = creds.iterator(); it.hasNext(); )
         {
            PasswordCredential pc = (PasswordCredential)it.next();
            user = pc.getUserName();
            pw = new String(pc.getPassword());
            break;
         }
      }
      else
      {
         if (info != null)
         {
            if (!(info instanceof JDBCConnectionRequestInfo))
            {
               throw new ResourceException("Passed ConnectionRequestInfo class '" + info.getClass().getName() + "' to XAManagedConnectionFactory!");
            }
            JDBCConnectionRequestInfo jdbcInfo = (JDBCConnectionRequestInfo)info;
            user = jdbcInfo.user;
            pw = jdbcInfo.password;
         }
      }

      // Check the connections in the Set
      for (Iterator it = cons.iterator(); it.hasNext(); )
      {
         Object unknown = it.next();
         if (!(unknown instanceof XAManagedConnection))
         {
            continue;
         }
         XAManagedConnection con = (XAManagedConnection)unknown;
         String conUser = con.getUser();
         if (((conUser == null)? (user == null) : conUser.equals(user))
             && con.getDataSource().equals(xads))
         {
            return con;
         }
      }
      return null;
   }

   private XADataSource getXADS()
   {
      if (xads != null)
      {
         return xads;
      }
      if (xaDataSourceClass != null)
      {
         try
         {
            log.trace("XADatasourceClass: " + xaDataSourceClass);
            Class cls = Class.forName(xaDataSourceClass);
            xads = (XADataSource)cls.newInstance();
            log.trace("got DataSource instance");

            Properties props = parseProperties();
            populate(xads, props);
            return xads;
         }
         catch (Exception e)
         {
            xads = null;
            log.warn("Unable to create and initialize XADataSource:", e);
         }
      }
      else if (xaDataSourceName != null)
      {
         try
         {
            InitialContext ic = new InitialContext();
            xads = (XADataSource)ic.lookup(xaDataSourceName);
            return xads;
         }
         catch (Exception e)
         {
            xads = null;
            log.warn("Unable to reach XADataSource in JNDI:", e);
         }
      }
      return null;
   }

   private Properties parseProperties()
   {
      Properties props = new Properties();
      log.trace("parsing props: " + xaDataSourceProperties);
      StringTokenizer tokens = new StringTokenizer(xaDataSourceProperties, ";=");

      while (tokens.hasMoreTokens())
      {
         String key = tokens.nextToken();
         String value = tokens.nextToken();
         props.put(key, value);
      }
      return props;
   }

   /**
    * Populate the obj with the properties in the Map. No exceptions
    * are thrown if a property is specified for which there is no setter,
    * however a warning is issued.
    *
    * @param props the properties to use.
    */
   private void populate(Object obj, Map props)
   {
      // get all valid set methods -
      Method[] methods = obj.getClass().getMethods();
      HashMap setters = new HashMap();

      for (int i = 0; i < methods.length; i++)
      {
         Method meth = methods[i];
         String name = meth.getName();
         if (name.startsWith("set"))
         {
            String attrName = name.substring(3);
            if (meth.getParameterTypes().length == 1)
            {
               Class type = meth.getParameterTypes()[0];
               if (type == String.class || type.isPrimitive())
               {
                  setters.put(attrName, meth);
               }
            }
         }
      }

      for (Iterator i = props.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry)i.next();
         String attributeName = (String)entry.getKey();
         String attributeValue = (String)entry.getValue();

         Method meth = (Method)setters.get(attributeName);
         if (meth != null)
         {
            try
            {
               Object val = convert(attributeValue, meth.getParameterTypes()[0]);
               meth.invoke(obj, new Object[] {val });
            }
            catch (Exception e)
            {
               log.warn("Unable to set XADataSource property " +
                        attributeName + "=" +
                        attributeValue + ":");
            }
         }
         else
         {
            log.warn("No setter method for attribute " + attributeName +
                     " (value=" + attributeValue + ").");
         }
      }
   }

   /**
    * Convert the specified value to the specified class. This should be
    * replaced with the org.apache.commons.beanUtils.ConvertUtils class.
    *
    * @return the value converted to type
    * @param value the String value to convert
    * @type the class to convert to. Must be a primitive type or String.
    */
   private Object convert(String value, Class type)
   {
      if (type == String.class)
      {
         return value;
      }
      if (type == Integer.TYPE)
      {
         return Integer.valueOf(value);
      }
      if (type == Double.TYPE)
      {
         return Double.valueOf(value);
      }
      if (type == Boolean.TYPE) {
         return new Boolean(
            value.equalsIgnoreCase("true") ||
            value.equalsIgnoreCase("on") ||
            value.equalsIgnoreCase("yes")
            );
      }
      if (type == Long.TYPE)
      {
         return Long.valueOf(value);
      }
      if (type == Float.TYPE)
      {
         return Float.valueOf(value);
      }
      if (type == Byte.TYPE)
      {
         return Byte.valueOf(value);
      }
      if (type == Short.TYPE)
      {
         return Short.valueOf(value);
      }
      if (type == Character.TYPE)
      {
         return new Character(value.charAt(0));
      }
      if (type == Double.TYPE)
      {
         return Double.valueOf(value);
      }
      return null;
   }

}

