//
// $Id: RestartAgent.java,v 1.10 2003/04/07 04:05:18 steveyz Exp $
//

package org.jboss.RR;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import javax.naming.InitialContext;
import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.DeploymentInfo;
import org.apache.log4j.Category;
import roc.config.ROCConfig;

/**
 *
 * @jmx:mbean name="jboss:type=FailureMonitor"
 *            extends="org.jboss.system.ServiceMBean"
 *            
 */
public class RestartAgent 
   extends ServiceMBeanSupport
   implements RestartAgentMBean
{
    protected RestartAgentThread restartAgentThread = null;
    protected static final int   restartPort = 1234;
    protected static final int   delayProxyPort = 1313;
    private DeploymentInfo warFile = null; // the app's WAR
    private Category    log; // log4j destination for this class


  /**
    * Constructs a restart agent.  Initializes the logging system for
    * this class and starts a thread listening for restart commands
    * from the brain.
    *
    * @param  
    * @return 
    */
    public RestartAgent () 
       throws java.net.SocketException
      {
         log = Category.getInstance("RestartAgent");

	 restartAgentThread = new RestartAgentThread();
	 restartAgentThread.start();
      }


  /**
    * <b>Private</b> method that converts a list of component names to
    * <code>DeploymentInfo</code>s based on the list of deployed
    * components obtained from the <code>MainDeployer</code>.  The
    * matching is done based on <code>jndiName=</code> string matches.
    * Duplicates are eliminated (we keep the one that appeared first).
    *
    * @param  names a list of strings
    * @return a list of <code>DeploymentInfo</code>s corresponding to
    *         the <i>names</i> list, in the corresponding order 
    */
   private List convertNamesToDepInfos (List names)
      throws Exception
      {
         // Obtain the list of DepInfo's for everything that is deployed
         MBeanServer  server = org.jboss.util.jmx.MBeanServerLocator.locate();
         String      svcName = "jboss.system:service=MainDeployer";
         ObjectName deployer = new ObjectName(svcName);
         List       depInfos = (List) server.invoke(deployer, "listDeployed", 
                                                    new Object[] {}, new String[] {});

         // Go through each deployed entity and see if any of our names
         // correspond.  The elements we find get added to a hash table that
         // maps names to their corresponding DeploymentInfo.  In effect, this
         // code finds all the JAR files containing EJBs in the list of names.
         Hashtable ejbToDepInfo = new Hashtable();
         for ( Iterator depI = depInfos.iterator(); depI.hasNext(); )
         {                                    // iterate over DeploymentInfo's
            DeploymentInfo di = (DeploymentInfo) depI.next();

            for ( Iterator nameI = names.iterator(); nameI.hasNext(); )
            {                                 // iterate over component names
               String compName = (String) nameI.next();

               // For each deployed EJB, there is an MBean carrying its name;
               // the MBeans are stored in the JAR's DeploymentInfo.
               // Individual EJBs don't have their own DeploymentInfo, unless
               // they are alone in the JAR.  We now iterate over the MBeans,
               // trying to identify the DepInfo for 'compName'.
               for (Iterator mbI = di.mbeans.iterator(); mbI.hasNext(); )
               {                                       // iterate over MBeans
                  String mbName = mbI.next().toString();

                  if ( mbName.matches(".*jndiName=" + compName + ".*") )
                  {
                     ejbToDepInfo.put(compName, di);
                     break;
                  }
               }
            }

            // If we found the app's war DeploymentInfo, save it for later.
            // FIXME: This is an app-specific hack!
            if ( di.url.toString().matches(".*petstore\\.war") || 
		 di.url.toString().matches(".*rubis_web\\.war") )
               warFile = di;
         }

         //
         // Now construct the list of DeploymentInfo's.
         //
         ArrayList retDepInfos = new ArrayList();
         for ( Iterator nameI = names.iterator(); nameI.hasNext(); )
         {
            String compName = (String) nameI.next();
            DeploymentInfo di = (DeploymentInfo) ejbToDepInfo.get(compName);

            if ( null != di  &&  !retDepInfos.contains(di) )
            {
               retDepInfos.add(di);
            }
         }

         return retDepInfos;
      }


    protected void sendProxyMessage(boolean pause)
    {
        try
        {
            byte[] buf = new byte[1];
            if(pause)
            {
                buf[0] = 'P';
            }
            else
            {
                buf[0] = 'U';
            }
            
            DatagramSocket s = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, 
                                                       InetAddress.getLocalHost(),
                                                       delayProxyPort);
            s.send(packet);
            if(pause)
                log.info("Pausing client(s)...");
            else
                log.info("Unpausing client(s)...");
        }
        catch(Exception e)
        {
            if(pause)
                log.warn("Failed to send pause message to DelayProxy!");
            else
                log.warn("Failed to send unpause message to DelayProxy!");
            e.printStackTrace();
        }
    }

  /**
    * <b>Private</b> method that micro-reboots a list of EJBs, in the
    * order in which they appear in the list..
    *
    * @param  ejbList a list of EJB names
    * @return 
    */
   private void rebootEJBs (List ejbList)
      throws Exception
      {
         List depInfoList;

	 //
	 // pause the client(s)
	 //
	 sendProxyMessage(true);

         try   // convert EJB list to a list of DeploymentInfo's
	 { 
            depInfoList = convertNamesToDepInfos(ejbList);
         }
         catch (Exception e) 
	 {
            log.warn("List conversion failed");
            e.printStackTrace();
            return;
         }

         // Get a ref to the MainDeployer
         MBeanServer server = org.jboss.util.jmx.MBeanServerLocator.locate();
         ObjectName deployer = new ObjectName("jboss.system:service=MainDeployer");

         // This is old code...
	 //
         // Undeploy the WAR files(s), to temporarily disable access to the
         // web site, while we recover.
         //
         if ( null == warFile )
	 {
	     throw new Exception("App's WAR is not initialized... giving up.");
	 }
         
	 // This is old code, that used to undeploy the WAR file(s) to
	 // keep clients out of the server.  Now, we pause the delay
	 // proxy to take care of that.
	 // 
  	 Object[] args = new Object[] { warFile };
           String[] sig  = new String[] { "org.jboss.deployment.DeploymentInfo" };
// DISABLE undeploying the war file for now
//           server.invoke(deployer, "undeploy", args, sig);
	 
         //
         // Undeploy JARs in order, and add them to a stack
         //
         Stack stack = new Stack();
         Iterator dis = depInfoList.iterator();
         while ( dis.hasNext() )
         {
            DeploymentInfo di = (DeploymentInfo) dis.next();

            try {
               server.invoke(deployer, "undeploy", 
                             new Object[] { di }, new String[] { "org.jboss.deployment.DeploymentInfo" });
               stack.push(di);
            }
            catch (Exception e) 
            {
               log.warn("Undeployment failed for " + di.shortName);
               e.printStackTrace();
               return;
            }
         }

         //
         // Deploy the JARs in reverse order
         //
         while ( !stack.empty() )
         {
            DeploymentInfo di = (DeploymentInfo) stack.pop();
            try 
	    {
               server.invoke(deployer, "deploy", 
                             new Object[] { di }, new String[] { "org.jboss.deployment.DeploymentInfo" });
            }
            catch (Exception e) 
            {
               log.warn("Could not deploy " + di.shortName);
               e.printStackTrace();
               return;
            }
         }

         // This is old code.  It used to re-deploy the WAR file(s),
         // to re-enable access to the web site; instead, we just
         // unpause the proxy.
         //

//      DISABLE for now
//	 server.invoke(deployer, "deploy", 
//  		       new Object[] { warFile }, new String[] { "org.jboss.deployment.DeploymentInfo" });

         // tell the delay proxy to unpause
         sendProxyMessage(false);
         
         // 
	 // Tell the FaultInjector we've completed recovery, so it can
	 // continue with the next fault (if part of an experiment)
	 //
	 if ( ROCConfig.RR_DO_PERFORMABILITY_EXPERIMENT )
	 {
	    log.info("rebootEJBs completed, alerting FaultInjector");
	    (new FaultInjector()).restartComplete();
	 }
      }



   //===========================================================================

   private class RestartAgentThread extends Thread 
   {
      DatagramSocket socket = null;
      boolean stop = false;
        
      public RestartAgentThread() 
	 throws SocketException
         {
            socket = new DatagramSocket(restartPort);
            try
            {
               String addr = InetAddress.getLocalHost().toString();
               log.info("listening on UDP " + addr + ":" + restartPort);
            }
            catch(UnknownHostException e)
            {
               log.info("listening on UDP port" + restartPort);		      	
            }      
         }
    
      public void run()
         {
            while (stop == false)
            {
               byte[] buf = new byte[1024];
            
               try
	       {
                  DatagramPacket packet = new DatagramPacket(buf, buf.length);
                  socket.receive(packet);
         
		  ByteArrayInputStream bArray_in = new ByteArrayInputStream(buf);
                  ObjectInputStream obj_in = new ObjectInputStream(bArray_in);

                  List ejbList = (List) obj_in.readObject();
		  obj_in.close();

                  log.info("Received RST command for " + ejbList);

                  rebootEJBs(ejbList);
               }
               catch (Exception e) 
	       {
                  log.info("Exceptiong while recv'ing packet or micro-rebooting");
                  e.printStackTrace();
               }
            }
         }
   }

}
