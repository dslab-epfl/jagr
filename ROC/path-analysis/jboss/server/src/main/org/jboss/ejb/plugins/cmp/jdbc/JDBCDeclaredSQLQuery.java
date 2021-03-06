/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.jboss.deployment.DeploymentException;

import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCDeclaredQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;

/**
 * This class generates a query based on the delcared-sql xml specification.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.1.1.1 $
 */
public class JDBCDeclaredSQLQuery extends JDBCAbstractQueryCommand {
   
   private JDBCDeclaredQueryMetaData metadata;

   /**
    * Creted a defined finder command based on the information
    * in a declared-sql declaration.
    */
   public JDBCDeclaredSQLQuery(JDBCStoreManager manager, 
         JDBCQueryMetaData q) throws DeploymentException {

      super(manager, q);

      metadata = (JDBCDeclaredQueryMetaData) q;
      
      // set the select object (either selectEntity or selectField)
      initSelectObject(manager);

      // set the preload fields
      JDBCReadAheadMetaData readAhead = metadata.getReadAhead();
      if(getSelectEntity() != null && readAhead.isOnFind()) {
         String eagerLoadGroupName = readAhead.getEagerLoadGroup();
         setPreloadFields(getSelectEntity().getLoadGroup(eagerLoadGroupName));
      }

      // set the sql and parameters 
      String sql = buildSQL();
      setSQL(parseParameters(sql));
    }
 
   /**
    * Initializes the entity or field to be selected.
    * @throws DeploymentException if the specified object is invalid or
    *    non-existant
    */
   private void initSelectObject(JDBCStoreManager manager)
         throws DeploymentException {

      String entityName = metadata.getEJBName();

      // if no name is specified we are done
      if(entityName == null) {
         return;
      }

      Catalog catalog = (Catalog)manager.getApplicationData("CATALOG");

      JDBCEntityBridge entity = 
            (JDBCEntityBridge)catalog.getEntityByEJBName(entityName);
      if(entity == null) {
         throw new DeploymentException("Unknown entity: " + entityName);
      }
      
      String fieldName = metadata.getFieldName();
      if(fieldName == null) {
         setSelectEntity(entity);
      } else {
         JDBCCMPFieldBridge field = entity.getCMPFieldByName(fieldName);
         if(field == null) {
            throw new DeploymentException("Unknown cmp field: " + fieldName);
         }
         setSelectField(field);
      }
   }
   
   /**
    * Builds the sql statement based on the delcared-sql metadata specification.
    * @return the sql statement for this query
    */
   private String buildSQL() {
      StringBuffer sql = new StringBuffer();

      sql.append("SELECT ");
      if(metadata.isSelectDistinct()) {
         sql.append("DISTINCT ");
      }
      
      String alias = metadata.getAlias();
      String from = metadata.getFrom();
      if(getSelectField() == null) {

         // we are selecting a full entity
         String table = getSelectEntity().getTableName();

         // get a list of all fields to be loaded
         List loadFields = new ArrayList();
         loadFields.addAll(getSelectEntity().getPrimaryKeyFields());
         loadFields.addAll(getPreloadFields());

         if(alias != null && alias.trim().length()>0) {
            sql.append(SQLUtil.getColumnNamesClause(loadFields, alias));
            sql.append(" FROM ");
            sql.append(table).append(" ").append(alias);
            if(from != null && from.trim().length()>0) {
               sql.append(" ").append(from);
            }
         } else if(from != null && from.trim().length()>0) {
            sql.append(SQLUtil.getColumnNamesClause(loadFields, table));
            sql.append(" FROM ").append(table).append(" ").append(from);
         } else {
            sql.append(SQLUtil.getColumnNamesClause(loadFields));
            sql.append(" FROM ").append(table);
         }
      } else {

         // we are just selecting one field
         JDBCCMPFieldBridge selectField = getSelectField();
         String table = 
               getSelectField().getManager().getEntityBridge().getTableName();
         if(alias != null && alias.trim().length()>0) {
            sql.append(SQLUtil.getColumnNamesClause(selectField, alias));
            sql.append(" FROM ");
            sql.append(table).append(" ").append(alias);
            sql.append(" ").append(from);
         } else if(from != null && from.trim().length()>0) {
            sql.append(SQLUtil.getColumnNamesClause(selectField, table));
            sql.append(" FROM ").append(table).append(" ").append(from);
         } else {
            sql.append(SQLUtil.getColumnNamesClause(selectField));
            sql.append(" FROM ").append(table);
         }
      }
       
      String where = metadata.getWhere();
      if(where != null && where.trim().length() > 0) {
         sql.append(" WHERE ").append(where);
      }
      
      String order = metadata.getOrder();
      if(order != null && order.trim().length() > 0) {
         sql.append(" ORDER BY ").append(order);
      }

      String other = metadata.getOther();
      if(other != null && other.trim().length() > 0) {
         sql.append(" ").append(other);
      }
      return sql.toString();
   }
}
