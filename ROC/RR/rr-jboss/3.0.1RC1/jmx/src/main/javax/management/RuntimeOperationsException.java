/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * Wraps runtime exceptions thrown when performing operations on MBeans.
 *
 * @author <a href="mailto:juha@jboss.org>Juha Lindfors</a>
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
public class RuntimeOperationsException
   extends JMRuntimeException
{
   // Attributes ----------------------------------------------------

   /**
    * The wrapped runtime exception.
    */
   private RuntimeException e = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new RuntimeOperationsException from a given runtime exception.
    *
    * @param e the runtime exception to wrap.
    */
   public RuntimeOperationsException(RuntimeException e)
   {
      super();
      this.e = e;
   }

   /**
    * Construct a new RuntimeOperationsException from a given runtime exception
    * and message.
    *
    * @param e the runtime exception to wrap.
    * @param message the specified message.
    */
   public RuntimeOperationsException(RuntimeException e, String message)
   {
      super(message);
      this.e = e;
   }

   // Public --------------------------------------------------------

   /**
    * Retrieves the wrapped runtime exception.
    *
    * @return the wrapped runtime exception.
    */
   public RuntimeException getTargetException()
   {
      return e;
   }

   // JMRuntimeException overrides ----------------------------------
   /**
    * Returns a string representation of this exception. The returned string
    * contains this exception name, message and a string representation of the
    * target exception if it has been set.
    *
    * @return string representation of this exception
    */
   public String toString()
   {
      return "RuntimeOperationsException: " + getMessage() + ((e == null) ? "" : "\nCause: " + e.toString());
   }

   // Private -------------------------------------------------------
}

