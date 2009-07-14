/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.relation;

import java.io.Serializable;

/**
 * Represents the result of multiple access to roles.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class RoleResult
  implements Serializable
{
   // Attributes ----------------------------------------------------

   /**
    * The successful roles
    */
   private RoleList roleList;

   /**
    * The unresolved roles.
    */
   private RoleUnresolvedList roleUnresolvedList;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new role result.
    * 
    * @param roleList the successful roles
    * @param roleUnresolvedList the roles not accessed
    */
   public RoleResult(RoleList roleList, RoleUnresolvedList roleUnresolvedList)
   {
     this.roleList = roleList;
     this.roleUnresolvedList = roleUnresolvedList;
   }

   // Public ---------------------------------------------------------

   /**
    * Retrieve the successful roles.
    * 
    * @return the successful roles.
    */
   public RoleList getRoles()
   {
     return roleList;
   }

   /**
    * Retrieve the unsuccessful roles.
    * 
    * @return the unsuccessful roles.
    */
   public RoleUnresolvedList getRolesUnresolved()
   {
     return roleUnresolvedList;
   }

   /**
    * Set the successful roles.
    * 
    * @param roleList the successful roles.
    */
   public void setRoles(RoleList roleList)
   {
     this.roleList = roleList;
   }

   /**
    * Set the unsuccessful roles.
    * 
    * @param roleUnresolvedList the unsuccessful roles.
    */
   public void setRolesUnresolved(RoleUnresolvedList roleUnresolvedList)
   {
     this.roleUnresolvedList = roleUnresolvedList;
   }
}

