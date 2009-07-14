/*
 * $Id: Item.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.shoppingcart.catalog.model;

import java.io.Serializable;

/**
 * This class represents a particular item in the Java Pet
 * Store Demo.  Each item belongs to particular type of product
 * and has attributes like id,listprice etc.
*/
public class Item implements Serializable {

    private String productId;
    private String attribute;
    private String itemId;
    private double listPrice;
    private double unitCost;

    public Item(String productId, String itemId, String attribute,
                         double listPrice, double unitCost) {
        this.productId = productId;
        this.itemId = itemId;
        this.attribute = attribute;
        this.listPrice = listPrice;
        this.unitCost = unitCost;
    }

    /**
     * Class constructor with no arguments, used by the web tier.
     */
    public Item() {}

    public String getProductId() {
        return productId;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getItemId() {
        return itemId;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public double getListCost() {
        return listPrice;
    }

}
