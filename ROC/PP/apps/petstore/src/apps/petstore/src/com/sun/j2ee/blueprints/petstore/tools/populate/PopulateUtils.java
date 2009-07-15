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

import java.sql.*;
import java.util.*;
import java.io.*;


public final class PopulateUtils {
  public static final String CREATE_OPERATION = "create";
  public static final String INSERT_OPERATION = "insert";
  public static final String DROP_OPERATION = "drop";
  public static final String CHECK_OPERATION = "check";
  private static final String DATABASE_DRIVER = "RmiJdbc.RJDriver";

  private PopulateUtils() {}

  public static boolean executeSQLStatement(Connection connection, Map sqlStatements, String sqlStatementKey, String[] parameterNames, XMLDBHandler handler)
    throws PopulateException {
      String statement = (String) sqlStatements.get(sqlStatementKey);
      if (statement != null) {
        return executeSQLStatement(connection, statement, parameterNames, handler);
      }
      throw new PopulateException("No statement found for: " + sqlStatementKey);
  }

  public static boolean executeSQLStatement(Connection connection, String sqlStatement, String[] parameterNames, XMLDBHandler handler)
    throws PopulateException {
      try {
        PreparedStatement statement = connection.prepareStatement(sqlStatement);
        if (parameterNames != null) {
          for (int i = 0; i < parameterNames.length; i++) {
            String value = handler.getValue(parameterNames[i]);
            statement.setString(i + 1, value);
          }
        }
        //int resultCount = statement.executeUpdate();
        statement.execute();
        ResultSet resultSet = statement.getResultSet();
        boolean result = resultSet != null ? resultSet.next() : statement.getUpdateCount() > 0;
        statement.close();
        return result;
      } catch (SQLException exception) {
        throw new PopulateException(sqlStatement, exception);
      }
  }

  public static void printSQLStatement(Map sqlStatements, String sqlStatementKey, String[] parameterNames, XMLDBHandler handler)
    throws PopulateException {
      String statement = (String) sqlStatements.get(sqlStatementKey);
      if (statement != null) {
        printSQLStatement(statement, parameterNames, handler);
        return;
      }
      throw new PopulateException("No statement found for: " + sqlStatementKey);
  }

  public static void printSQLStatement(String sqlStatement, String[] parameterNames, XMLDBHandler handler)
    throws PopulateException {
      StringTokenizer tokenizer = new StringTokenizer(sqlStatement, "?", true);
      for (int i = 0; tokenizer.hasMoreTokens();) {
        String token = tokenizer.nextToken();
        if (token.equals("?")) {
          String value = handler.getValue(parameterNames[i++]);
          System.out.print(value);
        } else {
          System.out.print(token);
        }
      }
      System.out.println(";");
      return;
  }

  static String makeSQLStatementKey(String operation, String table) {
    return table + "." + operation;
  }
}


