/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.propertyeditor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.util.NestedRuntimeException;

/**
 * A property editor for {@link java.net.InetAddress}.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class InetAddressEditor
   extends TextPropertyEditorSupport
{
   /**
    * Returns a InetAddress for the input object converted to a string.
    *
    * @return a InetAddress object
    *
    * @throws NestedRuntimeException   An UnknownHostException; occured.
    */
   public Object getValue()
   {
      try {
         return InetAddress.getByName(getAsText());
      }
      catch (UnknownHostException e) {
         throw new NestedRuntimeException(e);
      }
   }
}
