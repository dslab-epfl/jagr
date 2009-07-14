/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * A notification sent by the MBeanServer delegate when an MBean is
 * registered or unregisterd.<p>
 *
 * NOTE: The values from the spec are wrong, the real values are<b>
 * REGISTRATION_NOTIFICATION = "JMX.mbean.registered"<b>
 * UNREGISTRATION_NOTIFICATION = "JMX.mbean.registered"
 *
 * <p><b>Revisions:</b>
 * <p><b>20020315 Adrian Brock:</b>
 * <ul>
 * <li>Spec has wrong values for notification values
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanServerNotification
   extends Notification
{
   // Constants ---------------------------------------------------

   /**
    * Notification type sent at MBean registration
    */   
   public static final java.lang.String REGISTRATION_NOTIFICATION   = "JMX.mbean.registered";

   /**
    * Notification type sent at MBean registration
    */   
   public static final java.lang.String UNREGISTRATION_NOTIFICATION = "JMX.mbean.unregistered";

   // Attributes --------------------------------------------------

   /**
    * The object name of the mbean being (un)registered
    */
   private ObjectName mbeanName = null;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct a new MBeanServer notification
    *
    * @param type the type of notification to construct
    * @param source the source of the notification
    * @param sequence the sequence number of the notification
    * @param objectName the object name of the mbean being (un)registered
    */
   public MBeanServerNotification(String type, Object source, 
                                  long sequence, ObjectName objectName)
   {
      super(type, source, sequence);
      this.mbeanName = objectName;
   }

   // Public ------------------------------------------------------
   
   /**
    * Get the object name of the mbean being (un)registered
    *
    * @return the object name
    */
   public ObjectName getMBeanName()
   {
      return mbeanName;
   }

   // X Implementation --------------------------------------------

   // Y Overrides -------------------------------------------------

   // Protected ---------------------------------------------------

   // Private -----------------------------------------------------

   // Inner classes -----------------------------------------------
}
