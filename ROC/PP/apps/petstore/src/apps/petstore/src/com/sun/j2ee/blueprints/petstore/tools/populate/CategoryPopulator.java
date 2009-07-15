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
import java.sql.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class CategoryPopulator {
  public static final String XML_CATEGORIES = "Categories";
  static final String XML_CATEGORY = "Category";
  static final String XML_ID = "Category/@id";
  // The INSERT statement expects parameters to be passed in the same order as below
  private static final String[] PARAMETER_NAMES = { XML_ID };
  private String rootTag;
  private CategoryDetailsPopulator categoryDetailsPopulator;
  private Map sqlStatements;


  public CategoryPopulator(Map sqlStatements) throws PopulateException {
    this(XML_CATEGORIES, sqlStatements);
    return;
  }

  public CategoryPopulator(String rootTag, Map sqlStatements) throws PopulateException {
    this.rootTag = rootTag;
    this.sqlStatements = sqlStatements;
    categoryDetailsPopulator = new CategoryDetailsPopulator(XML_CATEGORY, sqlStatements);
    return;
  }

  public XMLFilter setup(XMLReader reader, final Connection connection) {
    return new XMLDBHandler(categoryDetailsPopulator.setup(reader, connection), rootTag, XML_CATEGORY, true) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        if (connection == null) {
          PopulateUtils.printSQLStatement(sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.INSERT_OPERATION, "category"), PARAMETER_NAMES, this);
        } else {
          PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.INSERT_OPERATION, "category"), PARAMETER_NAMES, this);
        }
      }
    };
  }

  public boolean check(Connection connection) throws PopulateException {
    return PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.CHECK_OPERATION, "category"), null, null) && categoryDetailsPopulator.check(connection);
  }

  public void dropTables(Connection connection) throws PopulateException {
    try {
      categoryDetailsPopulator.dropTables(connection);
    } catch (PopulateException exception) {
      // System.err.println(exception.getRootCause().getMessage());
    }
    PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.DROP_OPERATION, "category"), null, null);
    return;
  }

  public void createTables(Connection connection) throws PopulateException {
    PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.CREATE_OPERATION, "category"), null, null);
    categoryDetailsPopulator.createTables(connection);
    return;
  }
}

