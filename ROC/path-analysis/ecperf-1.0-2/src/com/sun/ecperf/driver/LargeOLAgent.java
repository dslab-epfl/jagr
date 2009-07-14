/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: LargeOLAgent.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
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
 * LargeOLAgent is the agent that runs the LargeOrderLine portion of the Mfg 
 * application.
 * It receives commands from the Driver.
 * The LargeOLAgent is responsible for spawning and managing the single 
 * LargeOrderLine and collecting its stats at the end of the run.
 *
 * @author Shanti Subramanyam
 * @see Agent
 * @see Driver
 */
public class LargeOLAgent extends UnicastRemoteObject 
    implements Agent, Unreferenced {

	static LargeOLAgent largeOLAgent;
	static Controller con;
	Properties connProps, runProps;
	Timer timer;
	LargeOrderLine largeOrderLine;
	String agentName;
	Serializable results = null;
	int numThreads;

	/**
	 * Constructor
	 * Create properties object from file
	 */
    protected LargeOLAgent(String name, String propsFile) throws RemoteException {
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
		largeOrderLine = new LargeOrderLine(0, timer, runProps);
    }
	
    /**
     * This method is responsible for starting up the benchmark run
	 * @param delay time before starting run
     */
    public void run(int delay) {
		Debug.println("LargeOLAgent: Starting benchmark run");
		// trigger.startRun(delay);
    }


    /**
     * This method kills off the current run
	 * It terminates all threads
     */
    public synchronized void kill() {

		Debug.println("LargeOLAgent: Killing benchmark run");
		largeOrderLine.destroy();
		results = null;
    }


	/**
	 * Report stats from a run
	 */
	public Serializable getResults() {
		results = (Serializable)(largeOrderLine.getResult());
		return(results);
	}

	public Serializable getCurrentResults() {
		return ((Serializable)(largeOrderLine.getCurrentResult()));
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
	    System.out.println("Usage: LargeOLAgent <propsFile> <agentName> <masterMachine>");
	    System.exit(-1);
	}
	String propsFile = argv[0];
	String name = argv[1];
	String master = argv[2];

	try {
	    largeOLAgent = new LargeOLAgent(name, propsFile);
	    String s1 = "//" + master + "/Controller";
	    con = (Controller)Naming.lookup(s1);
	    con.register(name, (Remote)largeOLAgent);
	    Debug.println(name + " started ...");
	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
}
