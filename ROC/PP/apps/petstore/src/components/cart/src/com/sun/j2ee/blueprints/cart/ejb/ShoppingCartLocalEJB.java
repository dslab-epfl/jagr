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
package com.sun.j2ee.blueprints.cart.ejb;

import java.util.Locale;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.sun.j2ee.blueprints.util.tracer.Debug;

// shoppingcart imports
import com.sun.j2ee.blueprints.cart.model.CartItem;

// catalog component imports
import com.sun.j2ee.blueprints.catalog.client.CatalogHelper;
import com.sun.j2ee.blueprints.catalog.model.Item;
import com.sun.j2ee.blueprints.catalog.client.CatalogException;

/**
 * This class represents the implementation of shopping
 * cart as a Session EJB.
 */
public class ShoppingCartLocalEJB implements SessionBean {

    private HashMap cart;
    // default to the US English
    private Locale locale = Locale.US;

    public ShoppingCartLocalEJB() {
        cart = new HashMap();
    }

    public HashMap getDetails()  {
        return cart;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

  /**
    * Return a collection of CartItems
   */
   public Collection getItems() {
       // get a catalog helper using local EJB Access
       CatalogHelper catalog = new CatalogHelper(false);
       HashMap map = getDetails();
       ArrayList items = new ArrayList();
       Iterator it = map.keySet().iterator();
       while (it.hasNext()) {
           String key = (String)it.next();
           Integer value = (Integer)map.get(key);
           Item item = null;
           try {
               item = catalog.getItem(key, locale);
               // convert catalog item to cart item
               CartItem ci = new CartItem(item.getItemId(),
                                      item.getProductId(),
                                      item.getCategory(),
                                      item.getProductName(),
                                      item.getAttribute(),
                                      value.intValue(),
                                      item.getListCost());
               items.add(ci);
           } catch (CatalogException cce) {
               System.out.println("ShoppingCartEJB caught: " + cce);
           }
       }
       return items;
   }

    public void addItem (String itemID) {
        cart.put(itemID, new Integer(1));
    }

    public void addItem (String itemID,int qty) {
        cart.put(itemID, new Integer(qty));
    }

    public void deleteItem (String itemID) {
        cart.remove(itemID);
    }

    public void updateItemQuantity (String itemID, int newQty) {
        cart.remove(itemID);
        // remove item if it is less than or equal to 0
        if (newQty > 0) cart.put(itemID, new Integer(newQty));
    }

    public Integer getCount() {
        return new Integer(cart.size());
    }

   public Double getSubTotal() {
        Collection items = getItems();
        if (items == null) return null;
        // put the subtotal in the request
        double ret = 0.0d;
        // total up the quantities
        for (Iterator it =items.iterator(); it.hasNext(); ) {
            CartItem i = (CartItem) it.next();
            ret += (i.getUnitCost() * i.getQuantity()) ;
        }
        return new Double(ret);
   }

    public void empty () {
        cart.clear();
    }

    public void ejbCreate() {
        cart = new HashMap();
    }

    public void setSessionContext(SessionContext sc) {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}


