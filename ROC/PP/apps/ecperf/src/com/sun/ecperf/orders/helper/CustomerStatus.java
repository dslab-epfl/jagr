
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: CustomerStatus.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.orders.helper;


import java.io.Serializable;


/**
 * An object of this class is returned by the getCustomerStatus method
 * of the OrderSession bean.
 */
public class CustomerStatus implements Serializable {

    public int            orderId;
    public java.sql.Date  shipDate;
    public ItemQuantity[] quantities;
}
