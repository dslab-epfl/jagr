/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.modelmbean;

/**
 * Exceptions related to XML handling.
 *
 * @see javax.management.modelmbean.DescriptorSupport
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class XMLParseException
         extends Exception
{

   // Attributes ----------------------------------------------------
   private Exception e = null;
   
   // Constructors --------------------------------------------------
   public XMLParseException()
   {
      super();
   }

   public XMLParseException(String s)
   {
      super(s);
   }

   public XMLParseException(Exception e, String s)
   {
      this(s);
      this.e = e;
   }


   // Throwable overrides -------------------------------------------
   public java.lang.String getMessage()
   {
      return super.getMessage();
   }

   // Object overrides ----------------------------------------------
   public String toString()
   {
      return super.toString();
   }
}




