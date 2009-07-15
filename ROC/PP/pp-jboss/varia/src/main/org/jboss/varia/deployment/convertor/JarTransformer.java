/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.varia.deployment.convertor;

import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * JarTransformer is used to transform passed in jar file.
 * Transformation algorithm:
 * 1. open JarInputStream on passed in Jar file,
 *    open JarOutputStream for result;
 * 2. read next Jar entry;
 * 3. check whether Jar entry is an XML file
 *    - if it's not, copy Jar entry to result and go to step 2.
 * 4. check whether there is an XSL file with name equal to XML file's
 *    in classpath.
 *    - if there isn't, copy Jar entry to result and go to step 2.
 * 5. check whether there is a properties file with the name equal to
 *    XML file's name + "-output.properties"
 * 6. set needed xsl parameters
 * 7. transform Jar entry with xsl template and output properties
 *    (if were found)
 * 8. check whether there is a property "newname" in output properties
 *    - if there is, write transformed entry to result with the value
 *      of "newname";
 *    - otherwise write transformed entry to result with the original
 *      Jar entry name
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 */
public class JarTransformer
{
   // Attributes --------------------------------------------------------
   private static Logger log = Logger.getLogger(JarTransformer.class.getName());


   // Public static methods ---------------------------------------------
   /**
    * Applies transformations to xml sources for passed in jar file
    */
   public static synchronized void transform( File root,
                                              Properties globalXslParams )
      throws Exception
   {
      log.debug( "transform(), root: " + root );

      // local xsl params
      Properties xslParams = new Properties( globalXslParams );

      File metaInf = new File( root, "META-INF");

      log.debug( "transform(), is META-INF available: " + metaInf.exists() );
      if( !metaInf.exists() )
      {
         throw new Exception( "No META-INF directory found" );
      }

      // set path to ejb-jar.xml in xslParams
      File ejbjar = new File( metaInf, "ejb-jar.xml" );
      if( ejbjar.exists() )
         xslParams.setProperty( "ejb-jar", ejbjar.getAbsolutePath() );

      // list only xml files.
      // Note: returns null only if the path name isn't a directory
      // or I/O exception occured
      File[] files = metaInf.listFiles(
        new FileFilter()
        {
           public boolean accept(File file)
           {
              if( file.getName().endsWith( ".xml" )
                 && !file.isDirectory() )
                 return true;
              return false;
           }
        }
      );

      log.debug( "transform(), list of files: " + java.util.Arrays.asList( files ) );
      for( int i = 0; i < files.length; i++ )
      {
         File file = files[i];

         // construct names for transformation resources
         String xmlName = file.getName();
         String xslName = xslParams.getProperty( "resources_path" )
                          + xmlName.substring( 0, xmlName.length() - 3 )
                          + "xsl";
         String propsName = xslParams.getProperty( "resources_path" )
                            + xmlName.substring( 0, xmlName.length() - 4 )
                            + "-output.properties";

         // try to find XSL template and open InputStream on it
         InputStream templateIs = null;
         try
         {
            templateIs = JarTransformer.class.getClassLoader().
               getResource( xslName ).openStream();
         }
         catch( Exception e )
         {
            // Ignore Exception because when it is not found then we are going to ignore it
         }

         if( templateIs == null )
         {
            log.debug( "xsl template wasn't found for '" + xmlName + "'" );
            continue;
         }

         log.debug( "Attempt to transform '" + xmlName + "' with '" + xslName + "'" );

         // try to load output properties
         Properties outputProps = loadProperties( propsName );

         // transform Jar entry and write transformed data to result
         try
         {
            // transformation closes the input stream, so read entry to byte[]
            InputStream input = new FileInputStream( file );
            byte[] bytes = readBytes( input );
            input.close();
            bytes = transformBytes( bytes, templateIs, outputProps, xslParams );

            String entryname = null;
            try
            {
               entryname = outputProps.getProperty( "newname" );
            }
            catch(Exception e) { }

            if(entryname == null)
            {
               entryname = file.getName();
            }

            OutputStream output = new FileOutputStream(
               new File( root, entryname ) );
            writeBytes( output, bytes );
            output.close();

            log.debug( "Entry '" + file.getName() +
               "' transformed to '" + entryname + "'" );
         }
         catch(Exception e)
         {
            log.debug( "Exception while transforming entry '" + file.getName(), e);
         }
      }
   }

   // Private static methods ------------------------------------------
   /**
    * Searches for, loads and returns properties from file
    * <code>propsName</code>
    */
   private static Properties loadProperties( String propsName )
   {
      Properties props = new Properties();
      try
      {
         InputStream propsIs = JarTransformer.class.getClassLoader().
            getResource( propsName ).openStream();
         props.load( propsIs );
         propsIs.close();
         log.debug( "Loaded properties '" + propsName + "'" );
      }
      catch(Exception e)
      {
         log.debug( "Properties file '" + propsName + "' wasn't found." );
      }
      return props;
   }

   /**
    * Returns byte array that is the result of transformation of
    * the passed in byte array with xsl template and output properties
    */
   private static byte[] transformBytes( byte[] bytes,
                                         InputStream xslIs,
                                         Properties outputprops )
      throws Exception
   {
      ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
      ByteArrayOutputStream baos = new ByteArrayOutputStream( 2048 );
      XslTransformer.applyTransformation( bais, baos, xslIs, outputprops );
      xslIs.close();
      return baos.toByteArray();
   }

   /**
    * Returns byte array that is the result of transformation of
    * the passed in byte array with xsl template, output properties
    * and xsl parameters
    */
   private static byte[] transformBytes( byte[] bytes,
                                         InputStream xslIs,
                                         Properties outputProps,
                                         Properties xslParams )
      throws Exception
   {
      ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
      ByteArrayOutputStream baos = new ByteArrayOutputStream( 2048 );
      XslTransformer.applyTransformation(
         bais, baos, xslIs, outputProps, xslParams );
      xslIs.close();
      return baos.toByteArray();
   }

   /**
    * Writes byte array to OutputStream.
    */
   private static void writeBytes( OutputStream os, byte[] bytes )
      throws Exception
   {
      os.write(bytes, 0, bytes.length);
   }

   /**
    * Copies bytes from InputStream to OutputStream.
    * Returns the number of bytes copied.
    */
   private static int copyBytes( InputStream is,OutputStream os )
      throws Exception
   {
      byte[] buffer = readBytes( is );
      os.write( buffer, 0, buffer.length );
      return buffer.length;
   }

   /**
    * Returns byte array read from InputStream
    */
   private static byte[] readBytes( InputStream is )
      throws IOException
   {
      byte[] buffer = new byte[ 8192 ];
      ByteArrayOutputStream baos = new ByteArrayOutputStream( 2048 );
      int n;
      baos.reset();
      while( (n = is.read(buffer, 0, buffer.length)) != -1 )
      {
         baos.write(buffer, 0, n);
      }
      return baos.toByteArray();
   }
}
