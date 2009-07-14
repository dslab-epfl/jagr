
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderCmpEJB.java,v 1.1.1.1 2002/11/16 05:35:28 emrek Exp $
 *
 *
 */
package com.sun.ecperf.orders.orderent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.util.sequenceses.ejb.*;
import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.corp.customerent.ejb.*;
import com.sun.ecperf.orders.itement.ejb.*;
import com.sun.ecperf.mfg.largeorderses.ejb.*;
import com.sun.ecperf.orders.orderlineent.ejb.*;
import com.sun.ecperf.common.*;


/**
 * This class implements the Order entity bean. It is
 * responsible for performing all the transactions in the Orders
 * application. It also manages the orderlines that are part of the
 * order.
 */
public class OrderCmpEJB implements EntityBean {

    public Integer              id;
    public int                  customerId;
    public int                  orderLineCount;
    public double               discount;
    public double               total;
    public java.sql.Timestamp   entryDate;
    public java.sql.Date        shipDate;
    public int                  orderStatus;
    protected EntityContext     entityContext;
    protected SequenceSesHome   sequenceHome;
    protected CustomerEntHome   customerHome;
    protected CustomerEnt       customer = null;
    protected LargeOrderSesHome largeOrderHome;
    protected ItemEntHome       itemHome;
    protected OrderEntHome      orderHome;
    protected OrderLineEntHome  orderLineHome;
    protected Debug             debug;
    protected boolean           debugging;

