/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

import java.lang.reflect.Constructor;
import java.util.WeakHashMap;

import org.jboss.mx.server.ServerConstants;

/**
 * The default context selector.<p>
 *
 * This has singleton context configured initially configured with the default
 * logger adapter implementation.<p>
 *
 * Extra contexts can be registered keyed by classloader.<p>
 *
 * When new loggers are requested, the thread's context classloader is
 * checked to discover if there is a special context. If that fails the
 * parent is checked and that its parent, etc. If none of the classloaders
 * has a special logging context, the default logging context is used.
 *
 * @see DefaultLoggerAdapter
 * @see DefaultLoggerAdapterFactory
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class DefaultLoggerContextSelector
  implements LoggerContextSelector, ServerConstants
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  /**
   * The association of classloader to logger context.
   */
  private WeakHashMap contexts = new WeakHashMap();

  // Static --------------------------------------------------------

  /**
   * The logger context
   */
  public static LoggerContext defaultLoggerContext;

  /**
   * Configure the logger context<br>
   * FIXME: This is a hack. It tries to use default implementation from
   * ServerConstants (currently log4j). If that isn't available, 
   * it quietly ignores it and reverts to System.err
   */
  static
  {
      String factoryClass = System.getProperty(
            LOGGER_FACTORY_CLASS_PROPERTY,
            DEFAULT_LOGGER_FACTORY_CLASS
      );
      
      // Try to instantiate the LoggerAdapterFactory
      LoggerAdapterFactory factory = null;
      try
      {
         // Try loading factory class via thread context classloader
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class clazz = cl.loadClass(factoryClass);
         
         // retrieve the constructor <init>()
         Constructor constructor = clazz.getConstructor(new Class[0]);
         
         // instantiate the factory
         factory = (LoggerAdapterFactory)constructor.newInstance(new Object[0]);
      }
      catch (Throwable e)
      {
         // Failed, use the default
         factory = new DefaultLoggerAdapterFactory();
      }

      // Create the logger context using the factory
      defaultLoggerContext = new LoggerContext(factory);
  }

  // Constructors --------------------------------------------------

  // Public --------------------------------------------------------

  // LoggerContextSelector Implementation --------------------------

  /**
   * Associate a special logging context with a classloader.
   *
   * @param context the class loader for the context cannot be null
   * @param loggerContext the logger context cannot be null
   */
  public final void associate(Object context, LoggerContext loggerContext)
  {
    if (context == null)
      throw new IllegalArgumentException("null context");
    if ((context instanceof ClassLoader) == false)
      throw new RuntimeException("Only classloaders are allowed as contexts");
    if (loggerContext == null)
      throw new IllegalArgumentException("null logger context");
    synchronized (contexts)
    {
      contexts.put(context, loggerContext);
    }
  }

  /**
   * Remove an association of a special logging context with a classloader.
   *
   * @param context the class loader for the context to remove cannot be null
   */
  public final void remove(Object context)
  {
    if (context == null)
      throw new IllegalArgumentException("null context");
    synchronized (contexts)
    {
      contexts.remove(context);
    }
  }

  public final LoggerContext getLoggerContext()
  {
    LoggerContext result = null;

    // Check for special context keep going up the class loader
    // tree until we find one or run out of class loaders
    synchronized (contexts)
    {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      while (cl != null)
      {
        result = (LoggerContext) contexts.get(cl);
        if (result != null)
          break;
        cl = cl.getParent();
      }
    }

    // No special context, use the default
    if (result == null)
      result = defaultLoggerContext;

    return result;
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
