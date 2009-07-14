/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.iiop;

import java.io.InputStream;
import java.util.Properties;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.LifespanPolicy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.IdAssignmentPolicyValue;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.iiop.rmi.ir.InterfaceRepository;
import org.jboss.logging.Logger;

/**
 *  This is a JMX service that provides the default CORBA ORB
 *  for JBoss to use.
 *      
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.1.1.1 $
 */
public class CorbaORBService
   extends ServiceMBeanSupport
   implements CorbaORBServiceMBean, ObjectFactory
{
   // Constants -----------------------------------------------------
   public static String ORB_NAME = "JBossCorbaORB";
   public static String POA_NAME = "JBossCorbaPOA";
   public static String HOME_POA_NAME = "JBossCorbaHomePOA";
   public static String IR_POA_NAME = "JBossCorbaInterfaceRepositoryPOA";
   public static String NAMING_NAME = "JBossCorbaNaming";
    
   // Attributes ----------------------------------------------------

   private MBeanServer server;

   private String orbClass = null;
   private String orbSingletonClass = null;
   private String orbSingletonDelegate = null;
   private String orbPropertiesFileName = "orb-properties-file-not-defined";
   private String portableInterceptorInitializerClass = null;

   // Static --------------------------------------------------------

   private static ORB orb;
   private static POA poa;
   private static POA homePOA;
   private static POA namingPOA;
   private static POA irPOA;
   private static NamingContextExt namingService;
   private static InterfaceRepository iri;
   private static final Logger logger = 
                           Logger.getLogger(CorbaORBService.class);

   // ServiceMBeanSupport overrides ---------------------------------

   public String getName()
   {
      return "JBoss CORBA ORB";
   }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      this.server = server;
      return OBJECT_NAME;
   }
    
   protected void startService()
      throws Exception
   {

      Properties props = new Properties();

      // Read orb properties file into props
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream is = cl.getResourceAsStream(orbPropertiesFileName);
      props.load(is);

      // Initialize the ORB
      Properties systemProps = System.getProperties();
      if (orbClass != null) {
         props.put("org.omg.CORBA.ORBClass", orbClass);
         systemProps.put("org.omg.CORBA.ORBClass", orbClass);
      }
      if (orbSingletonClass != null) {
         props.put("org.omg.CORBA.ORBSingletonClass", orbSingletonClass);
         systemProps.put("org.omg.CORBA.ORBSingletonClass", orbSingletonClass);
      }
      if (orbSingletonDelegate != null)
         systemProps.put(org.jboss.system.ORBSingleton.DELEGATE_CLASS_KEY,
                         orbSingletonDelegate);
      String jacorbVerbosity = props.getProperty("jacorb.verbosity");
      // JacORB-specific hack: we add jacorb.verbosity to the system properties
      // just to avoid the warning "jacorb.properties not found".
      if (jacorbVerbosity != null)
         systemProps.put("jacorb.verbosity", jacorbVerbosity);
      System.setProperties(systemProps);
      if (portableInterceptorInitializerClass != null)
         props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                   + portableInterceptorInitializerClass, "");
      orb = ORB.init(new String[0], props);
      bind(ORB_NAME, "org.omg.CORBA.ORB");
      CorbaORB.setInstance(orb);

      // Initialize the POA
      poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      poa.the_POAManager().activate();
      bind(POA_NAME, "org.omg.PortableServer.POA");

      // Create the home POA as a child of the root POA
      LifespanPolicy persistentLifespanPolicy = 
         poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
      IdAssignmentPolicy userIdAssignmentPolicy = 
         poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
      Policy[] policies = new Policy[] {
         persistentLifespanPolicy, 
         userIdAssignmentPolicy
      };
      homePOA = poa.create_POA("EJBHome", null, policies);
      homePOA.the_POAManager().activate();
      bind(HOME_POA_NAME, "org.omg.PortableServer.POA");

      // Create the naming server POA also as a child of the root POA,
      // with the same policies
      namingPOA = poa.create_POA("Naming", null, policies);
      namingPOA.the_POAManager().activate();

      // Create the naming service
      NamingContextImpl ns = new NamingContextImpl(namingPOA);
      byte[] rootContextId = "root".getBytes();
      namingPOA.activate_object_with_id(rootContextId, ns);
      namingService = NamingContextExtHelper.narrow(
                  namingPOA.create_reference_with_id(rootContextId, 
                                "IDL:omg.org/CosNaming/NamingContextExt:1.0"));
      bind(NAMING_NAME, "org.omg.CosNaming.NamingContextExt");
      logger.info("Naming: ["+orb.object_to_string(namingService)+"]");

      // Create a POA for interface repositories,
      // also with the same policies
      try {
         irPOA = poa.create_POA("IR", null, policies);
         bind(IR_POA_NAME, "org.omg.PortableServer.POA");
         
         // Activate the poa
         irPOA.the_POAManager().activate();
         
      } catch (Exception ex) {
         getLog().error("Error in IR POA initialization", ex);
      }

