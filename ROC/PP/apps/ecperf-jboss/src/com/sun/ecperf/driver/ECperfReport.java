/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ECperfReport.java,v 1.1 2004/02/19 14:45:06 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import java.io.*;
import java.util.*;

/**
 * This class is the Report generator for the ECperf experiment.
 * It generates the reports from the OrderEntry and Mfg workload runs
 * and also the final cumulative report.
 *
 * @see OrdersReport
 * @see MfgReport
 * @author Shanti Subramanyam
 */
public class ECperfReport {
	// Change version number below when generating a new driver version
	public static final String version = "ECperf 1.1 Final Release";
	String summary;
	double txPerMin, workOrdersPerMin;
	int users, stdyState;
	boolean doords = false, domfg = false;
	RunInfo runInfo;

        // Added by Ramesh to get txCounts for Auditing
        OrdersReport ordsReport = null;
        MfgReport    mfgReport  = null;

	/**
	 * Method : generateReport
	 * This is the method required by the Reporter interface to generate
	 * the final reports for a particular MWBench run.
	 * A seperate set of reports is generated for each set within
	 * the Experiment. All the reports live in a run directory
	 * (called <username>.<runid>) in the home directory
	 * of the user running the experiment.
	 * The final ECperf summary report is in a file called
	 * ecperf.summary<setnum> where the suffix setnum refers to the set
	 * within the experiment for which this report was generated.
	 * @param e - Experiment that was run
	 */
	public void generateReport(RunInfo runInfo, 
		OrdersAggStats ordersResults[], MfgStats mfgResults[]) {
		this.runInfo = runInfo;
		String resultsDir = null;

		/*
		 * Each element in the results vector is the result from
		 * one agent (repeated across sets). Each element is an array 
		 * of serializables, one per workload.
		 * We walk through the results vector to find out how many
		 * results are from the OrdersApp.
		 */

		int numOrds = 0, numMfg = 0;
		if (ordersResults != null) {
			numOrds = ordersResults.length;
			doords = true;
		}
		if (mfgResults != null) {
			numMfg = mfgResults.length;
			domfg = true;
		}
		if (numOrds > 0) {
			Debug.println("ECperfReport: Printing report from " + 
				numOrds + " OrdersAgents");
			resultsDir = (ordersResults[0]).resultsDir;
			try {
                                ordsReport = new OrdersReport();
				txPerMin = ordsReport.genReport(ordersResults);
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
		if (numMfg > 0) {
			Debug.println("ECperfReport: Printing report from " + numMfg +
				" Mfg Agents");
			resultsDir = (mfgResults[0]).resultsDir;
			try {
                                mfgReport = new MfgReport();
				workOrdersPerMin = mfgReport.genReport(mfgResults, runInfo.txRate);
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
		String filesep = System.getProperty("file.separator");
		summary = resultsDir + filesep + "ECperf.summary";
		System.out.println("summary file is " + summary);
		try {
			PrintStream sump = new PrintStream(new FileOutputStream(summary));
			printSummary(sump);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}



	// Print summary report
	private void printSummary(PrintStream p) throws IOException {
		p.println();
		p.println("\t\t\tECPerf Summary Report");
		p.println("\t\t\tVersion : " + version);
		p.println("\n");
		p.println("Run Parameters : ");
		p.print("runOrderEntry = ");
		Format.print(p, "%d", runInfo.runOrderEntry);
		p.println();
		p.print("runMfg = ");
		Format.print(p, "%d", runInfo.runMfg);
		p.println();
		if (runInfo.runLargeOrderLine == false)
			p.println("LargeOrderLine was not run. Non-compliant run");
		p.print("txRate = ");
		Format.print(p, "%d", runInfo.txRate);
		p.println();
		p.print("rampUp (in seconds) = ");
		Format.print(p, "%d", runInfo.rampUp/1000);
		p.println();
		p.print("rampDown (in seconds) = ");
		Format.print(p, "%d", runInfo.rampDown/1000);
		p.println();
		p.print("stdyState (in seconds) = ");
		Format.print(p, "%d", runInfo.stdyState/1000);
		p.println();
		p.print("triggerTime (in seconds) = ");
		Format.print(p, "%d", runInfo.triggerTime);
		p.println();
		// p.print("benchStartTime = ");
		// Format.print(p, "%d", runInfo.benchStartTime);
		// p.println();
		p.print("numOrdersAgents = ");
		Format.print(p, "%d", runInfo.numOrdersAgents);
		p.print(", numMfgAgents = ");
		Format.print(p, "%d", runInfo.numMfgAgents);
		p.println();
		p.print("dumpStats = ");
		Format.print(p, "%d", runInfo.dumpStats);
		p.println();
		Date d = new Date(runInfo.start);
		p.println("Benchmark Started At : " + d.toString());
		p.println();
		p.println();
		if (doords) {
		p.println("Orders Summary report is in : Orders.summary");
		p.println("Orders Detailed report is in : Orders.detail");
		p.print("Orders Transaction Rate : "); 
		Format.print(p, "%.02f Transactions/min", txPerMin);
		p.println();
		p.println();
		}
		if (domfg) {
		p.println("Manufacturing Summary report is in : Mfg.summary");
		p.println("Manufacturing Detail report is in : Mfg.detail");
		p.print("Manufaturing Rate : "); 
		Format.print(p, "%.02f WorkOrders/min", workOrdersPerMin);
		p.println();
		p.println();
		}
		if (doords && domfg) {
			p.print("ECperf Metric : ");	
			Format.print(p, "%.02f BBops/min", txPerMin + workOrdersPerMin);
			p.println();
			p.println();
		}
		p.close();
	}

}
