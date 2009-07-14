/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * A factory for the null logger.<p>
 *
 * This has singleton logger that does nothing.
 *
 * @see NullLoggerAdapter
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class NullLoggerAdapterFactory
  extends LoggerAdapterFactorySupport
{
  // Constants -----------------------------------------------------

  public static final NullLoggerAdapter adapter = new NullLoggerAdapter();

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
