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

package com.sun.j2ee.blueprints.petstore.controller.events;

import com.sun.j2ee.blueprints.waf.event.EventSupport;

// customer component imports
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfo;
import com.sun.j2ee.blueprints.customer.profile.ejb.ProfileInfo;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCard;

/**
 * This Event  contains the information for the  EJBController of a change in Locale
 * to start the order process.
 */
public class CustomerEvent extends EventSupport {

    public static final int UPDATE = 1;
    public static final int CREATE = 2;

    private ContactInfo info;
    private CreditCard creditCard;
    private ProfileInfo profileInfo;
    private int actionType = -1;

    public CustomerEvent(int actionType,
                                       ContactInfo info,
                                       ProfileInfo profileInfo,
                                       CreditCard creditCard) {
        this.info   = info;
        this.profileInfo = profileInfo;
        this.creditCard = creditCard;
        this.actionType = actionType;
    }

    public ContactInfo getContactInfo() {
       return info;
    }

    public ProfileInfo getProfileInfo() {
       return profileInfo;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public int getActionType() {
        return actionType;
    }

    public String toString() {
        return "CustomerEvent[ info=" + info + ", actionType=" + actionType +  "]";
    }

    public String getEventName() {
        return "CustomerEvent";
    }

}

