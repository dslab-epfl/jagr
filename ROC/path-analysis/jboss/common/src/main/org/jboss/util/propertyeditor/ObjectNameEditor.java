/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.propertyeditor;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.jboss.util.NestedRuntimeException;

/**
 * A property editor for {@link javax.management.ObjectName}.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ObjectNameEditor
   extends TextPropertyEditorSupport
{
   /**
    * Returns a ObjectName for the input object converted to a string.
    *
    * @return a ObjectName object
    *
    * @throws NestedRuntimeException   An MalformedObjectNameException occured.
    */
   public Object getValue()
   {
      try {
         return new ObjectName(getAsText());
      }
      catch (MalformedObjectNameException e) {
         throw new NestedRuntimeException(e);
      }
   }
}
