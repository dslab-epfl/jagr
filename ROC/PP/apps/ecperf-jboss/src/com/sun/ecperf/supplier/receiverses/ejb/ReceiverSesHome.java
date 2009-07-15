
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: ReceiverSesHome.java,v 1.1 2004/02/19 14:45:12 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.receiverses.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;


/**
 * Home interface for ReceiverSes session bean.
 *
 *
 * @author Damian Guy
 */
public interface ReceiverSesHome extends EJBHome {

    /**
     * Method create
     *
     *
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     */
    public ReceiverSes create() throws RemoteException, CreateException;
}

