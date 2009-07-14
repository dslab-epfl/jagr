/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * Describes an argument of an operation exposed by an MBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 *
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanParameterInfo extends MBeanFeatureInfo
   implements java.io.Serializable, Cloneable
{
   // Attributes ----------------------------------------------------
   protected String type = null;

   // Constructors --------------------------------------------------
   public MBeanParameterInfo(java.lang.String name,
                             java.lang.String type,
                             java.lang.String description)
   {
      super(name, description);
      this.type = type;
   }

   // Public --------------------------------------------------------
   public java.lang.String getType()
   {
      return type;
   }

   // Cloneable implementation --------------------------------------
   public java.lang.Object clone() throws CloneNotSupportedException
   {
      MBeanParameterInfo clone = (MBeanParameterInfo) super.clone();
      clone.type = getType();
      return clone;
   }
}
