/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package javax.management.relation;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * A notification from the relation service.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class RelationNotification
  extends Notification
  implements Serializable
{
  // Constants -----------------------------------------------------

  /**
   * Creation of an internal relation.
   */
  public static String RELATION_BASIC_CREATION = "jmx.relation.creation.basic";

  /**
   * Removal of an internal relation.
   */
  public static String RELATION_BASIC_REMOVAL = "jmx.relation.removal.basic";

  /**
   * Update of an internal relation.
   */
  public static String RELATION_BASIC_UPDATE = "jmx.relation.update.basic";

  /**
   * Creation of MBean relation added to the relation service.
   */
  public static String RELATION_MBEAN_CREATION = "jmx.relation.creation.mbean";

  /**
   * Removal of MBean relation added to the relation service.
   */
  public static String RELATION_MBEAN_REMOVAL = "jmx.relation.removal.mbean";

  /**
   * Update of MBean relation added to the relation service.
   */
  public static String RELATION_MBEAN_UPDATE = "jmx.relation.update.mbean";

  /**
   * Tag used to identify creation/removal constructor used.
   */
  private static int CREATION_REMOVAL = 0;

  /**
   * Tag used to identify update constructor used.
   */
  private static int UPDATE = 1;
  
  // Attributes ----------------------------------------------------

  /**
   * The MBeans removed when a relation type is removed.
   */
  private ArrayList unregMBeans;
  
  /**
   * The new list of object names in the role.
   */
  private ArrayList newRoleValue;

  /**
   * The relation's object name.
   */
  private ObjectName relObjName;
  
  /**
   * The old list of object names in the role.
   */
  private ArrayList oldRoleValue;

  /**
   * The relation id of this notification.
   */
  private String relTypeId;

  /**
   * The relation type name of this notification.
   */
  private String relTypeName;

  /**
   * The role name of an updated role, only for role updates.
   */
  private String roleName;

  // Static --------------------------------------------------------
  
  // Constructors --------------------------------------------------

  /**
   * Construct a new relation notification for a creation or removal.<p>
   *
   * The notification type should be one {@link #RELATION_BASIC_CREATION},
   * {@link #RELATION_BASIC_REMOVAL}, {@link #RELATION_MBEAN_CREATION} or
   * {@link #RELATION_MBEAN_REMOVAL}.<p>
   *
   * The relation type cannot be null, the source cannot be null and it
   * must be a relation service, the relation id cannot be null, the
   * relation type name cannot null.
   *
   * @param type the notification type.
   * @param source the object sending the notification (always the
   *        the relation service).
   * @param sequenceNumber the number identifying the notification
   * @param timeStamp the time of the notification
   * @param message human readable string
   * @param relTypeId the relation type id
   * @param relTypeName the relation type name
   * @param relObjName the relation MBean object name (null
   *        for internal relations)
   * @param unregMBeans the list of object names of mbeans to be
   *        unregistered from the relation service because of a relation
   *        removal. Only relevant for removals, can be null.
   * @exception IllegalArgumentException for null or invalid parameters.
   */
  public RelationNotification(String type, Object source, long sequenceNumber, 
               long timeStamp, String message, String relTypeId, 
               String relTypeName, ObjectName relObjName, List unregMBeans)
    throws IllegalArgumentException
  {
    super(type, source, sequenceNumber, timeStamp, message);
    init(CREATION_REMOVAL, type, source, sequenceNumber, timeStamp, message, 
         relTypeId, relTypeName, relObjName, unregMBeans, null, null, null);
  }

  /**
   * Construct a new relation notification for an update.<p>
   *
   * The notification type should be one {@link #RELATION_BASIC_UPDATE},
   * {@link #RELATION_MBEAN_UPDATE}
   *
   * The relation type cannot be null, the source cannot be null and it
   * must be a relation service, the relation id cannot be null, the
   * relation type name cannot null.
   *
   * @param type the notification type.
   * @param source the object sending the notification (always the
   *        the relation service).
   * @param sequenceNumber the number identifying the notification
   * @param timeStamp the time of the notification
   * @param message human readable string
   * @param relTypeId the relation type id
   * @param relTypeName the relation type name
   * @param relObjName the relation MBean object name (null
   *        for internal relations)
   * @param roleName the role name
   * @param newRoleValue the new value of the role
   * @param newRoleValue the old value of the role
   * @exception IllegalArgumentException for null or invalid parameters.
   */
  public RelationNotification(String type, Object source, long sequenceNumber, 
               long timeStamp, String message, String relTypeId, 
               String relTypeName, ObjectName relObjName, String roleName, 
               List newRoleValue, List oldRoleValue)
    throws IllegalArgumentException
  {
    super(type, source, sequenceNumber, timeStamp, message);
    init(UPDATE, type, source, sequenceNumber, timeStamp, message, relTypeId,
         relTypeName, relObjName, null, roleName, newRoleValue, oldRoleValue);
  }

  // Public --------------------------------------------------------

  /**
   * Retrieves a list of Object names of the mbeans that will be removed
   * from the relation service because of a relation's removal. This
   * is only relevant for relation removal events.
   *
   * @return the list of removed mbeans.
   */
  public List getMBeansToUnregister()
  {
    if (unregMBeans == null)
      return new ArrayList();
    else
      return new ArrayList(unregMBeans); 
  }
  
  /**
   * Retrieves the new list of object names in the role.
   *
   * @return the new list.
   */
  public List getNewRoleValue()
  {
    if (newRoleValue == null)
      return new ArrayList();
    else
      return new ArrayList(newRoleValue); 
  }

  /**
   * Retrieves the object name of the mbean (null for an internal relation).
   *
   * @return the relation's object name.
   */
  public ObjectName getObjectName()
  {
    return relObjName;
  }
  
  /**
   * Retrieves the old list of object names in the role.
   *
   * @return the old list.
   */
  public List getOldRoleValue()
  {
    if (oldRoleValue == null)
      return new ArrayList();
    else
      return new ArrayList(oldRoleValue); 
  }

  /**
   * Retrieves the relation id of this notification.
   *
   * @return the relation id.
   */
  public String getRelationId()
  {
    return relTypeId;
  }

  /**
   * Retrieves the relation type name of this notification.
   *
   * @return the relation type name.
   */
  public String getRelationTypeName()
  {
    return relTypeName;
  }

  /**
   * Retrieves the role name of an updated role, only for role updates.
   *
   * @return the name of the updated role.
   */
  public String getRoleName()
  {
    return roleName;
  }

  // Notification overrides ----------------------------------------

  // Package protected ---------------------------------------------

  // Protected -----------------------------------------------------

  // Private -------------------------------------------------------

  /**
   * Does most the work for the constructors, see the contructors
   * for details.<p>
   *
   * @param which the constructor called.
   * @param type the notification type.
   * @param source the object sending the notification (always the
   *        the relation service).
   * @param sequenceNumber the number identifying the notification
   * @param timeStamp the time of the notification
   * @param message human readable string
   * @param relTypeId the relation type id
   * @param relTypeName the relation type name
   * @param relObjName the relation MBean object name (null
   *        for internal relations)
   * @param unregMBeans the mbeans unregistered when a relation is removed.
   * @param roleName the role name
   * @param newRoleValue the new value of the role
   * @param newRoleValue the old value of the role
   * @exception IllegalArgumentException for null or invalid parameters.
   */
  private void init(int which, String type, Object source, 
               long sequenceNumber, long timeStamp, String message, 
               String relTypeId, String relTypeName, ObjectName relObjName,
               List unregMBeans, String roleName, List newRoleValue,  
               List oldRoleValue)
    throws IllegalArgumentException
  {
    // Invalid notification type
    if (type == null)
      throw new IllegalArgumentException("null notification type");
    if (which == CREATION_REMOVAL && type != RELATION_BASIC_CREATION &&
        type != RELATION_BASIC_REMOVAL && type != RELATION_MBEAN_CREATION &&
        type != RELATION_MBEAN_REMOVAL)
      throw new IllegalArgumentException("Invalid creation/removal notifcation");
    if (which == UPDATE && type != RELATION_BASIC_UPDATE && 
        type != RELATION_MBEAN_UPDATE)
      throw new IllegalArgumentException("Invalid update notifcation");

    // Source must be a Relation Service
    if (type == null)
      throw new IllegalArgumentException("null source");

    // REVIEW: According to the spec, this should be a RelationService
    // that doesn't make any sense, it's not serializable
    // I use the object name
    //if ((source instanceof RelationService) == false)
    //  throw new IllegalArgumentException("Source not a relation service");

    // Relation id
    if (relTypeId == null)
      throw new IllegalArgumentException("null relation id");

    // Relation type name
    if (relTypeName == null)
      throw new IllegalArgumentException("null relation type name");

    // Role Info
    if (which == UPDATE && roleName == null)
      throw new IllegalArgumentException("null role name");

    // New role value
    if (which == UPDATE && newRoleValue == null)
      throw new IllegalArgumentException("null new role value");

    // Old role value
    if (which == UPDATE && oldRoleValue == null)
      throw new IllegalArgumentException("null old role value");

    this.relTypeId = relTypeId;
    this.relTypeName = relTypeName;
    this.relObjName = relObjName;
    if (unregMBeans != null)
      this.unregMBeans = new ArrayList(unregMBeans);
    if (roleName != null)
      this.roleName = roleName;
    if (newRoleValue != null)
      this.newRoleValue = new ArrayList(newRoleValue);
    if (oldRoleValue != null)
      this.oldRoleValue = new ArrayList(oldRoleValue);
  }

  // Inner classes -------------------------------------------------
}
