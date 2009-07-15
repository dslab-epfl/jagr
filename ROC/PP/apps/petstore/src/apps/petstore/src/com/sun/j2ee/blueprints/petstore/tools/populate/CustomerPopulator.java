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

package com.sun.j2ee.blueprints.petstore.tools.populate;

import javax.naming.*;
import javax.ejb.*;
import java.util.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.sun.j2ee.blueprints.customer.ejb.*;
import com.sun.j2ee.blueprints.customer.account.ejb.*;
import com.sun.j2ee.blueprints.customer.profile.ejb.*;


public class CustomerPopulator {
  public static final String JNDI_CUSTOMER_HOME = "java:comp/env/ejb/Customer";
  public static final String XML_CUSTOMERS = "Customers";
  private static final String XML_CUSTOMER = "Customer";
  private static final String XML_ID = "Customer/@id";
  private String rootTag;
  private CustomerLocalHome customerHome = null;
  private AccountPopulator accountPopulator;
  private ProfilePopulator profilePopulator;


  public CustomerPopulator() throws PopulateException {
    this(XML_CUSTOMERS);
    return;
  }

  public CustomerPopulator(String rootTag) throws PopulateException {
    this.rootTag = rootTag;
    accountPopulator = new AccountPopulator(rootTag);
    profilePopulator = new ProfilePopulator(rootTag);
    return;
  }

  public XMLFilter setup(XMLReader reader) throws PopulateException {
    return new XMLDBHandler(profilePopulator.setup(accountPopulator.setup(reader)), rootTag, XML_CUSTOMER) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        createCustomer(getValue(XML_ID), accountPopulator.getAccount(), profilePopulator.getProfile());
        return;
      }
    };
  }

  public boolean check() throws PopulateException {
    try {
          InitialContext context = new InitialContext();
          customerHome = (CustomerLocalHome) context.lookup(JNDI_CUSTOMER_HOME);
          Collection customers = customerHome.findAllCustomers();
          if ((customers == null) || (customers.size() == 0)) {
        return false;
      }
    } catch (Exception e) {
          return false;
    }
    return true;
  }

  private CustomerLocal createCustomer(String id, AccountLocal account, ProfileLocal profile) throws PopulateException {
    try {
      if (customerHome == null) {
        InitialContext context = new InitialContext();
        customerHome = (CustomerLocalHome) context.lookup(JNDI_CUSTOMER_HOME);
      }
      try {
        CustomerLocal customer = customerHome.findByPrimaryKey(id);
        customer.remove();
      } catch (Exception exception) {}
      CustomerLocal customer = customerHome.create(id);
      if (account != null) {
        AccountLocal acct = customer.getAccount();
        acct.setStatus(account.getStatus());
        acct.setContactInfo(account.getContactInfo());
        acct.setCreditCard(account.getCreditCard());
        account.remove();
      }
      if (profile != null) {
        ProfileLocal prof = customer.getProfile();
        prof.setPreferredLanguage(profile.getPreferredLanguage());
        prof.setFavoriteCategory(profile.getFavoriteCategory());
        prof.setMyListPreference(profile.getMyListPreference());
        prof.setBannerPreference(profile.getBannerPreference());
        profile.remove();
      }
      return customer;
    } catch (Exception exception) {
      throw new PopulateException ("Could not create: " + exception.getMessage(), exception);
    }
  }
}


