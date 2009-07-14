/* $Id: RecoveryService.java,v 1.13 2004/08/28 19:26:30 skawamo Exp $ */

package roc.rr.afpi;

import java.util.*;
import java.sql.*;
import java.net.*;
import java.lang.Long;
import java.lang.RuntimeException;
import java.util.HashMap;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.*;
import org.jboss.logging.Logger;
import roc.rr.*;
import roc.rr.afpi.util.*;

/** MBean for the AFPI recovery control service.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.13 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 **/

public class RecoveryService
   extends ServiceMBeanSupport
   implements RecoveryServiceMBean
{
    private static Logger log = Logger.getLogger( RecoveryService.class );
    
    /* keep track of most-recent-uRB timestamp for each URL */
    private static HashMap lastMicrorebootStartedTime = new HashMap(25);
    private static HashMap lastMicrorebootStoppedTime = new HashMap(25);

    /* keep track of most-recent full reboot timestamp (end of reboot only) */
    private static long lastFullRebootStoppedTime = 0;

    /* keep track of current time */
    private long currentTime;

    /* instantiate the static part of Information */
    private Information info=null;


    /* 
       The successive microreboot must be prevented for 
       certain amount of time defined by "rebootDelaySeconds" 
       after microrebooting an ejb. 
       rebootDelaySeconds is an attribute of MBean
    */ 
    private int rebootDelaySeconds = 0;  // in seconds (by default zero)

    /**
     * Attributes
     *
     **/
    RecoveryManagerThread rmThread = null;

    /*================================================================================
     * MBean attributes to control activities done during MBean startup.  They can be
     * controlled from afpi.sar/META-INF/jboss-service.xml
     */

    private int recoMgrFullThreshold  = -1; // if > 0, start reco mgr in FULL mode
    private int recoMgrMicroThreshold = -1; // if > 0, start reco mgr in MICRO mode

    /* for load balancer */
    public static String clientHostName = null;
    public static int clientPort = 2375;

    /**
     * @jmx:managed-attribute
     **/
    public int getrecoMgrFullThreshold () { return recoMgrFullThreshold; }
    
    /**
     * @jmx:managed-attribute
     **/
    public void setrecoMgrFullThreshold ( int v ) { recoMgrFullThreshold = v; }
    
    /**
     * @jmx:managed-attribute
     **/
    public int getrecoMgrMicroThreshold () { return recoMgrMicroThreshold; }
    
    /**
     * @jmx:managed-attribute
     **/
    public void setrecoMgrMicroThreshold ( int v ) { recoMgrMicroThreshold = v; }

    /**
     * @jmx:managed-attribute
     **/
    public String getclientHostName () { return clientHostName; }
    
    /**
     * @jmx:managed-attribute
     **/
    public void setclientHostName (String h) { clientHostName = h; }
    
    
    //================================================================================

    
    /**
     *  setter for rebootDelaySeconds
     *
     *  @jmx:managed-attribute
     **/
    public void setrebootDelaySeconds(int rebootDelaySeconds) {
	this.rebootDelaySeconds = rebootDelaySeconds;
    }


    /**
     *  getter for rebootDelaySeconds
     *
     *  @jmx:managed-attribute
     **/
    public int getrebootDelaySeconds(){
	return rebootDelaySeconds;
    }

    /**
     * Takes the UID of a component and tries to microreboot it.
     *
     * @jmx:managed-operation
     */
    public String microrebootByUrl( String UID ) 
	throws MicrorebootTooFrequentException
    {
	String message = null;
	ObjectName deployerSvc;
	long tStart=0, tEnd=0;

	if ( microrebootable(UID) ) {
	    try {
		reportReboot(true);
		
		Thread.sleep(20);
		// record started time 
		updateTime(lastMicrorebootStartedTime, UID);
		tStart = this.currentTime;
		deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
		server.invoke(deployerSvc, "microrebootJar", 
			      new Object[] { UID }, 
			      new String[] { "java.lang.String" });

		// record stop time
		updateTime(lastMicrorebootStoppedTime, UID);
		tEnd = this.currentTime;
		
                reportReboot(false);
	    } catch ( Exception e ) {
		log.error( e );
		throw new RuntimeException(); // fail-stop behavior
	    }

	    injectMemoryLeak();

	} else {
	    throw new MicrorebootTooFrequentException();
	}

	long duration = tEnd-tStart;
	log.info( "uRB [" + duration + " msec] " + UID );
	return "Successfully microrebooted <B>" + UID + "</B><BR>Duration: " + duration + " msec";
    }


    /**
     * Interractive version of microrebootByURL
     *
     * @jmx:managed-operation
     */
    public String interractiveMicrorebootByUrl( String UID ) 
    {
	try {
	    return microrebootByUrl(UID);
	} catch (MicrorebootTooFrequentException e) {
	    return "Target has already been uRB-ed recently; please try later...";
	} 
    }


    /**
     * Takes the UID of a component and tries to microreboot it and
     * reinject fault.
     *
     * @jmx:managed-operation
     */
    public String microrebootAndInjectFault( String UID )
	throws MicrorebootTooFrequentException
    {
	String message=null;
        ObjectName deployerSvc;

	if ( microrebootable(UID) ) {
	    try {
		// record start time
		updateTime(lastMicrorebootStartedTime,UID);

		deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
		server.invoke(deployerSvc, "microrebootJar",
			      new Object[] { UID }, 
			      new String[] { "java.lang.String" });

		// record stop time
		updateTime(lastMicrorebootStoppedTime,UID);
	    } catch ( Exception e ) {
		log.error( e );

		/*
		  if invoke() throws exception, then something 
		  is fundamentally wrong, so fail-stop
		*/
		throw new RuntimeException();
	    }
 
	    injectMemoryLeak();
	} else {
	    throw new MicrorebootTooFrequentException();
	}

	return "Successfully microrebooted <B>" + UID + "</B>";
    }
 

    /**
     * Interractive version of microrebootandInjectFault
     *
     * @jmx:managed-operation
     */
    public String interractiveMicrorebootAndInjectFault( String UID ) 
    {
	try {
	    microrebootAndInjectFault(UID);
	} catch (MicrorebootTooFrequentException e) {
	    return "Cannot microreboot during the reboot delay slot";
	} 
	return 	"Successfully microrebooted <B>" + UID + "</B>";
    }


    /*
      microrebootable: 
        Check the time of last microreboot. If this is the first time 
        or it past a longer time than rebootDelaySeconds since last 
	microreboot, return true. Otherwise return false.
    */
    private boolean microrebootable(String UID){
	boolean returnValue = false;

	Long stoppedTime = (Long)lastMicrorebootStoppedTime.get((Object)UID);
	this.currentTime = System.currentTimeMillis();

	if ( stoppedTime == null 
	     || ((stoppedTime.longValue()+rebootDelaySeconds*1000) < this.currentTime )){
	    returnValue = true;
	} 

	return returnValue;
    }

    
    /*
     * inject fault. use when realizing sticky memory-leak.
    */
    private void injectMemoryLeak() throws RuntimeException {

	ObjectName deployerSvc;
	try {
	    deployerSvc = new ObjectName("RR:service=FaultInjection");
	    server.invoke(deployerSvc, "scheduleMemoryLeak", null, null);
	} catch ( Exception e) {
	    log.error( e );
	    
	    /*
	      if invoke() throws exception, then something 
	      is fundamentally wrong, so fail-stop
	    */
	    throw new RuntimeException();
	}
    }
    
    /* 
       updateTime: 
         Update value of StartedTime and StoppedTime of latest microreboot
    */
    private void updateTime(HashMap map, String key) {
	this.currentTime = System.currentTimeMillis();
	map.put((Object)key, (Object) new Long(this.currentTime));
    }

    /**
     * Takes the UID of a component and undeploys it.
     *
     * @jmx:managed-operation
     */
    public String undeploy( String UID ) 
    {
	ObjectName deployerSvc;

	try 
	{
	    deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
	    server.invoke(deployerSvc, "undeploy", 
			  new Object[] { UID }, new String[] { "java.lang.String" });
	} 
	catch ( Exception e ) 
	{
	    e.printStackTrace();
	    return "FAILED: See server.log for stack trace details...";
	}
	return "Successfully undeployed <B>" + UID + "</B>";
    }


    /**
     * Takes the UID of a component and deploys it.
     *
     * @jmx:managed-operation
     */
    public String deploy( String UID ) 
    {
	ObjectName deployerSvc;

	try 
	{
	    deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
	    server.invoke(deployerSvc, "deploy", 
			  new Object[] { UID }, new String[] { "java.lang.String" });
	} 
	catch ( Exception e ) 
	{
	    e.printStackTrace();
	    return "FAILED: See server.log for stack trace details...";
	}
	return "Successfully deployed <B>" + UID + "</B>";
    }

    /**
     * Takes the UID of a component and full-reboots it.
     *
     * @jmx:managed-operation
     */
    public String fullReboot( String UID ) 
	throws Exception
    {
	ObjectName deployerSvc;

	//System.out.println("last Full reboot : " + lastFullRebootStoppedTime );
	//System.out.println("last Full reboot : " + lastFullRebootStoppedTime );
	//System.out.println("before... curtime : " + currentTime);
	currentTime = System.currentTimeMillis();
	//System.out.println("after... curtime : " + currentTime);
	
	if ( lastFullRebootStoppedTime > 0  &&
	     lastFullRebootStoppedTime + 1000 * rebootDelaySeconds > currentTime )
	    throw new MicrorebootTooFrequentException();

        reportReboot(true);
	Thread.sleep(20);
	deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
	server.invoke(deployerSvc, "redeploy", 
		      new Object[] { UID }, new String[] { "java.lang.String" });
	currentTime = System.currentTimeMillis();
	lastFullRebootStoppedTime = currentTime;
	//Thread.sleep(30000);
	reportReboot(false);
	injectMemoryLeak();

	return "Successfully full-rebooted <B>" + UID + "</B>";
    }

    /**
     * Takes simple name of a component and micro-reboots it.
     *
     * @param jarName jar name without ".jar". Do not include path.
     *                ex. User-Item, SB_BrowseCategories, etc.
     *
     * @jmx:managed-operation
     */
    public String microrebootBySimpleArg ( String jarName ) throws Exception
    {
	String pathToJar = Information.getCompleteFileName( "rubis.ear", jarName.trim() + ".jar" );
	try 
	{
	    return microrebootByUrl ( pathToJar );
	}
	catch (MicrorebootTooFrequentException e ) 
	{
	    return "uRB of " + jarName + " has happened lately. Try again later.";
	}
    }

    /**
     * Micorreboot deployed archive file in RUBiS
     *
     * @param name jar file or war file name 
     * @jmx:managed-operation
     */
    public String microrebootArchiveFile ( String name ) throws Exception
    {
	String pathToJar = Information.getCompleteFileName( "rubis.ear", name );
	try 
	{
	    return microrebootByUrl ( pathToJar );
	}
	catch (MicrorebootTooFrequentException e ) 
	{
	    return "uRB of " + name + " has happened lately. Try again later.";
	}
    }



    /**
     * Takes simple name of an application component and full-reboots it.
     *
     * @param earName jar name without ".ear". Do not include path.
     *                ex. rubis
     *
     * @jmx:managed-operation
     */
    public String fullRebootBySimpleArg ( String earName ) throws Exception
    {
	String pathToEar = Information.getCompleteFileName( earName.trim() + ".ear" );
	System.out.println(pathToEar);
	try 
	{
	    return fullReboot ( pathToEar );
	}
	catch (MicrorebootTooFrequentException e ) 
	{
	    return "FRB of " + earName + " has happened lately. Try again later.";
	}
    }
      
 
    /**
     * Start recovery manager in microreboot mode.
     *
     * @param threshold recovery threshold for session EJBs
     *
     * @jmx:managed-operation
     **/
    public String startRecoMgrMicro ( int threshold )
    {
        return startRecoMgr( threshold, false );
    }

    /**
     * Start recovery manager in full reboot mode.
     *
     * @param threshold recovery threshold for session EJBs
     *
     * @jmx:managed-operation
     **/
    public String startRecoMgrFull ( int threshold )
    {
	return startRecoMgr( threshold, true );
    }

    private String startRecoMgr ( int threshold, boolean isFullReboot )
    {
	try {
	    rmThread = new RecoveryManagerThread( this, threshold, isFullReboot);
	    rmThread.start();
	} 
	catch( Exception e ) { 
	    return "FAILED: " + e; 
	}

	if ( isFullReboot )
	    return "Started recovery manager in Full-Reboot mode.";
	else
	    return "Started reco mgr in Microreboot mode.";
    }

    /**
     * Stop recovery manager.
     *
     * @jmx:managed-operation
     **/
    public String stopRecoveryManager ()
	throws Exception
    {
	if ( rmThread == null )
            return "Recovery manager is not running.";

	rmThread.die();
	rmThread = null;
	return "Recovery manager stopped.";
    }


    /**
     * Get timestamp of a component's most recent uRB start time.
     *
     * @param jar    JAR filename corresponding to component
     * 
     * @jmx:managed-operation
     **/
    public long mostRecentRebootStartTime ( String jar ) 
	throws Exception
    {
	String pathToJar = Information.getCompleteFileName( "rubis.ear", jar );
	Long ret = (Long) lastMicrorebootStartedTime.get( pathToJar );

	if ( ret != null )
	    return ret.longValue();
	else
	    return 0;
    }

    /**
     * Get timestamp of a component's most recent uRB end time.
     *
     * @param jar     JAR file corresponding to component
     * 
     * @jmx:managed-operation
     **/
    public long mostRecentRebootEndTime ( String jar ) 
	throws Exception
    {
	String pathToJar = Information.getCompleteFileName( "rubis.ear", jar );
	Long ret = (Long) lastMicrorebootStoppedTime.get( pathToJar );
	
	if ( ret == null )
	    return 0;
	else
	    return ret.longValue();
    }

    public void start()
	throws Exception 
    {
	if ( recoMgrMicroThreshold > 0  &&  recoMgrFullThreshold > 0 )
	    throw new Exception("Can't start both MICRO and FULL; check jboss-service.xml in afpi.sar.");

	if ( recoMgrMicroThreshold > 0 )
	    startRecoMgrMicro( recoMgrMicroThreshold );

	if ( recoMgrFullThreshold > 0 )
	    startRecoMgrFull( recoMgrFullThreshold );
    }

                                                                                     
    /**
      * report reboot incident to the client host.
      *
      * @param isStart if this is start of the reboot, then true,
      *                if this is end of the reboot, then false.
      */
     public void reportReboot(boolean isStart) {

	 if ( clientHostName == null )
	     return;

	 try
	 {
	     DatagramSocket socket = new DatagramSocket();
	     //string to byte stream
	     DatagramPacket packet = new DatagramPacket(new byte[64], 64);
	     String contents = null;
	 if (isStart)
	 {
	     contents = "starting reboot";
	 }
	 else
	 {
	     contents = "reboot finished";
	 }
	 byte[] byteContents = contents.getBytes();
	 packet = new DatagramPacket(byteContents, byteContents.length, InetAddress.getByName(clientHostName), clientPort);
	 
	 System.out.println("sent to " + clientHostName + " port " + clientPort);
	 
	 //send
	 socket.send(packet);
	 }
	 catch(Exception e)
	 {
	     e.printStackTrace();
	 }
     }



    public void stop() 
    {
	try {
	    stopRecoveryManager();
	}
	catch( Exception e ) {
	    log.error( "Couldn't stop reco mgr: " + e );
	}
    }
 
    public void create() throws Exception { this.info = new Information(server); }
    public void destroy()                 {}
}
