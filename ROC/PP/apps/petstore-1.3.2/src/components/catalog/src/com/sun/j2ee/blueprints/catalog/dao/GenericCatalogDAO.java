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
import java.net.*;
import java.io.*;
import javax.naming.*;
import javax.sql.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.sun.j2ee.blueprints.catalog.util.JNDINames;
import com.sun.j2ee.blueprints.catalog.model.Page;
import com.sun.j2ee.blueprints.catalog.model.Category;
import com.sun.j2ee.blueprints.catalog.model.Product;
import com.sun.j2ee.blueprints.catalog.model.Item;
import com.sun.j2ee.blueprints.catalog.exceptions.CatalogDAOSysException;

import com.sun.j2ee.blueprints.util.tracer.Debug;


/**
 * This class implements a generic CatalogDAO which loads the SQL statement
 * descriptions fron an XML configuration file.
 * This class encapsulates all the SQL calls made by Catalog EJB.
 * This layer maps the relational data stored in the database to
 * the objects needed by Catalog EJB.
*/
public class GenericCatalogDAO implements CatalogDAO {
  private static final boolean TRACE = false;
  private static final String XML_DAO_CONFIGURATION = "DAOConfiguration";
  private static final String XML_DAO_STATEMENTS = "DAOStatements";
  private static final String XML_DATABASE = "database";
  private static final String XML_SQL_STATEMENT = "SQLStatement";
  private static final String XML_METHOD = "method";
  private static final String XML_SQL_FRAGMENT = "SQLFragment";
  private static final String XML_PARAMETER_NB = "parameterNb";
  private static final String XML_OCCURRENCE = "occurrence";
  private static final String XML_ONCE = "ONCE";
  private static final String XML_VARIABLE = "VARIABLE";
  private static final String XML_GET_CATEGORY = "GET_CATEGORY";
  private static final String XML_GET_CATEGORIES = "GET_CATEGORIES";
  private static final String XML_GET_PRODUCT = "GET_PRODUCT";
  private static final String XML_GET_PRODUCTS = "GET_PRODUCTS";
  private static final String XML_GET_ITEM = "GET_ITEM";
  private static final String XML_GET_ITEMS = "GET_ITEMS";
  private static final String XML_SEARCH_ITEMS = "SEARCH_ITEMS";
  private Map sqlStatements = new HashMap();


  public GenericCatalogDAO() throws CatalogDAOSysException {
    try {
      InitialContext context = new InitialContext();
      URL daoSQLURL = (URL) context.lookup(JNDINames.CATALOG_DAO_SQL_URL);
      String database = (String) context.lookup(JNDINames.CATALOG_DAO_DATABASE);
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setValidating(true);
      parserFactory.setNamespaceAware(true);
      XMLReader reader = parserFactory.newSAXParser().getXMLReader();
      loadSQLStatements(parserFactory.newSAXParser(), database,
                        new InputSource(daoSQLURL.openStream()));
      if (TRACE) {
        System.err.println("DAO SQL statements used: " + sqlStatements);
      }
    } catch (Exception exception) {
      System.err.println(exception);
      throw new CatalogDAOSysException(exception.getMessage());
    }
    return;
  }

