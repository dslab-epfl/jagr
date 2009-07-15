
/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: LoadRules.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 * Main function for the EJBPerf load program for the rules in the Corp database
 *
 * @author Shanti Subramanyam
 *
 * $Mod: LoadRules.java,v 1.5 2001/04/14 08:30:00 hogstrom - Modified
 *       bean to support single row entities.
 */
package com.sun.ecperf.load;


import java.lang.*;

import java.net.*;

import java.io.*;

import java.sql.*;

import java.util.*;

import java.text.*;

import java.math.BigDecimal;


class LoadRules {

    static String         ruleName;
    static String         ruleFile;
    static int            scale;
    static int            jdbcVersion = 1;
    static Connection     dbConnection;
    static BufferedReader ipipe;
    int                   numItems = 0;    // # items in database

    LoadRules() {}

    private static void usage() {

        System.err.println("Usage: java Loadrules <rulename> <rulefile>");
        System.err.println("rulename: name for the rule to load");
        System.err.println("rulefile: file to load for rule");
    }

    /**
     * Method main
     *
     *
     * @param argv
     *
     * @throws Exception
     *
     */
    public static void main(String[] argv) throws Exception {

        if (argv.length != 2) {
            usage();

            return;
        }

        ruleName = argv[0];
        ruleFile = argv[1];

        LoadRules l = new LoadRules();

        // establish the database connection
        // via the appropriate jdbc driver
        dbConnection = DatabaseConnection.getConnection("corpdb.properties");
        jdbcVersion  = DatabaseConnection.getVersion();

        dbConnection.setAutoCommit(false);

        // Get pipe that Corp load is writing into
        ipipe = new BufferedReader(new FileReader(ruleFile));

        // Now load all tables
        l.loadAll();
    }

    private void loadAll() throws IOException, SQLException {
        loadRules();
    }

    private void loadRules() throws IOException, SQLException {

        PreparedStatement cs = null;

        try {
            cs = dbConnection.prepareStatement(
                "insert into C_rule (r_id, r_text) "
                + "values (?, ?)");

            cs.setString(1, ruleName);

            StringBuffer ruleText = new StringBuffer();

            String tempString = null;
            while ( (tempString = ipipe.readLine()) != null)
                ruleText.append(tempString+"\n");

            cs.setString(2, ruleText.toString());

            if ( cs.executeUpdate() < 1 )
              System.err.println("LoadRules: Error loading rule.  Insert failed");
            
            dbConnection.commit();
        } catch (IOException ie) {
            System.err.println("Failed to read input rule file: "
                               + ie.getMessage());
        } catch (SQLException se) {
            System.err.println("Error inserting into database: "
                               + se.getMessage());
        } finally {
            ipipe.close();
        }
    }
}

