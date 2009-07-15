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


public class ItemDetailsPopulator {
  private static final String XML_ITEMDETAILS = "ItemDetails";
  private static final String XML_LISTPRICE = "ListPrice";
  private static final String XML_UNITCOST = "UnitCost";
  private static final String XML_ATTRIBUTE1 = "Attribute[0]";
  private static final String XML_ATTRIBUTE2 = "Attribute[1]";
  private static final String XML_ATTRIBUTE3 = "Attribute[2]";
  private static final String XML_ATTRIBUTE4 = "Attribute[3]";
  private static final String XML_ATTRIBUTE5 = "Attribute[4]";
  private static final String XML_IMAGE = "Image";
  private static final String XML_DESCRIPTION = "Description";
  private static final String XML_LOCALE = "ItemDetails/@xml:lang";
  // The INSERT statement expects parameters to be passed in the same order as below
  private static final String[] PARAMETER_NAMES = { ItemPopulator.XML_ID,
                                                    XML_LISTPRICE,
                                                    XML_UNITCOST,
                                                    XML_LOCALE,
                                                    XML_ATTRIBUTE1,
                                                    XML_ATTRIBUTE2,
                                                    XML_ATTRIBUTE3,
                                                    XML_ATTRIBUTE4,
                                                    XML_ATTRIBUTE5,
                                                    XML_IMAGE,
                                                    XML_DESCRIPTION };
  private Map sqlStatements;
  private String rootTag;


  public ItemDetailsPopulator(Map sqlStatements) {
    this(ItemPopulator.XML_ITEM, sqlStatements);
    return;
  }

  public ItemDetailsPopulator(String rootTag, Map sqlStatements)  {
    this.rootTag = rootTag;
    this.sqlStatements = sqlStatements;
    return;
  }

  public XMLFilter setup(XMLReader reader, final Connection connection) {
    return new XMLDBHandler(reader, rootTag, XML_ITEMDETAILS) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        if (connection == null) {
          PopulateUtils.printSQLStatement(sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.INSERT_OPERATION, "item_details"), PARAMETER_NAMES, this);
        } else {
          PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.INSERT_OPERATION, "item_details"), PARAMETER_NAMES, this);
        }
      }
    };
  }

  public boolean check(Connection connection) throws PopulateException {
    return PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.CHECK_OPERATION, "item_details"), null, null);
  }

  public void dropTables(Connection connection) throws PopulateException {
    PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.DROP_OPERATION, "item_details"), null, null);
    return;
  }

  public void createTables(Connection connection) throws PopulateException {
    PopulateUtils.executeSQLStatement(connection, sqlStatements, PopulateUtils.makeSQLStatementKey(PopulateUtils.CREATE_OPERATION, "item_details"), null, null);
    return;
  }
}

