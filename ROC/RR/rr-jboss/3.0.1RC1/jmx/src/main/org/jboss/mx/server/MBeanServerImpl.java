//
// $Id: MBeanServerImpl.java,v 1.9 2003/02/23 07:35:40 candea Exp $
//

/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.server;

import org.jboss.mx.capability.DispatcherFactory;
import org.jboss.mx.loading.LoaderRepository;
import org.jboss.mx.logging.Logger;
import org.jboss.mx.metadata.MBeanCapability;
import org.jboss.mx.server.registry.BasicMBeanRegistry;
import org.jboss.mx.server.registry.MBeanEntry;
import org.jboss.mx.server.registry.MBeanRegistry;
import org.jboss.mx.util.MBeanProxy;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.DefaultLoaderRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//         //// MIKECHEN: BEGIN ////
//  import javax.management.MBeanInfo;
//  import javax.management.MBeanOperationInfo;
//  import javax.management.MBeanParameterInfo;
//  import javax.management.ObjectName;
//  import javax.naming.InitialContext;
//  //// Ugly workaround, put lib/jboss-jmx.jar in your classpath when compiling. May need to take it out when you run.
//  import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
//  //import org.jboss.jmx.adaptor.control.Server;
//         //// MIKECHEN: END   ////


/**
 * MBean server implementation. This is the default server implementation
 * returned by the <tt>MBeanServerFactory</tt> class
 ({@link ServerConstants#DEFAULT_MBEAN_SERVER_CLASS DEFAULT_MBEAN_SERVER_CLASS}). <p>
 *
 * The MBean server behaviour can be further configured by setting the following
 * system properties: <ul>
 *    <li><tt>jbossmx.loader.repository.class</tt>
 ({@link ServerConstants#LOADER_REPOSITORY_CLASS_PROPERTY LOADER_REPOSITORY_CLASS_PROPERTY})</li>
 *    <li><tt>jbossmx.mbean.registry.class</tt>
 ({@link ServerConstants#MBEAN_REGISTRY_CLASS_PROPERTY MBEAN_REGISTRY_CLASS_PROPERTY})</li>
 *    <li><tt>jbossmx.required.modelmbean.class</tt>
 ({@link ServerConstants#REQUIRED_MODELMBEAN_CLASS_PROPERTY REQUIRED_MODELMBEAN_CLASS_PROPERTY})</li>
 * </ul>
 *
 * The loader repository is used for managing class loaders in the MBean server.
 * The default repository uses the <tt>BasicLoaderRepository</tt> implementation
 * ({@link ServerConstants#DEFAULT_LOADER_REPOSITORY_CLASS DEFAULT_LOADER_REPOSITORY_CLASS}).<p>
 *
 * The default registry is
 * ({@link ServerConstants#DEFAULT_MBEAN_REGISTRY_CLASS DEFAULT_MBEAN_REGISTRY_CLASS}).<p>
 *
 * The <tt>RequiredModelMBean</tt> uses <tt>XMBean</tt> implementation by default
 * ({@link ServerConstants#DEFAULT_REQUIRED_MODELMBEAN_CLASS DEFAULT_REQUIRED_MODELMBEAN_CLASS}).
 *
 * @see javax.management.MBeanServer
 * @see javax.management.modelmbean.RequiredModelMBean
 * @see org.jboss.mx.server.ServerConstants
 * @see org.jboss.mx.loading.LoaderRepository
 * @see org.jboss.mx.loading.BasicLoaderRepository
 * @see org.jboss.mx.modelmbean.XMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.9 $
 */
