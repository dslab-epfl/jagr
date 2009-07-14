
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.supplier.poent.ejb;


import com.sun.ecperf.common.*;
import javax.ejb.*;
import java.rmi.*;


/**
 * This is the remote interface for the Purchase Order
 * Entity bean.
 *
 * @author Damian Guy
 */
public interface POEnt extends EJBObject {

    /**
     * poLineDeliverd - indicate that a POline has been delivered.
     * @param lineNumber - line number of delivered line.
     * @exception RemoteException - if there is a system failure.
     */
    public void poLineDelivered(int lineNumber) throws RemoteException;

    /**
     * generateXml - generates the  XML for this Purchase Order.
     * @return String - containing XML.
     * @exception RemoteException - if there is a system failure.
     */
    public String generateXml() throws ECperfException, RemoteException;
}

