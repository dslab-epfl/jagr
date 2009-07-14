/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

import java.security.AccessController;

import java.util.Enumeration;

/**
 * The log manager.<p>
 *
 * Ideally, there should be only one of these. In practice
 * there can be one per classloader provided they are diffent scopes.<p>
 *
 * This manager is mainly responsible for controlling logging contexts.
 * This involves using a LoggerContextSelector to determine a LoggerContext.
 * All requests are then delegated to that context.<p>
 *
 * These are main management areas.
 * <ol>
 * <li> Creating loggers.
 * <li> Specifying configuration.
 * <li> Querying.
 * <li> A pluggable LoggerContextSelector.
 * </ol>
 *
 * @see Logger
 * @see LoggerContext
 * @see LoggerContextSelector
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class LoggerManager
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  /**
   * The logging context selector.
   */
  private LoggerContextSelector selector;

  // Static --------------------------------------------------------

  /**
   * The log manager singleton.
   */
  private static LoggerManager manager = new LoggerManager();

  /**
   * Retrieve the log manager.<p>
   *
   * The log manager is a singleton, this method is used to obtain it.
   *
   * @return the log manager
   */
  public static final LoggerManager getLoggerManager()
  {
    return manager;
  }

  // Constructors --------------------------------------------------

  /**
   * Create the log manager singleton.<p>
   *
   * The singleton will be contructed called when the LoggerManager is first
   * loading. This will be either during initial configuration, or
   * when the first Logger is created.<p>
   *
   * When the first call is due Logger creation, there is enough
   * implementated in the default classes to get a simple logging
   * configuration.
   *
   * @see DefaultLoggerContextSelector
   * @see DefaultLoggerAdapter
   */
  private LoggerManager()
  {
    selector = new DefaultLoggerContextSelector();
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
   * @return a logger.
   */
  public final Logger getLogger(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("null name");
    return selector.getLoggerContext().getLogger(name);
  }

  /**
   * Register the adapter factory for the current context
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
    selector.getLoggerContext().setLoggerAdapterFactory(factory);
  }

  /**
   * Set the configurator for the current context.<p>
   *
   * The configurator is implementation dependent.
   *
   * @param configurator the object used to configure the context
   * @exception SecurityException when not authorised
   */
  public final void setConfiguration(Object configurator)
    throws SecurityException
  {
    selector.getLoggerContext().setConfiguration(configurator);
  }

  /**
   * Reset the configuration to the default for the current context.
   *
   * @exception SecurityException when not authorised
   */
  public final void resetConfiguration()
    throws SecurityException
  {
    selector.getLoggerContext().resetConfiguration();
  }

  /**
   * Retrieve the list of loggers.
   * These are loggers from the current context.<p>
   *
   * WARNING: The enumeration is NOT synchronized.
   *
   * @return an Enumeration of loggers
   */
  public final Enumeration getLoggers()
  {
    return selector.getLoggerContext().getLoggers();
  }

  /**
   * Set the logging context selector.<p>
   *
   * @param selector the new Logging Context Selector, cannot be null
   * @exception SecurityException when not authorised
   */
  public final void setLoggerContextSelector(LoggerContextSelector seletor)
    throws SecurityException
  {
    if (selector == null)
      throw new IllegalArgumentException("null selector");
    //TODO AccessController.checkPermission(new LoggerPermission("control"));
    this.selector = selector;
  }

  // X Implementation ----------------------------------------------

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  /**
   * Associate a special logging context with a context object.
   *
   * @param context the context object cannot be null
   * @param loggerContext the logger context cannot be null
   */
  void associateContext(Object context, LoggerContext loggerContext)
  {
    selector.associate(context, loggerContext);
  }

  /**
   * Remove an association of a special logging context with a context object.
   *
   * @param context the context object to remove cannot be null
   */
  void removeContext(Object context)
  {
    selector.remove(context);
  }

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
