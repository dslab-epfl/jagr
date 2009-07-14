
/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: LoadOrds.java,v 1.1.1.1 2002/11/16 05:35:26 emrek Exp $
 *
 * Main function for the EJBPerf load program for the Ords database
 * The Corp database must be loaded first.
 * The Ords database is loaded by extracting data out of the Corp
 * db. This program reads from a named pipe that is written to by
 * the LoadCorp program.
 *
 * @author Shanti Subramanyam
 *
 * --------------------------------------------------------------------- **
 *  Author     Date         Description                                  **
 * --------------------------------------------------------------------- **
 *  Hogstrom   2001-09-17   Modified the customer selection to avoid     **
 *  $MRH-001                creating orders for customers with bad       **
 *                          credit.                                      **
 * --------------------------------------------------------------------- **
 *                                                                       **
 * Hogstrom                                                              **
 * $MRH-002    2001-11-06  Modified  SQLDate to use a timestmp instead   **
 *                         of a java.sql.Date to match the actual table  **
 *                         column type.                                  **
 * --------------------------------------------------------------------- ** 
 */
package com.sun.ecperf.load;


import java.lang.*;

import java.net.*;

import java.io.*;

import java.sql.*;

import java.util.*;

import java.text.*;

import java.math.BigDecimal;


class LoadOrds {

    static int            scale;
    static int            jdbcVersion = 1;
    RandNum               rand;
    static Connection     dbConnection;
    static BufferedReader ipipe;
    int                   numItems = 0;    // Number of items in database

    LoadOrds() {
        rand = new RandNum();
    }

    private static void usage() {

        System.err.println("Usage: load <orders_injection_rate>");
        System.err.println("       75C customers, P items, 75C orders");
        System.err.println("       where C and P are defined in Clause 4.2");
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

        LoadOrds l = new LoadOrds();

        // establish the database connection
        // via the appropriate jdbc driver
        dbConnection = DatabaseConnection.getConnection("ordsdb.properties");
        jdbcVersion  = DatabaseConnection.getVersion();

        dbConnection.setAutoCommit(false);

        // Get pipe that Corp load is writing into
        String pipeDir = DatabaseConnection.getPipeDir() + File.separator; 
        ipipe = new BufferedReader(new FileReader(pipeDir + "ordspipe"));

        l.cleanAll();

        // Now load all tables
        l.loadAll();
    }

    private void cleanAll() throws IOException, SQLException {

        PreparedStatement cs = null;
        try {
            cs = dbConnection.prepareStatement("truncate table O_customer");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("truncate table O_item");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("truncate table O_orders");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("truncate table O_orderline");
            cs.executeUpdate();
        }
        catch (SQLException e) {
            cs = dbConnection.prepareStatement("delete from O_customer");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("delete from O_item");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("delete from O_orders");
            cs.executeUpdate();
            cs = dbConnection.prepareStatement("delete from O_orderline");
            cs.executeUpdate();
        }

        

        dbConnection.commit();
    }

