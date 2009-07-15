/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

import java.security.BasicPermission;

/**
 * The logging permission.<p>
 *
 * There is only one logging permission "control" that is required to
 * to setLevel() on a Logger, setContextSelector() in the LogManager
 * and setLoggerAdapterFactory() for a LoggerContext.
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class LoggerPermission
  extends BasicPermission
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Contruct a logging permission with the given name.
   *
   * @param name the name of the permission
   */
  public LoggerPermission(String name)
    throws IllegalArgumentException
  {
    super(name);
  }

  // X Implementation ----------------------------------------------

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
