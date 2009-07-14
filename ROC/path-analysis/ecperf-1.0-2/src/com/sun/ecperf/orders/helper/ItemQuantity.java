
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: ItemQuantity.java,v 1.1.1.1 2002/11/16 05:35:27 emrek Exp $
 *
 */
package com.sun.ecperf.orders.helper;


/**
 * An object of this class is used to represent item, qty pairs
 * in line items of an order.
 */
public class ItemQuantity implements java.io.Serializable {

    /**
     * Constructor ItemQuantity
     *
     *
     * @param itemId
     * @param itemQuantity
     *
     */
    public ItemQuantity(String itemId, int itemQuantity) {
        this.itemId       = itemId;
        this.itemQuantity = itemQuantity;
    }

    public String itemId;
    public int    itemQuantity;
}

