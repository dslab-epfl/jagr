/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.util.file;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import org.jboss.util.stream.Streams;

/**
 * A collection of file utilities.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public final class Files
{
   /**
    * Delete a directory and all of its contents.
    *
    * @param dir  The directory to delete.
    *
    * @throws IOException  Failed to delete directory.
    */
   public static void delete(File dir) throws IOException {
      File files[] = dir.listFiles();
      if (files != null) {
         for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
               // delete the directory and all of its contents.
               delete(files[i]);
            }

            // delete each file in the directory
            files[i].delete();
         }
      }

      // finally delete the directory
      dir.delete();
   }

   /** The default size of the copy buffer. */
   public static final int DEFAULT_BUFFER_SIZE = 8192; // 8k

   /**
    * Copy a file.
    *
    * @param source  Source file to copy.
    * @param target  Destination target file.
    * @param buff    The copy buffer.
    *
    * @throws IOException  Failed to copy file.
    */
   public static void copy(final File source,
                           final File target,
                           final byte buff[])
      throws IOException
   {
      DataInputStream in = new DataInputStream
         (new BufferedInputStream(new FileInputStream(source)));

      DataOutputStream out = new DataOutputStream
         (new BufferedOutputStream(new FileOutputStream(target)));

      int read;

      try {
         while ((read = in.read(buff)) != -1) {
            out.write(buff, 0, read);
         }
      }
      finally {
         Streams.flush(out);
         Streams.close(in);
         Streams.close(out);
      }
   }

   /**
    * Copy a file.
    *
    * @param source  Source file to copy.
    * @param target  Destination target file.
    * @param size    The size of the copy buffer.
    *
    * @throws IOException  Failed to copy file.
    */
   public static void copy(final File source,
                           final File target,
                           final int size)
      throws IOException
   {
      copy(source, target, new byte[size]);
   }

   /**
    * Copy a file.
    *
    * @param source  Source file to copy.
    * @param target  Destination target file.
    *
    * @throws IOException  Failed to copy file.
    */
   public static void copy(final File source, final File target)
      throws IOException
   {
      copy(source, target, DEFAULT_BUFFER_SIZE);
   }
}
