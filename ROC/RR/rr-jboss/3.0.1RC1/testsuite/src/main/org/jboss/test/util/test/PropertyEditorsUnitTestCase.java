/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.util.test;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/** Unit tests for the custom JBoss property editors

@see org.jboss.util.propertyeditor.PropertyEditors
@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
**/
public class PropertyEditorsUnitTestCase extends JBossTestCase
{
   /** Augment the PropertyEditorManager search path to incorporate the JBoss
    specific editors. This simply references the PropertyEditors.class to
    invoke its static initialization block.
    */
   static
   {
      Class c = org.jboss.util.propertyeditor.PropertyEditors.class;
   }

   public PropertyEditorsUnitTestCase(String name)
   {
      super(name);
   }

   public void testEditorSearchPath()
      throws Exception
   {
      getLog().debug("+++ testEditorSearchPath");
      String[] searchPath = PropertyEditorManager.getEditorSearchPath();
      boolean foundJBossPath = false;
      for(int p = 0; p < searchPath.length; p ++)
      {
         String path = searchPath[p];
         getLog().debug("path["+p+"]="+path);
         foundJBossPath |= path.equals("org.jboss.util.propertyeditor");
      }
      assertTrue("Found org.jboss.util.propertyeditor in search path", foundJBossPath);
   }

   /** The mechanism for mapping java.lang.* variants of the primative types
    misses editors for java.lang.Boolean and java.lang.Integer. Here we test
    the java.lang.* variants we expect editors for.
    **/
   public void testJavaLangEditors()
      throws Exception
   {
      getLog().debug("+++ testJavaLangEditors");
      // The supported java.lang.* types
      Class[] types = {
         Boolean.class,
         Short.class,
         Integer.class,
         Long.class,
         Float.class,
         Double.class,
      };
      // The input string data for each type
      String[][] inputData = {
         {"true", "false", "TRUE", "FALSE", "tRuE", "FaLsE", null},
         {"1", "-1", "0"},
         {"1", "-1", "0"},
         {"1", "-1", "0", "1000"},
         {"1", "-1", "0", "1000.1"},
         {"1", "-1", "0", "1000.1"},
      };
      // The expected java.lang.* instance for each inputData value
      Object[][] expectedData = {
         {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE},
         {Short.valueOf("1"), Short.valueOf("-1"), Short.valueOf("0")},
         {Integer.valueOf("1"), Integer.valueOf("-1"), Integer.valueOf("0")},
         {Long.valueOf("1"), Long.valueOf("-1"), Long.valueOf("0"), Long.valueOf("1000")},
         {Float.valueOf("1"), Float.valueOf("-1"), Float.valueOf("0"), Float.valueOf("1000.1")},
         {Double.valueOf("1"), Double.valueOf("-1"), Double.valueOf("0"), Double.valueOf("1000.1")},
      };

      doTests(types, inputData, expectedData);
   }

   /** Test custom JBoss property editors.
    **/
   public void testJBossEditors()
      throws Exception
   {
      getLog().debug("+++ testJBossEditors");
      Class[] types = {
         javax.management.ObjectName.class,
         java.util.Properties.class,
         java.io.File.class,
         java.net.URL.class,
         java.lang.Class.class
      };
      // The input string data for each type
      String[][] inputData = {
         {"jboss.test:test=1"},
         {"prop1=value1\nprop2=value2"},
         {"/tmp/test1", "/tmp/subdir/../test2"},
         {"http://www.jboss.org"},
         {"java.util.Arrays"},
         {"1", "-1", "0", "1000.1"},
      };
      // The expected java.lang.* instance for each inputData value
      Properties props = new Properties();
      props.setProperty("prop1", "value1");
      props.setProperty("prop2", "value2");
      Object[][] expectedData = {
         {new ObjectName("jboss.test:test=1")},
         {props},
         {new File("/tmp/test1").getCanonicalFile(), new File("/tmp/test2").getCanonicalFile()},
         {new URL("http://www.jboss.org")},
         {java.util.Arrays.class},
         {Double.valueOf("1"), Double.valueOf("-1"), Double.valueOf("0"), Double.valueOf("1000.1")},
      };
      doTests(types, inputData, expectedData);
   }

   private void doTests(Class[] types, String[][] inputData, Object[][] expectedData)
   {
      for(int t = 0; t < types.length; t ++)
      {
         Class type = types[t];
         getLog().debug("Checking property editor for: "+type);
         PropertyEditor editor = PropertyEditorManager.findEditor(type);
         assertTrue("Found property editor for: "+type, editor != null);
         getLog().debug("Found property editor for: "+type+", editor="+editor);
         assertTrue("inputData.length == expectedData.length", inputData[t].length == expectedData[t].length);
         for(int i = 0; i < inputData[t].length; i ++)
         {
            String input = inputData[t][i];
            editor.setAsText(input);
            Object expected = expectedData[t][i];
            Object output = editor.getValue();
            assertTrue("Transform of "+input+" equals "+expected+", output="+output, output.equals(expected));
         }
      }
   }

   /** Override the testServerFound since these test don't need the JBoss server
    */
   public void testServerFound()
   {
   }

}
