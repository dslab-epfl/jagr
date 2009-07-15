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

package com.sun.j2ee.blueprints.supplier.inventory.ejb;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;

/**
 * This is the Local Entity Bean class for the InventoryEJB
 */

public abstract class InventoryEJB implements EntityBean {

    private EntityContext ectx = null;

    /**
     * Accessor method to get container managed field itemId
     * @return String that represents the itemId
     */
    public abstract String getItemId();

    /**
     * setter method for setting the container managed field itemId
     * @param itemId a String that represents the itemId
     */
    public abstract void setItemId(String itemId);

    /**
     * Accessor method to get container managed field quantity
     * @return int that represents the quantity
     */
    public abstract int getQuantity();

    /**
     * setter method for setting the container managed field quantity
     * @param quantity an integer that represents the quantity
     */
    public abstract void setQuantity(int quantity);

    /**
     * This method allows reducing the quantity by a given amount
     * @param quantity an integer that represents the amount by which the
     *                 quantity has to be reduced
     */
    public void reduceQuantity(int quantity) {
        int q = this.getQuantity();
        setQuantity(q-quantity);
    }

    /**
     * This method allows increasing the quantity by a given amount
     * @param quantity an integer that represents the amount by which the
     *                 quantity has to be increased

    public void addQuantity(int quantity) {
        int q = this.getQuantity();
        setQuantity(q+quantity);
    }
    */

    /**
     * THE ejbCreate method
     * @param itemId a String that represents the itemId.
     * @param quantity an integer that represents the amount of items present
     * @throws <Code>CreateException</Code> if create fails
     */
    public String ejbCreate(String itemId, int quantity)
                                              throws CreateException {
        setItemId(itemId);
        setQuantity(quantity);
        return(null);
    }

    /*
     * Other ejb life cycle methids
     */
    public void ejbPostCreate(String itemId, int quantity) throws CreateException {}
    public void setEntityContext(EntityContext c){ ectx = c; }
    public void unsetEntityContext(){}
    public void ejbRemove() throws RemoveException {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void ejbStore() {}
    public void ejbLoad() {}
}

