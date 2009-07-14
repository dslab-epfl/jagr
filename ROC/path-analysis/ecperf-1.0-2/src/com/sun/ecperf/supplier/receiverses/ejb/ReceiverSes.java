
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: ReceiverSes.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.receiverses.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;
import com.sun.ecperf.common.ECperfException;
import com.sun.ecperf.supplier.helper.*;


/**
 * Remote interface for the the stateless session bean
 * ReceiverSes.
 *
 * @author Damian Guy
 */
public interface ReceiverSes extends EJBObject {

    /**
     * deliverPO - indicate the part of a PO has been delivered.
     * @param del - contains information about the POLine that has been delivered.
     * @exception RemoteException - if there is a system failure.
     */
    public void deliverPO(DeliveryInfo del) throws ECperfException, RemoteException;
}

