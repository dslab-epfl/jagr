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

package com.sun.j2ee.blueprints.cart.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Collection;
import com.sun.j2ee.blueprints.util.tracer.Debug;

/**
 * This class represents the model data for the shopping cart.
 * It is a value object and has fine grained getter methods.
 */
public class ShoppingCartModel implements Serializable {

    private Collection items;

    public ShoppingCartModel(Collection items) {
        this.items = items;
    }

    /**
     * Class constructor with no arguments, used by the web tier.
     */
    public ShoppingCartModel() {}

    public int getSize() {
        if (items != null) return items.size();
        else return 0;
    }


    /** @return an collection of all the CartItems. */
    public Collection getCart() {
        return items;
    }


    /** @return an iterator over all the CartItems. */
    public Iterator getItems() {
        return items.iterator();
    }

    public double getTotalCost() {
        double total = 0;
        for (Iterator li = getItems(); li.hasNext(); ) {
            CartItem item = (CartItem) li.next();
            total += item.getTotalCost();
        }
        return total;
    }

    /**
     * copies over the data from the specified shopping cart. Note
     * that it is a shallow copy.
     */

    public void copy(ShoppingCartModel src) {
        this.items = src.items;
    }
}

