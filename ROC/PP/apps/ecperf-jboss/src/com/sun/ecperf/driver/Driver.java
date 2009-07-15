/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Driver.java,v 1.1 2004/02/19 14:45:06 emrek Exp $
 */

package com.sun.ecperf.driver;

import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import com.sun.ecperf.orders.orderauditses.ejb.*;
import com.sun.ecperf.mfg.mfgauditses.ejb.*;

/**
 * This is the main Driver class for running the ECperf benchmark.
 * The Driver is instantiated on the <b>master machine</b> by the
 * user wishing to run a benchmark. It is responsible for co-ordinating 
 * the work of all the Agents, setting up the benchmark test, 
 * collecting the results etc.
 * NOTE: The controller and agents must have been brought up before
 * starting the driver. The driver will fail otherwise.
 *
 * @see        Agent
 * @see        Controller
 *
 */
public class Driver {
  
	protected Properties props;
	protected String homeDir, outDir, runOutputDir, pathSep;
	protected int runID, scaleFactor; 
	protected Remote[] ordersRefs, mfgRefs;	// Agent references
	protected Agent largeOrderLine;
	protected RunInfo runInfo;

	Timer timer;
        // Audit class reference
        Auditor auditor;

	/**
	 * Default constructor
	 */
	public Driver() {
		ordersRefs = null;
		mfgRefs = null;
		auditor = null;
	}

	/**
	 * Constructor
	 * @param propsfile - Name of properties file with input parameters
	 */
	public Driver(String propsFile) throws Exception {
		runInfo = new RunInfo();
		props = new Properties();
		try {
			FileInputStream in = new FileInputStream(propsFile);
			props.load(in);
			in.close();
		}
		catch (Exception e) {
			throw new Exception("Cannot read properties file " +
				propsFile + e);
		}

		homeDir = System.getProperty("user.home");
		pathSep = System.getProperty("file.separator");
		outDir = props.getProperty("outDir");
		if (outDir == null)
			outDir = homeDir + pathSep + "output";
			
		// Gets the ID for this run from the sequence file. 
		runID = getRunID();
		System.out.println("RunID for this run is : " + runID);
		props.setProperty("runID", Integer.toString(runID));

   		// make a new directory for the run.
    	runOutputDir = outDir + pathSep + runID;
    	File runDirFile = null;
    	try {
        	runDirFile = new File(runOutputDir);
			if ( !runDirFile.exists()) {
        		if ( !runDirFile.mkdir()) {
        		throw new Exception("Could not create the new Run Directory: " + runOutputDir);
				}
			}
    	}
    	catch (Exception e) {
        	throw new Exception("Could not create the new Run Directory: " + runOutputDir + e);
    	}
		System.out.println("Output directory for this run is : " + runOutputDir);
		props.setProperty("runOutputDir", runOutputDir);

		configure();
		configureAgents();
		executeRun();
	}


	/*
	 * This method retrieves the ID for the current run, by looking
	 * in the ecperf.seq file in the user's home directory.
	 * It increments the sequence file.
	 */
	private int getRunID() throws Exception{
		String runIDStr;
		int runID;

		String seqFileName = homeDir + pathSep + "ecperf.seq";
		File seqFile = new File(seqFileName);
		if (seqFile.exists()) {
	    	BufferedReader bufIn = null;
	    	try {
				bufIn = new BufferedReader(new FileReader(seqFile));
	    	}
	    	catch (FileNotFoundException fe) {
			throw new Exception("The sequence file '" + seqFile + "' does not exist" + fe);
	    	}
	    	runIDStr = null;
	    	try {
				runIDStr = bufIn.readLine();
				bufIn.close();
	    	}
	    	catch (IOException ie) {
				throw new Exception("Could not read/close the sequence file " + seqFileName + ie);
	    	}
	    	runID = Integer.parseInt(runIDStr);
		}
		else {
	    	try {
			seqFile.createNewFile();
	    	} 
	    	catch (IOException ie) {
			throw new Exception("Could not create the sequence file: " +
				seqFileName + ie);
	    	}
	    	runID = 1;
			runIDStr = "1";
		}
		// Update the runid in the sequence file
		try {
			BufferedWriter bufOut = new BufferedWriter(new FileWriter(seqFileName));
			bufOut.write(Integer.toString(runID+1));
			bufOut.close();
		}
		catch (IOException ie) {
			throw new Exception("Could not write to the sequence file: " +
				seqFileName + ie);
		}
		return(runID);
	}


