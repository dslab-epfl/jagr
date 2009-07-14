/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: MfgReport.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.driver;
import java.io.*;
import java.util.*;

/**
 * This class is the Report generator for the Mfg application. 
 * The genReport method is called with the aggregated MfgStat result 
 * and it generates the output reports - a summary report and a detail report.
 * The summary report contains a summary of the numerical quantities -
 * transaction counts, rates, response and think times.
 * The detail report contains the histogram data to draw the various graphs -
 * throughput, response times and think times.
 *
 * @author Shanti Subramanyam
 * @see driver.ECperfReport
 * @see driver.OrdersReport
 */
public class MfgReport {
	String prefix;
	String summary, detail;
	int stdyState;

	int workOrderCnt = 0;			/* number of workorders scheduled */
	int widgetCnt = 0;			/* number of widgets produced */
	int largeOrderCnt = 0;			/* no. of workorders scheduled due to largeorders */
	int largeOrderWidgetCnt = 0;	/* widgets produced by LargeOrderLine */
	int respMax = 0;			/* Max. response time */
	double respTime = 0;		/* Sum of response times */
	int respHist[] = new int [MfgStats.RESPMAX];/* Response time histogram */
	int thruput[] = new int[MfgStats.THRUMAX];	/* Thruput histogram */
	int numPlannedLines;
	String resultsDir;		/* Name of results directory */

	public MfgReport() {
	}


        List dumpStreams;
        int dumpInterval;
        int rampUp;
        int prevTxCnt = 0;
        double avgTps = 0;
	private static int elapsed = 0;
	private static int thruIndex = 0;

	/**
	 * This constructor is used for dumping data for charting
	 */
	public MfgReport(String file, int dumpInterval, int rampUp)
                        throws IOException {
                dumpStreams = Collections.synchronizedList(new ArrayList());
                dumpStreams.add(new DataOutputStream(
                                         new FileOutputStream(file)));
                this.dumpInterval = dumpInterval;
                this.rampUp = rampUp;
	}

	/**
	 * new constructor is used for dumping data for charting
	 */
        public MfgReport(List dumpStreams, int dumpInterval, int rampUp) {
                this.dumpStreams = dumpStreams;
                this.dumpInterval = dumpInterval;
                this.rampUp = rampUp;
        }

	/**
	 * Method : genReport
	 * This method is called from ECperfReport to generate the report
	 * for the MfgApp. 
	 * @param aggs - MfgStats object returned from Planned and LargeOrderlines
	 * @return double - workOrdersPerMin  metric
	 */
	public double genReport(MfgStats[] aggs, int txRate) throws IOException {
		BufferedReader bufp;

		String resultsDir = aggs[0].resultsDir;
		String filesep = System.getProperty("file.separator");
		summary = resultsDir + filesep + "Mfg.summary";
		detail = resultsDir + filesep + "Mfg.detail";
		PrintStream sump = new PrintStream(new FileOutputStream(summary));
		PrintStream detailp = new PrintStream(new FileOutputStream(detail));
		int i = 0;

		for (i = 0; i < aggs.length; i++) {
			processStats(aggs[i]);
		}	
		stdyState = aggs[0].stdyState;

		Debug.println("Printing summary report ...");
		double workOrdersPerMin = printSummary(sump, txRate);
		Debug.println("Summary finished. Now printing detail ...");
		printDetail(detailp);
		return(workOrdersPerMin);
	}


