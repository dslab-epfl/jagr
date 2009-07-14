
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.connectionmanager;



import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.logging.util.CategoryWriter;
import org.jboss.metadata.MetaData;
import org.jboss.naming.Util;
import org.jboss.system.Service;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.Classes;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The RARDeployment mbean manages instantiation and configuration of a ManagedConnectionFactory instance. It is intended to be configured primarily by xslt transformation of the ra.xml from a jca adapter. Until that is implemented, it uses the old RARDeployment and RARDeployer mechanism to obtain information from the ra.xml.  Properties for the ManagedConectionFactory should be supplied with their values in the ManagedConnectionFactoryProperties element.  A jndiname to bind the ConnectionFactory under must also be supplied.
 *
 *
 * Created: Fri Feb  8 13:44:31 2002
 *
 * @author <a href="toby.allsopp@peace.com">Toby Allsopp</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 * @jmx:mbean name="jboss.jca:service=RARDeployment"
 */

public class RARDeployment implements RARDeploymentMBean, ObjectFactory
{
   //for ObjectFactory implementation.
   private static final Map cfs = new HashMap();

   private Logger log = Logger.getLogger(getClass());

   //Hack to use previous ra.xml parsing code until xslt deployment is written.

   private ObjectName oldRarDeployment;

   private String displayName;
   private String vendorName;
   private String specVersion;
   private String eisType;
   private String version;
   private String managedConnectionFactoryClass;
   private String connectionFactoryInterface;
   private String connectionFactoryImplClass;
   private String connectionInterface;
   private String connectionImplClass;
   private String transactionSupport;
   private Element managedConnectionFactoryProperties;
   private String authenticationMechanismType;
   private String credentialInterface;
   private boolean reauthenticationSupport;

   private String jndiName;

   private Class mcfClass;
   private ManagedConnectionFactory mcf;

   /**
    * Default managed constructor for RARDeployment mbeans.
    *
    * @jmx.managed-constructor
    */
   public RARDeployment ()
   {
      
   }


   
   
   /**
    * The OldRarDeployment attribute refers to a previous-generation RARDeployment.
    * THIS IS A HACK UNTIL XSLT DEPLOYMENT IS WRITTEN
    * 
    * @return value of OldRarDeployment
    *
    * @jmx:managed-attribute
    * @todo remove this when xslt based deployment is written.
    */
   public ObjectName getOldRarDeployment()
   {
      return oldRarDeployment;
   }
   
   
   /**
    * Set the value of OldRarDeployment
    * @param OldRarDeployment  Value to assign to OldRarDeployment
    *
    * @jmx:managed-attribute
    * @todo remove this when xslt based deployment is written.
    */
   public void setOldRarDeployment(final ObjectName oldRarDeployment)
   {
      this.oldRarDeployment = oldRarDeployment;
   }
   
   


   /**
    * The DisplayName attribute holds the DisplayName from the ra.xml
    * It should be supplied by xslt from ra.xml
    *
    * @return the DisplayName value.
    * @jmx:managed-attribute
    */
   public String getDisplayName()
   {
      return displayName;
   }

