/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Agent.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.io.*;
import java.util.*;

/**
 * The methods in this interface are the public face of the
 * Orders and Manufacturing Agents. The agents register with the
 * Controller on startup. The Driver gets a reference to all the agents
 * from the Controller and can then communicate with them using
 * this (the Agent) interface.
 * @author Shanti Subrmanyam
 * @see Driver
 * @see OrderEntry
 * @see PlannedLine
 * @see LargeOrderLine
 */
public interface Agent extends Remote {

	/**
	 * initialize remote Agents
	 * @param run properties
	 */
	public void configure(Properties properties, Timer timer) throws RemoteException;

	/**
	 * This method is responsible for starting the benchmark run
	 * The caller does not wait for the run to complete. 
	 * @param int delay - time to delay(ms) before starting the run
	 */
	public void run(int delay) throws RemoteException;

	/**
	 * This method is responsible for aborting a run
	*/
	public void kill() throws RemoteException;

	/**
	 * Report stats from a run, aggregating across all threads of
	 * the Agent.
	 * The stats object is actually different for each Agent.
	 * @see OrdersAggStats
	 * @see MfgAggStats
	 */
	public Serializable getResults() throws RemoteException;

	public Serializable getCurrentResults() throws RemoteException;
}