	/**
	 * Configure the run
	 * Parse run properties
	 * Get a list of all the registered agents and configure them
	 * using the run properties
	 */
	protected void configure() throws Exception {
		timer = new Timer();

		// Compute total # of orders and planned line threads
		// trim all properties else spaces after properties in the 
		// config file cause errors.
		String propstr = (props.getProperty("scaleFactor")).trim();
		scaleFactor = Integer.parseInt(propstr);

		propstr = (props.getProperty("txRate")).trim();
		runInfo.txRate = Integer.parseInt(propstr);

		propstr =  (props.getProperty("runOrderEntry")).trim();
		runInfo.runOrderEntry = Integer.parseInt(propstr);

		propstr =  (props.getProperty("runMfg")).trim();
		runInfo.runMfg = Integer.parseInt(propstr);

		propstr =  (props.getProperty("doAudit")).trim();
		runInfo.doAudit = Integer.parseInt(propstr);

		propstr = (props.getProperty("dumpStats")).trim();
		runInfo.dumpStats = Integer.parseInt(propstr);

		propstr = (props.getProperty("rampUp")).trim();
		runInfo.rampUp = Integer.parseInt(propstr);
		runInfo.rampUp *= 1000;	// convert to ms
		props.setProperty("rampUp", Integer.toString(runInfo.rampUp));

		propstr = (props.getProperty("rampDown")).trim();
		runInfo.rampDown = Integer.parseInt(propstr);
		runInfo.rampDown *= 1000;	// convert to ms
		props.setProperty("rampDown", Integer.toString(runInfo.rampDown));

		propstr = (props.getProperty("stdyState")).trim();
		runInfo.stdyState = Integer.parseInt(propstr);
		runInfo.stdyState *= 1000;	// convert to ms
		props.setProperty("stdyState", Integer.toString(runInfo.stdyState));

                propstr = props.getProperty("triggerTime");

                if (propstr == null)
                    runInfo.triggerTime = -1;
                else {
                    propstr = propstr.trim();

                    if (propstr.length() == 0)
                        runInfo.triggerTime = -1;
                    else
		        runInfo.triggerTime = Integer.parseInt(propstr);
                }

                propstr = props.getProperty("msBetweenThreadStart");

                if (propstr == null)
                    runInfo.msBetweenThreadStart = -1;
                else {
                    propstr = propstr.trim();

                    if (propstr.length() == 0)
                        runInfo.msBetweenThreadStart = -1;
                    else
                        runInfo.msBetweenThreadStart = Integer.parseInt(propstr);
                }

                if(runInfo.doAudit == 1) {
                    // Create Auditor with runInfo
                    auditor = new Auditor(props, runInfo);

                    // Validate initial DB settings
                    auditor.validateInitialValues();
                }
	}
        
        protected void getAgentRefs()  throws Exception {
		String host = (InetAddress.getLocalHost()).getHostName();
		/*****
		String port = (props.getProperty("driverPort")).trim();
		String s1 = "//" + host + ":" + port + "/" + "Controller";
		****/
		String s1 = "//" + host + "/" + "Controller";
		Controller con = (Controller)Naming.lookup(s1);                
		if (runInfo.runOrderEntry == 1) {
			ordersRefs = con.getServices("OrdersAgent");
                }

                if (runInfo.runMfg == 1) {
			mfgRefs = con.getServices("MfgAgent");
			largeOrderLine = (Agent)con.getService("L1");
                }
        }                

