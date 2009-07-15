/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Station.java,v 1.1 2004/02/19 14:45:06 emrek Exp $
 *
 */
package com.sun.ecperf.driver;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import java.net.*;
import java.io.*;

import com.sun.ecperf.mfg.workorderses.ejb.*;

/**
 * class station
 * This class manages a particular station in the assembly line
 * It sleeps until asked to update the workorder status
 * Both the PlannedLine and the LargeOrderLine will create 3 stations
 * each per line.
 *
 * @see MfgApp
 * @see PlannedLine
 * @author Shanti Subramanyam
 */
 public class Station extends Thread {
	private int stationId;
	private WorkOrderSes workorder;
	private String sident;
	boolean start, finish = false;
	PrintStream errp;

	public Station(Context ctx, String pident, PrintStream errp, 
				String wohome, int id) {
		stationId = id;	
		this.errp = errp;
		sident = pident.concat("Station" +id + ": ");
		// Debug.println(sident  + "Creating workorder ...");
		try {
			// Create a workorder object that we will use 
			WorkOrderSesHome workOrderSesHome = 
				(WorkOrderSesHome) PortableRemoteObject.narrow
				(ctx.lookup(wohome), WorkOrderSesHome.class);
			workorder = workOrderSesHome.create();
		} catch (Exception e) {
			errp.println(sident + "Exception in creating WorkOrderBean " + e);
			start = false;
		}
		start();		/* Start running */
	}

	// We simply sleep in the run method
	public void run() {
		if (start == false)
			return;
		while (true) {
			try {
				sleep(3 * 60 * 60 * 1000);	// sleep for a long time
			} catch (InterruptedException ie) {
			if (finish)		// If we are finished, quit
				return;
			else
				continue;
			}
		}
	}
				
	
	/**
	 * updateStatus
	 * This method updates the status of a particular work-order
	 * by making a method call on our bean
	 * @param wo_id workorder to be updated
	 * @param wo_status value of status to update to
	 * @return true if update successful, false otherwise
	 */
	public boolean updateStatus(Integer wo_id, int wo_status) {
		try {
			if (wo_status == 2)
				workorder.completeWorkOrder(wo_id);
			else 
				workorder.updateWorkOrder(wo_id);
		} catch (Exception e) {
			errp.println(sident + "Exception in updateStatus(" + 
					wo_id + ", " + wo_status + ") " + e);
			return(false);
		}
		return(true);
	}

	/**
	 * This method is called to terminate the Station thread
	 * It destroys the workorder bean and causes a return from
	 * the run method indirectly.
	 */
	public void quit() {
		try {
			workorder.remove();
		} catch (Exception e) {
			errp.println(sident + " Error in removing WorkOrderbean" + e);
		}
		finish = true;		// Since 'destroy' is not implemented, tell
					// our 'run' method to return
	}
}
