
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CustomerItem.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 */
package com.sun.ecperf.webbeans;


/**
 * This is a convenience class used by helper beans and jsp files
 * to store item information.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class CustomerItem implements java.io.Serializable {

    public String item_name;
    public String item_id;
    public int    qty;

    /**
     * Constructor CustomerItem
     *
     *
     * @param item_NAME
     * @param item_ID
     * @param QTY
     *
     */
    public CustomerItem(String item_NAME, String item_ID, int QTY) {

        this.item_name = item_NAME;
        this.item_id   = item_ID;
        this.qty       = QTY;
    }

    /**
     * Constructor CustomerItem
     *
     *
     * @param item_ID
     * @param QTY
     *
     */
    public CustomerItem(String item_ID, int QTY) {

        this.item_name = null;
        this.item_id   = item_ID;
        this.qty       = QTY;
    }
}

