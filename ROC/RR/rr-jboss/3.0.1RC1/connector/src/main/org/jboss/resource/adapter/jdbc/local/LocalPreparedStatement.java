/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.resource.adapter.jdbc.local;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import roc.config.ROCConfig;


/**
 * LocalPreparedStatement.java
 *
 *
 * Created: Sat Apr 20 22:13:59 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class LocalPreparedStatement 
   extends LocalStatement
   implements PreparedStatement 
{

   private final PreparedStatement ps;

   public LocalPreparedStatement(final LocalConnection lc, final PreparedStatement ps) 
   {
      super(lc, ps);
      this.ps = ps;
   }
   // implementation of java.sql.PreparedStatement interface

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setBoolean(int parameterIndex, boolean value) throws SQLException
   {
      try 
      {
         ps.setBoolean(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setByte(int parameterIndex, byte value) throws SQLException
   {
      try 
      {
         ps.setByte(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setShort(int parameterIndex, short value) throws SQLException
   {
      try 
      {
         ps.setShort(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setInt(int parameterIndex, int value) throws SQLException
   {
      try 
      {
         ps.setInt(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setLong(int parameterIndex, long value) throws SQLException
   {
      try 
      {
         ps.setLong(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setFloat(int parameterIndex, float value) throws SQLException
   {
      try 
      {
         ps.setFloat(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setDouble(int parameterIndex, double value) throws SQLException
   {
      try 
      {
         ps.setDouble(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setURL(int parameterIndex, URL value) throws SQLException
   {
@JDK1.4START@
      try 
      {
         ps.setURL(parameterIndex, value);         
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
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setTime(int parameterIndex, Time value) throws SQLException
   {
      try 
      {
         ps.setTime(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setTime(int parameterIndex, Time value, Calendar calendar) throws SQLException
   {
      try 
      {
         ps.setTime(parameterIndex, value, calendar);         
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
   public boolean execute() throws SQLException
   {
      checkTransaction();
      try 
      {
 	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && 
	      ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      boolean result = ps.execute();  
	      //// 1. fetch the query from the StatmentMap and report it.
	      reportTrace(null);
	      return result;
	  }
	  else {
	  //// ROC PINPOINT MIKECHEN END   ////
	      return ps.execute();
	  }        
      }
      catch (SQLException e)
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && 
	      ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      //// 1. fetch the query from the StatmentMap and report it.
	      reportTrace(e);
	  }
	  //// ROC PINPOINT MIKECHEN END   ////
         checkException(e);
         return false;
      } // end of try-catch
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public ResultSetMetaData getMetaData() throws SQLException
   {
      try 
      {
         return ps.getMetaData();         
      }
      catch (SQLException e)
      {
         checkException(e);
         return null;
      } // end of try-catch
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public ResultSet executeQuery() throws SQLException
   {
      checkTransaction();
      try 
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && 
	      ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      ResultSet result = ps.executeQuery(); 
	      //// 1. fetch the query from the StatmentMap and report it.
	      reportTrace(null);
	      return result;
	  }
	  else {
	      //// ROC PINPOINT MIKECHEN END   ////
	      return ps.executeQuery();     
	  }
      }
      catch (SQLException e)
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && 
	      ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      //// 1. fetch the query from the StatmentMap and report it.
	      reportTrace(e);
	  }
	  //// ROC PINPOINT MIKECHEN END   ////

         checkException(e);
         return null;
      } // end of try-catch
   }

   /**
    *
    * @return <description>
    * @exception java.sql.SQLException <description>
    */
   public int executeUpdate() throws SQLException
   {
      checkTransaction();
      try 
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && 
	      ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      int result = ps.executeUpdate();  
	      //// 1. fetch the query from the StatmentMap and report it.
	      reportTrace(null);
	      return result;
	  }
	  else {
	      //// ROC PINPOINT MIKECHEN END   ////
	      return ps.executeUpdate();   
	  }      
      }
      catch (SQLException e)
      {
	  //// ROC PINPOINT MIKECHEN BEGIN ////
	  if( ROCConfig.ENABLE_PINPOINT && 
	      ROCConfig.ENABLE_PINPOINT_TRACING_DB ) {
	      //// 1. fetch the query from the StatmentMap and report it.
	      reportTrace(e);
	  }
	  //// ROC PINPOINT MIKECHEN END   ////
	  checkException(e);
	  return 0;
      } // end of try-catch
   }

   /**
    *
    * @exception java.sql.SQLException <description>
    */
   public void addBatch() throws SQLException
   {
      try 
      {
         ps.addBatch();         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setNull(int parameterIndex, int sqlType) throws SQLException
   {
      try 
      {
         ps.setNull(parameterIndex, sqlType);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
   {
      try 
      {
         ps.setNull(parameterIndex, sqlType, typeName);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setBigDecimal(int parameterIndex, BigDecimal value) throws SQLException
   {
      try 
      {
         ps.setBigDecimal(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setString(int parameterIndex, String value) throws SQLException
   {
      try 
      {
         ps.setString(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setBytes(int parameterIndex, byte[] value) throws SQLException
   {
      try 
      {
         ps.setBytes(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setDate(int parameterIndex, Date value) throws SQLException
   {
      try 
      {
         ps.setDate(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setDate(int parameterIndex, Date value, Calendar calendar) throws SQLException
   {
      try 
      {
         ps.setDate(parameterIndex, value, calendar);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setTimestamp(int parameterIndex, Timestamp value) throws SQLException
   {
      try 
      {
         ps.setTimestamp(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setTimestamp(int parameterIndex, Timestamp value, Calendar calendar) throws SQLException
   {
      try 
      {
         ps.setTimestamp(parameterIndex, value, calendar);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setAsciiStream(int parameterIndex, InputStream stream, int length) throws SQLException
   {
      try 
      {
         ps.setAsciiStream(parameterIndex, stream, length);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setUnicodeStream(int parameterIndex, InputStream stream, int length) throws SQLException
   {
@JDK1.4START@
      try 
      {
         ps.setUnicodeStream(parameterIndex, stream, length);         
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
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setBinaryStream(int parameterIndex, InputStream stream, int length) throws SQLException
   {
      try 
      {
         ps.setBinaryStream(parameterIndex, stream, length);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @exception java.sql.SQLException <description>
    */
   public void clearParameters() throws SQLException
   {
      try 
      {
         ps.clearParameters();         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @param param3 <description>
    * @param param4 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setObject(int parameterIndex, Object value, int sqlType, int scale) throws SQLException
   {
      try 
      {
         ps.setObject(parameterIndex, value, sqlType, scale);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setObject(int parameterIndex, Object value, int sqlType) throws SQLException
   {
      try 
      {
         ps.setObject(parameterIndex, value, sqlType);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setObject(int parameterIndex, Object value) throws SQLException
   {
      try 
      {
         ps.setObject(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @exception java.sql.SQLException <description>
    */
   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
   {
      try 
      {
         ps.setCharacterStream(parameterIndex, reader, length);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setRef(int parameterIndex, Ref value) throws SQLException
   {
      try 
      {
         ps.setRef(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setBlob(int parameterIndex, Blob value) throws SQLException
   {
      try 
      {
         ps.setBlob(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setClob(int parameterIndex, Clob value) throws SQLException
   {
      try 
      {
         ps.setClob(parameterIndex, value);         
      }
      catch (SQLException e)
      {
         checkException(e);
      } // end of try-catch
   }

   /**
    *
    * @param param1 <description>
    * @param value <description>
    * @exception java.sql.SQLException <description>
    */
   public void setArray(int parameterIndex, Array value) throws SQLException
   {
      try 
      {
         ps.setArray(parameterIndex, value);         
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
   public ParameterMetaData getParameterMetaData() throws SQLException
   {
@JDK1.4START@
      try 
      {
         return ps.getParameterMetaData();         
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

    //// ROC PINPOINT MIKECHEN EMK BEGIN ////
    java.util.Map PP_originInfo = null;

    static java.util.Map PP_attributes = null;

    protected void reportTrace( Exception e ) {

	try {
	    if( PP_attributes == null ) {
		PP_attributes = new java.util.HashMap();
		PP_attributes.put( "observationLocation", 
				   "org.jboss.resource.adapter.jdbc.local.LocalPreparedStatement" );
	    }

	    if( PP_originInfo == null ) {
		PP_originInfo = new java.util.HashMap( roc.pinpoint.tracing.java.EnvironmentDetails.GetDetails() );
		PP_originInfo.put( "name", roc.pinpoint.tracing.sql.SqlStatementsMap.get( this ));
		PP_originInfo.put( "type", "sql" );
	    }

	    roc.pinpoint.tracing.RequestInfo PP_reqInfo =
		roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
	    PP_reqInfo.incrementSeqNum();
	
	    roc.pinpoint.tracing.Observation PP_obs =
		new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_DATABASE_USE,
						      PP_reqInfo,
						      PP_originInfo,
						      null,
						      PP_attributes );
	    roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );

	    if( e != null ) {
		PP_reqInfo.incrementSeqNum();
		java.util.Map PP_rawDetails = new java.util.HashMap();
		StackTraceElement[] PP_ste = e.getStackTrace();
		java.util.List PP_stacktrace = new java.util.ArrayList( PP_ste.length );
		for( int PP_i=0; PP_i < PP_ste.length; PP_i++ ) {
		    // todo, later, we might want a more structure storage 
		    //       of the details in PP_ste[ PP_i ], rather than
		    //       just a toString() dump.
		    PP_stacktrace.add( PP_ste[ PP_i ].toString() );
		}
		PP_rawDetails.put( "exception", e.toString() );
		PP_rawDetails.put( "stacktrace", PP_stacktrace );
		PP_obs = new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_ERROR,
							       PP_reqInfo,
							       PP_originInfo,
							       PP_rawDetails,
							       PP_attributes );
		roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );
	    }
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}

    }
    //// ROC PINPOINT MIKECHEN EMK END   ////
    
}// LocalPreparedStatement
