/*
 * $Id: DatabaseNames.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.shoppingcart.util;

/**
 * This interface stores the name of all the database tables.
 * The String constants in this class should be used by other
 * classes instead of hardcoding the name of a database table
 * into the source code.
 */

import java.util.Locale;

public class DatabaseNames {

    public static final String PRODUCT_TABLE   = "product";
    public static final String CATEGORY_TABLE  = "category";
    public static final String ITEM_TABLE      = "item";

    public static String getTableName(String tableName, Locale locale) {
        if (locale.equals(Locale.US)) {
            return tableName;
        } else if (locale.equals(Locale.JAPAN)) {
            return tableName + "_ja";
        }
        return tableName;
    }

}