public class MBeanServerImpl
   implements MBeanServer, ServerConstants
{
   // Constants ------------------------------------------------------

   /**
    * No parameters array
    */
   private static final Object[] NOPARAMS = new Object[0];

   /**
    * No signature array
    */
   private static final String[] NOSIG = new String[0];

   // Attributes ----------------------------------------------------

   /**
    * Registry used by this server to map MBean object names to resource references.
    */
   protected MBeanRegistry registry                   = null;

   /**
    * Registry MBean, used for dynamic invocations on register/unregister
    */
   protected ObjectName registryName                  = null;

   /**
    * The notification listener proxies. It is a map of object names
    * to another map of listeners to another map of handback objects to
    * proxies. Phew!
    */
   private Map listenerProxies = Collections.synchronizedMap(new HashMap());

   /*---------- Start RR-specific attributes ---------------------------------*/

   public String badServiceName = null;
   public String badOperationName = null;
   public String exceptionType = null;

   /*---------- End RR-specific attributes -----------------------------------*/

   // Static --------------------------------------------------------

   /**
    * The logger
    */
   private static Logger log = Logger.getLogger(MBeanServerImpl.class);

   // Constructors --------------------------------------------------

   /**
    * Creates an MBean server implementation with a given default domain name and
    * registers the mandatory server delegate MBean to the server ({@link ServerConstants#MBEAN_SERVER_DELEGATE MBEAN_SERVER_DELEGATE}).
    *
    * @param defaultDomain default domain name
    */
   public MBeanServerImpl(String defaultDomain)
   {
      // Construct the registry
      String registryClass = System.getProperty(
            ServerConstants.MBEAN_REGISTRY_CLASS_PROPERTY,
            ServerConstants.DEFAULT_MBEAN_REGISTRY_CLASS
      );
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class clazz = cl.loadClass(registryClass);
         Constructor constructor = clazz.getConstructor(new Class[] { MBeanServer.class, String.class });
         this.registry = (MBeanRegistry)constructor.newInstance(new Object[] {this, defaultDomain});


         String loaderClassMBeanName = LoaderRepository.getDefaultLoaderRepository().getClass().getName() + "MBean";
         cl = LoaderRepository.getDefaultLoaderRepository().getClass().getClassLoader();
         Class mbean = cl.loadClass(loaderClassMBeanName);
         //there must be a class with the MBean extension.
         ObjectName loaderName = new ObjectName(DEFAULT_LOADER_NAME);
         LoaderRepository repository = LoaderRepository.getDefaultLoaderRepository();

         registry.registerMBean(repository, loaderName, cl, JMI_DOMAIN);
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException("The MBean registry implementation class " + registryClass + " was not found: " + e.toString());
      }
      catch (NoSuchMethodException e) 
      {
         throw new IllegalArgumentException("The MBean registry implementation class " + registryClass + " must contain a default MBeanServer, domain string constructor: " + registryClass + "(javax.management.MBeanServer, java.lang.String defaultDomain)");
      }
      catch (InstantiationException e) 
      {
         throw new IllegalArgumentException("Cannot instantiate class " + registryClass + ": " + e.toString());
      }
      catch (IllegalAccessException e)
      {
         throw new IllegalArgumentException("Unable to create the MBean registry instance. Illegal access to class " + registryClass + " constructor: " + e.toString());
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException("Unable to create the MBean registry instance. Class " + registryClass + " has raised an exception in constructor: " + e.getTargetException().toString());
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException("The LoaderRepository name is not valid +: " + DEFAULT_LOADER_NAME);
      }
      catch (InstanceAlreadyExistsException e)
      {
         throw new RuntimeException("The LoaderRepository already exists");
      }
      catch (MBeanRegistrationException e)
      {
         throw new RuntimeException("The LoaderRepository registration failed");
      }
      catch (NotCompliantMBeanException e)
      {
         throw new RuntimeException("The LoaderRepository is not a valid MBean");
      }

      /**
       * Check for a mbean version of the registry
       */
      try
      {
          registryName = new ObjectName(MBEAN_REGISTRY);
          registry.get(registryName);
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException("The registry name is not valid +: " + MBEAN_REGISTRY);
      }
      catch (InstanceNotFoundException e)
      {
         // POJO Registry
         registryName = null;
      }
   }

   // MBeanServer implementation ------------------------------------

   public Object instantiate(String className) throws ReflectionException, MBeanException
   {
      return instantiate(className, (ClassLoader)null, NOPARAMS, NOSIG);
   }

   public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException
   {
      return instantiate(className, (ClassLoader)null, params, signature);
   }

   public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException
   {
      return instantiate(className, loaderName, NOPARAMS, NOSIG);
   }

   public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException
   {
      ClassLoader cl = null;
      
      // if instantiate() is called with null loader name, we use the cl that
      // loaded the MBean server (see javadoc)

      try
      {
         if (loaderName != null)
            cl = (ClassLoader)registry.get(loaderName).getResourceInstance();
      }
      catch (ClassCastException e)
      {
         throw new ReflectionException(e, loaderName + " is not a class loader.");
      }

      if (cl == null)
         cl = this.getClass().getClassLoader();

      return instantiate(className, cl, params, signature);
   }

   public ObjectInstance createMBean(String className, ObjectName name)
   throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
   {
      Object mbean = instantiate(className);
      return registerMBean(mbean, name, (ClassLoader)null);
   }

   public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
   throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
   {
      Object mbean = instantiate(className, params, signature);
      return registerMBean(mbean, name, (ClassLoader)null);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
   throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      Object mbean = instantiate(className, loaderName);
      return registerMBean(mbean, name, loaderName);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
   throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      Object mbean = instantiate(className, loaderName, params, signature);
      return registerMBean(mbean, name, loaderName);
   }

   public ObjectInstance registerMBean(Object object, ObjectName name)
      throws InstanceAlreadyExistsException,
             MBeanRegistrationException,
             NotCompliantMBeanException
   {
      return registerMBean(object, name, (ClassLoader)null);
   }

   public void unregisterMBean(ObjectName name)
      throws InstanceNotFoundException, MBeanRegistrationException
   {
      // Get the mbean to remove
      Object mbean = registry.get(name).getResourceInstance();

      // Dynamic Invocation
      if (registryName != null)
      {
         try
         {
            invoke(registryName, "unregisterMBean",
                   new Object[] { name },
                   new String[] { ObjectName.class.getName() }
            );
         }
         catch (Exception e)
         {
            Exception result = handleInvocationException(registryName, e);
            if (result instanceof InstanceNotFoundException)
               throw (InstanceNotFoundException) result;
            if (result instanceof MBeanRegistrationException)
               throw (MBeanRegistrationException) result;
            throw new RuntimeException(result.toString());
         }
      }
      else
         // POJO Registry
         registry.unregisterMBean(name);

      // Unregistration worked, remove any proxies for a broadcaster
      if (mbean instanceof NotificationBroadcaster)
         removeListenerProxies((NotificationBroadcaster) mbean, name);
   }

   public ObjectInstance getObjectInstance(ObjectName name)
      throws InstanceNotFoundException
   {
      return registry.getObjectInstance(name);
   }

   public Set queryMBeans(ObjectName name, QueryExp query)
   {
      // Set up the query
      Set result = new HashSet();
      if (query != null)
         query.setMBeanServer(this);

      // Get the possible MBeans
      List entries = registry.findEntries(name);
      Iterator iterator = entries.iterator();
      while (iterator.hasNext())
      {
         // Check each MBean against the query
         MBeanEntry entry = (MBeanEntry) iterator.next();
         ObjectName objectName = entry.getObjectName();
         if (queryMBean(objectName, query) == true)
         {
            try
            {
               ObjectInstance instance = registry.getObjectInstance(objectName);
               result.add(registry.getObjectInstance(objectName));
            }
            catch (InstanceNotFoundException ignored) {}
         }
      }

      return result;
   }

   public Set queryNames(ObjectName name, QueryExp query)
   {
      // Set up the query
      Set result = new HashSet();
      if (query != null)
         query.setMBeanServer(this);

      // Get the possible MBeans
      List entries = registry.findEntries(name);
      Iterator iterator = entries.iterator();
      while (iterator.hasNext())
      {
         // Check each MBean against the query
         MBeanEntry entry = (MBeanEntry) iterator.next();
         ObjectName objectName = entry.getObjectName();
         if (queryMBean(objectName, query) == true)
            result.add(objectName);
      }

      return result;
   }

   public boolean isRegistered(ObjectName name)
   {
      return registry.contains(name);
   }

   public java.lang.Integer getMBeanCount()
   {
      return new Integer(registry.getSize());
   }

   public Object getAttribute(ObjectName name, String attribute)
      throws MBeanException,
             AttributeNotFoundException,
             InstanceNotFoundException,
             ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      DynamicMBean mbean = entry.getMBean();

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         return mbean.getAttribute(attribute);
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public AttributeList getAttributes(ObjectName name, String[] attributes)
      throws InstanceNotFoundException,
             ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      DynamicMBean mbean = entry.getMBean();

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         return mbean.getAttributes(attributes);
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public void setAttribute(ObjectName name, Attribute attribute)
   throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      DynamicMBean mbean = entry.getMBean();

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         mbean.setAttribute(attribute);
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public AttributeList setAttributes(ObjectName name, AttributeList attributes)
   throws InstanceNotFoundException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      DynamicMBean mbean = entry.getMBean();

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         return mbean.setAttributes(attributes);
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   //
   // Schedule an exception injection
   //
   public void injectException( String serviceName, String operationName, String excType )
   {
      log.info("Scheduling injection of |" + excType + "| into |" + serviceName + "|:|" + operationName + "|");

      if (serviceName != null)
         badServiceName = serviceName;
      else 
         badServiceName = "<none>";

      if (operationName != null)
         badOperationName = operationName;
      else
         badOperationName = "<none>";

      if (excType != null)
         exceptionType = excType;
      else
         exceptionType = "<none>";
   }


	  
