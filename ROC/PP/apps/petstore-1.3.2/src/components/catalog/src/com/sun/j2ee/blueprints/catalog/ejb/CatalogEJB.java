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

package com.sun.j2ee.blueprints.catalog.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;

import java.util.Locale;

import com.sun.j2ee.blueprints.catalog.dao.CatalogDAO;
import com.sun.j2ee.blueprints.catalog.dao.CatalogDAOFactory;
import com.sun.j2ee.blueprints.catalog.model.Page;
import com.sun.j2ee.blueprints.catalog.model.Category;
import com.sun.j2ee.blueprints.catalog.model.Product;
import com.sun.j2ee.blueprints.catalog.model.Item;

import com.sun.j2ee.blueprints.catalog.exceptions.CatalogDAOSysException;

import com.sun.j2ee.blueprints.util.tracer.Debug;

/**
 * Session Bean implementation of Catalog
 *
 */
public class CatalogEJB implements SessionBean {

    protected CatalogDAO dao;

    public void ejbCreate() {
        try {
            dao = CatalogDAOFactory.getDAO();
        }
        catch (CatalogDAOSysException se) {
            Debug.println("Exception getting dao " + se);
            throw new EJBException(se.getMessage());
        }
    }

    public void setSessionContext(SessionContext sc) {}
    public void ejbRemove() {}

    //activate is empty for StateLess Session EJBs
    public void ejbActivate() { }
    //passivate is empty for StateLess Session EJBs
    public void ejbPassivate() { }

    public void destroy() { dao = null; }

    public Category getCategory(String categoryID, Locale l) {
        try {
            return dao.getCategory(categoryID, l);
        }
        catch (CatalogDAOSysException se) {
            throw new EJBException(se.getMessage());
        }
    }

    public Page getCategories(int start, int count, Locale l) {
        try {
            return dao.getCategories(start, count, l);
        }
        catch (CatalogDAOSysException se) {
            throw new EJBException(se.getMessage());
        }
    }

    public Page getProducts(String categoryID, int start,
                            int count, Locale l) {
        try {
            return dao.getProducts(categoryID, start, count, l);
        }
        catch (CatalogDAOSysException se) {
            throw new EJBException(se.getMessage());
        }
    }

    public Product getProduct(String productID, Locale l) {
        try {
            return dao.getProduct(productID, l);
        }
        catch (CatalogDAOSysException se) {
            throw new EJBException(se.getMessage());
        }
    }

    public Page getItems(String productID, int start,
                         int count, Locale l) {
        try {
            return dao.getItems(productID, start, count, l);
        }
        catch (CatalogDAOSysException se) {
            throw new EJBException(se.getMessage());
        }
    }

    public Item getItem(String itemID, Locale l) {
        try {
            return dao.getItem(itemID, l);
        }
        catch (CatalogDAOSysException se) {
            throw new EJBException(se.getMessage());
        }
    }

    public Page searchItems(String searchQuery, int start,
                            int count, Locale l) {
        try {
            return dao.searchItems(searchQuery, start, count, l);
        }
        catch (CatalogDAOSysException se) {
            throw new EJBException(se.getMessage());
        }
    }
}

