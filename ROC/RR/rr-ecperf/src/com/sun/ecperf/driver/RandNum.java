/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: RandNum.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.driver;
import java.lang.*;
import java.util.*;

/**
 * This file generates random numbers for the RTE programs
 * Adapted from the TPCC RTE program which is proprietary to 
 * Sun Microsystems Inc.
 * 
 * @author Shanti Subramanyam
 */
public class RandNum {
	private Random r;
	private static String alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


/*
 * Constructor
 * Initialize random number generators 
 */
	RandNum(long seed)
	{
		r = new Random(seed);
	}

	RandNum() {
		r = new Random();
	}


/**
 * Select a random number uniformly distributed between x and y,
 * inclusively, with a mean of (x+y)/2 
 * @param x, y - range in which to select random number
 */
public int random(int x, int y)
{
	int n = r.nextInt(y);
        if(n < x) {
            n += x;
            if(n > y) {
                n = (x + (n-x) % (y - x + 1));
            }
        }
        return n;
}

/**
 * Select a double random number in specified range
 */
public double drandom(double x, double y)
{
	return ( x + (r.nextDouble()* (y - x)));
}


/**
 * Function NURand(A, x, y) = 
 *	((random(0,A) | random(x,y)) % (y - x + 1)) + x
 */
public int NURand(int A, int x, int y)
{
	int nurand;

	nurand = ((random(0, A) | random(x, y)) % (y - x + 1)) + x;
	return(nurand);
}


/**
 * Generates a random string of alphanumeric characters of random length
 * of mininum x, maximum y and mean (x+y)/2
 */
public String make_a_string(int x, int y)
{
	int len;	/* len of string */
	int i;
	String str = "";

	if ( x == y)
		len = x;
	else
		len = random(x, y);

	for (i=0; i < len; i++) {
		int j = random(0,61);
		str = str + alpha.substring(j,j+1);
	}
	return(str);
}

/**
 * Generates a random string of numeric characters of random length
 * of mininum x, maximum y and mean (x+y)/2
 */
public String make_n_string(int x, int y)
{
	int len, i;
	String str = "";

	if ( x == y)
		len = x;
	else
		len = random(x, y);

	for (i = 0; i < len; i++) 
		str = str + random(0, 9);
	return(str);
}
}
