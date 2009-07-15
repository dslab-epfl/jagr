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

package com.sun.j2ee.blueprints.creditcard.ejb;


import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentUtils;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;


public class CreditCard {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD CreditCard 1.1//EN";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/creditcard/rsrc/schemas/CreditCard.dtd";
  public static final String XML_CREDITCARD = "CreditCard";
  public static final String XML_CARD_NUMBER = "CardNumber";
  public static final String XML_EXPIRYDATE = "ExpiryDate";
  public static final String XML_CARD_TYPE = "CardType";
  private String cardNumber;
  private String expiryDate;
  private String cardType;

  // Constructor to be used when creating PO from data

  public CreditCard() {}

  public CreditCard(String cardNumber, String expiryDate, String cardType) {
    this.cardNumber = cardNumber;
    this.expiryDate = expiryDate;
    this.cardType = cardType;
    return;
  }

  // getter methods

  public String getCardNumber() {
    return cardNumber;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public String getCardType() {
    return cardType;
  }

  // setter methods

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
    return;
  }

  public void setExpiryDate(String expiryDate) {
    this.expiryDate = expiryDate;
    return;
  }

  public void setCardType(String cardType) {
    this.cardType = cardType;
    return;
  }

  // XML (de)serialization methods

  public Node toDOM(Document document) {
    Element root = document.createElement(XML_CREDITCARD);
    XMLDocumentUtils.appendChild(document, root, XML_CARD_NUMBER, cardNumber);
    XMLDocumentUtils.appendChild(document, root, XML_CARD_TYPE, cardType);
    XMLDocumentUtils.appendChild(document, root, XML_EXPIRYDATE, expiryDate);
    return root;
  }

  public static CreditCard fromDOM(Node node) throws XMLDocumentException {
    Element element;
    if (node.getNodeType() == Node.ELEMENT_NODE && (element = ((Element) node)).getTagName().equals(XML_CREDITCARD)) {
      Element child;
      CreditCard creditCard = new CreditCard();
      child = XMLDocumentUtils.getFirstChild(element, XML_CARD_NUMBER, false);
      creditCard.cardNumber = XMLDocumentUtils.getContentAsString(child, false);
      child = XMLDocumentUtils.getNextSibling(child, XML_CARD_TYPE, false);
      creditCard.cardType = XMLDocumentUtils.getContentAsString(child, false);
      child = XMLDocumentUtils.getNextSibling(child, XML_EXPIRYDATE, false);
      creditCard.expiryDate = XMLDocumentUtils.getContentAsString(child, false);
      return creditCard;
    }
    throw new XMLDocumentException(XML_CREDITCARD + " element expected.");
  }
}