	/*
	 * This method is called by the Driver every time it wants to dump
	 * the thruput data out to a file
	 */
	public void dumpStats(MfgStats[] aggs) {
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
                        System.err.println("Error writing Mfg stats.\n" +
                            "Closing stream and removing stream from list.\n" +
                            "Benchmark continues without interruption.");

                    }
                }

                // Get the aggregate tx
		for (int i = 0; i < aggs.length; i++) {
			txCnt += aggs[i].workOrderCnt;
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

		/* Now dump out the old average tps */
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
                        System.err.println("Error writing Mfg stats.\n" +
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

		/* Now dump out the average tps */
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


	private void processStats(MfgStats m) {
		int i, j;
		
		numPlannedLines += m.numPlannedLines;
		workOrderCnt += m.workOrderCnt;
		widgetCnt += m.widgetCnt;
		largeOrderCnt += m.largeOrderCnt;
		largeOrderWidgetCnt += m.largeOrderWidgetCnt;
		if (m.respMax > respMax)
			respMax = m.respMax;
		respTime += m.respTime;
		for (i = 0; i < MfgStats.RESPMAX; i++) {
			respHist[i] += m.respHist[i];
		}
		for (i = 0; i < MfgStats.THRUMAX; i++) {
			thruput[i] += m.thruput[i];
		}
	}


	// Print summary report
	private double printSummary(PrintStream p, int txRate) {
		double txcnt, widgetsPerMin, workOrdersPerMin, largeOrdersPerMin;
		boolean success = true;
		double avg, resp90, cycle;
		int sumtx, cnt90;
		boolean fail90 = false, failavg =false;
		int i;
                p.println();
                p.println("\t\t\tMfg Summary Report");
                p.println("\t\t\tVersion : " + ECperfReport.version);
                p.println();

		p.print("Total Number of WorkOrders Processed : "); 
		Format.print(p, "%d", workOrderCnt);
		p.println();
		p.print("Number of WorkOrders as a result of LargeOrders : "); 
		Format.print(p, "%d", largeOrderCnt);
		p.println();
		p.print("Total WorkOrders Production Rate : "); 
		workOrdersPerMin = (double)workOrderCnt * 1000 * 60 / stdyState;
		Format.print(p, "%.02f WorkOrders/min", workOrdersPerMin);
		p.println();
		p.print("LargeOrders Production Rate : "); 
		largeOrdersPerMin = (double)largeOrderCnt * 1000 * 60 / stdyState;
		Format.print(p, "%.02f LargeOrders/min", largeOrdersPerMin);
		p.println();
		p.println();

		txcnt = widgetCnt + largeOrderWidgetCnt;
		widgetsPerMin = (double)txcnt * 1000 * 60 / stdyState;
		p.print("Total Widget Manufacturing Rate : "); 
		Debug.println("widgets/min = " + widgetsPerMin);
		Format.print(p, "%.02f widgets/min", widgetsPerMin);
		p.println();
		p.print("LargeOrderLine Widget Rate : "); 
                double loRate = (double)largeOrderWidgetCnt * 1000 * 60 / stdyState;
		Format.print(p, "%.02f widgets/min", loRate);
                /*
                 * Since 10% of neworders can rollback due to bad credit,
                 * these need to be accounted for as they will not 
                 * generate largeorders. See bugid 4482440

                if((loRate >= 6.75 * 60 * txRate) && (loRate <= 8.25 * 60 * txRate))
                    p.print("\tPASSED");
                else
                    p.print("\tFAILED");
                 */
                if((loRate >= 364.5 * txRate) && (loRate <= 445.5 * txRate))
                    p.print("\tPASSED");
                else
                    p.print("\tFAILED");
		p.println();
		p.print("PlannedLines Widget Rate : "); 
                double plRate = (double)widgetCnt * 1000 * 60 / stdyState;
		Format.print(p, "%.02f widgets/min", plRate);
                if((plRate >= 6.08 * 60 * txRate) && (plRate <= 7.43 * 60 * txRate))
                    p.print("\tPASSED");
                else
                    p.print("\tFAILED");
		p.println();
		p.println();
		/* Compute response time info */
		p.println("RESPONSE TIMES\t\tAVG.\t\tMAX.\t\t90TH%\tREQD. 90TH%\n");
		if (workOrderCnt > 0) {
			avg  = (respTime/workOrderCnt) / 1000;
			cycle = avg;
			sumtx = 0;
			cnt90 = (int)(workOrderCnt * .90);
			for (i = 0; i < MfgStats.RESPMAX; i++) {
				sumtx += respHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * MfgStats.RESPUNIT;
			if (resp90 > MfgStats.RESPFAST)
				fail90 = true;
			if (resp90 <= (avg - 0.1))
				failavg = true;
			Format.print(p, "\t\t\t%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)respMax/1000);
			Format.print(p, "%.03f\t\t", resp90);
			p.println(MfgStats.RESPFAST);
		}
		else {
			p.println("\t\t\t0.000\t\t0.000\t\t0.000\n");
		}
		if (fail90)
			p.println("ECPerf Requirement for 90% Response Time FAILED");
		else
			p.println("ECPerf Requirement for 90% Response Time PASSED");
		if (failavg)
			p.println("ECPerf Requirement for Avg. Response Time FAILED\n\n");
		else
			p.println("ECPerf Requirement for Avg. Response Time PASSED\n\n");
		
		return(workOrdersPerMin);
	}


	/**
	 * This method prints the detailed report. This data is used to generate
	 * graphs of throughput, response times and think times
	 */
	private void printDetail(PrintStream p) {
		int i, j;
		double f;

		p.println("                   ECPERF Detailed Report\n");
		p.println("Manufacturing Throughput");
		p.println("TIME COUNT OF TX.");
		for (i = 0, j = 0; i < MfgStats.THRUMAX; i++, j += MfgStats.THRUUNIT) {
                        if(thruput[i] == 0)
                                break;
			p.println(j + "\t" +  thruput[i]);
                }
		p.println("\n\nFrequency Distribution of Response Times");
		for (i = 0, f = 0; i < MfgStats.RESPMAX; i++, f+= MfgStats.RESPUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(respHist[i]);
		}
		p.close();
	}


}
