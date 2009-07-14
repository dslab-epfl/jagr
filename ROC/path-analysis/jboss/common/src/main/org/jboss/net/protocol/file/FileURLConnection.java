/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.net.protocol.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.security.Permission;
import java.io.FilePermission;

/**
 * Provides local file access via URL semantics, correctly returning
 * the last modified time of the underlying file.
 *
 * @version $Revision: 1.1.1.1 $
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  Scott.Stark@jboss.org
 */
public class FileURLConnection
   extends URLConnection
{
   protected File file;

   /**
    * Creates a new <code>FileURLConnection</code> instance.
    *
    * If you deploy an exploded ear through the ant jmx task,
    * invoking MainDeployer deploy(URL), url.getPath() returns null.  
    * I suspect that somewhere a URL is being constructed in a way to confuse
    * the URL constructor, but I can't find it.
    * David Jencks
    *
    * @param url an <code>URL</code> value
    * @exception MalformedURLException if an error occurs
    * @exception IOException if an error occurs
    *
    * @todo figure out why url.getPath() returns null on sub package urls from 
    * exploded packages deployed directly through MainDeployer.deploy(URL url).
    */
   public FileURLConnection(final URL url)
      throws MalformedURLException, IOException
   {
      super(url);
      file = new File(url.getFile().replace('/', File.separatorChar).replace('|', ':'));
      //Original and seemingly better code, however sometimes getPath() returns null.
      //file = new File(url.getPath().replace('/', File.separatorChar).replace('|', ':'));

      doOutput = false;
   }

   /**
    * Returns the underlying file for this connection.
    */
   public File getFile()
   {
      return file;
   }

   /**
    * Checks if the underlying file for this connection exists.
    *
    * @throws FileNotFoundException
    */
   public void connect() throws IOException
   {
      if (connected)
         return;

      if (!file.exists())
      {
         throw new FileNotFoundException(file.getPath());
      }
      
      connected = true;
   }

   public InputStream getInputStream() throws IOException
   {
      if (!connected)
         connect();

      return new FileInputStream(file);
   }

   public OutputStream getOutputStream() throws IOException
   {
      if (!connected)
         connect();
      
      return new FileOutputStream(file);
   }

   /**
    * Provides support for returning the value for the
    * <tt>last-modified</tt> header.
    */
   public String getHeaderField(final String name)
   {
      String headerField = null;
      if (name.equalsIgnoreCase("last-modified"))
         headerField = String.valueOf(getLastModified());
      else if (name.equalsIgnoreCase("content-length"))
         headerField = String.valueOf(file.length());
      else if (name.equalsIgnoreCase("content-type"))
         headerField = getFileNameMap().getContentTypeFor(file.getName());
      else if (name.equalsIgnoreCase("date"))
         headerField = String.valueOf(file.lastModified());
      else
      {
         // This always returns null currently
         headerField = super.getHeaderField(name);
      }
      return headerField;
   }

   /**
    * Return a permission for both read & write since both
    * input and output streams are supported.
    */
   public Permission getPermission() throws IOException
   {
      return new FilePermission(file.getPath(), "read,write");
   }

   /**
    * Returns the last modified time of the underlying file.
    */
   public long getLastModified()
   {
      return file.lastModified();
   }
}

