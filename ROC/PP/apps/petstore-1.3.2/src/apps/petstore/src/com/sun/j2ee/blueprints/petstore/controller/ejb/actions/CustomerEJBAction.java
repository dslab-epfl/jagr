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


package com.sun.j2ee.blueprints.petstore.controller.ejb.actions;


import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

// j2ee
import javax.ejb.FinderException;

// waf imports
import com.sun.j2ee.blueprints.waf.event.Event;
import com.sun.j2ee.blueprints.waf.event.EventResponse;
import com.sun.j2ee.blueprints.waf.event.EventException;
import com.sun.j2ee.blueprints.waf.controller.ejb.action.EJBActionSupport;
import com.sun.j2ee.blueprints.waf.util.I18nUtil;

// customer imports
import com.sun.j2ee.blueprints.customer.ejb.CustomerLocal;
import com.sun.j2ee.blueprints.customer.ejb.CustomerLocalHome;
import com.sun.j2ee.blueprints.customer.account.ejb.AccountLocal;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocal;
import com.sun.j2ee.blueprints.customer.profile.ejb.ProfileLocal;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocal;
import com.sun.j2ee.blueprints.address.ejb.AddressLocal;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCard;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfo;
import com.sun.j2ee.blueprints.customer.profile.ejb.ProfileInfo;
import com.sun.j2ee.blueprints.address.ejb.Address;

// petstore imports
import com.sun.j2ee.blueprints.petstore.controller.events.CustomerEvent;
import com.sun.j2ee.blueprints.petstore.util.PetstoreKeys;

// petstore imports
import com.sun.j2ee.blueprints.petstore.controller.ejb.ShoppingClientFacadeLocal;

public class CustomerEJBAction extends EJBActionSupport {

    private ShoppingClientFacadeLocal scf = null;

    public EventResponse perform(Event e) throws EventException {
        CustomerEvent ce = (CustomerEvent)e;
        scf = (ShoppingClientFacadeLocal)machine.getAttribute(PetstoreKeys.SHOPPING_CLIENT_FACADE);
        switch (ce.getActionType()) {
            case CustomerEvent.CREATE : {
              String userId = scf.getUserId();
              scf.createCustomer(userId);
              updateCustomer(ce);
              break;
            }
            case CustomerEvent.UPDATE : {
              updateCustomer(ce);
              break;
            }
        }
        return null;
    }
    /*
     *  Look up the customer and do fine grain sets on the account.
     */

    private void updateCustomer(CustomerEvent ce) throws EventException {
          //user id was set by the CreaterUserEJBAction
          CustomerLocal customer = null;
          try {
              customer = scf.getCustomer();
          } catch (FinderException fe) {

          }

          AccountLocal account = customer.getAccount();
          // deep copy of Contact info to Local EJBs
          ContactInfoLocal contactInfoLocal = account.getContactInfo();

          ContactInfo contactInfo = ce.getContactInfo();

          contactInfoLocal.setFamilyName(contactInfo.getFamilyName());
          contactInfoLocal.setGivenName(contactInfo.getGivenName());
          contactInfoLocal.setTelephone(contactInfo.getPhone());
          contactInfoLocal.setEmail(contactInfo.getEmail());
          // deep copy over the address
          Address address = contactInfo.getAddress();
          AddressLocal addressLocal = contactInfoLocal.getAddress();
          addressLocal.setStreetName1(address.getStreetName1());
          addressLocal.setStreetName2(address.getStreetName2());
          addressLocal.setCity(address.getCity());
          addressLocal.setZipCode(address.getZipCode());
          addressLocal.setState(address.getState());
          addressLocal.setCountry(address.getCountry());
          // deep copy the credit card
          CreditCard creditCard = ce.getCreditCard();
          CreditCardLocal creditCardLocal = account.getCreditCard();
          creditCardLocal.setCardNumber(creditCard.getCardNumber());
          creditCardLocal.setCardType(creditCard.getCardType());
          creditCardLocal.setExpiryDate(creditCard.getExpiryDate());
          // deep copy the profile
          ProfileInfo profileInfo = ce.getProfileInfo();
          ProfileLocal profileLocal = customer.getProfile();
          profileLocal.setPreferredLanguage(profileInfo.getPreferredLanguage());
          profileLocal.setFavoriteCategory(profileInfo.getFavoriteCategory());
          profileLocal.setMyListPreference(profileInfo.getMyListPreference());
          profileLocal.setBannerPreference(profileInfo.getBannerPreference());
          // set the current locale to the choose locale
          Locale locale = I18nUtil.getLocaleFromString(profileInfo.getPreferredLanguage());
          machine.setAttribute("locale", locale);
    }
}

