/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * The interface is implemented to create adapters for each
 * logging implementation.
 *
 * @see LoggerAdapter
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public interface LoggerAdapterFactory
{
  // Constants -----------------------------------------------------

  // Public --------------------------------------------------------

  /**
   * Create a new logger adapter given a name.
   *
   * @return the adapter
   */
  public LoggerAdapter getLoggerAdapter(String name);

  /**
   * Retrieve the class of the adapter.
   * This is used to check an adapter is up-to-date during hot-swapping.<p>
   *
   * The LoggerAdapterFactorySupport class helps to implement this
   * this interface.
   *
   * @return the class of the adapter.
   */
  public Class getLoggerAdapterClass();

  /**
   * Retrieve a context object for this factory.<p>
   *
   * Null should be returned when there is no context.
   *
   * @return the context object
   */
  public Object getContext();

  /**
   * Set the configurator.<p>
   *
   * @param configurator the object used to configure the context
   * @exception SecurityException when not authorised
   */
  public void setConfiguration(Object configurator)
    throws SecurityException;

  /**
   * Reset the configuration to the default.
   *
   * @exception SecurityException when not authorised
   */
  public void resetConfiguration()
    throws SecurityException;

  // Inner classes -------------------------------------------------
}
