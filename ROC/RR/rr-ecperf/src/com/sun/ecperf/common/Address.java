
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Address.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.common;


import java.io.*;


/**
 * This class describes the Address fields used in the various tables
 *
 * @author Shanti Subramanyam
 */
public class Address implements Serializable {

    public String street1;
    public String street2;
    public String city;
    public String state;
    public String country;
    public String zip;
    public String phone;

    /**
     * Constructor Address
     *
     *
     */
    public Address() {}

    /**
     * Constructor Address
     *
     *
     * @param street1
     * @param street2
     * @param city
     * @param state
     * @param country
     * @param zip
     * @param phone
     *
     */
    public Address(String street1, String street2, String city, String state,
                   String country, String zip, String phone) {

        this.street1 = street1;
        this.street2 = street2;
        this.city    = city;
        this.state   = state;
        this.country = country;
        this.zip     = zip;
        this.phone   = phone;
    }

    /**
     * Method validate
     *
     *
     * @throws InvalidInfoException
     *
     */
    public void validate() throws InvalidInfoException {

        int i;

        // Check if zip and phone are numeric
        for (i = 0; i < zip.length(); i++) {
            if ((zip.charAt(i) < '0') || (zip.charAt(i) > '9')) {
                throw new InvalidInfoException("Invalid zip in address: "
                                               + zip);
            }
        }

        for (i = 0; i < phone.length(); i++) {
            if ((phone.charAt(i) < '0') || (phone.charAt(i) > '9')) {
                throw new InvalidInfoException("Invalid phone in address: "
                                               + phone);
            }
        }
    }
}

