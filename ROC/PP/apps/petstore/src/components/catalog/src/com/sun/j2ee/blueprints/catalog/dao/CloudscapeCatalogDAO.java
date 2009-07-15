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

import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;

// catalog imports

import com.sun.j2ee.blueprints.catalog.util.JNDINames;
import com.sun.j2ee.blueprints.catalog.model.Page;
import com.sun.j2ee.blueprints.catalog.model.Category;
import com.sun.j2ee.blueprints.catalog.model.Product;
import com.sun.j2ee.blueprints.catalog.model.Item;
import com.sun.j2ee.blueprints.catalog.util.DatabaseNames;
import com.sun.j2ee.blueprints.catalog.exceptions.CatalogDAOSysException;

// service locator imports
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

/**
 * This class implements CatalogDAO for cloudscape DB.
 * This class encapsulates all the SQL calls made by Catalog EJB.
 * This layer maps the relational data stored in the database to
 * the objects needed by Catalog EJB.
*/
public class CloudscapeCatalogDAO implements CatalogDAO {
  public static String GET_CATEGORY_STATEMENT
  = "select name, descn "
  + " from (category a join category_details b on a.catid=b.catid) "
  + " where locale = ? and a.catid = ?";
  public static String GET_CATEGORIES_STATEMENT
  = "select a.catid, name, descn "
  + " from (category a join category_details b on a.catid=b.catid) "
  + " where locale = ? order by name";
  public static String GET_PRODUCT_STATEMENT
  = "select name, descn "
  + " from (product a join product_details b on a.productid=b.productid) "
  + " where locale = ? and a.productid = ? ";
  public static String GET_PRODUCTS_STATEMENT
  = "select a.productid, name, descn "
  + " from (product a join product_details b on a.productid=b.productid) "
  + " where locale = ? and a.catid = ? order by name";
  public static String GET_ITEM_STATEMENT
  = "select catid, a.productid, name, b.image, b.descn, attr1, "
  + "  attr2, attr3, attr4, attr5, listprice, unitcost "
  + " from (((item a join item_details b on a.itemid=b.itemid)"
  + "   join product_details c on a.productid=c.productid)"
  + "   join product d on d.productid = c.productid and b.locale = c.locale) "
  + " where b.locale = ? and a.itemid = ?";
  public static String GET_ITEMS_STATEMENT
  = "select catid, name, a.itemid, b.image, b.descn, attr1, "
  + "  attr2, attr3, attr4, attr5, listprice, unitcost "
  + " from (((item a join item_details b on a.itemid=b.itemid)"
  + "  join product_details c on a.productid=c.productid)"
  + "  join product d on d.productid=c.productid and b.locale = c.locale)"
  + " where b.locale = ? and a.productid = ?";
  public static String[] SEARCH_ITEMS_STATEMENT_FRAGMENTS
  = { "select catid, a.productid, name, a.itemid, b.image, b.descn, attr1,"
      + "  attr2, attr3, attr4, attr5, listprice, unitcost"
      + " from (((item a join item_details b on a.itemid=b.itemid)"
      + "  join product_details c on a.productid=c.productid)"
      + "  join product d on d.productid=c.productid and b.locale = c.locale)"
      + " where b.locale = ? ",
      "    and ((lower(name) like ? ",
      "          or lower(name) like ? ",
      "    ) or (lower(catid) like ? ",
      "          or lower(catid) like ? ",
      "    ) or (lower(b.descn) like ? ",
      "          or lower(b.descn) like ? ",
      ") )"
  };

  // Helper methods

  protected static DataSource getDataSource() throws CatalogDAOSysException {
      try {
          ServiceLocator sl = new ServiceLocator();
          return (DataSource) sl.getDataSource(JNDINames.CATALOG_DATASOURCE);
      } catch (ServiceLocatorException slx) {
                throw new CatalogDAOSysException("NamingException while looking up DB context : " +
                                       slx.getMessage());
      }
  }

  // Business methods

