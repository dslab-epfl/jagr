/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * The logger.<p>
 *
 * It provides a standard interface based on log4j or
 * java.util.logging package.<p>
 *
 * This does no work itself, instead it delegates to the real
 * implementation via a LoggerAdapter.<p>
 *
 * The LoggerAdapter is initially set at creation, however it
 * can be hot-swapped at any time.
 *
 * @see LoggerAdapter
 * @see LoggerManager
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class Logger
{
  // Constants -----------------------------------------------------

  /**
   * No logging
   */
  public static final int NONE = Integer.MAX_VALUE;

  /**
   * The fatal level
   */
  public static final int FATAL = 50000;

  /**
   * The error level
   */
  public static final int ERROR = 40000;

  /**
   * The warning level
   */
  public static final int WARN = 30000;

  /**
   * The informational level
   */
  public static final int INFO = 20000;

  /**
   * The debug level
   */
  public static final int DEBUG = 10000;

  /**
   * The trace level
   */
  public static final int TRACE = 9000;

  /**
   * All logging
   */
  public static final int ALL = Integer.MIN_VALUE;

  /**
   * The log manager
   */
  private static LoggerManager manager = LoggerManager.getLoggerManager();

  // Attributes ----------------------------------------------------

  /**
   * The adapter to which this logger delegates.
   */
  private LoggerAdapter adapter;

  // Static --------------------------------------------------------

  /**
   * Create a new logger given a name.<p>
   *
   * It is common that the name is hierarchical like java class names.
   * However this does not mean that the real logger will understand
   * or use the hierarchy, or even the name.<p>
   *
   * @param name the name to assign to the logger, cannot be null
   * @return the logger
   */
  public static final Logger getLogger(String name)
  {
    // The name is checked by LoggerManager
    return manager.getLogger(name);
  }

  /**
   * Create a new logger given a class.<p>
   *
   * This is a convenience method to create a logger using a class name.
   * This is very common usage.
   *
   * @param clazz the class, cannot be null
   */
  public static final Logger getLogger(Class clazz)
  {
    if (clazz == null)
      throw new IllegalArgumentException("Null class");
    return getLogger(clazz.getName());
  }

  // Constructors --------------------------------------------------

  /**
   * Create a new logger that delegates to the given adapter.
   *
   * @param adapter the real logger cannot be null
   */
  /*package*/ Logger(LoggerAdapter adapter)
  {
    if (adapter == null)
      throw new IllegalArgumentException("Null adapter");
    this.adapter = adapter;
  }

  // Public --------------------------------------------------------

  /**
   * Get the name of this logger. This may not be the name as that
   * used to create the logger.
   *
   * @return the name of the logger.
   */
  public String getName()
  {
    return adapter.getName();
  }

  /**
   * Get the logging level.
   *
   * @return the level.
   */
  public int getLevel()
  {
    return adapter.getLevel();
  }

  /**
   * Set the logging level.
   *
   * @param level the new level.
   * @exception SecurityException when not authorised
   */
  public void setLevel(int level)
    throws SecurityException
  {
    adapter.setLevel(level);
  }

  /**
   * Log a message.
   *
   * @param the level to log
   * @param message the message to log.
   */
  public void log(int level, String message)
  {
    adapter.log(level, message);
  }

  /**
   * Log a message with a throwable.
   *
   * @param the level to log
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void log(int level, String message, Throwable throwable)
  {
    adapter.log(level, message, throwable);
  }

  /**
   * Test whether a level is enabled.
   *
   * @param the level to test
   * @return true when enabled, false otherwise.
   */
  public boolean isEnabled(int level)
  {
    return adapter.isEnabled(level);
  }

  /**
   * Log a fatal message.
   *
   * @param message the message to log.
   */
  public void fatal(String message)
  {
    adapter.fatal(message);
  }

  /**
   * Log a fatal message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void fatal(String message, Throwable throwable)
  {
    adapter.fatal(message, throwable);
  }

  /**
   * Test whether fatal is enabled.
   *
   * @return true when fatal is enabled, false otherwise.
   */
  public boolean isFatalEnabled()
  {
    return adapter.isFatalEnabled();
  }

  /**
   * Log an error message.
   *
   * @param message the message to log.
   */
  public void error(String message)
  {
    adapter.error(message);
  }

  /**
   * Log an error message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void error(String message, Throwable throwable)
  {
    adapter.error(message, throwable);
  }

  /**
   * Test whether error is enabled.
   *
   * @return true when error is enabled, false otherwise.
   */
  public boolean isErrorEnabled()
  {
    return adapter.isErrorEnabled();
  }

  /**
   * Log a warning message.
   *
   * @param message the message to log.
   */
  public void warn(String message)
  {
    adapter.warn(message);
  }

  /**
   * Log a warning message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void warn(String message, Throwable throwable)
  {
    adapter.warn(message, throwable);
  }

  /**
   * Test whether warning is enabled.
   *
   * @return true when warning is enabled, false otherwise.
   */
  public boolean isWarnEnabled()
  {
    return adapter.isWarnEnabled();
  }

  /**
   * Log an informational message.
   *
   * @param message the message to log.
   */
  public void info(String message)
  {
    adapter.info(message);
  }

  /**
   * Log an informational message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void info(String message, Throwable throwable)
  {
    adapter.info(message, throwable);
  }

  /**
   * Test whether info is enabled.
   *
   * @return true when info is enabled, false otherwise.
   */
  public boolean isInfoEnabled()
  {
    return adapter.isInfoEnabled();
  }

  /**
   * Log a debug message.
   *
   * @param message the message to log.
   */
  public void debug(String message)
  {
    adapter.debug(message);
  }

  /**
   * Log a debug message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void debug(String message, Throwable throwable)
  {
    adapter.debug(message, throwable);
  }

  /**
   * Test whether debug is enabled.
   *
   * @return true when debug is enabled, false otherwise.
   */
  public boolean isDebugEnabled()
  {
    return adapter.isDebugEnabled();
  }

  /**
   * Log a trace message.
   *
   * @param message the message to log.
   */
  public void trace(String message)
  {
    adapter.trace(message);
  }

  /**
   * Log a trace message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void trace(String message, Throwable throwable)
  {
    adapter.trace(message, throwable);
  }

  /**
   * Test whether trace is enabled.
   *
   * @return true when trace is enabled, false otherwise.
   */
  public boolean isTraceEnabled()
  {
    return adapter.isTraceEnabled();
  }

  /**
   * Retrieve the real logger.<p>
   *
   * Use this with caution. The aim of this method is to allow the
   * real logger to be manipulated using methods not exposed by this
   * class. No guarantees can be made that the object returned is
   * useful.<p>
   *
   * For example if you are sure you are using log4j before 1.2, this
   * method should return a Category.
   *
   * @return an object that might be of the expected logging class.
   */
  public Object getUnderlyingLogger()
  {
    return adapter.getUnderlyingLogger();
  }

  // X Implementation ----------------------------------------------

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  /**
   * Get the adapter of this logger.
   *
   * @return the adapter.
   */
  LoggerAdapter getLoggerAdapter()
  {
    return adapter;
  }

  /**
   * Set the adapter of this logger.
   *
   * @param the new adapter cannot be null
   */
  void setLoggerAdapter(LoggerAdapter adapter)
  {
    if (adapter == null)
      throw new IllegalArgumentException("Null adapter");
    this.adapter = adapter;
  }

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
