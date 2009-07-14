/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrdersStats.java,v 1.1.1.1 2002/11/16 05:35:26 emrek Exp $
 */

package com.sun.ecperf.driver;

/**
 *
 * This class contains the stats object that is saved by each 
 * OrdersApp thread. This object is passed back after the end of a 
 * run to be aggregated with results from other threads run within an
 * Agent.
 *
 * @author Shanti Subramanyam
 * @see OrdersApp
 */
public class OrdersStats implements java.io.Serializable {
	public static final int TXTYPES = 4;	// Number of tx. types
	public static final int NEWORDER = 0;
	public static final int CHGORDER = 1;
	public static final int ORDERSTATUS = 2;
	public static final int CUSTSTATUS = 3;

	/* The following finals are for histograms */

	// Response times
	public static final int RESPINTERVAL = 10;	/* total interval of buckets, in secs */
	public static final double RESPUNIT = 0.1;	/* time period of each bucket */
	public static final int RESPMAX = 100;	/* # of buckets = INTERVAL/UNIT */
	public static final int RESPBUCKET = 100;	/* bucket time in msec */

	public static final int NEWOFAST = 2;
	public static final int CHGOFAST = 2;
	public static final int ORDSFAST = 2;
	public static final int CUSTSFAST = 2;

	// Cycle times 
	public static final int CYCLEINTERVAL = 25;	/* 5 * max. think time */
	public static final double CYCLEUNIT = 0.25;
	public static final int CYCLEMAX = 100;
	public static final int CYCLEBUCKET = 250;

	// Thruput
	public static final int THRUINTERVAL = 172800;	/*  48 hours */
	public static final int THRUUNIT = 30;	/* 30 secs */
	public static final int THRUMAX = 5760; /* THRUINTERVAL / THRUUNIT */
	public static final int THRUBUCKET = 30000; /* Each bucket is for 30 sec */

	int newoBadCredit;	/* # neworders that failed due to insufficient credit */
	int chgoBadCredit;	/* # change orders that failed due to insufficient credit */
	int newoBuyCart;	/* # neworders submitted using CartSes.buy */
	int newoLrgCnt;		/* number of neworders for large orders */
	int newoOlCnt;		/* total number of items ordered */
	int newoLrgOlCnt;	/* total number of items for large orders */
        int cancelOrdCnt;      	/*  # orders cancelled */

	int threadCnt;		/* threads in this Agent */
	int stdyState;		/* Needed by OrdersAggStats */
	String resultsDir;	/* Name of results directory */
	int txRate;

	/* Stats for all transaction types */

	int txCnt[] = new int[TXTYPES];			/* number of transactions */
	int respMax[] = new int[TXTYPES];			/* Max. response time */
	double respSum[] = new double[TXTYPES];		/* Sum of response times */
	double cycleSum[] = new double[TXTYPES];	/* sum of cycle times */
	double targetedCycleSum[] = new double[TXTYPES];	/* targeted cycle times */
	int cycleMax[] = new int[TXTYPES];
	int cycleMin[] = new int[TXTYPES];
	double elapse[] = new double[TXTYPES];		/* sum of elapsed times */
	int respHist[][] = new int[TXTYPES][RESPMAX];/* Response time histogram */
	int cycleHist[][] = new int[TXTYPES][CYCLEMAX];	/* Cycle time histogram */
	int targetedCycleHist[][] = new int[TXTYPES][CYCLEMAX];	/* Cycle time histogram */
	int thruputHist[][] = new int[TXTYPES][THRUMAX];	/* Thruput histogram */

	public OrdersStats(int threadCnt, String resultsDir, int txRate) {
		this.threadCnt = threadCnt;
		this.resultsDir = resultsDir;
		this.txRate = txRate;
		for (int i = 0; i < cycleMin.length; i++)
			cycleMin[i] = 9999999; // init to a large number
	}


	public void setStdyState(int stdyState) {
		this.stdyState = stdyState;
	}

	/**
	 * This method updates the thruput histogram for the
	 * given transaction type
	 * @param int transaction type
	 * @param int elapsed time
	 */
	public void updateThruput(int txType, int elapsedTime) {
		if ((elapsedTime / THRUBUCKET) >= THRUMAX)
				thruputHist[txType][THRUMAX - 1]++;
		else
				thruputHist[txType][elapsedTime / THRUBUCKET]++;
	}


	/**
	 * This method updates the various stats for the requested
	 * type of transaction - txCnt, resptime and cycletime stats
	 * @param int transaction type
	 * @param int response time
	 * @param int targeted cycle time
	 * @param int actual cycle time
	 */
	public void update(int txType, int respTime, 
		int targetedCycleTime, int actualCycleTime) {
			txCnt[txType]++;
			respSum[txType] += respTime;
			cycleSum[txType] += actualCycleTime;
			targetedCycleSum[txType] += targetedCycleTime;
			if (respTime > respMax[txType])
				respMax[txType] = respTime;
			
			// post in histogram of response times
			if ((respTime / RESPBUCKET) >= RESPMAX)
				respHist[txType][RESPMAX - 1]++;
			else
				respHist[txType][respTime / RESPBUCKET]++;

			if (actualCycleTime > cycleMax[txType])
				cycleMax[txType] = actualCycleTime;
			if (actualCycleTime < cycleMin[txType])
				cycleMin[txType] = actualCycleTime;
			
			if ((actualCycleTime / CYCLEBUCKET) >= CYCLEMAX)
				cycleHist[txType][CYCLEMAX - 1]++;
			else
				cycleHist[txType][actualCycleTime / CYCLEBUCKET]++;
			if ((targetedCycleTime / CYCLEBUCKET) >= CYCLEMAX)
				targetedCycleHist[txType][CYCLEMAX - 1]++;
			else
				targetedCycleHist[txType][targetedCycleTime / CYCLEBUCKET]++;
	}
}
