
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
package com.sun.ecperf.mfg.mfgauditses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.mfg.helper.*;
import com.sun.ecperf.common.*;

import java.sql.*;

/**
 * This class is MfgAuditSesEJB seesion bean.
 *
 * This bean is stateless.
 *
 */
public class MfgAuditSesEJB implements SessionBean {

    private String           className = "MfgAuditSesEJB";
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
     * Constructor MfgAuditSesEJB
     *
     *
     */
    public MfgAuditSesEJB() {}

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
                        .lookup("java:comp/env/MfgDataSource");
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
        // Find the step function P (ref. clause 4.3.1.4)
	int stepFnP = (txRate / 100) * 100;
        if(txRate%100 > 0) 
            stepFnP += 100;

        if (debugging)
            debug.println(3, "validateInitialValues");

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();

            // Check M_workorder count
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM M_workorder");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount <  stepFnP) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for M_workorder ");
            }

            // Check M_parts 
            rs = stmt.executeQuery("SELECT COUNT (*) FROM M_parts");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(Math.abs(rowCount -  stepFnP * 11) / (stepFnP * 11) > 0.01) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for M_parts");
            }

            // Check M_bom
            rs = stmt.executeQuery("SELECT COUNT (*) FROM M_bom");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(Math.abs(rowCount -  stepFnP * 10) / (stepFnP * 10) > 0.01) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for M_bom");
            }

            // Check M_inventory  
            rs = stmt.executeQuery("SELECT COUNT (*) FROM M_inventory");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(Math.abs(rowCount -  stepFnP * 11) / (stepFnP * 11) > 0.01) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for M_inventory");
            }

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        return isValid;
    }

    public int getWorkOrderCount(long startTime, long endTime) throws RemoteException {

        Connection        conn = null;
        PreparedStatement stmt = null;
        Timestamp startTs = new Timestamp(startTime);
        Timestamp endTs = new Timestamp(endTime);

        int rowCount = 0;

        if (debugging)
            debug.println(3, "getWorkOrderCount()");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT COUNT (*) FROM M_workorder WHERE WO_START_DATE >= ? AND WO_START_DATE <= ?");
            stmt.setTimestamp(1, startTs);
            stmt.setTimestamp(2, endTs);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                rowCount = rs.getInt(1);

            if (debugging)
                debug.println(3, stmt + " returned " + rowCount);

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
        return rowCount;
    }
}