    private void loadAll() throws IOException, SQLException {

        int C            = (int) (Math.ceil((double) scale / 10.0)) * 10;
        int P            = (int) (Math.ceil((double) scale / 100.0)) * 100;
        int numCustomers = 75 * C;
        int numItems     = P;    // number of items
        int numOrders    = 75 * C;

        loadCustomer(numCustomers);

        // save the next customer id that can be used 
        loadSequence("customer", numCustomers + 1, 100);
        loadItem(numItems);
        loadOrders(numCustomers, numItems, numOrders);    // Orders will load orderline too
        loadSequence("order", numOrders + 1, 100);
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
            System.err.println("     SQL State: "+e.getSQLState());
            System.err.println("SQL Error Code: "+e.getErrorCode());
            System.err.println("   SQL Message: "+e.getMessage());
            System.err.println("Aborted Sequence Updation for " + id);

            throw e;
        } finally {
            cs.close();
        }
    }

    private void loadCustomer(int numCustomers)
            throws IOException, SQLException {

        PreparedStatement cs         = null;
        int               cId[]      = new int[100];
        String            cFirst[]   = new String[100];
        String            cLast[]    = new String[100];
        String            cPhone[]   = new String[100];
        String            cContact[] = new String[100];
        String            cStreet1[] = new String[100];
        String            cStreet2[] = new String[100];
        String            cCity[]    = new String[100];
        String            cState[]   = new String[100];
        String            cCountry[] = new String[100];
        String            cZip[]     = new String[100];
        java.sql.Date     cSince[]   = new java.sql.Date[100];
        Address           adr;
        String            b;
        StringTokenizer   st;
        int               i;

        try {
            cs = dbConnection
                .prepareStatement("insert into O_customer values("
                                  + "?,?,?,?,?,?,?,?,?,?,?,?)");

            //                      ((OraclePreparedStatement)cs).setExecuteBatch(100);
            i = 0;

            for (int cid = 1; cid <= numCustomers; cid++) {
                b  = ipipe.readLine();
                st = new StringTokenizer(b);

                //                              System.out.println("Read " + b + " from pipe");
                cId[i]      = Integer.parseInt(st.nextToken());
                cFirst[i]   = st.nextToken();
                cLast[i]    = st.nextToken();
                cStreet1[i] = st.nextToken();
                cStreet2[i] = st.nextToken();
                cCity[i]    = st.nextToken();
                cState[i]   = st.nextToken();
                cCountry[i] = st.nextToken();
                cZip[i]     = st.nextToken();
                cPhone[i]   = st.nextToken();
                cContact[i] = st.nextToken();
                cSince[i]   = java.sql.Date.valueOf(st.nextToken());

                cs.setInt(1, cid);
                cs.setString(2, cFirst[i]);
                cs.setString(3, cLast[i]);
                cs.setString(4, cStreet1[i]);
                cs.setString(5, cStreet2[i]);
                cs.setString(6, cCity[i]);
                cs.setString(7, cState[i]);
                cs.setString(8, cCountry[i]);
                cs.setString(9, cZip[i]);
                cs.setString(10, cPhone[i]);
                cs.setString(11, cContact[i]);
                cs.setDate(12, cSince[i]);

                //                              System.out.println(cid + " cfirst=" + cFirst[i] + 
                //                              " clast=" + cLast[i] + " cstreet1=" + cStreet1[i] +
                //                              " cstreet2=" + cStreet2[i] + " cCity=" + cCity[i] +
                //                              " ccountry=" + cCountry[i] + " cZip=" + cZip[i]);
                if (jdbcVersion == 1) {
                    cs.executeUpdate();
                } else {
                    cs.addBatch();
                }

                if (++i == 100) {
                    i = 0;

                    //                                      ((OraclePreparedStatement)cs).sendBatch();
                    if (jdbcVersion != 1) {
                        cs.executeBatch();
                    }

                    dbConnection.commit();
                }
            }

            if (i != 0) {
                if (jdbcVersion != 1) {
                    cs.executeBatch();
                }

                dbConnection.commit();
            }
        }

        /******
        catch (ParseException p) {
                System.err.println("Failed to parse date from ordspipe at cId " + cId[0]);
        }
        *****/
        catch (IOException io) {
            System.err
                .println("Failed to read customer from ordspipe at scale "
                         + scale + " cId " + cId[0]);

            throw io;
        } catch (SQLException e) {
            System.err.println("Aborted customer at cId " + cId[0]);

            throw e;
        } finally {
            cs.close();
        }
    }

    private void loadItem(int numItems) throws IOException, SQLException {

        PreparedStatement cs          = null;
        String            iId[]       = new String[100];
        String            iName[]     = new String[100];
        String            iDesc[]     = new String[100];
        double            iPrice[]    = new double[100];
        float             iDiscount[] = new float[100];
        String            b;
        StringTokenizer   st;
        int               i   = 0, j = 0;
        int               num = 0;

        try {
            cs = dbConnection.prepareStatement("insert into O_item values("
                                               + "?,?,?,?,?)");

            while ((b = ipipe.readLine()) != null) {
                st = new StringTokenizer(b);

                iId[i] = st.nextToken();

                num++;

                iName[i]     = st.nextToken();
                iDesc[i]     = st.nextToken();
                iPrice[i]    = Double.valueOf(st.nextToken()).doubleValue();
                iDiscount[i] = (float) (rand.drandom(0.00, 0.70));

                cs.setString(1, iId[i]);
                cs.setString(2, iName[i]);
                cs.setString(3, iDesc[i]);
                cs.setDouble(4, iPrice[i]);
                cs.setFloat(5, iDiscount[i]);

                if (jdbcVersion == 1) {
                    cs.executeUpdate();
                } else {
                    cs.addBatch();
                }

                if (++i == 100) {
                    i = 0;

                    //                                      ((OraclePreparedStatement)cs).sendBatch();
                    if (jdbcVersion != 1) {
                        cs.executeBatch();
                    }

                    dbConnection.commit();
                }
            }

            if (i != 0) {

                //                              ((OraclePreparedStatement)cs).sendBatch();
                if (jdbcVersion != 1) {
                    cs.executeBatch();
                }

                dbConnection.commit();
            }
        } catch (IOException io) {
            System.err.println("Failed to read item record " + j
                               + " from ordspipe");

            throw io;
        } catch (SQLException e) {
            System.err.println("Aborted item at iId " + iId[0]);

            throw e;
        } finally {
            cs.close();
        }

        if (num != numItems) {
            System.err.println("Internal error. Expected " + numItems
                               + " rows from ordspipe. Got " + num);
        }
    }

    private void loadOrders(int numCustomers, int numItems, int numOrders)
            throws SQLException {

        PreparedStatement cs          = null, cs1 = null, cs2 = null;
        Statement         stmt        = dbConnection.createStatement();
        int               oId[]       = new int[100];
        int               oCid[]      = new int[100];
        int               oOlcnt[]    = new int[100];
        int               oStatus[]   = new int[100];
        double            oDiscount[] = new double[100];
        double            oTotal[]    = new double[100];
        int               olOid[]     = new int[100];
        int               olId[]      = new int[100];
        int               olQty[]     = new int[100];
        int               olStatus[]  = new int[100];
        String            olIid[]     = new String[100];
//        java.sql.Date     sqlDate     = new java.sql.Date((new java.util.Date()).getTime()); $MRH-002
        java.sql.Timestamp     sqlDate     = new java.sql.Timestamp((new java.util.Date()).getTime());
        int               i, j = 0;
        RandPart          rp = new RandPart(scale);

        try {
            cs  = dbConnection
                .prepareStatement("insert into O_orders values("
                                  + "?,?,?,?,?,?,?, NULL)");
            cs1 = dbConnection
                .prepareStatement("insert into O_orderline values("
                                  + "?,?,?,?,?, NULL)");
            cs2 = dbConnection
                .prepareStatement("select c_credit from C_Customer where c_id = ?");
            
            i = 0;
            boolean badCredit = true;                       // $MRH-001-begin

            for (j = 1; j <= numOrders; j++) {
                badCredit = true;                           
                int cID = 0;
                while (badCredit) {
                  cID = rand.random(1, numCustomers);   
                  cs2.setInt(1, cID);                             

                  ResultSet resultSet = cs2.          
                                          executeQuery();
                  if (!resultSet.next()) {
                      System.out.println("Unable to locate customer "+cID);
                      System.exit(16);
                  }
                  if (! resultSet.getString(1).equals("BC")) {
                    badCredit = false;
                  }
                  resultSet.close();
                }                                           // $MRH-001-end

                oCid[i]   = cID;
                oOlcnt[i] = rand.random(1, 5);
                oId[i]    = j;
                oTotal[i] = rand.drandom(1000.00, 10000.00);

                // Decide on the status of this order
                // 3 order states ?
                oStatus[i] = rand.random(1, 3);

                // create orderline table entries first
                for (int k = 0; k < oOlcnt[i]; k++) {
                    olId[k]  = k + 1;
                    olOid[k] = oId[i];

                    // ensure that Iid generated is unique for this order
                    boolean done = false;

                    while (!done) {
                        olIid[k] = rp.getPart(numItems);

                        int l;

                        for (l = 0; l < k; l++) {
                            if (olIid[k].equals(olIid[l])) {
                                break;
                            }
                        }

                        if (l == k) {
                            done = true;
                        }
                    }

                    olStatus[k] = oStatus[i];

                    int prob = rand.random(1, 100);

                    if (prob <= 10) {
                        olQty[k] = rand.random(1, 99);
                    } else {
                        olQty[k] = rand.random(1, 9);
                    }
               }
               // Now sort the orderlines by item-id - Fix for bug 4480737
               Arrays.sort(olIid, 0, oOlcnt[i]);

               // Add orderlines to database
               for (int k = 0; k < oOlcnt[i]; k++) {
                    cs1.setInt(1, olId[k]);
                    cs1.setInt(2, oId[i]);
                    cs1.setString(3, olIid[k]);
                    cs1.setInt(4, olQty[k]);
                    cs1.setInt(5, olStatus[k]);

                    if (jdbcVersion == 1) {
                        cs1.executeUpdate();
                    } else {
                        cs1.addBatch();
                    }
                }

                if (jdbcVersion != 1) {
                    cs1.executeBatch();
                }

                // now create order row
                cs.setInt(1, oId[i]);
                cs.setInt(2, oCid[i]);
                cs.setInt(3, oOlcnt[i]);
                cs.setDouble(4, oDiscount[i]);
                cs.setDouble(5, oTotal[i]);
                cs.setInt(6, oStatus[i]);
//                cs.setDate(7, sqlDate); //$MRH-002
                cs.setTimestamp(7, sqlDate);

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
            }

            if (i != 0) {

                if (jdbcVersion != 1) {
                    cs.executeBatch();
                }

                dbConnection.commit();
            }
        } catch (SQLException e) {
            System.err.println("Aborted orders at record " + j);

            throw e;
        } finally {
            cs.close();
            cs1.close();
        }
    }
}

