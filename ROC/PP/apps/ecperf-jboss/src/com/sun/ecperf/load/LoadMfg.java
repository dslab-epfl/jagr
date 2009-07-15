
/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: LoadMfg.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 * Main function for the EJBPerf load program for the Mfg database
 * The Corp database must be loaded first.
 * The Mfg database is loaded by extracting data out of the Corp
 * db. This program reads from a named pipe that is written to by
 * the LoadCorp program.
 *
 * @author Shanti Subramanyam
 *
 * Modified:
 * 11/06/2001 Matt Hogstrom $MRH
 *            Modified startDate to use java.sql.Timestamp instead
 *            of java.sql.Date for DB vendors that don't map these types. 
 */
package com.sun.ecperf.load;


import java.lang.*;

import java.net.*;

import java.io.*;

import java.sql.*;

import java.util.*;

import java.text.*;

import java.math.BigDecimal;


class LoadMfg {

    static int            scale;
    static int            jdbcVersion = 1;
    static RandNum        rand;
    static Connection     dbConnection;
    static BufferedReader ipipe;
    int                   numAssemblies;
    static int            OPEN = 1;    /* Workorder open status */

    LoadMfg() {

        rand = new RandNum();

        int P = (int) (Math.ceil((double) scale / 100.0)) * 100;

        numAssemblies = P;    // number of assemblies
    }

    private static void usage() {

        System.err.println(
            "Usage: java com.sun.ecperf.load.loadMfg <orders_injection_rate>");
        System.err.println(
            "       11P parts, 11P boms, 11P workorders, 11P inventory");
        System.err.println("       where P is defined in Clause 4.2");
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

        if (argv.length != 1) {
            usage();

            return;
        }

        scale = Integer.parseInt(argv[0]);

        LoadMfg l = new LoadMfg();

        // establish the database connection via the appropriate jdbc driver
        dbConnection = DatabaseConnection.getConnection("mfgdb.properties");
        jdbcVersion  = DatabaseConnection.getVersion();

        dbConnection.setAutoCommit(false);

        // Get pipe that Corp load is writing into
        // This should get parts, supplier & site table info
        String pipeDir = DatabaseConnection.getPipeDir() + File.separator; 
        ipipe = new BufferedReader(new FileReader(pipeDir + "mfgpipe"));

        l.cleanAll();

        // Now load all tables
        l.loadAll();
    }

    private void cleanAll() throws IOException, SQLException {

        PreparedStatement cs = null;

	/*
        try {
            // If truncate table is not supported use delete
            cs = dbConnection.prepareStatement("truncate table M_parts");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("truncate table M_bom");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("truncate table M_workorder");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("truncate table M_largeorder");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("truncate table M_inventory");
            cs.executeUpdate();
        }
        catch(SQLException e) {
	*/
            cs = dbConnection.prepareStatement("delete from M_parts");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("delete from M_bom");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("delete from M_workorder");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("delete from M_largeorder");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("delete from M_inventory");
            cs.executeUpdate();
	    //}
        dbConnection.commit();
    }

    private void loadAll() throws IOException, SQLException {

        String            b;
        StringTokenizer   st;
        String            pId[]            = new String[100];
        String            pName[]          = new String[100];
        String            pDesc[]          = new String[100];
        String            pRev[]           = new String[100];
        int               pInd[]           = new int[100];
        int               pType[]          = new int[100];
        int               pPlanner[]       = new int[100];
        int               pLeadTime[]      = new int[100];
        double            pSafetyStock[]   = new double[100];
        int               pContainerSize[] = new int[100];
        int               pDemand[]        = new int[100];
        int               pLoMark[]        = new int[100];
        int               pHiMark[]        = new int[100];
        int               i                = 0, j = 0;
        PreparedStatement ps;
        Bom               bomp  = new Bom();
        WorkOrder         workp = new WorkOrder();
        Inventory         invp  = new Inventory();

        ps = dbConnection.prepareStatement("insert into M_parts values("
                                           + "?,?,?,?,?,?,?,?,?)");

        try {
            j = 0;

            while ((b = ipipe.readLine()) != null) {
                j++;

                st                = new StringTokenizer(b);
                pId[i]            = st.nextToken();
                pName[i]          = st.nextToken();
                pDesc[i]          = st.nextToken();
                pRev[i]           = st.nextToken();
                pPlanner[i]       = Integer.parseInt(st.nextToken());
                pType[i]          = Integer.parseInt(st.nextToken());
                pInd[i]           = Integer.parseInt(st.nextToken());
                pContainerSize[i] = Integer.parseInt(st.nextToken());
                pHiMark[i]        = Integer.parseInt(st.nextToken());
                pLoMark[i]        = pHiMark[i] / 2;

                // Load parts table
                ps.setString(1, pId[i]);
                ps.setString(2, pName[i]);
                ps.setString(3, pDesc[i]);
                ps.setString(4, pRev[i]);
                ps.setInt(5, pPlanner[i]);
                ps.setInt(6, pType[i]);
                ps.setInt(7, pInd[i]);
                ps.setInt(8, pLoMark[i]);
                ps.setInt(9, pHiMark[i]);

                if (jdbcVersion == 1) {
                    ps.executeUpdate();
                } else {
                    ps.addBatch();
                }

                // Load the BOM table
                bomp.loadBom(pId[i], pInd[i], pType[i]);

                // Call workorder load only if part is an assembly
                if (pType[i] != 0) {
                    workp.loadWorkOrder(pId[i], pInd[i], pType[i]);
                }

                invp.loadInventory(pId[i], pInd[i], pType[i], pHiMark[i],
                                   pLoMark[i]);

                if (++i == 100) {
                    i = 0;

                    if (jdbcVersion != 1) {
                        ps.executeBatch();
                    }

                    dbConnection.commit();
                }
            }

            if (i != 0) {
                if (jdbcVersion != 1) {
                    ps.executeBatch();
                }

                dbConnection.commit();
            }

            bomp.loadRemainder();
            workp.loadRemainder();
            invp.loadRemainder();
            loadSequence("workorder", numAssemblies + 1, 10000);
            loadSequence("largeorder", 1, 10000);
        } catch (IOException io) {
            System.err.println("Failed to read parts record " + j
                               + " from mfgpipe");

            throw io;
        } finally {
            ps.close();
        }
    }

