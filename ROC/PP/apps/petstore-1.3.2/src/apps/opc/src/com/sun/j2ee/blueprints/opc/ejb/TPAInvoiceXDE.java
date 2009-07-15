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


package com.sun.j2ee.blueprints.opc.ejb;


import java.io.*;
import java.util.*;
import java.net.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.sun.j2ee.blueprints.xmldocuments.*;


public class TPAInvoiceXDE extends XMLDocumentEditor.DefaultXDE {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD TPA-Invoice 1.0//EN";
  public static final String XSD_PUBLIC_ID = "http://blueprints.j2ee.sun.com/TPAInvoice";
  public static final String INVOICE_NS = "http://blueprints.j2ee.sun.com/TPAInvoice";
  public static final String LINEITEM_NS = "http://blueprints.j2ee.sun.com/TPALineItem";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPAInvoice.dtd";
  public static final String XSD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPAInvoice.xsd";
  public static final String XML_INVOICE = "Invoice";
  public static final String XML_ORDERID = "OrderId";
  public static final String XML_LINEITEMS = "LineItems";
  public static final String XML_LINEITEM = "LineItem";
  public static final String XML_ITEMID = "itemId";
  public static final String XML_QUANTITY = "quantity";
  private Transformer transformer;
  private String orderId = null;
  private Map lineItemIds = null;
  private Document invoiceDocument = null;


  public TPAInvoiceXDE(URL entityCatalogURL, boolean validating) throws XMLDocumentException {
        this(entityCatalogURL, validating, false);
        return;
  }

  public TPAInvoiceXDE() throws XMLDocumentException {
        this(null, true, false);
        return;
  }

  public TPAInvoiceXDE(URL entityCatalogURL, boolean validating, boolean xsdValidation) throws XMLDocumentException {
    setEntityCatalogURL(entityCatalogURL);
    setValidating(validating);
    setSupportingXSD(xsdValidation);
        transformer = XMLDocumentUtils.createTransformer();
        return;
  }

  public void setDocument(Source source) throws XMLDocumentException {
        invoiceDocument = XMLDocumentUtils.deserialize(transformer, source,
                                                   (isSupportingXSD() ? XSD_PUBLIC_ID : DTD_PUBLIC_ID),
                                                   getEntityCatalogURL(), isValidating(), isSupportingXSD());
        lineItemIds = new HashMap();
        orderId = null;
        extractData();
        return;
  }

  public void setDocument(String text) throws XMLDocumentException {
        setDocument(new StreamSource(new StringReader(text)));
        return;
  }

  public Source getDocument() throws XMLDocumentException {
        if (invoiceDocument != null) {
      return new DOMSource(invoiceDocument);
        }
        throw new XMLDocumentException("No document source previously set.");
  }

  public String getOrderId() {
        return orderId;
  }

  public Map getLineItemIds() {
        return lineItemIds;
  }

  private void extractData() throws XMLDocumentException {
        Element element = invoiceDocument.getDocumentElement();
        if (element.getLocalName().equals(XML_INVOICE) && element.getNamespaceURI().equals(INVOICE_NS)) {
      Element child;
      child = XMLDocumentUtils.getFirstChildNS(element, INVOICE_NS, XML_ORDERID, false);
      orderId = XMLDocumentUtils.getContentAsString(child, false);
      child = XMLDocumentUtils.getSiblingNS(child, INVOICE_NS, XML_LINEITEMS, false);
      for (child = XMLDocumentUtils.getFirstChildNS(child, LINEITEM_NS, XML_LINEITEM, false);
           child != null;
           child = XMLDocumentUtils.getNextSiblingNS(child, LINEITEM_NS, XML_LINEITEM, true)) {
                lineItemIds.put(XMLDocumentUtils.getAttribute(child, XML_ITEMID, false),
                        new Integer(XMLDocumentUtils.getAttributeAsInt(child, XML_QUANTITY, false)));
      }
      return;
        }
        throw new XMLDocumentException(XML_INVOICE + " element expected.");
  }

  public static void main(String[] args) {
        if (args.length <= 1) {
      String fileName = args.length > 0 ? args[0] : "Invoice.xml";
      try {
                TPAInvoiceXDE invoiceXDE = new TPAInvoiceXDE();
                invoiceXDE.setDocument(new StreamSource(new FileInputStream(new File(fileName)), fileName));
                System.err.println("fileName: " + fileName + ", orderId=" + invoiceXDE.getOrderId() + " lineItemIds=" + invoiceXDE.getLineItemIds());
                System.exit(0);
      } catch (IOException exception) {
                System.err.println(exception);
                System.exit(2);
      } catch (XMLDocumentException exception) {
                System.err.println(exception.getRootCause());
                System.exit(2);
      }
        }
        System.err.println("Usage: " + TPAInvoiceXDE.class.getName() + " [file-name]");
        System.exit(1);
  }
}
