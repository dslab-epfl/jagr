/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.relation;

import java.util.Vector;

import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;

/**
 * A helper class, used to filter notifications of registration,
 * unregistration of selected object names.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.1.1.1 $
 *
 */
public class MBeanServerNotificationFilter
  extends NotificationFilterSupport
{
  // Constants ---------------------------------------------------

  // Attributes --------------------------------------------------

  /**
   * Enabled Object Names.
   */
  private Vector enabled = new Vector();

  /**
   * Disable Object Names.
   */
  private Vector disabled = null;

  // Static ------------------------------------------------------

  // Constructors ------------------------------------------------

  /**
   * Create a filter selecting nothing by default<p>
   *
   * WARNING!! WARNING!! The spec says the MBeanServerNotificationFilter
   * accepts everything by default. The RI does exactly the opposite.
   * I follow the RI.
   */
  public MBeanServerNotificationFilter()
  {
  }

  // Public ------------------------------------------------------

  /**
   * Disable all object names. Rejects all notifications.
   */
  public synchronized void disableAllObjectNames()
  {
    enabled = new Vector();
    disabled = null;
  }

  /**
   * Disable an object name.
   *
   * @param objectName the object name to disable.
   * @exception IllegalArgumentException for a null object name
   */
  public synchronized void disableObjectName(ObjectName objectName)
  {
    if (objectName == null)
      throw new IllegalArgumentException("null object name");
    if (enabled != null)
      enabled.removeElement(objectName);
    if (disabled != null && disabled.contains(objectName) == false)
      disabled.addElement(objectName);
  }

  /**
   * Enable all object names. Accepts all notifications.
   */
  public synchronized void enableAllObjectNames()
  {
    enabled = null;
    disabled = new Vector();
  }

  /**
   * Enable an object name.
   *
   * @param objectName the object name to enable.
   * @exception IllegalArgumentException for a null object name
   */
  public synchronized void enableObjectName(ObjectName objectName)
  {
    if (objectName == null)
      throw new IllegalArgumentException("null object name");
    if (disabled != null)
      disabled.removeElement(objectName);
    if (enabled != null && enabled.contains(objectName) == false)
      enabled.addElement(objectName);
  }

  /**
   * Get all the disabled object names.<p>
   *
   * Returns a vector of disabled object names.<br>
   * Null for all object names disabled.
   * An empty vector means all object names enabled.
   *
   * @return the vector of disabled object names.
   */
  public synchronized Vector getDisabledObjectNames()
  {
    if (disabled == null)
      return null;
    return new Vector(disabled);
  }

  /**
   * Get all the enabled object names.<p>
   *
   * Returns a vector of enabled object names.<br>
   * Null for all object names enabled.
   * An empty vector means all object names disabled.
   *
   * @return the vector of enabled object names.
   */
  public synchronized Vector getEnabledObjectNames()
  {
    if (enabled == null)
      return null;
    return new Vector(enabled);
  }

  // NotificationFilterSupport overrides -------------------------

  /**
   * Test to see whether this notification is enabled
   *
   * @param notification the notification to filter
   * @return true when the notification should be sent, false otherwise
   * @exception IllegalArgumentException for null notification.
   */
  public synchronized boolean isNotificationEnabled(Notification notification)
    throws IllegalArgumentException
  {
    if (notification == null)
      throw new IllegalArgumentException("null notification");

    // Check the notification type
    if (super.isNotificationEnabled(notification) == false)
      return false;

    // Get the object name
    MBeanServerNotification mbsNotification = (MBeanServerNotification) notification;
    ObjectName objectName = mbsNotification.getMBeanName();

    // Is it enabled?
    if (enabled != null)
      return enabled.contains(objectName);

    // Is it not disabled?
    if (disabled.contains(objectName) == false)
      return true;

    // Disabled
    return false;
  }

  // Private -----------------------------------------------------
}
