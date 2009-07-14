/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * Describes a notification emitted by an MBean
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 *
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanNotificationInfo extends MBeanFeatureInfo
   implements Cloneable, java.io.Serializable
{

   // Attributes ----------------------------------------------------
   protected String[] notifsType = null;

   // Constructors --------------------------------------------------
   public MBeanNotificationInfo(String[] notifsType,
                                String name,
                                String description)
   {
      super(name, description);
      this.notifsType = (null == notifsType) ? new String[0] : (String[]) notifsType.clone();
   }

   // Public -------------------------------------------------------
   public String[] getNotifTypes()
   {
      return (String[]) notifsType.clone();
   }

   // CLoneable implementation -------------------------------------
   public Object clone() throws CloneNotSupportedException
   {
      MBeanNotificationInfo clone = (MBeanNotificationInfo) super.clone();
      clone.notifsType = getNotifTypes();

      return clone;
   }
}
