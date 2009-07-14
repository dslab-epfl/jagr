
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: DataIntegrityException.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.common;


/**
 * Class DataIntegrityException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class DataIntegrityException extends ECperfException {

    private Exception nextException;

    /**
     * Constructor DataIntegrityException
     *
     *
     * @param previous
     *
     */
    public DataIntegrityException(Exception previous) {
        super(previous);
    }

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public DataIntegrityException(String message) {
        super(message);
    }

    /**
     * Constructor DataIntegrityException
     *
     *
     * @param previous
     * @param message
     *
     */
    public DataIntegrityException(Exception previous, String message) {
        super(previous, message);
    }
    
    /**
     * Method equals
     * Compares two DataIntegrityException
     *
     * @param DataIntegrityException 
     * 
     *
     */    
    public boolean equals(DataIntegrityException theOther) {
        return this.getMessage().equals(theOther.getMessage());
    }
}