  private GenericCatalogDAO(String daoSQLFileName, String database) throws CatalogDAOSysException {
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setValidating(true);
      parserFactory.setNamespaceAware(true);
      XMLReader reader = parserFactory.newSAXParser().getXMLReader();
      loadSQLStatements(parserFactory.newSAXParser(), database, new InputSource(daoSQLFileName));
      if (TRACE) {
        System.err.println("DAO SQL statements used: " + sqlStatements);
      }
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      System.err.println(exception);
      throw new CatalogDAOSysException(exception.getMessage());
    }
    return;
  }

  protected static DataSource getDataSource() throws CatalogDAOSysException {
    try {
      InitialContext context = new InitialContext();
      return (DataSource) context.lookup(JNDINames.CATALOG_DATASOURCE);
    } catch (NamingException exception) {
      throw new CatalogDAOSysException("NamingException while looking up DB context : " +
                                       exception.getMessage());
    }
  }

  protected static void closeAll(Connection connection, PreparedStatement statement, ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (Exception exception) {}
    }
    if (statement != null) {
      try {
        statement.close();
      } catch (Exception exception) {}
    }
    if (connection != null) {
      try {
        connection.close();
      } catch (Exception exception) {}
    }
    return;
  }

  // Business methods

  public Category getCategory(String categoryID, Locale locale) throws CatalogDAOSysException {
    Connection connection = null;
    ResultSet resultSet = null;
    PreparedStatement statement = null;
    try {
      connection = getDataSource().getConnection();
      String[] parameterValues = new String[] { locale.toString(), categoryID };
      if (TRACE) {
        printSQLStatement(sqlStatements, XML_GET_CATEGORY, parameterValues);
      }
      statement = buildSQLStatement(connection, sqlStatements, XML_GET_CATEGORY, parameterValues);
      resultSet = statement.executeQuery();
      if (resultSet.first()) {
        return new Category(categoryID, resultSet.getString(1), resultSet.getString(2));
      }
      return null;
    } catch (SQLException exception) {
      throw new CatalogDAOSysException("SQLException: " + exception.getMessage());
    } finally {
      closeAll(connection, statement, resultSet);
    }
  }

  public Page getCategories(int start, int count, Locale locale) throws CatalogDAOSysException {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = getDataSource().getConnection();
      String[] parameterValues = new String[] { locale.toString() };
      if (TRACE) {
        printSQLStatement(sqlStatements, XML_GET_CATEGORIES, parameterValues);
      }
      statement = buildSQLStatement(connection, sqlStatements, XML_GET_CATEGORIES, parameterValues);
      resultSet = statement.executeQuery();
      if (start >= 0 && resultSet.absolute(start + 1)) {
        boolean hasNext = false;
        List categories = new ArrayList();
        do {
          categories.add(new Category(resultSet.getString(1).trim(),
                                      resultSet.getString(2),
                                      resultSet.getString(3)));
        } while ((hasNext = resultSet.next()) && (--count > 0));
        return new Page(categories, start, hasNext);
      }
      return Page.EMPTY_PAGE;
    } catch (SQLException exception) {
      throw new CatalogDAOSysException("SQLException: " + exception.getMessage());
    } finally {
      closeAll(connection, statement, resultSet);
    }
  }

  public Product getProduct(String productID, Locale locale) throws CatalogDAOSysException {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = getDataSource().getConnection();
      String[] parameterValues = new String[] { locale.toString(), productID };
      if (TRACE) {
        printSQLStatement(sqlStatements, XML_GET_PRODUCT, parameterValues);
      }
      statement = buildSQLStatement(connection, sqlStatements, XML_GET_PRODUCT, parameterValues);
      resultSet = statement.executeQuery();
      if (resultSet.first()) {
        return new Product(productID, resultSet.getString(1), resultSet.getString(2));
      }
      return null;
    }
    catch (SQLException exception) {
      throw new CatalogDAOSysException("SQLException: " + exception.getMessage());
    } finally {
      closeAll(connection, statement, resultSet);
    }
  }

  public Page getProducts(String categoryID, int start, int count, Locale locale) throws CatalogDAOSysException {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = getDataSource().getConnection();
      String[] parameterValues = new String[] { locale.toString(), categoryID };
      if (TRACE) {
        printSQLStatement(sqlStatements, XML_GET_PRODUCTS, parameterValues);
      }
      statement = buildSQLStatement(connection, sqlStatements, XML_GET_PRODUCTS, parameterValues);
      resultSet = statement.executeQuery();
      if (start >= 0 && resultSet.absolute(start + 1)) {
        boolean hasNext = false;
        List products = new ArrayList();
        do {
          products.add(new Product(resultSet.getString(1).trim(),
                                   resultSet.getString(2),
                                   resultSet.getString(3)));
        } while ((hasNext = resultSet.next()) && (--count > 0));
        return new Page(products, start, hasNext);
      }
      return Page.EMPTY_PAGE;
    } catch (SQLException exception) {
      throw new CatalogDAOSysException("SQLException: " + exception.getMessage());
    } finally {
      closeAll(connection, statement, resultSet);
    }
  }

  public Item getItem(String itemID, Locale locale) throws CatalogDAOSysException {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = getDataSource().getConnection();
      String[] parameterValues = new String[] { locale.toString(), itemID };
      if (TRACE) {
        printSQLStatement(sqlStatements, XML_GET_ITEM, parameterValues);
      }
      statement = buildSQLStatement(connection, sqlStatements, XML_GET_ITEM, parameterValues);
      resultSet = statement.executeQuery();
      if (resultSet.first()) {
        int i = 1;
        return new Item(resultSet.getString(i++).trim(),
                        resultSet.getString(i++).trim(),
                        resultSet.getString(i++),
                        itemID,
                        resultSet.getString(i++).trim(),
                        resultSet.getString(i++),
                        resultSet.getString(i++),
                        resultSet.getString(i++),
                        resultSet.getString(i++),
                        resultSet.getString(i++),
                        resultSet.getString(i++),
                        resultSet.getDouble(i++),
                        resultSet.getDouble(i++));
      }
      return null;
    } catch (SQLException exception) {
      throw new CatalogDAOSysException("SQLException: " + exception.getMessage());
    } finally {
      closeAll(connection, statement, resultSet);
    }
  }

  public Page getItems(String productID, int start, int count, Locale locale) throws CatalogDAOSysException {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = getDataSource().getConnection();
      String[] parameterValues = new String[] { locale.toString(), productID };
      if (TRACE) {
        printSQLStatement(sqlStatements, XML_GET_ITEMS, parameterValues);
      }
      statement = buildSQLStatement(connection, sqlStatements, XML_GET_ITEMS, parameterValues);
      resultSet = statement.executeQuery();
      if (start >= 0 && resultSet.absolute(start + 1)) {
        boolean hasNext = false;
        List items = new ArrayList();
        do {
          int i = 1;
          items.add(new Item(productID,
                             resultSet.getString(i++).trim(),
                             resultSet.getString(i++),
                             resultSet.getString(i++).trim(),
                             resultSet.getString(i++).trim(),
                             resultSet.getString(i++),
                             resultSet.getString(i++),
                             resultSet.getString(i++),
                             resultSet.getString(i++),
                             resultSet.getString(i++),
                             resultSet.getString(i++),
                             resultSet.getDouble(i++),
                             resultSet.getDouble(i++)));
        } while ((hasNext = resultSet.next()) && (--count > 0));
        return new Page(items, start, hasNext);
      }
      return Page.EMPTY_PAGE;
    } catch (SQLException exception) {
      throw new CatalogDAOSysException("SQLException: " + exception.getMessage());
    } finally {
      closeAll(connection, statement, resultSet);
    }
  }

  public Page searchItems(String searchQuery, int start, int count, Locale locale)
    throws CatalogDAOSysException {
      Collection keywordSet = new HashSet();
      StringTokenizer tokenizer = new StringTokenizer(searchQuery);
      while (tokenizer.hasMoreTokens()) {
        keywordSet.add(tokenizer.nextToken());
      }
      if (keywordSet.isEmpty()) {
        return Page.EMPTY_PAGE;
      }
      String[] keywords = (String[]) keywordSet.toArray(new String[0]);
      Connection connection = null;
      PreparedStatement statement = null;
      ResultSet resultSet = null;
      try {
        connection = getDataSource().getConnection();
        String[] parameterValues = new String[1 + (keywords.length * 3)];
        parameterValues[0] = locale.toString();
        for (int i = 0; i < keywords.length; i++) {
          parameterValues[(i * 3) + 1] = "%" + keywords[i] + "%";
          parameterValues[(i * 3) + 2] = "%" + keywords[i] + "%";
          parameterValues[(i * 3) + 3] = "%" + keywords[i] + "%";
        }
        if (TRACE) {
          printSQLStatement(sqlStatements, XML_SEARCH_ITEMS, parameterValues);
        }
        statement = buildSQLStatement(connection, sqlStatements, XML_SEARCH_ITEMS, parameterValues);
        resultSet = statement.executeQuery();
        if (start >= 0 && resultSet.absolute(start + 1)) {
          boolean hasNext = false;
          List items = new ArrayList();
          do {
            int i = 1;
            items.add(new Item(resultSet.getString(i++).trim(),
                               resultSet.getString(i++).trim(),
                               resultSet.getString(i++),
                               resultSet.getString(i++).trim(),
                               resultSet.getString(i++).trim(),
                               resultSet.getString(i++),
                               resultSet.getString(i++),
                               resultSet.getString(i++),
                               resultSet.getString(i++),
                               resultSet.getString(i++),
                               resultSet.getString(i++),
                               resultSet.getDouble(i++),
                               resultSet.getDouble(i++)));
          } while ((hasNext = resultSet.next()) && (--count > 0));
          return new Page(items, start, hasNext);
        }
        return Page.EMPTY_PAGE;
      } catch (SQLException exception) {
        throw new CatalogDAOSysException("SQLException: " + exception.getMessage());
      } finally {
        closeAll(connection, statement, resultSet);
      }
  }

  private PreparedStatement buildSQLStatement(Connection connection, Map sqlStatements, String sqlStatementKey,
                                              String[] parameterValues)
    throws SQLException {
      Statement statement = (Statement) sqlStatements.get(sqlStatementKey);
      if (statement != null) {
        return buildSQLStatement(connection, statement, parameterValues);
      }
      return null;
  }

  private PreparedStatement buildSQLStatement(Connection connection, Statement sqlStatement, String[] parameterValues)
    throws SQLException {
      StringBuffer buffer = new StringBuffer();
      int totalParameterValueNb = parameterValues != null ? parameterValues.length : 0;
      for (int i = 0; i < sqlStatement.fragments.length; i++) {
        if (sqlStatement.fragments[i].variableOccurrence) {
          while (totalParameterValueNb > 0 && totalParameterValueNb >= sqlStatement.fragments[i].parameterNumber) {
            buffer.append(sqlStatement.fragments[i].text);
            totalParameterValueNb -= sqlStatement.fragments[i].parameterNumber;
          }
        } else {
          buffer.append(sqlStatement.fragments[i].text);
          totalParameterValueNb -= sqlStatement.fragments[i].parameterNumber;
        }
      }
      if (totalParameterValueNb > 0) {
        System.err.println("Number of values doesn't match number of parameters: " +
                           totalParameterValueNb + "/" + parameterValues.length);
      }
      PreparedStatement statement = connection.prepareStatement(buffer.toString(),
                                                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                ResultSet.CONCUR_READ_ONLY);
      if (parameterValues != null) {
        for (int i = 0; i < parameterValues.length; i++) {
          statement.setString(i + 1, parameterValues[i]);
        }
      }
      return statement;
  }

  private void printSQLStatement(Map sqlStatements, String sqlStatementKey, String[] parameterValues) {
    Statement statement = (Statement) sqlStatements.get(sqlStatementKey);
    if (statement != null) {
      printSQLStatement(statement, parameterValues);
    } else {
      System.err.println("No statement found for: " + sqlStatementKey);
    }
    return;
  }

  private void printSQLStatement(Statement sqlStatement, String[] parameterValues) {
    StringBuffer buffer = new StringBuffer();
    int totalParameterValueNb = parameterValues != null ? parameterValues.length : 0;
    for (int i = 0; i < sqlStatement.fragments.length; i++) {
      if (sqlStatement.fragments[i].variableOccurrence) {
        while (totalParameterValueNb > 0 && totalParameterValueNb >= sqlStatement.fragments[i].parameterNumber) {
          buffer.append(' ').append(sqlStatement.fragments[i].text);
          totalParameterValueNb -= sqlStatement.fragments[i].parameterNumber;
        }
      } else {
        buffer.append(' ').append(sqlStatement.fragments[i].text);
        totalParameterValueNb -= sqlStatement.fragments[i].parameterNumber;
      }
    }
    if (totalParameterValueNb > 0) {
      System.err.println("Number of values doesn't match number of parameters: " +
                         totalParameterValueNb + "/" + parameterValues.length);
    }
    StringTokenizer tokenizer = new StringTokenizer(buffer.toString(), "?", true);
    for (int i = 0; tokenizer.hasMoreTokens();) {
      String token = tokenizer.nextToken();
      if (token.equals("?")) {
        System.out.print("\'" + parameterValues[i++] + "\'");
      } else {
        System.out.print(token);
      }
    }
    System.out.println(";");
    return;
  }

  private static class ParsingDoneException extends SAXException {

    ParsingDoneException() {
      super("");
    }
  }

  private static class Statement {
    Fragment[] fragments;

    static class Fragment {
      boolean variableOccurrence = false;
      int parameterNumber = 0;
      String text;

      public String toString() {
        return new StringBuffer(text).append('/')
          .append(parameterNumber).append('/')
          .append(variableOccurrence).toString();
      }
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < fragments.length; i++) {
        buffer.append(fragments[i].toString()).append("\n\t");
      }
      return buffer.toString();
    }
  }

  private void loadSQLStatements(SAXParser parser, final String database, InputSource source)
    throws SAXException, IOException {
      try {
        parser.parse(source, new DefaultHandler() {
          private boolean foundEntry = false;
          private String operation = null;
          List fragments = new ArrayList();
          Statement.Fragment fragment;
          private StringBuffer buffer = new StringBuffer();

          public void startElement(String namespace, String name, String qName, Attributes attrs)
            throws SAXException {
            if (!foundEntry) {
              if (name.equals(XML_DAO_STATEMENTS) && attrs.getValue(XML_DATABASE).equals(database)) {
                foundEntry = true;
              }
            } else if (operation != null) {
              if (name.equals(XML_SQL_FRAGMENT)) {
                fragment = new Statement.Fragment();
                String value = attrs.getValue(XML_PARAMETER_NB);
                if (value != null) {
                  try {
                    fragment.parameterNumber = Integer.parseInt(value);
                  } catch (NumberFormatException exception) {
                    //throw new SAXException(exception);
                  }
                }
                value = attrs.getValue(XML_OCCURRENCE);
                fragment.variableOccurrence = (value != null && value.equals(XML_VARIABLE));
                buffer.setLength(0);
              }
            } else {
              if (name.equals(XML_SQL_STATEMENT)) {
                operation = attrs.getValue(XML_METHOD);
                fragments.clear();
              }
            }
            return;
          }

          public void characters(char[] chars, int start, int length) throws SAXException {
            if (foundEntry && operation != null) {
              buffer.append(chars, start, length);
            }
            return;
          }

          public void endElement(String namespace, String name, String qName) throws SAXException {
            if (foundEntry) {
              if (name.equals(XML_DAO_STATEMENTS)) {
                foundEntry = false;
                throw new ParsingDoneException(); // Interrupt the parsing since everything has been collected
              } else if (name.equals(XML_SQL_STATEMENT)) {
                Statement statement = new Statement();
                statement.fragments = (Statement.Fragment[]) fragments.toArray(new Statement.Fragment[0]);
                sqlStatements.put(operation, statement);
                operation = null;
              } else if (name.equals(XML_SQL_FRAGMENT)) {
                fragment.text = buffer.toString().trim();
                fragments.add(fragment);
                fragment = null;
              }
            }
            return;
          }

          public void warning(SAXParseException exception) {
            System.err.println("[Warning]: " + exception.getMessage());
            return;
          }

          public void error(SAXParseException exception) {
            System.err.println("[Error]: " + exception.getMessage());
            return;
          }

          public void fatalError(SAXParseException exception) throws SAXException {
            System.err.println("[Fatal Error]: " + exception.getMessage());
            throw exception;
          }
        });
      } catch (ParsingDoneException exception) {} // Ignored
      return;
  }

  public static void main(String[] args) {
        if (args.length <= 2) {
      try {
        GenericCatalogDAO catalogDAO = new GenericCatalogDAO(args[0], args[1]);
        String[] parameterValues = new String[] { "FR_fr", "Chien" };
        catalogDAO.printSQLStatement(catalogDAO.sqlStatements, XML_GET_CATEGORY, parameterValues);
        parameterValues = new String[] { "FR_fr" };
        catalogDAO.printSQLStatement(catalogDAO.sqlStatements, XML_GET_CATEGORIES, parameterValues);
        parameterValues = new String[] { "FR_fr", "Caniche" };
        catalogDAO.printSQLStatement(catalogDAO.sqlStatements, XML_GET_PRODUCT, parameterValues);
        parameterValues = new String[] { "FR_fr", "Chien" };
        catalogDAO.printSQLStatement(catalogDAO.sqlStatements, XML_GET_PRODUCTS, parameterValues);
        parameterValues = new String[] { "FR_fr", "Medor" };
        catalogDAO.printSQLStatement(catalogDAO.sqlStatements, XML_GET_ITEM, parameterValues);
        parameterValues = new String[] { "FR_fr", "Caniche" };
        catalogDAO.printSQLStatement(catalogDAO.sqlStatements, XML_GET_ITEMS, parameterValues);
        String[] keywords = { "Chien", "Chat", "Poisson" };
        parameterValues = new String[1 + (keywords.length * 2)];
        parameterValues[0] = "FR_fr";
        for (int i = 0; i < keywords.length; i++) {
          parameterValues[(i * 2) + 1] = keywords[i];
          parameterValues[(i * 2) + 2] = keywords[i];
        }
        catalogDAO.printSQLStatement(catalogDAO.sqlStatements, XML_SEARCH_ITEMS, parameterValues);
                System.exit(0);
      } catch (Exception exception) {
                exception.printStackTrace(System.err);
                System.err.println(exception);
                System.exit(2);
      }
        }
        System.err.println("Usage: " + GenericCatalogDAO.class.getName() + " [file-name] [databas-type]");
        System.exit(1);
  }
}

