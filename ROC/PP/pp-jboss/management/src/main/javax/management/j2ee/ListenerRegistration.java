package javax.management.j2ee;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
* Interface how a client can add its local listener on the
* remote Management EJB.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public interface ListenerRegistration
   extends Serializable
{
   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------
   
   /**
    * Adds a new local (client-side) listener on the Management EJB (server-side)
    * to listen for Notifications. If the call is local (in the same JVM) then
    * it can optimize the call to local.
    *
    * @param pName Object Name of the Managed Object we want to listen for notifications
    * @param pListener Local (client-side) Notification Listener to finally receive the
    *                  notifications
    * @param pFilter Notification Filter to reduce the notifications to what the client
    *                expects
    * @param pHandback Handback object sent back to the client on every Notifications
    *                  delivered based on this registration
    **/
   public void addNotificationListener(
      ObjectName pName,
      NotificationListener pListener,
      NotificationFilter pFilter,
      Object pHandback
   )
      throws
         InstanceNotFoundException,
         RemoteException;
   
   /**
    * Removes the notification listener from the Management EJB (server-side)
    * based on the given local (client-side) listener.
    *
    * @param pName Object Name of the Managed Object the Listener was added to listen for
    * @param pListener Local (client-side) Notification Listener used to add the
    *                  notification listener
    **/
   public void removeNotificationListener(
      ObjectName pName,
      NotificationListener pListener
   )
      throws
         InstanceNotFoundException,
         ListenerNotFoundException,
         RemoteException;
   
}
