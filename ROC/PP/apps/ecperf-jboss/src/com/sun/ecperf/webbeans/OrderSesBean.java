
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderSesBean.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 *
 */
package com.sun.ecperf.webbeans;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import java.io.Serializable;

import com.sun.ecperf.orders.orderses.ejb.*;
import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.common.*;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a helper bean used by jsp files to  create a new order,
 * change an existing order, cancel an order, get customer status,
 * and to get order status.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class OrderSesBean implements Serializable {

    private final String jndiname = "java:comp/env/ejb/OrderSes";
    private OrderSesHome orderses_home;
    private OrderSes     orderses;
    protected Debug      debug;
    protected boolean    debugging;

    /**
     * Constructor OrderSesBean
     *
     *
     * @throws OtherException
     *
     */
    public OrderSesBean() throws OtherException {

        try {
            Context context    = new InitialContext();
            int     debugLevel = 0;

            try {
                debugLevel =
                    ((Integer) context.lookup("java:comp/env/debuglevel"))
                        .intValue();
            } catch (Exception e) {

                // If there's an error looking up debuglevel,
                // just leave it as the default - 0
            }
            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
            if (debugging)
                debug.println(3, "In constructor of OrderSesBean");

            Object obj = context.lookup(jndiname);

            if (debugging)
                debug.println(3, "Looked up " + jndiname);

            orderses_home = (OrderSesHome) PortableRemoteObject.narrow(obj,
                    OrderSesHome.class);
            orderses      = orderses_home.create();

            if (debugging)
                debug.println(3, "Successfully created OrderSes Bean");
        } catch (NamingException e) {
            throw new OtherException("Naming Exception in OrderSesBean", e);
        } catch (ClassCastException e) {
            throw new OtherException("Class cast Exception in OrderSesBean",
                                     e);
        } catch (RemoteException e) {
            throw new OtherException("Remote Exception in OrderSesBean", e);
        } catch (CreateException e) {
            throw new OtherException("Create Exception in OrderSesBean", e);
        } catch (Exception e) {
            throw new OtherException("Some Other exception in OrderSesBean",
                                     e);
        }
    }

    /**
     * Method newOrder - Create a new Order
     *
     *
     * @param customer_list List of all items in the order
     * @param customer_id Id of the customer
     *
     * @return int - Order number 
     *
     * @throws InvalidEntryException  Exception throw if customer id is invalid
     * @throws OtherException
     *
     */
    public int newOrder(Vector customer_list, String customer_id)
            throws InvalidEntryException, OtherException {

        int            item_qty;
        int            cust_id;
        CustomerItem   cust_item;
        ItemQuantity[] iqty;

        try {
            cust_id = Integer.parseInt(customer_id);
        } catch (NumberFormatException e) {
            throw new InvalidEntryException(
                "Customer Entry " + customer_id
                + " is invalid. Please try again");
        }

        if (customer_list.size() == 0) {
            throw new OtherException(
                "No Items in the list. Please add items to list");
        }

        iqty = new ItemQuantity[customer_list.size()];

        for (int i = 0; i < customer_list.size(); i++) {
            cust_item = (CustomerItem) customer_list.elementAt(i);
            iqty[i]   = new ItemQuantity(cust_item.item_id, cust_item.qty);
        }

        try {
            return orderses.newOrder(cust_id, iqty);
        } catch (InsufficientCreditException e) {
            if (debugging)
                debug.println(3, e.getMessage());

            throw new OtherException(
                " Credit check failed. Insufficient credit for the customer id entered");
        } catch (DataIntegrityException e) {
            if (debugging)
                debug.println(3, e.getMessage());

            throw new OtherException(" Error processing the request: "
                                     + e.getMessage(), e);
        } catch (RemoteException e) {
            if (debugging)
                debug.println(3, e.getMessage());

            throw new OtherException(
                " Remote Exception occured for the request.", e);
        } catch (CreateException e) {
            if (debugging)
                debug.println(3, e.getMessage());

            throw new OtherException(
                " Creation of New Order failed. Please try again.", e);
        }
    }

    /**
     * Method changeOrder - Change the current order
     *
     *
     * @param customer_list List of all items in the order 
     * @param order_number Order number
     *
     * @throws InvalidEntryException Exception throw if order number is invalid
     * @throws OtherException
     *
     */
    public void changeOrder(Vector customer_list, String order_number)
            throws InvalidEntryException, OtherException {

        ItemQuantity[] iqty;
        CustomerItem   cust_item;
        int            order_num;

            if (debugging)
                debug.println(3, "In changeOrder method of OrderSesBean");

        try {
            order_num = Integer.parseInt(order_number);
        } catch (NumberFormatException e) {
            throw new InvalidEntryException(
                "Order number " + order_number
                + " is invalid. Please try again");
        }

        if (customer_list.size() == 0) {
            throw new OtherException(
                "No Items in the list. Please add items to list");
        }

        iqty = new ItemQuantity[customer_list.size()];

        for (int i = 0; i < customer_list.size(); i++) {
            cust_item = (CustomerItem) customer_list.elementAt(i);
            iqty[i]   = new ItemQuantity(cust_item.item_id, cust_item.qty);
        }

        try {
            orderses.changeOrder(order_num, iqty);
        } catch (InsufficientCreditException e) {
            throw new OtherException(
                " Credit check failed. Insufficient credit ");
        } catch (RemoteException e) {
            throw new OtherException(" Remote Exception in change Order ", e);
        }
    }

    /**
     * Method getOrderStatus - Get status of the order
     *
     *
     * @param order_number Order number
     *
     * @return CustomerOrderStatus
     *
     * @throws InvalidEntryException Exception thrown if order number is invalid
     * @throws OtherException
     *
     */
    public CustomerOrderStatus getOrderStatus(String order_number)
            throws InvalidEntryException, OtherException {

        int                 order_num;
        OrderStatus         order_status;
        CustomerOrderStatus cust_status;

            if (debugging)
                debug.println(3, "In getOrderStatus of OrderSesBean");

        try {
            order_num = Integer.parseInt(order_number);
        } catch (NumberFormatException e) {
            throw new InvalidEntryException(
                "Order Number " + order_number
                + " is invalid. Please try again");
        }

        try {
            order_status           = orderses.getOrderStatus(order_num);
            cust_status            = new CustomerOrderStatus();
            cust_status.cust_id    = String.valueOf(order_status.customerId);
            cust_status.ship_date  = String.valueOf(order_status.shipDate);
            cust_status.cust_items =
                new CustomerItem[order_status.quantities.length];

            for (int i = 0; i < order_status.quantities.length; i++) {
                cust_status.cust_items[i] =
                    new CustomerItem(order_status.quantities[i].itemId,
                                     order_status.quantities[i].itemQuantity);
            }
        } catch (RemoteException e) {
            throw new OtherException(
                " Remote Exception Occured in getOrderStatus ", e);
        } catch (DataIntegrityException e) {
            throw new OtherException(
                " DataIntegrityException Occured in getOrderStatus ", e);
        }

        return cust_status;
    }

    /**
     * Method cancelOrder - Cancel the current order
     *
     *
     * @param order_number Order number
     *
     * @throws InvalidEntryException Exception thrown if order number is invalid
     * @throws OtherException
     *
     */
    public void cancelOrder(String order_number)
            throws InvalidEntryException, OtherException {

        int order_num;

            if (debugging)
                debug.println(3, "In cancelOrder method of OrderSesBean");

        try {
            order_num = Integer.parseInt(order_number);
        } catch (NumberFormatException e) {
            throw new InvalidEntryException(
                "Order number " + order_number
                + " is invalid. Please try again");
        }

        try {
            orderses.cancelOrder(order_num);
        } catch (RemoteException e) {
            throw new OtherException(
                " Remote Exception occured in cancelOrder ", e);
        }
    }

    /**
     * Method getCustomerStatus - Get the status of a customer
     *
     *
     * @param customer_id customer id
     *
     * @return StatusCustomer[]
     *
     * @throws InvalidEntryException Exception thrown if customer id is invalid
     * @throws OtherException
     *
     */
    public StatusCustomer[] getCustomerStatus(String customer_id)
            throws InvalidEntryException, OtherException {

        int              cust_id;
        StatusCustomer   cust_order_status[];
        CustomerStatus[] cust_array;

        try {
            cust_id = Integer.parseInt(customer_id);
        } catch (NumberFormatException e) {
            throw new InvalidEntryException(
                "Customer Entry " + customer_id
                + " is invalid. Please try again");
        }

        try {
            cust_array        = orderses.getCustomerStatus(cust_id);
            cust_order_status = new StatusCustomer[cust_array.length];

            for (int i = 0; i < cust_array.length; i++) {
                cust_order_status[i]            = new StatusCustomer();
                cust_order_status[i].order_num  =
                    String.valueOf(cust_array[i].orderId);
                cust_order_status[i].ship_date  =
                    String.valueOf(cust_array[i].shipDate);
                cust_order_status[i].cust_items =
                    new CustomerItem[cust_array[i].quantities.length];

                for (int j = 0; j < cust_array[i].quantities.length; j++) {
                    cust_order_status[i].cust_items[j] =
                        new CustomerItem(cust_array[i].quantities[j].itemId,
                                         cust_array[i].quantities[j]
                                             .itemQuantity);
                }
            }
        } catch (RemoteException e) {
            throw new OtherException(
                "Remote Exception occured in getCustomerStatus ", e);
        } catch (DataIntegrityException e) {
            throw new OtherException(
                "Remote Exception occured in getCustomerStatus ", e);
        }

        return cust_order_status;
    }

    // yella - Has to be removed when we use in j2ee webserver

    /**
     * Method getInitialContext
     *
     *
     * @return
     *
     * @throws Exception
     *
     */
    public Context getInitialContext() throws Exception {

        String     url = "http://localhost:1050";
        Properties p   = new Properties();

        p.put(Context.PROVIDER_URL, url);
        p.put(Context.SECURITY_PRINCIPAL, "guest");
        p.put(Context.SECURITY_CREDENTIALS, "guest123");

        return (new InitialContext(p));
    }
}

