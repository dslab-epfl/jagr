/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrdersParam.java,v 1.1 2004/02/19 14:45:06 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import java.io.*;
import java.util.*;

/**
 * This class contains the list of parameters to the OrdersApp & MfgApp.
 * Parameters are parsed and set from the input parameter file.
 *
 * @author Shanti Subramanyam
 */
public class OrdersParam {
	public int numPGS = 1;
	public int custDBSize = 7500;
	public int custPoolSize = 10000;
	public int newoWeight = 100, chgoWeight = 50, ordsWeight = 30, custsWeight = 10;
	public int rampUp = 0, stdyState = 0, rampDown = 0;
	public int txRate = 10;		// Targeted Transaction rate
	public int slaveCnt = 1;	// Total number of slaves

	// Think time related defines
	public static final int NEWO_THINK = 3000;
	public static final int CHGO_THINK = 3000;
	public static final int ORDS_THINK = 3000;
	public static final int CUSTS_THINK = 3000;
	public static final int NEWO_THINKMAX = 15000;
	public static final int CHGO_THINKMAX = 15000;
	public static final int ORDS_THINKMAX = 15000;
	public static final int CUSTS_THINKMAX = 15000;

	// home interface names
	public String orderSessionHome = "orders/public/orderSession";
	public String orderCustomerSessionHome = "orders/public/orderCustomerSession";

	private BufferedReader bufp;

	/**
	 * Get input parameters from file
	 * The file is an ascii file containing one parameter per line
	 * in the form "name = value". All lines beginning with a #
	 * are ignored (comment lines). Blank lines are not allowed.
	 * The filename should be set as a parameter when defining the
	 * set to MWbench.
	 * @param filename containing ECperf parameters
	 * 
	 */
	public OrdersParam(String file) throws FileNotFoundException, IOException {

		bufp = new BufferedReader(new FileReader(file));

		// Read file, parsing parameters into variables above
		String s;
		StringTokenizer st;
		String name, value;
		int intval = 0;
		while ((s = bufp.readLine()) != null) {
			if (s.startsWith("#"))		// ignore comment lines
				continue;
		//	System.out.println(s);
			st = new StringTokenizer(s);
			name = st.nextToken();
			s = st.nextToken();	// skip over = sign
			value = st.nextToken();
		//	System.out.println("name = " + name + ", value = " + value);
			if (name.equals("orderSessionHome")) {
				orderSessionHome = value;
			}
			else if (name.equals("orderCustomerSessionHome")) {
				orderCustomerSessionHome = value;
			}
			else {
				// All remaining parameters are integers
				intval = Integer.parseInt(value);
				if (name.equals("numPGS")) {
					numPGS = intval;
					custDBSize = 3750 * numPGS * (numPGS + 1); 
					custPoolSize = 5000 * numPGS * (numPGS + 1);
				}
		// Removing numCust parameter
		//		else if (name.equals("numCust"))
		//			numCust = intval;
				else if (name.equals("custsWeight"))
					custsWeight = intval;
				else if	(name.equals("ordsWeight"))
					ordsWeight = intval;
				else if (name.equals("chgoWeight"))
					chgoWeight = intval;
				else if (name.equals("newoWeight"))
					newoWeight = intval;
				else if (name.equals("txRate"))
					txRate = intval;
				else if	(name.equals("slaveCnt"))
					slaveCnt = intval;
			}
		}
		bufp.close();

		// Validate parameters
		if (txRate != (5 * numPGS * (numPGS + 1))) {
			System.err.println("Warning: Transaction arrival rate: " +
				txRate + ", does not correspond to number of PGS: " +
				numPGS);
		}
		if (slaveCnt != (25 * numPGS * (numPGS + 1))) {
			System.err.println("Warning: Number of users: " +
				slaveCnt + ", does not correspond to number of PGS: " +
				numPGS);
		}
	}
}
