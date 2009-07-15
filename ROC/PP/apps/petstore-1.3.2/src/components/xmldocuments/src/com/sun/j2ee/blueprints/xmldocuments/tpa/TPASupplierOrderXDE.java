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


package com.sun.j2ee.blueprints.xmldocuments.tpa;


import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.sun.j2ee.blueprints.xmldocuments.*;


public class TPASupplierOrderXDE extends XMLDocumentEditor.DefaultXDE {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD TPA-SupplierOrder 1.0//EN";
  public static final String XSD_PUBLIC_ID = "http://blueprints.j2ee.sun.com/TPASupplierOrder";
  public static final String XML_NAMESPACE = "http://blueprints.j2ee.sun.com/TPASupplierOrder";
  public static final String XML_PREFIX = "tpaso";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPASupplierOrder.dtd";
  public static final String XSD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPASupplierOrder.xsd";
  public static final String XML_SUPPLIERORDER = XML_PREFIX + ":" + "SupplierOrder";
  public static final String XML_ORDERID = XML_PREFIX + ":" + "OrderId";
  public static final String XML_ORDERDATE = XML_PREFIX + ":" + "OrderDate";
  public static final String XML_SHIPPINGADDRESS = XML_PREFIX + ":" + "ShippingAddress";
  public static final String XML_FIRSTNAME = XML_PREFIX + ":" + "FirstName";
  public static final String XML_LASTNAME = XML_PREFIX + ":" + "LastName";
  public static final String XML_STREET = XML_PREFIX + ":" + "Street";
  public static final String XML_CITY = XML_PREFIX + ":" + "City";
  public static final String XML_STATE = XML_PREFIX + ":" + "State";
  public static final String XML_COUNTRY = XML_PREFIX + ":" + "Country";
  public static final String XML_ZIPCODE = XML_PREFIX + ":" + "ZipCode";
  public static final String XML_EMAIL = XML_PREFIX + ":" + "Email";
  public static final String XML_PHONE = XML_PREFIX + ":" + "Phone";
  public static final String XML_LINEITEMS = XML_PREFIX + ":" + "LineItems";
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private final DocumentBuilder builder;
  private final Transformer transformer;
  private final String systemId;
  private Document supplierOrderDocument = null;
  private Element orderIdElement = null;
  private Element orderDateElement = null;
  private Element shippingAddressElement = null;
  private Element lineItemsElement = null;


  public TPASupplierOrderXDE() throws XMLDocumentException {
    this(null, false);
    return;
  }

  public TPASupplierOrderXDE(URL entityCatalogURL, boolean xsdValidation) throws XMLDocumentException {
    setEntityCatalogURL(entityCatalogURL);
    setSupportingXSD(xsdValidation);
    try {
      CustomEntityResolver entityResolver = new CustomEntityResolver(entityCatalogURL);
      String systemId = entityResolver.mapEntityURI(isSupportingXSD() ? XSD_PUBLIC_ID : DTD_PUBLIC_ID);
      this.systemId = (systemId != null) ? systemId : (isSupportingXSD() ? XSD_SYSTEM_ID : DTD_SYSTEM_ID);
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      throw new XMLDocumentException(exception);
    }
    builder = XMLDocumentUtils.createDocumentBuilder();
    transformer = XMLDocumentUtils.createTransformer();
    return;
  }

  public void newDocument() {
    supplierOrderDocument = builder.newDocument();
    orderIdElement = null;
    orderDateElement = null;
    shippingAddressElement = null;
    lineItemsElement = null;
    return;
  }

  public void copyDocument(Result result) throws XMLDocumentException {
    Element supplierOrderElement = supplierOrderDocument.createElementNS(XML_NAMESPACE, XML_SUPPLIERORDER);
    supplierOrderElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + XML_PREFIX, XML_NAMESPACE);
    supplierOrderElement.appendChild(orderIdElement);
    supplierOrderElement.appendChild(orderDateElement);
    supplierOrderElement.appendChild(shippingAddressElement);
    supplierOrderElement.appendChild(lineItemsElement);
    supplierOrderDocument.appendChild(supplierOrderElement);
    XMLDocumentUtils.serialize(XMLDocumentUtils.createTransformer(), supplierOrderDocument,
                               (isSupportingXSD() ? XSD_PUBLIC_ID : DTD_PUBLIC_ID),
                               systemId, isSupportingXSD(), XMLDocumentUtils.DEFAULT_ENCODING, result);
    return;
  }

  public Source getDocument() throws XMLDocumentException {
    DOMResult result = new DOMResult();
    copyDocument(result);
    return new DOMSource(result.getNode(), result.getSystemId());
  }

  public String getDocumentAsString() throws XMLDocumentException {
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      copyDocument(new StreamResult(stream));
      return stream.toString(XMLDocumentUtils.DEFAULT_ENCODING);
    } catch (Exception exception) {
      throw new XMLDocumentException(exception);
    }
  }

  public void setOrderId(String orderId) {
    orderIdElement = XMLDocumentUtils.createElement(supplierOrderDocument, XML_NAMESPACE, XML_ORDERID, orderId);
    return;
  }

  public void setOrderDate(Date orderDate) {
    orderDateElement = XMLDocumentUtils.createElement(supplierOrderDocument, XML_NAMESPACE, XML_ORDERDATE,
                                                      dateFormat.format(orderDate));
    return;
  }

  public void setShippingAddress(String firstName, String lastName, String street, String city,
                                 String state, String country, String zipCode, String email, String phone) {
    shippingAddressElement = supplierOrderDocument.createElementNS(XML_NAMESPACE, XML_SHIPPINGADDRESS);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_FIRSTNAME, firstName);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_LASTNAME, lastName);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_STREET, street);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_CITY, city);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_STATE, state);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_COUNTRY, country);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_ZIPCODE, zipCode);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_EMAIL, email);
    XMLDocumentUtils.appendChild(supplierOrderDocument, shippingAddressElement, XML_PHONE, phone);
    return;
  }

  public void addLineItem(String categoryId, String productId, String itemId, String lineNo,
                          int quantity, float unitPrice) {
    if (lineItemsElement == null) {
      lineItemsElement = supplierOrderDocument.createElementNS(XML_NAMESPACE, XML_LINEITEMS);
    }
    TPALineItemUtils.addLineItem(supplierOrderDocument, lineItemsElement,
                                 categoryId, productId, itemId, lineNo, quantity, unitPrice);
    return;
  }

  public static void main(String[] args) {
    try {
      TPASupplierOrderXDE builder = new TPASupplierOrderXDE(null, true);
      builder.newDocument();
      builder.setOrderId("007");
      builder.setShippingAddress("James", "Bond", "Seventh Street", "London", "LD", "Great Britain",
                                 "77007", "james.bond@mi6.uk.gov", "700 777 7777");
      builder.setOrderDate(new Date());
      builder.addLineItem("GUN", "BERETTA", "007", "001", 1, 1000);
      System.out.println(builder.getDocumentAsString());
      System.exit(0);
    } catch (XMLDocumentException exception) {
      System.err.println(exception.getRootCause());
      System.exit(2);
    }
  }
}
