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
import org.w3c.dom.*;

import com.sun.j2ee.blueprints.xmldocuments.*;


public class TPALineItemUtils {
  public static final String XML_NAMESPACE = "http://blueprints.j2ee.sun.com/TPALineItem";
  public static final String XML_PREFIX = "tpali";
  public static final String XML_LINEITEM = XML_PREFIX + ":" + "LineItem";
  public static final String XML_CATEGORYID = "categoryId";
  public static final String XML_PRODUCTID = "productId";
  public static final String XML_ITEMID = "itemId";
  public static final String XML_LINENO = "lineNo";
  public static final String XML_QUANTITY = "quantity";
  public static final String XML_UNITPRICE = "unitPrice";


  private TPALineItemUtils() {}

  public static void addLineItem(Document document, Element lineItemsElement,
                                 String categoryId, String productId, String itemId, String lineNo,
                                 int quantity, float unitPrice) {
    Element lineItemElement = document.createElementNS(XML_NAMESPACE, XML_LINEITEM);
    lineItemElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + XML_PREFIX, XML_NAMESPACE);
    lineItemElement.setAttributeNS(XML_NAMESPACE, XML_CATEGORYID, categoryId);
    lineItemElement.setAttributeNS(XML_NAMESPACE, XML_PRODUCTID, productId);
    lineItemElement.setAttributeNS(XML_NAMESPACE, XML_ITEMID, itemId);
    lineItemElement.setAttributeNS(XML_NAMESPACE, XML_LINENO, lineNo);
    lineItemElement.setAttributeNS(XML_NAMESPACE, XML_QUANTITY, Long.toString(quantity));
    lineItemElement.setAttributeNS(XML_NAMESPACE, XML_UNITPRICE, Float.toString(unitPrice));
    lineItemsElement.appendChild(lineItemElement);
    return;
  }
}
