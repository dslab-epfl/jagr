/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

/**
 * Describes a constructor exposed by an MBean
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 *
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanConstructorInfo extends MBeanFeatureInfo
   implements java.io.Serializable, Cloneable
{

   // Attributes ----------------------------------------------------
   protected MBeanParameterInfo[] signature = null;

   // Constructors --------------------------------------------------
   public MBeanConstructorInfo(java.lang.String description,
                               java.lang.reflect.Constructor constructor)
   {
      super(constructor.getName(), description);

      Class[] sign = constructor.getParameterTypes();
      signature = new MBeanParameterInfo[sign.length];

      for (int i = 0; i < sign.length; ++i)
      {
         String name = sign[i].getName();
         signature[i] = new MBeanParameterInfo(name, name, "MBean Constructor Parameter.");
      }
   }

   public MBeanConstructorInfo(java.lang.String name,
                               java.lang.String description,
                               MBeanParameterInfo[] signature)
   {
      super(name, description);
      this.signature = (null == signature) ? new MBeanParameterInfo[0] : (MBeanParameterInfo[]) signature.clone();
   }

   // Public --------------------------------------------------------
   public MBeanParameterInfo[] getSignature()
   {
      return (MBeanParameterInfo[]) signature.clone();
   }

   // Cloneable implementation --------------------------------------
   public synchronized Object clone() throws CloneNotSupportedException
   {
      MBeanConstructorInfo clone = (MBeanConstructorInfo) super.clone();
      clone.signature = (MBeanParameterInfo[])this.signature.clone();
      
      return clone;
   }
}
