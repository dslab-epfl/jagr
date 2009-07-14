package roc.loadgen.rubis;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

public class GAWStats 
{
    static Logger log = Logger.getLogger( "GAWStats" );

    // we really only need one for the whole system, and we do want it to be
    // static across all GAWStats to make it easier to merge GAWStats...
    private static long startTime = System.currentTimeMillis();

    private int        bucketWidth;   // width of a bucket in msec


    /* temporary data about the requests in the currently-executing user action.  This data gets
     * merged into historical data once we know whether the action as a whole was successful or not.  
     */
    private ArrayList currentOps;

    /* historical data that persists across user actions */
    private ArrayList goodResponses; // goodResponses[i] counts # of good resp's in the i-th bucketWidth time interval
    private ArrayList badResponses;  // badResponses[i] counts # of bad resp's in the i-th bucketWidth time interval
    
    private int goodRequests   = 0;
    private int badRequests = 0;
    private int goodActions    = 0;
    private int badActions  = 0;
    
    public GAWStats( int bucketWidth ) {
        this.bucketWidth = bucketWidth;
	currentOps = new ArrayList();
	goodResponses = new ArrayList();
	badResponses = new ArrayList();
    }

    public void commitUserAction() {
        goodRequests += commitCurrentOps( goodResponses );
        goodActions++;
    }

    public void abortUserAction() {
        badRequests += commitCurrentOps( badResponses );
        badActions++;
    }

    public void recordOperation() {
        currentOps.add( new Long( System.currentTimeMillis() ));
    }


    public void mergeStats( GAWStats other ) {
        if( other.bucketWidth != this.bucketWidth ) {
            throw new RuntimeException( "GAWStats::mergeStats -- incompatible bucketWidth!" );
        }

        mergeStatsHelper( goodResponses, other.goodResponses );
        mergeStatsHelper( badResponses, other.badResponses );
        
        this.goodRequests += other.goodRequests;
        this.badRequests += other.badRequests;
        this.goodActions += other.goodActions;
        this.badActions += other.badActions;
    }

    private void mergeStatsHelper( ArrayList dest, ArrayList src ) {
        while( dest.size() < src.size() ) {
            dest.add( new Integer(0));
        }

        for( int i=0; i < src.size(); i++ ) {
            int d = ((Integer)dest.get(i)).intValue();
            int s = ((Integer)src.get(i)).intValue();
            dest.set( i, new Integer( d+s ));
        }

    }

    private int commitCurrentOps( ArrayList destBuckets ) {

        Iterator iter = currentOps.iterator();

        while( iter.hasNext() ) {
            // figure out which bucket to increment
            long endtime = ((Long)iter.next()).longValue();
            int idx = (int) ((endtime - startTime) / bucketWidth );
            int c = 0;

            // get the existing value, if the bucket exists
            if( idx < destBuckets.size() &&
                destBuckets.get(idx) != null ) {
                c = ((Integer)destBuckets.get(idx)).intValue();
            }
            
            // increment the bucket
            c++;

            // ensure there's enough buckets to hold this count
            while( idx >= destBuckets.size() ) {
                destBuckets.add( new Integer(0));
            }

            destBuckets.set( idx, new Integer(c));
        }

        int ret = currentOps.size();
        currentOps.clear();

        return ret;
    }
    


   public void dumpBuckets( String reportDir ) 
       throws IOException
      {
	  FileWriter output = new FileWriter( reportDir + "user_data.tab" );

	  output.write("# First bucket starts " + (new Date( startTime )) + " (i.e., " + startTime + " msec)\n#\n" );
	  output.write("# Index\tStart\tEnd \tGood\tBad \n");
	  output.write("#      \t(ms) \t(ms)\tReqs\tReqs\n#\n#\n");

	  for (int i=0 ; i < goodResponses.size() || i<badResponses.size() ; i++)
	  {
	      String line = i + "\t";
	      long bucketStartTime = i*bucketWidth;
	      line += bucketStartTime + ".0\t";
	      line += (bucketStartTime + bucketWidth) + ".0\t";
	      line += ((i<goodResponses.size())?((Integer)goodResponses.get(i)).toString():"0") + "\t";
	      line += ((i<badResponses.size())?((Integer)badResponses.get(i)).toString():"0")  + "\t";
	      output.write(line+"\n");
	      bucketStartTime++;
	  }
	  output.flush();
	  output.close();
      }

