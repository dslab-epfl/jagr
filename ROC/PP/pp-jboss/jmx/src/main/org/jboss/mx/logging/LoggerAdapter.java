/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * The interface implemented by logging implementations.<p>
 *
 * The adapter is either a logger or it can delegate or it can
 * delegate to a more complete logging package like log4j or
 * java.util.logging.<p>
 *
 * The LoggerAdapterSupport class can be used to help in implementing
 * this interface.
 *
 * @see Logger
 * @see LoggerManager
 * @see LoggerAdapterSupport
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public interface LoggerAdapter
{
  // Constants -----------------------------------------------------


  // Public --------------------------------------------------------

  /**
   * Get the name of this logger.
   *
   * @return the name of the logger.
   */
  public String getName();

  /**
   * Get the logging level.
   *
   * @return the level
   */
  public int getLevel();

  /**
   * Set the logging level.
   *
   * @param level the new level.
   * @exception SecurityException when not authorised
   */
  public void setLevel(int level)
    throws SecurityException;

  /**
   * Log a message.
   *
   * @param the level to log
   * @param message the message to log.
   */
  public void log(int level, String message);

  /**
   * Log a message with a throwable.
   *
   * @param the level to log
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void log(int level, String message, Throwable throwable);

  /**
   * Test whether a level is enabled.
   *
   * @param the level to test
   * @return true when enabled, false otherwise.
   */
  public boolean isEnabled(int level);

  /**
   * Log a fatal message.
   *
   * @param message the message to log.
   */
  public void fatal(String message);

  /**
   * Log a fatal message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void fatal(String message, Throwable throwable);

  /**
   * Test whether fatal is enabled.
   *
   * @return true when fatal is enabled, false otherwise.
   */
  public boolean isFatalEnabled();

  /**
   * Log an error message.
   *
   * @param message the message to log.
   */
  public void error(String message);

  /**
   * Log an error message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void error(String message, Throwable throwable);

  /**
   * Test whether error is enabled.
   *
   * @return true when error is enabled, false otherwise.
   */
  public boolean isErrorEnabled();

  /**
   * Log a warning message.
   *
   * @param message the message to log.
   */
  public void warn(String message);

  /**
   * Log a warning message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void warn(String message, Throwable throwable);

  /**
   * Test whether warning is enabled.
   *
   * @return true when warning is enabled, false otherwise.
   */
  public boolean isWarnEnabled();

  /**
   * Log an informational message.
   *
   * @param message the message to log.
   */
  public void info(String message);

  /**
   * Log an informational message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void info(String message, Throwable throwable);

  /**
   * Test whether info is enabled.
   *
   * @return true when info is enabled, false otherwise.
   */
  public boolean isInfoEnabled();

  /**
   * Log a debug message.
   *
   * @param message the message to log.
   */
  public void debug(String message);

  /**
   * Log a debug message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void debug(String message, Throwable throwable);

  /**
   * Test whether debug is enabled.
   *
   * @return true when debug is enabled, false otherwise.
   */
  public boolean isDebugEnabled();

  /**
   * Log a trace message.
   *
   * @param message the message to log.
   */
  public void trace(String message);

  /**
   * Log a trace message with a throwable.
   *
   * @param message the message to log.
   * @param throwable the throwable to log.
   */
  public void trace(String message, Throwable throwable);

  /**
   * Test whether trace is enabled.
   *
   * @return true when trace is enabled, false otherwise.
   */
  public boolean isTraceEnabled();

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
   * @return an object that might be of the expected class.
   */
  public Object getUnderlyingLogger();

  // Inner classes -------------------------------------------------
}