   /**
    * Set the DisplayName value.
    * @param displayName The new DisplayName value.
    * @jmx:managed-attribute
    */
   public void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }


   /**
    * The VendorName attribute holds the VendorName from the ra.xml
    * It should be supplied by xslt from ra.xml
    *
    * @return the VendorName value.
    * @jmx:managed-attribute
    */
   public String getVendorName()
   {
      return vendorName;
   }

   /**
    * Set the VendorName value.
    * @param vendorName The new VendorName value.
    * @jmx:managed-attribute
    */
   public void setVendorName(String vendorName)
   {
      this.vendorName = vendorName;
   }


   /**
    * The SpecVersion attribute holds the SpecVersion from the ra.xml
    * It should be supplied by xslt from ra.xml
    *
    * @return the SpecVersion value.
    * @jmx:managed-attribute
    */
   public String getSpecVersion()
   {
      return specVersion;
   }

   /**
    * Set the SpecVersion value.
    * @param specVersion The new SpecVersion value.
    * @jmx:managed-attribute
    */
   public void setSpecVersion(String specVersion)
   {
      this.specVersion = specVersion;
   }


   /**
    * The EisType attribute holds the EisType from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the EisType value.
    * @jmx:managed-attribute
    */
   public String getEisType()
   {
      return eisType;
   }

   /**
    * Set the EisType value.
    * @param eisType The new EisType value.
    * @jmx:managed-attribute
    */
   public void setEisType(String eisType)
   {
      this.eisType = eisType;
   }


   /**
    * The Version attribute holds the Version from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the Version value.
    * @jmx:managed-attribute
    */
   public String getVersion()
   {
      return version;
   }

   /**
    * Set the Version value.
    * @param version The new Version value.
    * @jmx:managed-attribute
    */
   public void setVersion(String version)
   {
      this.version = version;
   }


   /**
    * The ManagedConnectionFactoryClass attribute holds the ManagedConnectionFactoryClass from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the ManagedConnectionFactoryClass value.
     * @jmx:managed-attribute
   */
   public String getManagedConnectionFactoryClass()
   {
      return managedConnectionFactoryClass;
   }

   /**
    * Set the ManagedConnectionFactoryClass value.
    * @param managedConnectionFactoryClass The new ManagedConnectionFactoryClass value.
    * @jmx:managed-attribute
    */
   public void setManagedConnectionFactoryClass(final String managedConnectionFactoryClass)
   {
      this.managedConnectionFactoryClass = managedConnectionFactoryClass;
   }

   

   /**
    * The ConnectionFactoryInterface attribute holds the ConnectionFactoryInterface from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the ConnectionFactoryInterface value.
    * @jmx:managed-attribute
    */
   public String getConnectionFactoryInterface()
   {
      return connectionFactoryInterface;
   }

   /**
    * Set the ConnectionFactoryInterface value.
    * @param connectionFactoryInterface The ConnectionFactoryInterface value.
    * @jmx:managed-attribute
    */
   public void setConnectionFactoryInterface(String connectionFactoryInterface)
   {
      this.connectionFactoryInterface = connectionFactoryInterface;
   }


   /**
    * The ConnectionFactoryImplClass attribute holds the ConnectionFactoryImplClass from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the ConnectionFactoryImplClass value.
    * @jmx:managed-attribute
    */
   public String getConnectionFactoryImplClass()
   {
      return connectionFactoryImplClass;
   }

   /**
    * Set the ConnectionFactoryImplClass value.
    * @param connectionFactoryImplClass The ConnectionFactoryImplClass value.
    * @jmx:managed-attribute
    */
   public void setConnectionFactoryImplClass(String connectionFactoryImplClass)
   {
      this.connectionFactoryImplClass = connectionFactoryImplClass;
   }


   /**
    * The ConnectionInterface attribute holds the ConnectionInterface from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the ConnectionInterface value.
    * @jmx:managed-attribute
    */
   public String getConnectionInterface()
   {
      return connectionInterface;
   }

   /**
    * Set the ConnectionInterface value.
    * @param connectionInterface The ConnectionInterface value.
    * @jmx:managed-attribute
    */
   public void setConnectionInterface(String connectionInterface)
   {
      this.connectionInterface = connectionInterface;
   }


   /**
    * The ConnectionImplClass attribute holds the ConnectionImplClass from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the connectionImplClass value.
    * @jmx:managed-attribute
    */
   public String getConnectionImplClass()
   {
      return connectionImplClass;
   }

   /**
    * Set the ConnectionImplClass value.
    * @param connectionImplClass The ConnectionImplClass value.
    * @jmx:managed-attribute
    */
   public void setConnectionImplClass(String connectionImplClass)
   {
      this.connectionImplClass = connectionImplClass;
   }


   /**
    * The TransactionSupport attribute holds the TransactionSupport from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    * It is ignored, and choice of ConnectionManager implementations determine
    * transaction support.
    *
    * Get the TransactionSupport value.
    * @return the TransactionSupport value.
    * @jmx:managed-attribute
    */
   public String getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * Set the TransactionSupport value.
    * @param transactionSupport The TransactionSupport value.
    * @jmx:managed-attribute
    */
   public void setTransactionSupport(String transactionSupport)
   {
      this.transactionSupport = transactionSupport;
   }


   /**
    * The ManagedConnectionFactoryProperties attribute holds the 
    * ManagedConnectionFactoryProperties from the ra.xml, together with 
    * user supplied values for all or some of these properties.  This must be
    * supplied as an element in the same format as in ra.xml, wrapped in a 
    * properties tag.
    * It should be supplied by xslt from ra.xml merged with an user
    * configuration xml file.
    * An alternative format has a config-property element with attributes for 
    * name and type and the value as content.
    *
    * @return the ManagedConnectionFactoryProperties value.
    * @jmx:managed-attribute
    */
   public Element getManagedConnectionFactoryProperties()
   {
      return managedConnectionFactoryProperties;
   }

   /**
    * Set the ManagedConnectionFactoryProperties value.
    * @param managedConnectionFactoryProperties The ManagedConnectionFactoryProperties value.
    * @jmx:managed-attribute
    */
   public void setManagedConnectionFactoryProperties(Element managedConnectionFactoryProperties)
   {
      this.managedConnectionFactoryProperties = managedConnectionFactoryProperties;
   }


   /**
    * The AuthenticationMechanismType attribute holds the AuthenticationMechanismType from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the AuthenticationMechanismType value.
    * @jmx:managed-attribute
    */
   public String getAuthenticationMechanismType()
   {
      return authenticationMechanismType;
   }

   /**
    * Set the AuthenticationMechanismType value.
    * @param authenticationMechanismType The AuthenticationMechanismType value.
    * @jmx:managed-attribute
    */
   public void setAuthenticationMechanismType(String authenticationMechanismType)
   {
      this.authenticationMechanismType = authenticationMechanismType;
   }


   /**
    * The CredentialInterface attribute holds the CredentialInterface from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the CredentialInterface value.
    * @jmx:managed-attribute
    */
   public String getCredentialInterface()
   {
      return credentialInterface;
   }

   /**
    * Set the CredentialInterface value.
    * @param credentialInterface The CredentialInterface value.
    * @jmx:managed-attribute
    */
   public void setCredentialInterface(String credentialInterface)
   {
      this.credentialInterface = credentialInterface;
   }


   /**
    * The ReauthenticationSupport attribute holds the ReauthenticationSupport from the ra.xml. 
    * It should be supplied by xslt from ra.xml
    *
    * @return the ReauthenticationSupport value.
    * @jmx:managed-attribute
    */
   public boolean isReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   /**
    * Set the ReauthenticationSupport value.
    * @param reauthenticationSupport The ReauthenticationSupport value.
    * @jmx:managed-attribute
    */
   public void setReauthenticationSupport(boolean reauthenticationSupport)
   {
      this.reauthenticationSupport = reauthenticationSupport;
   }


   /**
    * The JndiName attribute holds the jndi name the ConnectionFactory
    * will be bound under in jndi.  Note that an entry of the form DefaultDS2
    * will be bound to java:/DefaultDS2.
    *
    * @return the JndiName value.
    * @jmx:managed-attribute
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Set the JndiName value.
    * @param jndiName The JndiName value.
    * @jmx:managed-attribute
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   

   /**
    * Describe <code>startManagedConnectionFactory</code> method here.
    * creates managedConnectionFactory, creates ConnectionFactory, and binds it in jndi.
    * Returns the ManagedConnectionFactory to the ConnectionManager that called us.
    *
    * @return a <code>ManagedConnectionFactory</code> value
    * @jmx:managed-operation
    * @todo remove use of oldRarDeployment when xslt based deployment is written.
    */
   public ManagedConnectionFactory startManagedConnectionFactory(ConnectionManager cm) 
      throws DeploymentException
   {
      if (mcf != null) 
      {
         throw new DeploymentException("Stop the RARDeployment before restarting it");      
      } // end of if ()
      //WARNING HACK
      if (oldRarDeployment != null) 
      {
         copyRaInfo();
      } // end of if ()
      
      
      try 
      {
         mcfClass = Thread.currentThread().getContextClassLoader().loadClass(managedConnectionFactoryClass);
      }
      catch (ClassNotFoundException cnfe)
      {
         log.error("Could not find ManagedConnectionFactory class: " + managedConnectionFactoryClass, cnfe);
         throw new DeploymentException("Could not find ManagedConnectionFactory class: " + managedConnectionFactoryClass);
      } // end of try-catch
      try 
      {
         mcf = (ManagedConnectionFactory)mcfClass.newInstance();
      }
      catch (Exception e)
      {
         log.error("Could not instantiate ManagedConnectionFactory: " + managedConnectionFactoryClass, e);
         throw new DeploymentException("Could not instantiate ManagedConnectionFactory: " + managedConnectionFactoryClass);
      } // end of try-catch
      
      //set properties;
      setMcfProperties();

      // Give it somewhere to tell people things
      String categoryName = mcf.getClass().getName() + "." + jndiName;
      Logger log = Logger.getLogger(categoryName);
      PrintWriter logWriter = new CategoryWriter(log.getCategory());
      try
      {
         mcf.setLogWriter(logWriter);
      }
      catch (ResourceException re)
      {
         log.warn("Unable to set log writer '" + logWriter + "' on " +
               "managed connection factory", re);
         log.warn("Linked exception:", re.getLinkedException());
      }


      //bind into jndi
      String bindName = "java:/" + jndiName;
      try 
      {
      
         Object cf = mcf.createConnectionFactory(cm);
         if (log.isDebugEnabled())
            log.debug("Binding object '" + cf + "' into JNDI at '" + bindName + "'");
         synchronized (cfs)
         {
            cfs.put(jndiName, cf);
         }
         ((Referenceable)cf).setReference(new Reference(cf.getClass().getName(),
                                                        getClass().getName(),
                                                        null));
      
      
         Util.bind(new InitialContext(), bindName, cf);
         log.info("Bound connection factory for resource adapter '" +
               displayName + "' to JNDI name '" + bindName + "'");
      }
      catch (ResourceException re)
      {
         log.error("Could not create ConnectionFactory for adapter: " + managedConnectionFactoryClass, re);
         throw new DeploymentException("Could not create ConnectionFactory for adapter: " + managedConnectionFactoryClass);
      } // end of try-catch
      catch (NamingException ne)
      {
         log.error("Unable to bind connection factory to JNDI name '" +
               bindName + "'", ne);
         throw new DeploymentException("Could not bind ConnectionFactory into jndi: " + bindName);
      }

      return mcf;
   }

   /**
    * The <code>stopManagedConnectionFactory</code> method unbinds the ConnectionFactory
    * from jndi, releases the ManagedConnectionFactory instane, and releases the
    * ManagedConnectionFactory class.
    *
    * @jmx:managed-operation
    */
   public void stopManagedConnectionFactory() 
   {
      String bindName = "java:/" + jndiName;
      try 
      {
         new InitialContext().unbind(bindName);
      }
      catch (NamingException ne)
      {
         log.error("could not unbind managedConnectionFactory from jndi: " + jndiName, ne);       
      } // end of try-catch
      synchronized(cfs)
      {
         cfs.remove(jndiName);
      }
      mcf = null;
      mcfClass = null;
   }

   /**
    * The setManagedConnectionFactoryAttribute method can be used to set 
    * attributes on the ManagedConnectionFactory from code, without using the 
    * xml configuration.
    *
    * @param name a <code>String</code> value
    * @param clazz a <code>Class</code> value
    * @param value an <code>Object</code> value
    *
    * @jmx:managed-operation
    */
   public void setManagedConnectionFactoryAttribute(String name, Class clazz, Object value)
   {
      Method setter;

      try
      {
         setter = mcfClass.getMethod("set" + name, new Class[]{clazz});
      }
      catch (NoSuchMethodException nsme)
      {
         log.warn("The class '" + mcfClass.toString() + "' has no " +
                  "setter for config property '" + name + "'");
         throw new IllegalArgumentException("The class '" + mcfClass.toString() + "' has no " +
                  "setter for config property '" + name + "'");
      }
      try
      {
         setter.invoke(mcf, new Object[]{value});
         log.debug("set property " + name + " to value " + value);
      }
      catch (Exception e)
      {
         log.warn("Unable to invoke setter method '" + setter + "' " +
                  "on object '" + mcf + "'", e);
         throw new IllegalArgumentException("Unable to invoke setter method '" + setter + "' " +
                                            "on object '" + mcf + "'");
      }
   }


   //ObjectFactory implementation
   /**
    * Describe <code>getObjectInstance</code> method here.
    *
    * @param obj an <code>Object</code> value
    * @param name a <code>Name</code> value
    * @param nameCtx a <code>Context</code> value
    * @param environment a <code>Hashtable</code> value
    * @return an <code>Object</code> value
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
         Hashtable environment)
   {
      // Return the connection factory with the requested name
      if( log.isTraceEnabled() )
      {
         log.trace("RARDeployment.getObjectInstance, name = '" +
               name + "'");
      }
      synchronized (cfs)
      {
         return cfs.get(name.toString());
      }
   }

   

   //protected methods

   protected void setMcfProperties() throws DeploymentException
   {
      if (managedConnectionFactoryProperties == null) 
      {
         return;
      } // end of if ()
      
      // the properties that the deployment descriptor says we need to
      // set
      NodeList props = managedConnectionFactoryProperties.getChildNodes();
      for (int i = 0;  i < props.getLength(); i++ )
      {
         Node p = props.item(i);
         if (props.item(i).getNodeType() == Node.ELEMENT_NODE) 
         {
            Element prop = (Element)props.item(i);
            if (prop.getTagName().equals("config-property")) 
            {
               String name = null;
               String type = null;
               String value = null;
               //Support for more friendly config style
               //<config-property name="" type=""></config-property>
               if (prop.hasAttribute("name")) 
               {
                  name = prop.getAttribute("name");
                  type = prop.getAttribute("type");
                  value = MetaData.getElementContent(prop);
               } // end of if ()
               else
               {
                  name = MetaData.getElementContent(
                     MetaData.getUniqueChild(prop, "config-property-name"));
                  type = MetaData.getElementContent(
                     MetaData.getUniqueChild(prop, "config-property-type"));
                  value = MetaData.getElementContent(
                     MetaData.getUniqueChild(prop, "config-property-value"));
               } // end of else
               if (name == null || type == null || value == null)
               {
                  log.warn("Not setting config property '" + name + "'");
                  continue;
               }
         

               // see if it is a primitive type first
               Class clazz = Classes.getPrimitiveTypeForName(type);
               if (clazz == null)
               {
                  //not primitive, look for it.
                  try
                  {
                     clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                  }
                  catch (ClassNotFoundException cnfe)
                  {
                     log.warn("Unable to find class '" + type + "' for " +
                              "property '" + name + "' - skipping property.");
                     continue;
                  }
               }
               PropertyEditor pe = PropertyEditorManager.findEditor(clazz);
               if (pe == null)
               {
                  log.warn("Unable to find a PropertyEditor for class '" +
                           clazz + "' of property '" + name + "' - " +
                           "skipping property");
                  continue;
               }
               try
               {
                  pe.setAsText(value);
               }
               catch (IllegalArgumentException iae)
               {
                  log.warn("Value '" + value + "' is not valid for property '" +
                           name + "' of class '" + clazz + "' - skipping " +
                           "property");
                  continue;
               }
               Object v = pe.getValue();
               setManagedConnectionFactoryAttribute(name, clazz, v);
            } // end of if ()
         } // end of if ()
      } //end of for
   }

   /**
    * Describe <code>copyRaInfo</code> method here.
    *
    * @exception Exception if an error occurs
    * @todo remove this when xslt based deployment is written.
    */
   private void copyRaInfo() throws DeploymentException
   {
      try 
      {
         org.jboss.resource.RARMetaData rarMD = (org.jboss.resource.RARMetaData)org.jboss.util.jmx.MBeanServerLocator.locate().getAttribute(oldRarDeployment, "RARMetaData");
         setDisplayName(rarMD.getDisplayName());
         setManagedConnectionFactoryClass(rarMD.getManagedConnectionFactoryClass());
         //setTransactionSupport(rarMD.getTransactionSupport());
         //set(rarMD.getProperties());//???
         //setAuthMechType(rarMD.getAuthMechType());
         setReauthenticationSupport(rarMD.getReauthenticationSupport());
      }
      catch (Exception e)
      {
         throw new DeploymentException("couldn't get oldRarDeployment!", e);
      } // end of try-catch
      
   }

}// RARDeployment
