package com.sun.j2ee.blueprints.pkgen;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;

/**
 * This class has just a single static method for creating unique
 * primary key values for the given database table
 */
public final class PKGen {

    private static final String dsName = "java:/DefaultDS";
    private static DataSource ds = null;
    /**
     * The last counter values for the tables are cashed in this map
     */
    private static Map map = null;

    private PKGen() {}

    /**
     * This method returns the next available number for the
     * the primary key <Code>ID</Code> of the given table.
     * The table is accessed via datasource <Code>dsName</Code>.
     * @param <Code>String tableName</Code> name of the database table
     * @return <Code>Integer</Code> next value for primary key
     */
    public static Integer getNextKeyValue (String tableName) {
      Integer returnValue = null;

      if (map == null) {
        // Create map for synchronized access
        map = Collections.synchronizedMap (new HashMap(20));
      }

      // Get last counter value from map
      returnValue = (Integer) map.get (tableName);

      if (returnValue == null) {
        // Table is accessed for the first time
        PreparedStatement stmt = null;
        Connection con = null;
        ResultSet rs = null;

        if (ds == null) {
          // Get datasource
          try {
            Context context = new InitialContext();
            ds = (DataSource) context.lookup (dsName);
          } catch (Exception e) {
            System.err.println ("Error in PKGen.getNextKeyValue (): " + e.getMessage());
            e.printStackTrace ();
            return null;
          }
        }

        // Get the maximal used PK from the table
        try {
            con = ds.getConnection();
            stmt = con.prepareStatement ("SELECT MAX(ID) FROM " + tableName);
            rs = stmt.executeQuery();

            if (rs.next()) {
                returnValue = new Integer (rs.getInt (1));
            } else {
                // table is empty, start with 1 as PK
                returnValue = new Integer (0);
            }
        } catch (Exception e) {
            System.err.println ("Error in PKGen.getNextKeyValue (): " + e.getMessage());
            e.printStackTrace ();
            return null;
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) { }
            try {
                stmt.close();
            } catch (Exception ignored) { }
            try {
                con.close();
            } catch (Exception ignored) { }
        }
      }

      // Increment the counter and store it in our map
      returnValue = new Integer (returnValue.intValue() + 1);
      map.put (tableName, returnValue);
      // Some test output
      //System.out.println ("PKGen.getNextKeyValue (" + tableName + ") = " + returnValue);
      return returnValue;
  }

}
