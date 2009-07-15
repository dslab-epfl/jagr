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

package com.sun.j2ee.blueprints.catalog.client;

import java.util.Locale;

//j2ee imports
import javax.naming.NamingException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;

// service locator imports
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

// catalog component imports
import com.sun.j2ee.blueprints.catalog.util.JNDINames;
import com.sun.j2ee.blueprints.catalog.ejb.CatalogLocalHome;
import com.sun.j2ee.blueprints.catalog.ejb.CatalogLocal;
import com.sun.j2ee.blueprints.catalog.dao.CatalogDAO;
import com.sun.j2ee.blueprints.catalog.dao.CatalogDAOFactory;
import com.sun.j2ee.blueprints.catalog.exceptions.CatalogDAOSysException;
import com.sun.j2ee.blueprints.catalog.model.Page;
import com.sun.j2ee.blueprints.catalog.model.Category;
import com.sun.j2ee.blueprints.catalog.model.Product;
import com.sun.j2ee.blueprints.catalog.model.Item;


/**
 * This helper class makes calls to the local Catalog EJB or
 * access the catalog directly over JDBC
 *<br><br>
 * See the Fast Lane Reader pattern for more details.
 */
public class CatalogHelper implements java.io.Serializable {

    private CatalogDAO dao;

    private boolean useFastLane = false;
    private String searchQuery = "";
    private String categoryId = "";
    private String productId = "";
    private String itemId = "";
    private Locale locale;
    private int count = 2;
    private int start = 0;

    public CatalogHelper(boolean useFastLane) {
        this.useFastLane = useFastLane;
    }

    public CatalogHelper() {
        useFastLane = true;
    }

    /**
     * Bean setter method to set Locale as a String prior to calling getSearchItems(),
     *   getCategories(), getCategory(),  getProduct(), and getItem()
     */
    public void setLocale(String localeString) {
        locale = getLocaleFromString(localeString);
    }

   /**
     * Bean setter method to set prefered return count prior to calling getSearchItems(),
     *   getCategory(), and getProduct()
     */
    public void setCount(String countString) {
        this.count = (new Integer(countString)).intValue();
    }

  /**
     * Bean setter method to set start index prior to calling getSearchItems(),
     *   getCategory(), and getProduct()
     */
    public void setStart(String startString) {
        if (startString != null) {
            this.start = (new Integer(startString)).intValue();
        } else {
            start = 0;
        }
    }

   /**
     * Bean setter method to be set categoryId prior to calling getCategories()
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

   /**
     * Bean setter method to be set productId prior to calling getProduct()
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

   /**
     * Bean setter method to be set itemId prior to calling getItem()
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

   /**
     * Bean setter method to be set prior to calling getSearchItems()
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * Perform a search with the Bean set  of searchQuery, the start, count, and Locale as a String
     */
    public Page getSearchItems()
        throws CatalogException {

        return useFastLane
            ? searchItemsFromDAO(searchQuery, start, count, locale)
            : searchItemsFromEJB(searchQuery, start, count, locale);
    }


    /**
     * Uses the CatalogEJB to search with the specified searchQuery, the start, count, and Locale as a String
     */
    public Page searchItems(String searchQuery, int start, int count, String localeString)
        throws CatalogException {
        Locale locale = getLocaleFromString(localeString);
        return useFastLane
            ? searchItemsFromDAO(searchQuery, start, count, locale)
            : searchItemsFromEJB(searchQuery, start, count, locale);
    }

    private Page searchItemsFromEJB(String searchQuery, int start, int count, Locale locale)
        throws CatalogException {
        return getCatalogEJB().searchItems(searchQuery, start, count,locale);
    }

    private Page searchItemsFromDAO(String searchQuery, int start, int count, Locale locale)
        throws CatalogException {
        try {
            if (dao == null)
                dao = CatalogDAOFactory.getDAO();
            return dao.searchItems(searchQuery, start, count,
                                   locale);
        } catch (CatalogDAOSysException se) {
            throw new CatalogException(se.getMessage());
        }
    }

    public Page getCategories(int start, int count, String localeString)
        throws CatalogException {
        Locale locale = getLocaleFromString(localeString);
        return useFastLane
            ? getCategoriesFromDAO(start, count, locale)
            : getCategoriesFromEJB(start, count, locale);
    }

    /**
     *Gets the categories using the Bean Set start, count, and locale
     */
    public Page getCategories()
        throws CatalogException {
        return useFastLane
            ? getCategoriesFromDAO(start, count, locale)
            : getCategoriesFromEJB(start, count, locale);
    }

    private Page getCategoriesFromDAO(int start, int count, Locale locale)
        throws CatalogException {
        try {
            if (dao == null)
                dao = CatalogDAOFactory.getDAO();
            return dao.getCategories(start, count, locale);
        } catch (CatalogDAOSysException se) {
            throw new CatalogException(se.getMessage());
        }
    }

    /**
     * Uses the CatalogEJB to get a list of all the products in a category
     */
    private Page getCategoriesFromEJB(int start, int count, Locale locale)
        throws CatalogException {
        return getCatalogEJB().getCategories(start, count, locale);
    }

