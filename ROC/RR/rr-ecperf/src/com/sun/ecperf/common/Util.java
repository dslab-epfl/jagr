
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Util.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.common;


import java.sql.*;

import javax.ejb.*;


/**
 * Class Util
 *
 *
 * @author
 * @version %I%, %G%
 */
public class Util {

    private Util() {}

    /**
     * Method round
     *
     *
     * @param value
     * @param digits
     *
     * @return
     *
     */
    public static double round(double value, int digits) {

        double base = Math.pow(10, digits);

        value = (double) Math.round(value * base);
        value /= base;

        return value;
    }

    /**
     * Method closeConnection
     *
     *
     * @param conn
     * @param st
     *
     */
    public static void closeConnection(Connection conn, Statement st) {

        boolean closeError = false;

        if (st != null) {
            try {
                st.close();
                st = null;
            } catch (SQLException e) {
                e.printStackTrace(System.err);

                closeError = true;
            }
        }

        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                e.printStackTrace(System.err);

                closeError = true;
            }
        }

        if (closeError) {
            throw new EJBException("Exceptions trying to close connection!");
        }
    }

    /**
     * Method closeConnection
     *
     *
     * @param conn
     * @param st
     *
     */
    public static void closeConnection(Connection conn, Statement[] st) {

        boolean closeError = false;

        for (int i = 0; i < st.length; i++) {
            if (st[i] != null) {
                try {
                    st[i].close();
                    st[i] = null;
                } catch (SQLException e) {
                    e.printStackTrace(System.err);

                    closeError = true;
                }
            }
        }

        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                e.printStackTrace(System.err);

                closeError = true;
            }
        }

        if (closeError) {
            throw new EJBException("Exceptions trying to close connection!");
        }
    }
}

