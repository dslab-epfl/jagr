/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package javax.management.timer;

import java.io.Serializable;

import javax.management.Notification;

/**
 * A notification from the timer service.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class TimerNotification
  extends Notification
  implements Serializable
{
  // Constants -----------------------------------------------------
  
  // Attributes ----------------------------------------------------
  
  /**
   * The notification id of this timer notification.
   */
  private Integer id;

  // Static --------------------------------------------------------
  
  // Constructors --------------------------------------------------

  /**
   * Construct a new timer notification.
   *
   * @param type the notification type.
   * @param source the notification source.
   * @param sequenceNumber the notification sequence within the source object.
   * @param timeStamp the time the notification was sent.
   * @param message the detailed message.
   * @param id the timer notification id.
   * @param userData additional notification user data
   */
  TimerNotification(String type, Object source, long sequenceNumber, 
               long timeStamp, String message, Integer id, Object userData)
  {
    super(type, source, sequenceNumber, timeStamp, message);
    this.id = id;
    this.setUserData(userData);
  }

  // Public --------------------------------------------------------

  /**
   * Retrieves the notification id of this timer notification.
   *
   * @return the notification id.
   */
  public Integer getNotificationID()
  {
    return id;
  }

  // X implementation ----------------------------------------------

  // Notification overrides ----------------------------------------

  // Package protected ---------------------------------------------

  // Protected -----------------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
