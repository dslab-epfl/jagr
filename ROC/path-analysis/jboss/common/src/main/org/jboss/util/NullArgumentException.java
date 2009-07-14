/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util;

/**
 * Thrown to indicate that a method argument was <tt>null</tt> and 
 * should <b>not</b> have been.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class NullArgumentException 
   extends IllegalArgumentException
{
   /** The name of the argument that was <tt>null</tt>. */
   protected final String name;

   /**
    * Construct a <tt>NullArgumentException</tt>.
    *
    * @param name    Argument name.
    */
   public NullArgumentException(final String name) {
      super(makeMessage(name));

      this.name = name;
   }

   /**
    * Construct a <tt>NullArgumentException</tt>.
    */
   public NullArgumentException() {
      this.name = null;
   }

   /**
    * Get the argument name that was <tt>null</tt>.
    *
    * @return  The argument name that was <tt>null</tt>.
    */
   public final String getArgumentName() {
      return name;
   }

   /**
    * Make a execption message for the argument name.
    */
   private static String makeMessage(final String name) {
      return "'" + name + "' is null";
   }
}
