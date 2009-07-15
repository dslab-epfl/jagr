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
import com.sun.j2ee.blueprints.creditcard.ejb.*;


public class CreditCardPopulator {
  public static final String JNDI_CREDIT_CARD_HOME = "java:comp/env/ejb/CreditCard";
  private static final String XML_CREDITCARD = "CreditCard";
  private static final String XML_CARDNUMBER = "CardNumber";
  private static final String XML_CARDTYPE = "CardType";
  private static final String XML_EXPIRYDATE = "ExpiryDate";
  private String rootTag;
  private CreditCardLocalHome creditCardHome = null;
  private CreditCardLocal creditCard ;

  public CreditCardPopulator(String rootTag) {
    this.rootTag = rootTag;
    return;
  }

  public XMLFilter setup(XMLReader reader) throws PopulateException {
    return new XMLDBHandler(reader, rootTag, XML_CREDITCARD) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        creditCard = createCreditCard(getValue(XML_CARDNUMBER),
                                      getValue(XML_CARDTYPE),
                                      getValue(XML_EXPIRYDATE));
        return;
      }
    };
  }

  public boolean check() throws PopulateException {
    return true;
  }

  private CreditCardLocal createCreditCard(String cardNumber, String cardType, String expiryDate) throws PopulateException {
    try {
      if (creditCardHome == null) {
        InitialContext context = new InitialContext();
        creditCardHome = (CreditCardLocalHome) context.lookup(JNDI_CREDIT_CARD_HOME);
      }
      return creditCardHome.create(cardNumber, cardType, expiryDate);
    } catch (Exception exception) {
      throw new PopulateException ("Could not create: " + exception.getMessage(), exception);
    }

  }

  public CreditCardLocal getCreditCard() {
    return creditCard;
  }
}

