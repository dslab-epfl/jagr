/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderEntry.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.net.*;
import java.io.*;
import java.util.*;
import com.sun.ecperf.orders.orderses.ejb.*;
import com.sun.ecperf.orders.ordercustomerses.ejb.*;
import com.sun.ecperf.orders.cartses.ejb.*;
import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.common.*;

/**
 * This class implements the per-user thread of the OrderEntry application.
 * It generates calls to the OrdersBean methods based on the defined mix
 * and keeps track of response times, think times, transaction counts etc.
 * Objects of this class are controller by the OrdersAgent.
 *
 * @see OrdersAgent
 *
 * @author Shanti Subramanyam
 */
public class OrderEntry extends Thread {
	static final int AForCustomer = 255;	// A in NURand
    int id;
	Timer timer;
	Properties props;
	OrdersStats stats;
	boolean inRamp;		// indicator for rampup or rampdown
	int rampUp, stdyState, rampDown;
	int endRampUp, endStdyState, endRampDown;
	int numThreads, txRate, txRatePerAgent;
	int custPoolSize, custDBSize, numOrders;
	int benchStartTime;		// Actual time of rampup start
	String resultsDir;
	Context ctx;
	OrderSes orders;
	OrderCustomerSes customerSession;
	CartSesHome cartSesHome;
	RandNum r;
	RandPart rp;
	int numItems;
	int timePerTx;		// Avg. cycle yime
	int timeForThisTx;	// cycle time for this tx.
	String ident;
	boolean start = true;
	boolean statsDone = false;	// Has Agent collected our stats yet ?

	static final int cartPoolSize = 1000;
	static final int cartMean = 100;
	CartSes carts[] = new CartSes[cartPoolSize]; // array of cached shopping carts
	/* 
	 * Number of orders in initial db
	 * This field will be updated by neworders and is used by 
	 * getOrderStatus
	 */
	Vector newOrderIds = new Vector();
	PrintStream errp;

	/**
	 * Constructor 
	 * @param id for the agent
	 * @param Timer
	 * @param Properties of the run
	 */
       	public OrderEntry(int id, Timer timer, Properties props) {
                
		this.id = id;
		this.timer = timer;
                // Get an initial context
                try {
                    ctx = new InitialContext();
                } catch (NamingException ne) {
                    errp.println(ident + " : InitialContext failed. : " + ne);
                }
		this.props = props;
		start();
	}


	/**
	 * Each thread executes in the run method until the benchmark time is up
	 * The main loop chooses a tx. type according to the mix specified in
	 * the parameter file and calls the appropriate transaction
	 * method to do the job.
 	 * The stats for the entire run are stored in an OrdersStats object
 	 * which is returned to the OrdersAgent via the getResult() method.
	 * @see OrdersStats
	 */
	public void run() {
		int tx_type;
		int delay, endTime;

		resultsDir = props.getProperty("runOutputDir");
		getReady();		// Perform inits
		if (start == false)	// If error occured during setup, do not run
			return;

		// Calculate time periods
		benchStartTime = Integer.parseInt(props.getProperty("benchStartTime"));
		rampUp = Integer.parseInt(props.getProperty("rampUp"));
		stdyState = Integer.parseInt(props.getProperty("stdyState"));
		stats.setStdyState(stdyState);
		rampDown = Integer.parseInt(props.getProperty("rampDown"));
		endRampUp = benchStartTime + rampUp;
		endStdyState = endRampUp + stdyState;
		endRampDown = endStdyState + rampDown;
		/****
		Debug.println(ident + "rampup end time = " + endRampUp + 
			", stdy endtime = " + endStdyState + 
			", rampdown endtime = " + endRampDown);
		****/
		// If we haven't reached the benchmark start time, sleep
		delay = benchStartTime - timer.getTime();
		if (delay <= 0) {
			errp.println(ident + "Warning: triggerTime has expired. Need " + (-delay) + " ms more");
		}
		else {
			// Debug.println(ident + "Sleeping for " + delay + "ms");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
			}
		}
		inRamp = true;
		// Loop until time is up
		while (true) {
			//Compute cycle time for this tx
			timeForThisTx = getFromDistribution(timePerTx, timePerTx*5);

			tx_type = doMenu();
			switch(tx_type) {
			case OrdersStats.NEWORDER:	doNewOrder();
							break;
			case OrdersStats.CHGORDER:	doChgOrder();
							break;
			case OrdersStats.ORDERSTATUS:	doOrderStatus();
							break;
			case OrdersStats.CUSTSTATUS:doCustStatus();
							break;
			default:
				errp.println(ident + "Internal error. Tx-type = " + tx_type);
				return;
			}
			endTime = timer.getTime();
			// Debug.println(ident + "endTime = " + endTime);
			if (endTime >= endRampUp && endTime < endStdyState)
				inRamp = false;
			else
				inRamp = true;
			if (endTime >= endRampDown)
				break;
		}
		// End of run, destroy bean
		Debug.println(ident + "End of run. Removing beans");
		try {
			orders.remove();
			customerSession.remove();
		} catch (Exception e) {
			errp.println(ident + " Error in removing bean " + e);
		}

