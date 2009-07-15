
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ItemNameId.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 */
package com.sun.ecperf.webbeans;


/**
 * This is a convenience class used by helper beans to store information
 * about item names and their matching ids.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class ItemNameId implements java.io.Serializable {

    public String item_name;
    public String item_id;

    /**
     * Constructor ItemNameId
     *
     *
     * @param item_NAME
     * @param item_ID
     *
     */
    public ItemNameId(String item_NAME, String item_ID) {
        this.item_name = item_NAME;
        this.item_id   = item_ID;
    }
}