//    // Create an interface repository just for testing (TODO: remove this)
//    try {
//       org.jboss.iiop.rmi.ir.InterfaceRepository iri = 
//          new org.jboss.iiop.rmi.ir.InterfaceRepository(orb, irPoa, "IR");
// 
//       // Test this interface repository
//       iri.mapClass(org.jboss.iiop.TestBase.class);
//       iri.mapClass(org.jboss.iiop.Test.class);
//       iri.mapClass(org.jboss.iiop.TestValue.class);
//       iri.mapClass(org.jboss.iiop.TestException.class);
//       iri.finishBuild();
//       
//       java.io.FileOutputStream fos = new java.io.FileOutputStream("ir.ior");
//       java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos);
//       osw.write(iri.getReference());
//       osw.flush();
//       fos.flush();
//       osw.close();
//       fos.close();
//    } catch (org.jboss.iiop.rmi.RMIIIOPViolationException violation) {
//          getLog().error("RMI/IIOP violation, section: " 
//                         + violation.getSection(), violation);
//    } catch (Exception ex) {
//          getLog().error("Error in interface repository initialization", ex);
//    }

      // Make the ORB work
      new Thread(
         new Runnable() {
            public void run() {
               orb.run();
            }
         }, "ORB thread"
      ).start(); 
   }
    
   protected void stopService()
   {
      try {
         // Unbind from JNDI
         unbind(ORB_NAME);
         unbind(POA_NAME);
         unbind(HOME_POA_NAME);
         unbind(NAMING_NAME);
         unbind(IR_POA_NAME);
      } catch (Exception e) {
         log.error("Exception while stopping ORB service", e);
      }

      try {
         // Destroy naming POA
         namingPOA.destroy(false, false);
      } catch (Exception e) {
         log.error("Exception while stopping ORB service", e);
      }

      try {
         // Destroy home POA
         homePOA.destroy(false, false);
      } catch (Exception e) {
         log.error("Exception while stopping ORB service", e);
      }

      try {
         // Destroy IR POA
         irPOA.destroy(false, false);
      } catch (Exception e) {
         log.error("Exception while stopping ORB service", e);
      }

      try {
         // Stop ORB
         orb.shutdown(false);
      } catch (Exception e) {
         log.error("Exception while stopping ORB service", e);
      }
   }
    

   // CorbaORBServiceMBean implementation ---------------------------

   public String getORBClass()
   {
      return orbClass;
   }

   public void setORBClass(String orbClass)
   {
      this.orbClass = orbClass;
   }

   public String getORBSingletonClass()
   {
      return orbSingletonClass;
   }

   public void setORBSingletonClass(String orbSingletonClass)
   {
      this.orbSingletonClass = orbSingletonClass;
   }

   public String getORBSingletonDelegate()
   {
      return orbSingletonDelegate;
   }

   public void setORBSingletonDelegate(String orbSingletonDelegate)
   {
      this.orbSingletonDelegate = orbSingletonDelegate;
   }

   public void setORBPropertiesFileName(String orbPropertiesFileName)
   {
      this.orbPropertiesFileName = orbPropertiesFileName;
   }
   
   public String getORBPropertiesFileName()
   {
      return orbPropertiesFileName;
   }

   public String getPortableInterceptorInitializerClass()
   {
      return portableInterceptorInitializerClass;
   }

   public void setPortableInterceptorInitializerClass(
                                 String portableInterceptorInitializerClass)
   {
      this.portableInterceptorInitializerClass = 
                                          portableInterceptorInitializerClass;
   }

   // ObjectFactory implementation ----------------------------------

   public Object getObjectInstance(Object obj, Name name,
                                   Context nameCtx, Hashtable environment)
      throws Exception
   {
      String s = name.toString();
      if (logger.isTraceEnabled())
         logger.trace("getObjectInstance: obj.getClass().getName=\"" +
                      obj.getClass().getName() +
                      "\n                   name=" + s);
      if (ORB_NAME.equals(s))
         return orb;
      if (POA_NAME.equals(s))
         return poa;
      if (HOME_POA_NAME.equals(s))
         return homePOA;
      if (NAMING_NAME.equals(s))
         return namingService;
      if (IR_POA_NAME.equals(s))
         return irPOA;
      return null;
   }


   // Private -------------------------------------------------------

   private void bind(String name, String className)
      throws Exception
   {
      Reference ref = new Reference(className, getClass().getName(), null);
      new InitialContext().bind("java:/"+name, ref);
   }

   private void unbind(String name)
      throws Exception
   {
      new InitialContext().unbind("java:/"+name);
   }

   // Static inner class --------------------------------------------

   /**
    * This subclass of <code>org.jacorb.naming.NamingContextImpl</code>
    * overrides the method <code>new_context()</code>, because its
    * implementation in <code>org.jacorb.naming.NamingContextImpl</code>
    * is not suitable for our in-VM naming server. The superclass 
    * implementation of <code>new_context()</code> assumes that naming context
    * states are persistently stored and requires a servant activator that
    * reads context states from persistent storage.
    */
   static class NamingContextImpl
      extends org.jacorb.naming.NamingContextImpl
   {
      POA poa;
      int childCount = 0;

      NamingContextImpl(POA poa)
      {
         this.poa = poa;
      }

      public NamingContext new_context() 
      {
         try {
            NamingContextImpl newContextImpl = new NamingContextImpl(poa);
            byte[] oid = (new String(poa.servant_to_id(this)) +  
                          "/ctx" + (++childCount)).getBytes();
            poa.activate_object_with_id(oid, newContextImpl);
            return NamingContextExtHelper.narrow(
                        poa.create_reference_with_id(oid, 
                                "IDL:omg.org/CosNaming/NamingContextExt:1.0"));
         }
         catch (Exception e) {
            logger.error("Cannot create CORBA naming context", e);
            return null;
         }
      }
   }

}
