/* $Id */

package edu.rice.rubis.client;

import java.util.*;
import java.io.*;

public class ResponseProfile extends TimerTask 
{
    private static int             numBuckets=0;
    private static int             bucketWidth=0; // in msec
    private static int             numClients=0;
    private static int             currentBucket=0;
    private static long            startTime[];
    private static int             lastBucket=0;
    private static RUBiSProperties rubis=null;
    private static String          reportDir;
    private static boolean         plotScriptGenerated=false;

    private int goodResponses[];
    private int badResponses[];
    private int goodSessions[];
    private int badSessions[];

    /* Constructor takes total number of buckets */
    public ResponseProfile( int duration, int bucketWidth, int numClients, RUBiSProperties rubis, String reportDir )
    {
	// Check if static portion already intialized.  This has a
	// race condition if constructors are called in parallel.
	if (this.numBuckets==0)
	{
	    this.numBuckets = duration/bucketWidth + 30;
	    this.bucketWidth = bucketWidth;
	    this.numClients = numClients;
	    startTime = new long[numBuckets+1];
	    startTime[0] = System.currentTimeMillis();
	    startTime[numBuckets] = Long.MAX_VALUE;
	    this.rubis = rubis;
	    this.reportDir = reportDir;
	}

	// What follows is the non-static portion
	goodResponses = new int[numBuckets];
	badResponses  = new int[numBuckets];
	goodSessions  = new int[numBuckets];
	badSessions   = new int[numBuckets];
    }

    /* This runs periodically to update the index to the next bucket */
    public void run() 
    {
	if ( 1+currentBucket < numBuckets )
	{
	    currentBucket++; 
	    startTime[currentBucket] = System.currentTimeMillis();
	    goodResponses[currentBucket] = 0;
	    badResponses[currentBucket]  = 0;
	    goodSessions[currentBucket] = 0;
	    badSessions[currentBucket]  = 0;
	}

	if ( currentBucket-lastBucket > rubis.getDumpMultiple() )
	{
	    lastBucket = currentBucket;
	    plotData();
	}
    }

    /* Record a GOOD sample in the appropriate bucket, along with the
     * time the request was issued and the time the response was
     * received */
    public void recordGoodResponse ( long begin, long end ) 
    {
	goodResponses[currentBucket]++;
    }

    /* Record the completion of a successful session */
    public void recordGoodSession ( long begin, long end ) 
    {
	goodSessions[currentBucket]++;
    }

    /* Record the abortion of an unsuccessful session */
    public void recordBadSession ( long begin, long end ) 
    {
	badSessions[currentBucket]++;
    }

    /* Record a BAD sample in the appropriate bucket, along with the
     * time the request was issued and the time the response was
     * received */
    public void recordBadResponse ( long begin, long end ) 
    {
	badResponses[currentBucket]++;
    }

    /* Record a BAD sample in the appropriate bucket, with no timing
     * information */
    public void recordBadResponse() 
    {
	badResponses[currentBucket]++;
    }

    /* Merge the results of another ResponseProfile into ours.  If you
     * do updates during this merge, results are undefined. */
    public void mergeProfile ( ResponseProfile other )
    {
	if ( this.numBuckets != other.numBuckets ||
	     this.numClients != other.numClients   )
	    throw new RuntimeException("ResponseProfiles have different # of clients/buckets");

	for (int i=0 ; i < numBuckets ; i++)
	{
	    this.goodResponses[i] += other.goodResponses[i];
	    this.badResponses[i]  += other.badResponses[i];
	    this.goodSessions[i]  += other.goodSessions[i];
	    this.badSessions[i]   += other.badSessions[i];
	}
    }