	/**
	 * configureAgents()
	 * Get a list of all the registered agents and configure them
	 */
	protected void configureAgents() throws Exception {
                
                // To make sure that the right Controller class
                // is used to get the remote refs.
                getAgentRefs(); 

                int thrdTimeFactor = 0; // Time factor to wait
                                        // before starting the trigger.
                int ordsAgentCnt = 0;
                int mfgAgentCnt = 0;
                int ordsThrdsPerAgent = 0;
                int ordsRemThrds = 0;

		if (runInfo.runOrderEntry == 1) {
			ordsAgentCnt = ordersRefs.length;
			runInfo.numOrdersAgents += ordsAgentCnt;
			int numThreads = runInfo.txRate * 5;
			if (ordsAgentCnt == 0) {
			    System.err.println(
				"Cannot find OrdersAgent, " + 
				"please ensure it gets started!");
			    System.exit(1);
			}
			ordsThrdsPerAgent = numThreads/ordsAgentCnt;
			ordsRemThrds = numThreads - (ordsAgentCnt * ordsThrdsPerAgent);
                        thrdTimeFactor = numThreads + ordsAgentCnt * 3;

			props.setProperty("threadsPerAgent", Integer.toString(ordsThrdsPerAgent));
			props.setProperty("txRatePerAgent", Integer.toString(runInfo.txRate/ordsAgentCnt));
                }
		if (runInfo.runMfg == 1) {
			mfgAgentCnt = mfgRefs.length;
			runInfo.numMfgAgents += mfgAgentCnt;
			int numThreads = runInfo.txRate * 3;
			if (mfgAgentCnt == 0) {
			    System.err.println(
				"Cannot find MfgAgent, " + 
				"please ensure it gets started!");
			    System.exit(1);
			}

                        int thrdsPerAgent = numThreads/mfgAgentCnt;
                        thrdTimeFactor += numThreads + mfgAgentCnt * 3;

                        if (runInfo.runOrderEntry == 1)
                            thrdTimeFactor += 4;

			props.setProperty("plannedLines", Integer.toString(thrdsPerAgent));
                }
                // Recalculate the trigger time or wait time
                // Add 5 sec buffer time after all threads have
                // started before trigger.
                if (runInfo.msBetweenThreadStart >= 0) {
                    int minTriggerTime = 5 + runInfo.msBetweenThreadStart *
                                         thrdTimeFactor / 1000;
                    if (runInfo.triggerTime < 0)
                        runInfo.triggerTime = minTriggerTime;
		    else if (runInfo.triggerTime < minTriggerTime) {
                        System.out.println("         Minimum triggerTime of " +
                                           minTriggerTime + " required.");
		        System.out.println("         Current triggerTime of " +
                                           runInfo.triggerTime + " changed to "
                                           + minTriggerTime + ".");
                        runInfo.triggerTime = minTriggerTime;
                    }
		}
                else if (runInfo.msBetweenThreadStart < 0 && 
		         runInfo.triggerTime >= 0) {
                    runInfo.msBetweenThreadStart = 1000 * (runInfo.triggerTime
                                                   - 5) / thrdTimeFactor;
                    if (runInfo.msBetweenThreadStart < 1)
                        runInfo.msBetweenThreadStart = 1;
                    int minTriggerTime = 5 + runInfo.msBetweenThreadStart *
                                         thrdTimeFactor / 1000;
                    if (runInfo.triggerTime < minTriggerTime) {
                        System.out.println("         Minimum triggerTime of " +
                                           minTriggerTime + " required.");
		        System.out.println("         Current triggerTime of " +
                                           runInfo.triggerTime + " changed to "
                                           + minTriggerTime + ".");
                        runInfo.triggerTime = minTriggerTime;
                    }
		}
                else {
                    System.out.println("         Neither triggerTime nor msBetweenThreadStart is configured, exiting");
                    System.exit(1);
                }
		runInfo.benchStartTime = timer.getTime() + runInfo.triggerTime*1000;
		Debug.println("triggerTime = " + runInfo.triggerTime + " seconds");
		Debug.println("benchStartTime = " + runInfo.benchStartTime);
		props.setProperty("benchStartTime", Integer.toString(runInfo.benchStartTime));
                props.setProperty("triggerTime", Integer.toString(runInfo.triggerTime));
                props.setProperty("msBetweenThreadStart", Integer.toString(runInfo.msBetweenThreadStart));

                if (runInfo.runOrderEntry == 1) {
			Remote[] refs = ordersRefs;
			System.out.println("Configuring " + refs.length + " OrdersAgents...");
			for (int i = 0; i < refs.length; i++) {
				// If there are remaining threads left, assign them to
				// to the last agent. Ditto for txRate
				if (i == refs.length - 1) {
					ordsThrdsPerAgent += ordsRemThrds;
					props.setProperty("threadsPerAgent", 
						Integer.toString(ordsThrdsPerAgent));
					int rem = runInfo.txRate % ordsAgentCnt;
					props.setProperty("txRatePerAgent", 
						Integer.toString(runInfo.txRate/ordsAgentCnt + rem));
				}
				((Agent)refs[i]).configure(props, timer);
			}
		}
		if (runInfo.runMfg == 1) {
			// The workerrate assumes that each workorder will take 5 seconds
			// Since there are 3 threads, one workorder will finish in 5/3 seconds.
			props.setProperty("woRatePerAgent", Double.toString((double)runInfo.txRate/(1.66667*mfgAgentCnt)));
			if (largeOrderLine != null) {
				runInfo.runLargeOrderLine = true;
				System.out.println("Configuring LargeOLAgent...");
				largeOrderLine.configure(props, timer);
			}
			else {
				runInfo.runLargeOrderLine = false;
				System.out.println("Warning: MfgAgents configured, but LargeOLAgent missing.");
				System.out.println("         LargeOrderLine will not be run.");
			}
			Remote[] refs = mfgRefs;
			System.out.println("Configuring " + refs.length + " MfgAgents...");
			for (int i = 0; i < refs.length; i++) {
				((Agent)refs[i]).configure(props, timer);
			}
		}
	}