  public Category getCategory(String categoryID, Locale l)
    throws CatalogDAOSysException {
      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Category ret = null;

      try {
        c = getDataSource().getConnection();
        ps = c.prepareStatement(GET_CATEGORY_STATEMENT,
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, l.toString());
        ps.setString(2, categoryID);
        rs = ps.executeQuery();
        if (rs.first()) {
          ret = new Category(categoryID, rs.getString(1), rs.getString(2));
        }

        rs.close();
        ps.close();
        c.close();
        return ret;
      } catch (SQLException se) {
        throw new CatalogDAOSysException("SQLException: " + se.getMessage());
      }
  }

  public Page getCategories(int start, int count, Locale l)
    throws CatalogDAOSysException {
      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Page ret = null;

      try {
        c = getDataSource().getConnection();
        ps = c.prepareStatement(GET_CATEGORIES_STATEMENT,
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, l.toString());
        rs = ps.executeQuery();
        if (start >= 0 && rs.absolute(start+1)) {
          boolean hasNext = false;
          List items = new ArrayList();
          do {
            items.add(new Category(rs.getString(1).trim(),
                                   rs.getString(2),
                                   rs.getString(3)));
          } while ((hasNext = rs.next()) && (--count > 0));
          ret = new Page(items, start, hasNext);
        } else {
          ret = Page.EMPTY_PAGE;
        }

        rs.close();
        ps.close();
        c.close();
        return ret;
      } catch (SQLException se) {
        se.printStackTrace(System.err);
        throw new CatalogDAOSysException("SQLException: " + se.getMessage());
      }
  }

  public Product getProduct(String productID, Locale l)
    throws CatalogDAOSysException {
      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Product ret = null;

      try {
        c = getDataSource().getConnection();
        ps = c.prepareStatement(GET_PRODUCT_STATEMENT,
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, l.toString());
        ps.setString(2, productID);
        rs = ps.executeQuery();
        if (rs.first()) {
          ret = new Product(productID, rs.getString(1), rs.getString(2));
        }

        rs.close();
        ps.close();
        c.close();
        return ret;
      } catch (SQLException se) {
        throw new CatalogDAOSysException("SQLException: " + se.getMessage());
      }
  }

  public Page getProducts(String categoryID, int start, int count, Locale l)
    throws CatalogDAOSysException {
      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Page ret = null;

      try {
        c = getDataSource().getConnection();
        ps = c.prepareStatement(GET_PRODUCTS_STATEMENT,
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, l.toString());
        ps.setString(2, categoryID);
        rs = ps.executeQuery();
        if (start >= 0 && rs.absolute(start+1)) {
          boolean hasNext = false;
          List items = new ArrayList();
          do {
            items.add(new Product(rs.getString(1).trim(),
                                  rs.getString(2).trim(),
                                  rs.getString(3).trim()));
          } while ((hasNext = rs.next()) && (--count > 0));
          ret = new Page(items, start, hasNext);
        } else {
          ret = Page.EMPTY_PAGE;
        }

        rs.close();
        ps.close();
        c.close();
        return ret;
      } catch (SQLException se) {
        throw new CatalogDAOSysException("SQLException: " + se.getMessage());
      }
  }

  public Item getItem(String itemID, Locale l)
    throws CatalogDAOSysException {
      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Item ret = null;

      try {
        c = getDataSource().getConnection();
        ps = c.prepareStatement(GET_ITEM_STATEMENT,
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, l.toString());
        ps.setString(2, itemID);
        rs = ps.executeQuery();
        if (rs.first()) {
          int i = 1;
          ret = new Item(rs.getString(i++).trim(),
                         rs.getString(i++).trim(),
                         rs.getString(i++),
                         itemID,
                         rs.getString(i++).trim(),
                         rs.getString(i++),
                         rs.getString(i++),
                         rs.getString(i++),
                         rs.getString(i++),
                         rs.getString(i++),
                         rs.getString(i++),
                         rs.getDouble(i++),
                         rs.getDouble(i++));
        }

        rs.close();
        ps.close();
        c.close();
        return ret;
      } catch (SQLException se) {
        throw new CatalogDAOSysException("SQLException: " + se.getMessage());
      }
  }

