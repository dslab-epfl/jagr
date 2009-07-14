/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.server.registry;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * A managed version of the MBean registry. It registers itself as an
 * mbean, exposing methods and attributes for management.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class ManagedMBeanRegistry
   extends BasicMBeanRegistry
{
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Constructs a new BasicMBeanRegistry with itself already
    * registered.<p>
    *
    * @param the mbean server for which we are the registry
    * @param the default domain of this registry.
    */
   public ManagedMBeanRegistry(MBeanServer server, String defaultDomain)
   {
      super(server, defaultDomain);

      final boolean READABLE = true;
      final boolean WRITABLE = true;
      final boolean BOOLEAN = true;

      try
      {
         // Default Domain attribute
         DescriptorSupport descDefaultDomain = new DescriptorSupport();
         descDefaultDomain.setField("name", "DefaultDomain");
         descDefaultDomain.setField("descriptorType", "attribute");
         descDefaultDomain.setField("displayName", "Default Domain");
         descDefaultDomain.setField("default", defaultDomain);
         descDefaultDomain.setField("currencyTimeLimit", "-1");
         ModelMBeanAttributeInfo defaultDomainInfo = 
         new ModelMBeanAttributeInfo
         (
            "DefaultDomain", String.class.getName(),
            "The domain to use when an object name has no domain",
            READABLE, !WRITABLE, !BOOLEAN,
            descDefaultDomain
         );

         // Size attribute
         DescriptorSupport descSize = new DescriptorSupport();
         descSize.setField("name", "Size");
         descSize.setField("descriptorType", "attribute");
         descSize.setField("displayName", "Size");
         descSize.setField("getMethod", "getSize");
         ModelMBeanAttributeInfo sizeInfo = 
         new ModelMBeanAttributeInfo
         (
            "Size", Integer.TYPE.getName(),
            "The number of MBeans registered in the MBean Server",
            READABLE, !WRITABLE, !BOOLEAN,
            descSize
         );

         // registerMBean operation
         DescriptorSupport descRegisterMBean = new DescriptorSupport();
         descRegisterMBean.setField("name", "registerMBean");
         descRegisterMBean.setField("descriptorType", "operation");
         descRegisterMBean.setField("role", "operation");
         MBeanParameterInfo[] registerMBeanParms =
         new MBeanParameterInfo[]
         {
             new MBeanParameterInfo
             (
                "Resource", 
                Object.class.getName(),
                "A compliant MBean to be registered in the MBean Server"
             ),
             new MBeanParameterInfo
             (
                "ObjectName",
                ObjectName.class.getName(),
                "The object name of the MBean"
             ),
             new MBeanParameterInfo
             (
                "ClassLoader",
                ClassLoader.class.getName(),
                "The context classloader for the MBean"
             ),
             new MBeanParameterInfo
             (
                "Magic",
                Object.class.getName(),
                "A magic token used to register into the JMImplementation Domain"
             )
         };
         ModelMBeanOperationInfo registerMBeanInfo = 
         new ModelMBeanOperationInfo
         (
            "registerMBean",
            "Adds an MBean in the MBeanServer",
            registerMBeanParms,
            ObjectInstance.class.getName(),
            ModelMBeanOperationInfo.ACTION_INFO,
            descRegisterMBean
         );

         // unregisterMBean operation
         DescriptorSupport descUnregisterMBean = new DescriptorSupport();
         descUnregisterMBean.setField("name", "unregisterMBean");
         descUnregisterMBean.setField("descriptorType", "operation");
         descUnregisterMBean.setField("role", "operation");
         MBeanParameterInfo[] unregisterMBeanParms =
         new MBeanParameterInfo[]
         {
             new MBeanParameterInfo
             (
                "ObjectName",
                ObjectName.class.getName(),
                "The object name of the MBean to remove"
             )
         };
         ModelMBeanOperationInfo unregisterMBeanInfo = 
         new ModelMBeanOperationInfo
         (
            "unregisterMBean",
            "Removes an MBean from the MBeanServer",
            unregisterMBeanParms,
            Void.TYPE.getName(),
            ModelMBeanOperationInfo.ACTION,
            descUnregisterMBean
         );

         // getSize operation
         DescriptorSupport descGetSize = new DescriptorSupport();
         descGetSize.setField("name", "getSize");
         descGetSize.setField("descriptorType", "operation");
         descGetSize.setField("role", "operation");
         MBeanParameterInfo[] getSizeParms = new MBeanParameterInfo[0];
         ModelMBeanOperationInfo getSizeInfo = 
         new ModelMBeanOperationInfo
         (
            "getSize",
            "Gets the number of MBeans registered",
            getSizeParms,
            Integer.TYPE.getName(),
            ModelMBeanOperationInfo.INFO,
            descGetSize
         );

         // Construct the modelmbean
         DescriptorSupport descMBean = new DescriptorSupport();
         descMBean.setField("name", "MBeanRegistry");
         descMBean.setField("descriptorType", "MBean");
         ModelMBeanInfoSupport info = new ModelMBeanInfoSupport
         (
            RequiredModelMBean.class.getName(),
            "Managed Bean Registry",
            new ModelMBeanAttributeInfo[]
            {
               defaultDomainInfo,
               sizeInfo
            },
            (ModelMBeanConstructorInfo[]) null,
            new ModelMBeanOperationInfo[]
            {
               registerMBeanInfo,
               unregisterMBeanInfo,
               getSizeInfo
            },
            (ModelMBeanNotificationInfo[]) null,
            descMBean
         );
         RequiredModelMBean registry = new RequiredModelMBean();
         registry.setModelMBeanInfo(info);
         registry.setManagedResource(this, "ObjectReference");

         // Register it
         registerMBean(registry, new ObjectName(MBEAN_REGISTRY), null, 
                       JMI_DOMAIN);
      }
      catch (MalformedObjectNameException e)
      {
         throw new Error("Cannot create MBean Registry ObjectName.");
      }
      catch (InvalidTargetObjectTypeException e)
      {
         throw new Error("Registry cannot be managed: " + e.toString());
      }
      catch (InstanceAlreadyExistsException e)
      {
         throw new Error("Cannot register the MBean Registry.");
      }
      catch (InstanceNotFoundException e)
      {
         throw new Error("Registry cannot be managed: " + e.toString());
      }
      catch (MBeanException e)
      {
         throw new Error("Cannot create MBean Registry." + e.getTargetException().toString());
      }
      catch (NotCompliantMBeanException e)
      {
         throw new Error("MBean Registry is not a compliant MBean");
      }
   }

   // Public --------------------------------------------------------

   // X Implementation ----------------------------------------------

   // Y Overrides ---------------------------------------------------

   // Protected -----------------------------------------------------

   // Package -------------------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}

