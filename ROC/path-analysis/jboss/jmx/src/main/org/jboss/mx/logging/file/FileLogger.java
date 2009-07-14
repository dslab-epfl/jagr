/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.logging.file;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.security.AccessController;

import org.jboss.mx.logging.Logger;
import org.jboss.mx.logging.LoggerAdapterSupport;
import org.jboss.mx.logging.LoggerPermission;

/**
 * A file logger, the name is the fully qualifed file name.
 *
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public final class FileLogger
  extends LoggerAdapterSupport
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------

  /**
   * The logging level
   */
  private int level = Logger.ALL;

  /**
   * The file name
   */
  private String fileName;

  /**
   * The print stream for the file
   */
  private PrintStream output;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Construct a new FileLogger from the given file name
   * 
   * @param name the file name
   */
  public FileLogger(String name)
    throws FileNotFoundException
  {
    this(name, true);
  }

  /**
   * Construct a new FileLogger from the given file name
   * 
   * @param name the file name
   * @param append pass true to append to an existing file, false to
   *        create a new file
   */
  public FileLogger(String name, boolean append)
    throws FileNotFoundException
  {
    fileName = name;
    output = new PrintStream(new FileOutputStream(name, append), true);
  }

  // LoggerAdapter Implementation ----------------------------------

  public final String getName()
  {
    return fileName;
  }

  public final int getLevel()
  {
    return level;
  }

  public final void setLevel(int level)
    throws SecurityException
  {
    //TODO AccessController.checkPermission(new LoggerPermission("control"));
    this.level = level;
  }

  public final void log(int level, String message)
  {
    if (isEnabled(level) == false)
      return;
    doLog(level, message, null);
  }

  public final void log(int level, String message, Throwable throwable)
  {
    if (isEnabled(level) == false)
      return;
    doLog(level, message, throwable);
  }

  public final boolean isEnabled(int level)
  {
    return level <= this.level;
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

  /**
   * Perform the actual logging to system error.
   *
   * @param level the logging level
   * @param message the message
   * @param throwable an error
   */
  private final void doLog(int level, String message, Throwable throwable)
  {
    synchronized(output)
    {
      if (level == Logger.FATAL)
        output.print("FATAL ");
      if (level == Logger.ERROR)
        output.print("ERROR ");
      if (level == Logger.WARN)
        output.print("WARN  ");
      if (level == Logger.INFO)
        output.print("INFO  ");
      if (level == Logger.DEBUG)
        output.print("DEBUG ");
      if (level == Logger.TRACE)
        output.print("TRACE ");
      output.println(message);
      if (throwable != null)
        throwable.printStackTrace(output);
    }
  }

  // Inner classes -------------------------------------------------
}
