/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.server.registry;

import javax.management.DynamicMBean;
import javax.management.ObjectName;
import javax.management.MBeanRegistration;

/**
 * info@todo this docs
 *
 * @see org.jboss.mx.server.registry.MBeanRegistry
 * @see org.jboss.mx.server.MBeanServerImpl
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanEntry
{
   // Attributes ----------------------------------------------------

   /**
    * The registered object name of the mbean
    */
   private ObjectName objectName = null;

   /**
    * The class name of the mbean
    */
   private String resourceClassName = null;

   /**
    * The object used to invoke the mbean
    */
   private DynamicMBean invoker  = null;

   /**
    * The mbean registered
    */
   private Object resource  = null;

   /**
    * The context classloader of the mbean
    */
   private ClassLoader cl  = null;

   // Constructors --------------------------------------------------

   /**
    * Construct a new mbean registration entry.
    *
    * @param objectName the name with which the mbean is registered
    * @param invoker the dynamic mbean used to invoke the mbean
    * @param object the mbean
    * @param cl the thread context classloader with which to invoke the mbean
    */
   public MBeanEntry(ObjectName objectName, DynamicMBean invoker, 
                     Object resource, ClassLoader cl)
   {
      this.objectName = objectName;
      this.invoker = invoker;
      this.resourceClassName = resource.getClass().getName();
      this.resource = resource;
      this.cl = cl;
   }

   // Public --------------------------------------------------------

   /**
    * Retrieve the object name with the mbean is registered.
    *
    * @return the object name
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }

   /**
    * Retrieve the invoker for the mbean.
    *
    * @return the invoker
    */
   public DynamicMBean getMBean()
   {
      return invoker;
   }

   /**
    * Retrieve the class name for the mbean.
    *
    * @return the class name
    */
   public String getResourceClassName()
   {
      return resourceClassName;
   }

   /**
    * Retrieve the mbean.
    *
    * @return the mbean
    */
   public Object getResourceInstance()
   {
      return resource;
   }

   /**
    * Retrieve the context class loader with which to invoke the mbean.
    *
    * @return the class loader
    */
   public ClassLoader getClassLoader()
   {
      return cl;
   }
}