		// Now sleep forever. We can't exit, as if we do, the thread
		// will be destroyed and the OrdersAgent won't be able to
		// retrieve our stats.
		while ( !statsDone) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ie) {
			}
		}
		Debug.println(ident + " Exiting...");
	}

	/**
	 * Return result of running OrderEntry
	 * @return serializable form of OrdersStats
	 * @see OrdersStats
	 */
	public java.io.Serializable getResult() {
		// Debug.println(ident + "Returning stats");
		statsDone = true;
		return(stats);
	}

	public java.io.Serializable getCurrentResult() {
		return(stats);
	}


	/**
	 * This method is called from configure to open and read the
	 * parameter file and set up instance variables from it. It also
	 * create an error log in the run directory and does a lookup
	 * on the OrdersBean home interface
	 * @param none
	 * @return none
	 */
	private void getReady() {
		String errfile = resultsDir + 
						System.getProperty("file.separator") + 
						"ords.err";
		// Get our thread name and append it to the Agent name to
		// uniquely identify ourselves
		String name = props.getProperty("agentName");
		ident = name.concat(":" + id + ": ");
		System.out.println("OrdersAgent " + name + ", Thread " + id + " started");
		// Debug.println("In getReady of thread " + ident);
		// Create error log if it doesn't already exist
		try {
			if (new File(errfile).exists()) {
				errp = new PrintStream(new FileOutputStream(errfile, true));
			}
			else {	// try creating it
				// Debug.println(ident + "Creating " + errfile);
				errp = new PrintStream(new FileOutputStream(errfile));
			}
		} catch (Exception e) {
			System.err.println(ident + "Could not create " + errfile);
			errp = System.err;
		}

		// Get some properties
		numThreads = Integer.parseInt(props.getProperty("threadsPerAgent"));
		txRatePerAgent = Integer.parseInt(props.getProperty("txRatePerAgent"));
		txRate = Integer.parseInt(props.getProperty("txRate"));
		stats = new OrdersStats(numThreads, resultsDir, txRate);
		long seed = timer.getTime() + this.hashCode();
		r = new RandNum(seed);		// Seed random number generator
		/*** This should be the final version
		rp = new RandPart(r, txRate);
		****/
		numItems = (int)(Math.ceil((double)txRate/100.0)) * 100;
		rp = new RandPart(r, numItems, 1);
		// Compute some sizes
		int C = (int)(Math.ceil((double)txRate/10.0)) * 10;
		custDBSize = 75 * C;
		custPoolSize = 100 * C;
		numOrders = 75 * C;

		// compute our mean arrival rate in msecs
		timePerTx = (numThreads *1000)/ txRatePerAgent;
		try {
			// Create an OrderSes object 
			String ohome, chome, cartHome;
			String prefix = props.getProperty("homePrefix");
                        // The homePrefix will have the trailing '/'
			if (prefix != null) {
				ohome = prefix + "OrderSes";
				chome = prefix + "OrderCustomerSes";
				cartHome = prefix + "CartSes";
			}
			else {
				ohome = "OrderSes";
				chome = "OrderCustomerSes";
				cartHome = "CartSes";
			}
			// Debug.println("ohome = " + ohome);
			OrderSesHome orderSesHome = 
				(OrderSesHome) PortableRemoteObject.narrow
				(ctx.lookup(ohome), OrderSesHome.class);
			orders = orderSesHome.create();
			OrderCustomerSesHome customerHome = 
				(OrderCustomerSesHome) PortableRemoteObject.narrow
				(ctx.lookup(chome), OrderCustomerSesHome.class);
			customerSession = customerHome.create();
			cartSesHome = 
				(CartSesHome) PortableRemoteObject.narrow
				(ctx.lookup(cartHome), CartSesHome.class);
			
		} catch (NamingException e) {
			errp.println(ident + "Failure looking up home " + e);
			start = false;
		} catch (Exception ex) {
			errp.println(ident + "Failure in creating beans " + ex);
			start = false;
		}
	}


	/**
	 * This method selects a given tx. from the specified mix using the
	 * weighted distributed algorithm
	 */
	private int doMenu(){
		int val = r.random(1, Integer.parseInt(props.getProperty("newoWeight")));
		if (val <= Integer.parseInt(props.getProperty("custsWeight")))
			return (OrdersStats.CUSTSTATUS);
		else if (val <= Integer.parseInt(props.getProperty("ordsWeight")))
			return(OrdersStats.ORDERSTATUS);
		else if (val <= Integer.parseInt(props.getProperty("chgoWeight")))
			return(OrdersStats.CHGORDER);
		else
			return(OrdersStats.NEWORDER);
	}

	/**
	 * Use a negative exponential distribution with specified mean and max 
	 * We truncate the distribution at 5 times the mean for cycle times
	 * @param mean time 
	 * @param max time
	 * @return time to use for this transaction
	 */
	private int getFromDistribution(int mean, int max) {
		if (mean <= 0)
			return(0);
		else {
			double x = r.drandom(0.0, 1.0);
			if (x == 0)
				x = 0.05;
			int delay = (int)(mean * (-Math.log(x)));
			if (delay > max)
				delay = max;
			return(delay);
		}
	}

			
	private void doNewOrder() {
		int oid, cid, olCnt, totalQty;
		int startTime, endTime, respTime, meanThink, thinkTime, elapsedTime;
		int calcTime, cycleTime;
		int lrg;
		boolean fail = false;
		boolean badCredit = false;
		boolean cartBuy = false;

		calcTime = timer.getTime();	// to keep track of time to do computations

		olCnt = r.random(1, 5);
		ItemQuantity itms[] = new ItemQuantity[olCnt];
                String itmIds[] = new String[olCnt];

		// Select a small order 90% of the time
		lrg = r.random(1, 100);
		if (lrg <= 90)
			totalQty = r.random(10, 20);
		else
			totalQty = r.random(100, 200);
		
		// We divide this qty equally among the order-lines for now
		int q = totalQty/olCnt;
		int rem = totalQty - (q*olCnt);
		for (int i = 0; i < olCnt; i++) {
			boolean done = false;
			while ( !done) {
				itmIds[i] = rp.getPart();
				int l;
				for (l = 0; l < i; l++) {
					if (itmIds[i].equals(itmIds[l]))
						break;
				}
				if (l == i)
					done = true;
			}
		}
		
                // Sort items - fix for bugid 4480737
                Arrays.sort(itmIds);
		for (int i = 0; i < olCnt; i++) {
		    itms[i] = new ItemQuantity(itmIds[i], q);
                }
		// Add left-over qty to last item
		itms[olCnt-1].itemQuantity += rem;

		// Choose a random customer, using NURand
		cid = r.NURand(AForCustomer, 1, custPoolSize);

		// If this is a new customer, generate his info
		if (cid > custDBSize) {
			String zip = r.make_n_string(4, 4);
			zip = zip + "11111";
			Address adr = new Address(
				r.make_a_string(20,20),	//street1
				r.make_a_string(20,20),	//street2
				r.make_a_string(20,20),	//city
				r.make_a_string(2,2),		//state
				r.make_a_string(10,10),	//country
				zip,
				r.make_n_string(16,16));	//phone

                        // For additional Info to create Corp A/c
		        int x = r.random(1, 100);
                        String credit;
                        double creditLimit, balance, YtdPayment;

		        if ( x <= 10) {
		           credit = "BC";
		           creditLimit = 0;
		        }
		        else {
		          credit = "GC";
                          /** Change creditLimit in accordance with load - bugid 4482440
		          creditLimit = r.drandom(100000, 1000000);
                          **/
		          creditLimit = r.drandom(300000, 3000000);
		        }
			balance = r.random(0, 25000);
                        YtdPayment = r.random(0, 350000); 
			CustomerInfo info = new CustomerInfo(
				r.make_a_string(16,16),	//first
				r.make_a_string(16,16),	//last
				adr,
				r.make_a_string(25,25),	//contact
                                credit, 
                                creditLimit,
                                balance,
                                YtdPayment
			);

			startTime = timer.getTime();
			try {
				cid = customerSession.addCustomer(info);
			} catch (Exception e) {
				errp.println(ident + "Failure in addCustomer " + e);
				fail = true;
			}
		}
		else {
			startTime = timer.getTime();
			try {
				customerSession.validateCustomer(cid);
			} catch (Exception re) {
				errp.println(ident + "Failure in validateCustomer(" + 
					cid + ")" + re);
				fail = true;
			}
		}

		if ( !fail) {
            // 50% of the time, we use CartSes to add all the items to the
            // Shopping Cart and then buy it. The other 50% of the time, we
            // simply call newOrder of the OrderSes bean directly
            int x = r.random(1, 100); 
            if ( x <= 50) {
                // Now call newOrder method.
				try {
					oid = orders.newOrder(cid, itms);
					newOrderIds.addElement(new Integer(oid));	// Save oid got
				} catch (InsufficientCreditException ie) {
					badCredit = true;
				} catch (Exception e) {
					errp.println(ident + "Error occured in newOrder for cid " +
						cid);
					errp.println("        Number of orderlines = " + olCnt);
					for (int i = 0; i < olCnt; i++) {
						errp.println("        itemId = " + itms[i].itemId +
							"itemQuantity = " + itms[i].itemQuantity);
					}
					errp.println("        " + e);      
					fail = true;
				}
			}
			else {	
				// determine cart to use
				int cartId = getFromDistribution(cartMean, 10*cartMean - 1);
				int i = 0;
				try {
					if (carts[cartId] == null) {
						// Create a new shopping cart
						carts[cartId] = cartSesHome.create(cid);
					}
					else {
						carts[cartId].deleteAll();	// remove any previous items
					}
				} catch (CreateException ce) {
					errp.println(ident + "Failure in creating cartSes " + ce);
					fail = true;
				} catch (Exception e) {
					errp.println(ident + "Failure in cartSes.deleteAll " + e);
					fail = true;
				}
				if ( !fail) {
				try {
					// Add items to cart one at a time
					CartSes cartSes = carts[cartId];
					for (i = 0; i < olCnt; i++) {
						cartSes.add(itms[i]);
					}
					oid = cartSes.buy();
					newOrderIds.addElement(new Integer(oid));	// Save oid got
					cartBuy = true;

					// Now, delete the cart 90% of the time
					x = r.random(1, 100);
					if ( x <= 90) {
						carts[cartId] = null;
						cartSes.remove();
					}
				} catch (InsufficientCreditException ie) {
					badCredit = true;
					cartBuy = true;
				} catch (Exception ex) {
					if (i != olCnt) {
						errp.println("Error occured in CartSes.add for item " +  i);
						errp.println("        itemId = " + itms[i].itemId +
							"itemQuantity = " + itms[i].itemQuantity);
					}
					else {
						errp.println("Error occured in CartSes.buy for customer " +cid);
						errp.println("        Number of items = " + olCnt);
						for (int j = 0; j < olCnt; j++) {
						errp.println("        itemId = " + itms[j].itemId +
							"itemQuantity = " + itms[j].itemQuantity);
						}
					}
					errp.println("        " + ex);
					fail = true;
				}
				}
			}
		}
		endTime = timer.getTime();

		// Compute think time to use
		calcTime = startTime - calcTime;	// time taken to setup call
		respTime = endTime - startTime;
		// Debug.println(ident + "Neworder resptime = " + respTime + ", calcTime = " + calcTime);
		cycleTime = respTime + calcTime;
		// We reduce our sleep time by the time it took to do computations
		thinkTime = timeForThisTx - respTime - calcTime;
		// Debug.println(ident + "Neworder thinkTime = " + thinkTime);
		if (thinkTime > 0) {
			cycleTime += thinkTime;
			try{
				Thread.sleep(thinkTime);
			} catch (InterruptedException ie) { }
		}
		// Store elapsed time info for thruput
		elapsedTime = endTime - benchStartTime;
		if ( !fail) {
			stats.updateThruput(OrdersStats.NEWORDER, elapsedTime);

			// Post all stats if in stdy-state
			if ( !inRamp && endTime <= endStdyState) {
				if (lrg > 90) {
					stats.newoLrgCnt++;
					stats.newoLrgOlCnt += totalQty;
				}
				stats.newoOlCnt += totalQty;
				if (badCredit)
					stats.newoBadCredit++;
				if (cartBuy)
					stats.newoBuyCart++;
				stats.update(OrdersStats.NEWORDER, respTime, timeForThisTx, cycleTime);
			}
		}
	}


	private void doChgOrder() {
		int x, olCnt = 0, totalQty, calcTime;
		int startTime = 0, endTime, respTime, meanThink, thinkTime, elapsedTime, cycleTime;
		OrderStatus oStat = null;	// returned by getOrderStatus method
		int oid = 0;	// order id of last getOrderStatus
		boolean fail = false, badCredit = false;
		boolean tryAgain = true, useNew = false, deleteOrder = false;
		int retries = 0, indx = -1;

		calcTime = timer.getTime();	// time for computations
		// First do a getOrderStatus.
		int newCnt = newOrderIds.size();
		while (tryAgain) {
			useNew = false;
			deleteOrder = false;
			if (newCnt > 0 && r.random(1, 100) <= 50)
				useNew = true;
			if (useNew) {
				indx = r.random(1, newCnt) - 1;
				oid = ((Integer)newOrderIds.elementAt(indx)).intValue();
				if (r.random(1, 100) <= 20)
					deleteOrder = true;
			}
			else
				oid = r.random(1, numOrders);

			// We start timing here so that we only include one getOrderStatus
			// even if we re-enter this loop multiple times
			startTime = timer.getTime();
			try {
				oStat = orders.getOrderStatus(oid);
			} catch (EJBException e) {
				// No such order
				errp.println(ident + "Unexpected EJBException in getOrderStatus");
				errp.println("       " + e);
				if ( ++retries == 3) {
					errp.println(ident + "Retried getOrderStatus 3 times and failed. Last order tried is " + oid);
					errp.println("        " + e);      
					fail = true;
					break;
				}
				else
					continue;
			} catch (Exception e) {
				errp.println(ident + "Error occured during getOrderStatus of order " + oid + " in ChgOrder");
				errp.println("        " + e);      
				fail = true;
				break;
			}
			// If order has already shipped, choose another
			if (oStat.shipDate != (java.sql.Date)null) {
				Debug.println("Retrieved ship date of " + oStat.shipDate);
				continue;
			}
			if (oStat.quantities.length == 0) {
				errp.println(ident + "Error occured during getOrderStatus of order " + oid + " in ChgOrder");
				errp.println("        getOrderStatus returned 0 quantities");
				fail = true;
			}
			else if (oStat.quantities.length == 1) {
				/* 
				 * We don't want to use this order as it is possible that we may
				 * end up deleting the orderline and left with olcnt = 0
				 */
				continue;
			}
			else	
				olCnt = r.random(1, oStat.quantities.length);
			tryAgain = false;
		}
		/*
		 * Now setup parameters for a change order
		 * 90% of the time, we change the current order by
		 * adding or subtracting 1 from the qty for the first
		 * olCnt orderlines.
		 * The remaining 10% of the time, we delete the order
		 */
		if ( ! fail) {
			if ( ! deleteOrder) {
				ItemQuantity itms[] = new ItemQuantity[olCnt];
				String itmId; 
				int itmQty;
				for (int i = 0; i < olCnt; i++) {
					itmId = oStat.quantities[i].itemId;
					if (i % 2 > 0)
						itmQty = oStat.quantities[i].itemQuantity - 1;
					else
						itmQty = oStat.quantities[i].itemQuantity + 1;
					itms[i] = new ItemQuantity(itmId, itmQty);
				}
				// Now call chgOrder method.
				try {
					orders.changeOrder(oid, itms);
                                } catch (InsufficientCreditException ie) {
                                        // We don't need to print this as it is not an app error
                                        // Add this as one of the failed Tx due to InsufficientCreditException
                                        badCredit = true; 
				} catch (Exception e) {
					errp.println(ident + "Error occured in changeOrder of order " + oid);
					errp.println("        Number of orderlines = " + olCnt);
					for (int i = 0; i < olCnt; i++) {
						errp.println("        itemId = " + itms[i].itemId +
							"itemQuantity = " + itms[i].itemQuantity);
					}
					errp.println("        " + e);      
					fail = true;
				}
			}
			else {
				try {
					orders.cancelOrder(oid);

					// Remove this oid from list, so we don't reuse it
					newOrderIds.removeElementAt(indx);
				} catch (Exception e) {
					errp.println(ident + "Error occured in cancelOrder for order " + oid);
					errp.println("        " + e);      
					fail = true;
				}
			}
		}
		endTime = timer.getTime();

		// Compute think time to use
		calcTime = startTime - calcTime;	// time taken to setup call
		respTime = endTime - startTime;
		cycleTime = respTime + calcTime;
		thinkTime = timeForThisTx - cycleTime;
		// Debug.println("Chgorder thinkTime = " + thinkTime);
		if (thinkTime > 0) {
			cycleTime += thinkTime;
			try {
				Thread.sleep(thinkTime);
			} catch (InterruptedException ie) { }
		}
		// Store elapsed time info for thruput
		elapsedTime = endTime - benchStartTime;
		if ( ! fail) {
			stats.updateThruput(OrdersStats.CHGORDER, elapsedTime);

			// Post all stats if in stdy-state
			if ( !inRamp && endTime <= endStdyState) {
                                if (badCredit)
                                    stats.chgoBadCredit++;
                                if(deleteOrder)
                                    stats.cancelOrdCnt++;
				stats.update(OrdersStats.CHGORDER, respTime, timeForThisTx, cycleTime);
			}
		}
	}


	private void doOrderStatus() {
		int startTime = 0, endTime, respTime, meanThink, thinkTime, elapsedTime;
		OrderStatus oStat;		// returned by getOrderStatus method
		int oid;		// order id of last getOrderStatus
		boolean fail = false;
		boolean tryAgain = true, useNew = false;
		int calcTime;	// Time taken by Driver for calculations
		int cycleTime;
		int retries = 0;

		calcTime = timer.getTime();
		int newCnt = newOrderIds.size();
		while (tryAgain) {
			if (newCnt > 0 && r.random(1, 100) <= 50)
				useNew = true;
			if (useNew) {
				int indx = r.random(1, newCnt) - 1;
				oid = ((Integer)newOrderIds.elementAt(indx)).intValue();
			}
			else
				oid = r.random(1, numOrders);
			startTime = timer.getTime();
			try {
				oStat = orders.getOrderStatus(oid);
				tryAgain = false;
			} catch (EJBException ee) {
				// No such order, try again
				errp.println(ident + "Unexpected EJBException in getOrderStatus");
				errp.println("       " + ee);
				if ( ++retries == 3) {
					errp.println(ident + "Retried getOrderStatus 3 times and failed. Last order tried is " + oid);
					errp.println("        " + ee);
					fail = true;
					break;
				}
				else
					continue;
			} catch (Exception e) {
				errp.println(ident + "Error occured during getOrderStatus of order " + oid + " in doOrderStatus");
				errp.println("        " + e);      
				fail = true;
				tryAgain = false;
			}
		}
		endTime = timer.getTime();

		// Compute think time to use
		respTime = endTime - startTime;
		calcTime = startTime - calcTime;	// time spent in Driver
		cycleTime = respTime + calcTime;
		thinkTime = timeForThisTx - respTime - calcTime;
		if (thinkTime > 0) {
			cycleTime += thinkTime;
			try {
				Thread.sleep(thinkTime);
			} catch (InterruptedException ie) { }
		}
		// Store elapsed time info for thruput
		elapsedTime = endTime - benchStartTime;
		if ( !fail) {
			stats.updateThruput(OrdersStats.ORDERSTATUS, elapsedTime);

			// Post all stats if in stdy-state
			if ( !inRamp && endTime <= endStdyState) {
			stats.update(OrdersStats.ORDERSTATUS, respTime, timeForThisTx, cycleTime);
			}
		}
	}


	private void doCustStatus() {
		int startTime, endTime, respTime, meanThink, thinkTime, elapsedTime;
		CustomerStatus cstat[];
		int cid;		// cid to use for this tx.
		boolean fail = false;
		int cycleTime;

		// Choose a customer based on NURand
		cid = r.random(1, custDBSize);
		startTime = timer.getTime();
		try {
			cstat = orders.getCustomerStatus(cid);
		} catch (Exception e) {
			errp.println(ident + "Error occured during getCustStatus for customer " + cid);
			errp.println("        " + e);
			fail = true;
		}
		endTime = timer.getTime();

		// Compute think time to use
		respTime = endTime - startTime;
		cycleTime = respTime;
		thinkTime = timeForThisTx - respTime;
		if (thinkTime > 0) {
			cycleTime += thinkTime;
			try {
				Thread.sleep(thinkTime);
			} catch (InterruptedException ie) { }
		}
		// Store elapsed time info for thruput
		elapsedTime = endTime - benchStartTime;
		if ( ! fail) {
			stats.updateThruput(OrdersStats.CUSTSTATUS, elapsedTime);

			// Post all stats if in stdy-state
			if ( !inRamp && endTime <= endStdyState) {
				stats.update(OrdersStats.CUSTSTATUS, respTime, timeForThisTx, cycleTime);
			}
		}
	}
}
