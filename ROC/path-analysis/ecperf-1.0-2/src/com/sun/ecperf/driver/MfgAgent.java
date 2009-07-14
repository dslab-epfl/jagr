/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: MfgAgent.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;

/**
 * MfgAgent is the agent that runs the Mfg application.
 * It receives commands from the Driver.
 * The MfgAgent is responsible for spawning and managing 
 * threads, synchronizing between the threads and aggregating
 * the stats from all threads.
 *
 * @author Shanti Subramanyam
 * @see Agent
 * @see Driver
 */
public class MfgAgent extends UnicastRemoteObject 
    implements Agent, Unreferenced {

	static MfgAgent mfgAgent;
	static Controller con;
	Properties connProps, runProps;
	Timer timer;
	Vector plannedLineVector;
	String agentName;
	Serializable results[] = null;
	int numThreads;

	/**
	 * Constructor
	 * Create properties object from file
	 */
    protected MfgAgent(String name, String propsFile) throws RemoteException {
		agentName = name;
		connProps = new Properties();
		try {
			FileInputStream in = new FileInputStream(propsFile);
			connProps.load(in);
			in.close();
		}
		catch (Exception e) {
			throw new RemoteException("Cannot read properties file " +
				propsFile + e);
		}
    }

	 
    public void configure(Properties props, Timer timer) throws RemoteException {
		runProps = props;
		this.timer = timer;

		runProps.setProperty("agentName", agentName);
		results = null;		// so that we don't use old results
		plannedLineVector = new Vector();

		// Create the required number of Mfg threads
		numThreads = Integer.parseInt(runProps.getProperty("plannedLines"));
                int sleepTime = Integer.parseInt(runProps.getProperty("msBetweenThreadStart"));
		for (int i = 0; i < numThreads; i++) {
			plannedLineVector.addElement(new PlannedLine(i, timer, runProps));
			try {
				Thread.sleep(sleepTime);	// Give time for thread to connect to server
			} catch (InterruptedException ie) {
			}
		}
		/*****
		// Wait to ensure all threads are up
		try {
			Thread.sleep(200 * numThreads);
		} catch (InterruptedException ie) {
		}
		*****/
    }
	
    /**
     * This method is responsible for starting up the benchmark run
	 * @param delay time before starting run
     */
    public void run(int delay) {
		Debug.println("MfgAgent: Starting benchmark run");
		// trigger.startRun(delay);
    }


    /**
     * This method kills off the current run
	 * It terminates all threads
     */
    public synchronized void kill() {

		Debug.println("MfgAgent: Killing benchmark run");
		for (int i = 0; i < numThreads; i++) {
			((PlannedLine)(plannedLineVector.elementAt(i))).destroy();
		}
		// cleanup
		results = null;
    }


	/**
	 * Report stats from a run
	 * Each thread's result is obtained by calling that thread's getResult()
	 * All these results are then aggregated.
	 */
	public Serializable getResults() {
		results = new Serializable[numThreads];
		for (int i = 0; i < numThreads; i++) {
			results[i] = (Serializable)((PlannedLine)(plannedLineVector.elementAt(i))).getResult();
		}
		// Aggregate results from all threads of this agent
		MfgStats aggStats = new MfgStats(numThreads, ((MfgStats)results[0]).resultsDir);
		for (int index = 0; index < results.length; index++) {
				aggStats.addResult((MfgStats)(results[index]));
		}
		return(aggStats);
	}



	/**
	 * THis method is called by the Driver to retrieve current
	 * results to plot chart
	 */
	public Serializable getCurrentResults() {
		Serializable curResults[] = new Serializable[numThreads];
		for (int i = 0; i < numThreads; i++) {
			curResults[i] = (Serializable)((PlannedLine)(plannedLineVector.elementAt(i))).getCurrentResult();
		}
		// Aggregate results from all threads of this agent
		MfgStats aggStats = new MfgStats(numThreads, ((MfgStats)curResults[0]).resultsDir);
		for (int index = 0; index < curResults.length; index++) {
				aggStats.addResult((MfgStats)(curResults[index]));
		}
		return(aggStats);
	}


    /**
     * When this instance is unreferenced the application must exit.
     *
     * @see         Unreferenced
     *
     */
    public void unreferenced() {
			kill();
    }


    /**
     * Registration for RMI serving
     */

    public static void main(String [] argv) {

	//		LocateRegistry.createRegistry();
	System.setSecurityManager (new RMISecurityManager());
	if (argv.length != 3) {
	    System.out.println("Usage: MfgAgent <propsFile> <agentName> <masterMachine>");
	    System.exit(-1);
	}
	String propsFile = argv[0];
	String name = argv[1];
	String master = argv[2];

	try {
	    mfgAgent = new MfgAgent(name, propsFile);
	    String s1 = "//" + master + "/Controller";
	    con = (Controller)Naming.lookup(s1);
	    con.register("MfgAgent", name, (Remote)mfgAgent);
	    Debug.println(name + " started ...");
	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
}
