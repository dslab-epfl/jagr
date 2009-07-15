
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
package com.sun.ecperf.supplier.supplierauditses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.net.*;

import java.sql.*;

import java.io.*;

import com.sun.ecperf.supplier.helper.*;

import com.sun.ecperf.common.*;

/**
 * This class is SupplierAuditSesEJB seesion bean.
 *
 * This bean is stateless.
 *
 */
public class SupplierAuditSesEJB implements SessionBean {

    private static final int EMULATOR_INDEX = 0;
    private static final int DELIVERY_INDEX = 1;


    private String           className = "SupplierAuditSesEJB";
    protected Debug          debug;
    protected boolean        debugging;
    protected javax.sql.DataSource dataSource;

    private String deliveryServlet, emulatorServlet;


    // As we have no way of getting the # of POs & POLines created 
    // during study state due to long sleep times in emulator
    // we are verifying only the total for the whole run
    private static int initPOCount = 0;
    private static int initPOLineCount = 0;

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
     * Constructor SupplierAuditSesEJB
     *
     *
     */
    public SupplierAuditSesEJB() {}

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
                        .lookup("java:comp/env/SupplierDataSource");
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

        try {
            deliveryServlet = (String)initCtx.lookup("java:comp/env/deliveryServlet");
            emulatorServlet = (String)initCtx.lookup("java:comp/env/emulatorServlet");
        } catch (NamingException e) {
            debug.println(1, "Unable to get Servlet URLs " + e);
            debug.printStackTrace(e);
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

            // Check S_site count
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM S_site");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount !=  1) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for S_site");
            }

            // Check S_supplier count
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_supplier");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(rowCount != 10) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for S_supplier");
            }

            // Check S_component
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_component");
            if (rs.next())
                rowCount = rs.getInt(1);
            if(Math.abs(rowCount -  stepFnP * 10) / (stepFnP * 10) > 0.01) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for S_component");
            }

            // Check S_purchase_order
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_order");
            if (rs.next())
                rowCount = rs.getInt(1);
            /************
            // **** Too much variation 
            if((rowCount <  stepFnP * 0.2) && 
               (Math.abs(rowCount -  stepFnP * 0.2) / (stepFnP * 0.2) > 0.01)) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for S_purchase_order");
            }
            else 
            ****************/
                // Save the initial value
                initPOCount = rowCount;

            // Check S_purchase_orderline
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_orderline");
            if (rs.next())
                rowCount = rs.getInt(1);
            if((rowCount <  stepFnP) && 
               (Math.abs(rowCount -  stepFnP) / stepFnP > 0.01)) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for S_purchase_orderline");
            }
            else 
                // Save the initial value
                initPOLineCount = rowCount;

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        return isValid;
    }

    public int getPOCount() throws RemoteException {

        Connection conn = null;
        Statement stmt = null;

        int rowCount = 0;

        if (debugging)
            debug.println(3, "getPOCount()");
        
        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_order");
            if (rs.next())
                rowCount = rs.getInt(1);

            if (debugging)
                debug.println(3, "SELECT COUNT (*) FROM S_purchase_order returned " + rowCount);

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
        return rowCount;
    }


    public int getPOLineCount() throws RemoteException {

        Connection conn = null;
        Statement stmt = null;

        int rowCount = 0;

        if (debugging)
            debug.println(3, "getPoLineCount()");
        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
 
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_orderline");
            if (rs.next())
                rowCount = rs.getInt(1);

            if (debugging)
                debug.println(3, "SELECT COUNT (*) FROM S_purchase_orderline returned " + rowCount);

        } catch (SQLException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        return rowCount; 
    }

    public int[] getServletTx() throws RemoteException {

        String okMsg = "200 OK";
        String txMsg = "TxCount";
        // index 0 for Emulator, and 1 for Delivery
        int[] txCount = {0, 0};

        URL[] url = new URL[2];

        try {
            url[0] = new URL(emulatorServlet);

            url[1] = new URL(deliveryServlet);

            for (int i = 0; i < url.length; i++ ) {
                HttpURLConnection conn = (HttpURLConnection) url[i].openConnection();
                BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(conn.getInputStream()));

                if (debugging)
                    debug.println(3, "Connected to " + url[i]);

                boolean ok = false;
                String r = null;

                for (;;) {
                    r = reader.readLine();
                    if (r == null)
                        break;
                    else if(r.indexOf(txMsg) != -1) 
                        txCount[i] = Integer.parseInt(r.substring(r.indexOf('=') + 1, 
                                                                  r.indexOf(';')).trim());
                    else if (r.indexOf(okMsg) != -1)
                        ok = true;
                }
                reader.close();
                if (!ok) {
                    debug.println(1, "Unable to get Tx counts from servlets");
                    throw new EJBException("Unable to get Tx counts from servlets");
                }
            }
        } catch(Exception e) {
            debug.println(1, "Unable to get Tx counts from servlets " + e);
            throw new EJBException("Unable to get Tx counts from servlets " + e);
        }
        if (debugging)
            debug.println(3, "Emulator Tx = " + txCount[0] + ": Delivery Tx = " + txCount[1]);
        return txCount;
    }
}

