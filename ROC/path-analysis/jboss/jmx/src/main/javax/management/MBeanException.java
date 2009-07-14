/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A wrapper for exceptions thrown by MBeans.
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020313 Juha Lindfors:</b>
 * <ul>
 * <li> Overriding toString() to print out the root exception </li>
 * </ul>
 */
public class MBeanException
   extends JMException
{
   // Attributes ----------------------------------------------------

   /**
    * The wrapped exception.
    */
   private Exception e = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new MBeanException from a given exception.
    *
    * @param e the exception to wrap.
    */
   public MBeanException(Exception e)
   {
      super();
      this.e = e;
   }

   /**
    * Construct a new MBeanException from a given exception and message.
    *
    * @param e the exception to wrap.
    * @param message the specified message.
    */
   public MBeanException(Exception e, String message)
   {
      super(message);
      this.e = e;
   }

   // Public --------------------------------------------------------

   /**
    * Retrieves the wrapped exception.
    *
    * @return the wrapped exception.
    */
   public Exception getTargetException()
   {
     return e;
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print stream.
    *
    * @param stream  Stream to print to.
    */
   public void printStackTrace(final PrintStream stream)
   {
      super.printStackTrace(stream);
      if( e != null )
      {
         stream.println(" + nested throwable: ");
         e.printStackTrace(stream);
      }
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print writer.
    *
    * @param writer  Writer to print to.
    */
   public void printStackTrace(final PrintWriter writer)
   {
      super.printStackTrace(writer);
      if( e != null )
      {
         writer.println(" + nested throwable: ");
         e.printStackTrace(writer);
      }
   }

   // JMException overrides -----------------------------------------
   /**
    * Returns a string representation of this exception. The returned string
    * contains this exception name, message and a string representation of the
    * target exception if it has been set.
    *
    * @return string representation of this exception
    */
   public String toString()
   {
      return "MBeanException: " + getMessage() + ((e == null) ? "" : "\nCause: " + e.toString());
   }

}

