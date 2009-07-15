
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POLineEntPK.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.polineent.ejb;


//Import statements
import java.io.*;


/**
 * Primary Key class for POLine Entity Bean.
 *
 *
 * @author Damian Guy
 */
public class POLineEntPK implements Serializable {

    public int poLineNumber;
    public int poLinePoID;

    /**
     * Constructor POLineEntPK
     *
     *
     */
    public POLineEntPK() {}

    /**
     * Constructor POLineEntPK
     *
     *
     * @param poLineNumber
     * @param poLinePoID
     *
     */
    public POLineEntPK(int poLineNumber, int poLinePoID) {
        this.poLineNumber = poLineNumber;
        this.poLinePoID  = poLinePoID;
    }

    /**
     * Method hashCode
     *
     *
     * @return
     *
     */
    public int hashCode() {
        return poLineNumber | poLinePoID;
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

        if (rhs instanceof POLineEntPK) {
            POLineEntPK other = (POLineEntPK) rhs;

            return (other.poLineNumber == poLineNumber)
                   && (other.poLinePoID == poLinePoID);
        }

        return false;
    }
}

