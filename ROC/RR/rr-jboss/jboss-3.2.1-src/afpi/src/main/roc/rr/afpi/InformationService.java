/*
 * $Id: InformationService.java,v 1.5 2004/08/18 20:12:39 candea Exp $ 
 */

package roc.rr.afpi;

import java.util.*;
import java.sql.*;
import java.io.*;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.*;
import org.jboss.ejb.*;

import roc.rr.*;

/** MBean for the AFPI information service.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.5 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 **/

public class InformationService
   extends ServiceMBeanSupport
   implements InformationServiceMBean
{
    private LogAvailableMemoryThread lAMThread = null;

    /**
     * MBean Attributes
     *
     **/
    private int     interval;
    private boolean logAvailableMemoryOn = false;


    /**
     *  getter method for MBean attribute Interval
     *
     * @jmx:managed-attribute
     */
    public int getInterval(){
	return interval;
    }

    /**
     *  setter method for MBean attribute Interval
     *
     * @jmx:managed-attribute
     */
    public void setInterval(int interval){
	this.interval = interval;
    }

    /**
     *  getter method for MBean attribute logAvailableMemoryOn
     *
     * @jmx:managed-attribute
     */
    public boolean getLogAvailableMemoryOn(){
	return logAvailableMemoryOn;
    }

    /**
     *  setter method for MBean attribute logAvailableMemoryOn
     *
     * @jmx:managed-attribute
     */
    public void setLogAvailableMemoryOn(boolean on){
	this.logAvailableMemoryOn = on;
    }
    


    /**
     * Returns a string describing amount of available memory.
     *
     * @jmx:managed-operation
     **/
    public String availableMemory() 
	throws Exception
    {
	ObjectName serverInfo = new ObjectName("jboss.system:type=ServerInfo");

	long availBytes = ((Long) server.getAttribute(serverInfo, "FreeMemory")).longValue();
	long totalBytes = ((Long) server.getAttribute(serverInfo, "TotalMemory")).longValue();

	int availKB = (int) availBytes/1024;
	int availMB = (int) availKB/1024;
	int totalKB = (int) totalBytes/1024;
	int totalMB = (int) totalKB/1024;

	return "Available memory: " + availMB + " MB + " + (availKB-1024*availMB) + " KB<br>" +
	       "(out of a total of: " + totalMB + " MB + " + (totalKB-1024*totalMB) + " KB)";
    }


    /**
     * create thread for logging available and total memory 
     *
     * @paramm interval  Interval time of mesurement in second.
     *
     * @jmx:managed-operation
     **/
    public String logAvailableMemory() 	throws Exception {
	lAMThread = 
	    new LogAvailableMemoryThread(server,log,interval);
	lAMThread.start();

	return "Started logging available memory with "+interval
	    + " sec interval. See log for the result";
    }

    /**
     * stop logging available and total memory
     *
     * @jmx:managed-operation
     **/
    public String stopLoggingAvailableMemory()
	throws Exception
    {
	String message;

	if ( lAMThread != null ) {
	    lAMThread.stop();
	    lAMThread = null;
	    message = "Stoped logging available memory";
	} else {
	    message = "Logging available memory has't started yet";
	}
	return message;
    }

    /**
     * @jmx:managed-operation
     **/
    public String listEjbs()
    {
	String ret="";
	Hashtable h = ComponentMap.map();
	
	for( Iterator it=h.keySet().iterator() ; it.hasNext() ; )
	{
	    String ejbName = (String) it.next();
	    DeploymentInfo di = ComponentMap.getDeploymentInfo( ejbName );
	    ObjectName cont;
	    try {
		cont = ComponentMap.getContainerName( ejbName );
	    }
	    catch( Exception e )
		{
		    log.error("***************************************************************************");
		    e.printStackTrace();
		    log.error("***************************************************************************");
		    return null;
		}
	    ret += "<b>" + ejbName + "</b> --> " + di.url + "<br>" +
		   "&nbsp;&nbsp;&nbsp;&nbsp;container=" + cont + "<br>";
	}
	return ret;
    }

    /**
     * Lists all deployed components we know about.
     *
     * @jmx:managed-operation
     **/
    public String listDeployedComponents()
    {
	Collection packs;
	String ret="";

	//
	// Get all deployed packages
	//
	try 
	{
	    ObjectName deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
	    packs = (Collection) server.invoke(deployerSvc, "listDeployed", null, null);
	} 
	catch ( Exception e ) 
	{
	    e.printStackTrace();
	    return "FAILED: See server.log for stack trace details...";
	}

	//
	// Find all deployed packages
	//
	for (Iterator i = packs.iterator() ; i.hasNext() ; ) 
	{
	    DeploymentInfo di = (DeploymentInfo) i.next();

	    /* Ignore subdeployments and XML files */
	    if ( di.parent != null  ||  di.shortName.endsWith(".xml") )
	    {
		continue;
	    }

	    ret += "<p><b>" + di.shortName + "</b> [" + di.url + "]\n" ;

	    if ( di.webContext != null ) {
		ret += " <b>webContext</b>=" + di.webContext + "\n" ;
	    }

	    ret += listSubDeployments( di );

	    /* deploying an ejb-jar results in an EjbModule mbean, which is stored here. */
	    /* public ObjectName deployedObject; */
	}

	return ret;
    }


    /**
     * Lists all subdeployments of the given deployment.
     *
     * @param  di  deployment
     *
     **/
    private String listSubDeployments( DeploymentInfo di )
    {
	Iterator subs = di.subDeployments.iterator();

	if ( !subs.hasNext() ) {
	    return "";
	}

	String ret = "<blockquote>\n";

	while ( subs.hasNext() )
	{
	    DeploymentInfo sdi = (DeploymentInfo) subs.next();
	    ret += "<b>" + sdi.shortName + "</b> [" + sdi.url + "]\n" + listSubDeployments( sdi );
	}

	ret += "</blockquote>\n";
	return ret;
    }

    public void create() throws Exception {}

    public void start() throws Exception 
    {
	if ( logAvailableMemoryOn ) {
	    logAvailableMemory();
	}
    }

    public void stop()                
    {
	if ( logAvailableMemoryOn ) {
	    try {
		stopLoggingAvailableMemory();
	    } catch (Exception e) {}
	}
    }

    public void destroy()                 {}
}
