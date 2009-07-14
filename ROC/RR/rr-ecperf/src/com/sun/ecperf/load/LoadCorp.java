/**
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * $Id: LoadCorp.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 * Main function for the EJBPerf load program for the Corp database
 *
 * @author Shanti Subramanyam
 */


package com.sun.ecperf.load;

import java.lang.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.math.BigDecimal;

class LoadCorp {
	static int scale;
	static int jdbcVersion = 1;
	RandNum rand;
	static Connection dbConnection;

	static FileOutputStream opipe, mpipe, spipe;

	LoadCorp()
	{
		rand = new RandNum();
	}

	private static void usage() {
		System.err.println("Usage: java com.sun.ecperf.load.LoadCorp <orders_injection_rate>");
		System.err.println("       1 site, 10 suppliers, 75C customers, 11P parts");
		System.err.println("       where C and P are defined in Clause 4.2");

	}


	public static void main(String [] argv) 
		throws Exception {
		LoadCorp l = new LoadCorp();
		if (argv.length != 1) {
			usage();
			return;
		}
		scale = Integer.parseInt(argv[0]);

    	// establish the database connection
    	// via the appropriate jdbc driver
    	dbConnection = DatabaseConnection.getConnection("corpdb.properties");
		jdbcVersion = DatabaseConnection.getVersion();
		dbConnection.setAutoCommit(false);

		// Create pipe to write data to other db loads
                String pipeDir = DatabaseConnection.getPipeDir() + File.separator; 
		opipe = new FileOutputStream(pipeDir + "ordspipe");
		mpipe = new FileOutputStream(pipeDir + "mfgpipe");
		spipe = new FileOutputStream(pipeDir + "supppipe");

		l.cleanAll();
		l.loadAll();
	}

