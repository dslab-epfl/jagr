
/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: PartSupp.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 * Class that handles generation of parts for the supplier
 *
 * @author Shanti Subramanyam
 */
package com.sun.ecperf.load;


import java.lang.*;

import java.util.*;


/**
 * Class PartSupp
 *
 *
 * @author
 * @version %I%, %G%
 */
public class PartSupp {

    String            p_id, p_rev;
    int               p_ind, p_type;
    static LoadRandom r  = new LoadRandom();
    static int        p3 = 1;

    /*
     * Constructor
     */
    PartSupp() {

        // revision number is a combo of 4 numeric, 2 alpha
        p_rev = r.make_n_string(4, 4);
        p_rev = p_rev + r.make_a_string(2, 2);

        // We assume that 10% of the parts are manufactured
        int x = r.random(1, 100);

        if (x <= 10) {
            p_ind = 1;    /* Manufactured part */
        } else {
            p_ind = 2;    /* Purchased part */
        }

        // There are 4 types of manufactured products
        if (p_ind == 1) {
            p_type = r.random(1, 4);
        } else {
            p_type = 0;
        }
    }

    String prt(int i) {

        Integer j = new Integer(i);

        if (i < 10) {
            return ("0000" + j.toString());
        } else if (i < 100) {
            return ("000" + j.toString());
        } else if (i < 1000) {
            return ("00" + j.toString());
        } else if (i < 10000) {
            return ("0" + j.toString());
        } else {
            return (j.toString());
        }
    }
}

