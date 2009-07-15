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

package com.sun.j2ee.blueprints.petstore.tools.populate;

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import javax.sql.*;
import javax.xml.parsers.*;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.naming.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.sun.j2ee.blueprints.catalog.util.JNDINames;


public class PopulateServlet extends HttpServlet {
  private static final String POPULATE_SQL_PATH_PARAM = "PopulateSQL";
  private static final String POPULATE_DATA_PATH_PARAM = "PopulateData";
  private static final String DATABASE_PARAM = "Database";
  private static final String DEFAULT_POPULATE_SQL_PATH = "/populate/PopulateSQL.xml";
  private static final String DEFAULT_POPULATE_DATA_PATH = "/populate/Populate-UTF8.xml";
  private static final String DEFAULT_DATABASE = "cloudscape";
  private static final String SUCCESS_PAGE_URL_PARAM = "success_page";
  private static final String ERROR_PAGE_URL_PARAM = "error_page";
  private static final String FORCEFULLY_MODE_PARAM = "forcefully";
  private static final String REFERER_HEADER = "Referer";
  private static final String XML_DATABASE_STATEMENTS = "DatabaseStatements";
  private static final String XML_DATABASE = "database";
  private static final String XML_TABLE_STATEMENTS = "TableStatements";
  private static final String XML_TABLE = "table";
  private static final String XML_CREATE_STATEMENT = "CreateStatement";
  private static final String XML_INSERT_STATEMENT = "InsertStatement";
  private static final String XML_DROP_STATEMENT = "DropStatement";
  private static final String XML_CHECK_STATEMENT = "CheckStatement";
  private Map sqlStatements = new HashMap();
  private String populateDataPath;


  public void init(ServletConfig config) throws javax.servlet.ServletException {
    super.init(config);
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setValidating(true);
      parserFactory.setNamespaceAware(true);
      String database = config.getInitParameter(DATABASE_PARAM);
      if (database == null) {
        database = DEFAULT_DATABASE;
      }
      populateDataPath = config.getInitParameter(POPULATE_DATA_PATH_PARAM);
      if (populateDataPath == null) {
        populateDataPath = DEFAULT_POPULATE_DATA_PATH;
      }
      String populateSQLPath = config.getInitParameter(POPULATE_SQL_PATH_PARAM);
      if (populateSQLPath == null) {
        populateSQLPath = DEFAULT_POPULATE_SQL_PATH;
      }
      loadSQLStatements(parserFactory.newSAXParser(), database,
                        new InputSource(getResource(populateSQLPath)));
      //System.err.println("SQL statements used: " + sqlStatements);
    } catch (Exception exception) {
      throw new ServletException(exception);
    }
    return;
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws java.io.IOException, javax.servlet.ServletException {
      doPost(request, response);
      return;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String successPageURL = request.getParameter(SUCCESS_PAGE_URL_PARAM);
    String errorPageURL = request.getParameter(ERROR_PAGE_URL_PARAM);
    String forcefully = request.getParameter(FORCEFULLY_MODE_PARAM);
    String refererURL = request.getHeader(REFERER_HEADER);
    // kill the Servlet Session because all EJB Refs are no longer valid
    request.getSession().invalidate();
    try {
      populate(forcefully != null && Boolean.valueOf(forcefully).booleanValue());
    } catch(PopulateException exception) {
      System.err.println(exception.getRootCause().getMessage());
      if (errorPageURL == null) {
        throw new ServletException("Populate exception occured :" + exception.getMessage(), exception.getRootCause());
      } else {
        redirect(request, response, errorPageURL);
      }
    }
    if (successPageURL != null) {
      redirect(request, response, successPageURL);
    } else if (refererURL != null) {
      redirect(request, response, refererURL);
    }
    return;
  }

