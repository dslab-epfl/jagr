
/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: Address.java,v 1.1.1.1 2002/11/16 05:35:26 emrek Exp $
 *
 */
package com.sun.ecperf.load;


/**
 * Class Address
 *
 *
 * @author
 * @version %I%, %G%
 */
public class Address {

    String  street1, street2, city, state, country, zip, phone;
    RandNum r = new RandNum();

    /**
     * Constructor Address
     *
     *
     */
    public Address() {

        street1 = r.makeAString(10, 20);
        street2 = r.makeAString(10, 20);
        city    = r.makeAString(10, 20);
        state   = r.makeAString(2, 2);
        country = r.makeAString(3, 10);
        zip     = r.makeNString(4, 4);
        zip     = zip + "11111";
        phone   = r.makeNString(12, 16);
    }
}

