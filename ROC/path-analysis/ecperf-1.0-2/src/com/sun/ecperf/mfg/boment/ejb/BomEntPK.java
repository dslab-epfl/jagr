
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.boment.ejb;


import java.io.Serializable;


/**
 * Primary Key for the BOM entity bean. Special class needed
 * because the primary key is composed of several fields
 *
 * @author Agnes Jacob
 *
 * @see BomBmpEJB.java
 * @see BomEntHome.java
 */
public class BomEntPK implements Serializable {

    public String assemblyId;
    public String componentId;
    public int    lineNo;

    /**
     * Default public constructor. This seems to be requirement
     * when defining a customized primary key class.
     */
    public BomEntPK() {}

    /**
     * Initializes this object with the following params:
     * @param assemblyId - assembly Id
     * @param componentId - componentId
     * @param lineNo - Production line number
     */
    public BomEntPK(String assemblyId, String componentId, int lineNo) {

        this.assemblyId  = assemblyId;
        this.componentId = componentId;
        this.lineNo      = lineNo;
    }

    /**
     * hashCode method for this object which is a requirement
     * wben defining a customized primary key class
     * @return Integer value which is a string
     * hash of assemblyId, componentId, and lineNo.
     */
    public int hashCode() {
        return (assemblyId + componentId + lineNo).hashCode();
    }

    /**
     * Equals method for this object which is a requirement
     * wben defining a customized primary key class
     * @param obj - object to compare with.
     * @return a true/false value depending on comparison of this object
     * and object passed in.
     */
    public boolean equals(Object obj) {

        if (obj instanceof BomEntPK) {
            BomEntPK that = (BomEntPK) obj;

            return ((assemblyId.equals(that.assemblyId))
                    && (componentId.equals(that.componentId))
                    && (lineNo == that.lineNo));
        }

        return (false);
    }

    /**
     * toString method for debugging purposes
     */
    public String toString() {
        return (assemblyId + ":::" + componentId + ":::" + lineNo);
    }
}

