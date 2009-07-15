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

package com.sun.j2ee.blueprints.address.ejb;


import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentUtils;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;


public class Address {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD Address 1.1//EN";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/address/rsrc/schemas/Address.dtd";
  public static final String XML_ADDRESS = "Address";
  public static final String XML_STREET_NAME = "StreetName";
  public static final String XML_CITY = "City";
  public static final String XML_STATE = "State";
  public static final String XML_COUNTRY = "Country";
  public static final String XML_ZIPCODE = "ZipCode";
  private String streetName1;
  private String streetName2;
  private String city;
  private String state;
  private String zipCode;
  private String country;

  // Constructor to be used when creating PO from data

  public Address() {}

  public Address(String streetName1, String streetName2, String city,
                 String state, String zipCode, String country) {
    this.streetName1 = streetName1;
    this.streetName2 = streetName2;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;
    this.country = country;
    return;
  }

  // getter methods

  public String getStreetName1() {
    return streetName1;
  }

  public String getStreetName2() {
    return streetName2;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getCountry() {
    return country;
  }

  public String getZipCode() {
    return zipCode;
  }

  // setter methods

  public void setStreetName1(String streetName) {
    this.streetName1 = streetName;
    return;
  }

  public void setStreetName2(String streetName) {
    this.streetName2 = streetName;
    return;
  }

  public void setCity(String city) {
    this.city = city;
    return;
  }

  public void setState(String state) {
    this.state = state;
    return;
  }

  public void setCountry(String country) {
    this.country = country;
    return;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
    return;
  }

  // XML (de)serialization methods

  public Node toDOM(Document document) {
    Element root = document.createElement(XML_ADDRESS);
    XMLDocumentUtils.appendChild(document, root, XML_STREET_NAME, streetName1);
    if (streetName2 != null && !streetName2.equals("")) {
      XMLDocumentUtils.appendChild(document, root, XML_STREET_NAME, streetName2);
    }
    XMLDocumentUtils.appendChild(document, root, XML_CITY, city);
    XMLDocumentUtils.appendChild(document, root, XML_STATE, state);
    XMLDocumentUtils.appendChild(document, root, XML_ZIPCODE, zipCode);
    XMLDocumentUtils.appendChild(document, root, XML_COUNTRY, country);
    return root;
  }

  public static Address fromDOM(Node node) throws XMLDocumentException {
    Element element;
    if (node.getNodeType() == Node.ELEMENT_NODE && (element = ((Element) node)).getTagName().equals(XML_ADDRESS)) {
      Element child;
      Address address = new Address();
      child = XMLDocumentUtils.getFirstChild(element, XML_STREET_NAME, false);
      address.streetName1 = XMLDocumentUtils.getContentAsString(child, false);
      Element optionalChild = XMLDocumentUtils.getNextSibling(child, XML_STREET_NAME, true);
      if (optionalChild != null) {
        address.streetName2 = XMLDocumentUtils.getContentAsString(optionalChild, false);
        child = optionalChild;
      }
      child = XMLDocumentUtils.getNextSibling(child, XML_CITY, false);
      address.city = XMLDocumentUtils.getContentAsString(child, false);
      child = XMLDocumentUtils.getNextSibling(child, XML_STATE, false);
      address.state = XMLDocumentUtils.getContentAsString(child, false);
      child = XMLDocumentUtils.getNextSibling(child, XML_ZIPCODE, false);
      address.zipCode = XMLDocumentUtils.getContentAsString(child, false);
      child = XMLDocumentUtils.getNextSibling(child, XML_COUNTRY, false);
      address.country = XMLDocumentUtils.getContentAsString(child, false);
      return address;
    }
    throw new XMLDocumentException(XML_ADDRESS + " element expected.");
  }

  public String toString() {
    return "Address[streeName1=" + streetName1 + ", "
      + "streetName2=" + streetName2 + ", "
      + "city=" + city + ", "
      + "state=" + state + ", "
      + "zipCode=" + zipCode + ", "
      + "country=" + country + "]";
  }
}


