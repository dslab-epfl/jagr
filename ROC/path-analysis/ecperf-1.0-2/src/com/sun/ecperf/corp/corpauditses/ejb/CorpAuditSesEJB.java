
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * ramesh@eng.sun.com 
 * @author Ramesh Ramachandran
 *
 *
 */
package com.sun.ecperf.corp.corpauditses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;

import java.sql.*;

/**
 * This class is CorpAuditSesEJB seesion bean.
 *
 * This bean is stateless.
 *
 */
public class CorpAuditSesEJB implements SessionBean {

    private String           className = "CorpAuditSesEJB";
    protected Debug          debug;
    protected boolean        debugging;
    protected javax.sql.DataSource dataSource;
 
    /**
     * Method ejbCreate
     *
     *
     * @throws CreateException
     *
     */
    public void ejbCreate() throws CreateException {
	if (debugging)
	    debug.println(3, "ejbCreate ");
    }

    /**
     * Constructor CorpAuditSesEJB
     *
     *
     */
    public CorpAuditSesEJB() {}

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {}

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {}

    /**
     * Method setSessionContext
     *
     *
     * @param sc
     *
     */
    public void setSessionContext(SessionContext sc) {

        InitialContext initCtx;
        try {
            initCtx = new InitialContext();
            dataSource =
                (javax.sql
                    .DataSource) initCtx
                        .lookup("java:comp/env/CorpDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }

        try {

            int debugLevel =
                ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
                    .intValue();

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch (NamingException ne) {
            System.out.println(className + ":debuglevel Property not set. "
                               + "Turning off debug messages");
            debug = new Debug();
        }

	if (debugging)
	    debug.println(3, "setSessionContext");
    }


// Methods
    public boolean validateInitialValues(int txRate) throws RemoteException{

        Connection        conn = null;
        Statement stmt = null;
        boolean isValid = true;
        int rowCount = 0;

        // Compute the step function C (ref. clause 4.3.1.3)
        int stepFnC = (txRate / 10) * 10;
        if(txRate%10 > 0)
            stepFnC += 10;

        // Find the step function P (ref. clause 4.3.1.4)
	int stepFnP = (txRate / 100) * 100;
        if(txRate%100 > 0) 
            stepFnP += 100;

        if (debugging)
            debug.println(3, "validateInitialValues");

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();

            // Check C_site count
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM C_site");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount !=  1) {
                isValid = false;

                if (debugging)
                    debug.println(1, "Invalid Initial count for C_site");
            }

            // Check C_supplier count
            rs = stmt.executeQuery("SELECT COUNT (*) FROM C_supplier");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount != 10) {
                isValid = false;

                if (debugging)
                    debug.println(1, "Invalid Initial count for C_supplier");
            }

            // Check C_customer
            rs = stmt.executeQuery("SELECT COUNT (*) FROM C_customer");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount < stepFnC * 75) {
                isValid = false;

                if (debugging)
                    debug.println(1, "Invalid Initial count for C_customer");
            }

            // Check C_parts
            rs = stmt.executeQuery("SELECT COUNT (*) FROM C_parts");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(Math.abs(rowCount -  stepFnP * 11) / (stepFnP * 11) > 0.01) {
                isValid = false;

                if (debugging)
                    debug.println(1, "Invalid Initial count for C_parts");
            }

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        return isValid;
    }

    public int getCustomerCount() throws RemoteException {

        Connection conn = null;
        Statement stmt = null;
        int rowCount = 0;

        if (debugging)
            debug.println(3, "getCustomerCount()");

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM C_customer");
            if (rs.next())
                rowCount = rs.getInt(1);

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
        return rowCount;
    }
}

