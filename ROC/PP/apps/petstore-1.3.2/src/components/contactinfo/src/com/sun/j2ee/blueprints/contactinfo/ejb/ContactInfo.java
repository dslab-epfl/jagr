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

package com.sun.j2ee.blueprints.contactinfo.ejb;


import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentUtils;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.address.ejb.Address;


public class ContactInfo {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD ContactInfo 1.1//EN";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/contactinfo/rsrc/schemas/ContactInfo.dtd";
  public static final String XML_CONTACTINFO = "ContactInfo";
  public static final String XML_FAMILY_NAME = "FamilyName";
  public static final String XML_GIVEN_NAME = "GivenName";
  public static final String XML_ADDRESS = "Address";
  public static final String XML_EMAIL = "Email";
  public static final String XML_PHONE = "Phone";
  private String familyName;
  private String givenName;
  private Address address;
  private String email;
  private String phone;

  // Constructor to be used when creating PO from data

  public ContactInfo() {}

  public ContactInfo(String familyName, String givenName, Address address, String email, String phone) {
    this.familyName = familyName;
    this.givenName = givenName;
    this.address = address;
    this.email = email;
    this.phone = phone;
    return;
  }

  // getter methods

  public String getFamilyName() {
    return familyName;
  }

  public String getGivenName() {
    return givenName;
  }

  public Address getAddress() {
    return address;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  // setter methods

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
    return;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
    return;
  }

  public void setAddress(Address address) {
    this.address = address;
    return;
  }

  public void setEmail(String email) {
    this.email = email;
    return;
  }

  public void setPhone(String phone) {
    this.phone = phone;
    return;
  }

  // XML (de)serialization methods

  public Node toDOM(Document document) {
    Element root = document.createElement(XML_CONTACTINFO);
    XMLDocumentUtils.appendChild(document, root, XML_FAMILY_NAME, familyName);
    XMLDocumentUtils.appendChild(document, root, XML_GIVEN_NAME, givenName);
    root.appendChild(address.toDOM(document));
    XMLDocumentUtils.appendChild(document, root, XML_EMAIL, email);
    XMLDocumentUtils.appendChild(document, root, XML_PHONE, phone);
    return root;
  }

  public static ContactInfo fromDOM(Node node) throws XMLDocumentException {
    Element element;
    if (node.getNodeType() == Node.ELEMENT_NODE && (element = ((Element) node)).getTagName().equals(XML_CONTACTINFO)) {
      Element child;
      ContactInfo contactInfo = new ContactInfo();
      child = XMLDocumentUtils.getFirstChild(element, XML_FAMILY_NAME, false);
      contactInfo.familyName = XMLDocumentUtils.getContentAsString(child, false);
      child = child = XMLDocumentUtils.getNextSibling(child, XML_GIVEN_NAME, false);
      contactInfo.givenName = XMLDocumentUtils.getContentAsString(child, false);
      child =  XMLDocumentUtils.getNextSibling(child, Address.XML_ADDRESS, false);
      contactInfo.address = Address.fromDOM(child);
      child = XMLDocumentUtils.getNextSibling(child, XML_EMAIL, false);
      contactInfo.email = XMLDocumentUtils.getContentAsString(child, true /*false*/);
      child = XMLDocumentUtils.getNextSibling(child, XML_PHONE, false);
      contactInfo.phone = XMLDocumentUtils.getContentAsString(child, false);
      return contactInfo;
    }
    throw new XMLDocumentException(XML_CONTACTINFO + " element expected.");
  }

  public String toString() {
    return "ContactInfo[familyName=" + familyName + ", "
      + "givenName=" + givenName + ", "
      + "address=" + address.toString() + ", "
      + "email=" + email + ", "
      + "phone=" + phone + "]";
  }
}


