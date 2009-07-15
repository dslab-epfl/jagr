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
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.sun.j2ee.blueprints.customer.account.ejb.*;
import com.sun.j2ee.blueprints.creditcard.ejb.*;
import com.sun.j2ee.blueprints.contactinfo.ejb.*;


public class AccountPopulator {
  public static final String JNDI_ACCOUNT_HOME = "java:comp/env/ejb/Account";
  public static final String XML_ACCOUNT = "Account";
  private String rootTag;
  private AccountLocalHome accountHome = null;
  private AccountLocal account ;
  private ContactInfoPopulator contactInfoPopulator;
  private CreditCardPopulator creditCardPopulator;


  public AccountPopulator(String rootTag) {
    this.rootTag = rootTag;
    contactInfoPopulator = new ContactInfoPopulator(rootTag);
    creditCardPopulator = new CreditCardPopulator(rootTag);
    return;
  }

  public XMLFilter setup(XMLReader reader) throws PopulateException {
    return new XMLDBHandler(creditCardPopulator.setup(contactInfoPopulator.setup(reader)), rootTag, XML_ACCOUNT) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        account = createAccount(contactInfoPopulator.getContactInfo(), creditCardPopulator.getCreditCard());
        return;
      }
    };
  }

  public boolean check() throws PopulateException {
    return contactInfoPopulator.check() && creditCardPopulator.check();
  }

  private AccountLocal createAccount(ContactInfoLocal contactInfo, CreditCardLocal creditCard) throws PopulateException {
    try {
      if (accountHome == null) {
        InitialContext context = new InitialContext();
        accountHome = (AccountLocalHome) context.lookup(JNDI_ACCOUNT_HOME);
      }
      return accountHome.create(AccountLocalHome.Active, contactInfo, creditCard);
    } catch (Exception exception) {
      throw new PopulateException ("Could not create: " + exception.getMessage(), exception);
    }
  }

  public AccountLocal getAccount() {
    return account;
  }
}



