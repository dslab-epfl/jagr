//
// $Id: ServerImpl.java,v 1.8 2003/03/01 07:02:27 candea Exp $
//

/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.system.server;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MBeanException;

import org.jboss.Version;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.UnifiedClassLoader;
import org.jboss.mx.server.ServerConstants;
import org.jboss.net.protocol.URLStreamHandlerFactory;
import org.jboss.util.jmx.JMXExceptionDecoder;
import org.jboss.util.jmx.ObjectNameFactory;
import org.jboss.util.file.FileSuffixFilter;

/**
 * The main container component of a JBoss server instance.
 *
 * <h3>Concurrency</h3>
 * This class is <b>not</b> thread-safe.
 *
 * @jmx:mbean name="jboss.system:type=Server"
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.8 $
 */
public class ServerImpl
   implements Server, ServerImplMBean
{
   private final static ObjectName DEFAULT_LOADER_NAME = ObjectNameFactory.create(ServerConstants.DEFAULT_LOADER_NAME);
   
   /** 
    * Class logger.  We masqurade as Server for looks, but we log our
    * impl type too.
    */
   private static Logger log;

   /** Container for version information. */
   private final Version version = Version.getInstance();

   /** Package information for org.jboss */
   private final Package jbossPackage = Package.getPackage("org.jboss");

   /** The basic configuration for the server. */
   private ServerConfigImpl config;
   
   /** The JMX MBeanServer which will serve as our communication bus. */
   private MBeanServer server;

   /** The main deployer object name, replace with proxy once deployment moved to JBoss/System */
   private ObjectName mainDeployer;

   /** When the server was started. */
   private Date startDate;
   
   /** Flag to indicate if we are started. */
   private boolean started;

   /** The JVM shutdown hook */
   private ShutdownHook shutdownHook;

   /**
    * No-arg constructor for {@link ServerLoader}.
    */
   public ServerImpl()
   {
      super();
   }
   
   /**
    * Initialize the Server instance.
    *
    * @param props     The configuration properties for the server.
    * @return          Typed server configuration object.
    *
    * @throws IllegalStateException    Already initialized.
    * @throws Exception                Failed to initialize.
    */
   public void init(final Properties props) throws IllegalStateException, Exception
   {
      if (props == null)
         throw new IllegalArgumentException("props is null");
      if (config != null)
         throw new IllegalStateException("already initialized");

      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      
      try
      {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         doInit(props);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

   /** Actually does the init'ing... */
   private void doInit(final Properties props) throws Exception
   {
      // Create a new config object from the given properties
      this.config = new ServerConfigImpl(props);
      // Make sure jboss.server.home.dir is set for boot.log logging
      config.getServerHomeDir();

      log = Logger.getLogger(Server.class);

      // make sure our impl type is exposed
      log.debug("server type: " + getClass());

      // Show what release this is...
      log.info("JBoss Release: " + jbossPackage.getImplementationTitle());

      // Install a URLStreamHandlerFactory that uses the TCL
      URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory());

      // Preload JBoss URL handlers
      URLStreamHandlerFactory.preload();

      // Include the default JBoss protocol handler package
      String handlerPkgs = System.getProperty("java.protocol.handler.pkgs");
      if (handlerPkgs != null)
      {
         handlerPkgs += "|org.jboss.net.protocol";
      }
      else
      {
         handlerPkgs = "org.jboss.net.protocol";
      }
      System.setProperty("java.protocol.handler.pkgs", handlerPkgs);

      log.debug("Using config: " + config);

      // Log the basic configuration elements
      log.info("Home Dir: " + config.getHomeDir());
      log.info("Home URL: " + config.getHomeURL());
      log.info("Library URL: " + config.getLibraryURL());
      log.info("Patch URL: " + config.getPatchURL());
      log.info("Server Name: " + config.getServerName());
      log.info("Server Home Dir: " + config.getServerHomeDir());
      log.info("Server Home URL: " + config.getServerHomeURL());
      log.info("Server Data Dir: " + config.getServerDataDir());
      log.info("Server Temp Dir: " + config.getServerTempDir());
      log.info("Server Config URL: " + config.getServerConfigURL());
      log.info("Server Library URL: " + config.getServerLibraryURL());
      log.info("Root Deployemnt Filename: " + config.getRootDeploymentFilename());
   }

   /**
    * Get the typed server configuration object which the
    * server has been initalized to use.
    *
    * @return          Typed server configuration object.
    *
    * @throws IllegalStateException    Not initialized.
    */
   public ServerConfig getConfig() throws IllegalStateException
   {
      if (config == null)
         throw new IllegalStateException("not initialized");

      return config;
   }

   /**
    * Check if the server is started.
    *
    * @return   True if the server is started, else false.
    */
   public boolean isStarted()
   {
      return started;
   }

   /**
    * Start the Server instance.
    *
    * @throws IllegalStateException    Already started or not initialized.
    * @throws Exception                Failed to start.
    */
   public void start() throws IllegalStateException, Exception 
   {
      // make sure we are initialized
      getConfig();

      // make sure we aren't started yet
      if (started)
         throw new IllegalStateException("already started");

      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();

      try
      {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

         // Deal with those pesky JMX throwables
         try
         {
            doStart();
         }
         catch (Exception e)
         {
            JMXExceptionDecoder.rethrow(e);
         }
      }
      catch (Throwable t)
      {
         log.error("start failed", t);
         if (t instanceof Exception)
            throw (Exception)t;
         if (t instanceof Error)
            throw (Error)t;
         
         throw new org.jboss.util.UnexpectedThrowable(t);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }

      // CANDEA begin
      //        log.info("Injecting latent fault...");
      //        ObjectName name = new ObjectName("jboss:type=FaultInjector");
      //        String[] sig = {};
      //        Object[] opArgs = {};
      //        Object result = server.invoke(name, "startInjection", opArgs, sig);
      //        log.info("Giving the load generator a green light...");
      //        File greenlight = new File("/tmp/loadgen.go");
      //        greenlight.createNewFile();
      // CANDEA end
   }

   /** Actually does the starting... */
   private void doStart() throws Exception 
   {
      // remeber when we we started
      startDate = new Date();
      log.info("Starting General Purpose Architecture (GPA)...");
      
      // Create the MBeanServer
      server = MBeanServerFactory.createMBeanServer("jboss");
      log.debug("Created MBeanServer: " + server);
      
      // Register server components
      server.registerMBean(this, ServerImplMBean.OBJECT_NAME);
      server.registerMBean(config, ServerConfigImplMBean.OBJECT_NAME);
      
      
      // Initialize spine boot libraries
      UnifiedClassLoader ucl = initBootLibraries();
      
      //Set ServiceClassLoader as classloader for the construction of
      //the basic system
      Thread.currentThread().setContextClassLoader(ucl);

      // General Purpose Architecture information
      server.createMBean("org.jboss.system.server.ServerInfo", null);
      
      // Service Controller
      ObjectName controllerName =
         server.createMBean("org.jboss.system.ServiceController", null).getObjectName();
      log.debug("Registered service controller: " + controllerName);
      
      // Main Deployer
      mainDeployer =
         server.createMBean("org.jboss.deployment.MainDeployer",
                            null,
                            new Object[] {controllerName},
                            new String[] {ObjectName.class.getName()}).getObjectName();

      // Initialize the MainDeployer
      server.invoke(controllerName,
                    "create",
                    new Object[] { mainDeployer },
                    new String[] { ObjectName.class.getName() });

      server.invoke(controllerName,
                    "start",
                    new Object[] { mainDeployer },
                    new String[] { ObjectName.class.getName() });
      
      // Install the shutdown hook
      shutdownHook = new ShutdownHook(controllerName);
      shutdownHook.setDaemon(true);
      
      try
      {
         Runtime.getRuntime().addShutdownHook(shutdownHook);
         log.debug("Shutdown hook added");
      }
      catch (Exception e)
      {
         log.warn("Failed to add shutdown hook", e);
      }
      
      ObjectName objectName;
      
      // Jar Deployer
      objectName = server.createMBean("org.jboss.deployment.JARDeployer", null).getObjectName();
      initService(controllerName, objectName);

      // SAR Deployer
      objectName = server.createMBean("org.jboss.deployment.SARDeployer", null).getObjectName();
      initService(controllerName, objectName);

      log.info("Core system initialized");

      // TODO: Split up init (ie. create) from start ops so we can expose more control
      //       to embeded clients.

      // Ok, now deploy the root deployable to finish the job

      server.invoke(mainDeployer,
                    "deploy",
                    new Object[] { config.getServerConfigURL() + config.getRootDeploymentFilename() },
                    new String[] { String.class.getName() });

      // Create a simple life thread, with out it we might shutdown right away
      new Thread("JBoss Life Thread")
      {
         Object lock = new Object();
         
         public void run() {
            synchronized (lock) {
               try {
                  lock.wait();
               }
               catch (InterruptedException ignore) {}
            }
         }
      }.start();
      
      // Calculate how long it took
      long lapsedTime = System.currentTimeMillis() - startDate.getTime();
      long minutes = lapsedTime / 60000;
      long seconds = (lapsedTime - 60000 * minutes) / 1000;
      long milliseconds = (lapsedTime - 60000 * minutes - 1000 * seconds);

      // Tell the world how fast it was =)
      log.info("JBoss (MX MicroKernel) [" + jbossPackage.getImplementationVersion() +               
               "] Started in " + minutes  + "m:" + seconds  + "s:" + milliseconds +"ms");

      started = true;
   }

   /** Perform create/start on the given object name. */
   private void initService(final ObjectName controllerName, final ObjectName name) 
      throws Exception
   {
      server.invoke(controllerName,
                    "create",
                    new Object[] { name },
                    new String[] { ObjectName.class.getName() });

      server.invoke(controllerName,
                    "start",
                    new Object[] { name },
                    new String[] { ObjectName.class.getName() });
   }

   /**
    * Initialize the boot libraries.
    */
   private UnifiedClassLoader initBootLibraries() throws Exception
   {
      boolean debug = log.isDebugEnabled();
      
      // Build the list of URL for the spine to boot
      List list = new ArrayList();
      
      // Add the patch URL.  If the url protocol is file, then
      // add the contents of the directory it points to
      URL patchURL = config.getPatchURL();
      if (patchURL != null)
      {
         if (patchURL.getProtocol().equals("file"))
         {
            File dir = new File(patchURL.getFile());
            if (dir.exists())
            {
               // Add the local file patch directory
               list.add(dir.toURL());
               
               // Add the contents of the directory too
               File[] jars = dir.listFiles(new FileSuffixFilter(new String[] { ".jar", ".zip" }, true));
               
               for (int j = 0; jars != null && j < jars.length; j++)
               {
                  list.add(jars[j].getCanonicalFile().toURL());
               }
            }
         }
         else
         {
            list.add(patchURL);
         }
      }
      
      // Add the server configuration directory to be able to load config files as resources
      list.add(config.getServerConfigURL());

      // Not needed, ServerImpl will have the basics on its classpath from ServerLoader
      // may want to bring this back at some point if we want to have reloadable core
      // components...

      // URL libraryURL = config.getLibraryURL();
      // list.add(new URL(libraryURL, "jboss-spine.jar"));

      log.debug("Boot url list: " + list);

      // Create loaders for each URL
      UnifiedClassLoader loader = null;
      for (Iterator iter = list.iterator(); iter.hasNext();)
      {
         URL url = (URL)iter.next();
         if (debug)
         {
            log.debug("Creating loader for URL: " + url);
         }
         
         // This is a boot URL, so key it on itself.
         Object[] args = {url, Boolean.TRUE};
         String[] sig = {"java.net.URL", "boolean"};
         loader = (UnifiedClassLoader) server.invoke(DEFAULT_LOADER_NAME,
            "newClassLoader", args, sig);
      }
      return loader;
   }

   /**
    * Shutdown the Server instance and run shutdown hooks.  
    *
    * <p>If the exit on shutdown flag is true, then {@link #exit} 
    *    is called, else only the shutdown hook is run.
    *
    * @jmx:managed-operation
    *
    * @throws IllegalStateException    No started.
    */
   public void shutdown() throws IllegalStateException
   {
      if (!started)
         throw new IllegalStateException("not started");

      final ServerImpl server = this;
      
      log.info("Shutting down");
      
      boolean exitOnShutdown = config.getExitOnShutdown();
      if (log.isDebugEnabled())
      {
         log.debug("exitOnShutdown: " + exitOnShutdown);
      }
      
      if (exitOnShutdown)
      {
         server.exit(0);
      }
      else
      {
         // start in new thread to give positive
         // feedback to requesting client of success.
         new Thread()
         {
            public void run()
            {
               // just run the hook, don't call System.exit, as we may
               // be embeded in a vm that would not like that very much
               shutdownHook.shutdown();
            }
         }.start();
      }
   }
   
   /**
    * Shutdown the server, the JVM and run shutdown hooks.
    *
    * @jmx:managed-operation
    *
    * @param exitcode   The exit code returned to the operating system.
    */
   public void exit(final int exitcode)
   {
      // start in new thread so that we might have a chance to give positive
      // feed back to requesting client of success.
      new Thread()
      {
         public void run()
         {
            log.info("Shutting down the JVM now!");
            Runtime.getRuntime().exit(exitcode);
         }
      }.start();
   }
   
   /**
    * Shutdown the server, the JVM and run shutdown hooks.  Exits with
    * code 1.
    *
    * @jmx:managed-operation
    */
   public void exit()
   {
      exit(1);
   }
   
   /**
    * Forcibly terminates the currently running Java virtual machine.
    *
    * @param exitcode   The exit code returned to the operating system.
    *
    * @jmx:managed-operation
    */
   public void halt(final int exitcode)
   {
      // start in new thread so that we might have a chance to give positive
      // feed back to requesting client of success.
      new Thread()
      {
         public void run()
         {
            System.err.println("Halting the system now!");
            Runtime.getRuntime().halt(exitcode);
         }
      }.start();
   }
   
   /**
    * Forcibly terminates the currently running Java virtual machine.
    * Exits with code 1.
    *
    * @jmx:managed-operation
    */
   public void halt()
   {
      halt(1);
   }
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                            Runtime Access                             //
   ///////////////////////////////////////////////////////////////////////////
   
   /** A simple helper used to log the Runtime memory information. */ 
   private void logMemoryUsage(final Runtime rt)
   {
      log.info("Total/free memory: " + rt.totalMemory() + "/" + rt.freeMemory());
   }
   
   /**
    * Hint to the JVM to run the garbage collector.
    *
    * @jmx:managed-operation
    */
   public void runGarbageCollector()
   {
      Runtime rt = Runtime.getRuntime();
      
      logMemoryUsage(rt);
      rt.gc();
      log.info("Hinted to the JVM to run garbage collection");
      logMemoryUsage(rt);
   }
   
   /**
    * Hint to the JVM to run any pending object finailizations.
    *
    * @jmx:managed-operation
    */
   public void runFinalization()
   {
      Runtime.getRuntime().runFinalization();
      log.info("Hinted to the JVM to run any pending object finalizations");
   }
   
   /**
    * Enable or disable tracing method calls at the Runtime level.
    *
    * @jmx:managed-operation
    */
   public void traceMethodCalls(final Boolean flag)
   {
      Runtime.getRuntime().traceMethodCalls(flag.booleanValue());
   }
   
   /**
    * Enable or disable tracing instructions the Runtime level.
    *
    * @jmx:managed-operation
    */
   public void traceInstructions(final Boolean flag)
   {
      Runtime.getRuntime().traceInstructions(flag.booleanValue());
   }
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                          Server Information                           //
   ///////////////////////////////////////////////////////////////////////////
   
   /**
    * @jmx:managed-attribute
    */
   public Date getStartDate()
   {
      return startDate;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getVersion()
   {
      return version.toString();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getVersionName()
   {
      return version.getName();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getBuildNumber()
   {
      return version.getBuildNumber();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getBuildID()
   {
      return version.getBuildID();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getBuildDate()
   {
      return version.getBuildDate();
   }
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                             Shutdown Hook                             //
   ///////////////////////////////////////////////////////////////////////////
   
   private class ShutdownHook
      extends Thread
   {
      /** The ServiceController which we will ask to shut things down with. */
      private ObjectName controllerName;
      //private ObjectName mainDeployerName;
      private boolean forceHalt = true;
      
      public ShutdownHook(final ObjectName controllerName/*, final ObjectName mainDeployerName*/)
      {
         super("JBoss Shutdown Hook");
         
         this.controllerName = controllerName;
         //this.mainDeployerName = mainDeployerName;

         String value = System.getProperty("jboss.shutdown.forceHalt", null);
         if (value != null) {
            forceHalt = new Boolean(value).booleanValue();
         }
      }
      
      public void run()
      {
         shutdown();
         
         // later bitch
         if (forceHalt) {
            System.out.println("Halting VM");
            Runtime.getRuntime().halt(0);
         }
      }

      public void shutdown()
      {
         log.info("Undeploying all packages");
         shutdownDeployments();

         log.info("Shutting down all services");
         System.out.println("Shutting down");
         
         // Make sure all services are down properly
         shutdownServices();
         
         log.info("Shutdown complete");
         System.out.println("Shutdown complete");
      }
      
      protected void shutdownDeployments()
      {
         try
         {
            // get the deployed objects from ServiceController
            server.invoke(mainDeployer,
                          "shutdown",
                          new Object[0],
                          new String[0]);
         }
         catch (Exception e)
         {
            Throwable t = JMXExceptionDecoder.decode(e);
            log.error("failed to shutdown deployer", t);
         }
      }

      /**
       * The <code>shutdownServices</code> method calls the one and only
       * ServiceController to shut down all the mbeans registered with it.
       */
      protected void shutdownServices()
      {
         try
         {
            // get the deployed objects from ServiceController
            server.invoke(controllerName,
                          "shutdown",
                          new Object[0],
                          new String[0]);
         }
         catch (Exception e)
         {
            Throwable t = JMXExceptionDecoder.decode(e);
            log.error("failed to shutdown", t);
         }
      }
   }
}
