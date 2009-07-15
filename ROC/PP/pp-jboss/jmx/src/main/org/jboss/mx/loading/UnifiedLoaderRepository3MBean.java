
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.mx.loading;

import java.util.HashSet;

/**
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public interface UnifiedLoaderRepository3MBean extends UnifiedLoaderRepositoryMBean
{

   /** Called by LoadMgr to obtain all class loaders for the given className
    *@return HashSet<UnifiedClassLoader3>, may be null
    */
   public HashSet getPackageClassLoaders(String className);

   /** A utility method that iterates over all repository class loader and
    * display the class information for every UCL that contains the given
    * className
    */
   public String displayClassInfo(String className);
}
