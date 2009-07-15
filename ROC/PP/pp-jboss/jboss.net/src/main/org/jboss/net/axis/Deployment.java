/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: Deployment.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.axis;

// axis config and utils
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDService;
import org.apache.axis.deployment.wsdd.WSDDException;
import org.apache.axis.deployment.wsdd.WSDDTypeMapping;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.ConfigurationException;
import org.apache.axis.encoding.TypeMappingRegistry;
import javax.xml.rpc.encoding.DeserializerFactory;

import org.w3c.dom.Element;

// JAXP
import javax.xml.rpc.namespace.QName;

// java utils
import java.util.Map;
import java.util.Iterator;

/**
 * Represents a wsdd deployment descriptor/registry with
 * special classloading features.
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * <li> jung, 06.04.2002: Introduced parameterizable typemappings. </li>
 * </ul>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @created 09.03.2002
 * @version $Revision: 1.1.1.1 $
 */

public class Deployment extends WSDDDeployment {

   //
   // Attributes
   //

   /** 
    * holds a map of service q-names to classloaders 
    * this map must be initialised lazily, since accesses are already done
    * in the super-constructor! Newbies!
    */
   protected Map service2ClassLoader;

	/** whether the type mapping has been registered */
	protected boolean tmrCreated=false;
	
   //
   // Constructors
   // 

   /**
    * Constructor for Deployment.
    * @param e root element of the deployment document
    * @throws WSDDException
    */
   public Deployment(Element e) throws WSDDException {
      super(e);
      Element[] elements = getChildElements(e, "typeMapping");
      for (int i = 0; i < elements.length; i++) {
         TypeMapping mapping = new TypeMapping(elements[i]);
         deployTypeMapping(mapping);
      }
   }

   //
   // protected helpers
   //

   /** lazily initialises the classloader map */
   protected synchronized Map getService2ClassLoader() {
      if (service2ClassLoader == null) {
         service2ClassLoader = new java.util.HashMap();
      }

      return service2ClassLoader;
   }

   /** installs the typemappings parameter table inside a deserializer */
   protected void equipTypeMappingWithOptions(TypeMapping typeMapping)
      throws ConfigurationException {
      DeserializerFactory dser =
         (
            (org.apache.axis.encoding.TypeMapping) getTypeMappingRegistry().getTypeMapping(
               typeMapping.getEncodingStyle())).getDeserializer(
            typeMapping.getQName());
      if (dser instanceof ParameterizableDeserializerFactory) {
         // Load up our params
         ((ParameterizableDeserializerFactory) dser).setOptions(
            typeMapping.getParametersTable());
      }
   }

   //
   // Public API
   //

   /**
     * Put a WSDDService into this deployment, replacing any other
     * WSDDService which might already be present with the same QName.
     *
     * @param service a WSDDHandler to insert in this deployment
     */
   public void deployService(WSDDService service) {
      service.deployToRegistry(this);
      getService2ClassLoader().put(
         service.getQName(),
         Thread.currentThread().getContextClassLoader());
   }

   /** deploy the information inside a given target */
   public void deployToRegistry(WSDDDeployment target)
      throws DeploymentException {
      super.deployToRegistry(target);
      if (target instanceof Deployment) {
         Map targetMap = ((Deployment) target).getService2ClassLoader();
         Iterator myEntries = getService2ClassLoader().entrySet().iterator();
         while (myEntries.hasNext()) {
            Map.Entry nextEntry = (Map.Entry) myEntries.next();
            targetMap.put(nextEntry.getKey(), nextEntry.getValue());
         }
      }
   }

   /**
    * retrieve the classloader that loaded the given service
    */
   public ClassLoader getClassLoader(QName serviceName) {
      return (ClassLoader) getService2ClassLoader().get(serviceName);
   }

   /** overwrite to equip with options */
   public void deployTypeMapping(WSDDTypeMapping typeMapping)
      throws WSDDException {
      super.deployTypeMapping(typeMapping);
      if (typeMapping instanceof TypeMapping) {
         try {
            equipTypeMappingWithOptions((TypeMapping) typeMapping);
         } catch (ConfigurationException e) {
            throw new WSDDException("Could not equip typemapping with options because of"+ e);
         }
      }
   }


   /** overwrite to equip with options */
   public TypeMappingRegistry getTypeMappingRegistry()
      throws ConfigurationException {
      TypeMappingRegistry tmr = super.getTypeMappingRegistry();
      if(!tmrCreated) {
         tmrCreated=true;
      WSDDTypeMapping[] typeMappings = (WSDDTypeMapping[]) getTypeMappings();
      for (int count = 0; count < typeMappings.length; count++) {
         if (typeMappings[count] instanceof TypeMapping) {
            equipTypeMappingWithOptions((TypeMapping) typeMappings[count]);
         }
      }
      }
      return tmr;
   }

}