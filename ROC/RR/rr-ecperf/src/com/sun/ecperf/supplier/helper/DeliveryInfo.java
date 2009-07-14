
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.supplier.helper;


import java.io.Serializable;


/**
 * This class is the DeliveryInfo Object passed to the deliveredPO
 * method of the Receiver bean.
 *
 * @author Damian Guy
 */
public class DeliveryInfo implements Serializable {

    public int    poId;
    public int    lineNumber;
    public String partID;
    public int    qty;

    /**
     * Constructor DeliveryInfo
     *
     *
     * @param poId
     * @param lineNumber
     * @param partID
     * @param qty
     *
     */
    public DeliveryInfo(int poId, int lineNumber, String partID, int qty) {

        this.poId        = poId;
        this.lineNumber = lineNumber;
        this.partID     = partID;
        this.qty         = qty;
    }
}

