
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CustomerCmpEJB.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
 *
 */
package com.sun.ecperf.corp.customerent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.Date;

import java.io.ByteArrayInputStream;

import com.sun.ecperf.ruleengine.*;
import com.sun.ecperf.corp.ruleent.ejb.*;
import com.sun.ecperf.corp.discountent.ejb.*;
import com.sun.ecperf.common.*;


/**
 * This class implements the Customer entity Bean
 */
public class CustomerCmpEJB implements EntityBean {

    public Integer            customerId;
    public Date               since;
    public double             balance;
    public String             credit;
    public double             creditLimit;
    public double             ytdPayment;
    public double             dealAmount;
    protected EntityContext   entityContext;
    protected Debug           debug;
    protected boolean         debugging;
    protected RuleEnt         rule;
    protected DiscountEntHome discountHome;
    protected RuleParser      parser;

    /**
     *
     * This method will never be called as part of the ECperf workload
     * so we don't really do much here.
     */
    public Integer ejbCreate(CustomerInfo info)
            throws RemoteException, CreateException {

        if (debugging)
            debug.println(3, "ejbCreate ");

        this.customerId  = info.customerId;
        this.since       = info.since;
        this.balance     = info.balance;
        this.credit      = info.credit;
        this.creditLimit = info.creditLimit;
        this.ytdPayment  = info.YtdPayment;

        return customerId;
    }

    /**
     * Method hasSufficientCredit
     *
     *
     * @param amount
     *
     * @return
     *
     */
    public boolean hasSufficientCredit(double amount) {

        if(debugging)
            debug.println(3, "hasSufficientCredit for " + amount);

        if (credit.equals("BC")) {
            return false;
        }

        if (creditLimit >= amount) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method getPercentDiscount
     *
     *
     * @param amount
     *
     * @return
     *
     * @throws DataIntegrityException
     *
     */
    public double getPercentDiscount(double amount)
            throws DataIntegrityException {

        if (debugging)
            debug.println(3, "getPercentDiscount for " + amount);

        // The rule engine cannot deal with auto-variables. So
        // we have to set an instance variable for it to deal with.
        dealAmount = amount;

        double percentDiscount = 0;
        String discountCat     = "";

        try {
            parser.ReInit(new ByteArrayInputStream(rule.getBytes()));

            discountCat     = parser.evaluate();
            percentDiscount =
                discountHome.findByPrimaryKey(discountCat).getPercent();

            if (debugging)
                debug.println(3, "Discount category: " + discountCat
                              + " , Discount: " + percentDiscount + "%");
        } catch (FinderException fe) {
            if (debugging)
                debug.println(1, "Cannot find dicount category " +
                              discountCat + " : " + fe.getMessage());
            debug.printStackTrace(fe);

            throw new DataIntegrityException(fe,
                                             "Cannot find dicount category "
                                             + discountCat);
        } catch (RemoteException re) {
            if (debugging)
                debug.println(1, re.getMessage());
            debug.printStackTrace(re);

            throw new EJBException(re);
        }

        dealAmount = 0;

        return percentDiscount;
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
     */
    public void ejbRemove() {}

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

        this.entityContext = entityContext;

        InitialContext initCtx    = null;
        int            debugLevel = 0;

        try {
            initCtx = new InitialContext();
        } catch (NamingException ne) {
            throw new EJBException("Cannot construct InitialContext!");
        }

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
        } catch (NamingException ne) {
            System.out.println("CustomerCmpEJB: debuglevel Property not set. "
                               + "Turning off debug messages");

            debug = new Debug();
        }

        try {
            RuleEntHome ruleHome = (RuleEntHome) javax.rmi
                .PortableRemoteObject
                .narrow(initCtx
                    .lookup("java:comp/env/ejb/RuleEnt"), RuleEntHome.class);

            rule         = ruleHome.findByPrimaryKey("discount");
            discountHome =
                (DiscountEntHome) javax.rmi.PortableRemoteObject.narrow(
                    initCtx.lookup("java:comp/env/ejb/DiscountEnt"),
                    DiscountEntHome.class);
        } catch (NamingException ne) {
            throw new EJBException(ne);
        } catch (FinderException fe) {
            throw new EJBException(fe);
        } catch (RemoteException re) {
            throw new EJBException(re);
        }

        parser = new RuleParser(this, new ByteArrayInputStream(new byte[1]),
                                debugLevel);
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {}
}

