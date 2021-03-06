/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJBException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCStoreEntityCommand updates the row with the new state.
 * In the event that no field is dirty the command just returns.  
 * Note: read-only fields are never considered dirty.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.1.1.1 $
 */
public class JDBCStoreEntityCommand {
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private Logger log;
   
   public JDBCStoreEntityCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }
   
   public void execute(EntityEnterpriseContext ctx) {
      List dirtyFields = entity.getDirtyFields(ctx);
         
      if(dirtyFields.isEmpty()) {
         if(log.isTraceEnabled()) {
            log.trace("Store command NOT executed. Entity is not dirty: pk=" + 
                  ctx.getId());
         }
         return;
      }

      // generate sql
      StringBuffer sql = new StringBuffer(); 
      sql.append("UPDATE ").append(entity.getTableName());
      sql.append(" SET ").append(SQLUtil.getSetClause(dirtyFields));
      sql.append(" WHERE ").append(
            SQLUtil.getWhereClause(entity.getPrimaryKeyFields()));

      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected  = 0;
      try {
         // get the connection
         con = entity.getDataSource().getConnection();
         
         // create the statement
         if(log.isDebugEnabled()) {
            log.debug("Executing SQL: " + sql);
         }
         ps = con.prepareStatement(sql.toString());
         
         // set the parameters
         int index = 1;
         for(Iterator iter = dirtyFields.iterator(); iter.hasNext(); ) {
            JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
            index = field.setInstanceParameters(ps, index, ctx);
         }
         index = entity.setPrimaryKeyParameters(ps, index, ctx.getId());

         // execute statement
         rowsAffected = ps.executeUpdate();
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Store failed", e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // check results
      if(rowsAffected != 1) {
         throw new EJBException("Update failed. Expected one " +
               "affected row: rowsAffected=" + rowsAffected +
               "id=" + ctx.getId());
      }
      if(log.isDebugEnabled()) {
         log.debug("Rows affected = " + rowsAffected);
      }

      // Mark the inserted fields as clean.
      for(Iterator iter = dirtyFields.iterator(); iter.hasNext(); ) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         field.setClean(ctx);
      }
   }
}
