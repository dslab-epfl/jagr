/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.modelmbean;

/**
 * Thrown when unrecognizable target object type is set to a Model MBean
 * instance
 *
 * @see javax.management.modelmbean.ModelMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class InvalidTargetObjectTypeException
         extends Exception
{
   // Attributes ----------------------------------------------------
   private Exception e = null;

   // Constructors --------------------------------------------------
   public InvalidTargetObjectTypeException()
   {
      super();
   }
   
   public InvalidTargetObjectTypeException(String s)
   {
      super(s);
   }
   
   public InvalidTargetObjectTypeException(Exception e, String s)
   {
      this(s);
      this.e = e;
   }

   // Throwable overrides -------------------------------------------
   public String getMessage()
   {
      return super.getMessage();
   }

   // Object overrides ----------------------------------------------
   public String toString()
   {
      return super.toString();
   }

}




