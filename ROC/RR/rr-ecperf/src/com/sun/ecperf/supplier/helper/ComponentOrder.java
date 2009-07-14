
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: ComponentOrder.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.helper;


//Import statements
import java.io.Serializable;


/**
 * Class to hold information about a component that is
 * to be ordered.
 *
 * @author Damian Guy
 */
public class ComponentOrder implements Serializable {

    public String id;
    public int    qty;
    public int    leadTime;
    public double balance;

    /**
     * Constructor ComponentOrder
     *
     *
     * @param id
     * @param qty
     * @param leadTime
     * @param balance
     *
     */
    public ComponentOrder(String id, int qty, int leadTime, double balance) {

        this.id       = id;
        this.qty      = qty;
        this.leadTime = leadTime;
        this.balance  = balance;
    }
}

