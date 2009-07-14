/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * The default logger adapter factory.<p>
 *
 * This has singleton logger adapter that logs to System.err.
 *
 * @see DefaultLoggerAdapter
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class DefaultLoggerAdapterFactory
  extends LoggerAdapterFactorySupport
{
  // Constants -----------------------------------------------------

  public static final DefaultLoggerAdapter adapter = new DefaultLoggerAdapter();

  // Attributes ----------------------------------------------------

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  // LoggerAdapterFactory Implementation --------------------------

  public final LoggerAdapter getLoggerAdapter(String name)
  {
    return adapter;
  }

  public final Class getLoggerAdapterClass()
  {
    return adapter.getClass();
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
