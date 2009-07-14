/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jboss.mx.logging.Logger;

/** Utility methods for class loader to package names, etc.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class ClassLoaderUtils
{
   private static Logger log = Logger.getLogger(ClassLoaderUtils.class);


   /** Given the URL UCL this method determine what packages
    it contains and create a mapping from the package names to the cl.
    */
   public static String[] updatePackageMap(UnifiedClassLoader cl, HashMap packagesMap)
      throws Exception
   {
      URL url = cl.getURL();
      ClassPathIterator cpi = new ClassPathIterator(url);
      return updatePackageMap(cl, packagesMap, cpi);
   }

   static String[] updatePackageMap(UnifiedClassLoader cl, HashMap packagesMap,
      ClassPathIterator cpi)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      HashSet pkgNameSet = new HashSet();
      ClassPathEntry entry;
      while( (entry = cpi.getNextEntry()) != null )
      {
         String name = entry.getName();
         // First look for a META-INF/INDEX.LIST entry
         if( name.equals("META-INF/INDEX.LIST") )
         {
            readJarIndex(cl, cpi, packagesMap, pkgNameSet);
            // We are done
            break;
         }

         // Skip empty directory entries
         if( entry.isDirectory() == true )
            continue;

         String pkgName = entry.toPackageName();
         addPackage(pkgName, packagesMap, pkgNameSet, cl, trace);
      }
      cpi.close();

      // Return an array of the package names
      String[] pkgNames = new String[pkgNameSet.size()];
      pkgNameSet.toArray(pkgNames);
      return pkgNames;
   }

   /** Read the JDK 1.3+ META-INF/INDEX.LIST entry to obtain the package
    names without having to iterate through all entries in the jar.
    */
   private static void readJarIndex(UnifiedClassLoader cl, ClassPathIterator cpi,
      HashMap packagesMap, HashSet pkgNameSet)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      InputStream zis = cpi.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(zis));
      String line;
      // Skip the jar index header
      while( (line = br.readLine()) != null )
      {
         if( line.length() == 0 )
            break;
      }

      // Read the main jar section
      String jarName = br.readLine();
      if( trace )
         log.trace("Reading INDEX.LIST for jar: "+jarName);
      while( (line = br.readLine()) != null )
      {
         if( line.length() == 0 )
            break;
         String pkgName = line.replace('/', '.');
         addPackage(pkgName, packagesMap, pkgNameSet, cl, trace);
      }
      br.close();
   }

   private static void addPackage(String pkgName, HashMap packagesMap,
      HashSet pkgNameSet, UnifiedClassLoader cl, boolean trace)
   {
      // Skip the standard J2EE archive directories
      if( pkgName.startsWith("META-INF") || pkgName.startsWith("WEB-INF") )
         return;

      HashSet pkgSet = (HashSet) packagesMap.get(pkgName);
      if( pkgSet == null )
      {
         pkgSet = new HashSet();
         packagesMap.put(pkgName, pkgSet);
      }
      if( pkgSet.contains(cl) == false )
      {
         pkgSet.add(cl);
         pkgNameSet.add(pkgName);
         if( trace )
            log.trace(" Indexed pkg: "+pkgName);
      }
   }

   /**
   */
   static class FileIterator
   {
      LinkedList subDirectories = new LinkedList();
      FileFilter filter;
      File[] currentListing;
      int index = 0;

      FileIterator(File start)
      {
         currentListing = start.listFiles();
      }
      FileIterator(File start, FileFilter filter)
      {
         currentListing = start.listFiles(filter);
         this.filter = filter;
      }

      File getNextEntry()
      {
         File next = null;
         if( index >= currentListing.length && subDirectories.size() > 0 )
         {
            do
            {
               File nextDir = (File) subDirectories.removeFirst();
               currentListing = nextDir.listFiles(filter);
            } while( currentListing.length == 0 && subDirectories.size() > 0 );
            index = 0;
         }
         if( index < currentListing.length )
         {
            next = currentListing[index ++];
            if( next.isDirectory() )
               subDirectories.addLast(next);
         }
         return next;
      }
   }

   /** A filter that allows directories and .class files
   */
   static class ClassFilter implements FileFilter
   {
      public boolean accept(File file)
      {
         boolean accept = file.isDirectory() || file.getName().endsWith(".class");
         return accept;
      }
   }

   /**
    */
   static class ClassPathEntry
   {
      String name;
      ZipEntry zipEntry;
      File fileEntry;

      ClassPathEntry(ZipEntry zipEntry)
      {
         this.zipEntry = zipEntry;
         this.name = zipEntry.getName();
      }
      ClassPathEntry(File fileEntry, int rootLength)
      {
         this.fileEntry = fileEntry;
         this.name = fileEntry.getPath().substring(rootLength);
      }

      String getName()
      {
         return name;
      }
      /** Convert the entry path to a package name
       */
      String toPackageName()
      {
         String pkgName = name;
         char separatorChar = zipEntry != null ? '/' : File.separatorChar;
         int index = name.lastIndexOf(separatorChar);
         if( index > 0 )
         {
            pkgName = name.substring(0, index);
            pkgName = pkgName.replace(separatorChar, '.');
         }
         else
         {
            // This must be an entry in the default package (e.g., X.class)
            pkgName = "";
         }
         return pkgName;
      }

      boolean isDirectory()
      {
         boolean isDirectory = false;
         if( zipEntry != null )
            isDirectory = zipEntry.isDirectory();
         else
            isDirectory = fileEntry.isDirectory();
         return isDirectory;
      }
   }

   /** An iterator for jar entries or directory structures.
   */
   static class ClassPathIterator
   {
      ZipInputStream zis;
      FileIterator fileIter;
      File file;
      int rootLength;

      ClassPathIterator(URL url) throws IOException
      {
         String protocol = url.getProtocol();
         if( protocol.equals("file") )
         {
            File tmp = new File(url.getFile());
            String name = tmp.getName();
            if( tmp.isDirectory() )
            {
               rootLength = tmp.getPath().length() + 1;
               fileIter = new FileIterator(tmp, new ClassFilter());
            }
            else
            {
               // Assume this is a jar archive
               InputStream is = new FileInputStream(tmp);
               zis = new ZipInputStream(is);
            }
         }
         else
         {
            // Assume this points to a jar
            InputStream is = url.openStream();
            zis = new ZipInputStream(is);
         }
      }

      ClassPathEntry getNextEntry() throws IOException
      {
         ClassPathEntry entry = null;
         if( zis != null )
         {
            ZipEntry zentry = zis.getNextEntry();
            if( zentry != null )
               entry = new ClassPathEntry(zentry);
         }
         else
         {
            File fentry = fileIter.getNextEntry();
            if( fentry != null )
               entry = new ClassPathEntry(fentry, rootLength);
            file = fentry;
         }

         return entry;
      }

      InputStream getInputStream() throws IOException
      {
         InputStream is = zis;
         if( zis == null )
         {
            is = new FileInputStream(file);
         }
         return is;
      }

      void close() throws IOException
      {
         if( zis != null )
            zis.close();
      }

   }
}

