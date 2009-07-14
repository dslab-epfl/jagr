/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: AxisService.java,v 1.1.1.1 2002/11/16 03:16:50 mikechen Exp $

package org.jboss.net.axis.server;

import org.jboss.net.axis.XMLResourceProvider;
import org.jboss.net.axis.Deployment;

import org.jboss.net.DefaultResourceBundle;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.deployment.SubDeployer;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.mx.loading.UnifiedClassLoader;
import org.jboss.web.WebApplication;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

import org.apache.axis.MessageContext;
import org.apache.axis.utils.XMLUtils;
import org.apache.axis.AxisFault;
import org.apache.axis.server.AxisServer;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.deployment.wsdd.WSDDUndeployment;

import org.jboss.naming.Util;
import org.jboss.metadata.MetaData;
import org.jboss.system.server.ServerConfigLocator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Context;
import javax.naming.NamingException;

import java.io.FilenameFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import java.util.Map;
import java.util.Iterator;
import java.util.Collection;

/**
 * A deployer service that installs Axis and manages Web-Services 
 * within JMX.
 * <br>
 * <h3>Change History</h3>
 * <ul>
 *  <li> jung, 09.03.02: axis alpha 3 and wsdd deployment is here.</li>
 *  <li> jung, 01.02.02: Adapted to jboss3.0.0DR1 deployment changes. Arrgh! <li>
 *  <li> jung, 14.12.01: Added security domain support. <li>
 *  <li> jung, 28.11.01: Adapted to jboss3.0 single-phase mbean startup. <li>
 *  <li> jung, 28.11.01: Fixed zip-cache problem (sort of). <li>
 * </ul>
 * @created 27. September 2001
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class AxisService
   extends SubDeployerSupport
   implements AxisServiceMBean, MBeanRegistration {

   // 
   // Attributes
   //

   /**
    * A map of current deployment names to the
    * wsdd docs that created them.
    */
   protected Map deployments = new java.util.HashMap();

   /** the name registered */
   protected ObjectName _name = null;

   /** this is where the axis "web-application" has been installed */
   protected DeploymentInfo myDeploymentInfo = null;

   /** the engine belonging to this service */
   protected AxisServer axisServer;

   /** the web deployer that hosts our servlet */
   protected SubDeployer webDeployer;

   //
   // Constructors
   //

   /** default */
   public AxisService() {
      // we fake internationalisation if it is not globally catered
      if (log.getCategory().getResourceBundle() == null)
         log.getCategory().setResourceBundle(new DefaultResourceBundle());
   }

   //
   // Some helper methods
   //

   /** work around broken JarURLConnection caching... thx Jules for the hack ;-) */
   private URL fixURL(URL url) throws MalformedURLException {
      String urlString = url.toString();
      //determine the last slash
      int index = urlString.lastIndexOf(":") + 1;
      // no slash, we take the last protocol or drive id
      if (index == 0) {
         // we cannot fix this
         return url;
      } else {
         return new URL(
            urlString.substring(0, index) + "/." + urlString.substring(index));
      }
   }

   /** returns the jboss deploy dir */
   protected String getDeployDir() throws Exception {
      File systemTempDir = ServerConfigLocator.locate().getServerTempDir();

      return new File(new File(systemTempDir, "deploy"), Constants.AXIS_DEPLOY_DIR)
         .getCanonicalPath();
   }

   /** a tiny copy operation */
   protected void copy(InputStream in, OutputStream out) throws IOException {

      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) > 0) {
         out.write(buffer, 0, read);
      }
   }

   //----------------------------------------------------------------------------
   // 'name' interface
   //----------------------------------------------------------------------------

   /** registers at the MBean server with a default or a preconfigured name */
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException {
      // accept name proposal from JMX in order
      // to allow several instances
      if (name == null) {
         _name =
            new ObjectName(Constants.DOMAIN + ":" + Constants.TYPE + "=" + getName());
      } else {
         _name = name;
      }
      return _name;
   }

   /** return just the proper name part */
   public String getName() {
      return Constants.NAME;
   }

   //----------------------------------------------------------------------------
   // 'service' interface
   //----------------------------------------------------------------------------

   /** starts the associated AxisEngine */
   protected void ensureService() throws Exception {

      synchronized (AxisService.class) {
         // each engine must have a different context
         if (axisServerMap.get(rootContext) != null)
            throw new Exception(Constants.AXIS_SERVER_CONTEXT_OCCUPIED);

         // find the global config file in classpath
         URL resource =
            getClass().getClassLoader().getResource(Constants.AXIS_CONFIGURATION_FILE);

         if (resource == null) {
            log.getCategory().l7dlog(
               Priority.WARN,
               Constants.COULD_NOT_FIND_AXIS_CONFIGURATION_0,
               new Object[] { Constants.AXIS_CONFIGURATION_FILE },
               null);
            throw new Exception(Constants.COULD_NOT_FIND_AXIS_CONFIGURATION_0);
         }

         axisServer = new AxisServer(new XMLResourceProvider(resource));

         // Set the base path for the AxisServer to our WEB-INF directory
         axisServerMap.put(getRootContext(), axisServer);
      }
   }

   /**
    * start service means
    *  - initialise axis engine
    *  - register Axis servlet in WebContainer
    *  - contact the maindeployer
    */
   public void startService() throws Exception {
      // set up the axis engine
      ensureService();

      // we find out through which URLClassLoader we have been loaded
      UnifiedClassLoader myLoader = (UnifiedClassLoader) getClass().getClassLoader();

      // and get our web deployment descriptor through the
      // zip-cache faker
      URL resource =
         fixURL(myLoader.getResource(Constants.AXIS_DEPLOYMENT_DESCRIPTOR));
      InputStream input = resource.openStream();

      if (resource == null) {
         log.getCategory().l7dlog(
            Priority.ERROR,
            Constants.COULD_NOT_DEPLOY_DESCRIPTOR,
            new Object[0],
            null);
         throw new Exception(Constants.COULD_NOT_DEPLOY_DESCRIPTOR);
      }

      //next we build the deployment structure that should
      //persuade the web-server to run the axis servlet under
      //our root context
      File appDir = new File(getDeployDir() + File.separator + getRootContext());

      //seems like the deploymentinfo is a bit intolerant wrt the existance
      appDir.mkdirs();

      URL myUrl = appDir.toURL();
      myDeploymentInfo = new DeploymentInfo(myUrl, null, getServer());
      myDeploymentInfo.url = new URL("http://net.jboss.org/fake.war");
      myDeploymentInfo.ucl = myLoader;

      Iterator allDeployers =
         ((Collection) server
            .invoke(
               MainDeployerMBean.OBJECT_NAME,
               "listDeployers",
               new Object[0],
               new String[0]))
            .iterator();

      while (allDeployers.hasNext() && webDeployer == null) {
         SubDeployer nextBean = (SubDeployer) allDeployers.next();
         if (nextBean.accepts(myDeploymentInfo)) {
            webDeployer = nextBean;
         }
      }

      if (webDeployer != null) {

         myDeploymentInfo.localUrl = myUrl;
         myDeploymentInfo.deployer = webDeployer;

         try {
            // the target where the deployemnt descriptor goes to
            File target = new File(appDir + Constants.WEB_DEPLOYMENT_DESCRIPTOR);
            // create intermediate directories if not yet existing
            target.getParentFile().mkdirs();
            // this is the output stream for writing the descriptor into
            FileOutputStream output = new FileOutputStream(target);
            try {
               copy(input, output);
            } finally {
               output.close();
            }
         } finally {
            input.close();
         }

         // generate a jboss-web file in case that we have a non-trivial
         // security domain
         if (securityDomain != null) {
            File target = new File(appDir + Constants.JBOSS_WEB_DEPLOYMENT_DESCRIPTOR);
            PrintStream output = new PrintStream(new FileOutputStream(target));
            try {
               //lets hope that is a valid xml format ;-)
               output.println(
                  "<jboss-web><security-domain>"
                     + securityDomain
                     + "</security-domain></jboss-web>");
            } finally {
               output.close();
            }
         }

         // we use the root context as URL infix
         String rootContext = "/" + getRootContext() + "/*";

         log.getCategory().l7dlog(
            Priority.INFO,
            Constants.ABOUT_TO_DEPLOY_0_UNDER_CONTEXT_1,
            new Object[] { myDeploymentInfo, rootContext },
            null);

         // we fake a new deploymentinfo for the war deployer
         myDeploymentInfo.webContext = rootContext;

         // Call the war deployer through the JMX server to initialise
         // and deploy the info
         webDeployer.start(myDeploymentInfo);

		super.startService();
      } else {
         throw new Exception(Constants.CANNOT_FIND_WEB_DEPLOYER);
      }

   }

   /** what to do to stop axis temporarily --> undeploy the servlet */
   public void stopService() throws Exception {

	  super.stopService();
		
      // tear down all running web services
      //Is this really what you want to do? Not leave services running anyway? 
      for (Iterator apps = new java.util.ArrayList(deployments.values()).iterator();
         apps.hasNext();
         ) {
         DeploymentInfo info = (DeploymentInfo) apps.next();
         try {
            //unregister through server so it's bookeeping is up to date.
            server.invoke(
               MainDeployerMBean.OBJECT_NAME,
               "undeploy",
               new Object[] { info },
               new String[] { "org.jboss.deployment.DeploymentInfo" });
         } catch (Exception e) {
            log.error("Could not undeploy deployment " + info, e);
         }
      }

      // undeploy Axis servlet
      try {
         // undeploy axis servlet
         log.getCategory().l7dlog(
            Priority.INFO,
            Constants.ABOUT_TO_UNDEPLOY_0,
            new Object[] { myDeploymentInfo },
            null);

         webDeployer.stop(myDeploymentInfo);

      } catch (Exception e) {
         log.getCategory().error(Constants.COULD_NOT_STOP_AXIS, e);
      } finally {
         synchronized (AxisService.class) {
            axisServer.stop();
            axisServerMap.remove(getRootContext());
         }
         super.stopService();
         myDeploymentInfo = null;
      }

   }

   //----------------------------------------------------------------------------
   // security domain
   //----------------------------------------------------------------------------

   /** name of the security domain, null if none */
   String securityDomain = null;

   public String getSecurityDomain() {
      return securityDomain;
   }

   public void setSecurityDomain(String name) {
      log.getCategory().l7dlog(
         Priority.INFO,
         Constants.SET_SECURITY_DOMAIN_TO_0,
         new Object[] { name },
         null);
      securityDomain = name;
   }

   //----------------------------------------------------------------------------
   // root context  plug
   //----------------------------------------------------------------------------

   protected String rootContext = Constants.DEFAULT_ROOT_CONTEXT;

   public String getRootContext() {
      return rootContext;
   }

   /** the root context must be constant for the lifetime of the service */
   public void setRootContext(String name) {
      log.getCategory().l7dlog(
         Priority.INFO,
         Constants.SET_ROOT_CONTEXT_0,
         new Object[] { name },
         null);
      rootContext = name;
   }

   //----------------------------------------------------------------------------
   // Deployer interface
   //----------------------------------------------------------------------------

   /**
    * Provides a filter that decides whether a file can be deployed by
    * this deployer based on the filename.  This is for the benefit of
    * the {@link org.jboss.deployer.MainDeployer} service.
    *
    * @return a <tt>FilenameFilter</tt> that only
    *         <tt>accept</tt>s files with names that can be
    *         deployed by this deployer
    */
   public boolean accepts(DeploymentInfo sdi) {
      if (sdi.url.getFile().endsWith(Constants.WSR_FILE_EXTENSION)) {
         try {
            if (sdi.localCl.getResource(Constants.WEB_SERVICE_DESCRIPTOR) != null) {
               return true;
            }
         } catch (Exception e) {
         }
      }
      return false;
   }

   /*
    * Init a deployment
    *
    * parse the XML MetaData.  Init and deploy are separate steps allowing for subDeployment
    * in between.
    *
    * @param url    The URL to deploy.
    *
    * @throws MalformedURLException    Invalid URL
    * @throws IOException              Failed to fetch content
    * @throws DeploymentException      Failed to deploy
    */

   public void init(DeploymentInfo sdi) throws DeploymentException {
      try {
         URL metaInfos = null;

         if (sdi.metaData == null) {
            metaInfos = sdi.localCl.getResource(Constants.WEB_SERVICE_DESCRIPTOR);
         } else {
            metaInfos = (URL) sdi.metaData;
         }

         sdi.metaData = XMLUtils.newDocument(metaInfos.openStream());

         //Resolve what to watch
         if (sdi.url.getProtocol().startsWith("http")) {
            // We watch the top only, no directory support
            sdi.watch = sdi.url;

         } else if (sdi.url.getProtocol().startsWith("file")) {
            sdi.watch = metaInfos;
         }

      } catch (Exception e) {
         throw new DeploymentException("problem in init" + e.getMessage());
      }
   }

   /**
    * Describe <code>create</code> method here.
    *
    * This step should include deployment steps that expose the existence of the unit being 
    * deployed to other units.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   public void create(DeploymentInfo sdi) throws DeploymentException {
      log.getCategory().l7dlog(
         Priority.INFO,
         Constants.ABOUT_TO_CREATE_AXIS_0,
         new Object[] { sdi },
         null);

      if (deployments.containsKey(sdi.url)) {
         throw new DeploymentException(
            "attempting to redeploy a depoyed module! " + sdi.url);
      } else {
         deployments.put(sdi.url, sdi);
      }

   }

   /**
    * Describe <code>start</code> method here.
    *
    * This should only include deployment activities that refer to resources
    * outside the unit being deployed.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   public void start(DeploymentInfo sdi) throws DeploymentException {
      log.getCategory().l7dlog(
         Priority.INFO,
         Constants.ABOUT_TO_START_AXIS_0,
         new Object[] { sdi },
         null);

      // remember old classloader
      ClassLoader previous = Thread.currentThread().getContextClassLoader();

      // build new classloader for naming purposes
      URLClassLoader serviceLoader = URLClassLoader.newInstance(new URL[0], sdi.ucl);

      try {
         InitialContext iniCtx = new InitialContext();
         Context envCtx = null;

         // create a new naming context java:comp/env
         try {
            // enter the apartment
            Thread.currentThread().setContextClassLoader(serviceLoader);
            envCtx = (Context) iniCtx.lookup("java:comp");
            envCtx = envCtx.createSubcontext("env");
         } finally {
            // enter the apartment
            Thread.currentThread().setContextClassLoader(previous);
         }

         Document doc = (Document) sdi.metaData;
         // the original command
         Element root = doc.getDocumentElement();
         // the deployment command document
         Document deployDoc = XMLUtils.newDocument();
         // create command
         Element deploy =
            deployDoc.createElementNS(root.getNamespaceURI(), "deployment");
         NamedNodeMap attributes = root.getAttributes();
         for (int count = 0; count < attributes.getLength(); count++) {
            Attr attribute = (Attr) attributes.item(count);
            deploy.setAttributeNodeNS((Attr) deployDoc.importNode(attribute, true));
         }

         // and insert the nodes from the original document
         // and sort out the ejb-ref extensions
         NodeList children = root.getChildNodes();
         for (int count = 0; count < children.getLength(); count++) {
            Node actNode = children.item(count);
            if (actNode instanceof Element
               && ((Element) actNode).getTagName().equals("ejb-ref")) {
               String refName =
                  MetaData.getElementContent(
                     MetaData.getUniqueChild((Element) actNode, "ejb-ref-name"));
               String linkName =
                  MetaData.getElementContent(
                     MetaData.getUniqueChild((Element) actNode, "ejb-link"));
               if (refName == null)
                  throw new DeploymentException(Constants.EJB_REF_MUST_HAVE_UNIQUE_NAME);
               if (linkName == null)
                  throw new DeploymentException(Constants.EJB_REF_MUST_HAVE_UNIQUE_LINK);

               Util.bind(envCtx, refName, new LinkRef(linkName));
            } else {
               deploy.appendChild(deployDoc.importNode(actNode, true));
            }
         }
         // insert command into document
         deployDoc.appendChild(deploy);

         try {
            Thread.currentThread().setContextClassLoader(serviceLoader);
           new Deployment(deploy).deployToRegistry(((XMLResourceProvider) axisServer.
           		getConfig()).getDeployment());
        	axisServer.refreshGlobalOptions();
			axisServer.saveConfiguration();
         } catch (Exception e) {
            throw new DeploymentException(Constants.COULD_NOT_DEPLOY_DESCRIPTOR, e);
         } finally {
            Thread.currentThread().setContextClassLoader(previous);
         }
      } catch (NamingException e) {
         throw new DeploymentException(Constants.COULD_NOT_DEPLOY_DESCRIPTOR, e);
      }
   }

   /** stop a given deployment */
   public void stop(DeploymentInfo sdi) throws DeploymentException {
      log.getCategory().l7dlog(
         Priority.INFO,
         Constants.ABOUT_TO_STOP_AXIS_0,
         new Object[] { sdi },
         null);
      if (!deployments.containsKey(sdi.url)) {
         throw new DeploymentException(
            "Attempting to undeploy a not-deployed unit! " + sdi.url);
      }
      // this was the deployment command
      Element root = (Element) ((Document) sdi.metaData).getDocumentElement();
      // from which we extract an undeployment counterpart
      Document undeployDoc = XMLUtils.newDocument();
      Element undeploy =
         undeployDoc.createElementNS(root.getNamespaceURI(), "undeployment");
      NamedNodeMap attributes = root.getAttributes();
      for (int count = 0; count < attributes.getLength(); count++) {
         Attr attribute = (Attr) attributes.item(count);
         undeploy.setAttributeNodeNS((Attr) undeployDoc.importNode(attribute, true));
      }
      // all service and handler entries are copied for
      // that purpose
      NodeList children = root.getElementsByTagName("service");
      for (int count = 0; count < children.getLength(); count++) {
         Node actNode = children.item(count);
         undeploy.appendChild(undeployDoc.importNode(actNode, true));
      }
      children = root.getElementsByTagName("handler");
      for (int count = 0; count < children.getLength(); count++) {
         Node actNode = children.item(count);
         undeploy.appendChild(undeployDoc.importNode(actNode, true));
      }
      // put command into document
      undeployDoc.appendChild(undeploy);

      try {
         	// and call the administrator
			new WSDDUndeployment(undeploy).
				undeployFromRegistry(((XMLResourceProvider) axisServer.getConfig()).
					getDeployment());
        axisServer.refreshGlobalOptions();
		axisServer.saveConfiguration();
      } catch (Exception e) {
         throw new DeploymentException(Constants.COULD_NOT_UNDEPLOY, e);
      }
   }

   /** destroy a given deployment */
   public void destroy(DeploymentInfo sdi) throws DeploymentException {
      log.getCategory().l7dlog(
         Priority.INFO,
         Constants.ABOUT_TO_DESTROY_AXIS_0,
         new Object[] { sdi },
         null);
   }

   //
   // Statics
   //

   /** the axis engines */
   final static Map axisServerMap = new java.util.HashMap();

   /** return the engine if initialised */
   public static synchronized AxisServer getAxisServer(String context) {
      return (AxisServer) axisServerMap.get(context);
   }

}
