/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrdersReport.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
import java.io.*;
import java.util.*;

/**
 * This class is the Report generator for the Orders application. 
 * The genReport method is called with an array of aggregate results
 * from each agent and it generates the output reports - 
 * a summary report and a detail report.
 * The summary report contains a summary of the numerical quantities -
 * transaction counts, rates, response and cycle times.
 * The detail report contains the histogram data to draw the various graphs -
 * throughput, response times and cycle times.
 *
 * @author Shanti Subramanyam
 * @see driver.EcperfReport 
 * @see driver.MfgReport
 */
public class OrdersReport {
	String prefix;
	String summary, detail;

	double sumNewoResp = 0, sumNewoTargetedCycle = 0, sumNewoCycle = 0; 
	int sumNewoCount = 0, maxNewoResp = 0, maxNewoCycle = 0, minNewoCycle = 100000;
	int sumNewoBuyCart = 0, sumNewoBadCredit = 0, sumChgoBadCredit = 0, sumCancelOrdCnt = 0;
	int sumNewoLrgCnt = 0, sumNewoOlCnt = 0, sumNewoLrgOlCnt = 0;
	double sumChgoResp = 0, sumChgoTargetedCycle = 0, sumChgoCycle = 0; 
	int sumChgoCount = 0, maxChgoResp = 0, maxChgoCycle = 0, minChgoCycle = 100000;
	double sumOrdsResp = 0, sumOrdsTargetedCycle = 0, sumOrdsCycle = 0; 
	int sumOrdsCount = 0, maxOrdsResp = 0, maxOrdsCycle = 0, minOrdsCycle = 100000;
	double sumCustsResp = 0, sumCustsTargetedCycle = 0, sumCustsCycle = 0; 
	int sumCustsCount = 0, maxCustsResp = 0, maxCustsCycle = 0, minCustsCycle = 100000;
	int sumNewoThruHist[] = new int[OrdersStats.THRUMAX];
	int sumNewoRespHist[] = new int[OrdersStats.RESPMAX];
	int sumNewoCycleHist[] = new int[OrdersStats.CYCLEMAX];
	int sumNewoTargetedCycleHist[] = new int[OrdersStats.CYCLEMAX];
	int sumChgoThruHist[] = new int[OrdersStats.THRUMAX];
	int sumChgoRespHist[] = new int[OrdersStats.RESPMAX];
	int sumChgoCycleHist[] = new int[OrdersStats.CYCLEMAX];
	int sumChgoTargetedCycleHist[] = new int[OrdersStats.CYCLEMAX];
	int sumOrdsThruHist[] = new int[OrdersStats.THRUMAX];
	int sumOrdsRespHist[] = new int[OrdersStats.RESPMAX];
	int sumOrdsCycleHist[] = new int[OrdersStats.CYCLEMAX];
	int sumOrdsTargetedCycleHist[] = new int[OrdersStats.CYCLEMAX];
	int sumCustsThruHist[] = new int[OrdersStats.THRUMAX];
	int sumCustsRespHist[] = new int[OrdersStats.RESPMAX];
	int sumCustsCycleHist[] = new int[OrdersStats.CYCLEMAX];
	int sumCustsTargetedCycleHist[] = new int[OrdersStats.CYCLEMAX];

	int users = 0, stdyState, txRate;

        List dumpStreams;
        int dumpInterval;
        int rampUp;
        int prevTxCnt = 0;
        double avgTps = 0;
	private static int elapsed = 0;
	private static int thruIndex = 0;

	public OrdersReport() {
	}

	/**
	 * This constructor is used for dumping data for charting
	 */
	public OrdersReport(String file, int dumpInterval, int rampUp)
                throws IOException {
            dumpStreams = Collections.synchronizedList(new ArrayList());
            dumpStreams.add(new DataOutputStream(
                                     new FileOutputStream(file)));
            this.dumpInterval = dumpInterval;
            this.rampUp = rampUp;
	}

        /**
         * New constructor used for dumping data for charting
         */
        public OrdersReport(List dumpStreams, int dumpInterval, int rampUp) {
            this.dumpStreams = dumpStreams;
            this.dumpInterval = dumpInterval;
            this.rampUp = rampUp;
        }


