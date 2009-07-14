/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * This class can be used to implement relation types.<p>
 *
 * It holds RoleInfo objects for all roles in the relation.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020312 Adrian Brock:</b>
 * <ul>
 * <li>Fixed error handling for getRoleInfo
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class RelationTypeSupport
  implements RelationType
{
  // Constants ---------------------------------------------------

  // Attributes --------------------------------------------------

  /**
   * The name of the relation type.
   */
  private String name;

  /**
   * The list of role infos in the relation.
   */
  private ArrayList roleInfos;

  // Static ------------------------------------------------------

  // Constructors ------------------------------------------------

  /**
   * Create a relation type with a name but no role infos.<p>
   *
   * WARNING: No check is made on the arguments.
   *
   * @param name the relation type name.
   */
  protected RelationTypeSupport(String name)
  {
    this.name = name;
    roleInfos = new ArrayList();
  }

  /**
   * Create a relation type with a name and the passed role infos.<p>
   *
   * A relation type is invalid if the same name is used in two
   * different role infos, no role information is provided or a null
   * role is passed.
   *
   * @param name the relation type name.
   * @param roleInfos an array of role info objects.
   * @exception IllegalArgumentException for null parameters
   * @exception InvalidRelationTypeException for an invalid relation
   */
  public RelationTypeSupport(String name, RoleInfo[] infos)
    throws IllegalArgumentException, InvalidRelationTypeException
  {
    if (name == null)
      throw new IllegalArgumentException("Null name");
    if (infos == null)
      throw new IllegalArgumentException("No role information");
    if (infos.length == 0)
      throw new InvalidRelationTypeException("No role information");
    this.name = name;
    // Check and store the role information
    HashSet roleNames = new HashSet();
    roleInfos = new ArrayList(infos.length);
    for (int i = 0; i < infos.length; i++)
    {
      if (infos[i] == null)
        throw new InvalidRelationTypeException("Null role");
      if (roleNames.contains(infos[i].getName()))
        throw new InvalidRelationTypeException(
                  "Duplicate role name" + infos[i].getName());
      roleNames.add(infos[i].getName());
      roleInfos.add(infos[i]);
    }
  }

  // Public ------------------------------------------------------

  // Relation Type Implementation --------------------------------

  public String getRelationTypeName()
  {
    return name;
  }

  public List getRoleInfos()
  {
    return roleInfos;
  }

  public RoleInfo getRoleInfo(String roleInfoName)
    throws RoleInfoNotFoundException
  {
    if (roleInfoName == null)
       throw new IllegalArgumentException("Null role info name");
    RoleInfo result = null;
    ArrayList temp = new ArrayList(roleInfos);
    Iterator iterator = temp.iterator();
    while (iterator.hasNext())
    {
      RoleInfo info = (RoleInfo) iterator.next();
      if (info.getName().equals(roleInfoName))
      {
        result = info;
        break;
      }
    }
    // REVIEW: The spec is contradictory here it says throw an
    // exception and return null????
    if (result == null)
      throw new RoleInfoNotFoundException(roleInfoName);
    return result;
  }

  // Protected ---------------------------------------------------

  /**
   * Add a role information object to the relation type.
   *
   * @param roleInfos an array of role info objects.
   * @exception IllegalArgumentException for null parameters
   * @exception InvalidRelationTypeException for a duplicate role name.
   */
  protected void addRoleInfo(RoleInfo roleInfo)
    throws IllegalArgumentException, InvalidRelationTypeException
  {
    if (roleInfo == null)
      throw new IllegalArgumentException("No role information");

    // Check for a duplciate role name.
    String newName = roleInfo.getName();
    synchronized (roleInfos)
    {
      Iterator iterator = roleInfos.iterator();
      while (iterator.hasNext())
      {
        RoleInfo info = (RoleInfo) iterator.next();
        if (info.getName().equals(newName))
          throw new InvalidRelationTypeException("Duplicate role name");
      }
      // Not a duplicate, add it
      roleInfos.add(roleInfo);
    }
  }

  // Private -----------------------------------------------------
}
