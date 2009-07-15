
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POLineEnt.java,v 1.1 2004/02/19 14:45:08 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.polineent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;


/**
 * This is the remote interface for the Purchase Order Line Entity Bean.
 *
 * @author Damian Guy
 */
public interface POLineEnt extends EJBObject {

    /**
     * createXml - generate xml for this line.
     * @return String - the generated XML.
     * @exception RemoteException - if there is a system failure.
     */
    public String createXml() throws RemoteException;

    /**
     * setDeliveredDate: Sets the date that a Line item was delivered.
     * @param date  - the date that the delivery was made.
     * @exception RemoteException - if there is a system failure.
     */
    public void setDeliveredDate(java.sql.Date date) throws RemoteException;
}

