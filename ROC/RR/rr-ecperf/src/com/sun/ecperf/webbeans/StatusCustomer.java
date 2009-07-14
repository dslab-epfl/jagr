
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: StatusCustomer.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.webbeans;


/**
 * This is a convenience class used by helper beans and jsp files
 * to store customer status.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class StatusCustomer implements java.io.Serializable {

    public String       order_num;
    public String       ship_date;
    public CustomerItem cust_items[];
}

