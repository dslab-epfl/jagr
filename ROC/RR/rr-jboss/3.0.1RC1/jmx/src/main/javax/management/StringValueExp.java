/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * A String that is an arguement to a query.
 *
 * <p><b>Revisions:</b>
 * <p><b>20020317 Adrian Brock:</b>
 * <ul>
 * <li>Make queries thread safe
 * </ul>
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class StringValueExp
   extends ValueExpSupport
{
   // Constants ---------------------------------------------------

   // Attributes --------------------------------------------------

   /**
    * The value of the string
    */
   private String value;

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct a string value expression for the null string.
    */
   public StringValueExp()
   {
   }

   /**
    * Construct a string value expression for the passed string
    *
    * @param value the string
    */
   public StringValueExp(String value)
   {
      this.value = value;
   }

   // Public ------------------------------------------------------

   /**
    * Get the value of the string.
    *
    * @return the string value
    */
   public String getString()
   {
      return value;
   }

   // ValueExpSupport Overrides -----------------------------------

   // Object overrides --------------------------------------------

   public String toString()
   {
      return value;
   }

   // Protected ---------------------------------------------------

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   // Inner Classes -----------------------------------------------
}
