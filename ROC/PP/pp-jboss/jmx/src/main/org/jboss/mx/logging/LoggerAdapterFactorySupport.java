/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * This class helps to implement the LoggerAdapterFactory interface.<p>
 *
 * @see LoggerAdapterFactory
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class LoggerAdapterFactorySupport
  implements LoggerAdapterFactory
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  /**
   * The context object
   */
  private Object context;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Constructor with no context object
   */
  public LoggerAdapterFactorySupport()
  {
    this(null);
  }

  /**
   * Constructor with the given context object
   *
   * @param context the context object
   */
  public LoggerAdapterFactorySupport(Object context)
  {
    this.context = context;
  }

  // LoggerAdapterFactory Implementation ---------------------------

  public Object getContext()
  {
    return context;
  }

  /**
   * This operation is quietly ignored.
   */
  public void setConfiguration(Object configurator)
    throws SecurityException
  {
  }

  /**
   * This operation is quietly ignored.
   */
  public void resetConfiguration()
    throws SecurityException
  {
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