    /**
     * Tell the agents to start the run execution
	 * Note that the Agent's run method call is non-blocking
	 * i.e the Driver does not wait for an Agent. Instead, we
	 * wait for the total length of the run, after we signal
	 * all the agents to start.
     */
    protected void executeRun() {
		Remote refs[];
		StatsWriter sw = null;

		/* Now wait for the run to start */
		int delay = timer.getTime();
		int sleepTime = runInfo.benchStartTime - delay;
		if (sleepTime <= 0) {
		    System.err.println(
			"triggerTime set too short for thread startup." +
			"\nPlease increase by at least " +
			(int) Math.ceil(Math.abs(sleepTime / 1000.0)) +
			" and rerun.");
			System.exit(1);
		}
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException ie) {
		}

		// Start thread to dump stats for charting
		if (runInfo.dumpStats == 1) {
			System.out.println("Starting StatsWriter ...");
			sw = new StatsWriter();
		}

		/* Wait for the length of the run (+ grace time) */
		delay = runInfo.rampUp + runInfo.stdyState + runInfo.rampDown + 60000;
		try {
			Thread.sleep(delay);
		} catch (InterruptedException ie) {
		}
		/* Start gathering stats */
		OrdersAggStats ordersResults[] = null;
		MfgStats mfgResults[] = null;
		try {
		if (runInfo.runOrderEntry == 1) {
			ordersResults = new OrdersAggStats[runInfo.numOrdersAgents];
			refs = ordersRefs;
			System.out.println("Gathering OrdersStats ...");
			for (int i = 0; i < refs.length; i++) {
				ordersResults[i] = (OrdersAggStats)(((Agent)refs[i]).getResults());
			}
		}
		if (runInfo.runMfg == 1) {
			if (largeOrderLine == null)
				mfgResults = new MfgStats[runInfo.numMfgAgents];
			else
				mfgResults = new MfgStats[runInfo.numMfgAgents + 1];
			refs = mfgRefs;
			System.out.println("Gathering MfgStats ...");
			int i;
			for (i = 0; i < refs.length; i++) {
				mfgResults[i] = (MfgStats)(((Agent)refs[i]).getResults());
			}
			if (largeOrderLine != null)
				mfgResults[i] = (MfgStats)(largeOrderLine.getResults());
		}
		} catch (RemoteException re) {
			System.err.println("Driver: RemoteException got " + re);
		}
			
