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

package com.sun.j2ee.blueprints.petstore.controller.events;

import java.util.*;

import com.sun.j2ee.blueprints.waf.event.EventSupport;

/**
 * This Event  contains the information for the  EJBController of a change the
 * state of the shopping cart.
 * <br><br>
 * There are four basic types of cart events:
 * <br>&nbsp;&nbsp;ADD_ITEM
 * <br>&nbsp;&nbsp;DELETE_ITEM
 * <br>&nbsp;&nbsp;UPDATE_ITEM(S)
 * <br>
 *
 */
public class CartEvent extends EventSupport {

    public static final int ADD_ITEM = 1;
    public static final int DELETE_ITEM = 2;
    public static final int UPDATE_ITEMS = 3;
    public static final int EMPTY = 4;

    // private variables

    private int actionType = -1;
    // for updates to multiple items where the key = itemID
    // The Integer value = quantity
    private Map items = null;
    // for updates, addition, removal of target item
    private String itemId;
    // for updates and and addtion of single items; multiple items use the items HashMap
    private int quantity = 1;

    /**
     * This contructor is used for the EMPTY_ITEMS actionType
     * exclusively. It will cause all items to be remove from the Shopping  Cart
     */
    public CartEvent(int actionType) {
        this.actionType = actionType;
    }

   /**
     * This constructor is used for the ADD_ITEM and  DELETE_ITEM actionTypes
     * exclusively.
     * <br><br>
     * <br>&nbsp;&nbsp;actionType = ADD_ITEM or DELETE_ITEM
     * <br>&nbsp;&nbsp;itemId = id of the item to be removed or added.
     * <br> <br>
     * When adding items with this construtor the purchase quantity is  1.
     *
     */
    public CartEvent(int actionType, String itemId) {
        this.actionType = actionType;
        this.itemId = itemId;
    }

   /**
     * This constructor is used for the ADD_ITEM or UPDATE_ITEM actionTypes
     * exclusively.
     * <br><br>
     * <br>&nbsp;&nbsp;actionType = ADD_ITEM or UPDATE_ITEM
     * <br>&nbsp;&nbsp;itemId = id of the item to be removed or added.
     * <br>&nbsp;&nbsp;itemId = id of the item to be removed or added.
     * <br> <br>
     * When adding items with this construtor the purchase quantity is  1.
     *
     */
    public CartEvent(int actionType, String itemId, int quantity) {
        this.actionType = actionType;
        this.itemId = itemId;
        this.quantity = quantity;
    }


   /**
     * This constructor is used for the UPDATE_ITEM actionType
     * exclusively.
     * <br><br>
     * <br>&nbsp;&nbsp;actionType = UPDATE_ITEM
     * <br>&nbsp;&nbsp;HashMap items = a HashMap of the items to
     * be updated. In this HashMap the key is an itemId to be updated
     * and the value is an Iteger which contains the quantity to be changed
     * to. When the quantity is 0 the item will be remove from the cart..
     * <br> <br>
     * When adding items with this construtor the purchase quantity is  1.
     *
     */
    public CartEvent(int actionType, HashMap items) {
        this.actionType = actionType;
        this.items = items;
    }

    public static CartEvent createUpdateItemEvent(Map items) {
        // Defensive copy or items
        CartEvent ret = new CartEvent(UPDATE_ITEMS, new HashMap(items));
        return ret;
    }

    public int getActionType() {
        return actionType;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Map getItems() {
        return items;
    }

    public String toString() {
        return "CartEvent[actionType=" + actionType + ", itemId=" +
                   itemId + ", quantity=" + quantity + ", items=" + items + "]";
    }

    public String getEventName() {
        return "CartEvent";
    }

}

