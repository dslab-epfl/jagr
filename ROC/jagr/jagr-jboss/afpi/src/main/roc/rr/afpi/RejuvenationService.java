/* $Id: RejuvenationService.java,v 1.1 2004/06/08 13:33:04 emrek Exp $ */

package roc.rr.afpi;

import java.util.*;
import java.sql.*;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.*;

import roc.rr.*;
import roc.rr.afpi.util.XMLParser;

/** MBean for the AFPI rejuvenation control service.
 *      
 *   @author  skawamo@stanford.edu
 *   @version 
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 **/

public class RejuvenationService extends ServiceMBeanSupport
    implements RejuvenationServiceMBean {

    private Thread rrjThread = null;      // reactive rejuvenation thread
    private TTLManager ttlManager = null; // TTL Manager

    final static String REBOOTMODE_FULL = "full";
    final static String REBOOTMODE_MICRO = "micro";

    //
    // MBean Attribute
    //
    private int startThreshold; // MBytes for starting a series of reboots
    private int stopThreshold;  // MBytes for stoping a series of reboots
    private int watchInterval;       // interval in seconds for checking memory
    private int microRebootInterval; // interval in seconds for rebooting
    private String rebootMode = "micro";  // either the value of REBOOTMODE_FULL or REBOOTMODE_MICRO

    // getter and setter for MBean attributes

    /**
     * getter for startThreshold
     * @jmx:managed-attribute
     **/
    public int getStartThreshold(){
	return startThreshold;
    }

    /**
     * setter for startThreshold
     * @param startThreshold  threshold of available memory for starting reboots
     * @jmx:managed-attribute
     **/
     public void setStartThreshold(int startThreshold){
	this.startThreshold = startThreshold;
    }

    /**
     * getter for stopThreshold
     * @jmx:managed-attribute
     **/
    public int getStopThreshold(){
	return stopThreshold;
    }

    /**
     * setter for stopThreshold
     * @param stopThreshold  threshold of available memory for stopping reboot
     * @jmx:managed-attribute
     **/
    public void setStopThreshold(int stopThreshold){
	this.stopThreshold = stopThreshold;
    }

    /**
     * getter for watchInterval
     * @jmx:managed-attribute
     **/
    public int getWatchInterval(){
	return watchInterval;
    }

    /**
     * setter for watchInterval
     * @param watchInterval  interval for checking available memory
     * @jmx:managed-attribute
     **/
    public void setWatchInterval(int watchInterval){
	this.watchInterval = watchInterval;
    }

    /**
     * getter for microRebootInterval
     * @jmx:managed-attribute
     **/
    public int getMicroRebootInterval(){
	return microRebootInterval;
    }

    /**
     * setter for microRebootInterval
     * @param microRebootInterval  interval for rebooting each ejb
     * @jmx:managed-attribute
     **/
    public void setMicroRebootInterval(int microRebootInterval){
	this.microRebootInterval = microRebootInterval;
    }


    /**
     * getter for rebootMode
     * @jmx:managed-attribute
     **/
    public String getRebootMode(){
	return rebootMode;
    }

    /**
     * setter for rebootMode
     * @param rebootMode   either "full" or "micro" 
     * @jmx:managed-attribute
     **/
    public void setRebootMode(String rebootMode) throws Exception {
	if ( rebootMode.equals(REBOOTMODE_FULL) 
	     || rebootMode.equals(REBOOTMODE_MICRO) ){
	    this.rebootMode = rebootMode;
	} else {
	    throw new Exception();
	}
    }


    //////////////////////////
    // Reactive Rejuvenation// 
    //////////////////////////
   
    /**
     * start reactive micro rejuvenation
     *
     * @jmx:managed-operation
     */
    public String startReactiveRejuvenation()
    {
	String message = null;
	
	if (rrjThread == null){
	    //
	    // Convert unit of start and stop thresholds
	    // from percentage of the total memory to absolute 
	    // amount of memory in mega bytes.
	    // 

	    // get total memory in MBytes
	    int totalMB = 0; 
	    try {
		ObjectName serverInfo 
		    = new ObjectName("jboss.system:type=ServerInfo");
		totalMB = (int) ((Long) server.getAttribute(serverInfo, "TotalMemory")).longValue() / (1024*1024);
	    } catch (Exception e) {
		e.printStackTrace();
		message = "Can't get total memory: "+e;
		log.error(message);
		return message;
	    }
	    
	    int startMB = totalMB*startThreshold/100;
	    int stopMB  = totalMB*stopThreshold/100;

	    // initialize reactive rejuvenation thread 
	    rrjThread = new ReactiveRejuvenationThread(server,
						       log,
						       startMB,
						       stopMB,
						       watchInterval,
						       microRebootInterval,
						       rebootMode);
	    rrjThread.start();
	    message = "Start reactive "+rebootMode
		+" rejuvenation [startThreshold:"
		+startThreshold+"%("+startMB+"MB), stopThreshold:"
		+stopThreshold+"%("+stopMB+"MB)]";
	    log.info(message);
	} else {
	    message = "Reactive full or micro rejuvenation is running";
	}

	return message;
    }


    /**
     * stop reactive rejuvenation
     *
     * @jmx:managed-operation
     */
    public String stopReactiveRejuvenation()
    {
	String message;

	if ( rrjThread != null ){
	    rrjThread.stop();
	    rrjThread = null;
	    message = "Stopped reactive rejuvenation";
	} else {
	    message = "Reactive rejuvenation hasn't started yet";
	}
	return message;
    }


    ///////////////////////////////
    // Rolling Microrejuvenation //
    ///////////////////////////////

    int startOffset = 0;
    int disperseOffset = 0;

    /**
     *  Start rolling microrejuvenation
     *
     * @jmx:managed-operation
     */
    public String startRollingMicrorejuvenation()
    {
	String message;

	// initialize TTLManager
	ttlManager = new TTLManager(server,log,startOffset,disperseOffset);
	
	try {
	    // Load TTL Map from ttl.xml
	    ttlManager.loadTTL();
	    // Set timer
	    ttlManager.setTimer();

	    message = "Started rolling microrejuvenation successfully.";
	} catch (Exception e){
	    message = "Can't load ttl.xml because of "
		+e+"\nSee log for details.";
	}
	    
	return message;
    }

    /**
     * Stop rolling microrejuvenation
     *
     * @jmx:managed-operation
     */
    public String stopRollingMicrorejuvenation()
    {
	ttlManager.stopTimer();
	return "Stopped rolling microrejuvenation.";
    }

    /**
     * Set startOffset and disperseOffset for rolling microrejuvenation
     *   for tweaking the microreboot timing
     *
     * @param startOffset  offset in second for adjusting the start time of every ejbs
     * @param disperseOffset offset in second for dispersing the reboot timing of each ejb
     * @jmx:managed-operation
     */
    public String setRollingRejuvenationParameter(int startOffset, 
						  int disperseOffset)
    {
	this.startOffset = startOffset*1000;
	this.disperseOffset = disperseOffset*1000;
	return "Set parameters: start offset: "+startOffset
	    +" sec, disperse offset: "+disperseOffset+" sec";
    }



    public void create() throws Exception {}
    public void start() throws Exception  {}
    public void stop()                    {}
    public void destroy()                 {}
}
