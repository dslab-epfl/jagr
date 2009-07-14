/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;


/**
 * Thrown when a java.lang.error occurs.
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
public class RuntimeErrorException
   extends JMRuntimeException
{

   // Attributes ----------------------------------------------------

   /**
    * The wrapped error.
    */
   private Error e = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Construct a new RuntimeErrorException from a given error.
    *
    * @param e the error to wrap.
    */
   public RuntimeErrorException(Error e)
   {
      super();
      this.e = e;
   }

   /**
    * Construct a new RuntimeErrorException from a given error and message.
    *
    * @param e the error to wrap.
    * @param message the specified message.
    */
   public RuntimeErrorException(Error e, String message)
   {
      super(message);
      this.e = e;
   }

   // Public --------------------------------------------------------

   /**
    * Retrieves the wrapped error.
    *
    * @return the wrapped error.
    */
   public java.lang.Error getTargetError()
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
      return "RuntimeErrorException: " + getMessage() + ((e == null) ? "" : "\nCause: " + e.toString());
   }

   // Private -------------------------------------------------------
}
