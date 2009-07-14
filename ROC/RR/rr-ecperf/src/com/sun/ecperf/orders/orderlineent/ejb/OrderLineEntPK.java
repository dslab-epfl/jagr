
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.orders.orderlineent.ejb;


import java.io.Serializable;


/**
 * Primary Key class for OrderLine
 * The OrderLineBean's primary key is a combination of the orderId and
 * the id of the line within the order.
 */
public class OrderLineEntPK implements Serializable {

    public int id;
    public int orderId;

    /**
     * Constructor OrderLineEntPK
     *
     *
     */
    public OrderLineEntPK() {}

    /**
     * Constructor OrderLineEntPK
     *
     *
     * @param id
     * @param orderId
     *
     */
    public OrderLineEntPK(int id, int orderId) {
        this.id      = id;
        this.orderId = orderId;
    }

    /**
     * Method hashCode
     *
     *
     * @return
     *
     */
    public int hashCode() {
        return id | orderId;
    }

    /**
     * Method equals
     *
     *
     * @param rhs
     *
     * @return
     *
     */
    public boolean equals(Object rhs) {

        if (rhs instanceof OrderLineEntPK) {
            OrderLineEntPK that = (OrderLineEntPK) rhs;

            return (this.id == that.id) && (this.orderId == that.orderId);
        }

        return false;
    }
}