  public Page getItems(String productID, int start, int count, Locale l)
    throws CatalogDAOSysException {
      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Page ret = null;

      try {
        c = getDataSource().getConnection();
        ps = c.prepareStatement(GET_ITEMS_STATEMENT,
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, l.toString());
        ps.setString(2, productID);
        rs = ps.executeQuery();
        if (start >= 0 && rs.absolute(start+1)) {
          boolean hasNext = false;
          List items = new ArrayList();
          do {
            int i = 1;
            items.add(new Item(productID,
                               rs.getString(i++).trim(),
                               rs.getString(i++),
                               rs.getString(i++).trim(),
                               rs.getString(i++).trim(),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getDouble(i++),
                               rs.getDouble(i++)));
          } while ((hasNext = rs.next()) && (--count > 0));
          ret = new Page(items, start, hasNext);
        } else {
          ret = Page.EMPTY_PAGE;
        }

        rs.close();
        ps.close();
        c.close();
        return ret;
      } catch (SQLException se) {
        throw new CatalogDAOSysException("SQLException: " + se.getMessage());
      }
  }

  public Page searchItems(String searchQuery, int start, int count, Locale l)
    throws CatalogDAOSysException {
      Collection keywords = new HashSet();
      StringTokenizer st = new StringTokenizer(searchQuery);
      while (st.hasMoreTokens()) {
        keywords.add(st.nextToken());
      }
      if (keywords.isEmpty()) {
        return Page.EMPTY_PAGE;
      }

      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Page ret = null;

      try {
        c = getDataSource().getConnection();
        Iterator it;
        int i;
        StringBuffer sb = new StringBuffer();
        sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[0]);
        int keywordsSize = keywords.size();
        if (keywordsSize > 0) {
          sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[1]);
          for (i = 1; i != keywordsSize; i++) {
            sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[2]);
          }
          sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[3]);
          for (i = 1; i != keywordsSize; i++) {
            sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[4]);
          }
          sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[5]);
          for (i = 1; i != keywordsSize; i++) {
            sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[6]);
          }
          sb.append(SEARCH_ITEMS_STATEMENT_FRAGMENTS[7]);
        }
        //System.err.println(sb.toString());
        ps = c.prepareStatement(sb.toString(),
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, l.toString());
        // The three loops are necessary because of the way the
        // query was constructed.
        i = 2;
        for (it = keywords.iterator(); it.hasNext(); i++) {
          String keyword = ((String) it.next()).toLowerCase();
          ps.setString(i, "%" + keyword + "%");
        }
        for (it = keywords.iterator(); it.hasNext(); i++) {
          String keyword = ((String) it.next()).toLowerCase();
          ps.setString(i, "%" + keyword + "%");
        }
        for (it = keywords.iterator(); it.hasNext(); i++) {
          String keyword = ((String) it.next()).toLowerCase();
          ps.setString(i, "%" + keyword + "%");
        }
        rs = ps.executeQuery();
        if (start >= 0 && rs.absolute(start+1)) {
          boolean hasNext = false;
          List items = new ArrayList();
          do {
            i = 1;
            items.add(new Item(rs.getString(i++).trim(),
                               rs.getString(i++).trim(),
                               rs.getString(i++),
                               rs.getString(i++).trim(),
                               rs.getString(i++).trim(),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getString(i++),
                               rs.getDouble(i++),
                               rs.getDouble(i++)));
          } while ((hasNext = rs.next()) && (--count > 0));
          ret = new Page(items, start, hasNext);
        } else {
          ret = Page.EMPTY_PAGE;
        }

        rs.close();
        ps.close();
        c.close();
        return ret;
      } catch (SQLException se) {
        throw new CatalogDAOSysException("SQLException: " + se.getMessage());
      }
  }
}

