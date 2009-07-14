
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.adapter.jdbc.local;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import roc.config.ROCConfig;


/**
 * LocalConnection.java
 *
 *
 * Created: Fri Apr 19 13:35:32 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class LocalConnection implements Connection {

   private LocalManagedConnection mc;

   private boolean closed = false;

   public LocalConnection (final LocalManagedConnection mc)
   {
      this.mc = mc;   
   }

   void setManagedConnection(final LocalManagedConnection mc)
   {
      this.mc = mc;
   }

   // implementation of java.sql.Connection interface

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setReadOnly(boolean readOnly) throws SQLException
   {
      checkStatus();
      try 
      {
         mc.getConnection().setReadOnly(readOnly);
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch      
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public boolean isReadOnly() throws SQLException
   {
      checkStatus();
      try 
      {
         return mc.getConnection().isReadOnly();
      }
      catch (SQLException e)
      {
         checkException(e);
         return false;
      } // end of try-catch      
   }

   /**
    *
    * @exception java.sql.SQLException <description>
    */
   public void close() throws SQLException
   {
      closed = true;
      if (mc != null) 
      {
         mc.closeHandle(this);
      } // end of if ()
      mc = null;
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public boolean isClosed() throws SQLException
   {
      return closed;
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public Statement createStatement() throws SQLException
   {
      checkStatus();
      try 
      {
         return new LocalStatement(this, mc.getConnection().createStatement());
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException
   {
      checkStatus();
      try 
      {
         return new LocalStatement(this, mc.getConnection().createStatement(resultSetType, resultSetConcurrency));
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         return new LocalStatement(this, mc.getConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      checkStatus();
      try 
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	  return trackStatement(new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql)), sql);
	  }
	  else {
	  //// ROC PINPOINT MIKECHEN END   ////
	  // ORIGINAL
	      return new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql));
	  }
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      checkStatus();
      try 
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      return trackStatement(new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency)), sql);
	  //// ROC PINPOINT MIKECHEN END   ////
	  }
	  else {
	  // ORIGINAL
	  return new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency));
	  }
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @param param4 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      return trackStatement(new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)), sql);
	  }
	  //// ROC PINPOINT MIKECHEN END   ////
	  else {
	  // ORIGINAL
	  return new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	  }
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	  return trackStatement(new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, autoGeneratedKeys)), sql);
	  }
	  //// ROC PINPOINT MIKECHEN END   ////
	  else {
	  // ORIGINAL
	  return new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, autoGeneratedKeys));
	  }
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	  return trackStatement(new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, columnIndexes)), sql);
	  }
	  //// ROC PINPOINT MIKECHEN END   ////
	  else {
	  // ORIGINAL
	  return new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, columnIndexes));
	  }
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      return trackStatement(new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, columnNames)), sql);
	  }
	  //// ROC PINPOINT MIKECHEN END   ////
	  else {
	  // ORIGINAL
	  return new LocalPreparedStatement(this, mc.getConnection().prepareStatement(sql, columnNames));
	  }
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public CallableStatement prepareCall(String sql) throws SQLException
   {
      checkStatus();
      try 
      {
         return new LocalCallableStatement(this, mc.getConnection().prepareCall(sql));
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      checkStatus();
      try 
      {
         return new LocalCallableStatement(this, mc.getConnection().prepareCall(sql, resultSetType, resultSetConcurrency));
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @param param4 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         return new LocalCallableStatement(this, mc.getConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public String nativeSQL(String sql) throws SQLException
   {
      checkStatus();
      try 
      {
         return mc.getConnection().nativeSQL(sql);
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setAutoCommit(boolean autocommit) throws SQLException
   {
      checkStatus();
      mc.setJdbcAutoCommit(autocommit);
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public boolean getAutoCommit() throws SQLException
   {
      checkStatus();
      return mc.isJdbcAutoCommit();
   }

   /**
    *
    * @exception java.sql.SQLException <description>
    */
   public void commit() throws SQLException
   {
      checkStatus();
      mc.jdbcCommit();
   }

   /**
    *
    * @exception java.sql.SQLException <description>
    */
   public void rollback() throws SQLException
   {
      checkStatus();
      mc.jdbcRollback();
   }

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    */
   public void rollback(Savepoint savepoint) throws SQLException
   {
      checkStatus();
      mc.jdbcRollback(savepoint);
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public DatabaseMetaData getMetaData() throws SQLException
   {
      checkStatus();
      try 
      {
         return mc.getConnection().getMetaData();
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setCatalog(String catalog) throws SQLException
   {
      checkStatus();
      try 
      {
         mc.getConnection().setCatalog(catalog);
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch      
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public String getCatalog() throws SQLException
   {
      checkStatus();
      try 
      {
         return mc.getConnection().getCatalog();
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    * @todo check we are not in a managed transaction.
    */
   public void setTransactionIsolation(int isolationLevel) throws SQLException
   {
      checkStatus();
      //Should check we are not in a managed transaction!
      try 
      {
         mc.getConnection().setTransactionIsolation(isolationLevel);
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch      
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public int getTransactionIsolation() throws SQLException
   {
      checkStatus();
      try 
      {
         return mc.getConnection().getTransactionIsolation();
      }
      catch (SQLException e)
      {
         checkException(e);
         throw e;
      } // end of try-catch      
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public SQLWarning getWarnings() throws SQLException
   {
      checkStatus();
      return mc.getConnection().getWarnings();
   }

   /**
    *
    * @exception java.sql.SQLException <description>
    */
   public void clearWarnings() throws SQLException
   {
      checkStatus();
      mc.getConnection().clearWarnings();
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public Map getTypeMap() throws SQLException
   {
      checkStatus();
      try 
      {
         return mc.getConnection().getTypeMap();
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setTypeMap(Map typeMap) throws SQLException
   {
      checkStatus();
      try 
      {
         mc.getConnection().setTypeMap(typeMap);
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch      
   }

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setHoldability(int holdability) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         mc.getConnection().setHoldability(holdability);
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public int getHoldability() throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         return mc.getConnection().getHoldability();
      }
      catch (SQLException e)
      {
         checkException(e);
         throw e;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public Savepoint setSavepoint() throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         return mc.getConnection().setSavepoint();
      }
      catch (SQLException e)
      {
         checkException(e);
         throw e;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public Savepoint setSavepoint(String name) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         return mc.getConnection().setSavepoint(name);
      }
      catch (SQLException e)
      {
         checkException(e);
         throw e;
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }

   /**
    *
    * @param param1 <description>
    * @exception java.sql.SQLException <description>
    */
   public void releaseSavepoint(Savepoint savepoint) throws SQLException
   {
@JDK1.4START@
      checkStatus();
      try 
      {
         mc.getConnection().releaseSavepoint(savepoint);
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch      
@JDK1.4END@
@JDK1.3START@
   throw new SQLException("JDK1.4 method not available in JDK1.3");
@JDK1.3END@
   }
   //Public non-jdbc methods

   public Connection getUnderlyingConnection() throws SQLException
   {
      checkStatus();
      return mc.getConnection();
   }

   //package methods

   void checkTransaction()
      throws SQLException
   {
      checkStatus();
      mc.checkTransaction();
   }

   //protected methods

   /**
    * The checkStatus method checks that the handle has not been closed and that it is associated with a managed connection.
    *
    * @exception SQLException if an error occurs
    */
   protected void checkStatus() throws SQLException
   {
      if (closed) 
      {
         throw new SQLException("Connection handle has been closed and is unusable");
      } // end of if ()
      if (mc == null) 
      {
         throw new SQLException("Connection handle is not currently associated with a ManagedConnection");
      } // end of if ()
   }

   /**
    * The base checkException method rethrows the supplied exception, informing the ManagedConnection of the error.
    * Subclasses may override this to filter exceptions based on their severity.
    *
    * @param e a <code>SQLException</code> value
    * @exception Exception if an error occurs
    */
   protected void checkException(SQLException e) throws SQLException
   {
      if (mc != null) 
      {
         mc.connectionError(e);
      } // end of if ()
      
      throw e;
   }
      
    //// ROC PINPOINT MIKECHEN BEGIN ////
    //// 1. create the statement and save the SQL query in a static mapping
    //// somewhere where the key is a reference to the PreparedStatment and
    //// the values are the corresponding SQL statements.
    //// We will use a WeakReference to these statements because we 
    //// want it to be garbage collected when it's no longer being used.
    protected PreparedStatement trackStatement(PreparedStatement statement, String sql) {
	roc.pinpoint.tracing.sql.SqlStatementsMap.put(statement, sql);
	return statement;
    }	
    //// ROC PINPOINT MIKECHEN END   ////


}
