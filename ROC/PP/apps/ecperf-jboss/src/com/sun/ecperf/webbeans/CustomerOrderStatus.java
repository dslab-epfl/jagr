
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CustomerOrderStatus.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 */
package com.sun.ecperf.webbeans;


/**
 * This is a convenience class used by helper beans and jsp files
 * to store customer order status.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class CustomerOrderStatus implements java.io.Serializable {

    public String       cust_id;
    public String       ship_date;
    public CustomerItem cust_items[];
}

