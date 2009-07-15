
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POLine.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.emulator;


//Import statements
import java.net.*;

import java.io.*;

import com.sun.ecperf.supplier.helper.*;


/**
 * Each instance of this class represents an Orderline
 * from a Purchase Order. It is responsible for delivering
 * the OrderLine to the Supplier Domain.
 *
 * @author Damian Guy
 */
public class POLine {

    private String poNum;      // Purchase ORder that this belongs to.
    private String siteID;    // ID of site that Order should be delivered to
    private String lnum;       // Line number
    private String part;       // ID of component that was ordered.
    private String balance;    // balance due.
    private String qty;        // Qty ordered/delivered
    private int    leadTime;    // Max time that parts must be delivered within.

    /**
     * Create new POLine Object.
     * @param poNum - Purchase ORder that this belongs to.
     * @param siteID - ID of site that Order should be delivered to
     * @param lnum - Line number
     * @param part - ID of component that was ordered.
     * @param balance - balance due.
     * @param qty - Qty ordered/delivered
     * @param leadTime - Max time that parts must be delivered within.
     */
    public POLine(String poNum, String siteID, String lnum, String part,
                  String qty, String balance, int leadTime) {

        this.poNum    = poNum;
        this.siteID  = siteID;
        this.lnum     = lnum;
        this.part     = part;
        this.balance  = balance;
        this.qty      = qty;
        this.leadTime = leadTime;
    }

    /**
     * Method getXml
     *
     *
     * @return
     *
     */
    public String getXml() {

        StringBuffer xml = new StringBuffer(XmlTags.POLINESTART);

        xml.append(XmlTags.LINENUMSTART);
        xml.append(lnum);
        xml.append(XmlTags.LINENUMEND);
        xml.append(XmlTags.PARTIDSTART);
        xml.append(part);
        xml.append(XmlTags.PARTIDEND);
        xml.append(XmlTags.QTYSTART);
        xml.append(qty);
        xml.append(XmlTags.QTYEND);
        xml.append(XmlTags.POLINEEND);

        return xml.toString();
    }

    /**
     * getLeadTime.
     * @return leadTime
     */
    public int getLeadTime() {
        return leadTime;
    }
}

