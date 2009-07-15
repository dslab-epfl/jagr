
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 *
 */
package com.sun.ecperf.orders.cartses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import javax.rmi.PortableRemoteObject;

import java.util.*;

import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.orders.orderses.ejb.*;
import com.sun.ecperf.common.*;


/**
 * This class is the implementation of the CartBean
 * It is a stateful bean used for creating new orders.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class CartSesEJB implements SessionBean {

    private Vector items_list;
    int            customer_id;
    protected boolean              debugging;
    Debug          debug;
    SessionContext ctx;
    OrderSesHome   orderses_home;

    /**
     * create method
     * @param cust_id - Id of the customer creating Cart Bean.
     *
     */
    public void ejbCreate(int cust_id) throws CreateException {
        this.customer_id = cust_id;
        items_list       = new Vector();
    }

    /**
     * create method
     *
     */
    public void ejbCreate() throws CreateException {
        items_list = new Vector();
    }

    /**
     * Method add - add an item to the list
     * First check if the item exists, if it does, then add new qty to
 * the current qty.
     * @param item_qty - contains item_id and qty.
     */
    public void add(ItemQuantity item_qty) throws RemoteException {

        ItemQuantity cur_item_qty;
        int          new_qty;

        for (int i = 0; i < items_list.size(); i++) {
            cur_item_qty = (ItemQuantity) items_list.elementAt(i);

            if (cur_item_qty.itemId.equals(item_qty.itemId)) {
                new_qty      = item_qty.itemQuantity
                               + cur_item_qty.itemQuantity;
                cur_item_qty = new ItemQuantity(item_qty.itemId, new_qty);

                items_list.setElementAt(cur_item_qty, i);

                return;
            }
        }

        items_list.add(item_qty);
    }

    /**
 * delete: Delete item from the cart
 * @param item_id   - item id of the item to be deleted
 * @exception RemoteException - if there is a system failure.
 */
    public void delete(String item_id) throws RemoteException {

        ItemQuantity cur_item_qty;

        for (int i = 0; i < items_list.size(); i++) {
            cur_item_qty = (ItemQuantity) items_list.elementAt(i);

            if (cur_item_qty.itemId.equals(item_id)) {
                items_list.removeElementAt(i);

                return;
            }
        }
    }

    /**
 * deleteAll: Delete all items from the cart
 * @exception RemoteException - if there is a system failure.
 */
    public void deleteAll() throws RemoteException {
        items_list.removeAllElements();
    }

    /**
 * buy: Buy the contents of the cart. This will call newOrder method
 *      of OrderSes bean.
     *
     * @return int - Order Id of the new order created.
 * @exception RemoteException - if there is a system failure.
 * @exception CreateException - if creation of newOrder fails.
 * @exception InsufficientCreditException - if newOrder fails due to bad credit.
 */
    public int buy() throws RemoteException, CreateException, InsufficientCreditException {

        OrderSes       orderses;
        ItemQuantity[] iqty;

        if(debugging)
            debug.println(3, "In buy method of CartSesBean");

        if (items_list.size() == 0) {
            throw new EJBException(
                " No Items in the list. Please add items to the list");
        }

        iqty = new ItemQuantity[items_list.size()];

        for (int i = 0; i < items_list.size(); i++) {
            iqty[i] =
                new ItemQuantity(((ItemQuantity) items_list.elementAt(i))
                    .itemId, ((ItemQuantity) items_list.elementAt(i))
                    .itemQuantity);
        }

        try {
            orderses      = orderses_home.create();
            int oid = orderses.newOrder(customer_id, iqty);
            orderses.remove();
            return oid;
        } catch (CreateException e) {
            if(debugging)
                debug.println(3, e.getMessage());

            throw new EJBException("Create Exception in OrderSesBean");
        } catch (DataIntegrityException e) {
            if(debugging)
                debug.println(3, e.getMessage());

            throw new EJBException(" Error processing the request: "
                                   + e.getMessage());
        } catch (RemoteException e) {
            if(debugging)
                debug.println(3, e.getMessage());

            throw new EJBException(
                " Remote Exception occured for the request.");
        } catch (RemoveException e) {
            if(debugging)
                debug.println(3, e.getMessage());

            throw new EJBException(
                " Deletion of Order ses failed. Please try again.");
        }
    }

    /**
     * removeItem: remove an item to the list
     * First check if the item exists, if it does, then remove new qty from
 * the current qty.
     * @param item_qty - contains item_id and qty.
     */
    public void removeItem(ItemQuantity item_qty) throws RemoteException {

        ItemQuantity cur_item_qty;
        int          new_qty;

        for (int i = 0; i < items_list.size(); i++) {
            cur_item_qty = (ItemQuantity) items_list.elementAt(i);

            if (cur_item_qty.itemId.equals(item_qty.itemId)) {
                if (item_qty.itemQuantity >= cur_item_qty.itemQuantity) {
                    items_list.removeElementAt(i);
                } else {
                    new_qty      = cur_item_qty.itemQuantity
                                   - item_qty.itemQuantity;
                    cur_item_qty = new ItemQuantity(item_qty.itemId, new_qty);

                    items_list.setElementAt(cur_item_qty, i);

                    return;
                }
            }
        }
    }

    /**
 * getItemsList: Get the list of items in the cart. This is used only
 *                  by GUI client
     *
     * @return
 * @exception RemoteException - if there is a system failure.
 */
    public Vector getItemsList() throws RemoteException {
        return items_list;
    }

    /**
 * setCustId: Set Customer Id. This is used only by GUI client
     * @param cust_id   - Id of the customer
 * @exception RemoteException - if there is a system failure.
 */
    public void setCustId(int cust_id) throws RemoteException {
        this.customer_id = cust_id;
    }

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {
        items_list.removeAllElements();
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {}

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {}

    /**
     * Method setSessionContext
     *
     *
     * @param ctx
     *
     */
    public void setSessionContext(SessionContext ctx) {
        this.ctx = ctx;
        final String   jndiname = "java:comp/env/ejb/OrderSes";
        int debugLevel = 0;

        try {
            InitialContext initCtx    = new InitialContext();

            try {
                debugLevel =
                    ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
                        .intValue();

                if (debugLevel > 0) {
                    debug = new DebugPrint(debugLevel, this);
                    debugging = true;
                } else {
                    debug = new Debug();
                    debugging = false;
                }

            } catch (NamingException e) {

                // If there's an error looking up debuglevel,
                // just leave it as the default - 0
                e.printStackTrace(System.err);
            }

            debug = (debugLevel > 0)
                    ? new DebugPrint(debugLevel, this)
                    : new Debug();

            if(debugging)
                debug.println(3, "In setSessionContext method of CartSesBean");

            Object obj = initCtx.lookup(jndiname);

            if(debugging)
                debug.println(3, "Looked up " + jndiname);

            orderses_home = (OrderSesHome) PortableRemoteObject.narrow(obj,
                    OrderSesHome.class);
        } catch (NamingException e) {
            throw new EJBException("Naming Exception in OrderSesBean");
        } catch (ClassCastException e) {
            throw new EJBException("Class cast Exception in OrderSesBean");
        } 
    }
}

