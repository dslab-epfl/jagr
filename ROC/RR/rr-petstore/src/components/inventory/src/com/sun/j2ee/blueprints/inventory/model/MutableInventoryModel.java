/*
 * $Id: MutableInventoryModel.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.inventory.model;

import com.sun.j2ee.blueprints.inventory.model.InventoryModel;

/**
 * This class represents the model date for the
 * inventory. Note that this object is mutable
 * since it is intended to be read the inventory EJB only.
 */
public class MutableInventoryModel extends InventoryModel {

    public MutableInventoryModel(String itemId, int quantity) {
        super(itemId, quantity);
    }

    public void reduceQty(int qty) {
        quantity -= qty;
    }

    public void addQty(int qty) {
        quantity += qty;
    }
}
