
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SuppCompEntPK.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.suppliercompent.ejb;


//Import statements
import java.io.Serializable;


/**
 * Primary Key class for SupplierCompEJB
 *
 *
 * @author Damian Guy
 */
public class SuppCompEntPK implements Serializable {

    public String  suppCompID;
    public int suppCompSuppID;

    /**
     * Constructor SuppCompEntPK
     *
     *
     */
    public SuppCompEntPK() {}

    /**
     * Constructor SuppCompEntPK
     *
     *
     * @param pID
     * @param suppID
     *
     */
    public SuppCompEntPK(String pID, int suppID) {
        this.suppCompID    = pID;
        this.suppCompSuppID = suppID;
    }

    /**
     * Method hashCode
     *
     *
     * @return
     *
     */
    public int hashCode() {
        return suppCompID.hashCode() | suppCompSuppID;
    }

    /**
     * Method equals
     *
     *
     * @param rhs
     *
     * @return
     *
     */
    public boolean equals(Object rhs) {

        if (rhs instanceof SuppCompEntPK) {
            SuppCompEntPK other = (SuppCompEntPK) rhs;

            return this.suppCompID.equals(other.suppCompID)
                   && (this.suppCompSuppID == other.suppCompSuppID);
        }

        return false;
    }
}

