/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.logging.Logger;


/**
 * JDBCStopCommand drops the table for this entity if specified in the xml.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1.1.1 $
 */
public class JDBCStopCommand {

   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private JDBCEntityMetaData entityMetaData;
   private Logger log;
 
   public JDBCStopCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();
      entityMetaData = entity.getMetaData();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }
   
   public void execute() {
      if(entityMetaData.getRemoveTable()) {
         log.debug("Dropping table for entity " + entity.getEntityName());
         dropTable(entity.getDataSource(), entity.getTableName());
      }

      // drop relation tables
      List cmrFields = entity.getCMRFields();
      for(Iterator iter = cmrFields.iterator(); iter.hasNext();) { 
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)iter.next();

         JDBCRelationMetaData relationMetaData = cmrField.getRelationMetaData();

         if(relationMetaData.isTableMappingStyle() &&
            relationMetaData.getTableExists()) {
            
            if(relationMetaData.getRemoveTable()) {
               dropTable(
                     relationMetaData.getDataSource(),
                     cmrField.getTableName());
            }
            relationMetaData.setTableExists(false);
         }
      }
   }
   
   private void dropTable(DataSource dataSource, String tableName) {
      Connection con = null;
      ResultSet rs = null;

      // was the table already delete?
      try {
         con = dataSource.getConnection();
         DatabaseMetaData dmd = con.getMetaData();
         rs = dmd.getTables(con.getCatalog(), null, tableName, null);
         if(!rs.next()) {
            return;
         }
      } catch(SQLException e) {
         log.debug("Error getting database metadata for DROP TABLE command. " +
               " DROP TABLE will not be executed. ", e);
         return;
      } finally {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(con);
      }

      // since we use the pools, we have to do this within a transaction

      // suspend the current transaction
      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction = null;
      try {
         oldTransaction = tm.suspend();
      } catch(Exception e) {
         log.error("Could not suspend current transaction before drop table. " +
               "'" + tableName + "' will not be dropped.", e);
      }

      try {
         Statement statement = null;
         try {
            con = dataSource.getConnection();
            statement = con.createStatement();
         
            // execute sql
            String sql = "DROP TABLE " + tableName;
            log.debug("Executing SQL: " + sql);
            statement.executeUpdate(sql);
         }
         finally
         {
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }

      } catch(Exception e) {
         log.debug("Could not drop table " + tableName);
      } finally {
         try {
            // resume the old transaction
            if(oldTransaction != null) {
               tm.resume(oldTransaction);
            }
         } catch(Exception e) {
            log.error("Could not reattach original transaction after " +
                  "drop table");
         }
      }
   }
}
