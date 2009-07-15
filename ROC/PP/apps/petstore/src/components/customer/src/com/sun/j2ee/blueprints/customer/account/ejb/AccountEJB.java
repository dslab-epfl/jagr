/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

package com.sun.j2ee.blueprints.customer.account.ejb;

import java.util.Random;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.naming.InitialContext;

import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocal;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocalHome;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocal;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocalHome;

public abstract class AccountEJB implements javax.ejb.EntityBean {

    private EntityContext context = null;

    // counter for primary key
    //static private int idCounter = 0;
    private static Random rand = new Random();

    // primary key field
    public abstract Integer getId();
    public abstract void setId( Integer id );

    // getters and setters for CMP fields
    //====================================
    public abstract String getStatus();
    public abstract void setStatus(String status);

    // CMR fields
    //============
    public abstract ContactInfoLocal getContactInfo();
    public abstract void setContactInfo(ContactInfoLocal contactInfo);

    public abstract CreditCardLocal getCreditCard();
    public abstract void setCreditCard(CreditCardLocal creditCard);

    // EJB create method
    //===================
    public Object ejbCreate(String status, ContactInfoLocal contactInfo, CreditCardLocal creditCard) throws CreateException {
        setId( new Integer( rand.nextInt() ));
        setStatus(status);
        return null;
    }

    public Object ejbCreate(String status) throws CreateException {
        setId( new Integer( rand.nextInt() ));
        setStatus(status);
        return null;
    }

    public void ejbPostCreate(String status) throws CreateException {
        setStatus(status);
        try {
            InitialContext ic = new InitialContext();
            ContactInfoLocalHome cih = (ContactInfoLocalHome) ic.lookup("java:comp/env/ejb/ContactInfo");
            ContactInfoLocal contactInfo = cih.create();
            setContactInfo(contactInfo);
            CreditCardLocalHome cch = (CreditCardLocalHome) ic.lookup("java:comp/env/ejb/CreditCard");
            CreditCardLocal creditCard = cch.create();
            setCreditCard(creditCard);
        } catch (javax.naming.NamingException ne) {
            throw new CreateException("ContactInfoEJB error: naming exception looking up contact info or credit card");
        }

    }

    public void ejbPostCreate(String status, ContactInfoLocal contactInfo, CreditCardLocal creditCard) throws CreateException {
        setContactInfo(contactInfo);
        setCreditCard(creditCard);
    }


    // Misc Method
    //=============
    public void setEntityContext(EntityContext c) {
        context = c;
    }
    public void unsetEntityContext() {
        context = null;
    }
    public void ejbRemove() throws RemoveException { }
    public void ejbActivate() { }
    public void ejbPassivate() { }
    public void ejbStore() { }
    public void ejbLoad() { }
}
