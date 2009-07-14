/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging;

/**
 * The null logger adapter.<p>
 *
 * This does nothing
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class NullLoggerAdapter
  extends LoggerAdapterSupport
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  // LoggerAdapter Implementation ----------------------------------

  public final String getName()
  {
    return "Null";
  }

  public final int getLevel()
  {
    return Logger.NONE;
  }

  public final void setLevel(int level)
  {
  }

  public final void log(int level, String message)
  {
  }

  public final void log(int level, String message, Throwable throwable)
  {
  }

  public final boolean isEnabled(int level)
  {
    return false;
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

  // Inner classes -------------------------------------------------
}
