/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging.file;

import java.io.FileNotFoundException;

import org.jboss.mx.logging.LoggerAdapter;
import org.jboss.mx.logging.LoggerAdapterFactorySupport;

/**
 * A file logger factory. The logger name is used as the file name.
 *
 * @see FileLogger
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class FileLoggerFactory
  extends LoggerAdapterFactorySupport
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  // LoggerAdapterFactory Implementation --------------------------

  public final LoggerAdapter getLoggerAdapter(String name)
  {
    try
    {
      return new FileLogger(name);
    }
    catch (FileNotFoundException nfe)
    {
      throw new RuntimeException(nfe.toString());
    }
  }

  public final Class getLoggerAdapterClass()
  {
    return FileLogger.class;
  }

  // Y Overrides ---------------------------------------------------

  // Protected -----------------------------------------------------

  // Package Private -----------------------------------------------

  // Private -------------------------------------------------------

  // Inner classes -------------------------------------------------
}
