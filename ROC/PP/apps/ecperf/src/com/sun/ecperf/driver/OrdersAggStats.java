/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrdersAggStats.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import java.io.*;
import java.net.*;

/**
 * This class computes the aggregate stats of all the OrdersApp threads
 * run from a particular agent. A single instance of this class should be
 * instantiated
 *
 * @see OrdersStats
 * @see OrdersApp
 * @author Shanti Subramanyam
 */
 public class OrdersAggStats implements java.io.Serializable {

	String logfile;	// name of output log file
	int threadCnt, stdyState, txRate;
	String resultsDir;
	boolean debug = false;
	int sumNewoBuyCart = 0, sumNewoBadCredit = 0, sumCancelOrdCnt = 0;
        int sumChgoBadCredit = 0;
	int sumNewoLrgCount = 0, sumNewoOlCnt = 0, sumNewoLrgOlCnt = 0;
	String txNames[] = {"Newo", "Chgo", "Ords", "Custs"};

	/* Stats for all transaction types */

	int txCnt[] = new int[OrdersStats.TXTYPES];			/* number of transactions */
	int respMax[] = new int[OrdersStats.TXTYPES];			/* Max. response time */
	double respSum[] = new double[OrdersStats.TXTYPES];		/* Sum of response times */
	double cycleSum[] = new double[OrdersStats.TXTYPES];		/* sum of cycle times */
	double targetedCycleSum[] = new double[OrdersStats.TXTYPES];		/* sum of cycle times */
	int cycleMax[] = new int[OrdersStats.TXTYPES];
	int cycleMin[] = new int[OrdersStats.TXTYPES];
	double elapse[] = new double[OrdersStats.TXTYPES];		/* sum of elapsed times */
	int respHist[][] = new int[OrdersStats.TXTYPES][OrdersStats.RESPMAX];/* Response time histogram */
	int cycleHist[][] = new int[OrdersStats.TXTYPES][OrdersStats.CYCLEMAX];	/* Think time histogram */
	int targetedCycleHist[][] = new int[OrdersStats.TXTYPES][OrdersStats.CYCLEMAX];	/* Think time histogram */
	int thruputHist[][] = new int[OrdersStats.TXTYPES][OrdersStats.THRUMAX];	/* Thruput histogram */

	/**
 	 * This method aggregates the stats of all the threads on this RTE machine
 	 * It is called repeatedly, and the called passes it the stats of a different
 	 * thread, each time 
 	 * @param OrdersStats stats of next thread to be aggregated
   	 *
	 */
	public void addResult(OrdersStats s) {
		int j;
		txRate = s.txRate;
		threadCnt = s.threadCnt;
		stdyState = s.stdyState;
		resultsDir = s.resultsDir;

		/* Neworder info */
		sumNewoBuyCart += s.newoBuyCart;
		sumNewoBadCredit += s.newoBadCredit;
                sumChgoBadCredit += s.chgoBadCredit;
		sumNewoLrgCount += s.newoLrgCnt;
		sumNewoOlCnt += s.newoOlCnt;
		sumNewoLrgOlCnt += s.newoLrgOlCnt;
                sumCancelOrdCnt += s.cancelOrdCnt;

		for (int i = 0; i < OrdersStats.TXTYPES; i++) {
			txCnt[i] += s.txCnt[i];
			respSum[i] += s.respSum[i];
			cycleSum[i] += s.cycleSum[i];
			targetedCycleSum[i] += s.targetedCycleSum[i];
			if (s.respMax[i] > respMax[i])
				respMax[i] = s.respMax[i];
			if (s.cycleMax[i] > cycleMax[i])
				cycleMax[i] = s.cycleMax[i];
			if (s.cycleMin[i] < cycleMin[i])
				cycleMin[i] = s.cycleMin[i];
		
			// sum up histogram buckets
			for (j = 0; j < OrdersStats.RESPMAX; j++)
				respHist[i][j] += s.respHist[i][j];
			for (j = 0; j < OrdersStats.THRUMAX; j++)
				thruputHist[i][j] += s.thruputHist[i][j];
			for (j = 0; j < OrdersStats.CYCLEMAX; j++)
				cycleHist[i][j] += s.cycleHist[i][j];
			for (j = 0; j < OrdersStats.CYCLEMAX; j++)
				targetedCycleHist[i][j] += s.targetedCycleHist[i][j];
		}
	}

	/**
	 * This method is used by the Agent to get a displayable
	 * result for MWBench. In addition to computing a tps for display.
	 * we use this method to write out the results to a file if in
	 * debug mode
	 */
	public String toString() {
		int i, j, totalCnt = 0;
		PrintStream p = System.out;
		double tps;

		for (i = 0; i < OrdersStats.TXTYPES; i++)
			totalCnt += txCnt[i];
		tps = (double)(totalCnt) * 1000 / stdyState;

		/* Write out aggregate info into file */
		if (debug) {
		try {
			logfile = resultsDir + System.getProperty("file.separator") +
			(InetAddress.getLocalHost()).getHostName() + ".log";
			p = new PrintStream(new FileOutputStream(logfile));
		} catch (Exception e) {}
	
		p.println("sumusers=" + threadCnt); 
		p.println("runtime=" + stdyState);
		p.println("sumNewoLrgCount=" + sumNewoLrgCount);
		p.println("sumNewoOlCnt=" + sumNewoOlCnt);
		p.println("sumNewoLrgOlCnt=" + sumNewoLrgOlCnt);

		for (i = 0; i < OrdersStats.TXTYPES; i++) {
			p.println("sum" + txNames[i] + "Count=" + txCnt[i]);
			p.println("sum" + txNames[i] + "Resp=" + respSum[i]);
			p.println("max" + txNames[i] + "Resp=" + respMax[i]);
			p.println("sum" + txNames[i] + "Cycle=" + cycleSum[i]);
			p.println("max" + txNames[i] + "Cycle=" + cycleMax[i]);
			p.println("min" + txNames[i] + "Cycle=" + cycleMin[i]);
		}	
		/* Now print out the histogram data */
		for (i = 0; i < OrdersStats.TXTYPES; i++) {
			p.println(txNames[i] + " Response Times Histogram");
			for (j = 0; j < OrdersStats.RESPMAX; j++)
				p.print(" " + respHist[j]); 
			p.println();
			p.println(txNames[i] + " Throughput Histogram");
			for (j = 0; j < OrdersStats.THRUMAX; j++)
				p.print(" " + thruputHist[j]);
			p.println();
			p.println(txNames[i] + " Cycle Times Histogram");
			for (j = 0; j < OrdersStats.CYCLEMAX; j++)
				p.print(" " + cycleHist[j]);
			p.println();
		}	
		p.close();
		}
	return(Double.toString(tps));
	}
}	
