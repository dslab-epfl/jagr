/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.iiop.server;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;

/**
 * Implements <code>org.omg.CORBA.Policy</code> objects containing codebase
 * strings. 
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 1.1.1.1 $
 */
class CodebasePolicy 
      extends LocalObject
      implements Policy
{
   // Private -----------------------------------------------------------------

   private final String codebase;

   // Static  -----------------------------------------------------------------

   public static final int TYPE = 0x12345678;

   // Constructor -------------------------------------------------------------

   public CodebasePolicy(String codebase)
   {
      this.codebase = codebase;
   }

   /**
    * Returns the codebase string contained in this Policy.
    */
   public String getCodebase()
   {
      return codebase;
   }

   // org.omg.CORBA.Policy operations -----------------------------------------

   /**
    * Returns a copy of the Policy object.
    */
   public Policy copy() 
   {
      return new CodebasePolicy(codebase);
   }
   
   /**
    * Destroys the Policy object.
    */
   public void destroy() 
   {
   }

   /**
    * Returns the constant value that corresponds to the type of the policy 
    * object.
    */
   public int policy_type() 
   {
      return TYPE;
   }

    public String toString()
    {
        return "CodebasePolicy[" + codebase + "]";
    }

}
