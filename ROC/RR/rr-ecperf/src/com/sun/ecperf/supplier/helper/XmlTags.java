
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: XmlTags.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.helper;


//Import statements

/**
 * Enum class for XML Tags used in Supplier Domain
 *
 *
 * @author Damian Guy
 */
public class XmlTags {

    public static final String XMLVERSION = "<?xml version=\"1.0\"?>";

    //public static final String DELIVERYDOC = "<!DOCTYPE DELIVERY SYSTEM \"http://pellet/delivery.dtd\">";
    public static final String DELIVERYDOC = "<!DOCTYPE DELIVERY SYSTEM ";

    //public static final String PODOC = "<!DOCTYPE PURCHASE-ORDER SYSTEM \"http://pellet/po.dtd\">";
    public static final String PODOC         =
        "<!DOCTYPE PURCHASE-ORDER SYSTEM ";
    public static final String PO            = "PURCHASE-ORDER";
    public static final String POSTART       = "<PURCHASE-ORDER>";
    public static final String POEND         = "</PURCHASE-ORDER>";
    public static final String PONUMBER      = "PO-NUMBER";
    public static final String PONUMBERSTART = "<PO-NUMBER>";
    public static final String PONUMBEREND   = "</PO-NUMBER>";
    public static final String SITE          = "SITE-ID";
    public static final String SITESTART     = "<SITE-ID>";
    public static final String SITEEND       = "</SITE-ID>";
    public static final String NUMLINES      = "NUMLINES";
    public static final String NUMLINESSTART = "<NUMLINES>";
    public static final String NUMLINESEND   = "</NUMLINES>";
    public static final String POLINE        = "POLINE";
    public static final String POLINESTART   = "<POLINE>";
    public static final String POLINEEND     = "</POLINE>";
    public static final String LINENUM       = "LINE-NUMBER";
    public static final String LINENUMSTART  = "<LINE-NUMBER>";
    public static final String LINENUMEND    = "</LINE-NUMBER>";
    public static final String PARTID        = "PART-ID";
    public static final String PARTIDSTART   = "<PART-ID>";
    public static final String PARTIDEND     = "</PART-ID>";
    public static final String QTY           = "QTY";
    public static final String QTYSTART      = "<QTY>";
    public static final String QTYEND        = "</QTY>";
    public static final String BALANCE       = "BALANCE";
    public static final String BALANCESTART  = "<BALANCE>";
    public static final String BALANCEEND    = "</BALANCE>";
    public static final String LEADTIME      = "LEAD-TIME";
    public static final String LEADTIMESTART = "<LEAD-TIME>";
    public static final String LEADTIMEEND   = "</LEAD-TIME>";
    public static final String DELIVERY      = "DELIVERY";
    public static final String DELIVERYSTART = "<DELIVERY>";
    public static final String DELIVERYEND   = "</DELIVERY>";

    /**
     * Constructor XmlTags
     *
     *
     */
    public XmlTags() {}
}

