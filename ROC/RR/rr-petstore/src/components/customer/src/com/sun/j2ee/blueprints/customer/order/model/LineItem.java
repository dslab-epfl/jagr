/*
 * $Id: LineItem.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.customer.order.model;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * This class represents the line item details of an order.
 * This is a helper class used by remote class Order for
 * displaying the line item details of an order.
 */
public class LineItem implements java.io.Serializable {

    private String itemNo;
    private int qty;
    private double unitPrice;
    private int lineNo;

    public LineItem () {
        itemNo = new String();
        qty = 0;
        unitPrice = 0;
        lineNo = 0;
    }

    public LineItem (String itemNo, int qty,
                     double unitPrice, int lineNo) {
        this.itemNo = itemNo;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.lineNo = lineNo;
    }

    public String getItemNo() {
        return itemNo;
    }

    public int getQty() {
        return qty;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String toString() {
        return "itemno=" + itemNo + " qty=" + qty + " unitPrice=" + unitPrice + " lineNo=" + lineNo;
    }

    public Element toXml(Document doc) {
        Element root = doc.createElement("Item");
        root.setAttribute("Id", String.valueOf(itemNo));

        Element node = doc.createElement("Quantity");
        node.appendChild(doc.createTextNode(String.valueOf(qty)));
        root.appendChild(node);

        node = doc.createElement("UnitPrice");
        node.appendChild(doc.createTextNode(String.valueOf(unitPrice)));
        root.appendChild(node);

        return root;
    }
}
