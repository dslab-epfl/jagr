/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging.log4j;

import org.jboss.mx.logging.LoggerAdapter;
import org.jboss.mx.logging.LoggerAdapterFactorySupport;

/**
 * Creates adapters for log4j categories.
 *
 * @see Log4jAdapter
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class Log4jAdapterFactory
  extends LoggerAdapterFactorySupport
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  // Static --------------------------------------------------------

  public static final TracePriority dummy = TracePriority.TRACE;

  // Constructors --------------------------------------------------

  // LoggerAdapterFactory Implementation --------------------------

  public final LoggerAdapter getLoggerAdapter(String name)
  {
    return new Log4jAdapter(name);
  }

  public final Class getLoggerAdapterClass()
  {
    return Log4jAdapter.class;
  }

  public final void setConfiguration(Object configurator)
    throws SecurityException
  {
//TODO
  }

  public final void resetConfiguration()
    throws SecurityException
  {
//TODO
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
