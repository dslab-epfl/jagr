/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: PlannedLine.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import java.net.*;
import java.io.*;
import java.util.*;

import com.sun.ecperf.mfg.workorderses.ejb.*;
import com.sun.ecperf.mfg.helper.*;

/**
 * This class implements the Planned Line of the Manufacturing 
 * Application of the ECperf workload. The MfgAgent will create
 * the PlannedLine threads and they run for the benchmark duration.
 *
 * @see MfgAgent
 * @see MfgStats
 * @see MfgReport
 * @author Shanti Subramanyam
 */
public class PlannedLine extends Thread {
	int id;
	Timer timer;
	Properties props;
	boolean inRamp;
	int rampUp, stdyState, rampDown;
	int endRampUp, endStdyState, endRampDown;
	int numPlannedLines, timePerTx, txRate;
	int benchStartTime;		// Actual time of rampup start
	String ident, resultsDir;
	Context ctx;
	boolean start = true, statsDone = false;
	MfgStats stats;		// keeps track of aggregate stats
	PrintStream errp;
	Station stations[];
	int numItems;
	RandNum r;
	RandPart rp;
	String wohome;
	WorkOrderSes workorder;

	/**
	 * Constructor 
	 * @param id of this planned line (used in error msgs)
	 * @param Timer object to use for timing functions
	 * @param Properties of the run
	 */
	public PlannedLine(int id, Timer timer, Properties props) {
		this.id = id;
                // Get an initial context
                try {
                    ctx = new InitialContext();
                } catch (NamingException ne) {
                    errp.println(ident + " : InitialContext failed. : " + ne);
                }
		this.timer = timer;
		this.props = props;
		start();
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
		int delay, startTime, endTime;

		getReady();		// Perform inits
		if (start == false)	// If error occured during setup, do not run
			return;
		/**
	 	 * Getting rid of stations, as in rare cases there can
		 * be conflict between multiple threads - createWidget will do
		 * status updates. Shanti 5/2/01 	
		createStations();
		 */

		// If we haven't reached the benchmark start time, sleep
		delay = benchStartTime - timer.getTime();
		if (delay <= 0) {
			errp.println(ident + "Warning: triggerTime has expired. Need " + (-delay) + " ms more");
		}
		else {
			// We vary the sleep time a bit
			try {
				Thread.sleep(delay + id*5);
			} catch (InterruptedException ie) {
			}
		}
		inRamp = true;
		// Loop until time is up
		while (true) {
			
			// Do work
			// Create workorder, send it thru stations
			startTime = timer.getTime();
			createWidget(workorder, stats);
			endTime = timer.getTime();
			delay = endTime - startTime;
			// Debug.println(ident + "delay after createWidget = " + delay);
			if (delay < timePerTx) {
				try {
					Thread.sleep(timePerTx - delay);
				} catch (InterruptedException ie) {
				}
			}
			endTime = timer.getTime();
			if (endTime >= endRampUp && endTime < endStdyState)
				inRamp = false;
			else
				inRamp = true;
			if (endTime >= endRampDown)
				break;
		}
		Debug.println(ident + "Widget cnt = " + stats.widgetCnt);
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

	/**
	 * Return result of running this PlannedLine
	 * @return serializable form of MfgStats
	 * @see MfgStats
	 */
	public java.io.Serializable getResult() {
		// Debug.println(ident + "Returning stats");
		statsDone = true;
		return(stats);
	}

	public java.io.Serializable getCurrentResult() {
		return(stats);
	}


	/**
	 * This method is called from configure to open and read the
	 * parameter file and set up instance variables from it. It also
	 * create an error log in the run directory and does a lookup
	 * on the OrdersBean home interface
	 * @param none
	 * @return none
	 */
	protected void getReady() {
		resultsDir = props.getProperty("runOutputDir");
		String errfile = getErrFileName();

		// Get our thread name and append it to the Agent name to
		// uniquely identify ourselves
		String name = props.getProperty("agentName");
		ident = name.concat(":" + id + ": ");
		// Debug.println("In getReady of thread " + ident);
		// Create error log if it doesn't already exist
		try {
			if (new File(errfile).exists()) {
				errp = new PrintStream(new FileOutputStream(errfile, true));
			}
			else {	// try creating it
				Debug.println(ident + "Creating " + errfile);
				errp = new PrintStream(new FileOutputStream(errfile));
			}
		} catch (Exception e) {
			System.err.println(ident + "Could not create " + errfile);
			errp = System.err;
		}

		// Get some properties
		numPlannedLines = Integer.parseInt(props.getProperty("plannedLines"));
		double woRatePerAgent = Double.parseDouble(props.getProperty("woRatePerAgent"));
		txRate = Integer.parseInt(props.getProperty("txRate"));
		stats = new MfgStats(numPlannedLines, resultsDir);	

		// compute our mean arrival rate in msecs
		timePerTx = (int)((numPlannedLines *1000)/ woRatePerAgent);
		// Debug.println("timePerTx = " + timePerTx);

		// Calculate time periods
		benchStartTime = Integer.parseInt(props.getProperty("benchStartTime"));
		rampUp = Integer.parseInt(props.getProperty("rampUp"));
		stdyState = Integer.parseInt(props.getProperty("stdyState"));
		stats.setStdyState(stdyState);
		rampDown = Integer.parseInt(props.getProperty("rampDown"));
		endRampUp = benchStartTime + rampUp;
		endStdyState = endRampUp + stdyState;
		endRampDown = endStdyState + rampDown;
/****
		Debug.println(ident + "rampup end time = " + endRampUp + 
			", stdy endtime = " + endStdyState + 
			", rampdown endtime = " + endRampDown);
****/
		System.out.println(ident + " started");
		long seed = timer.getTime() + this.hashCode();
		r = new RandNum(seed);		// Seed random number generator
		/*** This should be the final version
		rp = new RandPart(r, txRate);
		****/
		numItems = (int)(Math.ceil((double)txRate/100.0)) * 100;
		rp = new RandPart(r, numItems, 1);

		getBeanHomes();
	}

	protected String getErrFileName() {
		String errfile = resultsDir + 
						System.getProperty("file.separator") + 
						"plannedlines.err";
		return(errfile);
	}

	protected void getBeanHomes() {
		try {
			// Create an WorkOrderSes object 
			String prefix = props.getProperty("homePrefix");
                        // The homePrefix will have the trailing '/'
			if (prefix != null) {
				wohome = prefix + "WorkOrderSes";
			}
			else {
				wohome = "WorkOrderSes";
			}
			// Debug.println("wohome = " + wohome);
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
	}

/*****
	protected void createStations() {
		// Now create the stations
		stations = new Station[3];
		for (int i = 0; i < 3; i++) {
			stations[i] = new Station(ctx, ident, errp, wohome, i+1);
		}
	}
*****/

	void createWidget(WorkOrderSes workorder, MfgStats stats) {
		createWidget(workorder, stats, null);
	}

	void createWidget(WorkOrderSes workorder, MfgStats stats, 
			LargeOrderInfo lrgOrderInfo) {
		int startTime, endTime, elapsedTime, respTime;
		int qty, avg, peak;
		Integer woId = new Integer(0);
		int woStatus = 1;
		boolean fail = false;
		// 75% of the time, choose a qty of 11
		// 25% of the time, choose a qty of 12
		// This will result in a mean of 11.25
		int x = r.random(1, 100);
		if (x <= 75) {
			qty = 11;
		}
		else {
			qty = 12;
		}
/******
		// Pick a qty to manufacture
		int qty = r.random(1, peak);
		int qty = r.random(22, 23);
*****/
		// select a part number
		String assembly = rp.getPart();

		// Create workorder
		startTime = timer.getTime();
		try {
			if (lrgOrderInfo == null) {
				// Debug.println(ident + "PlannedLine: creating " + qty + " of workorder for " + assembly);
				woId = workorder.scheduleWorkOrder(assembly, qty, 
					new java.sql.Date((new java.util.Date()).getTime()));
			}
			else  {
				// Debug.println(ident + "LargeOrderLine: creating " + 
				// 	lrgOrderInfo.qty + " of workorder for " + lrgOrderInfo.assemblyId);
				woId = workorder.scheduleWorkOrder(
					lrgOrderInfo.salesOrderId, lrgOrderInfo.orderLineNumber,
					lrgOrderInfo.assemblyId, 
					lrgOrderInfo.qty, lrgOrderInfo.dueDate);
			}
		} catch (Exception e) {
			errp.println(ident + "Error occured in scheduleWorkOrder" + e);
			fail = true;
		}

		// Now simulate activity at the 3 stations, sleeping
		// for a fixed 0.3333secs and updating status
		// Debug.println(ident + "workorder created for " + assembly);
		if ( ! fail) {
			for (int i = 0; i < 3; i++) {
				try {
					sleep(333);
				} catch (InterruptedException ie) { }
				try {
				if ( i == 2) {
					woStatus = 2;
					workorder.completeWorkOrder(woId);
				}
				else {
					woStatus = 1;
					workorder.updateWorkOrder(woId);
				}
				} catch (Exception e) {
					errp.println(ident + 
					"PlannedLine Exception in update WorkOrder(" + 
					woId + ", " + woStatus + ") " + e);
					fail = true;
					break;
				}
			}
		}
		endTime = timer.getTime();
		respTime = endTime - startTime;
		elapsedTime = endTime - benchStartTime;
		if ( ! fail) {
		if ((elapsedTime / MfgStats.THRUBUCKET) >= MfgStats.THRUMAX)
			stats.thruput[MfgStats.THRUMAX - 1]++;
		else
			stats.thruput[elapsedTime / MfgStats.THRUBUCKET]++;

		if ( !inRamp && endTime <= endStdyState) {
			if (lrgOrderInfo != null) {
				stats.largeOrderCnt++;
				stats.largeOrderWidgetCnt += lrgOrderInfo.qty;
			}
			else
				stats.widgetCnt += qty;	// PlannedLine cnt
			stats.workOrderCnt++;
			stats.respTime += respTime;
			if (respTime > stats.respMax)
				stats.respMax = respTime;
			if ((respTime / MfgStats.RESPBUCKET) >= MfgStats.RESPMAX)
				stats.respHist[MfgStats.RESPMAX - 1]++;
			else
				stats.respHist[respTime / MfgStats.RESPBUCKET]++;
		}
		}
	}

	/**
	 * This method is called at the end of the run to do cleanup
	 * operations
	 */
	protected void endRun() {
/****
		for (int i = 0; i < stations.length; i++) {
			stations[i].quit();
		}
****/
		// End of run, destroy bean
		Debug.println(ident + "End of run. Removing beans");
		try {
			workorder.remove();
		} catch (Exception e) {
			errp.println(ident + " Error in removing workorder bean " + e);
		}
	}

}
