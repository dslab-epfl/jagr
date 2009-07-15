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
package com.sun.j2ee.blueprints.mailer.ejb;

import java.io.*;
import java.util.*;
import java.net.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentUtils;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;


public class Mail {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD OPC Mail 1.0//EN";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/mailer/rsrc/schemas/Mail.dtd";
  public static final boolean VALIDATING = true;
  public static final String XML_MAIL = "Mail";
  public static final String XML_SUBJECT = "Subject";
  public static final String XML_ADDRESS = "Address";
  public static final String XML_CONTENT = "Content";
  private String address = null;
  private String subject  = null;
  private String content = null;


  private Mail() {}

  public Mail(String address, String subject, String content) {
    this.address = address;
    this.subject = subject;
    this.content = content;
    return;
  }

  // getters

  public String getAddress() {
    return address;
  }

  public String getSubject() {
    return subject;
  }
  public String getContent() {
    return content;
  }

  // XML (de)serialization methods

  public void toXML(Result result) throws XMLDocumentException {
    toXML(result, null);
    return;
  }

  public String toXML() throws XMLDocumentException {
    return toXML((URL) null);
  }

  public void toXML(Result result, URL entityCatalogURL) throws XMLDocumentException {
    if (entityCatalogURL != null) {
      XMLDocumentUtils.toXML(toDOM(), DTD_PUBLIC_ID, entityCatalogURL, XMLDocumentUtils.DEFAULT_ENCODING, result);
    } else {
      XMLDocumentUtils.toXML(toDOM(), DTD_PUBLIC_ID, DTD_SYSTEM_ID, XMLDocumentUtils.DEFAULT_ENCODING, result);
    }
    return;
  }

  public String toXML(URL entityCatalogURL) throws XMLDocumentException {
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      toXML(new StreamResult(stream), entityCatalogURL);
      //System.err.println("toXML: " + stream.toString(XMLDocumentUtils.DEFAULT_ENCODING));
      return stream.toString(XMLDocumentUtils.DEFAULT_ENCODING);
    } catch (Exception exception) {
      throw new XMLDocumentException(exception);
    }
  }

  public static Mail fromXML(Source source) throws XMLDocumentException {
    return fromXML(source, null, VALIDATING);
  }

  public static Mail fromXML(String buffer) throws XMLDocumentException {
    return fromXML(buffer, null, VALIDATING);
  }

  public static Mail fromXML(Source source, URL entityCatalogURL, boolean validating) throws XMLDocumentException {
    return fromDOM(XMLDocumentUtils.fromXML(source, DTD_PUBLIC_ID, entityCatalogURL, validating).getDocumentElement());
  }

  public static Mail fromXML(String buffer, URL entityCatalogURL, boolean validating) throws XMLDocumentException {
    //System.err.println(buffer);
    try {
      return fromXML(new StreamSource(new StringReader(buffer)), entityCatalogURL, validating);
    } catch (XMLDocumentException exception) {
      System.err.println(exception.getRootCause().getMessage());
      throw new XMLDocumentException(exception);
    }
  }

  public Document toDOM() throws XMLDocumentException {
    Document document = XMLDocumentUtils.createDocument();
    Element root = (Element) toDOM(document);
    document.appendChild(root);
    return document;
  }

  public Node toDOM(Document document) {
    Element root = document.createElement(XML_MAIL);
    XMLDocumentUtils.appendChild(document, root, XML_ADDRESS, address);
    XMLDocumentUtils.appendChild(document, root, XML_SUBJECT, subject);
    XMLDocumentUtils.appendChild(document, root, XML_CONTENT, content);
    return root;
  }

  private static Mail fromDOM(Node node) throws XMLDocumentException {
    Element element;
    if (node.getNodeType() == Node.ELEMENT_NODE && (element = ((Element) node)).getTagName().equals(XML_MAIL)) {
      Element child;
      Mail mail = new Mail();
      mail.address = XMLDocumentUtils.getContentAsString(child = XMLDocumentUtils.getFirstChild(element, XML_ADDRESS, false), false);
      mail.subject = XMLDocumentUtils.getContentAsString(child = XMLDocumentUtils.getNextSibling(child, XML_SUBJECT, false), false);
      mail.content = XMLDocumentUtils.getContentAsString(child = XMLDocumentUtils.getNextSibling(child, XML_CONTENT, false), false);
      return mail;
    }
    throw new XMLDocumentException(XML_MAIL + " element expected.");
  }

  public static void main(String[] args) {
    if (args.length <= 1) {
      String fileName = args.length > 0 ? args[0] : "Mail.xml";
      try {
        Mail mail = Mail.fromXML(new StreamSource(new FileInputStream(new File(fileName)), fileName));
        System.out.println(Mail.fromXML(mail.toXML()).getContent());
        System.exit(0);
      } catch (IOException exception) {
        System.err.println(exception);
        System.exit(2);
      } catch (XMLDocumentException exception) {
        System.err.println(exception.getRootCause());
        System.exit(2);
      }
    }
    System.err.println("Usage: " + Mail.class.getName() + " [file-name]");
    System.exit(1);
  }
}
