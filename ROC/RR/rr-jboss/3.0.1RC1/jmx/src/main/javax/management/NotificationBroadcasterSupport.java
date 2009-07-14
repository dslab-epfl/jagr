/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *
 */
public class NotificationBroadcasterSupport implements NotificationBroadcaster
{

   private HashMap listenerMap = new HashMap();

   public NotificationBroadcasterSupport()
   {}

   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      synchronized (listenerMap)
      {
         Map hbMap = (Map)listenerMap.get(listener);

         if (hbMap != null)
         {
            hbMap.put(handback, filter);
         }
         else
         {
            hbMap = new HashMap();
            hbMap.put(handback, filter);
            listenerMap.put(listener, hbMap);
         }
      }
   }

   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      synchronized (listenerMap)
      {
         listenerMap.remove(listener);
      }
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return null;
   }

   public void sendNotification(Notification notification)
   {
      Collection copy = null;
      synchronized (listenerMap)
      {
         copy = new ArrayList(listenerMap.keySet());
      }
      Iterator listenerIterator = copy.iterator();

      while(listenerIterator.hasNext())
      {
         NotificationListener listener = (NotificationListener)listenerIterator.next();
         Map hbMap = (Map)listenerMap.get(listener);
         Iterator it = hbMap.keySet().iterator();

         while(it.hasNext())
         {
            Object hb = it.next();
            NotificationFilter filter = (NotificationFilter)hbMap.get(hb);

            if (filter == null)
               listener.handleNotification(notification, hb);
            else if (filter.isNotificationEnabled(notification))
               listener.handleNotification(notification, hb);
         }
      }
   }

}

