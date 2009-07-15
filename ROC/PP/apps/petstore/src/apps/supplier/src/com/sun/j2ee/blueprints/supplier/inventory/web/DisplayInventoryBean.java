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

package com.sun.j2ee.blueprints.supplier.inventory.web;

import java.util.Collection;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.ejb.FinderException;

import com.sun.j2ee.blueprints.supplier.inventory.ejb.InventoryLocal;
import com.sun.j2ee.blueprints.supplier.inventory.ejb.InventoryLocalHome;

import com.sun.j2ee.blueprints.supplier.inventory.web.JNDINames;

import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import  com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

/**
 * This class has an accessor method that allows the "receiver" application
 * obtain all inventory items along with their current quantity. This is to
 * enable the receiver application to display the items and get them quantity
 * updated
 */

public class DisplayInventoryBean {

    private InventoryLocalHome inventoryHomeRef = null;

    public DisplayInventoryBean() {}

    /**
     * This method gets a list of the current items in the inventory along
     * with the current quantity
     * @return <Code>Collection</Code> of Items and their quantitites
     */
    public Collection getInventory() {
        Collection invColl = null;

        try {
            if(inventoryHomeRef == null) {
                ServiceLocator serviceLocator = new ServiceLocator();
                inventoryHomeRef = (InventoryLocalHome)
                             serviceLocator.getLocalHome(JNDINames.INV_EJB);
            }
            invColl = inventoryHomeRef.findAllInventoryItems();
        } catch(ServiceLocatorException ne) {
            ne.printStackTrace();
        } catch(FinderException fe) {
            fe.printStackTrace();
        }
        return invColl;
    }
}
