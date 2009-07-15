/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.deployment.scanner;

import java.io.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Comparator;

/**
 * <p>A simple filter to for the URLDeploymentScanner.  Three arrays are 
 * maintained for checking: a prefix, suffix, and match array.  If the
 * filename starts with any of the prefixes, ends with any of the 
 * suffixes, or exactly matches any of the matches, then the accepts
 * method will return false.
 */

public class DeploymentFilter implements FileFilter
{
   /**
    * Compare the strings backwards.  This assists in suffix comparisons.
    */
   private static final Comparator reverseComparator = new Comparator()
   {
      public int compare(Object o1, Object o2)
      {
         int idx1 = ((String)o1).length();
         int idx2 = ((String)o2).length();
         int comp = 0;
         
         while (comp == 0 && idx1 > 0 && idx2 > 0)
            comp = ((String)o1).charAt(--idx1) - ((String)o2).charAt(--idx2);
         
         return (comp == 0) ? (idx1 - idx2) : comp;
      }
   };
   
   /** the default prefix list */
   private static final String[] DEFAULT_PREFIXES = 
      { "#", "%", ",", ".", "_$" };
      
   /** the default suffix list */
   private static final String[] DEFAULT_SUFFIXES = 
      { "#", "$", "%", ".BAK", ".old", ".orig", ".rej", ".bak", ",v", "~" };
      
   /** the default matches list */
   private static final String[] DEFAULT_MATCHES = 
      { ".make.state", ".nse_depinfo", "CVS", "CVS.admin", "RCS", "RCSLOG", 
        "SCCS", "TAGS", "core", "tags" };

   static 
   {
      // though the current lists are already properly sorted, this explicit
      // sorting adds a bit of safety for future modifications.
      Arrays.sort(DEFAULT_PREFIXES);
      Arrays.sort(DEFAULT_SUFFIXES, reverseComparator);
      Arrays.sort(DEFAULT_MATCHES);
   }
   
   /** The list of disallowed suffixes, sorted using reverse values */
   private String[] suffixes;
   
   /** The sorted list of disallowed prefixes */
   private String[] prefixes;
   
   /** The sorted list of disallowed values */
   private String[] matches;
   
   /** Use the default values for suffixes, prefixes, and matches */
   public DeploymentFilter() 
   {
      suffixes = DEFAULT_SUFFIXES;
      prefixes = DEFAULT_PREFIXES;
      matches = DEFAULT_MATCHES;
   }
   
   /**
    * Create using a custom set of matches, prefixes, and suffixes.  If any of
    * these arrays are null, then the corresponding default will be 
    * substituted.  
    */
   public DeploymentFilter(String[] matches, String[] prefixes, String[] suffixes)
   {
      if (matches != null)
      {
         Arrays.sort(matches);
         this.matches = matches;
      } else this.matches = DEFAULT_MATCHES;
      
      if (prefixes != null)
      {
         Arrays.sort(prefixes);
         this.prefixes = prefixes;
      } else this.prefixes = DEFAULT_PREFIXES;
      
      if (suffixes != null)
      {
         Arrays.sort(suffixes, reverseComparator);
         this.suffixes = suffixes;
      } else this.suffixes = DEFAULT_SUFFIXES;
   }
   
   /**
    * If the filename matches any string in the prefix, suffix, or matches 
    * array, return false.  Perhaps a bit of overkill, but this method 
    * operates in log(n) time, where n is the size of the arrays.
    *
    * @param  file  The file to be tested
    * @return  <code>false</code> if the filename matches any of the prefixes,
    *          suffixes, or matches.
    */
   public boolean accept(File file)
   {
      String name = file.getName();
      
      // check exact match
      int index = Arrays.binarySearch(matches, name);
      if (index >= 0) return false;
      
      // check prefix
      index = Arrays.binarySearch(prefixes, name);
      if (index >= 0) return false;
      if (index < -1 && name.startsWith(prefixes[-2 - index]))
         return false;
      
      // check suffix
      index = Arrays.binarySearch(suffixes, name, reverseComparator);
      if (index >= 0) return false;
      if (index < -1 && name.endsWith(suffixes[-2 - index]))
         return false;
      
      // everything checks out.
      return true;
   }
}
