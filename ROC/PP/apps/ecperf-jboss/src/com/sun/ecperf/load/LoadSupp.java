/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: LoadSupp.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 * Main program for the EJBPerf loading of the Supplier database
 * This program reads off part numbers from the Supp database for
 * simplicity, but in reality there must be a way to have different
 * part numbers between the company & supplier database
 * We load only the customer and parts tables initially.
 * @author: Shanti Subramanyam
 */

package com.sun.ecperf.load;

import java.lang.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

class LoadSupp {
	static int scale;
	static int jdbcVersion = 1;
	static RandNum rand;
	int numSuppliers, numSites, numAssemblies;
	static BufferedReader ipipe;
	static Connection dbConnection;


	LoadSupp()
	{
		rand = new RandNum();
	}

	private static void usage() {
		System.err.println("Usage: java sun.com.ecperf.load.LoadSupp <orders_injection_rate>");
		System.err.println("       1 site, 10 suppliers, 10P components, 25P purchaseorders");
		System.err.println("       where C and P are defined in Clause 4.2");
	}


	public static void main(String [] argv) 
		throws Exception {
		LoadSupp l = new LoadSupp();
		if (argv.length != 1) {
			usage();
			return;
		}
		scale = Integer.parseInt(argv[0]);

    	// establish the database connection
    	// via the appropriate jdbc driver
    	dbConnection = DatabaseConnection.getConnection("suppdb.properties");
		jdbcVersion = DatabaseConnection.getVersion();
		dbConnection.setAutoCommit(false);

		// Get pipe that Corp load is writing into
		// This should get parts, supplier & site table info
                String pipeDir = DatabaseConnection.getPipeDir() + File.separator; 
		ipipe = new BufferedReader(new FileReader(pipeDir + "supppipe"));

		l.cleanAll();
		l.loadAll();
		dbConnection.close();
	}

