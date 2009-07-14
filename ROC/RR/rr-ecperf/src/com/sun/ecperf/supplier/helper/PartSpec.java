
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: PartSpec.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.helper;


//Import statements
import java.io.Serializable;


/**
 * This class represents a Part supplied buy a particular supplier.
 *
 *
 * @author Damian Guy
 */
public class PartSpec implements Serializable {

    /** added field suppID **/
    public Integer suppID;
    public String  pID;
    public double  price;
    public int     qty;
    public double  disc;
    public int     delDate;

    /**
     * Constructor PartSpec
     *
     *
     * @param suppID
     * @param pID
     * @param price
     * @param qty
     * @param disc
     * @param delDate
     *
     */
    public PartSpec(Integer suppID, String pID, double price, int qty,
                    double disc, int delDate) {

        this.suppID  = suppID;
        this.pID     = pID;
        this.price    = price;
        this.qty      = qty;
        this.disc     = disc;
        this.delDate = delDate;
    }

    /**
     * Constructor PartSpec
     *
     *
     */
    public PartSpec() {}

    /**
     * Method calculatePrice
     *
     *
     * @return
     *
     */
    public double calculatePrice() {
        return (price - price * disc) / qty;
    }
}

