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

package com.sun.j2ee.blueprints.catalog.dao;

import java.util.Collection;
import java.util.Locale;

import com.sun.j2ee.blueprints.catalog.exceptions.CatalogDAOSysException;

import com.sun.j2ee.blueprints.catalog.model.Page;
import com.sun.j2ee.blueprints.catalog.model.Category;
import com.sun.j2ee.blueprints.catalog.model.Product;
import com.sun.j2ee.blueprints.catalog.model.Item;

/**
 * This class is an interface which will be implemented by database specific
 * code.
 * This class encapsulates all the SQL calls made by Catalog EJB.
 * This layer maps the relational data stored in the database to
 * the objects needed by Catalog EJB.
*/
public interface CatalogDAO {

    public Category getCategory(String categoryID, Locale l)
        throws CatalogDAOSysException;

    public Page getCategories(int start, int count, Locale l)
        throws CatalogDAOSysException;

    public Product getProduct(String productID, Locale l)
        throws CatalogDAOSysException;

    public Page getProducts(String categoryID, int start, int count, Locale l)
        throws CatalogDAOSysException;

    public Item getItem(String itemID, Locale l)
        throws CatalogDAOSysException;

    public Page getItems(String productID, int start, int size, Locale l)
        throws CatalogDAOSysException;

    public Page searchItems(String query, int start, int size, Locale l)
        throws CatalogDAOSysException;
}

