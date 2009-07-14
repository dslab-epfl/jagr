
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.supplier.scomponentent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the public interface of the Component Entity bean.
 * It provides methods to update inventory and check for outstanding
 * purchase orders.
 * @author Damian Guy
 */
public interface SComponentEnt extends EJBObject {

    /**
     * getID - get the id of this component.
     * @return String
     * @exception RemoteException - if there is a system failure.
     */
    public String getID() throws RemoteException;

    /**
     * getQtyOnOrder: get method for qtyOnOrder instance variable.
     * @return int - the quantity on order for this component.
     * @exception RemoteException - if there is a system failure.
     */
    public int getQtyOnOrder() throws RemoteException;

    /**
     * getQtyDemanded: get methof for qtyDemanded instance variable.
     * @return int - the quantity currently demanded for this component.
     * @exception RemoteException - if there is a system failure.
     */
    public int getQtyDemanded() throws RemoteException;

    /**
     * getContainerSize: get method for containerSize instance variable.
     * @return int - the size of the container (How many parts to order).
     * @exception RemoteException - if there is a system failure.
     */
    public int getContainerSize() throws RemoteException;

    /**
     * getLeadTime: get the maximum allowable lead time
     * for this component.
     * @return int -the maximum lead time.
     *
     * @throws RemoteException
     */
    public int getLeadTime() throws RemoteException;

    /**
     * checkForPO: check if there is an outstanding PO
     * for this component, and if the qtyOnOrder will
     * satisfy the qtyDemanded + the current qty required.
     *
     * @param qtyRequired
     * @return boolean - true if above condition satisified.
     * @exception RemoteException - if there is a system failure.
     */
    public boolean checkForPO(int qtyRequired) throws RemoteException;

    /**
     * updateDemand: update the qtyDemanded for a component.
     * @param qtyRequired - quantity to add to existing qtyDemanded.
     * @exception RemoteException - if there is a system failure.
     */
    public void updateDemand(int qtyRequired) throws RemoteException;

    /**
     * updateQuantities: update the qtyOnOrder and qtyDemanded fields.
     *
     * @param qtyOrdered
     * @param qtyDemanded - qty to add to qtyDemanded.
     * @exception RemoteException - if there is a system failure.
     */
    public void updateQuantities(int qtyOrdered, int qtyDemanded)
        throws RemoteException;

    /**
     * deliveredQuantity: used to update the qtyOnOrder and
     * qtyDemanded fields when an order has been delivered.
     * @param quantity - quantity that was delivered.
     * @exception RemoteException - if there is a system failure.
     */
    public void deliveredQuantity(int quantity) throws RemoteException;
}

