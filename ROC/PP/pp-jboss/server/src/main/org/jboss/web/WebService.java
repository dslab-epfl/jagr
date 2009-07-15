/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.web;

import java.io.InputStream;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Enumeration;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;

/** The WebService implementation. It configures a WebServer instance to
 perform dynamic class and resource loading.

 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 *   @version $Revision: 1.1.1.1 $
 */
public class WebService
   extends ServiceMBeanSupport
   implements WebServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   private WebServer webServer;
   private String host;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public WebService()
   {
      try
      {
         this.webServer = new WebServer();
      }
      catch(Throwable e)
      {
         log.error("Failed to create WebServer", e);
      }

      // Load the file mime.types into the mapping list
      try
      {
         Properties mimeTypes = new Properties();
         ClassLoader tcl = Thread.currentThread().getContextClassLoader();
         InputStream is = tcl.getResourceAsStream("org/jboss/web/mime.types");
         mimeTypes.load(is);
			Enumeration keys = mimeTypes.keys();
			while (keys.hasMoreElements())
			{
				String extension = (String)keys.nextElement();
				String type = mimeTypes.getProperty(extension);
				webServer.addMimeType(extension, type);
			}
      }
      catch (Throwable e)
      {
         log.error("Failed to load org/jboss/web/mime.types", e);
      }

      // Get the public host name
      try
      {
         host = System.getProperty("java.rmi.server.hostname");
      }
      catch(SecurityException e)
      {
      }

      try
      {
         if( host == null )
            host = InetAddress.getLocalHost().getHostName();
      }
      catch(Throwable e)
      {
         log.error("Failed to get localhost name", e);
      }
   }

   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return OBJECT_NAME;
   }
   
   public String getName()
   {
      return "Webserver";
   }

   /** Start the web server for dynamic downloading of classes and resources.
    The system java.rmi.server.codebase is also set to
    "http://"+getHost()":"+getPort()+"/" if the property has not been set.
    */
   public void startService()
      throws Exception
   {
      // Start the WebServer running
      webServer.start();
      log.info("Started webserver with address: " + webServer.getBindAddress()
         + " port: "+webServer.getPort());

      // Set the rmi codebase if it is not already set
      String codebase = System.getProperty("java.rmi.server.codebase");
      if( codebase == null )
      {
        codebase = "http://"+host+":"+getPort()+"/";
        System.setProperty("java.rmi.server.codebase", codebase);
      }
      log.info("Codebase set to: "+codebase);
   }

   public void stopService()
   {
      webServer.stop();
   }

   public URL addClassLoader(ClassLoader cl)
   {
      return webServer.addClassLoader(cl);
   }
   
   public void removeClassLoader(ClassLoader cl)
   {
      webServer.removeClassLoader(cl);
   }

	public void setPort(int port)
	{
		webServer.setPort(port);
	}
	
   public int getPort()
   {
   	return webServer.getPort();
   }

	public void setHost(String host)
	{
		this.host = host;
	}
	
   public String getHost()
   {
   	return host;
   }

   /** Get the specific address the WebService listens on.t
    @return the interface name or IP address the WebService binds to.
    */
   public String getBindAddress()
   {
      return webServer.getBindAddress();
   }
   /** Set the specific address the WebService listens on.  This can be used on
    a multi-homed host for a ServerSocket that will only accept connect requests
    to one of its addresses.
    @param host , the interface name or IP address to bind. If host is null,
    connections on any/all local addresses will be allowed.
    */
   public void setBindAddress(String host) throws UnknownHostException
   {
      webServer.setBindAddress(host);
   }

   /** Get the WebService listen queue backlog limit. The maximum queue length
    for incoming connection indications (a request to connect) is set to the
    backlog parameter. If a connection indication arrives when the queue is
    full, the connection is refused. 
    @return the queue backlog limit. 
    */
   public int getBacklog()
   {
      return webServer.getBacklog();
   }
   /** Set the WebService listen queue backlog limit. The maximum queue length
    for incoming connection indications (a request to connect) is set to the
    backlog parameter. If a connection indication arrives when the queue is
    full, the connection is refused. 
    @param backlog , the queue backlog limit.
    */
   public void setBacklog(int backlog)
   {
      webServer.setBacklog(backlog);
   }

   /** A flag indicating if the server should attempt to download classes from
    * thread context class loader when a request arrives that does not have a
    * class loader key prefix.
    */
   public boolean getDownloadServerClasses()
   {
      return webServer.getDownloadServerClasses();
   }
   public void setDownloadServerClasses(boolean flag)
   {
      webServer.setDownloadServerClasses(flag);
   }
   
   // Protected -----------------------------------------------------
}
