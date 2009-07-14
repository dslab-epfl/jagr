/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Timer.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */

package com.sun.ecperf.driver;

/**
 * This class has the functions to get timestamps
 */
public class Timer implements java.io.Serializable {
	long startSec;

	public Timer() {
		startSec = System.currentTimeMillis();
	//	Debug.println("Timer: startSec = " + startSec);
	}


	public long getOffsetTime() {
		Debug.println("Timer: startSec in getOffsetTime = " + startSec);
		return(startSec);
	}


	/**
	 * This  method returns the current time relative to startSec.
	 * This way, we don't need to keep track of large numbers and
	 * worry about long variables
	 */
	public int getTime() {
		long c = System.currentTimeMillis();
		return ((int)(c - startSec));
	}

}
