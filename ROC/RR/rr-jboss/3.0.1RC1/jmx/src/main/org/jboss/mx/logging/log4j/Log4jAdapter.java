/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging.log4j;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

import org.jboss.mx.logging.LoggerAdapter;

/**
 * An adapter for the log4j Category.<p>
 *
 * Note: the level is interpreted as a log4j priority.
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class Log4jAdapter
  implements LoggerAdapter
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  /**
   * The log4j category.
   */
  Category category;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Construct a new log4j adapter, the name is used to contruct the category.
   * 
   * @param name the log4j category name
   */
  public Log4jAdapter(String name)
  {
    category = Category.getInstance(name);
  }

  // LoggerAdapter Implementation ----------------------------------

  public final String getName()
  {
    return category.getName();
  }

  public final int getLevel()
  {
    return category.getPriority().toInt();
  }

  public final void setLevel(int level)
    throws SecurityException
  {
    category.setPriority(Priority.toPriority(level));
  }

  public final void log(int level, String message)
  {
    category.log(Priority.toPriority(level), message);
  }

  public final void log(int level, String message, Throwable throwable)
  {
    category.log(Priority.toPriority(level), message);
  }

  public final boolean isEnabled(int level)
  {
    Priority p = Priority.toPriority(level);
    if (category.isEnabledFor(p) == false)
      return false;
    return p.isGreaterOrEqual(category.getChainedPriority());
  }

  public final void fatal(String message)
  {
    category.fatal(message);
  }

  public final void fatal(String message, Throwable throwable)
  {
    category.fatal(message, throwable);
  }

  public final boolean isFatalEnabled()
  {
    Priority p = Priority.FATAL;
    if (category.isEnabledFor(p) == false)
      return false;
    return p.isGreaterOrEqual(category.getChainedPriority());
  }

  public final void error(String message)
  {
    category.error(message);
  }

  public final void error(String message, Throwable throwable)
  {
    category.error(message, throwable);
  }

  public final boolean isErrorEnabled()
  {
    Priority p = Priority.ERROR;
    if (category.isEnabledFor(p) == false)
      return false;
    return p.isGreaterOrEqual(category.getChainedPriority());
  }

  public final void warn(String message)
  {
    category.warn(message);
  }

  public final void warn(String message, Throwable throwable)
  {
    category.warn(message, throwable);
  }

  public final boolean isWarnEnabled()
  {
    Priority p = Priority.WARN;
    if (category.isEnabledFor(p) == false)
      return false;
    return p.isGreaterOrEqual(category.getChainedPriority());
  }

  public final void info(String message)
  {
    category.info(message);
  }

  public final void info(String message, Throwable throwable)
  {
    category.info(message, throwable);
  }

  public final boolean isInfoEnabled()
  {
    Priority p = Priority.INFO;
    if (category.isEnabledFor(p) == false)
      return false;
    return p.isGreaterOrEqual(category.getChainedPriority());
  }

  public final void debug(String message)
  {
    category.debug(message);
  }

  public final void debug(String message, Throwable throwable)
  {
    category.debug(message, throwable);
  }

  public final boolean isDebugEnabled()
  {
    Priority p = Priority.DEBUG;
    if (category.isEnabledFor(p) == false)
      return false;
    return p.isGreaterOrEqual(category.getChainedPriority());
  }

  public final void trace(String message)
  {
    category.log(TracePriority.TRACE, message);
  }

  public final void trace(String message, Throwable throwable)
  {
    category.log(TracePriority.TRACE, message, throwable);
  }

  public final boolean isTraceEnabled()
  {
    Priority p = TracePriority.TRACE;
    if (category.isEnabledFor(p) == false)
      return false;
    return p.isGreaterOrEqual(category.getChainedPriority());
  }

  /**
   * Retrieve the log4j category
   * 
   * @return the category.
   */
  public Object getUnderlyingLogger()
  {
    return category;
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
