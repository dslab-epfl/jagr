/***************************************
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 ***************************************/

package org.jboss.jmx.adaptor.control;

import java.beans.PropertyEditor;

/** A simple tuple of an mbean operation name, sigature and result.

@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
 */
public class AttrResultInfo
{
   public String name;
   public PropertyEditor editor;
   public Object result;

   public AttrResultInfo(String name, PropertyEditor editor, Object result)
   {
      this.name = name;
      this.editor = editor;
      this.result = result;
   }

   public String getAsText()
   {
      String text = null;
      if( result != null )
      {
         if( editor != null )
         {
            editor.setValue(result);
            text = editor.getAsText();
         }
         else
         {
            text = result.toString();
         }
      }
      return text;
   }
}
