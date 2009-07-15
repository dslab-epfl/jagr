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
package com.sun.j2ee.blueprints.petstore.controller.web.actions;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ArrayList;

// j2ee imports
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

// waf imports
import com.sun.j2ee.blueprints.waf.event.Event;
import com.sun.j2ee.blueprints.waf.event.EventResponse;
import com.sun.j2ee.blueprints.waf.controller.web.action.HTMLActionSupport;
import com.sun.j2ee.blueprints.waf.controller.web.action.HTMLActionException;

// address, contactinfo, creditcard component imports
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfo;
import com.sun.j2ee.blueprints.address.ejb.Address;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCard;

// petstore imports
import com.sun.j2ee.blueprints.petstore.util.PetstoreKeys;
import com.sun.j2ee.blueprints.petstore.controller.events.OrderEvent;
import com.sun.j2ee.blueprints.petstore.controller.web.exceptions.MissingFormDataException;

/**
 * Implementation of OrderHTMLAction that processes a
 * user change in the order.
 *
 */

public final class OrderHTMLAction extends HTMLActionSupport {


    public Event perform(HttpServletRequest request)
        throws HTMLActionException {
        // Extract attributes we will need
        // XXXX this needs to be part of the form
        ContactInfo shipper = extractContactInfo(request, "_a");
        ContactInfo receiver = extractContactInfo(request, "_b");
        CreditCard creditCard = new CreditCard( "1234-2334", "Duke Express", "10/2001") ;
        return new OrderEvent(shipper,receiver,creditCard);
    }


    /* parse address form and generate a ContactInfo object */
    private ContactInfo extractContactInfo(HttpServletRequest request, String suffix)
        throws HTMLActionException {
        ArrayList missingFields = null;
        String familyName =  request.getParameter("family_name" +suffix).trim();
        if (familyName.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("Last Name");
        }
        String givenName =  request.getParameter("given_name" +suffix).trim();
        if (givenName.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("First Name");
        }
        String address1 = request.getParameter("address_1" +suffix).trim();
        if (address1.equals("")){
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("Street Address");
        }
        String address2 = request.getParameter("address_2" +suffix).trim();
        if (address2.equals("")) {
          address2 = null;
        }
        String city =   request.getParameter("city"  +suffix).trim();
        if (city.equals("")){
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("City");
        }
        String stateOrProvince = request.getParameter("state_or_province" +suffix).trim();
        if (stateOrProvince.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("State or Province" +suffix);
        }
        String postalCode = request.getParameter("postal_code" +suffix).trim();
        if (postalCode.equals("")){
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("Postal Code");
        }

        String country = request.getParameter("country" + suffix).trim();
        String telephone = request.getParameter("telephone_number" +suffix).trim();
        if (telephone.equals("")){
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("Telephone Number");
        }
        String email = null;
        if (request.getParameter("email"  +suffix) != null) {
            email = request.getParameter("email" +suffix).trim();
        }
        if (missingFields != null) {
            MissingFormDataException ex = new MissingFormDataException("Missing Address Data", missingFields);
            request.setAttribute(PetstoreKeys.MISSING_FORM_DATA_EXCEPTION_KEY, ex);
            return null;
        }

        Address address = new Address(address1, address2, city, stateOrProvince, postalCode, country);
        return new ContactInfo(familyName, givenName, address, email, telephone);
    }

        // put the customer response in the request so it can be accessed by the order page
    public void doEnd(HttpServletRequest request, EventResponse eventResponse) {
        request.setAttribute(PetstoreKeys.ORDER_RESPONSE, eventResponse);
    }
}