    /* Dump bucket contents to the given file */
    public void dumpBuckets () 
    {
	int i, limit;
	try {
	    FileWriter output = new FileWriter(reportDir + "response_profile.dat");
	    output.write("# Index\tStart\tEnd \tGood\tBad\tGood\tBad\n");
	    output.write("#      \t(ms) \t(ms)\tReqs\tReqs\tSess\tSess\n");
            output.write("#--------------------------------------------------\n#\n");

	    if (currentBucket >= numBuckets) 
		currentBucket = numBuckets-1;

	    startTime[currentBucket+1] = System.currentTimeMillis();

	    for (i=0 ; i <= currentBucket ; i++)
	    {
		String line = i + "\t";
		line += (startTime[i]   - startTime[0]) + ".0\t";
		line += (startTime[i+1] - startTime[0]) + ".0\t";
		line += goodResponses[i] + ".0\t";
		line += badResponses[i]  + ".0\t";
		line += goodSessions[i]  + ".0\t";
		line += badSessions[i]   + ".0\n";
		output.write(line);
	    }

	    output.close();
	} 
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /* Generate the Gnuplot script to plot the data */
    private void genPlotScript ()
    {
	if (plotScriptGenerated)
	    return; // don't do extra work if not necessary

	try {
	    String plot = "";
	    PrintStream out = new PrintStream( new FileOutputStream("/tmp/error.log") );
	    FileWriter  gp  = new FileWriter( reportDir + "myplot.gp" );
	    
	    gp.write("set title 'Raw Response Profile (" + numClients + " clients, bucket size = " + bucketWidth/1000 + " sec)' \n");
	    gp.write("set xlabel 'Time [seconds]'        \n");
	    gp.write("set ylabel 'Responses/second' \n");

	    gp.write("#\n# First we plot the raw requests into a PNG file\n#\n");
	    gp.write("set term png small color\n");
	    gp.write("set output '" + reportDir + "request_profile_raw.png'   \n");

	    plot += "plot '" + reportDir + "response_profile.dat' ";
	    plot +=          "using ($2/1000.0):($4*1000/($3-$2)) ";
	    plot +=          "title 'Correctly satisfied requests' with histeps 3,";
	    plot +=      "'" + reportDir + "response_profile.dat' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$5*1000/($3-$2)) ";
	    plot +=          "title 'Unsatisfied requests' with histeps 1\n";
	    gp.write( plot );

	    gp.write("#\n# Next we plot the raw requests into a TGIF file (scale down by 0.45)\n#\n");
	    gp.write("set term tgif\n");
	    gp.write("set output '" + reportDir + "request_profile_raw.obj'\n");
	    plot = "";
	    plot += "plot '" + reportDir + "response_profile.dat' ";
	    plot +=          "using ($2/1000.0):($4*1000/($3-$2)) ";
	    plot +=          "title 'Correctly satisfied requests' with histeps linetype 1,";
	    plot +=      "'" + reportDir + "response_profile.dat' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$5*1000/($3-$2)) ";
	    plot +=          "title 'Unsatisfied requests' with histeps linetype 9\n";
	    gp.write( plot );

	    gp.write("set title 'Session Profile (" + numClients + " clients, bucket size = " + bucketWidth/1000 + " sec)' \n");
	    gp.write("set ylabel 'Sessions/second' \n");
	    gp.write("#\n# Plot the session into a PNG file\n#\n");
	    gp.write("set term png small color\n");
	    gp.write("set output '" + reportDir + "request_profile_sessions.png'\n");
	    plot = "";
	    plot += "plot '" + reportDir + "response_profile.dat' ";
	    plot +=          "using ($2/1000.0):($6*1000/($3-$2)) ";
	    plot +=          "title 'Successfully completed sessions' with histeps 3,";
	    plot +=      "'" + reportDir + "response_profile.dat' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$7*1000/($3-$2)) ";
	    plot +=          "title 'Aborted sessions' with histeps 1\n";
	    gp.write( plot );

	    gp.write("#\n# Plot the session into a TGIF file (scale down by 0.45)\n#\n");
	    gp.write("set term tgif\n");
	    gp.write("set output '" + reportDir + "request_profile_sessions.obj'\n");
	    plot = "";
	    plot += "plot '" + reportDir + "response_profile.dat' ";
	    plot +=          "using ($2/1000.0):($6*1000/($3-$2)) ";
	    plot +=          "title 'Successfully completed sessions' with histeps linetype 1,";
	    plot +=      "'" + reportDir + "response_profile.dat' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$7*1000/($3-$2)) ";
	    plot +=          "title 'Aborted sessions' with histeps linetype 9\n";
	    gp.write( plot );

	    gp.close();
	    plotScriptGenerated = true;
	}
	catch (Exception e) { 
	    e.printStackTrace(); 
	}
    }

    /* Run gnuplot to graph the data */
    public void plotData ()
    {
	genPlotScript(); // generate the plot script
	dumpBuckets();   // dump current buckets to the data file

	try {
	    Process p = Runtime.getRuntime().exec("/usr/bin/gnuplot " + reportDir + "myplot.gp");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    String s;
            while ((s = stdInput.readLine()) != null) { System.out.println(s); }
            while ((s = stdError.readLine()) != null) { System.err.println(s); }
	}
	catch (Exception e) { 
	    e.printStackTrace(); 
	}
    }
}
