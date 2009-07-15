/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.relation;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An unresolved role. Used when a role could not be retrieved from a
 * relation due to a problem. It has the role name, the value if that
 * was passed and the problem type.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class RoleUnresolved
  implements Serializable
{
   // Attributes ----------------------------------------------------

   /**
    * The role name
    */
   private String name;

   /**
    * An ordered list of MBean object names.
    */
   private ArrayList value;

   /**
    * The problem type.
    */
   private int problem;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new unresolved role.<p>
    *
    * See {@link RoleStatus} for the problem types.<p>
    * 
    * The passed list must be an ArrayList.
    *
    * @param roleName the role name
    * @param roleValue the MBean object names in the role can be null
    * @param problemType the problem type. 
    * @exception IllegalArgumentException for null values or
    *            incorrect problem type.
    */
   public RoleUnresolved(String roleName, List roleValue, int problemType)
     throws IllegalArgumentException
   {
     if (roleName == null)
       throw new IllegalArgumentException("Null roleName");
     if (roleValue == null)
       throw new IllegalArgumentException("Null roleValue");
     if (RoleStatus.isRoleStatus(problemType) == false)
       throw new IllegalArgumentException("Invalid problem type.");
     name = roleName;
     value = (ArrayList) roleValue; 
     problem = problemType; 
   }

   // Public ---------------------------------------------------------

   /**
    * Retrieve the problem type.
    * 
    * @return the problem type.
    */
   public int getProblemType()
   {
     return problem;
   }

   /**
    * Retrieve the role name.
    * 
    * @return the role name.
    */
   public String getRoleName()
   {
     return name;
   }

   /**
    * Retrieve the role value.
    * 
    * @return a list of MBean object names.
    */
   public List getRoleValue()
   {
     return value;
   }

   /**
    * Set the problem type.
    * 
    * @param problemType the problem type.
    * @exception IllegalArgumentException for an invalid problem type
    */
   public void setProblemType(int problemType)
     throws IllegalArgumentException
   {
     if (RoleStatus.isRoleStatus(problemType) == false)
       throw new IllegalArgumentException("Invalid problem type.");
     problem = problemType;
   }
   /**
    * Set the role name.
    * 
    * @param roleName the role name.
    * @exception IllegalArgumentException for a null name
    */
   public void setRoleName(String roleName)
     throws IllegalArgumentException
   {
     if (roleName == null)
       throw new IllegalArgumentException("Null roleName");
     name = roleName;
   }

   /**
    * Set the role value it must be an ArrayList.
    * A list of mbean object names.
    * 
    * @param roleValue the role value.
    */
   public void setRoleValue(List roleValue)
   {
     value = (ArrayList) roleValue;
   }

   // Object Overrides -------------------------------------------------

   /**
    * Clones the object.
    *
    * @info.todo shouldn't use the copy constructor?
    */
   public Object clone()
   {
     return new RoleUnresolved(name, new ArrayList(value), problem);
   }

   /**
    * Formats the unresolved role for output.
    */
   public String toString()
   {
     StringBuffer buffer = new StringBuffer("Problem (");
     buffer.append(problem); // REVIEW?????
     buffer.append(") Role Name (");
     buffer.append(name);
     buffer.append(") ObjectNames (");
     Iterator iterator = new ArrayList(value).iterator(); 
     while (iterator.hasNext())
     {
       buffer.append(iterator.next());
       if (iterator.hasNext())
         buffer.append(" ");
     }
     buffer.append(")");
     return buffer.toString();
   }
}

