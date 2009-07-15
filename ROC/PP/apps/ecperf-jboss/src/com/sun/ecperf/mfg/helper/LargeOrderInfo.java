
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: LargeOrderInfo.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 */
package com.sun.ecperf.mfg.helper;


import java.io.*;


/**
 * Class LargeOrderInfo
 *
 *
 * @author
 * @version %I%, %G%
 */
public class LargeOrderInfo implements Serializable {

    public Integer       id;
    public int           salesOrderId;
    public int           orderLineNumber;
    public String        assemblyId;
    public short         qty;
    public java.sql.Date dueDate;

    /**
     * Method duplicate
     *
     *
     * @return
     *
     */
    public LargeOrderInfo duplicate() {

        LargeOrderInfo loi = new LargeOrderInfo();

        loi.id              = this.id;
        loi.salesOrderId    = this.salesOrderId;
        loi.orderLineNumber = this.orderLineNumber;
        loi.assemblyId      = this.assemblyId;
        loi.qty             = this.qty;
        loi.dueDate         = this.dueDate;

        return loi;
    }
}

