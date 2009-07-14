/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * This is the mandated object for sending notifications of attribute
 * changes. The MBean may also send other types of notifications.
 * The MBean must implement the
 * {@link javax.management.NotificationBroadcaster} interface to send
 * this notification.<p>
 *
 * The following information is provided in addition to the that provided by
 * {@link javax.management.Notification Notification}.
 * The attribute's {@link #getAttributeName() name},
 * {@link #getAttributeType() type},
 * {@link #getOldValue() old value} and
 * {@link #getNewValue() new value}. <p>
 *
 * The notification type is "jmx.attribute.change", defined in the
 * static string {@link #ATTRIBUTE_CHANGE}.
 *
 * @see javax.management.AttributeChangeNotificationFilter
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.1.1.1 $
 *
 */
public class AttributeChangeNotification
   extends Notification
{
   // Constants -----------------------------------------------------


   /**
    * The AttributeChangeNotification notification type. It is
    * "jmx.attribute.change".
    */
   public static final String ATTRIBUTE_CHANGE = "jmx.attribute.change";

   // Attributes --------------------------------------------------------

   /**
    * The name of the attribute.
    */
   private String attributeName = null;

   /**
    * The type of the attribute.
    */
   private String attributeType = null;

   /**
    * The old value of the attribute.
    */
   private Object oldValue = null;

   /**
    * The new value of the attribute.
    */
   private Object newValue = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Contruct a new attribute change notification.
    *
    * @param source the source of the notification.
    * @param sequenceNumber the instance of this notification.
    * @param timeStamp the time the notification was generated.
    * @param msg a human readable form of the change.
    * @param attributeName the name of the attribute.
    * @param attributeType the type of the attribute.
    * @param oldValue the old value of the attribute.
    * @param newValue the new value of the attribute.
    */
   public AttributeChangeNotification(Object source,
                                      long sequenceNumber,
                                      long timeStamp,
                                      String msg,
                                      String attributeName,
                                      String attributeType,
                                      Object oldValue,
                                      Object newValue)
   {
      super(ATTRIBUTE_CHANGE, source, sequenceNumber, timeStamp, msg);

      this.attributeName = attributeName;
      this.attributeType = attributeType;
      this.oldValue = oldValue;
      this.newValue = newValue;
   }


   // Public --------------------------------------------------------

   /**
    * Retrieves the name of the attribute.
    *
    * @return the name of the attribute.
    */
   public String getAttributeName()
   {
      return attributeName;
   }

   /**
    * Retrieves the type of the attribute.
    *
    * @return the type of the attribute.
    */
   public String getAttributeType()
   {
      return attributeType;
   }

   /**
    * Retrieves the old value of the attribute.
    *
    * @return the old value of the attribute.
    */
   public Object getOldValue()
   {
      return oldValue;
   }

   /**
    * Retrieves the new value of the attribute.
    *
    * @return the new value of the attribute.
    */
   public Object getNewValue()
   {
      return newValue;
   }

   // Overrides -----------------------------------------------------

   // Implementation ------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}


