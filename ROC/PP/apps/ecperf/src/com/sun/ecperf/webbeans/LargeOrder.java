
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: LargeOrder.java,v 1.2 2003/03/22 04:55:02 emrek Exp $
 *
 */
package com.sun.ecperf.webbeans;


/**
 * This is a convenience class used by helper beans and jsp files
 * to store large orders information.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class LargeOrder implements java.io.Serializable {

    public String assembly_id;
    public int    qty;
    public String due_date;
    public int    order_line_num;
    public int    sales_order_id;

    /**
     * Constructor LargeOrder
     *
     *
     * @param ASSEMBLY_ID
     * @param QTY
     * @param DUE_DATE
     * @param ORDER_LINE_NUM
     * @param SALES_ORDER_ID
     *
     */
    public LargeOrder(String ASSEMBLY_ID, int QTY, String DUE_DATE,
                      int ORDER_LINE_NUM, int SALES_ORDER_ID) {

        this.assembly_id    = ASSEMBLY_ID;
        this.qty            = QTY;
        this.due_date       = DUE_DATE;
        this.order_line_num = ORDER_LINE_NUM;
        this.sales_order_id = SALES_ORDER_ID;
    }
}

