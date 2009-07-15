
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderCustomerCmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.orders.ordercustomerent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.util.sequenceses.ejb.*;
import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.corp.customerent.ejb.*;
import com.sun.ecperf.common.*;


/**
 * This class implements the OrderCustomer entity bean. It is
 * responsible for performing operations on the customer table
 * in the Orders database.
 */
public class OrderCustomerCmpEJB implements EntityBean {

    public Integer                 customerId;
    public String                  firstName, lastName, contact;
    public Address                 address = new Address();
    public String                  street1;
    public String                  street2;
    public String                  city;
    public String                  state;
    public String                  country;
    public String                  zip;
    public String                  phone;
    public java.sql.Date           customerSince;
    protected EntityContext        entityContext;
    protected SequenceSesHome      sequenceHome;
    protected OrderCustomerEntHome customerHome;
    protected CustomerEntHome      corpCustHome;
    protected Debug                debug;
    protected boolean              debugging;

    /**
     * The ejbCreate method gets called when a new customer needs to
     * be created. This method first generates a new unique customer id.
     * It then validates the various fields passed (done solely to
     * introduce some business logic in the bean).
     * Finally, it adds the new customer record
     * @param info - CustomerInfo object
     * @return customer id
     * @exception InvalidInfoException if CustomerInfo
     *             fails validation checks
     * @exception CreateException if the create fails
     * @exception InvalidInfoException if the customer info passed is invalid
     * @exception DataIntegrityException if the entry for 'customer' doesn't exist in U_sequences
     */
    public Integer ejbCreate(CustomerInfo info)
            throws InvalidInfoException, DataIntegrityException, CreateException {

        if(debugging)
            debug.println(3, "ejbCreate ");

        try {
            SequenceSes sequence = sequenceHome.create();

            this.customerId = new Integer(sequence.nextKey("customer"));
        } catch (CreateException ce) {
            debug.printStackTrace(ce);
            throw new EJBException(ce);
        } catch (ObjectNotFoundException oe) {
            if(debugging)
                debug.println(1, "'customer' object not found in sequences table");
            debug.printStackTrace(oe);
            throw new DataIntegrityException(oe);
        } catch (FinderException fe) {
            debug.printStackTrace(fe);
            throw new EJBException(fe);
        } catch (RemoteException re) {
            debug.printStackTrace(re);
            throw new EJBException(re);
        }

        // Needed to create Corp A/C
        info.customerId    = this.customerId;
        this.firstName     = info.firstName;
        this.lastName      = info.lastName;
        this.address       = info.address;
        this.street1       = this.address.street1;
        this.street2       = this.address.street2;
        this.city          = this.address.city;
        this.state         = this.address.state;
        this.country       = this.address.country;
        this.zip           = this.address.zip;
        this.phone         = this.address.phone;
        this.contact       = info.contact;
        this.customerSince = info.since;

        if (!isStringValid(firstName)) {
            throw new InvalidInfoException("Invalid firstName '" + firstName
                                           + "'");
        }

        if (!isStringValid(lastName)) {
            throw new InvalidInfoException("Invalid lastName '" + lastName
                                           + "'");
        }

        if (!isStringValid(contact)) {
            throw new InvalidInfoException("Invalid contact '" + contact
                                           + "'");
        }

        address.validate();

        // Create the Corporate A/c for this customer
        try {
            corpCustHome.create(info);
        } catch (RemoteException re) {
            debug.printStackTrace(re);
            throw new EJBException(re);
        }

        return null;
    }

    /**
     * This method is called by newOrder() in the OrderBean to
     * compute the customer discount based on discount rules
     * that are yet to be defined.
     */
    public double getPercentDiscount(double totalAmount)
            throws DataIntegrityException {

        double percentDiscount = 0;

        try {
            CustomerEnt customer = corpCustHome.findByPrimaryKey(customerId);

            percentDiscount = customer.getPercentDiscount(totalAmount);
        } catch (FinderException fe) {
            throw new DataIntegrityException(fe,
                                             "Customer " + customerId
                                             + " record not found!");
        } catch (RemoteException re) {
            throw new EJBException(re);
        }

        return percentDiscount;
    }

    protected boolean isStringValid(String str) {

        char ch;

        // Check if String has only alphanumeric characters
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);

            if (((ch >= '0') && (ch <= '9')) || ((ch >= 'A') && (ch <= 'Z'))
                    || ((ch >= 'a') && (ch <= 'z'))) {
                continue;
            } else {
                return (false);
            }
        }

        return (true);
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param info
     *
     */
    public void ejbPostCreate(CustomerInfo info) {}

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {}

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
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {}

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {}

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
            System.out.println("OrderCustomerCmpEJB: debuglevel Property "
                               + "not set. Turning off debug messages");

            debug = new Debug();
        }

        this.entityContext = entityContext;

        // we need to get some home objects
        // customer home is easy, because it is my own home!
        customerHome =
            (OrderCustomerEntHome) javax.rmi.PortableRemoteObject
                .narrow(entityContext
                    .getEJBHome(), OrderCustomerEntHome.class);

        try {

            // the other homes are available via EJB links
            sequenceHome =
                (SequenceSesHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SequenceSes"),
                    SequenceSesHome.class);

            if(debugging)
                debug.println(3, "found SequenceSesHome interface");

            corpCustHome =
                (CustomerEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/CustomerEnt"),
                    CustomerEntHome.class);

            if(debugging)
                debug.println(3, "found CustomerEntHome interface");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up home " + e);
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {}
}