	/*
	 * This method is called by the Driver every time it wants to dump
	 * the thruput data out to files
	 */
	void dumpStats(OrdersAggStats[] aggs) {
		int txCnt = 0; 
		double tps = 0;

                for (int stream = 0; stream < dumpStreams.size(); stream++) {
                    DataOutputStream dumpStream = (DataOutputStream)
                                                  dumpStreams.get(stream);
                    try {
                        dumpStream.writeDouble(elapsed);
                    } catch (IOException e) {
                        dumpStreams.remove(stream--);
                        closeStream(dumpStream);
                        e.printStackTrace();
                        System.err.println("Error writing Orders stats.\n" +
                            "Closing stream and removing stream from list.\n" +
                            "Benchmark continues without interruption.");
                    }
                }

                // Get the aggregate tx
		for (int i = 0; i < aggs.length; i++) {
			txCnt += aggs[i].txCnt[OrdersStats.NEWORDER];
			txCnt += aggs[i].txCnt[OrdersStats.CHGORDER];
			txCnt += aggs[i].txCnt[OrdersStats.ORDERSTATUS];
			txCnt += aggs[i].txCnt[OrdersStats.CUSTSTATUS];
		}

                // Dump the immediate tps;
                tps = (double) (txCnt - prevTxCnt) / dumpInterval;

                for (int stream = 0; stream < dumpStreams.size(); stream++) {
                    DataOutputStream dumpStream = (DataOutputStream)
                                                  dumpStreams.get(stream);
                    try {
		        dumpStream.writeDouble(tps);
                    } catch (IOException e) {
                        dumpStreams.remove(stream--);
                        closeStream(dumpStream);
                        e.printStackTrace();
                    }
                }

		// Now dump out the old average tps
                for (int stream = 0; stream < dumpStreams.size(); stream++) {
                    DataOutputStream dumpStream = (DataOutputStream)
                                                  dumpStreams.get(stream);
                    try {
		        dumpStream.writeDouble(avgTps);
                    } catch (IOException e) {
                        dumpStreams.remove(stream--);
                        closeStream(dumpStream);
                        e.printStackTrace();
                    }
                }

		elapsed += dumpInterval;

                for (int stream = 0; stream < dumpStreams.size(); stream++) {
                    DataOutputStream dumpStream = (DataOutputStream)
                                                  dumpStreams.get(stream);
                    try {
                        dumpStream.writeDouble(elapsed);
                    } catch (IOException e) {
                        dumpStreams.remove(stream--);
                        closeStream(dumpStream);
                        e.printStackTrace();
                        System.err.println("Error writing Orders stats.\n" +
                            "Closing stream and removing stream from list.\n" +
                            "Benchmark continues without interruption.");
                    }
                }

                // Dump the immediate tps;
                for (int stream = 0; stream < dumpStreams.size(); stream++) {
                    DataOutputStream dumpStream = (DataOutputStream)
                                                  dumpStreams.get(stream);
                    try {
		        dumpStream.writeDouble(tps);
                    } catch (IOException e) {
                        dumpStreams.remove(stream--);
                        closeStream(dumpStream);
                        e.printStackTrace();
                    }
                }

		// Now dump out the average tps
		if (elapsed <= rampUp)
			avgTps = 0;
		else
			avgTps = (double)txCnt / (elapsed - rampUp);

                for (int stream = 0; stream < dumpStreams.size(); stream++) {
                    DataOutputStream dumpStream = (DataOutputStream)
                                                  dumpStreams.get(stream);
                    try {
		        dumpStream.writeDouble(avgTps);
		        dumpStream.flush();
                    } catch (IOException e) {
                        dumpStreams.remove(stream--);
                        closeStream(dumpStream);
                        e.printStackTrace();
                    }
                }

		thruIndex++;	// each time it is called, we dump the next interval
                prevTxCnt = txCnt;
	}

        private void closeStream(OutputStream s) {
            try {
                s.close();
            } catch (IOException e) {
            }
        }


	/**
	 * Method : genReport
	 * This method is called from ECperfReport to generate the report
	 * for the OrdersApp. 
	 * @param aggs - Array of OrdersAggStats objects, one from each agent
	 * @return double - txPerMin
	 */
	public double genReport(OrdersAggStats[] aggs) throws IOException {
		BufferedReader bufp;

		String resultsDir = aggs[0].resultsDir;
		String filesep = System.getProperty("file.separator");
		summary = resultsDir + filesep + "Orders.summary";
		detail = resultsDir + filesep + "Orders.detail";
		PrintStream sump = new PrintStream(new FileOutputStream(summary));
		PrintStream detailp = new PrintStream(new FileOutputStream(detail));
		int i = 0;

		for (i = 0; i < aggs.length; i++) {
		//	bufp = new BufferedReader(new FileReader(files[i]));
			processStats(aggs[i]);
			users += aggs[i].threadCnt;
		//	bufp.close();
		}
		stdyState = aggs[0].stdyState;
		txRate = aggs[0].txRate;
		Debug.println("OrdersReport: Printing Summary report...");

		double txPerMin = printSummary(sump);
		Debug.println("OrdersReport: Summary finished. Now printing detail ...");
		printDetail(detailp);
		return(txPerMin);
	}


