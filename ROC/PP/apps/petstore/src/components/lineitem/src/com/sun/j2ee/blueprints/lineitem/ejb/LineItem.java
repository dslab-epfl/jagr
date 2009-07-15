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

package com.sun.j2ee.blueprints.lineitem.ejb;


import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentUtils;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;


public class LineItem {
  public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD LineItem 1.1//EN";
  public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/lineitem/rsrc/schemas/LineItem.dtd";
  public static final String XML_LINEITEM = "LineItem";
  public static final String XML_CATEGORYID = "CategoryId";
  public static final String XML_PRODUCTID = "ProductId";
  public static final String XML_ITEMID = "ItemId";
  public static final String XML_LINENUM = "LineNum";
  public static final String XML_QUANTITY = "Quantity";
  public static final String XML_UNITPRICE = "UnitPrice";
  private String categoryId;
  private String productId;
  private String itemId;
  private String lineNumber;
  private int    quantity;
  private float  unitPrice;


  public LineItem() {} // Used by the fromDOM() factory method

  public LineItem(String categoryId, String productId, String itemId, String lineNumber,
                  int quantity, float unitPrice) {
    this.categoryId = categoryId;
    this.productId = productId;
    this.itemId = itemId;
    this.lineNumber = lineNumber;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  public String getCategoryId() {
    return this.categoryId;
  }

  public String getProductId() {
    return this.productId;
  }

  public String getItemId() {
    return this.itemId;
  }

  public String getLineNumber() {
    return this.lineNumber;
  }

  public int getQuantity() {
    return this.quantity;
  }

  public float getUnitPrice() {
    return this.unitPrice;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
    return;
  }

  public void setProductId(String productId) {
    this.productId = productId;
    return;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
    return;
  }

  public void setLineNumber(String lineNumber) {
    this.lineNumber = lineNumber;
    return;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
    return;
  }

  public void setUnitPrice(float unitPrice) {
    this.unitPrice = unitPrice;
    return;
  }

  public Node toDOM(Document document) {
    Element root = document.createElement(XML_LINEITEM);
    XMLDocumentUtils.appendChild(document, root, XML_CATEGORYID, categoryId);
    XMLDocumentUtils.appendChild(document, root, XML_PRODUCTID, productId);
    XMLDocumentUtils.appendChild(document, root, XML_ITEMID, itemId);
    XMLDocumentUtils.appendChild(document, root, XML_LINENUM, lineNumber);
    XMLDocumentUtils.appendChild(document, root, XML_QUANTITY, quantity);
    XMLDocumentUtils.appendChild(document, root, XML_UNITPRICE, unitPrice);
    return root;
  }

  public static LineItem fromDOM(Node node) throws XMLDocumentException {
    Element element;
    if (node.getNodeType() == Node.ELEMENT_NODE && (element = ((Element) node)).getTagName().equals(XML_LINEITEM)) {
      Element child;
      LineItem lineItem = new LineItem();
      lineItem.categoryId = XMLDocumentUtils.getContentAsString(child = XMLDocumentUtils.getFirstChild(element, XML_CATEGORYID, false), false);
      lineItem.productId = XMLDocumentUtils.getContentAsString(child = XMLDocumentUtils.getNextSibling(child, XML_PRODUCTID, false), false);
      lineItem.itemId = XMLDocumentUtils.getContentAsString(child = XMLDocumentUtils.getNextSibling(child, XML_ITEMID, false), false);
      lineItem.lineNumber = XMLDocumentUtils.getContentAsString(child = XMLDocumentUtils.getNextSibling(child, XML_LINENUM, false), false);
      lineItem.quantity = XMLDocumentUtils.getContentAsInt(child = XMLDocumentUtils.getNextSibling(child, XML_QUANTITY, false), false);
      lineItem.unitPrice = XMLDocumentUtils.getContentAsFloat(child = XMLDocumentUtils.getNextSibling(child, XML_UNITPRICE, false), false);
      return lineItem;
    }
    throw new XMLDocumentException(XML_LINEITEM + " element expected.");
  }
}
