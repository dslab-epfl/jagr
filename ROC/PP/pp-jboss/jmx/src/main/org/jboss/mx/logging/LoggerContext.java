/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.security.AccessController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A logger context.<p>
 *
 * The logger context holds the state for loggers in within its context.<p>
 *
 * It delegates most implementation to the registered LoggerAdapterFactory,
 * this allows different logging implementations in different contexts. It
 * allows the logging implementation to vary configuration according to
 * context.<p>
 *
 * The processing performed within this class is the "hot-swapping" of
 * implementations and the querying of the state.
 *
 * @see Logger
 * @see LoggerAdapter
 * @see LoggerAdapterFactory
 * @see LoggerContextSelector
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class LoggerContext
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  /**
   * The current logging adapter factory.
   */
  private LoggerAdapterFactory factory;

  /**
   * The loggers by name. The loggers are stored in weak references
   * for garbage collection.
   */
  private final HashMap loggers = new HashMap();

  // Static --------------------------------------------------------

  /**
   * The log manager
   */
  private static final LoggerManager manager = LoggerManager.getLoggerManager();

  /**
   * The background thread waiting for loggers to be garbage collected
   * When the first logger is created, the thread is started
   */
  private static final GarbageMonitor garbageMonitor = new GarbageMonitor();

  /**
   * The reference queue for garbage collection
   */
  private static final ReferenceQueue garbageCollected = new ReferenceQueue();

  /**
   * The logger info by weak reference, used for garbage collection.
   */
  private static final HashMap monitored = new HashMap();

  /**
   * Monitor garbage collection information
   *
   * @param weak the weak reference that should be garbage collected
   * @param context the context of the logger
   * @param name the name of the logger
   */
  private static final void monitor(WeakReference weak, 
                                    LoggerContext context,
                                    String name)
  {
    LoggerInfo info = new LoggerInfo(context, name);
    synchronized (monitored)
    {
      monitored.put(weak, info);
    }
  }

  /**
   * Remove a weak reference from the loggers
   *
   * @param weak the weak reference that has been garbage collected
   */
  private static final void remove(WeakReference weak)
  {
    LoggerInfo info;
    synchronized (monitored)
    {
      info = (LoggerInfo) monitored.remove(weak);
    }
    info.context.remove(info.name);
  }

  // Constructors --------------------------------------------------

  /**
   * Create a new context with the given LoggerAdapterFactory.<p>
   *
   * @param factory the factory
   */
  public LoggerContext(LoggerAdapterFactory factory)
  {
    this.factory = factory;
    Object context = factory.getContext();
    if (context != null)
      manager.associateContext(context, this);
  }

  // Public --------------------------------------------------------

  /**
   * Create a new logger given a name.<p>
   *
   * It is common that the name is hierarchical like java class names.
   * However this does not mean that the real logger will understand
   * or use the hierarchy, or even the name.<p>
   *
   * @param name the name to assign to the logger, cannot be null
   * @return a logger
   */
  public final Logger getLogger(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("null name");

    Logger result;
    WeakReference weak; 
    synchronized (loggers)
    {
      // Check to see if we already have a logger
      weak = (WeakReference) loggers.get(name); 
      if (weak != null)
      {
        result = (Logger) weak.get();
        if (result != null)
          return result;
      }

      // Delegate the "creation" of a new logger
      LoggerAdapter adapter = factory.getLoggerAdapter(name);
      result = new Logger(adapter);

      // Keep track of the loggers
      weak = new WeakReference(result, garbageCollected);
      loggers.put(name, weak);
    }

    // Monitor the logger for garbage collection
    monitor(weak, this, name);
    
    return result;
  }

  /**
   * Retrieve the current logger adapter factory.
   *
   * @return the logger adapter factory
   * @exception SecurityException when not authorised
   */
  public final LoggerAdapterFactory getLoggerAdapterFactory()
    throws SecurityException
  {
    return factory;
  }

  /**
   * Register the adapter factory for this context.
   * Any existing loggers are updated to use an adapter from this factory.<p>
   *
   * WARNING: New loggers cannot be retrieved during this operation.
   *
   * @param factory the factory creating new adapters
   * @exception SecurityException when not authorised
   */
  public final void setLoggerAdapterFactory(LoggerAdapterFactory factory)
    throws SecurityException
  {
    if (factory == null)
      throw new IllegalArgumentException("null factory");
    // TODO AccessController.checkPermission(new LoggerPermission("control"));

    synchronized (loggers)
    {
      Class oldDelegateClass = this.factory.getLoggerAdapterClass();
      Class newDelegateClass = factory.getLoggerAdapterClass();

      // No need to update adapters when using the same class
      if (oldDelegateClass != newDelegateClass)
        return;

      // Remove any old context
      Object context = this.factory.getContext();
      if (context != null)
        manager.removeContext(context);

      // Switch the factory
      this.factory = factory;

      // Set the new context
      context = factory.getContext();
      if (context != null)
        manager.associateContext(context, this);

      // Update each logger to have the correct adapter
      Iterator iterator = loggers.entrySet().iterator();
      while (iterator.hasNext())
      {
        Map.Entry entry = (Map.Entry) iterator.next();

        // See whether this logger is still used
        WeakReference weak = (WeakReference) entry.getValue();
        Logger logger = (Logger) weak.get();
        if (logger == null)
          iterator.remove();

        // If we have the incorrect adapter, update it
        LoggerAdapter adapter = logger.getLoggerAdapter();
        if (adapter.getClass().equals(newDelegateClass) == false)
        {
          String name = (String) entry.getKey();
          logger.setLoggerAdapter(factory.getLoggerAdapter(name));
        }
      }
    }
  }

  /**
   * Set the configurator for this context.<p>
   *
   * The configurator is implementation dependent.
   *
   * @param configurator the object used to configure the context
   * @exception SecurityException when not authorised
   */
  public final void setConfiguration(Object configurator)
    throws SecurityException
  {
    factory.setConfiguration(configurator);
  }

  /**
   * Reset the configuration to the default for this context.
   *
   * @exception SecurityException when not authorised
   */
  public final void resetConfiguration()
    throws SecurityException
  {
    factory.resetConfiguration();
  }

  /**
   * Retrieve the list of loggers in this context.
   *
   * WARNING: The enumeration is NOT synchronized.
   *
   * @return an Enumeration of loggers
   */
  public final Enumeration getLoggers()
  {
    return new LoggersEnumeration();
  }

  // X Implementation ----------------------------------------------

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  /**
   * Remove a logger, used during garbage collection
   *
   * @param the name of the logger to remove
   */
  private final void remove(String name)
  {
    synchronized (loggers)
    {
      loggers.remove(name);
    }
  }

  // Inner classes -------------------------------------------------

  /**
   * An enumeration for the loggers.
   */
  private class LoggersEnumeration
    implements Enumeration
  {
    // Attributes --------------------------------------------------

    /**
     * The iterator for this enumeration
     */
    private Iterator iterator;

    /**
     * The current logger, the logger return by nextElement()
     */
    private Object current;

    // Constructors ------------------------------------------------

    /**
     * Construct an enumeration using an iterator over the values
     * parts of the name to loggers map.
     */
    public LoggersEnumeration()
    {
      iterator = loggers.values().iterator();
      next();
    }

    // Public ------------------------------------------------------

    // Enumeration Implementation ----------------------------------

    public final boolean hasMoreElements()
    {
      return (current != null);
    }

    public final Object nextElement()
    {
      Object result = current;
      next();
      return result;
    }

    // Y Overrides -------------------------------------------------
 
    // Private -----------------------------------------------------

    /**
     * Retrieve the next logger after
     * We need to skip over loggers that have been garbage collected.
     */
    private final void next()
    {
      current = null;
      while (iterator.hasNext())
      {
        WeakReference weak = (WeakReference) iterator.next();
        current = weak.get();
        if (current != null)
          return;
      }
    }
  }

  /**
   * Logger information used in garbage collection.
   */
  private static class LoggerInfo
  {
    // Attributes --------------------------------------------------

    /**
     * The context of the logger
     */
    public LoggerContext context;

    /**
     * The name of the logger
     */
    private String name;

    // Constructors ------------------------------------------------

    /**
     * Construct logger information
     * 
     * @param context the logger context
     * @param name the name of the logger
     */
    public LoggerInfo(LoggerContext context, String name)
    {
      this.context = context;
      this.name = name;
    }

    // Public ------------------------------------------------------

    // X Implementation --------------------------------------------

    // Y Overrides -------------------------------------------------
 
    // Private -----------------------------------------------------
  }

  /**
   * The garbage collection monitor.
   */
  private static class GarbageMonitor
    extends Thread
  {
    // Attributes --------------------------------------------------

    // Constructors ------------------------------------------------

    /**
     * Construct a new Garbage Monitor
     */
    public GarbageMonitor()
    {
      setDaemon(true);
      start();
    }

    // Public ------------------------------------------------------

    // X Implementation --------------------------------------------

    // Thread Overrides --------------------------------------------

    /**
     * Waits for loggers to be garbage collected and tidys up
     * the loggers in the contexts
     */
    public final void run()
    {
      while (true)
      {
        try
        {
          WeakReference weak = (WeakReference) garbageCollected.remove();
          remove(weak);
        }
        catch (InterruptedException ie)
        {
          break;
        }
      }
    }
 
    // Private -----------------------------------------------------
  }
}
