/**
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

/**
 * This class represents one pair of entities in a relation.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1.1.1 $
 */
public class RelationPair {
   private JDBCCMRFieldBridge leftCMRField;
   private JDBCCMRFieldBridge rightCMRField;

   private Object leftId;      
   private Object rightId;
   
   public RelationPair(
         JDBCCMRFieldBridge leftCMRField, Object leftId, 
         JDBCCMRFieldBridge rightCMRField, Object rightId) {

      this.leftCMRField = leftCMRField;
      this.leftId = leftId;
      
      this.rightCMRField = rightCMRField;
      this.rightId = rightId;
   }
   
   public JDBCCMRFieldBridge getLeftCMRField() {
      return leftCMRField;
   }
   
   public JDBCCMRFieldBridge getRightCMRField() {
      return rightCMRField;
   }
   
   public Object getLeftId() {
      return leftId;
   }
   
   public Object getRightId() {
      return rightId;
   }
   
   public boolean equals(Object obj) {
      if(obj instanceof RelationPair) {
         RelationPair pair = (RelationPair) obj;
         
         // check left==left and right==right
         if(leftCMRField == pair.leftCMRField && 
               rightCMRField == pair.rightCMRField &&
               leftId.equals(pair.leftId) && 
               rightId.equals(pair.rightId)) {
            return true;
         }
         
         // check left==right and right==left
         if(leftCMRField == pair.rightCMRField && 
               rightCMRField == pair.leftCMRField &&
               leftId.equals(pair.rightId) && 
               rightId.equals(pair.leftId)) {
            return true;
         }
      }
      return false;
   }
   
   public int hashCode() {
      return leftId.hashCode() ^ rightId.hashCode();
   }
}   

