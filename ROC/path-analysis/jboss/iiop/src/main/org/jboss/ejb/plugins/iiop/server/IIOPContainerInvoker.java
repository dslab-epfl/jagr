/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.iiop.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.URL;
import java.rmi.Remote;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
import javax.rmi.PortableRemoteObject;
import javax.transaction.Transaction;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.CORBA.Repository;
import org.omg.CORBA.SetOverrideType;
import org.omg.PortableServer.Current;
import org.omg.PortableServer.CurrentHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;

import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.invocation.Invocation;
import org.jboss.ejb.plugins.iiop.client.StubStrategy;
import org.jboss.ejb.plugins.iiop.EJBMetaDataImpl;
import org.jboss.ejb.plugins.iiop.HandleImpl;
import org.jboss.ejb.plugins.iiop.HomeFactory;
import org.jboss.ejb.plugins.iiop.HomeHandleImpl;
import org.jboss.ejb.plugins.iiop.LocalInvoker;
import org.jboss.iiop.CorbaORBService;
import org.jboss.iiop.rmi.ir.InterfaceRepository;
import org.jboss.iiop.rmi.AttributeAnalysis;
import org.jboss.iiop.rmi.OperationAnalysis;
import org.jboss.iiop.rmi.InterfaceAnalysis;
import org.jboss.iiop.rmi.RmiIdlUtil;
import org.jboss.logging.Logger;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.web.WebClassLoader;

/**
 * A <code>ContainerInvoker</code> for invoking enterprise beans
 * over the IIOP invocation transport.
 * <p>
 * An <code>IIOPContainerInvoker</code> provides two CORBA servants: one for
 * its container's <code>EJBHome</code>, another for its container's set of 
 * <code>EJBObjects</code>. 
 * The <code>EJBObject</code> servant is the <code>IIOPContainerInvoker</code>
 * itself, which extends <code>org.omg.PortableServer.Servant</code> and 
 * implements <code>org.omg.CORBA.portable.InvokeHandler</code>. 
 * The <code>EJBHome</code> servant is a field of the 
 * <code>IIOPContainerInvoker</code>. This field is an instance of an inner
 * class of the container invoker.
 * <p>
 * An <code>IIOPContainerInvoker</code> has its own POA, used for all 
 * <code>EJBObject</code>s in its container. This POA's lifespan policy 
 * depends on the kind of container: transient for a session container, 
 * persistent for an entity container. 
 * <p>
 * The <code>EJBHome</code> is registered on a different POA, which is shared 
 * by all <code>IIOPContainerInvokers</code> and has persistent lifespan 
 * policy.
 * <p>
 * The JNDI name of a bean is the "reference data" field embedded into the 
 * CORBA references for the beans' <code>EJBHome</code>. The JNDI name is also
 * used as the name of POA of the bean's <code>IIOPContainerInvoker</code>. 
 * The id of a bean instance is the "reference data" field embedded into the 
 * CORBA reference for the corresponding <code>EJBObject</code>.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 1.1.1.1 $
 */
