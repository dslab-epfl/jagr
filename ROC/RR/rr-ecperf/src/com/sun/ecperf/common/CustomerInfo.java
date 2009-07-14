
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CustomerInfo.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.common;


import java.io.*;


/**
 * This class is the CustomerInfo object passed to the addCustomer
 * method of the OrderCustomer beans
 *
 * @author Shanti Subramanyam
 */
public class CustomerInfo implements Serializable {

    /**
     * Constructor CustomerInfo
     *
     *
     * @param first
     * @param last
     * @param address
     * @param contact
     * @param credit
     * @param creditLimit
     * @param balance
     * @param YtdPayment
     *
     */
    public CustomerInfo(String first, String last, Address address,
                        String contact, String credit, double creditLimit,
                        double balance, double YtdPayment) {

        this.firstName   = first;
        this.lastName    = last;
        this.address     = address;
        this.contact     = contact;
        this.since       = new java.sql.Date(new java.util.Date().getTime());
        this.credit      = credit;
        this.creditLimit = creditLimit;
        this.balance     = balance;
        this.YtdPayment  = YtdPayment;
    }

    public Integer       customerId;
    public String        firstName;
    public String        lastName;
    public Address       address;
    public String        contact;
    public String        credit;
    public double        creditLimit;
    public java.sql.Date since;
    public double        balance;
    public double        YtdPayment;
}

