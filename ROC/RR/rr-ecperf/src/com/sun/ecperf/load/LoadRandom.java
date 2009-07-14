
/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: LoadRandom.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 * This file contains common routines for the random number generators
 * @author Shanti Subramanyam
 */
package com.sun.ecperf.load;


import java.lang.*;

import java.util.*;


/**
 * Class LoadRandom
 *
 *
 * @author
 * @version %I%, %G%
 */
public class LoadRandom {

    private static int    seed = 111119;
    private Random        r;
    private static String alpha =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /*
     * Constructor
     * Initialize random number generators
     */
    LoadRandom() {
        r = new Random();
    }

    /*
     * Function :random
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
     * TPC_C function NURand(A, x, y) =
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
     * Function: make_a_string [x..y]
     * Generates a random string of alphanumeric characters of random length
     * of mininum x, maximum y and mean (x+y)/2
     */

    /**
     * Method make_a_string
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public String make_a_string(int x, int y) {

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
     * Function: make_a_ORIGINAL
     * This function is used to put the string "ORIGINAL" in a random
     * position.
     */

    /**
     * Method make_a_ORIGINAL
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public String make_a_ORIGINAL(int x, int y) {

        int    i, j, len, orig;
        String str = "";

        if (x == y) {
            len = x;
        } else {
            len = random(x, y);
        }

        /* Select a random position for ORIGINAL */
        orig = random(0, len - 8);    /* since the string should start at least 8 chars from end */

        for (i = 0; i < len; i++) {
            if (i == orig) {
                str = str + "ORIGINAL";
                i   += 7;    /* 1 incremented in the for loop */
            } else {
                j   = random(0, 61);
                str = str + alpha.substring(j, j + 1);
            }
        }

        return (str);
    }

    /*
     * Function: make_n_string
     * Generates a random string of numeric characters of random length
     * of mininum x, maximum y and mean (x+y)/2
     */

    /**
     * Method make_n_string
     *
     *
     * @param x
     * @param y
     *
     * @return
     *
     */
    public String make_n_string(int x, int y) {

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

    /*
     * Function: make_clast
     * Make a customer's last name
     */
    static String clast_arr[] = {
        "BAR", "OUGHT", "ABLE", "PRI", "PRES", "ESE", "ANTI", "CALLY",
        "ATION", "EING"
    };

    /**
     * Method make_clast
     *
     *
     * @param cid
     *
     * @return
     *
     */
    public String make_clast(int cid) {

        int    i, j;
        String str;

        if (cid < 1001) {
            i = cid - 1;
        } else {
            i = NURand(255, 0, 999);    /* Get the number to determine the 3 syllables */
        }

        str = clast_arr[i / 100];
        str = str + clast_arr[(i / 10) % 10];
        str = str + clast_arr[i % 10];    /* 3rd syllable */

        return (str);
    }

    /*
     * Function: rand_1_3000(x, y)
     * Random permutation of x, y is a sequence of numbers from x to y, arranged in a
     * random order.
     * Used to select customer id from 1 to 3000
     */
    static short cid_array[];    /* Array element 0 not used */

    /**
     * Method init_1_3000
     *
     *
     */
    public void init_1_3000() {

        int i;

        for (i = 0; i <= 3000; i++) {
            cid_array[i] = 0;
        }
    }

    /**
     * Method rand_1_3000
     *
     *
     * @return
     *
     */
    public int rand_1_3000() {

        int r;

        while (true) {
            r = random(1, 3000);

            if (cid_array[r] != 0) {    /* This number already taken */
                continue;
            }

            cid_array[r] = 1;           /* mark taken */

            break;
        }

        return (r);
    }
}

