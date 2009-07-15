/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Debug.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */

package com.sun.ecperf.driver;

/**
 * This class can be used to print Debug messages during development.
 * <pre>
 * Use as follows : 
 * Debug.print("Now in method foo in class bar");
 * </pre>
 * For production, both the print methods should be null methods.
 */

public class Debug {
	public static void print(String msg) {
		System.out.print(msg);
	}

	public static void println(String msg) {
		System.out.println(msg);
	}
}
