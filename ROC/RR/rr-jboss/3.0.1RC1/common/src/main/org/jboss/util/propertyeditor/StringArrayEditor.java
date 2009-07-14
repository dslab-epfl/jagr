/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.propertyeditor;

import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * A property editor for String[].
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class StringArrayEditor
   extends TextPropertyEditorSupport
{
   /**
    * Returns a String[] by spliting up the input string where 
    * elements are seperated by commas.
    *
    * @return a URL object
    *
    * @throws NestedRuntimeException   An MalformedURLException occured.
    */
   public Object getValue()
   {
      StringTokenizer stok = new StringTokenizer(getAsText(), ",");
      List list = new LinkedList();
      
      while (stok.hasMoreTokens()) {
         list.add(stok.nextToken());
      }

      return (String[])list.toArray(new String[list.size()]);
   }
}