//      //// BEGIN: MIKECHEN ////
//      public void reportFailure(ObjectName name, String operationName, Throwable e) throws Exception {
//  	//// read in the stack trace
//  	java.io.StringWriter sw = new java.io.StringWriter();
//  	java.io.PrintWriter pw = new java.io.PrintWriter(sw);
//  	e.printStackTrace(pw);
//  	String stacktrace = new String(sw.getBuffer());
//  	java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(stacktrace));
	
//  	//// get the callee
//  	String callee = name.toString().substring(name.toString().indexOf("=")+1);
//  	String calleeMethod = operationName;
	
//  	//// skip to the first Container
//  	String line = br.readLine();
//  	while (line != null) {
//  	    if (line.indexOf("org.jboss.proxy.ClientContainer.invoke") != -1) 
//  		break;
//  	    line = br.readLine();
//  	}
	
//  	if (line != null) {
//  	    //// get the caller
//  	    line = br.readLine(); // at $Proxy71.handleEvent(Unknown Source)
//  	    line = br.readLine(); // actual caller
//  	    line = line.substring(line.indexOf("at ") + "at ".length());
//  	    line = line.substring(0, line.indexOf("("));
	    
//  	    // find the ejb and the method
//  	    String caller    = line.substring(0, line.lastIndexOf("."));
//  	    String callerMethod = line.substring(line.lastIndexOf(".")+1);
	    
