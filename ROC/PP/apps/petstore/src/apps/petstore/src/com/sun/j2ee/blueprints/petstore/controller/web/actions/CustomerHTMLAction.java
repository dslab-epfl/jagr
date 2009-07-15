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
import java.util.Vector;
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

// customer imports
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfo;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCard;
import com.sun.j2ee.blueprints.address.ejb.Address;
import com.sun.j2ee.blueprints.customer.profile.ejb.ProfileInfo;

// petstore imports
import com.sun.j2ee.blueprints.petstore.util.PetstoreKeys;
import com.sun.j2ee.blueprints.petstore.controller.events.CustomerEvent;
import com.sun.j2ee.blueprints.petstore.controller.web.exceptions.MissingFormDataException;


// signon filter import for signed on key
import com.sun.j2ee.blueprints.signon.web.SignOnFilter;


/**
 * Implementation of CustomerHTMLAction that processes a
 * user changes/creation of a customer.
 *
 * Changes include:
 *    create customer
 *    update customer
 */
public final class CustomerHTMLAction extends HTMLActionSupport {

    private int eventType = -1;

    public Event perform(HttpServletRequest request)
        throws HTMLActionException {
        // Extract attributes we will need
        String actionType= (String)request.getParameter("action");

        CustomerEvent event = null;
        if (actionType == null) return null;

        if (actionType.equals("create")) {
            ContactInfo info = extractContactInfo(request, "_a");
            CreditCard creditCard = extractCreditCard(request);
            ProfileInfo profileInfo  = extractProfileInfo(request);
            event = new CustomerEvent(CustomerEvent.CREATE, info, profileInfo, creditCard);
            eventType = CustomerEvent.CREATE;
        } else if (actionType.equals("update")) {
            ContactInfo info = extractContactInfo(request, "_a");
            CreditCard creditCard = extractCreditCard(request);
            ProfileInfo profileInfo  = extractProfileInfo(request);
            event = new CustomerEvent(CustomerEvent.UPDATE, info, profileInfo, creditCard);
        }
        return event;
    }

    /* parse address form and generate a ProfileInfo object */
    private ProfileInfo extractProfileInfo(HttpServletRequest request) {
        ArrayList missingFields = null;
        String preferredLanguage =  request.getParameter("language").trim();
        if (preferredLanguage.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("Language");
        }
        String defaultFavoriteCategory =  request.getParameter("favorite_category").trim();
        if (defaultFavoriteCategory.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            missingFields.add("Favorite Category");
        }
        boolean defaultMyListPreference = true;
        String defaultMyListPreferenceString =  request.getParameter("mylist_on");
        if (defaultMyListPreferenceString == null) defaultMyListPreference=false;
        boolean defaultBannerPreference = true;
        String defaultBannerPreferenceString =  request.getParameter("banners_on");

        if (defaultBannerPreferenceString == null) defaultBannerPreference=false;
        return new ProfileInfo(preferredLanguage,
                                           defaultFavoriteCategory,
                                           defaultMyListPreference,
                                           defaultBannerPreference);
    }

    /* parse address form and generate a CreditCard object */
    private CreditCard extractCreditCard(HttpServletRequest request)
        throws HTMLActionException {
        ArrayList missingFields = null;
        String creditCardNumber =  request.getParameter("credit_card_number").trim();
        if (creditCardNumber.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            // this need to be internationalized
            missingFields.add("Credit Card");
        }
        String creditCardType =  request.getParameter("credit_card_type");
	creditCardType = (creditCardType==null)?(""):(creditCardType.trim());
        if (creditCardNumber.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            // this need to be internationalized
            missingFields.add("Credit Card Type");
        }
        String creditCardExpiryMonth =  request.getParameter("credit_card_expiry_month");
	creditCardExpiryMonth = (creditCardExpiryMonth==null)?(""):(creditCardExpiryMonth.trim());
        if (creditCardNumber.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            // this need to be internationalized
            missingFields.add("Credit Card Expiry Month");
        }
        String creditCardExpiryYear =  request.getParameter("credit_card_expiry_year");
        creditCardExpiryYear = (creditCardExpiryYear==null)?(""):(creditCardExpiryYear.trim());
        if (creditCardNumber.equals("")) {
            if (missingFields == null) {
                missingFields = new ArrayList();
            }
            // this need to be internationalized
            missingFields.add("Credit Card Expiry Month");
        }
        String expiryDate = creditCardExpiryMonth + "/" + creditCardExpiryYear;
        return new CreditCard(creditCardNumber,
                                            creditCardType,
                                            expiryDate);
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
        String stateOrProvince = request.getParameter("state_or_province" +suffix);
	stateOrProvince = (stateOrProvince==null)?(""):(stateOrProvince.trim());
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

    // load the customer here if we made it through the action processing
    public void doEnd(HttpServletRequest request, EventResponse eventResponse) {
        // put the signed on ok in the sesion if creating a customer
        if (eventType == CustomerEvent.CREATE) {
            // remove the SIGNED_ON_USER_SESSION_KEY
            if (request.getSession().getAttribute(SignOnFilter.SIGNED_ON_USER) != null) {
                request.getSession().removeAttribute(SignOnFilter.SIGNED_ON_USER);
            }
            request.getSession().setAttribute(SignOnFilter.SIGNED_ON_USER, new Boolean(true));
        }
        eventType = -1;
    }
}
