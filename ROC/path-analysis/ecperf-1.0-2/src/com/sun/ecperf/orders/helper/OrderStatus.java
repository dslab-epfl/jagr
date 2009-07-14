
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: OrderStatus.java,v 1.1.1.1 2002/11/16 05:35:27 emrek Exp $
 *
 */
package com.sun.ecperf.orders.helper;


import java.io.*;


/**
 * An object of this class is returned by OrderSession.getOrderStatus
 *
 */
public class OrderStatus implements Serializable {

    public int            customerId;
    public java.sql.Date  shipDate;
    public ItemQuantity[] quantities;
}