	private void processStats(OrdersAggStats agg) {
		String s;
		int m, i, j;
		
		sumNewoCount += agg.txCnt[OrdersStats.NEWORDER];
		sumNewoBuyCart += agg.sumNewoBuyCart;
                sumCancelOrdCnt += agg.sumCancelOrdCnt;
		sumNewoBadCredit += agg.sumNewoBadCredit;
                sumChgoBadCredit += agg.sumChgoBadCredit;
		sumNewoLrgCnt += agg.sumNewoLrgCount;
		sumNewoResp += agg.respSum[OrdersStats.NEWORDER];
		if ( agg.respMax[OrdersStats.NEWORDER] > maxNewoResp)
			maxNewoResp = agg.respMax[OrdersStats.NEWORDER];
		sumNewoCycle += agg.cycleSum[OrdersStats.NEWORDER];
		sumNewoTargetedCycle += agg.targetedCycleSum[OrdersStats.NEWORDER];
		if ( agg.cycleMax[OrdersStats.NEWORDER] > maxNewoCycle)
			maxNewoCycle = agg.cycleMax[OrdersStats.NEWORDER];
		if ( agg.cycleMin[OrdersStats.NEWORDER] < minNewoCycle)
			minNewoCycle = agg.cycleMin[OrdersStats.NEWORDER];
		sumNewoOlCnt += agg.sumNewoOlCnt;
		sumNewoLrgOlCnt += agg.sumNewoLrgOlCnt;

		sumChgoCount += agg.txCnt[OrdersStats.CHGORDER];
		sumChgoResp += agg.respSum[OrdersStats.CHGORDER];
		if ( agg.respMax[OrdersStats.CHGORDER] > maxChgoResp)
			maxChgoResp = agg.respMax[OrdersStats.CHGORDER];
		sumChgoCycle += agg.cycleSum[OrdersStats.CHGORDER];
		sumChgoTargetedCycle += agg.targetedCycleSum[OrdersStats.CHGORDER];
		if (agg.cycleMax[OrdersStats.CHGORDER] > maxChgoCycle)
			maxChgoCycle = agg.cycleMax[OrdersStats.CHGORDER];
		if ( agg.cycleMin[OrdersStats.CHGORDER] < minChgoCycle)
			minChgoCycle = agg.cycleMin[OrdersStats.CHGORDER];

		sumOrdsCount += agg.txCnt[OrdersStats.ORDERSTATUS];
		sumOrdsResp += agg.respSum[OrdersStats.ORDERSTATUS];
		if ( agg.respMax[OrdersStats.ORDERSTATUS] > maxOrdsResp)
			maxOrdsResp = agg.respMax[OrdersStats.ORDERSTATUS];
		sumOrdsCycle += agg.cycleSum[OrdersStats.ORDERSTATUS];
		sumOrdsTargetedCycle += agg.targetedCycleSum[OrdersStats.ORDERSTATUS];
		if (agg.cycleMax[OrdersStats.ORDERSTATUS] > maxOrdsCycle)
			maxOrdsCycle = agg.cycleMax[OrdersStats.ORDERSTATUS];
		if ( agg.cycleMin[OrdersStats.ORDERSTATUS] < minOrdsCycle)
			minOrdsCycle = agg.cycleMin[OrdersStats.ORDERSTATUS];

		sumCustsCount += agg.txCnt[OrdersStats.CUSTSTATUS];
		sumCustsResp += agg.respSum[OrdersStats.CUSTSTATUS];
		if ( agg.respMax[OrdersStats.CUSTSTATUS] > maxCustsResp)
			maxCustsResp = agg.respMax[OrdersStats.CUSTSTATUS];
		sumCustsCycle += agg.cycleSum[OrdersStats.CUSTSTATUS];
		sumCustsTargetedCycle += agg.targetedCycleSum[OrdersStats.CUSTSTATUS];
		if (agg.cycleMax[OrdersStats.CUSTSTATUS] > maxCustsCycle)
			maxCustsCycle = agg.cycleMax[OrdersStats.CUSTSTATUS];
		if (agg.cycleMin[OrdersStats.CUSTSTATUS] < minCustsCycle)
			minCustsCycle = agg.cycleMin[OrdersStats.CUSTSTATUS];

		/* Now get the histogram data */
		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumNewoRespHist[j] += agg.respHist[OrdersStats.NEWORDER][j];
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumNewoThruHist[j] += agg.thruputHist[OrdersStats.NEWORDER][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumNewoCycleHist[j] += agg.cycleHist[OrdersStats.NEWORDER][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumNewoTargetedCycleHist[j] += agg.targetedCycleHist[OrdersStats.NEWORDER][j];

		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumChgoRespHist[j] += agg.respHist[OrdersStats.CHGORDER][j];
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumChgoThruHist[j] += agg.thruputHist[OrdersStats.CHGORDER][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumChgoCycleHist[j] += agg.cycleHist[OrdersStats.CHGORDER][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumChgoTargetedCycleHist[j] += agg.targetedCycleHist[OrdersStats.CHGORDER][j];

		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumOrdsRespHist[j] += agg.respHist[OrdersStats.ORDERSTATUS][j];
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumOrdsThruHist[j] += agg.thruputHist[OrdersStats.ORDERSTATUS][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumOrdsCycleHist[j] += agg.cycleHist[OrdersStats.ORDERSTATUS][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumOrdsTargetedCycleHist[j] += agg.targetedCycleHist[OrdersStats.ORDERSTATUS][j];

		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumCustsRespHist[j] += agg.respHist[OrdersStats.CUSTSTATUS][j];
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumCustsThruHist[j] += agg.thruputHist[OrdersStats.CUSTSTATUS][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumCustsCycleHist[j] += agg.cycleHist[OrdersStats.CUSTSTATUS][j];
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumCustsTargetedCycleHist[j] += agg.targetedCycleHist[OrdersStats.CUSTSTATUS][j];
	}


	// Print summary report
	private double printSummary(PrintStream p) {
		double txcnt = 0, txPerMin = 0;
		double newoPer = 0, chgoPer = 0, ordsPer = 0, custsPer = 0;
		boolean success = true;
		double avg, tavg, resp90;
		int sumtx, cnt90;
		boolean fail90 = false, failavg =false;
		int i;
		String passStr = null;
		double newoCycle = 0, chgoCycle = 0, ordsCycle = 0, custsCycle = 0;
                p.println();
                p.println("\t\t\tOrders Summary Report");
                p.println("\t\t\tVersion : " + ECperfReport.version);
                p.println();

		txcnt = sumNewoCount + sumChgoCount + sumOrdsCount + sumCustsCount;
                if(txcnt > 0) {
		    txPerMin = txcnt * 1000 * 60 / stdyState;
		    newoPer = (sumNewoCount*100) / txcnt;
		    chgoPer = (sumChgoCount*100) / txcnt;
		    ordsPer = (sumOrdsCount*100) / txcnt;
		    custsPer = (sumCustsCount*100) / txcnt;
                }
		p.print("Orders Transaction Rate : "); 
		Debug.println("Transactions/min = " + txPerMin);
		Format.print(p, "%.02f Transactions/min", txPerMin);
		p.println();
		p.println();
		p.println("TRANSACTION MIX\n");
		p.println("Total number of transactions = " + (int)txcnt);
		p.println("TYPE\t\tTX. COUNT\tMIX\t\tREQD. MIX.(5% Deviation Allowed)");
		p.println("----\t\t---------\t---\t\t----------");
		Format.print(p, "NewOrder:\t%05d\t\t", sumNewoCount);
		if (newoPer < 47.5 || newoPer > 52.5) {
			success = false;
			passStr = "FAILED";
		}
		else 
			passStr = "PASSED";
		Format.print(p, "%5.02f%\t\t50%\t", newoPer);
		Format.print(p, "%s\n", passStr);
		Format.print(p, "ChangeOrder:\t%05d\t\t", sumChgoCount);
		if (chgoPer < 19 || chgoPer > 21) {
			success = false;
			passStr = "FAILED";
		}
		else 
			passStr = "PASSED";
		Format.print(p, "%5.02f%\t\t20%\t", chgoPer);
		Format.print(p, "%s\n", passStr);
		Format.print(p, "OrderStatus:\t%05d\t\t", sumOrdsCount);
		if (ordsPer < 19 || ordsPer > 21) {
			success = false;
			passStr = "FAILED";
		}
		else 
			passStr = "PASSED";
		Format.print(p, "%5.02f%\t\t20%\t", ordsPer);
		Format.print(p, "%s\n", passStr);
		Format.print(p, "CustStatus:\t%05d\t\t", sumCustsCount);
		if (custsPer < 9.5 || custsPer > 10.5) {
			success = false;
			passStr = "FAILED";
		}
		else 
			passStr = "PASSED";
		Format.print(p, "%5.02f%\t\t10%\t", custsPer);
		Format.print(p, "%s\n", passStr);
		if (success)
			p.println("ECPerf Requirement PASSED\n");
		else
			p.println("ECPerf Requirement FAILED\n");

		/* Compute response time info */
		p.println("RESPONSE TIMES\t\tAVG.\t\tMAX.\t\t90TH%\tREQD. 90TH%\n");
		if (sumNewoCount > 0) {
			avg  = (sumNewoResp/sumNewoCount) / 1000;
			sumtx = 0;
			cnt90 = (int)(sumNewoCount * .90);
			for (i = 0; i < OrdersStats.RESPMAX; i++) {
				sumtx += sumNewoRespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * OrdersStats.RESPUNIT;
			if (resp90 > OrdersStats.NEWOFAST)
				fail90 = true;
			if (avg > (resp90 + 0.1))
				failavg = true;
			Format.print(p, "NewOrder\t\t%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxNewoResp/1000);
			Format.print(p, "%.03f\t\t", resp90);
			p.println(OrdersStats.NEWOFAST);
		}
		else {
			p.println("NewOrder\t\t0.000\t\t0.000\t\t0.000\n");
		}
		if (sumChgoCount > 0) {
			avg  = (sumChgoResp/sumChgoCount) / 1000;
			chgoCycle = avg;
			sumtx = 0;
			cnt90 = (int)(sumChgoCount * .90);
			for (i = 0; i < OrdersStats.RESPMAX; i++) {
				sumtx += sumChgoRespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * OrdersStats.RESPUNIT;
			if (resp90 > OrdersStats.NEWOFAST)
				fail90 = true;
			if (avg > (resp90 + 0.1))
				failavg = true;
			Format.print(p, "ChgOrder\t\t%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxChgoResp/1000);
			Format.print(p, "%.03f\t\t", resp90);
			p.println(OrdersStats.CHGOFAST);
		}
		else {
			p.println("ChgOrder\t\t0.000\t\t0.000\t\t0.000\n");
		}
		if (sumOrdsCount > 0) {
			avg  = (sumOrdsResp/sumOrdsCount) / 1000;
			ordsCycle = avg;
			sumtx = 0;
			cnt90 = (int)(sumOrdsCount * .90);
			for (i = 0; i < OrdersStats.RESPMAX; i++) {
				sumtx += sumOrdsRespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * OrdersStats.RESPUNIT;
			if (resp90 > OrdersStats.NEWOFAST)
				fail90 = true;
			if (avg > (resp90 + 0.1))
				failavg = true;
			Format.print(p, "OrderStatus\t\t%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxOrdsResp/1000);
			Format.print(p, "%.03f\t\t", resp90);
			p.println(OrdersStats.ORDSFAST);
		}
		else {
			p.println("OrderStatus\t\t0.000\t\t0.000\t\t0.000\n");
		}
		if (sumCustsCount > 0) {
			avg  = (sumCustsResp/sumCustsCount) / 1000;
			custsCycle = avg;
			sumtx = 0;
			cnt90 = (int)(sumCustsCount * .90);
			for (i = 0; i < OrdersStats.RESPMAX; i++) {
				sumtx += sumCustsRespHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * OrdersStats.RESPUNIT;
			if (resp90 > OrdersStats.NEWOFAST)
				fail90 = true;
			if (avg > (resp90 + 0.1))
				failavg = true;
			Format.print(p, "CustStatus\t\t%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)maxCustsResp/1000);
			Format.print(p, "%.03f\t\t", resp90);
			p.println(OrdersStats.CUSTSFAST);
		}
		else {
			p.println("CustStatus\t\t0.000\t\t0.000\t\t0.000\n");
		}
		if (fail90)
			p.println("ECPerf Requirement for 90% Response Time FAILED");
		else
			p.println("ECPerf Requirement for 90% Response Time PASSED");
		if (failavg)
			p.println("ECPerf Requirement for Avg. Response Time FAILED\n\n");
		else
			p.println("ECPerf Requirement for Avg. Response Time PASSED\n\n");
		
		
		p.println("CYCLE TIMES\tTARGETED AVG.\tACTUAL AVG.\tMIN.\tMAX.\n");
		if (sumNewoCount > 0) {
			avg = sumNewoCycle / sumNewoCount;
			newoCycle = avg/1000;
			tavg = sumNewoTargetedCycle / sumNewoCount;
			Format.print(p, "NewOrder\t%6.3f\t\t", tavg/1000);
			Format.print(p, "%6.3f\t\t", avg/1000);
			Format.print(p, "%5.3f\t\t", (double)minNewoCycle/1000);
			Format.print(p, "%6.3f\t\t", (double)maxNewoCycle/1000);
			if (Math.abs(avg - tavg)/tavg <= .05)
				p.println("PASSED");
			else
				p.println("FAILED");
		}
		else
			p.println("NewOrder\t0.000\t0.000");
		if (sumChgoCount > 0) {
			avg = sumChgoCycle / sumChgoCount;
			chgoCycle = avg/1000;
			tavg = sumChgoTargetedCycle / sumChgoCount;
			Format.print(p, "ChgOrder\t%6.3f\t\t", tavg/1000);
			Format.print(p, "%6.3f\t\t", avg/1000);
			Format.print(p, "%5.3f\t\t", (double)minChgoCycle/1000);
			Format.print(p, "%6.3f\t\t", (double)maxChgoCycle/1000);
			if (Math.abs(avg - tavg)/tavg <= .05)
				p.println("PASSED");
			else
				p.println("FAILED");
		}
		else
			p.println("ChgOrder\t0.000\t0.000");
		if (sumOrdsCount > 0) {
			avg = sumOrdsCycle / sumOrdsCount;
			ordsCycle = avg/1000;
			tavg = sumOrdsTargetedCycle / sumOrdsCount;
			Format.print(p, "OrderStatus\t%6.3f\t\t", tavg/1000);
			Format.print(p, "%6.3f\t\t", avg/1000);
			Format.print(p, "%5.3f\t\t", (double)minOrdsCycle/1000);
			Format.print(p, "%6.3f\t\t", (double)maxOrdsCycle/1000);
			if (Math.abs(avg - tavg)/tavg <= .05)
				p.println("PASSED");
			else
				p.println("FAILED");
		}
		else
			p.println("OrderStatus\t0.000\t0.000");
		if (sumCustsCount > 0) {
			avg = sumCustsCycle / sumCustsCount;
			custsCycle = avg/1000;
			tavg = sumCustsTargetedCycle / sumCustsCount;
			Format.print(p, "CustStatus\t%6.3f\t\t", tavg/1000);
			Format.print(p, "%6.3f\t\t", avg/1000);
			Format.print(p, "%5.3f\t\t", (double)minCustsCycle/1000);
			Format.print(p, "%6.3f\t\t", (double)maxCustsCycle/1000);
			if (Math.abs(avg - tavg)/tavg <= .05)
				p.println("PASSED");
			else
				p.println("FAILED");
		}
		else
			p.println("CustStatus\t0.000\t0.000");

		p.println("\nMISC. STATISTICS\n");
		if (sumNewoCount > 0) {
			avg = (double)(sumNewoOlCnt) / sumNewoCount;
			double widgets = avg * txPerMin * newoPer/100;
			Format.print(p, "Average items per order\t\t\t%5.3f\n", avg);
			Format.print(p, "Widget Ordering Rate\t\t\t%5.3f/min", widgets);
			if (widgets < 13.54 * 60 * txRate || widgets > 14.96 * 60 * txRate)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
			double percentLrgOrders = (double)(sumNewoLrgCnt) * 100.0 / sumNewoCount;
			Format.print(p, "Percent orders that are Large Orders\t%3.2f", percentLrgOrders);
			if (percentLrgOrders < 9.5 || percentLrgOrders > 10.5)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
			avg = (double)(sumNewoLrgOlCnt) / sumNewoLrgCnt;
			widgets = avg * txPerMin * newoPer * percentLrgOrders/(100 * 100);
			Format.print(p, "Average items per Large order\t\t%5.3f", avg);
			if (avg < 142.5 || avg > 157.5)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
			Format.print(p, "Largeorder Widget Ordering Rate\t\t%5.3f/min", widgets);
			if (widgets < 7.13 * 60 * txRate || widgets > 7.88 * 60 * txRate)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
			avg = (double)(sumNewoOlCnt - sumNewoLrgOlCnt) / (sumNewoCount - sumNewoLrgCnt);
			Format.print(p, "Average items per Regular order\t\t%5.3f", avg);
			if (avg < 14.25 || avg > 15.75)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
			avg = avg * (100 - percentLrgOrders)/100;	// regular orders
			widgets = avg * txPerMin * newoPer /100;
			Format.print(p, "Regular Widget Ordering Rate\t\t%5.3f/min", widgets);
			if (widgets < 6.41 * 60 * txRate || widgets > 7.09 * 60 * txRate)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
			avg = (double)(sumNewoBuyCart) * 100.0 / sumNewoCount;
			Format.print(p, "Percent orders submitted from Cart\t%3.2f", avg);
			if (avg < 47.5 || avg > 52.5)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
			avg = (double)sumNewoBadCredit * 100.0 / sumNewoCount;
                        /**
                         * Removing this check for 1.0 Update 1 as the NURand distribution
                         * being used for choosing customers for neworders doesn't result
                         * in a mean of 10% of customers with bad credit
			Format.print(p, "Percent orders that failed Credit Check\t%3.2f", avg);
			if (avg < 9.0 || avg > 11.0)
				p.println("\tFAILED");
			else
				p.println("\tPASSED");
                         */
		}
                if(sumChgoCount > 0) {
                    avg = (double)(sumCancelOrdCnt) * 100.0 / sumChgoCount;
                    Format.print(p, "Percent ChgOrders that were delete\t%3.2f", avg);
                             /*** Changing error margin to 10% so that this
                                  criteria has a better chance of being met
					if (avg < 9.5 || avg > 10.5)
                             ***/
					if (avg < 9.0 || avg > 11.0)
						p.println("\tFAILED");
					else
						p.println("\tPASSED");
                }

		p.println("\n\nLITTLE'S LAW VERIFICATION\n\n");
		p.println("Number of users = " + users);

		/* avg.rt = cycle time = tx. rt + cycle time */
		Format.print(p, "Sum of Avg. RT * TPS for all Tx. Types = %f\n",
		((sumNewoCycle + sumChgoCycle + sumOrdsCycle + sumCustsCycle) /
				  stdyState));
		return(txPerMin);
	}


	/**
	 * This method prints the detailed report. This data is used to generate
	 * graphs of throughput, response times and cycle times
	 */
	private void printDetail(PrintStream p) {
		int i, j;
		double f;

		p.println("                   ECPERF Detailed Report\n");
		p.println("NewOrder Throughput");
		p.println("TIME COUNT OF TX.");
		for (i = 0, j = 0; i < OrdersStats.THRUMAX; i++, j+= OrdersStats.THRUUNIT) {
                        if(sumNewoThruHist[i] == 0)
                                break;
			p.println(j + "\t" +  sumNewoThruHist[i]);
                }
		p.println("ChgOrder Throughput");
		p.println("TIME COUNT OF TX.");
		for (i = 0, j = 0; i < OrdersStats.THRUMAX; i++, j+= OrdersStats.THRUUNIT) {
                        if(sumChgoThruHist[i] == 0)
                                break;
			p.println(j + "\t" +  sumChgoThruHist[i]);
                }
		p.println("OrderStatus Throughput");
		p.println("TIME COUNT OF TX.");
		for (i = 0, j = 0; i < OrdersStats.THRUMAX; i++, j+= OrdersStats.THRUUNIT) {
                        if(sumOrdsThruHist[i] == 0)
                                break;
			p.println(j + "\t" +  sumOrdsThruHist[i]);
                }
		p.println("CustStatus Throughput");
		p.println("TIME COUNT OF TX.");
		for (i = 0, j = 0; i < OrdersStats.THRUMAX; i++, j+= OrdersStats.THRUUNIT) {
                        if(sumCustsThruHist[i] == 0)
                                break;
			p.println(j + "\t" +  sumCustsThruHist[i]);
                }

		p.println("\n\nFrequency Distribution of Response Times");
		p.println("\nNEWORDER");
		for (i = 0, f = 0; i < OrdersStats.RESPMAX; i++, f+= OrdersStats.RESPUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumNewoRespHist[i]);
		}
		p.println("\nCHGORDER");
		for (i = 0, f = 0; i < OrdersStats.RESPMAX; i++, f+= OrdersStats.RESPUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumChgoRespHist[i]);
		}
		p.println("\nORDERSTATUS");
		for (i = 0, f = 0; i < OrdersStats.RESPMAX; i++, f+= OrdersStats.RESPUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumOrdsRespHist[i]);
		}
		p.println("\nCUSTSTATUS");
		for (i = 0, f = 0; i < OrdersStats.RESPMAX; i++, f+= OrdersStats.RESPUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumCustsRespHist[i]);
		}
		p.println("\n\nFrequency Distribution of Cycle Times");
		p.println("\nNEWORDER");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumNewoCycleHist[i]);
		}
		p.println("\nCHGORDER");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumChgoCycleHist[i]);
		}
		p.println("\nORDERSTATUS");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumOrdsCycleHist[i]);
		}
		p.println("\nCUSTSTATUS");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumCustsCycleHist[i]);
		}
		p.println("\n\nFrequency Distribution of Targeted Cycle Times");
		p.println("\nNEWORDER");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumNewoTargetedCycleHist[i]);
		}
		p.println("\nCHGORDER");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumChgoTargetedCycleHist[i]);
		}
		p.println("\nORDERSTATUS");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumOrdsTargetedCycleHist[i]);
		}
		p.println("\nCUSTSTATUS");
		for (i = 0, f = 0; i < OrdersStats.CYCLEMAX; i++, f+= OrdersStats.CYCLEUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(sumCustsTargetedCycleHist[i]);
		}
		p.close();
	}


	// This method is not used. It can be used if each agent's output
	// is a logfile 
	private void processFile(BufferedReader bufp) throws IOException {
		String s;
		StringTokenizer st;
		int m, i, j;
		
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		users += Integer.parseInt(st.nextToken());
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		stdyState = Integer.parseInt(st.nextToken());
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumNewoCount += Integer.parseInt(st.nextToken());
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumNewoResp += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m > maxNewoResp)
			maxNewoResp = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumNewoCycle += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m > maxNewoCycle)
			maxNewoCycle = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m < minNewoCycle)
			minNewoCycle = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumNewoOlCnt += Integer.parseInt(st.nextToken());
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumNewoLrgOlCnt += Integer.parseInt(st.nextToken());

		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumChgoCount += Integer.parseInt(st.nextToken());
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumChgoResp += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m > maxChgoResp)
			maxChgoResp = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumChgoCycle += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if (m > maxChgoCycle)
			maxChgoCycle = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m < minChgoCycle)
		minChgoCycle = m;

		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumOrdsCount += Integer.parseInt(st.nextToken());
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumOrdsResp += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m > maxOrdsResp)
			maxOrdsResp = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumOrdsCycle += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if (m > maxOrdsCycle)
			maxOrdsCycle = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m < minOrdsCycle)
		minOrdsCycle = m;

		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumCustsCount += Integer.parseInt(st.nextToken());
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumCustsResp += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if ( m > maxCustsResp)
			maxCustsResp = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		sumCustsCycle += (Double.valueOf(st.nextToken())).doubleValue();
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if (m > maxCustsCycle)
			maxCustsCycle = m;
		st = new StringTokenizer(bufp.readLine(), "=");
		s = st.nextToken();		// skip over description
		m = Integer.parseInt(st.nextToken());
		if (m < minCustsCycle)
			minCustsCycle = m;

		/* Now get the histogram data */
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumNewoRespHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumNewoThruHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumNewoCycleHist[j] += Integer.parseInt(st.nextToken());

		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumChgoRespHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumChgoThruHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumChgoCycleHist[j] += Integer.parseInt(st.nextToken());

		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumOrdsRespHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumOrdsThruHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumOrdsCycleHist[j] += Integer.parseInt(st.nextToken());

		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.RESPMAX; j++)
			sumCustsRespHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.THRUMAX; j++)
			sumCustsThruHist[j] += Integer.parseInt(st.nextToken());
		s = bufp.readLine();	// skip over histogram desc.
		st = new StringTokenizer(bufp.readLine(), " ");
		for (j = 0; j < OrdersStats.CYCLEMAX; j++)
			sumCustsCycleHist[j] += Integer.parseInt(st.nextToken());
	}

}
