/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.jndi;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Set;
import java.util.HashMap;
import javax.naming.InitialContext;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.Query;

import org.jboss.ha.framework.server.HARMIServerImpl;
import org.jboss.ha.jndi.HANamingService.AutomaticDiscovery;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.RoundRobin;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.ha.framework.server.ClusterPartition;
import org.jboss.ha.framework.server.ClusterPartitionMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jnp.interfaces.Naming;

/** 
 *   Management Bean for HA-JNDI service.
 *
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *   @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/11/19 bill burke:</b>
 * <ul>
 * <li> implemented PartitionServiceMBean interface for new 2-phase initilization
 * <li> added stop method to AutomaticDiscovery so that it could be cleaned up.
 * </ul>
 */

public class HANamingService
   extends ServiceMBeanSupport
   implements Runnable, HANamingServiceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   /** The Naming interface server implementation */
   protected HAJNDI theServer;
   /** The jnp server socket through which the HAJNDI stub is vended */
   protected ServerSocket serverSocket;
   /** An optional custom client socket factory */
   protected RMIClientSocketFactory clientSocketFactory;
   /** An optional custom server socket factory */
   protected RMIServerSocketFactory serverSocketFactory;
   /** The class name of the optional custom client socket factory */
   protected String clientSocketFactoryName;
   /** The class name of the optional custom server socket factory */
   protected String serverSocketFactoryName;
   /** The interface to bind to. This is useful for multi-homed hosts
    that want control over which interfaces accept connections.
    */
   protected InetAddress bindAddress;
   /** The serverSocket listen queue depth */
   protected int backlog = 50;
   /** The jnp protocol listening port. The default is 1100, the same as
    the RMI registry default port. */
   protected int port = 1100;
   /** The RMI port on which the Naming implementation will be exported. The
    default is 0 which means use any available port. */
   protected int rmiPort = 0;
   /** The mapping from the long method hash to the Naming Method */
   protected HashMap marshalledInvocationMapping = new HashMap();

   protected HARMIServerImpl rmiserver;
   protected Naming stub;
   protected HAPartition partition;
   protected String partitionName = "DefaultPartition";

   protected AutomaticDiscovery autoDiscovery = null;
   protected String adGroupAddress = org.jnp.interfaces.NamingContext.DEFAULT_DISCOVERY_GROUP_ADDRESS;
   protected int adGroupPort = org.jnp.interfaces.NamingContext.DEFAULT_DISCOVERY_GROUP_PORT;
   
   // Public --------------------------------------------------------

   public HANamingService()
   {
      // for JMX
   }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }
   
   public String getPartitionName()
   {
      return partitionName;
   }

   public void setPartitionName(final String partitionName)
   {
      this.partitionName = partitionName;
   }

   public void setRmiPort(int p)
   {
      rmiPort = p;
   }
   public int getRmiPort()
   {
      return rmiPort;
   }

   public void setPort(int p)
   {
      port = p;
   }
   public int getPort()
   {
      return port;
   }

   public String getBindAddress()
   {
      String address = null;
      if( bindAddress != null )
         address = bindAddress.getHostAddress();
      return address;
   }
   public void setBindAddress (String host) throws java.net.UnknownHostException
   {
      bindAddress = InetAddress.getByName(host);
   }

   public int getBacklog()
   {
      return backlog;
   }
   public void setBacklog(int backlog)
   {
      if( backlog <= 0 )
         backlog = 50;
      this.backlog = backlog;
   }

   public String getClientSocketFactory()
   {
      return serverSocketFactoryName;
   }
   public void setClientSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.clientSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(clientSocketFactoryName);
      clientSocketFactory = (RMIClientSocketFactory) clazz.newInstance();
   }
   
   public String getServerSocketFactory()
   {
      return serverSocketFactoryName;
   }
   public void setServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.serverSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(serverSocketFactoryName);
      serverSocketFactory = (RMIServerSocketFactory) clazz.newInstance();
   }

   
   public void startService(HAPartition haPartition)
      throws Exception
   {
      this.partition = haPartition;
      this.startService ();
   }
   
   protected void startService()
      throws Exception
   {
      log.debug("Create HARMIServer proxy");
      this.rmiserver = new HARMIServerImpl(partition, "HAJNDI", Naming.class, theServer, rmiPort, this.clientSocketFactory, this.serverSocketFactory);
      this.stub = (Naming)rmiserver.createHAStub(new RoundRobin());

      log.debug("Starting listener");
      try
      {
         serverSocket = new ServerSocket(port, backlog, bindAddress);
         // If an anonymous port was specified get the actual port used
         if( port == 0 )
            port = serverSocket.getLocalPort();
         listen();
         log.info("Listening on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());

      } catch (IOException e)
      {
         log.error("Could not start on port " + port, e);
      }
      
      // Automatic Discovery for unconfigured clients
      //
      autoDiscovery = new AutomaticDiscovery ();
      autoDiscovery.listen ();
   }

   protected void createService()
      throws Exception
   {
      boolean debug = log.isDebugEnabled();
      
      if (debug) 
         log.debug("Initializing HAJNDI server on partition: " + partitionName);
      
      partition = findHAPartitionWithName (partitionName);
      log.debug("Create remote object");
      theServer = new HAJNDI(partition);
      log.debug("initialize HAJNDI");
      theServer.init();

      // Build the Naming interface method map
      Method[] methods = Naming.class.getMethods();
      for(int m = 0; m < methods.length; m ++)
      {
         Method method = methods[m];
         Long hash = new Long(MarshalledInvocation.calculateHash(method));
         marshalledInvocationMapping.put(hash, method);
      }
   }

   protected void stopService() throws Exception
   {
      boolean debug = log.isDebugEnabled();
      
      // Unexport server
      if (debug)
         log.debug("destroy ha rmiserver");
      this.rmiserver.destroy ();         
      
      // Stop listener
      ServerSocket s = serverSocket;
      serverSocket = null;
      if (debug)
         log.debug("closing socket");
      
      s.close();
      if (debug)
         log.debug("Stopping AutomaticDiscovery");
      autoDiscovery.stop();
   }

   /** Expose the Naming service via JMX to invokers.
    *
    * @jmx:managed-operation
    *
    * @param invocation    A pointer to the invocation object
    * @return              Return value of method invocation.
    * 
    * @throws Exception    Failed to invoke method.
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      // Set the method hash to Method mapping
      if (invocation instanceof MarshalledInvocation)
      {
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         mi.setMethodMap(marshalledInvocationMapping);
      }
      // Invoke the Naming method via reflection
      Method method = invocation.getMethod();
      Object[] args = invocation.getArguments();
      Object value = null;
      try
      {
         value = method.invoke(theServer, args);
      }
      catch(InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if( t instanceof Exception )
            throw (Exception) e;
         else
            throw new UndeclaredThrowableException(t, method.toString());
      }

      return value;
   }

   // Runnable implementation ---------------------------------------
   public void run()
   {
      java.net.Socket socket = null;
      
      // Accept a connection
      try
      {
         socket = serverSocket.accept();
      } catch (IOException e)
      {
         if (serverSocket == null) return; // Stopped by normal means
         log.error("Naming stopped", e);
         log.info("Restarting naming");
         try
         {
            start();
         } catch (Exception ex)
         {
            log.error("Restart failed", ex);
            return;
         }
      }

      // Create a new thread to accept the next connection
      listen();
      
      // Return the naming server stub
      try
      {
         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
         synchronized(stub)
         {
            out.writeObject(new java.rmi.MarshalledObject(stub));
         }
      }
      catch (IOException ex)
      {
         log.error("Error writing response", ex);
      }
      finally
      {
         try
         {
            socket.close();
         } catch (IOException e)
         {
         }
      }
   }
   
   // Protected -----------------------------------------------------
   
   protected void listen()
   {
      Thread t = new Thread(this, "HAJNDI-Listener");
      t.start();
   }
   
   protected HAPartition findHAPartitionWithName (String name) throws Exception
   {
      HAPartition result = null;
      QueryExp exp = Query.and(
                        Query.eq(
                           Query.classattr(),
                           Query.value(org.jboss.ha.framework.server.ClusterPartition.class.getName ())
                        ),
                        Query.match(
                           Query.attr("PartitionName"),
                           Query.value(name)
                        )
                      );

      Set mbeans = this.getServer ().queryMBeans (null, exp);
      if (mbeans != null && mbeans.size () > 0)
      {
         ObjectInstance inst = (ObjectInstance)(mbeans.iterator ().next ());
         ClusterPartitionMBean cp = (ClusterPartitionMBean)org.jboss.mx.util.MBeanProxy.get (
                                                      ClusterPartitionMBean.class, 
                                                      inst.getObjectName (),
                                                      this.getServer ());
         result = (HAPartition)cp.getHAPartition();
      }
      
      return result;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
   protected class AutomaticDiscovery
      implements Runnable
   {
      protected org.jboss.logging.Logger log =
         org.jboss.logging.Logger.getLogger(AutomaticDiscovery.class);
      
      protected java.net.MulticastSocket socket = null;
      protected byte[] ipAddress = null;
      protected InetAddress group = null;
      protected boolean stopping = false;
      
      public AutomaticDiscovery () throws Exception
      {
         socket = new java.net.MulticastSocket (adGroupPort);
         group = InetAddress.getByName (adGroupAddress);
         socket.joinGroup (group);
         
         String address = getBindAddress();
         if (address == null)
            ipAddress = (java.net.InetAddress.getLocalHost().getHostAddress() + ":" + port).getBytes();
         else
            ipAddress = (address + ":" + port).getBytes();


         log.info("Listening on " + socket.getInterface() + ":" + socket.getLocalPort());
      }
      
      public void stop()
      {
         try
         {
            stopping = true;
            socket.leaveGroup(group);
            socket.close();
         }
         catch (Exception ex)
         {
            log.error("Stopping AutomaticDiscovery failed", ex);
         }
      }

      public void run ()
      {
         boolean trace = log.isTraceEnabled();
         // Wait for a datagram
         //
         byte[] buf = new byte[256];
         DatagramPacket packet = new DatagramPacket(buf, buf.length);
         try
         {
            if( trace )
               log.trace("HA-JNDI AutomaticDiscovery waiting for queries...");
            socket.receive(packet);
            if( trace )
               log.trace("HA-JNDI AutomaticDiscovery Packet received.");
         } 
         catch (IOException e)
         {
            if (!stopping) log.error ("HA-JNDI AutomaticDiscovery stopped", e);
            return;
         }
         
         // Create a new thread to accept the next datagram
         listen ();
         
         // Return the naming server IP address and port to the client
         //
         try
         {            
            DatagramPacket p = new DatagramPacket (ipAddress, ipAddress.length, packet.getAddress(), packet.getPort());
            if( trace )
               log.trace("Sending AutomaticDiscovery answer: " + p);
            socket.send (p);
            if( trace )
               log.trace("AutomaticDiscovery answer sent.");
         }
         catch (IOException ex)
         {
            log.error ("Error writing response", ex);
         }
      }
      
      protected void listen ()
      {
         Thread t = new Thread (this, "HAJNDI-AutomaticDiscovery");
         t.start ();
      }
   }
}
