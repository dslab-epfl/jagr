/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import javax.management.AttributeChangeNotification;
import javax.management.NotificationFilter;

/**
 * Notification filter support for attribute change notifications.
 *
 * @see javax.management.AttributeChangeNotification
 * @see javax.management.NotificationFilter
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class AttributeChangeNotificationFilter
   implements NotificationFilter, java.io.Serializable
{
   
   // Attributes ----------------------------------------------------
   private Map attributes = new HashMap();
   
   // Constructors --------------------------------------------------
   public AttributeChangeNotificationFilter()
   {
   }

   // Public --------------------------------------------------------
   public void enableAttribute(String name)
   {
      attributes.put(name, "true");
   }

   public void disableAttribute(String name)
   {
      attributes.remove(name);
   }

   public void disableAllAttributes()
   {
      attributes.clear();
   }

   public Vector getEnabledAttributes()
   {
      return new Vector(attributes.keySet());
   }

   // NotificationFilter implementation -----------------------------
   public boolean isNotificationEnabled(Notification notification)
   {
      AttributeChangeNotification notif = (AttributeChangeNotification)notification;
      if (attributes.containsKey(notif.getAttributeName()))
         return true;
         
      return false;
   }

}