//  	    //// report the failure to failure monitor
//  	    System.err.println("MBean Exception: caller=" + caller + ", method=" + callerMethod + ", callee=" + callee + ", method = " + calleeMethod);
	    
//  	    InitialContext ic = new InitialContext();
//  	    String serverName = java.net.InetAddress.getLocalHost().getHostName();		    
//  	    org.jboss.jmx.adaptor.rmi.RMIAdaptor server = (org.jboss.jmx.adaptor.rmi.RMIAdaptor) ic.lookup("jmx:" + serverName + ":rmi");
	    
//  	    ObjectName objName = new ObjectName("jboss:type=FailureMonitor");
//  	    String[] sig = {"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.Throwable"};
//  	    Object[] opArgs = {caller, callerMethod, callee, calleeMethod, e};
//  	    Object result = server.invoke(objName, "reportFailure", opArgs, sig);
	    
//  	}

//  	// 
//  	  /*
//  	  // sample trace 
	    
//  	    18:42:41,075 ERROR [STDERR] objectname: jboss:type=FailureMonitor, operationName=reportFailure
//  18:42:41,075 ERROR [STDERR] java.lang.Exception
//  18:42:41,075 ERROR [STDERR]     at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:557)
//  18:42:41,075 ERROR [STDERR]     at org.jboss.jmx.adaptor.rmi.RMIAdaptorImpl.invoke(RMIAdaptorImpl.java:284)
//  18:42:41,075 ERROR [STDERR]     at org.jboss.proxy.ClientContainer.invoke(ClientContainer.java:192)
//  18:42:41,075 ERROR [STDERR]     at $Proxy71.handleEvent(Unknown Source)
//  18:42:41,075 ERROR [STDERR]     at com.sun.j2ee.blueprints.petstore.control.web.ShoppingClientControllerWebImpl.handleEvent(ShoppingClientControllerWebImpl.java:140)
//  	  */
//      }
//      //// END: MIKECHEN ////



   public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException
      // CANDEA start
      , Exception
      // CANDEA end
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      DynamicMBean mbean = entry.getMBean();

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();

      // log.info("+++ Invoked svc " + name.toString() + " method " + operationName);
      //
      // First we check whether an exception is to be injected
      //

      /*
      try {
	  throw new Exception();
      }
      catch (Throwable e) {
	  System.err.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
	  System.err.println("objectname: " + name + ", operationName=" + operationName);
	  reportFailure(name, operationName, e);
	  e.printStackTrace();
	  System.err.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  ");
      }
      */

      if (badServiceName!=null  &&  operationName!=null)
      {
         if (badServiceName.equals(name.toString()) && badOperationName.equals(operationName))
         {
            log.info("+++ Injecting exc |" + exceptionType + "| into |" + badServiceName + "|:|" + badOperationName + "|");
            badServiceName = null;
            badOperationName = null;
// MIKECHEN
// 	    Exception e = new Exception(exceptionType);
//  	    reportFailure(name, operationName, e);
//              throw (e);
            throw (new Exception(exceptionType));
// MIKECHEN END
         }
      }
      
      //
      // Now do the real invocation work
      //
      // log.info("+++     doing the real invocation work");
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

	 // log.info("+++         returning mbean.invoke");
         //return mbean.invoke(operationName, params, signature);
	 //// BEGIN: MIKECHEN ////

