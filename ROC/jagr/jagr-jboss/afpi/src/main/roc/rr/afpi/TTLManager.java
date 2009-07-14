/*
 * $Id: TTLManager.java,v 1.1 2004/06/08 13:33:04 emrek Exp $
 *
 * TTLManager: manage teime to live (TTL) of each ejb. 
 *
 */
package roc.rr.afpi;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;
import java.util.Iterator;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import org.jboss.logging.Logger;
import roc.rr.afpi.util.Information;
import roc.rr.afpi.util.XMLParser;

public class TTLManager {
    private MBeanServer server;
    private Logger log;                                   
    private static TTL ttlMap;                            // TTL Map
    private static TreeMap scheduleMap = new TreeMap();   // Schedule
    private Timer timer = new Timer();                    // Timer
    private long startOffset;       // adjust timing of first microreboot
    private long disperseOffset;    // disperse the timing of microreboots


    /**
     * Constructor
     */
    TTLManager(MBeanServer server, Logger log, 
	       int startOffset, int disperseOffset){
	this.server = server;
	this.log = log;
	this.startOffset = startOffset;
	this.disperseOffset = disperseOffset;
    }

    /**
     * Load xml configuration file and setup TTL map
     */
    public void loadTTL() throws Exception {

	// get configuration XML file name
	String fileName = null;
	ObjectName serverInfo 
	    = new ObjectName("jboss.system:type=ServerConfig");
	String dir = (String) ((File)server.getAttribute(serverInfo, "ServerHomeDir")).getAbsolutePath();
	fileName = dir + "/conf/ttl.xml";
	
	// Load ttl.xml and extract data
	XMLParser parser = XMLParser.getInstance();
	ttlMap = parser.parseTTL(fileName);

	// Set microrebooting schedule for each ejb
	long offset = startOffset;
	Set jars = ttlMap.getAllJarName();
	Iterator iter = jars.iterator();
	while (iter.hasNext()){
	    String jarName = (String)iter.next();
	    addNewEvent(jarName,offset);
	    offset += disperseOffset;
	}
    }

    /**
     * Extract the first event of scheduleMap and set it to timer
     */
    public void setTimer() {
	// extract the first event from scheduleMap
	Long   time = (Long) scheduleMap.firstKey();
	String rebootJarName = (String) scheduleMap.get((Object)time);
	scheduleMap.remove((Object)time);

	// create new timer task
	RebootTimerTask task = new RebootTimerTask(rebootJarName);

	// set timer
	timer.schedule(task,new Date(time.longValue()));
    }

    /**
     * Stop timer
     */
    public void stopTimer() {
	timer.cancel();
    }

    /**
     * Each microreboot event is put into scheduleMap
     */
    public void addNewEvent(String jarName) {
	long interval = ttlMap.get(jarName);
	long endTime = System.currentTimeMillis()+interval;
	scheduleMap.put((Object)new Long(endTime),(Object)jarName);
    }

    /**
     * Each microreboot event is put into scheduleMap 
     * Use only for initializing schedule
     */
    public void addNewEvent(String jarName,long offset) {
	long interval = ttlMap.get(jarName);
	long endTime = System.currentTimeMillis()+interval+offset;
	scheduleMap.put((Object)new Long(endTime),(Object)jarName);
    }


    /**
     * RebootTask will be invoked by timer and microreboot a jar file.
     */
    private class RebootTimerTask extends TimerTask {
	private String jarName;

	//
	// constructor
	//
	RebootTimerTask(String jarName){
	    this.jarName = jarName;
	}
	    
	//
	// RebootTimer task main code
	//
	public void run() {
	    // microreboot target jar file
	    microReboot(jarName);
	    // add new microreboot event of this jar file to scheduleMap
	    addNewEvent(jarName);
	    // set timer for next event
	    setTimer();
	}


	//
	// microreboot target jar file
	//
	private void microReboot(String targetJar) {
	    String UID = null;

	    try {
		Information info = new Information(server);
		UID = info.getCompleteFileName("rubis.ear",targetJar);
		ObjectName deployerSvc
		    = new ObjectName("RR:service=RecoveryControl");
		server.invoke(deployerSvc, "microrebootAndInjectFault",
			      new Object[] { UID },
			      new String[] { "java.lang.String" });

		// Invoke gc in order to reclaime leaked memory
		System.gc();
		log.info("Life time of "+targetJar
			 +" has expired and it was rebooted successfully");
	    } catch (Exception e) {
		e.printStackTrace();
		log.info("Can't microreboot "+UID+" : "+e);
	    }
	}
    }
}

