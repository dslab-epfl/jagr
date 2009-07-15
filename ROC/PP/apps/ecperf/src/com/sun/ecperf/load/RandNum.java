
/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: RandNum.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 * This file contains common routines for the random number generators
 * @author Shanti Subramanyam
 */
package com.sun.ecperf.load;


import java.lang.*;

import java.util.*;


/**
 * Class RandNum
 *
 *
 * @author
 * @version %I%, %G%
 */
public class RandNum {

    private static int    seed = 111119;
    private Random        r;
    private static String alpha =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /*
     * Constructor
     * Initialize random number generators
     */
    RandNum() {
        r = new Random();
    }

    /*
     * Function random
     * Select a random number uniformly distributed between x and y,
     * inclusively, with a mean of (x+y)/2
     */

    /**
     * Method random
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public int random(int x, int y) {

        int n = r.nextInt();

        return (x + (Math.abs(n) % (y - x + 1)));
    }

    /*
     * Function lrandom
     * Select a long random number uniformly distributed between x and y,
     * inclusively, with a mean of (x+y)/2
     */

    /**
     * Method lrandom
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public long lrandom(long x, long y) {

        long n = r.nextLong();

        return (x + (Math.abs(n) % (y - x + 1)));
    }

    /*
     * Function drandom
     * Same as above, but returns double
     */

    /**
     * Method drandom
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public double drandom(double x, double y) {
        return (x + (r.nextDouble() * (y - x)));
    }

    /*
     * FUnction: NURand
     * TPC-C function NURand(A, x, y) =
            (((random(0,A) | random(x,y)) + C) % (y - x + 1)) + x
     */

    /**
     * Method NURand
     *
     *
     * @param A
     * @param x
     * @param y
     *
     * @return
     *
     */
    public int NURand(int A, int x, int y) {

        int C, nurand;

        C      = 123;    /* Run-time constant chosen between 0, A */
        nurand = (((random(0, A) | random(x, y)) + C) % (y - x + 1)) + x;

        return (nurand);
    }

    /*
     * Function: makeAString [x..y]
     * Generates a random string of alphanumeric characters of random length
     * of mininum x, maximum y and mean (x+y)/2
     */

    /**
     * Method makeAString
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public String makeAString(int x, int y) {

        int    len;    /* len of string */
        int    i;
        String str = "";

        if (x == y) {
            len = x;
        } else {
            len = random(x, y);
        }

        for (i = 0; i < len; i++) {
            int j = random(0, 61);

            str = str + alpha.substring(j, j + 1);
        }

        return (str);
    }

    /*
     * Function: makeNString
     * Generates a random string of numeric characters of random length
     * of mininum x, maximum y and mean (x+y)/2
     */

    /**
     * Method makeNString
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public String makeNString(int x, int y) {

        int    len, i;
        String str = "";

        if (x == y) {
            len = x;
        } else {
            len = random(x, y);
        }

        for (i = 0; i < len; i++) {
            str = str + random(0, 9);
        }

        return (str);
    }
}

