/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * Describes an MBeans' management interface.
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
 *
 * @see javax.management.MBeanServer
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 *
 * @version $Revision: 1.1.1.1 $
 *
 */
public class MBeanInfo
   implements Cloneable, java.io.Serializable
{

   // Attributes ----------------------------------------------------
   protected String className = null;
   protected String description = null;
   protected MBeanAttributeInfo[] attributes = null;
   protected MBeanConstructorInfo[] constructors = null;
   protected MBeanOperationInfo[] operations = null;
   protected MBeanNotificationInfo[] notifications = null;

   // Constructors --------------------------------------------------
   public MBeanInfo(String className, String description,
                    MBeanAttributeInfo[] attributes,
                    MBeanConstructorInfo[] constructors,
                    MBeanOperationInfo[] operations,
                    MBeanNotificationInfo[] notifications)
   {
      this.className = className;
      this.description = description;
      this.attributes = (null == attributes) ? new MBeanAttributeInfo[0] : (MBeanAttributeInfo[]) attributes.clone();
      this.constructors = (null == constructors) ? new MBeanConstructorInfo[0] : (MBeanConstructorInfo[]) constructors.clone();
      this.operations = (null == operations) ? new MBeanOperationInfo[0] : (MBeanOperationInfo[]) operations.clone();
      this.notifications = (null == notifications) ? new MBeanNotificationInfo[0] : (MBeanNotificationInfo[]) notifications.clone();
   }

   // Public --------------------------------------------------------
   public String getClassName()
   {
      return className;
   }

   public String getDescription()
   {
      return description;
   }

   public MBeanAttributeInfo[] getAttributes()
   {
      return (MBeanAttributeInfo[]) attributes.clone();
   }

   public MBeanOperationInfo[] getOperations()
   {
      return (MBeanOperationInfo[]) operations.clone();
   }

   public MBeanConstructorInfo[] getConstructors()
   {
      return (MBeanConstructorInfo[]) constructors.clone();
   }

   public MBeanNotificationInfo[] getNotifications()
   {
      return (MBeanNotificationInfo[]) notifications.clone();
   }

   // Cloneable implementation --------------------------------------
   public Object clone() throws CloneNotSupportedException
   {
      MBeanInfo clone = (MBeanInfo) super.clone();

      clone.className = getClassName();
      clone.description = getDescription();

      clone.attributes = getAttributes();
      clone.constructors = getConstructors();
      clone.operations = getOperations();
      clone.notifications = getNotifications();

      return clone;
   }
}
