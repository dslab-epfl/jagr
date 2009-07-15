/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Level;

/** 
 * A custom log4j Category wrapper that adds a trace level priority, 
 * is serializable and only exposes the relevent factory and logging 
 * methods.
 *
 * @see #isTraceEnabled
 * @see #trace(Object)
 * @see #trace(Object,Throwable)
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  Scott.Stark@jboss.org
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Logger
   implements java.io.Serializable
{
   /** The category name. */
   private final String name;

   /** The Log4j delegate logger. */
   private transient Category log;

   /** 
    * Creates new JBossCategory with the given category name.
    *
    * @param name    the category name.
    */
   protected Logger(final String name)
   {
      this.name = name;
      log = Category.getInstance(name);
   }

   /**
    * Expose the raw category for this logger.
    */
   public Category getCategory()
   {
      return log;
   }

   /**
    * Return the category name of this logger.
    *
    * @return The category name of this logger.
    */
   public String getName()
   {
      return name;
   }
   
   /** 
    * Check to see if the TRACE priority is enabled for this category.
    *
    * @return true if a {@link #trace(Object)} method invocation would pass
    * the msg to the configured appenders, false otherwise.
    */
   public boolean isTraceEnabled()
   {
      if (log.isEnabledFor(XLevel.TRACE) == false)
         return false;
      return XLevel.TRACE.isGreaterOrEqual(log.getEffectiveLevel());
   }

   /** 
    * Issue a log msg with a priority of TRACE.
    * Invokes log.log(XLevel.TRACE, message);
    */
   public void trace(Object message)
   {
      log.log(XLevel.TRACE, message);
   }

   /** 
    * Issue a log msg and throwable with a priority of TRACE.
    * Invokes log.log(XLevel.TRACE, message, t);
    */
   public void trace(Object message, Throwable t)
   {
      log.log(XLevel.TRACE, message, t);
   }

   /**
    * Check to see if the TRACE priority is enabled for this category.
    *
    * @return true if a {@link #trace(Object)} method invocation would pass
    * the msg to the configured appenders, false otherwise.
    */
   public boolean isDebugEnabled()
   {
      Level p = Level.DEBUG;
      if (log.isEnabledFor(p) == false)
         return false;
      return p.isGreaterOrEqual(log.getEffectiveLevel());
   }

   /** 
    * Issue a log msg with a priority of DEBUG.
    * Invokes log.log(Level.DEBUG, message);
    */
   public void debug(Object message)
   {
      log.log(Level.DEBUG, message);
   }

   /** 
    * Issue a log msg and throwable with a priority of DEBUG.
    * Invokes log.log(Level.DEBUG, message, t);
    */
   public void debug(Object message, Throwable t)
   {
      log.log(Level.DEBUG, message, t);
   }

   /** 
    * Check to see if the INFO priority is enabled for this category.
    *
    * @return true if a {@link #info(Object)} method invocation would pass
    * the msg to the configured appenders, false otherwise.
    */
   public boolean isInfoEnabled()
   {
      Level p = Level.INFO;
      if (log.isEnabledFor(p) == false)
         return false;
      return p.isGreaterOrEqual(log.getEffectiveLevel());
   }

   /** 
    * Issue a log msg with a priority of INFO.
    * Invokes log.log(Level.INFO, message);
    */
   public void info(Object message)
   {
      log.log(Level.INFO, message);
   }

   /**
    * Issue a log msg and throwable with a priority of INFO.
    * Invokes log.log(Level.INFO, message, t);
    */
   public void info(Object message, Throwable t)
   {
      log.log(Level.INFO, message, t);
   }

   /** 
    * Issue a log msg with a priority of WARN.
    * Invokes log.log(Level.WARN, message);
    */
   public void warn(Object message)
   {
      log.log(Level.WARN, message);
   }

   /** 
    * Issue a log msg and throwable with a priority of WARN.
    * Invokes log.log(Level.WARN, message, t);
    */
   public void warn(Object message, Throwable t)
   {
      log.log(Level.WARN, message, t);
   }

   /** 
    * Issue a log msg with a priority of ERROR.
    * Invokes log.log(Level.ERROR, message);
    */
   public void error(Object message)
   {
      log.log(Level.ERROR, message);
   }

   /** 
    * Issue a log msg and throwable with a priority of ERROR.
    * Invokes log.log(Level.ERROR, message, t);
    */
   public void error(Object message, Throwable t)
   {
      log.log(Level.ERROR, message, t);
   }

   /** 
    * Issue a log msg with a priority of FATAL.
    * Invokes log.log(Level.FATAL, message);
    */
   public void fatal(Object message)
   {
      log.log(Level.FATAL, message);
   }

   /** 
    * Issue a log msg and throwable with a priority of FATAL.
    * Invokes log.log(Level.FATAL, message, t);
    */
   public void fatal(Object message, Throwable t)
   {
      log.log(Level.FATAL, message, t);
   }

   /** 
    * Issue a log msg with the given priority.
    * Invokes log.log(p, message);
    */
   public void log(Level p, Object message)
   {
      log.log(p, message);
   }

   /** 
    * Issue a log msg with the given priority.
    * Invokes log.log(p, message, t);
    */
   public void log(Level p, Object message, Throwable t)
   {
      log.log(p, message, t);
   }


   /////////////////////////////////////////////////////////////////////////
   //                         Custom Serialization                        //
   /////////////////////////////////////////////////////////////////////////

   private void readObject(java.io.ObjectInputStream stream)
      throws java.io.IOException, ClassNotFoundException
   {
      // restore non-transient fields (aka name)
      stream.defaultReadObject();
      
      // Restore logging
      log = Category.getInstance(name);
   }


   /////////////////////////////////////////////////////////////////////////
   //                            Factory Methods                          //
   /////////////////////////////////////////////////////////////////////////

   /** 
    * Create a Logger instance given the category name.
    *
    * @param name    the category name
    */
   public static Logger getLogger(String name)
   {
      Logger logger = new Logger(name);
      return logger;
   }

   /** 
    * Create a Logger instance given the category name with the given suffix.
    *
    * <p>This will include a category seperator between classname and suffix
    *
    * @param name     The category name
    * @param suffix   A suffix to append to the classname.
    */
   public static Logger getLogger(String name, String suffix)
   {
      return new Logger(name + "." + suffix);
   }

   /** 
    * Create a Logger instance given the category class. This simply
    * calls create(clazz.getName()).
    *
    * @param clazz    the Class whose name will be used as the category name
    */
   public static Logger getLogger(Class clazz)
   {
      Logger logger = new Logger(clazz.getName());
      return logger;
   }

   /** 
    * Create a Logger instance given the category class with the given suffix.
    *
    * <p>This will include a category seperator between classname and suffix
    *
    * @param clazz    The Class whose name will be used as the category name.
    * @param suffix   A suffix to append to the classname.
    */
   public static Logger getLogger(Class clazz, String suffix)
   {
      return new Logger(clazz.getName() + "." + suffix);
   }
}