    /**
     * The ejbCreate method gets called when a new order needs to
     * be created. This method first generates a new unique order id.
     * It then checks the customer's credit by making a call to the
     * customer bean in the Corp domain. If the customer has sufficient
     * credit, the orderlines that make up this order are created.
     * @param customerId - id of customer creating the order
     * @param quantities - item,qty pairs for orderlines
     * @return order id
     * @exception InsufficientCreditException if customer
     *             doesn't have sufficient credit for the order total - discount
     * @exception CreateException if the create fails
     */
    public Integer ejbCreate(int customerId, ItemQuantity[] quantities)
            throws InsufficientCreditException, CreateException {

        if(debugging)
            debug.println(3, "ejbCreate ");

        try {
            SequenceSes sequence = sequenceHome.create();

            this.id = new Integer(sequence.nextKey("order"));
	    // For Atomicity 1, 2, 3
	    if (debugging)
		debug.println(4, "Atomicity Test (1,2,3): Order Id: " + this.id);

        } catch (CreateException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (FinderException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        this.customerId     = customerId;
        this.orderLineCount = quantities.length;
	// For Atomicity Test 3
	if (debugging)
	    debug.println(4, "Atomicity Test 3: OrderLineId: " + orderLineCount);

        this.total          = 0d;
        this.entryDate      =
            new java.sql.Timestamp(new java.util.Date().getTime());
        this.shipDate       = null;
        this.orderStatus    = 1;
        customer            = null;

        try {

            // now we have to add the order lines, and compute the total
            for (int i = 0; i < quantities.length; i++) {
                String itemId            = quantities[i].itemId;
                int    itemQuantity      = quantities[i].itemQuantity;
                double priceWithDiscount = getPriceWithDiscount(itemId);

                orderLineHome.create(i + 1, id.intValue(), itemId,
                                     itemQuantity, null);

                total += priceWithDiscount * itemQuantity;
            }

            total    = Util.round(total, 2);
            discount = getPercentDiscount(total);

            checkCustomerCredit();
            checkForLargeOrders(quantities);

            return null;    // As per spec, the CMP ejbCreate should return null.
        } catch (DataIntegrityException e) {
            if(debugging)
                debug.println(1, "Failed to create bean " + e);
            debug.printStackTrace(e);

            throw new CreateException(e.getMessage());
        } catch (RemoteException e) {
            if(debugging)
                debug.println(1, "Remote Exception " + e);
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param customerId
     * @param quantities
     *
     */
    public void ejbPostCreate(int customerId, ItemQuantity[] quantities) {}

    /**
     * change: Change the quantities of orderlines
     *
     * @param   quantities  array of Items & Quantities to be changed
     *
     * @throws DataIntegrityException
     * @exception       InsufficientCreditException if the customer
     *                  has insufficient credit for the changes
     */
    public void change(ItemQuantity[] quantities)
            throws InsufficientCreditException, DataIntegrityException {

        if(debugging)
            debug.println(3, "change ");

        for (int i = 0; i < quantities.length; i++) {
            String itemId            = quantities[i].itemId;
            int    itemQuantity      = quantities[i].itemQuantity;
            double priceWithDiscount = 0;

            priceWithDiscount = getPriceWithDiscount(itemId);

            try {
                OrderLineEnt orderLine =
                    orderLineHome.findByOrderAndItem(id.intValue(), itemId);

                // reduce the price by the old order quantity
                total -= priceWithDiscount * orderLine.getQuantity();

                // increase the amount by the new order quantity
                total += priceWithDiscount * itemQuantity;

                // If new qty is 0, delete orderline
                if (itemQuantity == 0) {
                    try {
                        orderLine.remove();

                        orderLineCount--;
                    } catch (RemoveException re) {
                        throw new EJBException(re);
                    } catch (RemoteException re) {
                        throw new EJBException(re);
                    }
                } else {
                    orderLine.setQuantity(itemQuantity);
                }
            } catch (FinderException fe) {
                // If new qty is 0, don't create orderline
                if (itemQuantity != 0) {
                    // this is a new order line
                    // first we increment the number of order lines
                    orderLineCount++;

                    // then we increment the price
                    total += priceWithDiscount * itemQuantity;

                    try {    // finally insert the new order line
                        orderLineHome.create(orderLineCount, id.intValue(),
                                             itemId, itemQuantity, null);
                    } catch (CreateException ce) {
                        throw new EJBException(ce);
                    } catch (RemoteException re) {
                        throw new EJBException(re);
                    }
                }
            } catch (RemoteException re) {
                throw new EJBException(re);
            }
        }

        total    = Util.round(total, 2);
        discount = getPercentDiscount(total);

        checkCustomerCredit();
    }

    /**
     * Method getStatus
     *
     *
     * @return
     *
     * @throws DataIntegrityException
     *
     */
    public OrderStatus getStatus() throws DataIntegrityException {

        OrderStatus status = new OrderStatus();

        status.customerId = customerId;
        status.shipDate   = shipDate;
        status.quantities = new ItemQuantity[orderLineCount];

        int i = 0;

        if(debugging)
            debug.println(3, "getStatus");

        try {
            Enumeration orderLines = orderLineHome.findByOrder(id.intValue());

            if (orderLineCount == 0) {
                if(debugging)
                    debug.println(2, "orderLineCount = 0 ");
            }

            for (i = 0; i < orderLineCount; i++) {
                if (!orderLines.hasMoreElements()) {
                    if(debugging)
                        debug.println(2, "Too few order lines for Order " + id
                                      + " Expecting " + orderLineCount 
                                      + ", got "  + i);

                    throw new DataIntegrityException(
                        "Too few order lines for Order " + id + " Expecting "
                        + orderLineCount + ", got " + i);
                }

                OrderLineEnt orderLine =
                    (OrderLineEnt) javax.rmi.PortableRemoteObject
                        .narrow(orderLines.nextElement(), OrderLineEnt.class);

                status.quantities[i] =
                    new ItemQuantity(orderLine.getItemId(),
                                     orderLine.getQuantity());
            }

            if (orderLines.hasMoreElements()) {
                while(orderLines.hasMoreElements()) {
                    orderLines.nextElement();
                    i++;
                }
                if(debugging)
                    debug.println(2, "Too many order lines for Order " + id
                                  + " Expecting " + orderLineCount + ", got "
                                  + i);

                throw new DataIntegrityException("Too many order lines for Order "
                                                 + id + " Expecting "
                                                 + orderLineCount + ", got "
                                                 + i);
            }
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (RemoteException re) {
            throw new EJBException(re);
        }

        return status;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {

        if(debugging)
            debug.println(3, "ejbRemove ");

        try {

            // remove all the order lines associated with this order
            Enumeration orderLines = orderLineHome.findByOrder(id.intValue());

            while (orderLines.hasMoreElements()) {
                OrderLineEnt orderLine =
                    (OrderLineEnt) javax.rmi.PortableRemoteObject
                        .narrow(orderLines.nextElement(), OrderLineEnt.class);

                orderLine.remove();
            }
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (RemoteException re) {
            throw new EJBException(re);
        }
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {

        if(debugging)
            debug.println(3, "ejbActivate ");

        customer = null;
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if(debugging)
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if(debugging)
            debug.println(3, "ejbLoad ");
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if(debugging)
            debug.println(3, "ejbStore ");
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        Context context = null;

        try {
            context = new InitialContext();
        } catch (NamingException e) {
            System.out.println("Exception in setEntityContext" + e);
            e.printStackTrace(System.err);

            throw new EJBException(e);
        }

        try {
            int debugLevel =
                ((Integer) context.lookup("java:comp/env/debuglevel"))
                    .intValue();

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch (NamingException ne) {
            System.out.println("OrderCmpEJB: debuglevel Property "
                               + "not set. Turning off debug messages");

            debug = new Debug();
        }

        this.entityContext = entityContext;
        orderHome          =
            (OrderEntHome) javax.rmi.PortableRemoteObject
                .narrow(entityContext.getEJBHome(), OrderEntHome.class);

        try {

            // the other homes are available via EJB links
            // TOM D: Note these references will change when the Customer and
            // manufacturing domains are updated to the new naming convention.
            sequenceHome =
                (SequenceSesHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SequenceSes"),
                    SequenceSesHome.class);

            if(debugging)
                debug.println(3, "found SequenceSesHome interface");

            customerHome =
                (CustomerEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/CustomerEnt"),
                    CustomerEntHome.class);

            if(debugging)
                debug.println(3, "found CustomerEntHome interface");

            largeOrderHome =
                (LargeOrderSesHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/LargeOrderSes"),
                    LargeOrderSesHome.class);

            if(debugging)
                debug.println(3, "found LargeOrderSesHome interface");

            itemHome = (ItemEntHome) javax.rmi.PortableRemoteObject
                .narrow(context
                    .lookup("java:comp/env/ejb/ItemEnt"), ItemEntHome.class);

            if(debugging)
                debug.println(3, "found ItemEntHome interface");

            orderLineHome =
                (OrderLineEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/OrderLineEnt"),
                    OrderLineEntHome.class);

            if(debugging)
                debug.println(3, "found OrderLineEntHome interface");
        } catch (NamingException e) {
            debug.println(1, "Naming Exception " + e);
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up home " + e);
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        if(debugging)
            debug.println(3, "unsetEntityContext ");
    }

    protected double getPriceWithDiscount(String itemId) {

        if(debugging)
            debug.println(3, "getPriceWithDiscount ");

        try {
            ItemEnt item     = itemHome.findByPrimaryKey(itemId);
            double  price    = item.getPrice();
            float   discount = item.getDiscount();

            return (1 - discount) * price;
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (RemoteException e) {
            throw new EJBException(e);
        }
    }

    protected void findCustomer() throws DataIntegrityException {

        if (customer == null) {
            try {
                if(debugging)
                    debug.println(3, "findCustomer");

                customer =
                    customerHome.findByPrimaryKey(new Integer(customerId));
            } catch (ObjectNotFoundException e) {
                debug.println(1, "Customer not found for ID  " + customerId);
                debug.printStackTrace(e);

                throw new DataIntegrityException(
                    e, "Customer not found for ID " + customerId);
            } catch (FinderException e) {
                debug.println(1, "findCustomer() failed " + e.getMessage());
                debug.printStackTrace(e);

                throw new EJBException(e);
            } catch (RemoteException e) {
                debug.println(1, "findCustomer() failed " + e.getMessage());
                debug.printStackTrace(e);

                throw new EJBException(e);
            }
        }
    }

    protected void checkCustomerCredit()
            throws InsufficientCreditException, DataIntegrityException {

        if(debugging)
            debug.println(3, "checkCustomerCredit ");
        findCustomer();

        try {
            if (!customer.hasSufficientCredit(Util.round(total
                    * (1 - discount), 2))) {
                entityContext.setRollbackOnly();

                throw new InsufficientCreditException("Not enough credit for "
                                                      + total);
            }
        } catch (RemoteException e) {
            debug.println(1, "findCustomer() failed " + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    protected double getPercentDiscount(double amount)
            throws DataIntegrityException {

        if(debugging)
            debug.println(3, "getPercentDiscount");

        double percentDiscount = 0;

        findCustomer();

        try {
            percentDiscount = customer.getPercentDiscount(amount);
        } catch (RemoteException e) {
            debug.println(1, "findCustomer() failed " + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        return percentDiscount;
    }

    /**
     * This method checks for large orderlines and creates a large order for
     * these items.
     * A item quantity of >= 20 is considered a large order, since the largest
     * qty for a regular order is 20.
     */
    protected void checkForLargeOrders(ItemQuantity[] quantities) {

        LargeOrderSes largeorder;
        String        itemId = null;

        if(debugging)
            debug.println(3, "checkForLargeOrders ");

        //Check for large orders
        try {
            for (int i = 0; i < quantities.length; i++) {
                short itemQuantity = (short) quantities[i].itemQuantity;

                if (itemQuantity > 20) {
                    itemId = quantities[i].itemId;

                    java.util.Date cur_date  = new java.util.Date();
                    java.sql.Date  test_date =
                        new java.sql.Date(cur_date.getTime());

                    largeorder = largeOrderHome.create();

                    largeorder.createLargeOrder(id.intValue(), i + 1, itemId,
                                                itemQuantity, test_date);
                    largeorder.remove();
                }
            }
        } catch (CreateException e) {
            debug.println(1, "CreateException in create large order for "
                          + id);
            debug.println(1, e.getMessage());

            throw new EJBException("Failure to create large order for order "
                                   + id + e);
        } catch (RemoveException re) {
            debug.println(1, "RemoveException in remove large order for "
                          + id);
            debug.println(1, re.getMessage());

            throw new EJBException("Failure to remove large order for order "
                                   + id + re);
        } catch (RemoteException rme) {
            debug.println(1, "RemoteException in createLargeOrder for item "
                          + itemId + " in order " + id);

            throw new EJBException(rme);
        }
    }
}

