/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * The default logger adapter.<p>
 *
 * This logs to System.err
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class DefaultLoggerAdapter
  extends LoggerAdapterSupport
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  /**
   * The default logging level
   */
  private int level = Logger.WARN;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  // LoggerAdapter Implementation ----------------------------------

  public final String getName()
  {
    return "Default";
  }

  public final int getLevel()
  {
    return level;
  }

  public final void setLevel(int level)
  {
    this.level = level;
  }

  public final void log(int level, String message)
  {
    if (isEnabled(level) == false)
      return;
    doLog(level, message, null);
  }

  public final void log(int level, String message, Throwable throwable)
  {
    if (isEnabled(level) == false)
      return;
    doLog(level, message, throwable);
  }

  public final boolean isEnabled(int level)
  {
    return level >= this.level;
  }

  /**
   * This is a logger implementation.
   * 
   * @return this logger adapter.
   */
  public Object getUnderlyingLogger()
  {
    return this;
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  /**
   * Perform the actual logging to system error.
   *
   * @param level the logging level
   * @param message the message
   * @param throwable an error
   */
  private static final void doLog(int level, String message, Throwable throwable)
  {
    synchronized(System.err)
    {
      if (level == Logger.FATAL)
        System.err.print("FATAL ");
      if (level == Logger.ERROR)
        System.err.print("ERROR ");
      if (level == Logger.WARN)
        System.err.print("WARN  ");
      if (level == Logger.INFO)
        System.err.print("INFO  ");
      if (level == Logger.DEBUG)
        System.err.print("DEBUG ");
      if (level == Logger.TRACE)
        System.err.print("TRACE ");
      System.err.println(message);
      if (throwable != null)
        throwable.printStackTrace(System.err);
    }
  }

  // Inner classes -------------------------------------------------
}