//  	 try {
//  	     Object result = mbean.invoke(operationName, params, signature);
//  	     return result;
//  	 }
//  	 catch (Exception e) {
//  	     reportFailure(name, operationName, e);
//  	     throw e;
//  	 }

         return mbean.invoke(operationName, params, signature);
	 //// END: MIKECHEN ////
      }
      catch (Exception e) {
	  // log.info("+++         got exception " + e.toString() );
	  // e.printStackTrace();
	  throw e;
      }
      finally
      {
	  // log.info("+++            FINALLY");
	  if (newTCL != oldTCL && newTCL != null)
	      thread.setContextClassLoader(oldTCL);
      }
   }

   public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      DynamicMBean mbean = entry.getMBean();

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         return mbean.getMBeanInfo();
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public String getDefaultDomain()
   {
      return registry.getDefaultDomain();
   }

   public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
   throws InstanceNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      NotificationBroadcaster broadcaster = null;
      try
      {
         broadcaster = (NotificationBroadcaster)entry.getResourceInstance();
      }
      catch (ClassCastException e)
      {
         throw new RuntimeOperationsException(e, "MBean " + name + " does not implement the NotificationBroadcaster interface.");
      }

      NotificationListener proxy = createListenerProxy(entry.getObjectName(),
                                                       listener, handback);

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         broadcaster.addNotificationListener(proxy, filter, handback);
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
   throws InstanceNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      NotificationBroadcaster broadcaster = null;
      try
      {
         broadcaster = (NotificationBroadcaster)entry.getResourceInstance();
      }
      catch (ClassCastException e)
      {
         throw new RuntimeOperationsException(e, "MBean " + listener + " is not a notification listener or " + name + " does not implement notification broadcaster interface.");
      }

      NotificationListener proxy = createListenerProxy(entry.getObjectName(), 
         (NotificationListener)registry.get(listener).getResourceInstance(),
          handback);

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         broadcaster.addNotificationListener(proxy, filter, handback);
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public void removeNotificationListener(ObjectName name, NotificationListener listener)
   throws InstanceNotFoundException, ListenerNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      NotificationBroadcaster broadcaster = null;
      try
      {
         broadcaster = (NotificationBroadcaster)entry.getResourceInstance();
      }
      catch (ClassCastException e)
      {
         throw new RuntimeOperationsException(e, "MBean " + name + " does not implement the NotificationBroadcaster interface.");
      }

      Iterator proxies = removeListenerProxies(entry.getObjectName(), listener);

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         // REVIEW: Try to remove all before throwing an exception?
         while (proxies.hasNext())
            broadcaster.removeNotificationListener(
               (NotificationListener) proxies.next());
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public void removeNotificationListener(ObjectName name, ObjectName listener)
   throws InstanceNotFoundException, ListenerNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      ClassLoader newTCL = entry.getClassLoader();
      NotificationBroadcaster broadcaster = null;
      try
      {
         broadcaster = (NotificationBroadcaster)entry.getResourceInstance();
      }
      catch (ClassCastException e)
      {
         throw new RuntimeOperationsException(e, "MBean " + name + " does not implement the NotificationBroadcaster interface.");
      }

      Iterator proxies = removeListenerProxies(entry.getObjectName(),
         (NotificationListener)registry.get(listener).getResourceInstance());

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(newTCL);

         // REVIEW: Try to remove all before throwing an exception?
         while (proxies.hasNext())
            broadcaster.removeNotificationListener(
               (NotificationListener) proxies.next());
      }
      finally
      {
         if (newTCL != oldTCL && newTCL != null)
            thread.setContextClassLoader(oldTCL);
      }
   }

   public boolean isInstanceOf(ObjectName name, String className)
      throws InstanceNotFoundException
   {
      // REVIEW: TCL required?
      Object resourceInstance = registry.get(name).getResourceInstance();
      try
      {
        Class clazz = DefaultLoaderRepository.loadClass(className);
        return clazz.isInstance(resourceInstance);
      }
      catch (ClassNotFoundException cfe)
      {
        return false;
      }
   }

   public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException
   {
      try
      {
         // REVIEW: Should this be the beans TCL?
         ClassLoader cl = registry.get(name).getResourceInstance().getClass().getClassLoader();
         return new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(data), cl);
      }
      catch (IOException e)
      {
         throw new OperationsException("I/O exception deserializing: " + e.getMessage());
      }
   }

   public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException
   {
      try
      {
         // REVIEW: Should this be the beans TCL?
         Class c = DefaultLoaderRepository.loadClass(className);
         ClassLoader cl = c.getClassLoader();
         return new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(data), cl);
      }
      catch (IOException e)
      {
         throw new OperationsException("I/O exception deserializing: " + e.getMessage());
      }
      catch (ClassNotFoundException e)
      {
         throw new ReflectionException(e, "Class not found from default repository: " + className);
      }
   }

   public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data)
   throws InstanceNotFoundException, OperationsException, ReflectionException
   {
      try
      {
         ClassLoader cl = (ClassLoader)registry.get(loaderName).getResourceInstance();
         return new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(data), cl);
      }
      catch (IOException e)
      {
         throw new OperationsException("I/O exception deserializing: " + e.getMessage());
      }
   }

   // Protected -----------------------------------------------------

   /**
    * Instantiate an object, the passed classloader is set as the
    * thread's context classloader for the duration of this method.
    *
    * @param className the class name of the object to instantiate
    * @param cl the thread classloader, pass null to use the
    *        DefaultLoaderRepository
    * @param param the parameters for the constructor
    * @param signature the signature of the constructor
    * @exception ReflectionException wraps a ClassCastException or
    *            any Exception trying to invoke the constructor
    * @expeption MBeanException wraps any exception thrown by the constructor
    * @exception RuntimeOperationsException Wraps an IllegalArgument for a
    *            null className
    */
   protected Object instantiate(String className, ClassLoader cl, Object[] params, String[] signature) throws ReflectionException, MBeanException
   {
      if (className == null)
         throw new RuntimeOperationsException(new IllegalArgumentException(
                   "Null className"));

      Thread thread = Thread.currentThread();
      ClassLoader oldTCL = thread.getContextClassLoader();
      try
      {
         Class clazz = null;
         if (cl != null)
         {
            if (cl != oldTCL)
               thread.setContextClassLoader(cl);
            clazz = cl.loadClass(className);
         }
         else
            clazz = DefaultLoaderRepository.loadClass(className);

         Class[] sign = new Class[signature.length];
         for (int i = 0; i < signature.length; ++i)
         {
            try
            {
               if (cl != null)
                  sign[i] = cl.loadClass(signature[i]);
               else
                  sign[i] = DefaultLoaderRepository.loadClass(signature[i]);
            }
            catch (ClassNotFoundException e)
            {
               // FIXME: should we delegate to DLR before throwing the exception?
               throw new ReflectionException(e, "Constructor parameter class not found: " + signature[i]);
            }
         }
         Constructor constructor = clazz.getConstructor(sign);
         return constructor.newInstance(params);
      }
      catch (Throwable t)
      {
         handleInstantiateExceptions(t, className);

         log.error("Unhandled exception instantiating class: " + className, t);

         return null;
      }
      finally
      {
         if (cl != null && cl != oldTCL)
            thread.setContextClassLoader(oldTCL);
      }
   }

   /**
    * Handles errors thrown during class instantiation
    */
   protected void handleInstantiateExceptions(Throwable t, String className) throws ReflectionException, MBeanException
   {
      if (t instanceof ClassNotFoundException)
         throw new ReflectionException((Exception)t, "Class not found: " + className);

      else if (t instanceof InstantiationException)
         throw new ReflectionException((Exception)t, "Cannot instantiate with no-args constructor: "  + className);

      else if (t instanceof IllegalAccessException)
         throw new ReflectionException((Exception)t, "Illegal access to default constructor: "  + className);

      else if (t instanceof NoSuchMethodException)
         throw new ReflectionException((Exception)t, className + " does not have a public no args constructor.");

      else if (t instanceof SecurityException)
         throw new ReflectionException((Exception)t, "Can't access default constructor for " + className + ": " + t.toString());

      else if (t instanceof InvocationTargetException)
      {
         Throwable root = ((InvocationTargetException)t).getTargetException();

         if (root instanceof RuntimeException)
            throw new RuntimeMBeanException((RuntimeException)root, className + " constructor has thrown an exception: " + root.toString());
         else if (root instanceof Error)
            throw new RuntimeErrorException((Error)root, className + " constructor has thrown an error: " + root.toString());
         else if (root instanceof Exception)
            throw new MBeanException((Exception)root, className + " constructor has thrown an exception: " + root.toString());

         throw new Error("Something went wrong with handling the exception from " + className + " default constructor.");
      }

      else if (t instanceof ExceptionInInitializerError)
      {
         Throwable root = ((ExceptionInInitializerError)t).getException();

         // the root cause can be only a runtime exception
         if (root instanceof RuntimeException)
            throw new RuntimeMBeanException((RuntimeException)root, "Exception in class " + className + " static initializer: " + root.toString());
         else
            // shouldn't get here
            throw new Error("ERROR: it turns out the root cause is not always a runtime exception!");
      }

      else if (t instanceof IllegalArgumentException)
      {
         // if mismatch between constructor instance args and supplied args -- shouldn't happen
         throw new Error("Error in the server: mismatch between expected constructor arguments and supplied arguments.");
      }

      else if (t instanceof Error)
      {
         throw new RuntimeErrorException((Error)t, "instantiating " + className + " failed: " + t.toString());
      }
   }


   /**
    * Register an MBean<p>
    *
    * The classloader is used as the thread context classloader during
    * access to the mbean and it's interceptors
    *
    * @param object the mbean to register
    * @param name the object name to register
    * @param loaderName the object name of a class loader also used as
    *        as the MBeans TCL
    * @param magicToken used to get access to the
    * @exception InstanceAlreadyExistsException when already registered
    * @exception MBeanRegistrationException when
    *            preRegister(MBeanServer, ObjectName) throws an exception
    * @exception NotCompliantMBeanException when the object is not an MBean
    * @exception RuntimeOperationException containing an
    *            IllegalArgumentException for another problem with the name
    *            or a null object
    */
   protected ObjectInstance registerMBean(Object mbean, ObjectName name, ObjectName loaderName)
   throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      ClassLoader cl = null;
      try
      {
         cl = (ClassLoader)registry.get(loaderName).getResourceInstance();
      }
      catch (ClassCastException e)
      {
         throw new ReflectionException(e, loaderName + " is not a class loader.");
      }
      if (cl == null)
         cl = this.getClass().getClassLoader();

      return registerMBean(mbean, name, cl);
   }

   /**
    * Register an MBean<p>
    *
    * The classloader is used as the thread context classloader during
    * access to the mbean and it's interceptors
    *
    * @param object the mbean to register
    * @param name the object name to register
    * @param cl the thread classloader, pass null for the current one
    * @exception InstanceAlreadyExistsException when already registered
    * @exception MBeanRegistrationException when
    *            preRegister(MBeanServer, ObjectName) throws an exception
    * @exception NotCompliantMBeanException when the object is not an MBean
    * @exception RuntimeOperationException containing an
    *            IllegalArgumentException for another problem with the name
    *            or a null object
    */
   protected ObjectInstance registerMBean(Object object, ObjectName name, 
                                          ClassLoader cl)
      throws InstanceAlreadyExistsException, 
             MBeanRegistrationException, 
             NotCompliantMBeanException
   {
      // Dynamic Invocation
      if (registryName != null)
      {
         try
         {
            return (ObjectInstance) invoke(registryName, "registerMBean",
                    new Object[] { object, name, cl, null },
                    new String[] { Object.class.getName(),
                                   ObjectName.class.getName(),
                                   ClassLoader.class.getName(),
                                   Object.class.getName() }
            );
         }
         catch (Exception e)
         {
            Exception result = handleInvocationException(registryName, e);
            if (result instanceof InstanceAlreadyExistsException)
               throw (InstanceAlreadyExistsException) result;
            if (result instanceof MBeanRegistrationException)
               throw (MBeanRegistrationException) result;
            if (result instanceof NotCompliantMBeanException)
               throw (NotCompliantMBeanException) result;
            throw new RuntimeException(result.toString());
         }
      }
      else
         // POJO Registry
         return registry.registerMBean(object, name, cl, null);
   }

   /**
    * Add a notification listener proxy
    *
    * @param name the broadcaster's object name
    * @param listener the original listener
    * @return a proxy notification listener
    * @exception IllegalArgumentException for a null listener
    */
   protected NotificationListener createListenerProxy(ObjectName name, 
                            NotificationListener listener, Object handback)
   {
      // Sanity check
      if (listener == null)
         throw new IllegalArgumentException("Null listener");

      NotificationListener result = null;

      // Retrieve any previous listener or construct the data structure
      HashMap listeners = (HashMap) listenerProxies.get(name);
      HashMap handbacks = null;
      if (listeners == null)
      {
         listeners = new HashMap();
         listenerProxies.put(name, listeners);
         handbacks = new HashMap();
         listeners.put(listener, handbacks);
      }
      else
      {
         handbacks = (HashMap) listeners.get(listener);
         if (handbacks == null)
         {
            handbacks = new HashMap();
            listeners.put(listener, handbacks);
         }
         else
         {
            result = (NotificationListener) handbacks.get(handback);
            if (result != null)
               return result;
         }
      }

      // Create a new proxy
      result = new NotificationListenerProxy(name, listener);
      handbacks.put(handback, result);
      return result;
   }

   /**
    * Remove notification listener proxies for a listener
    *
    * @param name the broadcaster's object name
    * @param listener the original listener
    * @return an iterator of notification listeners
    */
   protected Iterator removeListenerProxies(ObjectName name, 
                                            NotificationListener listener)
   {
      // See if we know this listener
      HashMap listeners = (HashMap) listenerProxies.get(name);
      if (listeners != null)
      {
         HashMap handbacks = (HashMap) listeners.remove(listener);
         if (handbacks != null && handbacks.size() != 0)
            return handbacks.values().iterator();
      }

      // Give the broadcaster chance to remove the original listener
      // REVIEW: Is this correct? Or do we just throw a not found?
      return Arrays.asList(new Object[]{ listener }).iterator();
   }

   /**
    * Remove notification listener proxies for a broadcaster
    *
    * @param broadcaster the broadcaster implementation
    * @param name the broadcaster's object name
    */
   protected void removeListenerProxies(NotificationBroadcaster broadcaster,
                                        ObjectName name)
   {
      // See if we know this broadcaster
      HashMap listeners = (HashMap) listenerProxies.remove(name);
      if (listeners == null)
         return;
      Iterator listener = listeners.values().iterator();
      while (listener.hasNext())
      {
         Iterator handback = ((HashMap) listener.next()).values().iterator();
         while (handback.hasNext())
         {
            NotificationListener original = (NotificationListener) handback.next();
            try
            {
               broadcaster.removeNotificationListener(original);
            }
            catch (Exception ignored)
            {
            }
         }
      }
   }

   // Private -------------------------------------------------------

   /**
    * Handles exceptions thrown by the implementation MBeans<p>
    *
    * Either returns a wrapped exception or throws a runtime exception
    *
    * @param name the ObjectName of the implementation invoked
    * @param e the exception thrown by the invocation
    * @return any wrapped exception
    */
   private Exception handleInvocationException(ObjectName name, Exception e)
   {
       // Return the wrapped exception
       if (e instanceof MBeanException)
       {
          return ((MBeanException) e).getTargetException();
       }
       // The following are runtime errors, normally caused by the user
       if  (e instanceof RuntimeOperationsException)
       {
          throw ((RuntimeOperationsException) e).getTargetException();
       }
       if (e instanceof ReflectionException)
       {
          Exception target = ((ReflectionException) e).getTargetException();
          if (target instanceof RuntimeException)
             throw (RuntimeException) target;
          else
             throw new RuntimeException(target.toString());
       }
       if (e instanceof RuntimeMBeanException)
       {
          throw ((RuntimeMBeanException) e).getTargetException();
       }
       if (e instanceof RuntimeErrorException)
       {
          throw ((RuntimeErrorException) e).getTargetError();
       }
       // Don't know what to do with this, wrap it in a runtime error
       throw new RuntimeException(e.toString());
   }

   /**
    * Query an MBean against the query
    *
    * @param objectName the object name of the mbean to check
    * @param queryExp the query expression to test
    * @return true when the query applies to the MBean or the query is null,
    *         false otherwise.
    */
    protected boolean queryMBean(ObjectName objectName, QueryExp queryExp)
    {
       if (queryExp == null)
          return true;

       try
       {
          return queryExp.apply(objectName);
       }
       catch (Exception e)
       {
// TODO Is this correct?
          return false;
       }
    }

   // Private -------------------------------------------------------
}

