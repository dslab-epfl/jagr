package org.jboss.jmx.adaptor.model;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

/** An mbean ObjectNamd and MBeanInfo pair
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanData
{
   private ObjectName objectName;
   private MBeanInfo metaData;

   /** Creates a new instance of MBeanInfo */
   public MBeanData(ObjectName objectName, MBeanInfo metaData)
   {
      this.objectName = objectName;
      this.metaData = metaData;
   }

   /** Getter for property objectName.
    * @return Value of property objectName.
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }
   
   /** Setter for property objectName.
    * @param objectName New value of property objectName.
    */
   public void setObjectName(ObjectName objectName)
   {
      this.objectName = objectName;
   }
   
   /** Getter for property metaData.
    * @return Value of property metaData.
    */
   public MBeanInfo getMetaData()
   {
      return metaData;
   }
   
   /** Setter for property metaData.
    * @param metaData New value of property metaData.
    */
   public void setMetaData(MBeanInfo metaData)
   {
      this.metaData = metaData;
   }

   public String getName()
   {
      return objectName.toString();
   }
   public String getClassName()
   {
      return metaData.getClassName();
   }
}