public class IIOPContainerInvoker
      extends Servant
   implements InvokeHandler, ContainerInvoker, LocalInvoker
{

   // Constants  --------------------------------------------------------------

   private static final byte[] nullId = createNullId();
   private static final Logger staticLogger = 
                           Logger.getLogger(IIOPContainerInvoker.class);

   // Attributes -------------------------------------------------------------

   /**
    * This <code>IIOPContainerInvoker</code>'s container. 
    */ 
   private Container container;

   /**
    * <code>JNDI</code> name of the enterprise bean in the container. 
    */ 
   private String jndiName;

   /**
    * <code>EJBMetaData</code> the enterprise bean in the container. 
    */ 
   private EJBMetaDataImpl ejbMetaData;

   /**
    * Mapping from bean methods to <code>SkeletonStrategy</code> instances.
    */
   private Map beanMethodInvokerMap;

   /**
    * Mapping from home methods to <code>SkeletonStrategy</code> instances.
    */
   private Map homeMethodInvokerMap;

   /**
    * CORBA repository ids of the RMI-IDL interfaces implemented by the bean 
    * (<code>EJBObject</code> instance).
    */
   private String[] beanRepositoryIds;

   /**
    * CORBA repository ids of the RMI-IDL interfaces implemented by the bean's
    * home (<code>EJBHome</code> instance).
    */
   private String[] homeRepositoryIds;

   /**
    * A reference for the ORB.
    */
   private ORB orb;

   /**
    * EJBHomes are registered on this POA.
    */
   private POA homePOA;

   /**
    * POA for all <code>EJBObject</code>s in the container.
    */
   private POA poa;

   /**
    * Thread-local <code>Current</code> object from which we get the target oid
    * in an incoming IIOP request.
    */
   private Current poaCurrent;

   /**
    * The container's <code>CodebasePolicy</code>.
    */
   private Policy codebasePolicy;

   /**
    * Servant for the container's <code>EJBHome</code>.
    */
   private HomeServant ejbHomeServant;

   /**
    * The container's <code>EJBHome</code>.
    */
   private EJBHome ejbHome;

   /**
    * The enterprise bean's interface repository implementation.
    */
   private InterfaceRepository iri;

   /**
    * IR object describing the bean's home interface.
    */
   private InterfaceDef homeInterfaceDef;
      
   /**
    * IR object describing the bean's remote interface.
    */
   private InterfaceDef beanInterfaceDef;
      
   /**
    * POA for the enterprise bean's interface repository.
    */
   private POA irPOA;

   /**
    * This <code>IIOPContainerInvoker</code>'s logger. Initialized with a
    * per-class logger. Once the enterprise bean's JNDI name is known, the
    * per-class logger will be replaced by a per-instance logger whose name
    * includes the JNDI name.
    */ 
   private Logger logger = staticLogger;

   // Implementation of the interface ContainerPlugin -------------------------

   public void setContainer(Container container) 
   {
      this.container = container;
      if (container != null) {
         String loggerName = IIOPContainerInvoker.class.getName() + '.' 
                                   + container.getBeanMetaData().getJndiName();
         logger = Logger.getLogger(loggerName);
      }
   }

   public void create() throws Exception 
   {
      // Initialize orb, homePoa, and iri references
      try {
         orb = (ORB)new InitialContext().lookup("java:/"
                                                + CorbaORBService.ORB_NAME);
      } 
      catch (NamingException e) {
         throw new RuntimeException("Cannot lookup java:/" 
                                    + CorbaORBService.ORB_NAME + ": " + e);
      }
      try {
         homePOA = (POA)new InitialContext().lookup("java:/"
                                              + CorbaORBService.HOME_POA_NAME);
      } 
      catch (NamingException e) {
         throw new RuntimeException("Cannot lookup java:/" 
                                   + CorbaORBService.HOME_POA_NAME + ": " + e);
      }
      try {
         irPOA = (POA)new InitialContext().lookup("java:/"
                                                + CorbaORBService.IR_POA_NAME);
      } 
      catch (NamingException e) {
         throw new RuntimeException("Cannot lookup java:/" 
                                    + CorbaORBService.IR_POA_NAME + ": " + e);
      }

      // Get the jndi name of the enterprise bean. This name will be embedded 
      // into the CORBA reference for the bean's IR and for its EJBHome.
      jndiName = container.getBeanMetaData().getJndiName();

      // Create a CORBA interface repository for the enterprise bean
      iri = new InterfaceRepository(orb, irPOA, jndiName);

      // Add bean interface info to the interface repository
      iri.mapClass(((ContainerInvokerContainer)container).getRemoteClass());
      iri.mapClass(((ContainerInvokerContainer)container).getHomeClass());
      iri.finishBuild();

      logger.info("CORBA interface repository for " + jndiName + ":\n"
                  + orb.object_to_string(iri.getReference()));

      // Create bean method mappings for container invoker
      logger.debug("Bean methods:");

      InterfaceAnalysis interfaceAnalysis = 
            InterfaceAnalysis.getInterfaceAnalysis(
                  ((ContainerInvokerContainer)container).getRemoteClass());

      beanMethodInvokerMap = new HashMap();

      AttributeAnalysis[] attrs = interfaceAnalysis.getAttributes();
      for (int i = 0; i < attrs.length; i++) {
         OperationAnalysis op = attrs[i].getAccessorAnalysis();

         logger.debug("    " + op.getJavaName()
                      + "\n                " + op.getIDLName());
         beanMethodInvokerMap.put(op.getIDLName(), 
                                  new SkeletonStrategy(op.getMethod()));
         op = attrs[i].getMutatorAnalysis();
         if (op != null) {
            logger.debug("    " + op.getJavaName()
                         + "\n                " + op.getIDLName());
            beanMethodInvokerMap.put(op.getIDLName(), 
                                     new SkeletonStrategy(op.getMethod()));
         }
      }

      OperationAnalysis[] ops = interfaceAnalysis.getOperations();
      for (int i = 0; i < ops.length; i++) {
         logger.debug("    " + ops[i].getJavaName()
                      + "\n                " + ops[i].getIDLName());
         beanMethodInvokerMap.put(ops[i].getIDLName(),
                                  new SkeletonStrategy(
                                        ops[i].getMethod()));
      }

      // Initialize repository ids of remote interface
      beanRepositoryIds = interfaceAnalysis.getAllTypeIds();

      // Create home method mappings for container invoker
      logger.debug("Home methods:");

      interfaceAnalysis = 
            InterfaceAnalysis.getInterfaceAnalysis(
                  ((ContainerInvokerContainer)container).getHomeClass());

      homeMethodInvokerMap = new HashMap();

      attrs = interfaceAnalysis.getAttributes();
      for (int i = 0; i < attrs.length; i++) {
         OperationAnalysis op = attrs[i].getAccessorAnalysis();

         logger.debug("    " + op.getJavaName()
                      + "\n                " + op.getIDLName());
         homeMethodInvokerMap.put(op.getIDLName(), 
                                  new SkeletonStrategy(op.getMethod()));
         op = attrs[i].getMutatorAnalysis();
         if (op != null) {
            logger.debug("    " + op.getJavaName()
                         + "\n                " + op.getIDLName());
            homeMethodInvokerMap.put(op.getIDLName(), 
                                     new SkeletonStrategy(op.getMethod()));
         }
      }

      ops = interfaceAnalysis.getOperations();
      for (int i = 0; i < ops.length; i++) {
         logger.debug("    " + ops[i].getJavaName()
                      + "\n                " + ops[i].getIDLName());
         homeMethodInvokerMap.put(ops[i].getIDLName(),
                                  new SkeletonStrategy(ops[i].getMethod()));
      }

      // Initialize repository ids of home interface
      homeRepositoryIds = interfaceAnalysis.getAllTypeIds();

      // Create codebasePolicy containing the container's codebase string
      logger.debug("container classloader: " + container.getClassLoader()
                   + "\ncontainer parent classloader: "
                   + container.getClassLoader().getParent());
      String codebaseString = container.getCodebase();
      Any codebase = orb.create_any();
      codebase.insert_string(codebaseString);
      codebasePolicy = orb.create_policy(CodebasePolicy.TYPE, codebase);
      logger.debug("codebasePolicy: " + codebasePolicy);

      // Initialize IR objects describing the bean's home and remote interfaces
      Repository ir = iri.getReference();
      homeInterfaceDef =
         InterfaceDefHelper.narrow(ir.lookup_id(homeRepositoryIds[0]));
      beanInterfaceDef =
         InterfaceDefHelper.narrow(ir.lookup_id(beanRepositoryIds[0]));
      
      // Activate ejbHome and get a CORBA reference to it
      ejbHomeServant = new HomeServant();
      homePOA.activate_object_with_id(jndiName.getBytes(), ejbHomeServant);
      org.omg.CORBA.Object corbaRef = 
         homePOA.id_to_reference(jndiName.getBytes());
      
      // Add to the ejbHome IOR a component with JAVA_CODEBASE_TAG
      corbaRef = corbaRef._set_policy_override(
                  new Policy[] { codebasePolicy },
                  SetOverrideType.ADD_OVERRIDE);

      // Just for testing
      logger.debug("EJBHome reference for " + jndiName + ":\n"
                  + orb.object_to_string(corbaRef));

      ejbHome = (EJBHome)PortableRemoteObject.narrow(corbaRef, EJBHome.class);

      // Get a reference for the root POA
      POA rootPOA = null;
      try {
         rootPOA = (POA)new InitialContext().lookup("java:/"
                                              + CorbaORBService.POA_NAME);
      } 
      catch (NamingException e) {
         throw new RuntimeException("Cannot lookup java:/"
                                    + CorbaORBService.POA_NAME + ": " + e);
      }

      // Initialize policies for bean POA
      // (policies[0] will be set later on, depending on the kind of bean)
      Policy[] policies = new Policy[6];
      policies[1] = rootPOA.create_id_assignment_policy(
            IdAssignmentPolicyValue.USER_ID);
      policies[2] = rootPOA.create_servant_retention_policy(
            ServantRetentionPolicyValue.NON_RETAIN);
      policies[3] = rootPOA.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_DEFAULT_SERVANT);
      policies[4] = rootPOA.create_id_uniqueness_policy(
            IdUniquenessPolicyValue.MULTIPLE_ID);
      policies[5] = codebasePolicy.copy();

      // Set policies[0] and create metadata depending on the kind of bean
      if (container.getBeanMetaData() instanceof EntityMetaData) {
         
         // This is an entity bean (lifespan: persistent)
         policies[0] = rootPOA.create_lifespan_policy(
               LifespanPolicyValue.PERSISTENT);

         Class pkClass;
         EntityMetaData metaData = (EntityMetaData)container.getBeanMetaData();
         String pkClassName = metaData.getPrimaryKeyClass();
         try {
            if (pkClassName != null)
               pkClass = container.getClassLoader().loadClass(pkClassName);
            else
               pkClass = container.getClassLoader().loadClass(
                     metaData.getEjbClass()).getField(
                           metaData.getPrimKeyField()).getClass();
         } 
         catch (NoSuchFieldException e) {
            logger.error("Unable to identify Bean's Primary Key class! "
                         + "Did you specify a primary key class and/or field? "
                         + "Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         } 
         catch (NullPointerException e) {
            logger.error("Unable to identify Bean's Primary Key class! " 
                         + "Did you specify a primary key class and/or field? "
                         + "Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         }

         ejbMetaData = new EJBMetaDataImpl(
               ((ContainerInvokerContainer)container).getRemoteClass(),
               ((ContainerInvokerContainer)container).getHomeClass(),
               pkClass,
               false, // Session
               false, // Stateless
               ejbHome);
      } 
      else {

         // This is a session bean (lifespan: transient)
         policies[0] = rootPOA.create_lifespan_policy(
               LifespanPolicyValue.TRANSIENT);
         if (((SessionMetaData)container.getBeanMetaData()).isStateless()) {

            // Stateless session bean
            ejbMetaData = new EJBMetaDataImpl(
                  ((ContainerInvokerContainer)container).getRemoteClass(),
                  ((ContainerInvokerContainer)container).getHomeClass(),
                  null, // No PK
                  true, // Session
                  true, // Stateless
                  ejbHome);

         } 
         else { 

            // Stateful session bean
            ejbMetaData = new EJBMetaDataImpl(
                  ((ContainerInvokerContainer)container).getRemoteClass(),
                  ((ContainerInvokerContainer)container).getHomeClass(),
                  null,  // No PK
                  true,  // Session
                  false, // Stateless
                  ejbHome);
         }
      }

      // Create bean POA (with name jndiName) and set its servant
      poa = rootPOA.create_POA(jndiName, null, policies);
      poa.set_servant(this);

      // Get the POACurrent object
      poaCurrent = CurrentHelper.narrow(
                  orb.resolve_initial_references("POACurrent"));
   }

   public void start() throws Exception 
   {
      // Activate bean POA
      poa.the_POAManager().activate();
      
      // Just for testing
      logger.info("EJBHome reference for " + jndiName + ":\n"
                  + orb.object_to_string((org.omg.CORBA.Object)ejbHome));

      Context initialContext = null;
      try {
         // Bind the bean home in the JNDI initial context
         initialContext = new InitialContext();
         rebind(initialContext, jndiName, new Reference(
                     "javax.ejb.EJBHome", 
                     new StringRefAddr("IOR", 
                                       orb.object_to_string(
                                               (org.omg.CORBA.Object)ejbHome)),
                     HomeFactory.class.getName(),
                     null));
         logger.info("Bound " + container.getBeanMetaData().getEjbName()
                     + " to " + jndiName);
      } 
      catch (NamingException e) {
         throw new RuntimeException("Cannot bind EJBHome in JNDI:\n" + e);
      }

      NamingContextExt corbaContext = null;
      try {
         // Obtain local (in-VM) CORBA naming context
         corbaContext = NamingContextExtHelper.narrow((org.omg.CORBA.Object)
               initialContext.lookup("java:/" + CorbaORBService.NAMING_NAME));


      } 
      catch (NamingException e) {
         throw new RuntimeException("Cannot lookup java:/"
                                   + CorbaORBService.NAMING_NAME + ":\n" + e);
      }
      try {
         // Register bean home in local CORBA naming context
         rebind(corbaContext, jndiName, (org.omg.CORBA.Object)ejbHome);
      }
      catch (Exception e) {
         logger.error("Cannot bind EJBHome in CORBA naming service:", e);
         throw new RuntimeException(
               "Cannot bind EJBHome in CORBA naming service:\n" + e);
      }

      // TODO: this should be after all beans were deployed
      // ORBSingleton.start(); //!!!!!!!!!!!!!!!!!!

   }

   public void stop() 
   {
      try {
         // Get initial JNDI context and local (in-VM) CORBA naming context
         Context initialContext = new InitialContext();
         NamingContextExt corbaContext = 
               NamingContextExtHelper.narrow((org.omg.CORBA.Object)
                     initialContext.lookup("java:/"
                                   + CorbaORBService.NAMING_NAME));

         // Unbind bean home from the JNDI initial context
         try {
            initialContext.unbind(jndiName);
         }
         catch (NamingException namingException) {
            logger.error("Cannot unbind EJBHome from JNDI", namingException);
         }

         // Unregister bean home from local CORBA naming context
         try {
            NameComponent[] name = corbaContext.to_name(jndiName);
            corbaContext.unbind(name);
         }
         catch (InvalidName invalidName) {
            logger.error("Cannot unregister EJBHome from CORBA naming service",
                         invalidName);
         }
         catch (NotFound notFound) {
            logger.error("Cannot unregister EJBHome from CORBA naming service",
                         notFound);
         }
         catch (CannotProceed cannotProceed) {
            logger.error("Cannot unregister EJBHome from CORBA naming service",
                         cannotProceed);
         }
      }
      catch (NamingException namingException) {
         logger.error("Unexpected error in JNDI lookup", namingException);
      }

      // Deactivate the EJBHome 
      try {
         homePOA.deactivate_object(jndiName.getBytes());
      }
      catch (WrongPolicy wrongPolicy) {
         logger.error("Cannot deactivate EJBHome", wrongPolicy);
      }
      catch (ObjectNotActive objectNotActive) {
         logger.error("Cannot deactivate EJBHome", objectNotActive);
      }

      // Destroy bean POA
      try {
         poa.the_POAManager().deactivate(false, /* etherealize_objects */
                                         true   /* wait_for_completion */ );
         poa.destroy(false, /* etherealize_objects */
                     false  /* wait_for_completion */ );
       }
      catch (AdapterInactive adapterInactive) {
          logger.error("Cannot deactivate home POA", adapterInactive);
      }

      // Deactivate the interface repository
      iri.shutdown();
   }

   public void destroy() 
   { 
   }
   
   // Implementation of the interface ContainerInvoker ------------------------

   public EJBMetaData getEJBMetaData() 
   {
      return ejbMetaData;
   }

   public Object getEJBHome() 
   {
      return ejbHome;
   }

   public Object getStatelessSessionEJBObject()
   {
      try {
         return (EJBObject)PortableRemoteObject.narrow(
               poa.create_reference_with_id(nullId, beanRepositoryIds[0]),
               EJBObject.class);
      }
      catch (Exception e) {
         throw new RuntimeException("Unable to create reference to EJBObject\n"
                                    + e);
      }
   }

   public Object getStatefulSessionEJBObject(Object id)
   {
      try {
         return (EJBObject)PortableRemoteObject.narrow(
               poa.create_reference_with_id(toByteArray(id), 
                                            beanRepositoryIds[0]),
               EJBObject.class);
      }
      catch (Exception e) {
         throw new RuntimeException("Unable to create reference to EJBObject\n"
                                    + e);
      }
   }

   public Object getEntityEJBObject(Object id)
   {
      if (logger.isTraceEnabled()) {
         logger.trace("getEntityEJBObject(), id class is "
                      + id.getClass().getName());
      }
      try {
         return (EJBObject)PortableRemoteObject.narrow(
               poa.create_reference_with_id(toByteArray(id),
                                            beanRepositoryIds[0]),
               EJBObject.class);
      }
      catch (Exception e) {
         throw new RuntimeException("Unable to create reference to EJBObject\n"
                                    + e);
      }
   }
   
   public Collection getEntityCollection(Collection ids)
   {
      if (logger.isTraceEnabled()) {
         logger.trace("entering getEntityCollection()");
      }
      ArrayList list = new ArrayList(ids.size());
      Iterator idEnum = ids.iterator();
      while(idEnum.hasNext()) {
         list.add(getEntityEJBObject(idEnum.next()));
      }
      if (logger.isTraceEnabled()) {
         logger.trace("leaving getEntityCollection()");
      }
      return list;
   }

   // This method overrides the one in org.omg.PortableServer.Servant ---------

   /**
    * Returns an IR object describing the bean's remote interface.
    */
   public org.omg.CORBA.Object _get_interface_def()
   {
      if (beanInterfaceDef != null)
         return beanInterfaceDef;
      else
         return super._get_interface_def();
   }
   
   // Implementation of the interface InvokeHandler ---------------------------

   /**
    * Returns an array with the CORBA repository ids of the RMI-IDL interfaces 
    * implemented by the container's <code>EJBObject</code>s.
    */
   public String[] _all_interfaces(POA poa, byte[] objectId) 
   {
      return (String[])beanRepositoryIds.clone();
   }

   /**
    * Receives IIOP requests to the the container's <code>EJBObject</code>s
    * and forwards them to the container.
    */
   public OutputStream _invoke(String opName,
                               InputStream in,
                               ResponseHandler handler) 
   {

      if (logger.isTraceEnabled()) {
         logger.trace("EJBObject invocation: " + opName);
      }

      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
      
      try {
         SkeletonStrategy op = 
            (SkeletonStrategy) beanMethodInvokerMap.get(opName);
         if (op == null) {
            throw new BAD_OPERATION(opName);
         }

         Object id;
         try {
            id = toObject(poaCurrent.get_object_id());
            if (logger.isTraceEnabled()) {
               logger.trace("                      id class is " 
                            + id.getClass().getName());
            }
         }
         catch (Exception e) {
            logger.error("Error getting EJBObject id", e);
            throw new UnknownException(e);
         }

         org.omg.CORBA_2_3.portable.OutputStream out;
         try {
            Object retVal;
            
            // The EJBObject methods getHandle() and getPrimaryKey() receive
            // special treatment because the container does not implement 
            // them. The remaining EJBObject methods (getEJBHome(), remove(),
            // and isIdentical()) are forwarded to the container.

            if (opName.equals("_get_primaryKey")) {
               retVal = id;
            }
            else if (opName.equals("_get_handle")) {
               retVal = new HandleImpl(_this_object());
            }
            else {
               Object[] params = 
                  op.readParams((org.omg.CORBA_2_3.portable.InputStream)in);
               Invocation inv = new Invocation(id, 
                                               op.getMethod(), 
                                               params,
                                               null, /* tx */
                                               null, /* identity */
                                               null  /* credential */);
               inv.setType(Invocation.REMOTE);
               retVal = container.invoke(inv);
            }
            out = (org.omg.CORBA_2_3.portable.OutputStream) 
                  handler.createReply();
            if (op.isNonVoid()) {
               op.writeRetval(out, retVal);
            }
         }
         catch (Exception e) {
            if (logger.isTraceEnabled()) {
               logger.trace("Exception in EJBObject invocation", e);
            }
            out = (org.omg.CORBA_2_3.portable.OutputStream) 
                  handler.createExceptionReply();
            op.writeException(out, e);
         }
         return out;
      }
      finally {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   // Implementation of the interface LocalInvoker ----------------------------

   public Object invoke(String opName,
                 Object[] arguments, 
                 Transaction tx, 
                 Principal identity, 
                 Object credential)
      throws Exception
   {
      if (logger.isTraceEnabled()) {
         logger.trace("EJBObject local invocation: " + opName);
      }

      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());

      try {
         SkeletonStrategy op = 
            (SkeletonStrategy) beanMethodInvokerMap.get(opName);
         if (op == null) {
            throw new BAD_OPERATION(opName);
         }
         
         Object id;
         try {
            id = toObject(poaCurrent.get_object_id());
            if (logger.isTraceEnabled()) {
               logger.trace("                      id class is " 
                            + id.getClass().getName());
            }
         }
         catch (Exception e) {
            logger.error("Error getting EJBObject id", e);
            throw new UnknownException(e);
         }
         
         Invocation inv = new Invocation(id, 
                                         op.getMethod(), 
                                         arguments,
                                         null, /* tx */
                                         null, /* identity */
                                         null  /* credential */);
         inv.setType(Invocation.LOCAL);
         return container.invoke(inv);
      }
      finally {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   // Static methods ----------------------------------------------------------

   /**
    * Returns the CORBA repository id of a given the RMI-IDL interface.
    */
   public static String rmiRepositoryId(Class clz) 
   {
      return "RMI:" + clz.getName() + ":0000000000000000";
   }
   
   /**
    * (Re)binds a value to a name in a given JNDI context, creating any
    * non-existent intermediate contexts along the way.
    */
   public static void rebind(Context ctx, String name, Object val)
         throws NamingException 
   {
      Name n = ctx.getNameParser("").parse(name);

      while (n.size() > 1) {
         String ctxName = n.get(0);

         try {
            ctx = (Context)ctx.lookup(ctxName);
         } 
         catch (NameNotFoundException e) {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }
      ctx.rebind(n.get(0), val);
   }

   /**
    * (Re)binds an object to a name in a given CORBA naming context, creating 
    * any non-existent intermediate contexts along the way.
    */
   public static void rebind(NamingContextExt ctx, 
                             String strName, org.omg.CORBA.Object obj)
          throws Exception
   { 
      NameComponent[] name = ctx.to_name(strName);
      NamingContext intermediateCtx = ctx;
      
      for (int i = 0; i < name.length - 1; i++ ) {
         NameComponent[] relativeName = new NameComponent[] { name[i] };
         try {
            intermediateCtx = NamingContextHelper.narrow(
                  intermediateCtx.resolve(relativeName));
         }
         catch (NotFound e) {
            intermediateCtx = intermediateCtx.bind_new_context(relativeName);
         }
      }
      intermediateCtx.rebind(new NameComponent[] { name[name.length - 1] }, 
                             obj);
   }

   /**
    * Returns an array with the CORBA repository ids of all RMI-IDL interfaces 
    * implemented or extended by a given class or interface, sorted so that
    * no derived interface appears after a base interface.
    */
   public static String[] rmiInterfaces(Class clz) 
   { 
      Class[] interfs = clz.getInterfaces(); 
      ArrayList list = new ArrayList(interfs.length);

      list.add(clz);
      for (int i = 0; i < interfs.length; i++) { 
         if (interfs[i] != Remote.class 
               && RmiIdlUtil.isRMIIDLRemoteInterface(interfs[i])) { 
            list.add(interfs[i]); 
         }
      }

      Object[] remoteInterfs = list.toArray();
      arraysort(remoteInterfs);

      String[] repositoryIds = new String[remoteInterfs.length];
      for (int i = 0; i < remoteInterfs.length; i++) {
         repositoryIds[i] = rmiRepositoryId((Class)remoteInterfs[i]);
      }
      return repositoryIds;
   }

   /**
    * Sorts an array of classes or interfaces so that no derived class or 
    * interface appears after a base class or interface.
    */
   protected static void arraysort(Object[] a) 
   {
      int len = a.length;

      for (int i = 0; i < len - 1; i++) {
         for (int j = i + 1; j < len; j++) {
            if (((Class)a[i]).isAssignableFrom((Class)a[j])) {
               Object tmp = a[i];
               a[i] = a[j];
               a[j] = tmp;
            }
         }
      }
   }

   /**
    * Creates a null id. The null id is the "reference data" field embedded
    * into the CORBA references for session beans. 
    */
   protected static byte[] createNullId()
   {
      byte[] nullId = null;
      try {
         nullId = toByteArray(new Integer(0));
      }
      catch (Exception e) {
      }
      return nullId;
   }

   /**
    * Receives an object and converts it into a byte array. Used to embed
    * a JBoss oid into the "reference data" (object id) field of a CORBA 
    * reference.
    */
   protected static byte[] toByteArray(Object obj) 
   {
      try {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         ObjectOutputStream oos = new CustomObjectOutputStream(os);

         oos.writeObject(obj);
         oos.flush();
         byte[] a = os.toByteArray();
         os.close();
         return a;
      }
      catch (IOException ioe) {
         throw new RuntimeException("Object id serialization error:\n" + ioe);
      }
   }

   /**
    * Receives a byte array previously returned by a call to 
    * <code>toByteArray</code> and retrieves an object from it. Used to
    * extract a JBoss oid from the "reference data" (object id) field of a
    * CORBA reference. 
    */
   protected static Object toObject(byte[] a) 
         throws IOException, ClassNotFoundException 
   {
      ByteArrayInputStream is = new ByteArrayInputStream(a);
      ObjectInputStream ois = new CustomObjectInputStreamWithClassloader(is,
                               Thread.currentThread().getContextClassLoader());
      Object obj = ois.readObject();
      is.close();
      return obj;
   }

   // Inner class for the EJBHome servant--------------------------------------

   private class HomeServant 
         extends Servant
         implements InvokeHandler, LocalInvoker {

      // This method overrides the one in org.omg.PortableServer.Servant ------

      /**
       * Returns an IR object describing the bean's home interface.
       */
      public org.omg.CORBA.Object _get_interface_def()
      {
         if (homeInterfaceDef != null)
            return homeInterfaceDef;
         else
            return super._get_interface_def();
      }
   
      // Implementation of the interface InvokeHandler ------------------------

      /**
       * Returns an array with the CORBA repository ids of the RMI-IDL 
       * interfaces implemented by the container's <code>EJBHome</code>.
       */
      public String[] _all_interfaces(POA poa, byte[] objectId) 
      {
         return (String[])homeRepositoryIds.clone();
      }

      /**
       * Receives IIOP requests to the the container's <code>EJBHome</code>
       * and forwards them to the container.
       */
      public OutputStream _invoke(String opName,
                                  InputStream in,
                                  ResponseHandler handler) 
      {
         if (logger.isTraceEnabled()) {
            logger.trace("EJBHome invocation: " + opName);
         }

         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(
               container.getClassLoader());

         try {

            SkeletonStrategy op = 
               (SkeletonStrategy) homeMethodInvokerMap.get(opName);
            if (op == null) {
               throw new BAD_OPERATION(opName);
            }

            org.omg.CORBA_2_3.portable.OutputStream out;
            try {
               Object retVal;
               
               // The EJBHome method getHomeHandle() receives special 
               // treatment because the container does not implement it. 
               // The remaining EJBObject methods (getEJBMetaData, 
               // remove(java.lang.Object), and remove(javax.ejb.Handle))
               // are forwarded to the container.

               if (opName.equals("_get_homeHandle")) {
                  retVal = new HomeHandleImpl(_this_object());
               }
               else {
                  Object[] params = op.readParams(
                                  (org.omg.CORBA_2_3.portable.InputStream)in);
                  Invocation inv = new Invocation(null, 
                                                  op.getMethod(), 
                                                  params,
                                                  null, /* tx */
                                                  null, /* identity */
                                                  null  /* credential*/);
                  inv.setType(Invocation.HOME);
                  retVal = container.invokeHome(inv);
               }
               out = (org.omg.CORBA_2_3.portable.OutputStream) 
                  handler.createReply();
               if (op.isNonVoid()) {
                  op.writeRetval(out, retVal);
               }
            }
            catch (Exception e) {
               if (logger.isTraceEnabled()) {
                  logger.trace("Exception in EJBHome invocation", e);
               }
               out = (org.omg.CORBA_2_3.portable.OutputStream) 
                  handler.createExceptionReply();
               op.writeException(out, e);
            }
            return out;
         }
         finally {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
      }

      // Implementation of the interface LocalInvoker -------------------------

      public Object invoke(String opName,
                    Object[] arguments, 
                    Transaction tx, 
                    Principal identity, 
                    Object credential)
         throws Exception
      {
         if (logger.isTraceEnabled()) {
            logger.trace("EJBHome local invocation: " + opName);
         }
         
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(
                                                   container.getClassLoader());
         
         try {
            SkeletonStrategy op = 
               (SkeletonStrategy) homeMethodInvokerMap.get(opName);
            if (op == null) {
               throw new BAD_OPERATION(opName);
            }
            
            Invocation inv = new Invocation(null, 
                                            op.getMethod(), 
                                            arguments,
                                            null, /* tx */
                                            null, /* identity */
                                            null  /* credential */);
            inv.setType(Invocation.LOCALHOME);
            return container.invokeHome(inv);
         }
         finally {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
      }
   }

}