    private void loadSequence(String id, int nextSeq, int block)
            throws SQLException {

        PreparedStatement cs = null;

        // First delete the row if it exists
        try {
            String tmp = new String("DELETE FROM U_sequences WHERE S_ID = '"
                                    + id + "'");

            cs = dbConnection.prepareStatement(tmp);

            cs.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {

            // Do nothing its OK
        }

        String sqlbuf = new String("INSERT INTO U_sequences (" + "s_id, "
                                   + "s_nextnum, " + "s_blocksize "
                                   + ") VALUES ( ?,?,? ) ");

        try {
            cs = dbConnection.prepareStatement(sqlbuf);

            int i = 1;

            cs.setString(i++, id);
            cs.setInt(i++, nextSeq);
            cs.setInt(i++, block);
            cs.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            System.err.println("Aborted Sequence Updation for " + id);

            throw e;
        } finally {
            cs.close();
        }
    }

    private static class Bom {

        PreparedStatement cs;
        String            bCompId[]     = new String[100];
        String            bAssemblyId[] = new String[100];
        int               bLineNum[]    = new int[100];
        int               bQty[]        = new int[100];
        int               bOps[]        = new int[100];
        String            bEngChange[]  = new String[100];
        String            bOpsDesc[]    = new String[100];
        static int        i             = 0;
        static boolean    first         = true;
        static String     lastAssembly;
        int               lineno = 1;

        void loadBom(String pId, int pInd, int pType) throws SQLException {

            try {
                if (first) {
                    cs    =
                        dbConnection
                            .prepareStatement("insert into M_bom values("
                                              + "?,?,?,?,?,?,?)");
                    first = false;
                }

                if (pType == 0) {        // This is a component
                    bCompId[i]     = pId;
                    bAssemblyId[i] = lastAssembly;
                    bLineNum[i]    = lineno++;
                    bQty[i]        = rand.random(1, 20);
                    bOps[i]        = rand.random(1, 20);
                    bEngChange[i]  = "";
                    bOpsDesc[i]    = rand.makeAString(50, 100);

                    cs.setString(1, bCompId[i]);
                    cs.setString(2, bAssemblyId[i]);
                    cs.setInt(3, bLineNum[i]);
                    cs.setInt(4, bQty[i]);
                    cs.setInt(5, bOps[i]);
                    cs.setString(6, bEngChange[i]);
                    cs.setString(7, bOpsDesc[i]);

                    if (jdbcVersion == 1) {
                        cs.executeUpdate();
                    } else {
                        cs.addBatch();
                    }

                    if (++i == 100) {
                        i = 0;

                        if (jdbcVersion != 1) {
                            cs.executeBatch();
                        }

                        dbConnection.commit();
                    }
                } else {                 // This is an assembly - just save it
                    lastAssembly = pId;
                    lineno       = 1;    // reset line number
                }
            } catch (SQLException e) {
                System.err.println("Aborted Bom load at Assembly "
                                   + bAssemblyId[0]);

                throw e;
            }
        }

        void loadRemainder() throws SQLException {

            try {
                if (i != 0) {
                    if (jdbcVersion != 1) {
                        cs.executeBatch();
                    }

                    dbConnection.commit();
                }
            } catch (SQLException e) {
                System.err.println("Aborted Bom load at Assembly "
                                   + bAssemblyId[0]);

                throw e;
            } finally {
                cs.close();
            }
        }
    }

    private static class WorkOrder {

        static PreparedStatement wos;
        static int               woNumber[]     = new int[100];
        static int               woOid[]        = new int[100];
        static int               woOlid[]       = new int[100];
        static int               woStatus[]     = new int[100];
        static int               woOrigQty[]    = new int[100];
        static int               woCompQty[]    = new int[100];
        static String            woAssemblyId[] = new String[100];
//        static java.sql.Date     woStartDate[]  = new java.sql.Date[100];   $MRH
        static java.sql.Timestamp  woStartDate[]  = new java.sql.Timestamp[100];
        static int               i              = 0, j = 0;
        static boolean           first          = true;
        java.util.Date           curDate;

        private void loadWorkOrder(String pId, int pInd, int pType)
                throws SQLException {

            try {
                if (first) {
                    wos   = dbConnection
                        .prepareStatement("insert into M_workorder values("
                                          + "?,?,?,?,?,?,?,NULL,?)");
                    first = false;
                }

                woNumber[i] = ++j;

                // 25% of workorders are from direct sales custom orders
                // For now, there are no custom orders, since this will entail
                // a) having large custom orders and b) figuring out which
                // o_id to use from Orders db
                woOid[i]        = woOlid[i] = 0;
                woOrigQty[i]    = rand.random(1, 1000);
                woCompQty[i]    = rand.random(1, woOrigQty[i]);
                woStatus[i]     = OPEN;
                woAssemblyId[i] = pId;
                curDate         = new java.util.Date();
                woStartDate[i]  = new java.sql.Timestamp(curDate.getTime());
//                woStartDate[i]  = new java.sql.Date(curDate.getTime());  $MRH

                // now create workorder row
                wos.setInt(1, woNumber[i]);
                wos.setInt(2, woOid[i]);
                wos.setInt(3, woOlid[i]);
                wos.setInt(4, woStatus[i]);
                wos.setString(5, woAssemblyId[i]);
                wos.setInt(6, woOrigQty[i]);
                wos.setInt(7, woCompQty[i]);
//                wos.setDate(8, woStartDate[i]); $MRH
                wos.setTimestamp(8, woStartDate[i]);

                if (jdbcVersion == 1) {
                    wos.executeUpdate();
                } else {
                    wos.addBatch();
                }

                if (++i == 100) {
                    i = 0;

                    if (jdbcVersion != 1) {
                        wos.executeBatch();
                    }

                    dbConnection.commit();
                }
            } catch (SQLException e) {
                System.err.println("Aborted Workorder at id " + woNumber[0]);

                throw e;
            }
        }

        void loadRemainder() throws SQLException {

            try {
                if (i != 0) {
                    if (jdbcVersion != 1) {
                        wos.executeBatch();
                    }

                    dbConnection.commit();
                }
            } catch (SQLException e) {
                System.err.println("Aborted Workorder load at id "
                                   + woNumber[0]);

                throw e;
            } finally {
                wos.close();
            }
        }
    }

    private static class Inventory {

        static PreparedStatement is;
        static String            invPid[]      = new String[100];
        static int               invQty[]      = new int[100];
        static int               inAccCode[]   = new int[100];
        static String            invLocation[] = new String[100];
        static int               i             = 0, j = 0;
        static boolean           first         = true;
        java.sql.Date            sqlDate       = new java.sql.Date((new java.util.Date()).getTime());

        private void loadInventory(
                String pId, int pInd, int pType, int pHigh, int pLow)
                    throws SQLException {

            try {
                if (first) {
                    is    = dbConnection
                        .prepareStatement("insert into M_inventory values("
                                          + "?,?,?,?,0,?)");
                    first = false;
                }

                invPid[i]      = pId;
                invQty[i]      = pHigh;
                invLocation[i] = rand.makeAString(20, 20);

                // now create workorder row
                is.setString(1, invPid[i]);
                is.setInt(2, invQty[i]);
                is.setInt(3, 0);
                is.setString(4, invLocation[i]);
                is.setDate(5, sqlDate);

                if (jdbcVersion == 1) {
                    is.executeUpdate();
                } else {
                    is.addBatch();
                }

                if (++i == 100) {
                    i = 0;

                    if (jdbcVersion != 1) {
                        is.executeBatch();
                    }

                    dbConnection.commit();
                }
            } catch (SQLException e) {
                System.err.println("Aborted Inventory load at record " + j);

                throw e;
            }
        }

        void loadRemainder() throws SQLException {

            try {
                if (i != 0) {
                    if (jdbcVersion != 1) {
                        is.executeBatch();
                    }

                    dbConnection.commit();
                }
            } catch (SQLException e) {
                System.err.println("Aborted Inventory load at part "
                                   + invPid[0]);

                throw e;
            } finally {
                is.close();
            }
        }
    }
}

