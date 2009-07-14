/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: MfgStats.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */

package com.sun.ecperf.driver;

/**
 * This file contains the stats object that is saved by each mfg line.
 * This object is passed back after the end of a run to be aggregated 
 * with other mfg lines.
 *
 * @see MfgApp
 * @author Shanti Subramanyam
 */
public class MfgStats implements java.io.Serializable {
	/* The following finals are for histograms */

	// Response times
	public static final int RESPINTERVAL = 25;	/* total interval of buckets, in secs */
	public static final double RESPUNIT = 0.25;	/* time period of each bucket */
	public static final int RESPMAX = 100;	/* # of buckets = INTERVAL/UNIT */
	public static final int RESPBUCKET = 250;	/* bucket time in msec */

	public static final int RESPFAST = 5;

        // Thruput
        public static final int THRUINTERVAL = 172800;  /*  48 hours */
        public static final int THRUUNIT = 30;  /* 30 secs */
        public static final int THRUMAX = 5760; /* THRUINTERVAL / THRUUNIT */
        public static final int THRUBUCKET = 30000; /* Each bucket is for 30 sec */

	int workOrderCnt = 0;			/* number of workorders scheduled */
	int widgetCnt = 0;			/* number of widgets produced */
	int largeOrderCnt = 0;			/* no. of workorders scheduled due to largeorders */
	int largeOrderWidgetCnt = 0;	/* widgets produced by LargeOrderLine */
	int respMax = 0;			/* Max. response time */
	double respTime = 0;		/* Sum of response times */
	int respHist[] = new int [MfgStats.RESPMAX];/* Response time histogram */
	int thruput[] = new int[MfgStats.THRUMAX];	/* Thruput histogram */
	int numPlannedLines;
	String resultsDir;		/* Name of results directory */
	int stdyState;

	public MfgStats(int numPlannedLines, String resultsDir) {
		this.numPlannedLines = numPlannedLines;
		this.resultsDir = resultsDir;
	}

	public void setStdyState(int stdyState) {
		this.stdyState = stdyState;
	}

	/**
	 * This method adds the stats from the passed object to ours
	 */
	public void addResult(MfgStats m) {
		resultsDir = m.resultsDir;
		stdyState = m.stdyState;
		numPlannedLines += m.numPlannedLines;
		workOrderCnt += m.workOrderCnt;
		largeOrderCnt += m.largeOrderCnt;
		widgetCnt += m.widgetCnt;
		largeOrderWidgetCnt += m.largeOrderWidgetCnt;
		if (m.respMax > respMax)
			respMax = m.respMax;
		respTime += m.respTime;
		for (int i = 0; i < RESPMAX; i++) {
			respHist[i] += m.respHist[i];
		}
		for (int i = 0; i < THRUMAX; i++) {
			thruput[i] += m.thruput[i];
		}
	}


	public String toString() {
		return (Double.toString((double)(workOrderCnt) * 1000.0/stdyState));
	}
}
