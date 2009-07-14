/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: LargeOrderLine.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
 *
 */
package com.sun.ecperf.driver;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import java.net.*;
import java.io.*;
import java.util.*;

import com.sun.ecperf.mfg.largeorderses.ejb.*;
import com.sun.ecperf.mfg.workorderses.ejb.*;
import com.sun.ecperf.mfg.helper.*;

/**
 * This is the class that implements the LargeOrderLine driver. 
 * This driver will call the get method of the LargeOrderBean every
 * second to see if there are any new large orders. It will then
 * create workorders and cycle them through the stations just like
 * the PlannedLine does.
 *
 * @see PlannedLine
 * @author Shanti Subramanyam
 */
public class LargeOrderLine extends PlannedLine {
	LargeOrderSes largeorder;

	public LargeOrderLine(int id, Timer timer, Properties props) {
		super(id, timer, props);
	}

	protected String getErrFileName() {
		String errfile = resultsDir + 
						System.getProperty("file.separator") + 
						"loline.err";
		return(errfile);
	}

	protected void getBeanHomes() {
		String lohome;
		String prefix = props.getProperty("homePrefix");
                // The homePrefix will have the trailing '/'
		if (prefix != null) {
			lohome = prefix + "LargeOrderSes";
			wohome = prefix + "WorkOrderSes";
		}
		else {
			lohome = "LargeOrderSes";
			wohome = "WorkOrderSes";
		}
                // Debug.println("lohome = " + lohome);
                // Debug.println("wohome = " + wohome);

		try {
			LargeOrderSesHome lrgOrdHome = 
				(LargeOrderSesHome) PortableRemoteObject.narrow
				(ctx.lookup(lohome), LargeOrderSesHome.class);
			largeorder = lrgOrdHome.create();
		} catch (Exception e) {
			errp.println(ident + "Exception in creating LargeOrderBean" + e);
			start = false;
		}
	}

	/**
	 * Each thread executes in the run method until the benchmark time is up
	 * creating workorders and running them to completion.
 	 * The stats for the entire run are stored in an MfgStats object
 	 * which is returned to the MfgAgent via the getResult() method.
	 * @see MfgStats
	 */
	public void run() {
		int tx_type;
		int delay, endTime;

		getReady();		// Perform inits
		stats.numPlannedLines = 0;

		if (start == false)	// If error occured during setup, do not run
			return;

		// If we haven't reached the benchmark start time, sleep
		delay = benchStartTime - timer.getTime();
		if (delay <= 0) {
			errp.println(ident + "Warning: triggerTime has expired. Need " + (-delay) + " ms more");
		}
		else {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
			}
		}
		inRamp = true;
		// Loop until time is up
		while (true) {
			
			// Do work
			doLargeOrders();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
			}
			
			endTime = timer.getTime();
			// Debug.println(ident + "endTime = " + endTime);
			if (endTime >= endRampUp && endTime < endStdyState)
				inRamp = false;
			else
				inRamp = true;
			if (endTime >= endRampDown)
				break;
		}
		// Debug.println(ident + "Widget cnt = " + stats.widgetCnt);
		endRun();

		// Now sleep forever. We can't exit, as if we do, the thread
		// will be destroyed and the OrdersAgent won't be able to
		// retrieve our stats.
		while ( !statsDone) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ie) {
			}
		}
		Debug.println(ident + " Exiting...");
	}
	
	private void doLargeOrders() {
		Vector loi;

		// Check if there are any waiting largeorders
		try {
			loi = largeorder.findLargeOrders();
		} catch (Exception e) {
			errp.println(ident + "Error occured in findLargeOrders " + e);
			return;
		}
		if (loi.size() > 0) {
			LrgLine lrgLine[] = new LrgLine[loi.size()];
			MfgStats lstats[] = new MfgStats[loi.size()];
			int i;
			for (i = 0; i < loi.size(); i++) {
				lstats[i] = new MfgStats(0, resultsDir);
				lstats[i].setStdyState(stdyState);
				lrgLine[i] = new LrgLine((LargeOrderInfo)(loi.elementAt(i)), lstats[i]);	
			}

			// Now wait for all lines to finish and aggregate stats
			for (i = 0; i < loi.size(); i++) {
				try {
					lrgLine[i].join();
				} catch (InterruptedException ie) {
				}
				stats.addResult(lstats[i]);
			}
		}
	}

	/**
	 * This method is called at the end of the run to do cleanup
	 * operations
	 */
	protected void endRun() {
		// End of run, destroy bean
		try {
			largeorder.remove();
		} catch (Exception e) {
			errp.println(ident + " Error in removing beans " + e);
		}
	}


	/**
	 * Class LrgLine
	 * This class spawns and manages a workorder for a specific large order
	 */
	private class LrgLine extends Thread {
		private MfgStats stats;
		private LargeOrderInfo loi;
		private WorkOrderSes workorder;
	
		LrgLine(LargeOrderInfo loi, MfgStats stats) {
			this.loi = loi;
			this.stats = stats;
			start();
		}

		public void run() {
			boolean start = true;
			try {
			// Create an WorkOrderSes object 
			WorkOrderSesHome workOrderSesHome = 
				(WorkOrderSesHome) PortableRemoteObject.narrow
				(ctx.lookup(wohome), WorkOrderSesHome.class);
			workorder = workOrderSesHome.create();
			} catch (NamingException e) {
				errp.println(ident + "Failure looking up home " + e);
				start = false;
			} catch (Exception ex) {
				errp.println(ident + "Failure in creating bean " + ex);
				start = false;
			}
			if (start) {
/****
				createStations();
****/
				createWidget(workorder, stats, loi);
/****
				// Now destory the stations, as we no longer need them
				for (int i = 0; i < stations.length; i++) {
					stations[i].quit();
				}
****/
			}
			return;
		}
	}
}
