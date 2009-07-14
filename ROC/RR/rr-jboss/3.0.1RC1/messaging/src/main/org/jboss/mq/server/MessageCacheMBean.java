/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mq.server;

import org.jboss.system.ServiceMBean;
import javax.management.ObjectName;

/**
 * Defines the managment interface that is exposed to the MessageCache
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version    $Revision: 1.1.1.1 $
 */
public interface MessageCacheMBean
   extends ServiceMBean
{
   /**
    * Gets the hardRefCacheSize
    * 
    * @return Returns a int
    */
   int getHardRefCacheSize();

   /**
    * Gets the softRefCacheSize
    * 
    * @return Returns a int
    */
   int getSoftRefCacheSize();

   /**
    * Gets the totalCacheSize
    * 
    * @return Returns a int
    */
   int getTotalCacheSize();
   	
   /**
    * Gets the cacheMisses
    * 
    * @return Returns a int
    */
   int getCacheMisses();

   /**
    * Gets the cacheHits
    * 
    * @return Returns a int
    */
   int getCacheHits();
   
   /**
    * Gets the highMemoryMark
    * 
    * @return Returns a long
    */
   long getHighMemoryMark();

   /**
    * Sets the highMemoryMark
    * 
    * @param highMemoryMark The highMemoryMark to set
    */
   void setHighMemoryMark(long highMemoryMark);

   /**
    * Gets the maxMemoryMark
    * 
    * @return Returns a long
    */
   long getMaxMemoryMark();

   /**
    * Sets the maxMemoryMark
    * 
    * @param maxMemoryMark The maxMemoryMark to set
    */
   void setMaxMemoryMark(long maxMemoryMark);

   long getCurrentMemoryUsage();

   MessageCache getInstance();
   
   void setCacheStore(ObjectName cacheStore);
}
