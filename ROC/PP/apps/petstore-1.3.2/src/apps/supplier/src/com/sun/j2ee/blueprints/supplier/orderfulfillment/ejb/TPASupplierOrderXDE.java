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


package com.sun.j2ee.blueprints.supplier.orderfulfillment.ejb;


import java.io.*;
import java.util.*;
import java.net.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.sun.j2ee.blueprints.xmldocuments.*;
import com.sun.j2ee.blueprints.supplierpo.ejb.SupplierOrder;


public class TPASupplierOrderXDE extends XMLDocumentEditor.DefaultXDE {
  public static final String DEFAULT_SCHEMA_URI
  = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD TPA-SupplierOrder 1.0//EN";
  public static final String STYLE_SHEET_CATALOG_PATH
  = "/com/sun/j2ee/blueprints/supplier/rsrc/SupplierOrderStyleSheetCatalog.properties";
  private String schemaURI;
  private Transformer transformer;
  private SupplierOrder supplierOrder = null;


  public TPASupplierOrderXDE() throws XMLDocumentException {
    this(null, true, DEFAULT_SCHEMA_URI);
    return;
  }

  public TPASupplierOrderXDE(URL entityCatalogURL, boolean validating, String schemaURI)
    throws XMLDocumentException {
      setEntityCatalogURL(entityCatalogURL);
      setValidating(validating);
      this.schemaURI = schemaURI;
      Properties styleSheetCatalog = new Properties();
      InputStream stream = getClass().getResourceAsStream(STYLE_SHEET_CATALOG_PATH);
      if (stream != null) {
        try {
          styleSheetCatalog.load(stream);
        } catch (IOException exception) {
          System.err.println("Can't load from resource: " + STYLE_SHEET_CATALOG_PATH + ": " + exception);
        }
      } else {
        System.err.println("Can't access resource: " + STYLE_SHEET_CATALOG_PATH);
      }
      String styleSheetPath = styleSheetCatalog.getProperty(schemaURI);
      //System.err.println("styleSheetPath: " + styleSheetPath);
      String supportingXSD = styleSheetCatalog.getProperty(schemaURI + ".XSDSupport");
      setSupportingXSD(supportingXSD != null && Boolean.getBoolean(supportingXSD));
      if (styleSheetPath != null && !styleSheetPath.trim().equals("")) {
        stream = getClass().getResourceAsStream(styleSheetPath);
        if (stream != null) {
          try {
            transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(stream));
          } catch (Exception exception) {
            throw new XMLDocumentException(exception);
          }
        } else {
          throw new XMLDocumentException("Can't access style sheet: " + styleSheetPath);
        }
      } else {
        try {
          transformer = XMLDocumentUtils.createTransformer();
        } catch (Exception exception) {
          throw new XMLDocumentException(exception);
        }
      }
      return;
  }

  public void setDocument(String buffer) throws XMLDocumentException {
    setDocument(new StreamSource(new StringReader(buffer)));
    return;
  }

  public void setDocument(Source source) throws XMLDocumentException {
    supplierOrder = null;
    supplierOrder
      = SupplierOrder.fromDOM(XMLDocumentUtils.transform(transformer, source, schemaURI,
                                                         getEntityCatalogURL(), isValidating(), isSupportingXSD()));
    return;
  }

  public SupplierOrder getSupplierOrder() {
    return supplierOrder;
  }

  public static void main(String[] args) {
    if (args.length <= 1) {
      String fileName = args[0];
      try {
        TPASupplierOrderXDE supplierOrderXDE = new TPASupplierOrderXDE();
        supplierOrderXDE.setDocument(new StreamSource(new FileInputStream(new File(fileName)), fileName));
        supplierOrderXDE.getSupplierOrder().toXML(new StreamResult(System.out));
        System.exit(0);
      } catch (IOException exception) {
        System.err.println(exception);
        System.exit(2);
      } catch (XMLDocumentException exception) {
        exception.printStackTrace(System.err);
        System.err.println(exception.getRootCause());
        System.exit(2);
      }
    }
    System.err.println("Usage: " + SupplierOrder.class.getName() + " [file-name]");
    System.exit(1);
  }
}