		// Adjust times to real times
		runInfo.start = runInfo.benchStartTime + timer.getOffsetTime();
                // Consolidate the reports from all the agents
		ECperfReport ecReport = new ECperfReport();
                ecReport.generateReport(runInfo, ordersResults, mfgResults);
                if(runInfo.doAudit == 1)
                    // Auditor will validate the report
		    try {
                        auditor.validateReport(ecReport);
		    } catch (RemoteException re) {
			System.err.println("Driver: RemoteException got " + re);
		    }
		// Tell StatsWriter to quit
		if (runInfo.dumpStats == 1) {
			System.out.println("Quitting StatsWriter...");
			sw.quit();
		}
		return;
    } 


        private class DumpListener extends Thread {

                String resource;
                List resourceList;
                int elapsed = 0; // current time position
                                 // to zero set new streams.

                public DumpListener(String resource, List resourceList) {
                    this.resource = resource;
                    this.resourceList = resourceList;
                    setDaemon(true);
                    start();
                }

	        public int getSocketPort() {
                    int port = 0;
                    for (int i = 0; i < resource.length(); i++) {
                        char c = resource.charAt(i);
                        if (c < '0' || c > '9')
                           return -1;
                    }
                    try {
                        port = Integer.parseInt(resource);
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                    return port;
                }
                

                public void run() {
                    int port = getSocketPort();

                    try {
                        // Check if it is a simple file
                        if (port == -1) {
                            resourceList.add(new DataOutputStream(
                                new FileOutputStream(resource)));

                        } else {
                        // Here we act as a server

                            ServerSocket sock = new ServerSocket(port);
                            for (;;)

                                /* IOException on one connection should
                                 * not terminate the loop. So we catch
                                 * it internally.
                                 */
                                try {
                                    DataOutputStream dumpStream = new
                                        DataOutputStream(
                                        sock.accept().getOutputStream());

                                    // Zero set the current time position
                                    int elapsed = this.elapsed;
                                    // Avoid locking, use atomic op
                                    // to capture value.
                                    if (elapsed > 0) {
                                        dumpStream.writeDouble(elapsed);
                                        dumpStream.writeDouble(0);
                                        dumpStream.writeDouble(0);
                                    }

                                    // Then let people write to it.
                                    resourceList.add(dumpStream);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }

	private class StatsWriter extends Thread {

                DumpListener ordsListener = null;
                DumpListener mfgListener  = null;
		boolean endFlag = false;
		OrdersAggStats curOrdResults[];
		MfgStats curMfgResults[];
		OrdersReport oRep;
		MfgReport mRep;
		Remote refs[];
                long dumpInterval;
                int dumpSecs;

		public StatsWriter() {

                        List ordersTargets = Collections.synchronizedList(
                                             new ArrayList());
                        List mfgTargets    = Collections.synchronizedList(
                                             new ArrayList());
                        
                        ordsListener = new DumpListener(props.getProperty(
                                               "ordDumpTarget"), ordersTargets);
                        mfgListener  = new DumpListener(props.getProperty(
                                               "mfgDumpTarget"), mfgTargets);
                        String dumpInt = props.getProperty("dumpInterval", "5");

                        dumpSecs = Integer.parseInt(dumpInt);

                        oRep = new OrdersReport(ordersTargets, dumpSecs,
                                                runInfo.rampUp/1000);
                        mRep = new MfgReport(mfgTargets, dumpSecs,
                                             runInfo.rampUp/1000);

                        dumpInterval = dumpSecs * 1000;
                                              // Make millis so we do not
                                              // have to re-calculate.

                        start();
                }

		public void run() {

                        long baseTime = System.currentTimeMillis();

			// Loop, sleeping for dumpInterval and then dump stats

			while (! endFlag) {
                                baseTime += dumpInterval;
                                for (;;)

                                    /* This algorithm may not be very accurate
                                     * but accurate enough. The more important
                                     * thing is it adjusts for cumulative
                                     * errors/delays caused by other ops,
                                     * network, and environment.
                                     */
				    try {

                                        // Adjust for time spent in other ops.
                                        long sleepTime = baseTime -
                                            System.currentTimeMillis();

                                        // Only sleep the remaining time.
                                        if (sleepTime > 0)
					    Thread.sleep(sleepTime);

                                        /* Break loop when sleep complete
                                         * or no time left to sleep.
                                         */
                                        break;
				    } catch (InterruptedException ie) {
                                        /* If interrupted, just loop
                                         * back and sleep the remaining time.
                                         */
				    }

				try {
				if (runInfo.runOrderEntry == 1) {
					curOrdResults = new OrdersAggStats[runInfo.numOrdersAgents];
					refs = ordersRefs;
					// System.out.println("Gathering interim OrdersStats ...");
					for (int i = 0; i < refs.length; i++) {
						curOrdResults[i] = (OrdersAggStats)(((Agent)refs[i]).getCurrentResults());
					}
					oRep.dumpStats(curOrdResults);
				}
				if (runInfo.runMfg == 1) {
					if (largeOrderLine == null)
						curMfgResults = new MfgStats[runInfo.numMfgAgents];
					else
						curMfgResults = new MfgStats[runInfo.numMfgAgents + 1];
					refs = mfgRefs;
					// System.out.println("Gathering MfgStats ...");
					int i;
					for (i = 0; i < refs.length; i++) {
						curMfgResults[i] = (MfgStats)(((Agent)refs[i]).getCurrentResults());
					}
					if (largeOrderLine != null)
						curMfgResults[i] = (MfgStats)(largeOrderLine.getCurrentResults());
					mRep.dumpStats(curMfgResults);
				}
				} catch (RemoteException re) {
					System.err.println("Driver: RemoteException got " + re);
				}

                                // Intentionally do this last.
                                ordsListener.elapsed += dumpSecs;
                                mfgListener.elapsed += dumpSecs;
			}
		}

		void quit() {
			endFlag = true;
		}
	}
		
	public static void main(String [] argv) throws Exception {

		if (argv.length < 1 || argv.length > 1) {
			System.err.println("Usage: Driver <properties_file>");
			return;
		}
		String propsfile = argv[0];
		Driver d = new Driver(propsfile);
	}
}
