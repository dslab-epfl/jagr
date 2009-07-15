/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * Describes an operation exposed by an MBean
 *
 * This implementation protects its immutability by taking shallow clones of all arrays
 * supplied in constructors and by returning shallow array clones in getXXX() methods.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 *
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanOperationInfo extends MBeanFeatureInfo
   implements Serializable, Cloneable
{

   // Constants -----------------------------------------------------
   
   /**
    * Management operation impact: INFO. The operation should not alter the
    * state of the MBean component (read operation).
    */
   public static final int INFO        = 0;
   
   /**
    * Management operation impact: ACTION. The operation changes the state
    * of the MBean component (write operation).
    */
   public static final int ACTION      = 1;
   
   /**
    * Management operation impact: ACTION_INFO. Operation behaves like a 
    * read/write operation.
    */
   public static final int ACTION_INFO = 2;
   
   /**
    * Management operation impact: UNKNOWN. Reserved for Standard MBeans.
    */
   public static final int UNKNOWN     = 3;

   
   // Attributes ----------------------------------------------------
   
   /**
    * Impact of this operation.
    */
   private int impact = UNKNOWN;
   
   /**
    * Signature of this operation.
    */
   private MBeanParameterInfo[] signature = null;
   
   /**
    * Return type of this operation as a fully qualified class name.
    */
   private String returnType = null;

   // Constructors --------------------------------------------------
   
   /**
    * Constructs management operation metadata from a given <tt>Method</tt>
    * object and description.
    *
    * @param   description human readable description of this operation
    * @param   method   used for build the metadata for the management operation
    */
   public MBeanOperationInfo(String description, Method method)
   {
      super(method.getName(), description);
      this.returnType = method.getReturnType().getName();

      Class[] sign = method.getParameterTypes();
      signature = new MBeanParameterInfo[sign.length];

      for (int i = 0; i < sign.length; ++i)
      {
         String name = sign[i].getName();
         signature[i] = new MBeanParameterInfo(name, name, "MBean Operation Parameter.");
      }
   }

   /**
    * Constructs a management operation metadata.
    *
    * @param   name  name of the management operation
    * @param   description human readable description string of the operation
    * @param   signature   signature of the operation
    * @param   returnType  return type of the operation as a fully qualified class name
    * @param   impact impact of this operation: {@link #ACTION ACTION}, {@link #INFO INFO}, {@link #ACTION_INFO ACTION_INFO} or {@link #UNKNOWN UNKNOWN}
    */
   public MBeanOperationInfo(String name, String description,
                             MBeanParameterInfo[] signature,
                             String returnType, int impact)
   {
      super(name, description);
      
      this.signature = (null == signature) ? new MBeanParameterInfo[0] : (MBeanParameterInfo[]) signature.clone();
      this.returnType = returnType;
      this.impact = impact;
   }

   // Public --------------------------------------------------------
   
   /**
    * Returns a fully qualified class name of the return type of this operation.
    *
    * @return  fully qualified class name
    */
   public String getReturnType()
   {
      return returnType;
   }

   /**
    * Returns the signature of this operation. <b>Note:</b> an operation with a void
    * signature returns a zero-length array, not a <tt>null</tt> reference.
    *
    * @return  operation's signature
    */
   public MBeanParameterInfo[] getSignature()
   {
      return (MBeanParameterInfo[]) signature.clone();
   }

   /**
    * Returns the impact of this operation. The impact is one of the following values:
    * {@link #ACTION ACTION}, {@link #INFO INFO}, {@link #ACTION_INFO ACTION_INFO} or {@link #UNKNOWN UNKNOWN} (reserved for Standard MBeans).
    *
    * @return  operation's impact
    * @throws  JMRuntimeException if the object contains an invalid impact value
    */
   public int getImpact()
   {
      if (impact != ACTION && impact != INFO && impact != ACTION_INFO && impact != UNKNOWN)
         throw new JMRuntimeException("Invalid impact value (" + impact + "). The impact must be either ACTION, INFO, ACTION_INFO or UNKNOWN.");
         
      return impact;
   }

   // Cloneable implementation --------------------------------------
   /**
    * Creates a copy of this object. This is a deep copy; the <tt>MBeanParameterInfo</tt> objects
    * forming the operation's signature are also cloned.
    *
    * @return  a clone of this object
    * @throws CloneNotSupportedException if there was a failure trying to copy the object
    */
   public synchronized Object clone() throws CloneNotSupportedException
   {
      MBeanOperationInfo clone = (MBeanOperationInfo) super.clone();
      clone.signature = (MBeanParameterInfo[])this.signature.clone();
      clone.returnType = this.returnType;
      clone.impact = this.impact;

      return clone;
   }
   
}