        private void cleanAll() throws IOException, SQLException {
                PreparedStatement cs = null;
		
		/*
                try { 
                    cs = dbConnection.prepareStatement("truncate table S_site");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table S_supplier");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table S_component");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table S_supp_component");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table S_purchase_order");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table S_purchase_orderline");
                    cs.executeUpdate();
                }
                catch(SQLException e) {
		*/
                    cs = dbConnection.prepareStatement("delete from S_site");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from S_supplier");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from S_component");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from S_supp_component");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from S_purchase_order");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from S_purchase_orderline");
                    cs.executeUpdate();
		    //}

                dbConnection.commit();
        }


	private void loadAll() throws IOException, SQLException {
		numSites = 1;
		numSuppliers = 10;
		int C = (int)(Math.ceil((double)scale/10.0)) * 10;
		int P = (int)(Math.ceil((double)scale/100.0)) * 100;
		int numAssemblies = P;	// number of assemblies
		loadSites();
		loadSuppliers();
		loadComponents();
	}


        private void loadSequence(String id, int nextSeq, int block) throws SQLException {
                PreparedStatement cs = null;

                // First delete the row if it exists
                try {
                   String tmp = new String("DELETE FROM U_sequences WHERE S_ID = '" + id + "'");
                   cs = dbConnection.prepareStatement(tmp);
                   cs.executeUpdate();
                   dbConnection.commit();
                }
                catch(SQLException e) { 
                   // Do nothing its OK
                }

                String sqlbuf = new String("INSERT INTO U_sequences (" +
                                           "s_id, " +
                                           "s_nextnum, " +
                                           "s_blocksize " +
                                           ") VALUES ( ?,?,? ) ");
                try {
                    cs = dbConnection.prepareStatement(sqlbuf);
                    int i = 1;
                    cs.setString(i++, id);
                    cs.setInt(i++, nextSeq);
                    cs.setInt(i++, block);
                    cs.executeUpdate();
                    dbConnection.commit();
                }
                catch (SQLException e) {
                    System.err.println("Aborted Sequence Updation for " + id);
                    throw e;
                }
                finally {
                   cs.close();
                }

        }


	private void loadComponents() throws IOException, SQLException {
		PreparedStatement cs = null;
		String pId[] = new String[100];
    	String pName[] = new String[100];
    	String pDesc[] = new String[100];
    	String pUnit[] = new String[100];
    	double pCost[] = new double[100];
		String b;
		StringTokenizer st;
		int j = 0, i = 0;

		PurchaseOrder purchaseOrder = new PurchaseOrder(numSites, numSuppliers);
		try {
			cs = dbConnection.prepareStatement(
				"insert into S_component values(" +
					"?,?,?,?,?,?,?,?,?)");
//			((OraclePreparedStatement)cs).setExecuteBatch(100);

			while ((b = ipipe.readLine()) != null) {
				st = new StringTokenizer(b);
				pId[i] = st.nextToken();
				pName[i] = st.nextToken();
				pDesc[i] = st.nextToken();
				pUnit[i] = st.nextToken();
				pCost[i] = Double.valueOf(st.nextToken()).doubleValue();
                                int lead_time = Integer.parseInt(st.nextToken());
                                int container_size = Integer.parseInt(st.nextToken());
				cs.setString(1, pId[i]);
				cs.setString(2, pName[i]);
				cs.setString(3, pDesc[i]);
				cs.setString(4, pUnit[i]);
				cs.setDouble(5, pCost[i]);
                                cs.setInt(6,0);
                                cs.setInt(7,0);
                                cs.setInt(8, lead_time);
                                cs.setInt(9, container_size);

				if (jdbcVersion == 1)
					cs.executeUpdate();
				else
					cs.addBatch();
				j++;	// Keep track of total # of components
				loadSupplierComponents(pId[i], pCost[i], lead_time);
				purchaseOrder.load(pId[i]);

				if (++i == 100) {
					i = 0;
					//	((OraclePreparedStatement)cs).sendBatch();
					if (jdbcVersion != 1)
						cs.executeBatch();
					dbConnection.commit();
				}
			}
			/* Load remaining rows if any */
			if (i != 0) {
				// ((OraclePreparedStatement)cs).sendBatch();
				if (jdbcVersion != 1)
					cs.executeBatch();
				dbConnection.commit();
			}
			loadSequence("purchaseorder", purchaseOrder.getNumberOfPO() + 1, 10000);

		}
		catch (IOException io) {
			System.err.println("Failed to read Parts from supppipe at pId " + pId[0]);
			throw io;
		}
		catch (SQLException e) {
			System.err.println("Aborted component at scale " + scale + " component " + j);
			throw e;
		}
		finally {
			cs.close();
		}
	}

	private void loadSites() throws IOException, SQLException {
		PreparedStatement cs = null;
		int siteId[] = new int[10];
    	String siteName[] = new String[10];
    	String siteStreet1[] = new String[10];
    	String siteStreet2[] = new String[10];
    	String siteCity[] = new String[10];
    	String siteState[] = new String[10];
    	String siteCountry[] = new String[10];
    	String siteZip[] = new String[10];
		String b;
		StringTokenizer st;
		int j = 0;

		try {
			cs = dbConnection.prepareStatement(
				"insert into S_site values(" +
					"?,?,?,?,?,?,?,?)");
			// ((OraclePreparedStatement)cs).setExecuteBatch(10);
			int i = 0;
			for( j = 0; j < numSites; j++) {
				b = ipipe.readLine();
				st = new StringTokenizer(b);
				siteId[i] = Integer.parseInt(st.nextToken());
				siteName[i] = st.nextToken();
				siteStreet1[i] = st.nextToken();
				siteStreet2[i] = st.nextToken();
				siteCity[i] = st.nextToken();
				siteState[i] = st.nextToken();
				siteCountry[i] = st.nextToken();
				siteZip[i] = st.nextToken();
				cs.setInt(1, siteId[i]);
				cs.setString(2, siteName[i]);
				cs.setString(3, siteStreet1[i]);
				cs.setString(4, siteStreet2[i]);
				cs.setString(5, siteCity[i]);
				cs.setString(6, siteState[i]);
				cs.setString(7, siteCountry[i]);
				cs.setString(8, siteZip[i]);
				if (jdbcVersion == 1)
					cs.executeUpdate();
				else
					cs.addBatch();
				if (++i == 1) {
					i = 0;
					// ((OraclePreparedStatement)cs).sendBatch();
					if (jdbcVersion != 1)
						cs.executeBatch();
					dbConnection.commit();
				}
			}
		}
		catch (IOException io) {
			System.err.println("Failed to read Site from supppipe at record " + j);
			throw io;
		}
		catch (SQLException e) {
			System.err.println("Aborted site load site " + j);
			throw e;
		}
		finally {
			cs.close();
		}
	}

	private void loadSuppliers() throws IOException, SQLException {
		PreparedStatement cs = null;
		int suppId[] = new int[10];
    	String suppName[] = new String[10];
    	String suppPhone[] = new String[10];
    	String suppContact[] = new String[10];
    	String suppStreet1[] = new String[10];
    	String suppStreet2[] = new String[10];
    	String suppCity[] = new String[10];
    	String suppState[] = new String[10];
    	String suppCountry[] = new String[10];
    	String suppZip[] = new String[10];
		String b;
		StringTokenizer st;
		int j = 0;

		try {
			cs = dbConnection.prepareStatement(
				"insert into S_supplier values(" +
					"?,?,?,?,?,?,?,?,?,?)");
		//	((OraclePreparedStatement)cs).setExecuteBatch(10);
			int i = 0;
			for( j = 0; j < numSuppliers; j++) {
				b = ipipe.readLine();
				st = new StringTokenizer(b);
				suppId[i] = Integer.parseInt(st.nextToken());
				suppName[i] = st.nextToken();
				suppStreet1[i] = st.nextToken();
				suppStreet2[i] = st.nextToken();
				suppCity[i] = st.nextToken();
				suppState[i] = st.nextToken();
				suppCountry[i] = st.nextToken();
				suppZip[i] = st.nextToken();
				suppPhone[i] = st.nextToken();
				suppContact[i] = st.nextToken();
				cs.setInt(1, suppId[i]);
				cs.setString(2, suppName[i]);
				cs.setString(3, suppStreet1[i]);
				cs.setString(4, suppStreet2[i]);
				cs.setString(5, suppCity[i]);
				cs.setString(6, suppState[i]);
				cs.setString(7, suppCountry[i]);
				cs.setString(8, suppZip[i]);
				cs.setString(9, suppPhone[i]);
				cs.setString(10, suppContact[i]);
				if (jdbcVersion == 1)
					cs.executeUpdate();
				else
					cs.addBatch();
				if (++i == 10) {
					i = 0;
				// ((OraclePreparedStatement)cs).sendBatch();
					if (jdbcVersion != 1)
						cs.executeBatch();
					dbConnection.commit();
				}
			}
			/* Load remaining rows if any */
			if (i != 0) {
				// ((OraclePreparedStatement)cs).sendBatch();
				if (jdbcVersion != 1)
					cs.executeBatch();
				dbConnection.commit();
			}
		}
		catch (IOException io) {
			System.err.println("Failed to read Supplier from supppipe at record " + j);
			throw io;
		}
		catch (SQLException e) {
			System.err.println("Aborted supplier load at supplier " + j);
			throw e;
		}
		finally {
			cs.close();
		}
	}


	private void loadSupplierComponents(String pId, double pCost, int lead_time) 
			throws SQLException {
		String scPId = pId;
		int scSuppId[] = new int[10];
    	double scPrice[] = new double[10];
		int scQty[] = new int[10];
		double scDiscount[] = new double[10];
		int scDelivery[] = new int[10];
		int j = 0;
		boolean first = true, chosen = true;
		PreparedStatement ps = null;

		try {
			if (first) {
				ps = dbConnection.prepareStatement(
					"insert into S_supp_component values(" +
						"?,?,?,?,?,?)");
				first = false;
			}
			/* Choose number of suppliers for this part */
			// int num = rand.random(5, numSuppliers);

			int num = numSuppliers;

			for (int i = 0; i < num; i++) {
				chosen = true;
				while (chosen) {
					scSuppId[i] = rand.random(1, numSuppliers);

					// Verify that this supplier has not been previously chosen
					chosen = false;
					for (j = 0; j < i; j++) {
						if (scSuppId[j] == scSuppId[i]) {
							chosen = true;
							break;
						}
					}
				}
				/* Fill in the rest of the fields */
				/* Choose one supplier to have the current part's pCost field */
				scPrice[i] = pCost;
				scQty[i] = rand.random(10, 20);
				scDiscount[i] = rand.drandom(0.0, 0.50);
				scDelivery[i] = rand.random(1, lead_time);
				ps.setString(1, scPId);
				ps.setInt(2, scSuppId[i]);
				ps.setDouble(3, scPrice[i]);
				ps.setInt(4, scQty[i]);
				ps.setDouble(5, scDiscount[i]);
				ps.setInt(6, scDelivery[i]);
				if (jdbcVersion == 1)
					ps.executeUpdate();
				else
					ps.addBatch();
			}
			// ((OraclePreparedStatement)ps).sendBatch();
			if (jdbcVersion != 1)
				ps.executeBatch();
			dbConnection.commit();
		} catch (SQLException sqe) {
			System.err.println("Aborted SupplierComponent load for component " +
				scPId);
			throw sqe;
		}
		finally {
			ps.close();
		}
	}


	private static class PurchaseOrder {
		int numSites, numSuppliers;
		PreparedStatement ps, pos;
		int poNumber;
		int poSuppId;
		int poSiteId;
		static int polcnt; 
		int polNumber[] = new int[10];
		int polPoId[] = new int[10];
		int polQty[] = new int[10];
		String polPId[] = new String[10];
		String polMessage[] = new String[10];
		double polBalance[] = new double[10];
		static int i = 0, j = 0;
		static boolean first = true;
		static boolean newpo = true;
		java.util.Date curDate;
		java.sql.Date polDelDate[] = new java.sql.Date[10];

		public PurchaseOrder(int numSites, int numSuppliers) {
			this.numSites = numSites;
			this.numSuppliers = numSuppliers;
		}

		public int getNumberOfPO() {
			return poNumber;
		}

		private void load(String pId) throws SQLException {
		try {
			if (first) {
				ps = dbConnection.prepareStatement(
					"insert into S_purchase_order values(" +
						"?,?,?)");
				pos = dbConnection.prepareStatement(
					"insert into S_purchase_orderline values(" +
						"?,?,?,?,?,?,?)");
				first = false;
			}

			// Create a purchase order for 10% of the parts
			int x = rand.random(1, 100);
			if ( x > 10)
				return;
			if (newpo) {
				poNumber = ++j;
				poSiteId = rand.random(1, numSites);
				poSuppId = rand.random(1, numSuppliers);
				ps.setInt(1, poNumber);
				ps.setInt(2, poSuppId);
				ps.setInt(3, poSiteId);
				ps.executeUpdate();

				polcnt = rand.random(1, 9);
				i = 0;
				newpo = false;
			}
			polNumber[i] = i + 1;
			polPoId[i] = poNumber;
			polPId[i] = pId;
			polQty[i] = rand.random(1, 10000);
			polBalance[i] = 0;
			polMessage[i] = rand.makeAString(25, 100);
			curDate = new java.util.Date();
			polDelDate[i] = new java.sql.Date(
					(curDate.getTime() + 90*24*60*60000));
			pos.setInt(1, polNumber[i]);
			pos.setInt(2, polPoId[i]);
			pos.setString(3, polPId[i]);
			pos.setInt(4, polQty[i]);
			pos.setDouble(5, polBalance[i]);
			pos.setDate(6, polDelDate[i]);
			pos.setString(7, polMessage[i]);
			if (jdbcVersion == 1)
				pos.executeUpdate();
			else
				pos.addBatch();
			if (++i == polcnt) {
			// Finished all purchase_orderlines for this PO
			//	((OraclePreparedStatement)ps).sendBatch();
			//	((OraclePreparedStatement)pos).sendBatch();
				if (jdbcVersion != 1) {
					ps.executeBatch();
					pos.executeBatch();
				}
				dbConnection.commit();
				newpo = true;
			}
		} catch (SQLException e) {
			System.err.println("Aborted Purchase Order load at record " + j);
			throw e;
		}
	}
	}
}
