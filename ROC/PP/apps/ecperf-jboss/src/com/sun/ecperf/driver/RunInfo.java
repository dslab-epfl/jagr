/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: RunInfo.java,v 1.1 2004/02/19 14:45:06 emrek Exp $
 *
 */
 package com.sun.ecperf.driver;

 /**
  * RunInfo
  * This class contains the run parameters used for this run. These
  * are printed out in the ECperf.summary report
  */
public class RunInfo {
	public int txRate;
	public int runOrderEntry;
	public int runMfg;
	public int dumpStats;
	public int rampUp;
	public int rampDown;
	public int stdyState;
	public int triggerTime;
        public int msBetweenThreadStart;
	public int benchStartTime;
	public int numOrdersAgents;
	public int numMfgAgents;
	public boolean runLargeOrderLine;
	public long start;			// benchStartTime in actual time

        public int doAudit;
}