  private boolean populate(boolean forcefully) throws PopulateException {
    XMLReader reader = null;
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setValidating(true);
      parserFactory.setNamespaceAware(true);
      reader = parserFactory.newSAXParser().getXMLReader();
    } catch (Exception exception) {
      throw new PopulateException(exception);
    }
    Connection connection = null;
    boolean alreadyPopulated = false;
    try {
      connection = getConnection();
      CatalogPopulator catalogPopulator = new CatalogPopulator(sqlStatements);
      CustomerPopulator customerPopulator = new CustomerPopulator();
      UserPopulator userPopulator = new UserPopulator();
      if (!forcefully) {
        try {
          alreadyPopulated = catalogPopulator.check(connection) && customerPopulator.check() && userPopulator.check();
        } catch (PopulateException exception) {}
        //System.err.println("Already populated: " + alreadyPopulated);
      }
      if (forcefully || !alreadyPopulated) {
        catalogPopulator.dropTables(connection);
        catalogPopulator.createTables(connection);
        try {
          catalogPopulator.setup(customerPopulator.setup(userPopulator.setup(reader)), connection)
            .parse(new InputSource(getResource(populateDataPath)));
          //.parse(new InputSource(getServletConfig().getServletContext().getRealPath(populateDataPath)));
        } catch (Exception exception) {
          throw new PopulateException(exception);
        }
      }
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception exception) {}
    }
    return forcefully || !alreadyPopulated;
  }

  protected Connection getConnection() throws PopulateException {
    try {
      InitialContext context = new InitialContext();
      return ((DataSource) context.lookup(JNDINames.CATALOG_DATASOURCE)).getConnection();
    } catch (Exception exception) {
      throw new PopulateException("Can't get catalog data source connection", exception);
    }
  }

  private void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
    String url;
    if (path.startsWith("//")) {
      url = new URL(new URL(HttpUtils.getRequestURL(request).toString()), path.substring(1)).toString();
    } else {
      url = request.getContextPath() + (path.startsWith("/") ? path : "/" + path);
    }
    //System.err.println("redirecting to: " + url);
    response.sendRedirect(url);
    return;
  }

  private String getResource(String path) throws IOException {
    String url;
    try {
      url = new URL(path).toString();
    } catch (MalformedURLException exception) {
      URL u = getServletContext().getResource(path);
      url = u != null ? u.toString() : path;
    }
    //System.err.println("Made " + url + " from " + path);
    return url;
  }

  private class ParsingDoneException extends SAXException {

    ParsingDoneException() {
      super("");
    }
  }

  private void loadSQLStatements(SAXParser parser, final String database, InputSource source) throws SAXException, IOException {
    try {
      parser.parse(source, new DefaultHandler() {
        private boolean foundEntry = false;
        private String table = null;
        private String operation = null;
        private StringBuffer statement = new StringBuffer();

        public void startElement(String namespace, String name, String qName, Attributes attrs) {
          if (!foundEntry) {
            if (name.equals(XML_DATABASE_STATEMENTS) && attrs.getValue(XML_DATABASE).equals(database)) {
              foundEntry = true;
            }
          } else {
            if (name.equals(XML_TABLE_STATEMENTS)) {
              table = attrs.getValue(XML_TABLE);
            } else {
              if (name.equals(XML_CREATE_STATEMENT)) {
                operation = PopulateUtils.CREATE_OPERATION;
                statement.setLength(0);
              } else if (name.equals(XML_INSERT_STATEMENT)) {
                operation = PopulateUtils.INSERT_OPERATION;
                statement.setLength(0);
              } else if (name.equals(XML_DROP_STATEMENT)) {
                operation = PopulateUtils.DROP_OPERATION;
                statement.setLength(0);
              } else if (name.equals(XML_CHECK_STATEMENT)) {
                operation = PopulateUtils.CHECK_OPERATION;
                statement.setLength(0);
              }
            }
          }
          return;
        }

        public void characters(char[] chars, int start, int length) throws SAXException {
          if (foundEntry && table != null && operation != null) {
            statement.append(chars, start, length);
          }
          return;
        }

        public void endElement(String namespace, String name, String qName) throws SAXException {
          if (foundEntry) {
            if (name.equals(XML_DATABASE_STATEMENTS)) {
              foundEntry = false;
              throw new ParsingDoneException(); // Interrupt the parsing since everything has been collected
            } else {
              if (name.equals(XML_TABLE_STATEMENTS)) {
                table = null;
              } else {
                if (name.equals(XML_CREATE_STATEMENT)
                    || name.equals(XML_INSERT_STATEMENT)
                    || name.equals(XML_DROP_STATEMENT)
                    || name.equals(XML_CHECK_STATEMENT)) {
                  sqlStatements.put(PopulateUtils.makeSQLStatementKey(operation, table), statement.toString());
                  statement.setLength(0);
                  operation = null;
                }
              }
            }
          }
          return;
        }

      });
    } catch (ParsingDoneException exception) {} // Ignored
    return;
  }
}

