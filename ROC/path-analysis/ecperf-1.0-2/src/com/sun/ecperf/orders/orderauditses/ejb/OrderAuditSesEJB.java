
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
package com.sun.ecperf.orders.orderauditses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.sql.*;

import com.sun.ecperf.common.*;

/**
 * This class is OrderAuditSesEJB seesion bean.
 *
 * This bean is stateless.
 *
 */
public class OrderAuditSesEJB implements SessionBean {

    private String           className = "OrderAuditSesEJB";
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
     * Constructor OrderAuditSesEJB
     *
     *
     */
    public OrderAuditSesEJB() {}

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
                        .lookup("java:comp/env/OrdersDataSource");
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

        // Compute the step function P (ref. clause 4.3.1.4)
	int stepFnP = (txRate / 100) * 100;
        if(txRate%100 > 0) 
            stepFnP += 100;

        if (debugging)
            debug.println(3, "validateInitialValues");

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();

            // Check O_customer count
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM O_customer");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount <  stepFnC * 75) {
                isValid = false;

                if(debugging)
                    debug.println(1, "Invalid Initial count for O_customer");
            }

            // Check O_item
            rs = stmt.executeQuery("SELECT COUNT (*) FROM O_item");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount !=  stepFnP) {
                isValid = false;

                if (debugging)
                    debug.println(1, "Invalid Initial count for O_item");
            }

            // Check O_orders
            rs = stmt.executeQuery("SELECT COUNT (*) FROM O_orders");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount <  stepFnC * 75) {
                isValid = false;

                if (debugging)
                    debug.println(1, "Invalid Initial count for O_orders");
            }

            // Check O_orderline
            rs = stmt.executeQuery("SELECT COUNT (*) FROM O_orderline");
            if (rs.next())
                rowCount = rs.getInt(1);
            if((rowCount < stepFnC * 225) && 
               (Math.abs(rowCount - stepFnC * 225) / (stepFnC * 225) > 0.01)) {
                isValid = false;

                if (debugging)
                    debug.println(1, "Invalid Initial count for O_orderline");
            }

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        return isValid;
    }

    public int getOrderCount(long startTime, long endTime) throws RemoteException {

        Connection        conn = null;
        PreparedStatement stmt = null;
        Timestamp startTs = new Timestamp(startTime);
        Timestamp endTs = new Timestamp(endTime);

        int rowCount = 0;

        if (debugging)
            debug.println(3, "getOrderCount(startTime, endTime)");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT COUNT (*) FROM O_orders WHERE O_entry_date >= ? AND O_entry_date <= ?");
            stmt.setTimestamp(1, startTs);
            stmt.setTimestamp(2, endTs);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                rowCount = rs.getInt(1);

            if (debugging)
                debug.println(3, " DB TX Count = " + rowCount);

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
        return rowCount;
    }
}

