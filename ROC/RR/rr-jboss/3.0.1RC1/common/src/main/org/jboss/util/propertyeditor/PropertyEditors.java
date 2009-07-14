/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.propertyeditor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.jboss.util.Classes;

/**
 * A collection of PropertyEditor utilities.  Provides the same interface
 * as PropertyManagerEditor plus more...
 *
 * <p>Installs the default PropertyEditors.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PropertyEditors
{
   /** Augment the PropertyEditorManager search path to incorporate the JBoss
    specific editors by appending the org.jboss.util.propertyeditor package
    to the PropertyEditorManager editor search path.
    */
   static
   {
      String[] currentPath = PropertyEditorManager.getEditorSearchPath();
      int length = currentPath != null ? currentPath.length : 0;
      String[] newPath = new String[length+1];
      System.arraycopy(currentPath, 0, newPath, 0, length);
      // May want to put the JBoss editor path first, for now append it
      newPath[length] = "org.jboss.util.propertyeditor";
      PropertyEditorManager.setEditorSearchPath(newPath);
   }

   /**
    * Locate a value editor for a given target type.
    *
    * @param type   The class of the object to be edited.
    * @return       An editor for the given type or null if none was found.
    */
   public static PropertyEditor findEditor(final Class type)
   {
      return PropertyEditorManager.findEditor(type);
   }

   /**
    * Locate a value editor for a given target type.
    *
    * @param typeName    The class name of the object to be edited.
    * @return            An editor for the given type or null if none was found.
    */
   public static PropertyEditor findEditor(final String typeName)
      throws ClassNotFoundException
   {
      // see if it is a primitive type first
      Class type = Classes.getPrimitiveTypeForName(typeName);
      if (type == null)
      {
         // nope try look up
         type = Class.forName(typeName);
      }
      
      return PropertyEditorManager.findEditor(type);
   }

   /**
    * Get a value editor for a given target type.
    *
    * @param type    The class of the object to be edited.
    * @return        An editor for the given type.
    *
    * @throws RuntimeException   No editor was found.
    */
   public static PropertyEditor getEditor(final Class type)
   {
      PropertyEditor editor = findEditor(type);
      if (editor == null)
      {
         throw new RuntimeException("No property editor for type: " + type);
      }

      return editor;
   }

   /**
    * Get a value editor for a given target type.
    *
    * @param typeName    The class name of the object to be edited.
    * @return            An editor for the given type.
    *
    * @throws RuntimeException   No editor was found.
    */
   public static PropertyEditor getEditor(final String typeName)
      throws ClassNotFoundException
   {
      PropertyEditor editor = findEditor(typeName);
      if (editor == null)
      {
         throw new RuntimeException("No property editor for type: " + typeName);
      }

      return editor;
   }
   
   /**
    * Register an editor class to be used to editor values of a given target class.
    *
    * @param type         The class of the objetcs to be edited.
    * @param editorType   The class of the editor.
    */
   public static void registerEditor(final Class type, final Class editorType)
   {
      PropertyEditorManager.registerEditor(type, editorType);
   }

   /**
    * Register an editor class to be used to editor values of a given target class.
    *
    * @param typeName         The classname of the objetcs to be edited.
    * @param editorTypeName   The class of the editor.
    */
   public static void registerEditor(final String typeName,
                                     final String editorTypeName)
      throws ClassNotFoundException
   {
      Class type = Class.forName(typeName);
      Class editorType = Class.forName(editorTypeName);

      PropertyEditorManager.registerEditor(type, editorType);
   }

   /**
    * Gets the package names that will be searched for property editors.
    *
    * @return   The package names that will be searched for property editors.
    */
   public String[] getEditorSearchPath()
   {
      return PropertyEditorManager.getEditorSearchPath();
   }

   /**
    * Sets the package names that will be searched for property editors.
    *
    * @param path   The serach path.
    */
   public void setEditorSearchPath(final String[] path)
   {
      PropertyEditorManager.setEditorSearchPath(path);
   }
}
