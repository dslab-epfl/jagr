
/**
 * Copyright (c) 1998, 1999 by Sun Microsystems, Inc.
 *
 * $Id: RandPart.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 * @author Shanti Subramanyam
 */
package com.sun.ecperf.load;


import java.lang.*;

import java.util.*;


/**
 * Class RandPart
 *
 *
 * @author
 * @version %I%, %G%
 */
public class RandPart {

    /* The following are used by getPart */
    int    scale, numItems;
    String pId, pRev;
    int    pInd, pType;

    /*
     * The following are used by mkAssembly to make an assembly
     * and all its componets
     */
    String         partId[]   = new String[16], partRev[] = new String[16];
    int            partInd[]  = new int[16], partType[] = new int[16];
    static String  p2[]       = {
        "BARAA", "OUGHT", "ABLEA", "PRIAA", "PRESA", "ESEAA", "ANTIA",
        "CALLY", "ATION", "EINGA", "IRESE", "QUIET", "LINES", "NAILS", "SCREW"
    };
    static RandNum r          = new RandNum();
    static int     p3         = 0;              // part 3 for components
    static int     mp3        = 0;              // part 3 for assemblies
    static int     p2chosen[] = new int[15];    // keep track of p2's

    /**
     * Constructors
     */
    public RandPart(int scale) {
        this.scale = scale;
        numItems   = (int) (Math.ceil((double) scale / 100.0)) * 100;
    }

    /**
     * Constructor RandPart
     *
     *
     */
    public RandPart() {}

    /** Create a part
     * pId is a combination of 5 numeric, 5 alpha, 5 numeric
     * For manufactured parts, the pId is of the form
     * <scale>"MITEM"<n> where n ranges from 1-10. (There are
     * 10 manufactured parts per PG).
     * For purchased parts, pId is of the form
     * <scale><string><n> where n corresponds to the manufactured
     * part this relates to and <string> is chosen randomly from
     * a set of 15 strings.
     * This method creates an assembly and all its components
    */
    public int mkAssembly(int scale) {

        this.scale = scale;

        int cnt = r.random(5, 15);    // generate number of components

        for (int i = 0; i <= cnt; i++) {

            // revision number is a combo of 4 numeric, 2 alpha
            pRev       = r.makeNString(4, 4);
            partRev[i] = pRev + r.makeAString(2, 2);

            if (i == 0) {           // Assembly
                ++mp3;

                // Clear all p2chosen's, as we are starting a new m. part
                for (int k = 0; k < 15; k++) {
                    p2chosen[k] = 0;
                }

                partInd[i] = 1;     /* Manufactured part */

                // There are 4 types of manufactured products
                partType[i] = r.random(1, 4);
                partId[i]   = prt(scale) + "MITEM" + prt(mp3);
            } else {                // Component
                ++p3;

                partInd[i]  = 2;    /* Purchased part */
                partType[i] = 0;

                int j = r.random(0, 14);

                while (p2chosen[j] != 0) {
                    j = r.random(0, 14);
                }

                p2chosen[j] = 1;
                partId[i]   = prt(scale) + p2[j] + prt(p3);
            }
        }

        return (cnt);
    }

    /**
     * This method generates a random part id to use for inserting
     * into the orderline table in the orders database
     *
     *
     * @param numItems
     * @return String part id
     */
    public String getPart(int numItems) {

        /*
         * part_id is a combination of 5 numeric, 5 alpha,5 numeric
     * For manufactured parts, the part_id is of the form
     * <scale>"MITEM"<n> where n ranges from 1-P.
         */
        int    p3   = r.random(1, numItems);
        String p_id = prt(1) + "MITEM" + prt(p3);

        return (p_id);
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