	private void cleanAll() throws IOException, SQLException {
		PreparedStatement cs = null;
                try {
                    cs = dbConnection.prepareStatement("truncate table C_customer");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table C_parts");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table C_supplier");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table C_site");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("truncate table C_rule");
                    cs.executeUpdate();
                }
                catch (SQLException e) {
                    cs = dbConnection.prepareStatement("delete from C_customer");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from C_parts");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from C_supplier");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from C_site");
                    cs.executeUpdate();
                    cs = dbConnection.prepareStatement("delete from C_rule");
                    cs.executeUpdate();
                }
                dbConnection.commit();

	}

	private void loadAll() throws IOException, SQLException {
		int numSites = 1;
		int numSuppliers = 10;
		int C = (int)(Math.ceil((double)scale/10.0)) * 10;
		int P = (int)(Math.ceil((double)scale/100.0)) * 100;
		int numCustomers = 75 * C;
		int numAssemblies = P;	// number of assemblies
		loadSite(numSites);
		loadSupplier(numSuppliers);
		loadCustomer(numCustomers);
		loadParts(numAssemblies);
	}

      
	private void loadSequence(String id, int nextSeq, int block) throws SQLException {
                PreparedStatement cs = null;
	
		// First delete the row if it exists
		try {
		   String tmp = new String("DELETE FROM U_sequeces WHERE S_ID = '" + id + "'");
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


	private void loadCustomer(int numCustomers) throws IOException, SQLException {
		PreparedStatement cs = null;
		int cId[] = new int[100];
    	String cFirst[] = new String[100];
    	String cLast[] = new String[100];
    	String cPhone[] = new String[100];
    	String cContact[] = new String[100];
    	String cCredit[] = new String[100];
    	String cStreet1[] = new String[100];
    	String cStreet2[] = new String[100];
    	String cCity[] = new String[100];
    	String cState[] = new String[100];
    	String cCountry[] = new String[100];
    	String cZip[] = new String[100];
    	java.sql.Date cSince[] = new java.sql.Date[100];
	double cBalance[] = new double[100];
	double cYtdPayment[] = new double[100];
    	double cCreditLimit[] = new double[100];
		Address adr;
		StringBuffer s;
		byte b[];
		int i = 0;

                CardDeck custDeck = new CardDeck(1, numCustomers);
                int numBadCredit = numCustomers / 10;
		try {
			cs = dbConnection.prepareStatement(
				"insert into C_customer values(" +
					"?,?,?,?,?,?,?,?,?,?,?," +
					"?,?,?,?,?)");

//			((OraclePreparedStatement)cs).setExecuteBatch(100);

			for (int cid = 1; cid <= numCustomers; cid++) {

				cId[i] = cid;
				cLast[i] = rand.makeAString(8, 16);
				cFirst[i] = rand.makeAString(8, 16);
				adr = new Address();
				cStreet1[i] = adr.street1;
				cStreet2[i] = adr.street2;
				cCity[i] = adr.city;
				cState[i] = adr.state;
				cCountry[i] = adr.country;
				cZip[i] = adr.zip;
				cPhone[i] = adr.phone;
				long tm = System.currentTimeMillis();
				// Range is back ~7 yrs. 
				tm = rand.lrandom(
				     tm - 7l * 365 * 24 * 60 * 60 * 1000, tm);
				cSince[i] = new java.sql.Date(tm);
				cBalance[i] = rand.random(0, 25000);
			        if (cBalance[i] < 500)
				    cBalance[i] = 0;

			        cYtdPayment[i] = rand.random(0, 350000);
                                /****
                                 * Changing this to use more deterministic card deck
                                 * algorithm to generate customers with bad credit
				int x = rand.random(1, 100);
				if ( x <= 10) {
                                 */
                                if (custDeck.nextCard() <= numBadCredit) {
					cCredit[i] = "BC";
					cCreditLimit[i] = 0;
				}
				else {
				// Credit limit should be large enough to encompass
				// orders. A regular customer order is on avg. 11,250
				// (based on a widget price of $750) and a large order
				// is 112,500
					cCredit[i] = "GC";
                                /***
                                 * The calculations should be done in terms of max. and
                                 * not avg. So the max. order totals would be 20*1500 =
                                 * 30000 for regular orders and 300,000 for largeorders
                                 * bugid 4482440.
                           
					cCreditLimit[i] = rand.drandom(100000, 1000000);
                                 */
					cCreditLimit[i] = rand.drandom(300000, 3000000);
				}
				cContact[i] = rand.makeAString(10, 25);
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
				cs.setDouble(13, cBalance[i]);
				cs.setString(14, cCredit[i]);
				cs.setDouble(15, cCreditLimit[i]);
				cs.setDouble(16, cYtdPayment[i]);
//				System.out.println(cid + " cfirst=" + cFirst[i] + 
//				" clast=" + cLast[i] + " cstreet1=" + cStreet1[i] +
//				" cstreet2=" + cStreet2[i] + " cCity=" + cCity[i] +
//				" ccountry=" + cCountry[i] + " cZip=" + cZip[i]);

				s = new StringBuffer(cid + " " + cFirst[i] + " " +
						cLast[i] + " " + cStreet1[i] + " " +
						cStreet2[i] + " " + cCity[i] + " " +
						cState[i] + " " + cCountry[i] + " " +
						cZip[i] + " " + cPhone[i] + " " +
						cContact[i] + " "  + cSince[i] + "\n");
				b = (s.toString()).getBytes();
//				System.out.println("writing " + s + " to pipe");
				opipe.write(b);
				if (jdbcVersion == 1)
					cs.executeUpdate();
				else
					cs.addBatch();
				if (++i == 100) {
					i = 0;
//					((OraclePreparedStatement)cs).sendBatch();
					if (jdbcVersion != 1)
						cs.executeBatch();
					dbConnection.commit();
				}
			}
			if ( i != 0) {	// Load any remaining rows
				if (jdbcVersion != 1)
					cs.executeBatch();
				dbConnection.commit();
			}
		}
		catch(IOException io) {
			System.err.println("Error while writing customer " + i +
				" to ords pipe. Aborting load");
			throw io;
		}
		catch (SQLException e) {
			System.err.println("Aborted customer at cId " + cId[0]);
			throw e;
		}
		finally {
			cs.close();
		}
	}



	private void loadParts(int numAssemblies) throws IOException, SQLException {
		PreparedStatement cs = null;
		String pId[] = new String[100];
    	String pName[] = new String[100];
    	String pDesc[] = new String[100];
    	String pRev[] = new String[100];
    	String pUnit[] = new String[100];
    	double pCost[] = new double[100];
    	double pPrice[] = new double[100];
		int pPlanner[] = new int[100];
		int pType[] = new int[100];
		int pInd[] = new int[100];
		int pLeadTime[] = new int[100];
        double pSafetyStock[] = new double [100];
        int pContainerSize [] = new int [100];
        int pDemand [] = new int [100];
        int pHiMark [] = new int [100];

		RandPart rp = new RandPart();
		StringBuffer obuf = null, mbuf = null, sbuf = null;
		byte b[];
		int i = 0;
		int curBatch = 1, numComponents;

		try {
			cs = dbConnection.prepareStatement(
				"insert into C_parts (p_id, p_name, " +
				"p_desc, p_rev, p_unit, p_cost, p_price, " +
				"p_planner, p_type, p_ind) values(" +
				"?,?,?,?,?,?,?,?,?,?)");

//			((OraclePreparedStatement)cs).setExecuteBatch(100);

			for (int j = 0; j < numAssemblies; j++) {
				/*
				 * We'll leave this for when we use multiple PGs
				 * For now, p1 is always 1 (in RandPart)
				curBatch = j / 100 + 1;	// compute value of p1
				*/
				numComponents = rp.mkAssembly(curBatch);
				for (int k = 0; k <= numComponents; k++) {
					pId[i] = rp.partId[k];
					pName[i] = rand.makeAString(5, 10);
					pDesc[i] = rand.makeAString(30, 100);
					pUnit[i] = rand.makeAString(2, 10);
					pRev[i] = rp.partRev[k];
					pType[i] = rp.partType[k];
					pInd[i] = rp.partInd[k];

					pLeadTime[i] = rand.random(2,10);
					pSafetyStock[i] = rand.drandom(0.125, 0.175);
					/*
					 * pDemand is set such that we will
					 * only call out to supplier for every
					 * 5 workorders. This is achieved
					 * by also setting lomark = himark/2
					 */
					pDemand[i] = rand.random(100 * scale, 334 * scale);
                	pHiMark[i] = (int)Math.ceil((double)pDemand[i] * 
					(double)pLeadTime[i] * (1.0 + pSafetyStock[i]));
					// pContainerSize[i] = rand.random((int)(pHiMark[i]*.25), (int)(pHiMark[i]*.75));

					pContainerSize[i] = pHiMark[i] / 2;

					// Only manufactured parts will have a price
					if (pInd[i] == 1) {
						pCost[i] = rand.drandom(.10, 1000.00);
						pPrice[i] = pCost[i] * 1.5;
					}
					else {
						pCost[i] = rand.drandom(.10, 100.00);
						pPrice[i] = 0;
					}
					pPlanner[i] = rand.random(1, 100000);

					cs.setString(1, pId[i]);
					cs.setString(2, pName[i]);
					cs.setString(3, pDesc[i]);
					cs.setString(4, pRev[i]);
					cs.setString(5, pUnit[i]);
					cs.setDouble(6, pCost[i]);
					cs.setDouble(7, pPrice[i]);
					cs.setInt(8, pPlanner[i]);
					cs.setInt(9, pType[i]);
					cs.setInt(10, pInd[i]);
	
					// We tailor the info written to each pipe based on what's
					// required for that db's schema
					obuf = new StringBuffer(pId[i] + " " + pName[i] + " " +
							pDesc[i] + " " + pPrice[i] + "\n");
					mbuf = new StringBuffer(pId[i] + " " + pName[i] + " " +
							pDesc[i] + " " + pRev[i] + " " + pPlanner[i] +
							" " + pType[i] + " " + pInd[i] + " " + pContainerSize[i] +
							" " + pHiMark[i] + "\n");

					sbuf = new StringBuffer(pId[i] + " " + pName[i] + " " +
							pDesc[i] + " " + pUnit[i] + " " + pCost[i] +
							" " + pLeadTime[i] + " " + pContainerSize[i] + "\n");
					if (jdbcVersion == 1)
						cs.executeUpdate();
					else
						cs.addBatch();
					// write out mfg parts (assemblies) to ords pipe
					if (pInd[i] == 1) {
						b = (obuf.toString()).getBytes();
						opipe.write(b);
					}
					else {	// write out purchased parts (components) to supp pipe
						b = (sbuf.toString()).getBytes();
						spipe.write(b);
					}
					b = (mbuf.toString()).getBytes();
					mpipe.write(b);	// write all parts to mfg pipe
				
					if (++i == 100) {
						i = 0;
	//					((OraclePreparedStatement)cs).sendBatch();
						if (jdbcVersion != 1)
							cs.executeBatch();
						dbConnection.commit();
					}
				}
			}
			if (i != 0) {
				if (jdbcVersion != 1)
					cs.executeBatch();
				dbConnection.commit();
			}
		}
		catch (IOException io) {
			System.err.println("Error while writing parts to pipe. Aborting load");
			throw io;
		}
		catch (SQLException e) {
			System.err.println("Aborted parts at part " + pId[0]);
			throw e;
		}
		finally {
			cs.close();
		}
	}


	private void loadSupplier(int numSuppliers) throws IOException, SQLException {
		PreparedStatement cs = null;
		int suppId[] = new int[100];
    	String suppName[] = new String[100];
    	String suppPhone[] = new String[100];
    	String suppContact[] = new String[100];
    	String suppStreet1[] = new String[100];
    	String suppStreet2[] = new String[100];
    	String suppCity[] = new String[100];
    	String suppState[] = new String[100];
    	String suppCountry[] = new String[100];
    	String suppZip[] = new String[100];
		Address adr;
		StringBuffer sbuf = null;
		byte b[];

		try {
			cs = dbConnection.prepareStatement(
				"insert into C_supplier values(" +
					"?,?,?,?,?,?,?,?,?,?)");

//			((OraclePreparedStatement)cs).setExecuteBatch(10);

			int i = 0;
			for (int sid = 1; sid <= numSuppliers; sid++) {

				suppId[i] = sid;
				suppName[i] = rand.makeAString(8, 16);
				adr = new Address();
				suppStreet1[i] = adr.street1;
				suppStreet2[i] = adr.street2;
				suppCity[i] = adr.city;
				suppState[i] = adr.state;
				suppCountry[i] = adr.country;
				suppZip[i] = adr.zip;
				suppPhone[i] = adr.phone;
				suppContact[i] = rand.makeAString(10, 25);
				cs.setInt(1, sid);
				cs.setString(2, suppName[i]);
				cs.setString(3, suppStreet1[i]);
				cs.setString(4, suppStreet2[i]);
				cs.setString(5, suppCity[i]);
				cs.setString(6, suppState[i]);
				cs.setString(7, suppCountry[i]);
				cs.setString(8, suppZip[i]);
				cs.setString(9, suppPhone[i]);
				cs.setString(10, suppContact[i]);
				sbuf = new StringBuffer(sid + " " + suppName[i] + " " +
					suppStreet1[i] + " " + suppStreet2[i] + " " +
					suppCity[i] + " " + suppState[i] + " " +
					suppCountry[i] + " " + suppZip[i] + " " +
					suppPhone[i] + " " + suppContact[i] + "\n");
				if (jdbcVersion == 1)
					cs.executeUpdate();
				else
					cs.addBatch();
				b = (sbuf.toString()).getBytes();
				spipe.write(b);
				if (++i == 10) {
					i = 0;
//					((OraclePreparedStatement)cs).sendBatch();
					if (jdbcVersion != 1)
						cs.executeBatch();
					dbConnection.commit();
				}
			}
		}
		catch (IOException io) {
			System.err.println("Error while writing Supplier to supplier pipe. Aborting load");
			throw io;
		}
		catch (SQLException e) {
			System.err.println("Aborted supplier at suppId " + suppId[0]);
			throw e;
		}
		finally {
			cs.close();
		}
	}


	private void loadSite(int numSites) throws IOException, SQLException {
		PreparedStatement cs = null;
		int siteId[] = new int[100];
    	String siteName[] = new String[100];
    	String siteStreet1[] = new String[100];
    	String siteStreet2[] = new String[100];
    	String siteCity[] = new String[100];
    	String siteState[] = new String[100];
    	String siteCountry[] = new String[100];
    	String siteZip[] = new String[100];
		Address adr;
		StringBuffer sbuf = null;
		byte b[];

		try {
			cs = dbConnection.prepareStatement(
				"insert into C_site values(" +
					"?,?,?,?,?,?,?,?)");

//			((OraclePreparedStatement)cs).setExecuteBatch(10);

			int i = 0;
			for (int sid = 1; sid <= numSites; sid++) {

				siteId[i] = sid;
				siteName[i] = rand.makeAString(8, 16);
				adr = new Address();
				siteStreet1[i] = adr.street1;
				siteStreet2[i] = adr.street2;
				siteCity[i] = adr.city;
				siteState[i] = adr.state;
				siteCountry[i] = adr.country;
				siteZip[i] = adr.zip;

				cs.setInt(1, sid);
				cs.setString(2, siteName[i]);
				cs.setString(3, siteStreet1[i]);
				cs.setString(4, siteStreet2[i]);
				cs.setString(5, siteCity[i]);
				cs.setString(6, siteState[i]);
				cs.setString(7, siteCountry[i]);
				cs.setString(8, siteZip[i]);
				sbuf = new StringBuffer(sid + " " + siteName[i] + " " +
					siteStreet1[i] + " " + siteStreet2[i] + " " +
					siteCity[i] + " " + siteState[i] + " " +
					siteCountry[i] + " " + siteZip[i] + "\n");
				if (jdbcVersion == 1)
					cs.executeUpdate();
				else
					cs.addBatch();
				b = (sbuf.toString()).getBytes();
				spipe.write(b);
				if (++i == 1) {
					i = 0;
//					((OraclePreparedStatement)cs).sendBatch();
					if (jdbcVersion != 1)
						cs.executeBatch();
					dbConnection.commit();
				}
			}
		}
		catch (IOException io) {
			System.err.println("IO error while writing Site to supplier pipe");
			throw io;
		}
		catch (SQLException e) {
			System.err.println("Aborted site at siteId " + siteId[0]);
			throw e;
		}
		finally {
			cs.close();
		}
	}
}
