/*
 * $Id: InventoryModel.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.inventory.model;

/**
 * This class represents the model date for the
 * inventory. Note that this object is immutable
 * since it is intended to be read only.
 */
public class InventoryModel implements java.io.Serializable {

    public String itemId;
    public int quantity;

    public InventoryModel(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    /**
     * Class constructor with no arguments, used by the web tier.
     */
    public InventoryModel() {}

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }
}