    /**
     * Gets the categories using the Bean Set start, count, and locale
     */
    public Page getProducts()
        throws CatalogException {
        return useFastLane
            ? getProductsFromDAO(categoryId, start, count, locale)
            : getProductsFromEJB(categoryId, start, count,locale);
    }


    public Page getProducts(String categoryId, int start, int count, String localeString)
        throws CatalogException {
         Locale locale = getLocaleFromString(localeString);
        return useFastLane
            ? getProductsFromDAO(categoryId, start, count, locale)
            : getProductsFromEJB(categoryId, start, count,locale);
    }

    private Page getProductsFromEJB(String categoryId, int start, int count, Locale locale)
        throws CatalogException {
        return getCatalogEJB().getProducts(categoryId, start, count, locale);
    }

    private Page getProductsFromDAO(String categoryId, int start, int count, Locale locale)
        throws CatalogException {
        try {
            if (dao == null)
                dao = CatalogDAOFactory.getDAO();
            return dao.getProducts(categoryId, start, count, locale);
        } catch (CatalogDAOSysException se) {
            throw new CatalogException(se.getMessage());
        }
    }

    /**
     * Get the items using the Bean set productId, start, count, and locale
     */
    public Page getItems()
        throws CatalogException {
        return useFastLane
            ? getItemsFromDAO(productId, start, count, locale)
            : getItemsFromEJB(productId, start, count, locale);
    }


    public Page getItems(String productId, int start, int count, String localeString)
        throws CatalogException {
        Locale locale = getLocaleFromString(localeString);
        return useFastLane
            ? getItemsFromDAO(productId, start, count, locale)
            : getItemsFromEJB(productId, start, count, locale);
    }

    private Page getItemsFromEJB(String productId, int start, int count, Locale locale)
        throws CatalogException {
            return getCatalogEJB().getItems(productId, start, count, locale);

    }

    private Page getItemsFromDAO(String productId, int start, int count, Locale locale)
        throws CatalogException {
        try {
            if (dao == null)
                dao = CatalogDAOFactory.getDAO();
            return dao.getItems(productId, start, count, locale);
        } catch (CatalogDAOSysException se) {
            throw new CatalogException(se.getMessage());
        }
    }

    /**
     * Get the item using the Bean set itemId and Locale
     */
    public Item getItem() throws CatalogException {
        return useFastLane ? getItemFromDAO(itemId,locale) : getItemFromEJB(itemId,locale);
    }

    /**
     * Get the item using the specified itemId and Locale object
     */
    public Item getItem(String itemId, Locale locale) throws CatalogException {
        return useFastLane ? getItemFromDAO(itemId,locale) : getItemFromEJB(itemId,locale);
    }

    /**
     * Get the item using the speicifed itemId and locale as a Sting
     */
    public Item getItem(String itemId, String localeString) throws CatalogException {
        Locale locale = getLocaleFromString(localeString);
        return useFastLane ? getItemFromDAO(itemId,locale) : getItemFromEJB(itemId,locale);
    }

    private Item getItemFromDAO(String itemId, Locale locale) throws CatalogException {
        try {
            if (dao == null)
                dao = CatalogDAOFactory.getDAO();
            return dao.getItem(itemId, locale);
        } catch (CatalogDAOSysException se) {
            throw new CatalogException(se.getMessage());
        }
    }

    private Item getItemFromEJB(String itemId, Locale locale) throws CatalogException {
        return getCatalogEJB().getItem(itemId, locale);
    }

    /*
     * Use the Service locator pattern to located the Catalog Home and use the home
     * to create an instance of the CatalogLocale EJB.
     */

    private CatalogLocal getCatalogEJB() throws CatalogException {
        try {
            ServiceLocator sl = new ServiceLocator();
            CatalogLocalHome home =(CatalogLocalHome)sl.getLocalHome(JNDINames.CATALOG_EJBHOME);
            return home.create();
        } catch (javax.ejb.CreateException cx) {
                throw new CatalogException("CatalogHelper: failed to create CatalogLocal EJB: caught " + cx);
        } catch (ServiceLocatorException slx) {
                throw new CatalogException("CatalogHelper: failed to look up Catalog Home: caught " + slx);
        }
    }

    /**
     * Convert a string based locale into a Locale Object
     * <br>
     * <br>Strings are formatted:
     * <br>
     * <br>language_contry_variant
     *
     **/

    private Locale getLocaleFromString(String localeString) {
        if (localeString == null) return null;
        if (localeString.toLowerCase().equals("default")) return Locale.getDefault();
        int languageIndex = localeString.indexOf('_');
        if (languageIndex  == -1) return null;
        int countryIndex = localeString.indexOf('_', languageIndex +1);
        String country = null;
        if (countryIndex  == -1) {
            if (localeString.length() > languageIndex) {
                country = localeString.substring(languageIndex +1, localeString.length());
            } else {
                return null;
            }
        }
        int variantIndex = -1;
        if (countryIndex != -1) countryIndex = localeString.indexOf('_', countryIndex +1);
        String language = localeString.substring(0, languageIndex);
        String variant = null;
        if (variantIndex  != -1) {

            variant = localeString.substring(variantIndex +1, localeString.length());
        }
        if (variant != null) {
            return new Locale(language, country, variant);
        } else {
            return new Locale(language, country);
        }
    }
}

