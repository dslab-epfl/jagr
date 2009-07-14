
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
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;


/**
 * LocalManagedConnection.java
 *
 *
 * Created: Fri Apr 19 13:31:47 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class LocalManagedConnection 
   implements LocalTransaction, ManagedConnection  
{


   private final LocalManagedConnectionFactory mcf;
   private final Connection con;
   private final Properties props;
   private final int transactionIsolation;

   private final Collection cels = new ArrayList();
   private final Set handles = new HashSet();

   private boolean inManagedTransaction = false;
   private boolean jdbcAutoCommit = true;
   private boolean underlyingAutoCommit = true;

   public LocalManagedConnection (final LocalManagedConnectionFactory mcf, 
                                  final Connection con, 
                                  final Properties props, 
                                  final int transactionIsolation)
      throws SQLException
   {
      this.mcf = mcf;
      this.con = con;
      this.props = props;
      if (transactionIsolation == -1) 
      {
         this.transactionIsolation = con.getTransactionIsolation();
      } // end of if ()
      else
      {
         this.transactionIsolation = transactionIsolation;
         con.setTransactionIsolation(transactionIsolation);
      } // end of else
   }
   // implementation of javax.resource.spi.ManagedConnection interface

   /**
    *
    * @param param1 <description>
    */
   public synchronized void addConnectionEventListener(ConnectionEventListener cel)
   {
      cels.add(cel);
   }

   /**
    *
    * @param param1 <description>
    */
   public synchronized void removeConnectionEventListener(ConnectionEventListener cel)
   {
      cels.remove(cel);
   }

   /**
    *
    * @param param1 <description>
    * @exception javax.resource.ResourceException <description>
    */
   public void associateConnection(Object handle) throws ResourceException
   {
      if (!(handle instanceof LocalConnection))
      {
	 throw new ResourceException("Wrong kind of connection handle to associate" + handle);
      }
      ((LocalConnection)handle).setManagedConnection(this);
      synchronized(handles)
      {
	 handles.add(handle);
      }
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return this;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnection method
      return null;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnection method
      return null;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public XAResource getXAResource() throws ResourceException
   {
      throw new ResourceException("Local tx only!");
   }

   /**
    *
    * @param param1 <description>
    * @exception javax.resource.ResourceException <description>
    */
   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnection method
   }

   /**
    *
    * @exception javax.resource.ResourceException <description>
    */
   public void cleanup() throws ResourceException
   {
      synchronized (handles)
      {
         for (Iterator i = handles.iterator(); i.hasNext(); )
         {
            LocalConnection lc = (LocalConnection)i.next();
            lc.setManagedConnection(null);
         }
         handles.clear();
      }
      //reset all the properties we know about to defaults.
      jdbcAutoCommit = true;
      try 
      {
         if (transactionIsolation != con.getTransactionIsolation()) 
         {
            con.setTransactionIsolation(transactionIsolation);
         } // end of if ()
      }
      catch (SQLException e)
      {
         throw new ResourceException("Could not cleanup: " + e);
      } // end of try-catch
   }
   
   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Object getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      checkIdentity(subject, cri);
      LocalConnection lc =  new LocalConnection(this);
      handles.add(lc);
      return lc;
   }
   
   /**
    *
    * @exception javax.resource.ResourceException <description>
    */
   public void destroy() throws ResourceException
   {
      cleanup();
      try 
      {         
         con.close();
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }
   

   // implementation of javax.resource.spi.LocalTransaction interface
   
   /**
    *
    * @exception javax.resource.ResourceException <description>
    */
   public void commit() throws ResourceException
   {
      if (inManagedTransaction)
      {
         try 
         {
            inManagedTransaction = false;
            con.commit();
         }
         catch (SQLException e)
         {
            checkException(e);
         } // end of try-catch
      } // end of if ()
      else
      {
         throw new ResourceException("Trying to commit outside of a local tx");         
      } // end of else
      
   }
   
   /**
    *
    * @exception javax.resource.ResourceException <description>
    */
   public void rollback() throws ResourceException
   {
      if (inManagedTransaction) 
      {
         try 
         {
            inManagedTransaction = false;
            con.rollback();
         }
         catch (SQLException e)
         {
            e.printStackTrace();
try
   {
            checkException(e);
   }
catch (Exception e2) {}
         } // end of try-catch
      } // end of if ()
      else
      {
         throw new ResourceException("Trying to rollback outside of a local tx");         
      } // end of else
   }
   
   /**
    *
    * @exception javax.resource.ResourceException <description>
    */
   public void begin() throws ResourceException
   {
      if (!inManagedTransaction) 
      {
         try 
         {
            if (underlyingAutoCommit) 
            {
               underlyingAutoCommit = false;
               con.setAutoCommit(false);
            } // end of if ()
            inManagedTransaction = true;
         }
         catch (SQLException e)
         {
            checkException(e);
         } // end of try-catch
      } // end of if ()
      else
      {
         throw new ResourceException("Trying to begin a nested local tx");         
      } // end of else
   }
   


   //package methods

   void closeHandle(LocalConnection handle)
   {
      synchronized(handles)
      {
         handles.remove(handle);
      }
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(handle);
      Collection copy = null;
      synchronized(cels)
      {
         copy = new ArrayList(cels);
      }
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         cel.connectionClosed(ce);
      }
   }
   
   /**
    * Describe <code>connectionError</code> method here.
    *
    * @param e a <code>SQLException</code> value
    * @todo Figure out when connectionError should be called, and uncomment it.
    */
   void connectionError(SQLException e)
   {
      /*  I'm commenting this out for now until I have a better understanding of just when
          such an error event should be propagated.  Should this method be uncommented, many 
          methods that rely on eg integrity checks by provoking a SQLException "duplicate key"
          will fail.
          
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
      Collection copy = null;
      synchronized(cels)
      {
         copy = new ArrayList(cels);
      }
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         cel.connectionErrorOccurred(ce);
      }
      */
   }
   
   Connection getConnection()
      throws SQLException
   {
      if (con == null)
      {
         throw new SQLException("Connection has been destroyed!!!");
      }
      return con;
   }

   Properties getProps()
   {
      return props;
   }
   
   //private methods
   
   /**
    * Describe <code>checkIdentity</code> method here.
    *
    * @exception ResourceException if an error occurs
    * @todo check if subject or cri should have higher priority.
    */
   private void checkIdentity(Subject subject, ConnectionRequestInfo cri) 
      throws ResourceException
   {
      Properties newProps = mcf.getConnectionProperties(subject, cri);
      if (!props.equals(newProps)) 
      {
         throw new ResourceException("Wrong credentials passed to getConnection!");
      } // end of if ()
   }

   /**
    * The <code>checkTransaction</code> method makes sure the adapter follows the JCA
    * autocommit contract, namely all statements executed outside a container managed transaction 
    * or a component managed transaction should be autocommitted. To avoid continually calling
    * setAutocommit(enable) before and after container managed transactions, we keep track of the state
    * and check it before each transactional method call.
    *
    */
   void checkTransaction() throws SQLException
   {
      if (inManagedTransaction) 
      {
         return;
      } // end of if ()
      //Not in managed transaction. 
      //Should we autocommit?
      if (jdbcAutoCommit != underlyingAutoCommit) 
      {
         //set connection autocommit to agree with jdbcAutoCommit.
         underlyingAutoCommit = jdbcAutoCommit;
         con.setAutoCommit(jdbcAutoCommit);
      } // end of if ()
      
   }


   /**
    * Get the JdbcAutoCommit value.
    * @return the JdbcAutoCommit value.
    */
   boolean isJdbcAutoCommit()
   {
      return jdbcAutoCommit;
   }

   /**
    * Set the JdbcAutoCommit value.
    * @param newJdbcAutoCommit The new JdbcAutoCommit value.
    */
   void setJdbcAutoCommit(final boolean jdbcAutoCommit)
      throws SQLException
   {
      if (inManagedTransaction) 
      {
         throw new SQLException("You cannot set autocommit during a managed transaction!");
      } // end of if ()
      this.jdbcAutoCommit = jdbcAutoCommit;
   }

   void jdbcCommit() throws SQLException
   {
      if (inManagedTransaction) 
      {
         throw new SQLException("You cannot commit during a managed transaction!");
      } // end of if ()
      if (jdbcAutoCommit) 
      {
         throw new SQLException("You cannot commit with autocommit set!");
      } // end of if ()
      con.commit();
   }
   
   void jdbcRollback() throws SQLException
   {
      if (inManagedTransaction) 
      {
         throw new SQLException("You cannot rollback during a managed transaction!");
      } // end of if ()
      if (jdbcAutoCommit) 
      {
         throw new SQLException("You cannot rollback with autocommit set!");
      } // end of if ()
      con.rollback();
   }
   
   void jdbcRollback(Savepoint savepoint) throws SQLException
   {
@JDK1.4START@
      if (inManagedTransaction) 
      {
         throw new SQLException("You cannot rollback during a managed transaction!");
      } // end of if ()
      if (jdbcAutoCommit) 
      {
         throw new SQLException("You cannot rollback with autocommit set!");
      } // end of if ()
      con.rollback(savepoint);
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   //private methods

   private void checkException(SQLException e) throws ResourceException
   {
      connectionError(e);
      ResourceException re = new ResourceException("SQLException");
      re.setLinkedException(e);
      throw re;
   }
   
}// LocalManagedConnection
