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


package com.sun.j2ee.blueprints.supplier.webservice.porcvr;


import java.io.*;
import java.util.*;
import java.net.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.sun.j2ee.blueprints.xmldocuments.*;
import com.sun.j2ee.blueprints.supplierpo.ejb.SupplierOrder;


public class TPASupplierOrderXDE extends XMLDocumentEditor.DefaultXDE {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD TPA-SupplierOrder 1.0//EN";
  public static final String XSD_PUBLIC_ID = "http://blueprints.j2ee.sun.com/TPASupplierOrder";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPASupplierOrder.dtd";
  public static final String XSD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPASupplierOrder.xsd";
  public static final String STYLE_SHEET_PATH = "/com/sun/j2ee/blueprints/supplier/rsrc/xsl/TPASupplierOrder.xsl";
  public static final String XML_ORDER_ID = "OrderId";
  private XMLFilter filter;
  private Transformer transformer;
  private Source source = null;
  private String orderId = null;


  public TPASupplierOrderXDE(URL entityCatalogURL, boolean validating) throws XMLDocumentException {
        this(entityCatalogURL, validating, false);
        return;
  }

  public TPASupplierOrderXDE() throws XMLDocumentException {
        this(null, true, false);
        return;
  }

  public TPASupplierOrderXDE(URL entityCatalogURL, boolean validating, boolean xsdValidation) throws XMLDocumentException {
        setEntityCatalogURL(entityCatalogURL);
        setValidating(validating);
        setSupportingXSD(xsdValidation);
        InputStream stream = getClass().getResourceAsStream(STYLE_SHEET_PATH);
        if (stream != null) {
      try {
                SAXParser parser = XMLDocumentUtils.createParser(validating, xsdValidation, entityCatalogURL,
                                                         (xsdValidation ? XSD_PUBLIC_ID : DTD_PUBLIC_ID));

                filter = new XMLFilterImpl(parser.getXMLReader()) {
          private boolean acquired = false;
          private StringBuffer buffer = new StringBuffer();

          public void startElement(String namespace, String name, String qName, Attributes attrs) throws SAXException {
            if (name.equals(XML_ORDER_ID)) {
              buffer.setLength(0);
              acquired = true;
            }
            getContentHandler().startElement(namespace, name, qName, attrs);
            return;
          }

          public void endElement(String namespace, String name, String qName) throws SAXException {
            if (name.equals(XML_ORDER_ID)) {
              orderId = buffer.toString();
              acquired = false;
            }
            getContentHandler().endElement(namespace, name, qName);
            return;
          }

          public void characters(char[] chars, int start, int length) throws SAXException {
            if (acquired) {
              buffer.append(chars, start, length);
            }
            getContentHandler().characters(chars, start, length);
            return;
          }
        };
                transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(stream));
      } catch (Exception exception) {
                throw new XMLDocumentException(exception);
      }
        } else {
      throw new XMLDocumentException("Can't access style sheet: " + STYLE_SHEET_PATH);
        }
        return;
  }

  public void setDocument(String buffer) throws XMLDocumentException {
        setDocument(new StreamSource(new StringReader(buffer)));
        return;
  }

  public void setDocument(Source source) throws XMLDocumentException {
        this.source = source;
        return;
  }

  public void copyDocument(Result result) throws XMLDocumentException {
        InputSource inputSource  = SAXSource.sourceToInputSource(source);
        if (inputSource != null) {
      orderId = null;
      SAXSource saxSource = new SAXSource(filter, inputSource);
      XMLDocumentUtils.transform(transformer, saxSource, result,
                                 (isSupportingXSD() ? XSD_PUBLIC_ID : DTD_PUBLIC_ID),
                                 getEntityCatalogURL(), isValidating(), isSupportingXSD());
        } else {
      throw new XMLDocumentException("Source not supported: " + source);
        }
        return;
  }

  public Source getDocument() throws XMLDocumentException {
        return new StreamSource(new StringReader(getDocumentAsString()));
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

  public String getOrderId() {
        return orderId;
  }
}
