/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * The interface is implemented to control logging contexts.<p>
 *
 * The method for determining the context is entirely at the implementation's
 * control.<p>
 *
 * The selector must be registered with the Logger Manager.
 *
 * @see LoggerManager
 * @see LoggerContext
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public interface LoggerContextSelector
{
  // Constants -----------------------------------------------------

  // Public --------------------------------------------------------

  /**
   * Associate a special logging context with a context object.
   *
   * @param context the context object cannot be null
   * @param loggerContext the logger context cannot be null
   */
  public void associate(Object context, LoggerContext loggerContext);

  /**
   * Remove an association of a special logging context with a context object.
   *
   * @param context the context object to remove cannot be null
   */
  public void remove(Object context);

  /**
   * Determine the logging context.
   *
   * @return the current logging context.
   */
  public LoggerContext getLoggerContext();

  // Inner classes -------------------------------------------------
}
