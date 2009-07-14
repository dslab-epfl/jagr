/*
 * $Id: CartItem.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.shoppingcart.cart.model;

import java.io.Serializable;

/**
 * This class represents an individual line item
 * of the shopping cart.
 */
public class CartItem implements Serializable {

    private String itemId;
    private String productId;
    private String name;
    private String attribute;
    private int quantity;
    private double unitCost;

    public CartItem(String itemId, String productId, String name,
                    String attribute, int quantity, double unitCost) {

        this.itemId = itemId;
        this.productId = productId;
        this.name = name;
        this.attribute = attribute;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public String getItemId() {
        return itemId;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getAttribute() {
        return attribute;
    }
    public int getQuantity() {
        return quantity;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public double getTotalCost() {
        return quantity * unitCost;
    }
}
