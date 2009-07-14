
/**
 * Copyright (c) 1999, 2002 by Sun Microsystems, Inc.
 *
 * $Id: DatabaseConnection.java,v 1.1.1.1 2002/11/16 05:35:26 emrek Exp $
 */
package com.sun.ecperf.load;


import java.sql.*;

import java.util.*;

import java.io.*;


/**
 * Class to create a JDBC connection. Connection parameters are
 * read from a properties file.
 *
 * The following entries are required in the properties file:
 * <UL>
 * <LI> dbURL : JDBC URL used to connect to database
 * <LI> dbUser : Name of database user
 * <LI> dbPassword : Password of database user
 * </UL>
 * The following entry specifies the name of the JDBC driver:
 * <LI> dbDriver: JDBC driver name - default is oracle.jdbc.driver.OracleDriver
 * </UL>
 *
 * @author Hugo Rivero, MDE enterprise team
 */
public class DatabaseConnection {

    static final String DEFAULT_JDBC_DRIVER =
        "oracle.jdbc.driver.OracleDriver";
    private static int  jdbcVersion         = 1;

    // The temp directory to create the pipes
    private static String pipeDir = "/tmp";

    /**
     * Method getVersion
     *
     *
     * @return
     *
     */
    public static int getVersion() {
        return jdbcVersion;
    }

    /**
     * Method getPipeDir
     *
     *
     * @return
     *
     */
    public static String getPipeDir() {
        return pipeDir;
    }

    /**
     * Method getConnection
     *
     *
     * @param propsFileName
     *
     * @return
     *
     * @throws Exception
     *
     */
    static public Connection getConnection(String propsFileName)
            throws Exception {

        String prefix = System.getProperty("ecperf.home");

        if (prefix == null) {
            throw new Exception("System property ecperf.home not set");
        }

        propsFileName = prefix + "/config/" + propsFileName;

        // Read properties from file
        Properties props = new Properties();

        try {
            FileInputStream in = new FileInputStream(propsFileName);

            props.load(in);
            in.close();
        } catch (Exception e) {
            throw new Exception("Cannot read properties file "
                                + propsFileName
                                + " . Make sure file exists\n"
                                + e.toString());
        }

        String dbURL = props.getProperty("dbURL");

        if (dbURL == null) {
            throw new Exception(
                "Property \"dbURL\" missing in properties file "
                + propsFileName);
        }

        String dbUser = props.getProperty("dbUser");

        if (dbUser == null) {
            throw new Exception(
                "Property \"dbUser\" missing in properties file "
                + propsFileName);
        }

        String dbPassword = props.getProperty("dbPassword");

        if (dbPassword == null) {
            throw new Exception(
                "Property \"dbPassword\" missing in properties " + "file "
                + propsFileName);
        }

        // Get Database driver class, or use default if not defined
        String dbDriverClassName = props.getProperty("dbDriver");

        if (dbDriverClassName == null) {
            dbDriverClassName = DEFAULT_JDBC_DRIVER;
        }

        String version = props.getProperty("jdbcVersion");

        if (version != null) {
            try {
                jdbcVersion = Integer.parseInt(version);
            } catch (NumberFormatException e) {
                throw new Exception("Property \"jdbcVersion\" in file "
                                    + propsFileName + " is not an integer");
            }
        }

        String pipe = props.getProperty("pipeDir");

        if ((pipe != null) && (new File(pipe)).isDirectory())
            pipeDir = pipe;
        else { 
            System.out.println("WARNING : pipeDir not set or does not exist");
            System.out.println("WARNING : using default /tmp ");
        }
        
        // Instantiate and register JDBC driver

        /***
        System.out.println("dbDriverClassName is " + dbDriverClassName);
        Class.forName(dbDriverClassName);
        ***/
        Class  dbDriverClass = Class.forName(dbDriverClassName);
        Driver driver        = (Driver) dbDriverClass.newInstance();

        DriverManager.registerDriver(driver);

        return DriverManager.getConnection(dbURL, dbUser, dbPassword);
    }
}

