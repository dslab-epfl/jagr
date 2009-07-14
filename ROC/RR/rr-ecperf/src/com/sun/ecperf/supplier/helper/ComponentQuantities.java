
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: ComponentQuantities.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.helper;


//Import statements
import java.io.*;

import com.sun.ecperf.supplier.scomponentent.ejb.*;


/**
 * Class to hold a Component and the quantity that is
 * required.
 *
 * @author Damian Guy
 */
public class ComponentQuantities implements Serializable {

    public SComponentEnt component;
    public int           quantity;

    /**
     * Constructor ComponentQuantities
     *
     *
     * @param component
     * @param qty
     *
     */
    public ComponentQuantities(SComponentEnt component, int qty) {
        this.component = component;
        this.quantity  = qty;
    }
}

