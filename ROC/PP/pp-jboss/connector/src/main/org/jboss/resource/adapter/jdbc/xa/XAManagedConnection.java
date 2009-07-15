/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.jboss.resource.adapter.jdbc.xa;

import java.sql.Connection;
import java.sql.SQLException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.sql.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import org.jboss.resource.adapter.jdbc.BaseManagedConnection;

/**
 * ManagedConnection implementation for XADataSource connections. Does nothing
 * on cleanup, closes on destroy. This represents one physical connection to the
 * DB. It cannot be shared, and uses XAResources only (no LocalTransactions).
 *
 * @author    Aaron Mulder <ammulder@alumni.princeton.edu>
 * @author    <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version   $Revision: 1.1.1.1 $
 */
public class XAManagedConnection extends BaseManagedConnection
{
   private XAConnection con;
   private XADataSource source;
   private XAResource res;
   private int transactionIsolation;

   /*
    * For logging, use JBossCategory getLog() from the superclass
    */

   /**
    * Constructor for the XAManagedConnection object
    *
    * @param source                Description of Parameter
    * @param con                   Description of Parameter
    * @param user                  Description of Parameter
    * @param transactionIsolation  Description of Parameter
    */
   public XAManagedConnection(XADataSource source, XAConnection con, String user, int transactionIsolation)
   {
      super(user);
      this.con = con;
      this.source = source;
      this.transactionIsolation = transactionIsolation;
   }

   /**
    * Gets the LocalTransaction attribute of the XAManagedConnection object
    *
    * @return                                      The LocalTransaction value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public LocalTransaction getLocalTransaction()
          throws javax.resource.ResourceException
   {
      throw new ResourceException("getLocalTransaction not supported");
   }

   /**
    * Gets the XAResource attribute of the XAManagedConnection object
    *
    * @return                                      The XAResource value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public synchronized XAResource getXAResource()
          // ifedorenko remove synchronized if it does not make sense here
       throws javax.resource.ResourceException
    {
       try
       {
         // ifedorenko
         // Cache XAResource as a workaround for a bug in oracle xa driver
         // which returns a different XAResource for each getXAResource call
         // (see JDBC 2.0 spec, section 7.2.2 for a reason why it should not).
          //david jencks -- I don't see any problem in always caching the 
          //XAResource.  It can't do anything but speed up access.
         if (res == null)
         {
            res = con.getXAResource();
         }
         return res;
      }
      catch (SQLException e)
      {
         ResourceException re = new ResourceException("Unable to get XAResource: " + e);
         re.setLinkedException(e);
         throw re;
      }
   }

   /**
    * This implementation does not support re-authentication. It also does not
    * support connection sharing.
    *
    * @param sub                                   Description of Parameter
    * @param info                                  Description of Parameter
    * @return                                      The Connection value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public Object getConnection(Subject sub, ConnectionRequestInfo info)
          throws javax.resource.ResourceException
   {
      try
      {
         final Connection wrapper = con.getConnection();
         con.addConnectionEventListener(
            new ConnectionEventListener()
            {
               /**
                * #Description of the Method
                *
                * @param evt  Description of Parameter
                */
               public void connectionClosed(javax.sql.ConnectionEvent evt)
               {
                  javax.resource.spi.ConnectionEvent ce = new javax.resource.spi.ConnectionEvent(XAManagedConnection.this, javax.resource.spi.ConnectionEvent.CONNECTION_CLOSED);
                  ce.setConnectionHandle(wrapper);
                  fireConnectionEvent(ce);
                  con.removeConnectionEventListener(this);
               }

               /**
                * #Description of the Method
                *
                * @param evt  Description of Parameter
                */
               public void connectionErrorOccurred(javax.sql.ConnectionEvent evt)
               {
                  javax.resource.spi.ConnectionEvent ce = new javax.resource.spi.ConnectionEvent(XAManagedConnection.this, javax.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED);
                  ce.setConnectionHandle(wrapper);
                  fireConnectionEvent(ce);
                  con.removeConnectionEventListener(this);
               }
            });
         if (transactionIsolation != -1)
         {
            wrapper.setTransactionIsolation(transactionIsolation);
         }
         try
         {
            wrapper.setAutoCommit(false);
         }
         catch (Exception e)
         {
         }
         return wrapper;
      }
      catch (SQLException e)
      {
         ResourceException re = new ResourceException("Unable to get XAResource: " + e);
         re.setLinkedException(e);
         throw re;
      }
   }

   /**
    * Gets the MetaData attribute of the XAManagedConnection object
    *
    * @return                                      The MetaData value
    * @exception javax.resource.ResourceException  Description of Exception
    */
   public ManagedConnectionMetaData getMetaData()
          throws javax.resource.ResourceException
   {

      throw new java.lang.UnsupportedOperationException("Method getMetaData() not yet implemented.");
   }

   /**
    * #Description of the Method
    *
    * @exception ResourceException  Description of Exception
    */
   public void destroy()
          throws ResourceException
   {
      super.destroy();
      try
      {
         con.close();
      }
      catch (SQLException e)
      {
         ResourceException re = new ResourceException("Unable to close DB connection: " + e);
         re.setLinkedException(e);
         throw re;
      }
      con = null;
      source = null;
   }

   /**
    * #Description of the Method
    *
    * @exception ResourceException  Description of Exception
    */
   public void cleanup()
          throws ResourceException
   {
   }

   //To work around apparent jvm or compiler bug where inner class cannot call protected 
   //method on superclass of containing class.
   protected void fireConnectionEvent(javax.resource.spi.ConnectionEvent ce)
   {
      super.fireConnectionEvent(ce);
   }

   XADataSource getDataSource()
   {
      return source;
   }
}
