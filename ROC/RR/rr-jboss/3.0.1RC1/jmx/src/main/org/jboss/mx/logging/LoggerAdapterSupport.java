/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * This class helps to implement the LoggerAdapter interface.<p>
 *
 * Only a small number of methods need implementing.<p>
 *
 * WARNING: The implemented methods are declared final for speed.
 *
 * @see LoggerAdapter
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class LoggerAdapterSupport
  implements LoggerAdapter
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  // LoggerAdapter Implementation ----------------------------------

  public final void fatal(String message)
  {
    log(Logger.FATAL, message);
  }

  public final void fatal(String message, Throwable throwable)
  {
    log(Logger.FATAL, message, throwable);
  }

  public final boolean isFatalEnabled()
  {
    return isEnabled(Logger.FATAL);
  }

  public final void error(String message)
  {
    log(Logger.ERROR, message);
  }

  public final void error(String message, Throwable throwable)
  {
    log(Logger.ERROR, message, throwable);
  }

  public final boolean isErrorEnabled()
  {
    return isEnabled(Logger.ERROR);
  }

  public final void warn(String message)
  {
    log(Logger.WARN, message);
  }

  public final void warn(String message, Throwable throwable)
  {
    log(Logger.WARN, message, throwable);
  }

  public final boolean isWarnEnabled()
  {
    return isEnabled(Logger.WARN);
  }

  public final void info(String message)
  {
    log(Logger.INFO, message);
  }

  public final void info(String message, Throwable throwable)
  {
    log(Logger.INFO, message, throwable);
  }

  public final boolean isInfoEnabled()
  {
    return isEnabled(Logger.INFO);
  }

  public final void debug(String message)
  {
    log(Logger.DEBUG, message);
  }

  public final void debug(String message, Throwable throwable)
  {
    log(Logger.DEBUG, message, throwable);
  }

  public final boolean isDebugEnabled()
  {
    return isEnabled(Logger.DEBUG);
  }

  public final void trace(String message)
  {
    log(Logger.TRACE, message);
  }

  public final void trace(String message, Throwable throwable)
  {
    log(Logger.TRACE, message, throwable);
  }

  public final boolean isTraceEnabled()
  {
    return isEnabled(Logger.TRACE);
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
