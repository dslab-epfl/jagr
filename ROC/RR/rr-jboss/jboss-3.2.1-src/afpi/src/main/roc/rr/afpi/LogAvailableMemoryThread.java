/* $Id: LogAvailableMemoryThread.java,v 1.2 2004/04/27 16:41:39 skawamo Exp $ */

/*
 *  LogAvailableMemoryThread.java 
 *     log available and total memory periodically
 *
 *      Apr/07/2004  S.Kawamoto
 */

package roc.rr.afpi;

import java.util.*;
import java.sql.*;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import org.jboss.deployment.*;
import org.jboss.logging.Logger;

public class LogAvailableMemoryThread extends Thread {
    private MBeanServer server = null;
    private Logger log = null;
    private int interval;

    //
    // constructor
    // 
    public LogAvailableMemoryThread(MBeanServer server,
				    Logger log, int interval){
	this.server  = server;
	this.log = log;
	this.interval = interval*1000;  // transform second to milisecond
    }

    //
    // infinite loop for measuring 
    //
    public void run() {
	while ( true ) {
	    try {
		ObjectName serverInfo = 
		    new ObjectName("jboss.system:type=ServerInfo");
		long availBytes 
		    = ((Long) server.getAttribute(serverInfo, "FreeMemory")).longValue();
		long totalBytes 
		    = ((Long) server.getAttribute(serverInfo, "TotalMemory")).longValue();

		log.info("Available memory[B]: " + availBytes 
			 + "  Total memory[B]: "+totalBytes);

		// 
		// sleep for interval milliseconds
		// 
		this.sleep(interval);

	    } catch (Exception e) {
		log.info(" Error in logAvailableMemory: "+e);
	    }
	}
    }
}
