/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.jboss.resource.adapter.jdbc;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;

/**
 * ConnectionFactory implementation for JDBC drivers, i.e. javax.sql.DataSource.
 * As of this writing, CCI ConnectionFactory is inconsistent with DataSource due
 * to some incompatible exceptions. It doesn't actually implement the CCI
 * interfaces because JDBC already has well-defined interfaces, but it is the
 * equivalent. Note that this implementation will not work if JNDI Serialization
 * is used instead of a JNDI Reference. The Reference should be set by the app
 * server at deployment.
 *
 * @see       javax.resource.Referenceable
 * @author    Aaron Mulder <ammulder@alumni.princeton.edu>
 * @author    <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version   $Revision: 1.1.1.1 $
 */
public class JDBCDataSource implements DataSource, Serializable, Referenceable
{

   /*
    * No logging is done in this, so there is no log4j Category
    */
   private transient ConnectionManager manager;
   private transient ManagedConnectionFactory factory;
   private Reference jndiReference;

   /**
    * Constructor for the JDBCDataSource object
    *
    * @param manager  Description of Parameter
    * @param factory  Description of Parameter
    */
   public JDBCDataSource(ConnectionManager manager, ManagedConnectionFactory factory)
   {
      this.manager = manager;
      this.factory = factory;
   }

   /**
    * JNDI reference is set during deployment
    *
    * @param ref  The new Reference value
    */
   public void setReference(Reference ref)
   {
      jndiReference = ref;
   }

   /*
    * We ignore this and use log4j instead
    */
   /**
    * Sets the LogWriter attribute of the JDBCDataSource object
    *
    * @param writer                     The new LogWriter value
    * @exception java.sql.SQLException  Description of Exception
    */
   public void setLogWriter(PrintWriter writer)
          throws java.sql.SQLException
   {
   }

   /**
    * Sets the LoginTimeout attribute of the JDBCDataSource object
    *
    * @param timeout                    The new LoginTimeout value
    * @exception java.sql.SQLException  Description of Exception
    */
   public void setLoginTimeout(int timeout)
          throws java.sql.SQLException
   {
      throw new SQLException("Method setLoginTimeout() not implemented.");
   }

   /**
    * Used by JNDI
    *
    * @return   The Reference value
    */
   public Reference getReference()
   {
      return jndiReference;
   }

   /**
    * Gets the Connection attribute of the JDBCDataSource object
    *
    * @return                           The Connection value
    * @exception java.sql.SQLException  Description of Exception
    */
   public Connection getConnection()
          throws java.sql.SQLException
   {
      try
      {
         return (Connection)manager.allocateConnection(factory, null);
      }
      catch (ResourceException e)
      {
         throw new SQLException("Unable to get Connection: " + e);
      }
   }

   /**
    * Gets the Connection attribute of the JDBCDataSource object
    *
    * @param user                       Description of Parameter
    * @param password                   Description of Parameter
    * @return                           The Connection value
    * @exception java.sql.SQLException  Description of Exception
    */
   public Connection getConnection(String user, String password)
          throws java.sql.SQLException
   {
      try
      {
         return (Connection)manager.allocateConnection(factory, new JDBCConnectionRequestInfo(user, password));
      }
      catch (ResourceException e)
      {
         throw new SQLException("Unable to get Connection: " + e);
      }
   }

   /*
    * We ignore this and use log4j instead
    */
   /**
    * Gets the LogWriter attribute of the JDBCDataSource object
    *
    * @return                           The LogWriter value
    * @exception java.sql.SQLException  Description of Exception
    */
   public PrintWriter getLogWriter()
          throws java.sql.SQLException
   {
      return null;
   }

   /**
    * Gets the LoginTimeout attribute of the JDBCDataSource object
    *
    * @return                           The LoginTimeout value
    * @exception java.sql.SQLException  Description of Exception
    */
   public int getLoginTimeout()
          throws java.sql.SQLException
   {
      return 0;
   }
}
