/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.net.protocol.njar;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

/**
 * A protocol handler for the n(ested)jar protocol.
 *
 * <p>
 * This is class allows you to use the njar: URL protocol. It is very
 * similar to it's jar: cusin.  The difference being that jars can be
 * nested.
 *
 * <p>
 * An example of how to use this class is:
 * <pre>
 *
 *    URL url = new URL("njar:njar:file:c:/test1.zip^/test2.zip^/hello.txt");
 *    url.openStream();
 *
 * </pre>
 *
 * <p>
 * Please be aware that the njar protocol caches it's jar in temporary 
 * storage when connections are opened into them.  So for the above 
 * example, 2 files would cached a temp files names similar to nested-xxxx.jar
 *
 * @todo Add accessors so that the cache can be flushed.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 */
public class Handler
   extends URLStreamHandler
{
   // URL protocol designations
   public static final String PROTOCOL = "njar";
   public static final String NJAR_SEPARATOR = "^/";
   public static final String JAR_SEPARATOR = "!/";

   protected Map savedJars = new HashMap();

   public URLConnection openConnection(final URL url)
      throws IOException
   {
      String file = url.getFile();
      String embeddedURL = file;
      String jarPath = "";
      
      int pos = file.lastIndexOf(NJAR_SEPARATOR);
      if (pos >= 0)
      {
         embeddedURL = file.substring(0, pos);
         if (file.length() > pos + NJAR_SEPARATOR.length())
            jarPath = file.substring(pos + NJAR_SEPARATOR.length());
      }

      if (embeddedURL.startsWith(PROTOCOL))
      {
         //System.out.println("Opening next  nested jar: " + embeddedURL);
         File tempJar = (File) savedJars.get(embeddedURL);
         if (tempJar == null)
         {
            InputStream embededData = new URL(embeddedURL).openStream();
            tempJar = File.createTempFile("nested-", ".jar");
            tempJar.deleteOnExit();
            //System.out.println("temp file location : " + tempJar);
            storeJar(embededData, new FileOutputStream(tempJar));
            savedJars.put(embeddedURL, tempJar);
         }

         String t = tempJar.getCanonicalFile().toURL().toExternalForm();
         //System.out.println("file URL : " + t);
         t = "njar:" + t + NJAR_SEPARATOR + jarPath;
         //System.out.println("Opening saved jar: " + t);
         
         return new URL(t).openConnection();
         
      }
      else
      {
         //System.out.println("Opening final nested jar: " + embeddedURL);
         return new URL("jar:" + embeddedURL + JAR_SEPARATOR + jarPath).openConnection();
      }
   }

   protected void storeJar(final InputStream in, final OutputStream out) 
      throws IOException
   {

      BufferedInputStream bis = null;
      BufferedOutputStream bos = null;
      try
      {
         bis = new BufferedInputStream(in);
         bos = new BufferedOutputStream(out);

         byte data[] = new byte[512];
         int c;
         while ((c = bis.read(data)) >= 0)
         {
            bos.write(data, 0, c);
         }

      }
      finally
      {
         try
         {
            bis.close();
         }
         catch (IOException ignore)
         {
         }
         try
         {
            bos.close();
         }
         catch (IOException ignore)
         {
         }
      }
   }

  public static URL njarToFile(URL url)
    {
      if (url.getProtocol().equals(PROTOCOL))
      {
	try
	{
	  // force the resource we are after to be unpacked - thanks
	  // Jan & David...!
	  URL dummy=new URL(PROTOCOL+":"+url.toString()+NJAR_SEPARATOR+"dummy.jar");
	  String tmp=dummy.openConnection().getURL().toString();
	  tmp=tmp.substring("jar:".length());
	  tmp=tmp.substring(0, tmp.length()-(JAR_SEPARATOR+"dummy.jar").length());
	  return new URL(tmp);
	}
	catch (Exception ignore)
	{
	}
      }

      return url;
    }
}

