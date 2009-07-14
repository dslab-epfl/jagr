
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.helper;


import java.io.*;


/**
 * This class defines the object ComponentDemand that is basically
 * composed of the componentId and qty. Used in LargeOrderSes*
 *
 * @author Agnes Jacob
 * @see LargeOrderSesEJB.java
 */
public class ComponentDemand implements Serializable {

    String componentId;
    int    qty;

    /**
     * Constructor ComponentDemand
     *
     *
     */
    public ComponentDemand() {}

    /**
     * Sets the componentDemand fields
     * @param componentId
     * @param qty
     */
    public ComponentDemand(String componentId, int qty) {
        this.componentId = componentId;
        this.qty         = qty;
    }

    /**
     * Method getComponentId
     *
     *
     * @return
     *
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Method getQty
     *
     *
     * @return
     *
     */
    public int getQty() {
        return qty;
    }
}