   /**
    * Generate the Gnuplot script for plotting the requests data.
    *
    * @param reportDir  Directory where to place the Gnuplot script
    * @param numClients Number of concurrent clients in this expt
    *
    */
    void genPlotScript ( String reportDir, int numClients )
      {
         try {
	    String plot = "";
	    FileWriter gp = new FileWriter( reportDir + "plot.gp" );
	    
	    gp.write("set title '" + numClients + " clients, " +
		     "bucket="  + bucketWidth/1000  + " sec, " +
		     "requests: " + this.goodRequests + " OK / " + this.badRequests + " bad, " +
		     "actions: "  + this.goodActions  + " OK / " + this.badActions  + " bad' \n");
	    gp.write("set xlabel 'Time [seconds]'        \n");
	    gp.write("set ylabel 'Gaw (responses/second)' \n");

	    gp.write("#\n# First we plot the raw requests into a PNG file\n#\n");
	    gp.write("set term png small color\n");
	    gp.write("set output '" + reportDir + "request_profile_raw.png'   \n");

	    plot += "plot '" + reportDir + "user_data.tab' ";
	    plot +=          "using ($2/1000.0):($4*1000/($3-$2)) ";
	    plot +=          "title 'Correctly satisfied requests' with linespoints lt 21 pt 3, "; // histeps 3,";
	    plot +=      "'" + reportDir + "user_data.tab' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$5*1000/($3-$2)) ";
	    plot +=          "title 'Failed requests' with linespoints lt 1 pt 2\n"; // histeps 1\n";
	    gp.write( plot );

	    gp.write("#\n# Next we plot the raw requests into a TGIF file (scale down by 0.45)\n#\n");
	    gp.write("set term tgif\n");
	    gp.write("set output '" + reportDir + "request_profile_raw.obj'\n");
	    plot = "";
	    plot += "plot '" + reportDir + "user_data.tab' ";
	    plot +=          "using ($2/1000.0):($4*1000/($3-$2)) ";
	    plot +=          "title 'Correctly satisfied requests' with linespoints lt -1 pt 13,"; // histeps linetype -1,";
	    plot +=      "'" + reportDir + "user_data.tab' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$5*1000/($3-$2)) ";
	    plot +=          "title 'Failed requests' with linespoints lt 1 pt 1\n"; // histeps linetype 1\n";
	    gp.write( plot );
/*
	    gp.write("set title 'Session Profile (" + numClients + " clients, bucket = " + bucketWidth/1000 + " sec)' \n");
	    gp.write("set ylabel 'Sessions/second' \n");
	    gp.write("#\n# Plot the session into a PNG file\n#\n");
	    gp.write("set term png small color\n");
	    gp.write("set output '" + reportDir + "request_profile_sessions.png'\n");
	    plot = "";
	    plot += "plot '" + reportDir + "user_data.tab' ";
	    plot +=          "using ($2/1000.0):($6*1000/($3-$2)) ";
	    plot +=          "title 'Successfully completed sessions' with histeps 3,";
	    plot +=      "'" + reportDir + "user_data.tab' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$7*1000/($3-$2)) ";
	    plot +=          "title 'Aborted sessions' with histeps 1\n";
	    gp.write( plot );

	    gp.write("#\n# Plot the session into a TGIF file (scale down by 0.45)\n#\n");
	    gp.write("set term tgif\n");
	    gp.write("set output '" + reportDir + "request_profile_sessions.obj'\n");
	    plot = "";
	    plot += "plot '" + reportDir + "user_data.tab' ";
	    plot +=          "using ($2/1000.0):($6*1000/($3-$2)) ";
	    plot +=          "title 'Successfully completed sessions' with histeps linetype 1,";
	    plot +=      "'" + reportDir + "user_data.tab' ";
	    plot +=          "using (.1+$2/1000.0):(.1+$7*1000/($3-$2)) ";
	    plot +=          "title 'Aborted sessions' with histeps linetype 9\n";
	    gp.write( plot );
*/
	    gp.close();
         }
         catch (Exception e) { 
	    e.printStackTrace(); 
         }
      }

   /**
    * Run gnuplot to graph the data.
    *
    * @param reportDir  Directory where the Gnuplot script resides
    * @param numClients Number of concurrent clients in this expt
    *
    */
   public void plotData ( String reportDir, int numClients )
       throws IOException
      {
         dumpBuckets( reportDir ); // dump current buckets to the data file
         genPlotScript( reportDir, numClients );

         try {
	    Process p = Runtime.getRuntime().exec("/usr/bin/gnuplot " + reportDir + "plot.gp >& plot.log");
         }
         catch (Exception e) { 
	    e.printStackTrace(); 
         }
      }

    

}
