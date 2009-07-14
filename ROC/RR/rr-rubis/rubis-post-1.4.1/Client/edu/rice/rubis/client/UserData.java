/* $Id: UserData.java,v 1.11 2004/05/12 02:13:39 candea Exp $ */

package edu.rice.rubis.client;

import java.util.*;
import java.io.*;

public class UserData
{
   private UserAction userAction;      // reusable user action object
   private int        bucketWidth=0;   // width of a bucket in msec
   private long       userStartTime=0; // time (in msec) when user started 

   /* historical data that persists across user actions */
   private int goodResponses[]; // goodResponses[i] counts # of good resp's in the i-th bucketWidth time interval
   private int badResponses[];  // badResponses[i] counts # of bad resp's in the i-th bucketWidth time interval

   private int goodRequests   = 0;
   private int failedRequests = 0;
   private int goodActions    = 0;
   private int failedActions  = 0;

   private int numBuckets;      // number of buckets

   private static boolean printBoundsWarning=true;

   /*---------------------------------------------------------------------------*/

   public UserData( long userStartTime, int totalDuration, int bucketWidth )
      {
         this.userAction = new UserAction( 500 );
         this.bucketWidth = bucketWidth;
         this.userStartTime = userStartTime;

         this.numBuckets = (int) (totalDuration/bucketWidth + 500);
         this.goodResponses = new int[numBuckets];
         this.badResponses  = new int[numBuckets];
         for (int i=0 ; i < numBuckets ; i++)
         {
            this.goodResponses[i] = 0;
            this.badResponses[i]  = 0;
         }
      }

   public void commitUserAction()
      {
         // committed user action data goes into goodResponses[]
	 goodRequests += userAction.distributeData( goodResponses, userStartTime, bucketWidth );
	 goodActions++;
         userAction.startNewUserAction();
      }

   public void abortUserAction()
      {
	 // aborted user action data goes into badResponses[]
	 failedRequests += userAction.distributeData( badResponses, userStartTime, bucketWidth );
	 failedActions++;
         userAction.startNewUserAction();
      }

   // Record an operation in the user action; this op will count as
   // successful or failed depending on whether the user action
   // completes successfully.  Calling recordOp() also restarts the
   // timing, because we're assuming this op is being followed by
   // another one.
   public void recordOp()
      {
         userAction.addOp();
      }

   /**
    * Record an operation that has failed, but is being retried.  This
    * op is not supposed to fail the user action.  Unlike recordOp(),
    * this call does not restart the timing and does not get recorded
    * in the current user action -- it just counts toward the count of
    * failed ops.  Consequently, an operation's duration includes the
    * time it takes to retry it.
    *
    */
   public void recordFailedAttempt()
      {
	  if ( !printBoundsWarning ) // ignore
	      return;

	  long now = System.currentTimeMillis();
	  int idx = (int) ( (now - userStartTime) / bucketWidth );

	  try {
	      badResponses[idx]++;
	      failedRequests++;
	  }
	  catch( ArrayIndexOutOfBoundsException e ) {
	      if ( printBoundsWarning )
	      {
		  System.err.println( "WARNING: idx=" + idx + " is not a valid index" );
		  System.err.println( "         ignoring future distributeData and recordFailedAttempt calls" );
		  printBoundsWarning = false;
	      }
	  }
      }

   /**
    * Merge the contents of another UserData into this one.
    *
    * @param other The UserData object to get the data from
    */
   public void mergeUserData ( UserData other )
      {
         if ( this.numBuckets    != other.numBuckets   ||
              this.bucketWidth   != other.bucketWidth  ||
              this.userStartTime != other.userStartTime )
	    throw new RuntimeException("UserData:mergeUserData -- incompatible UserData profiles");

         for (int i=0 ; i < numBuckets ; i++)
         {
	    this.goodResponses[i] += other.goodResponses[i];
	    this.badResponses[i]  += other.badResponses[i];
         }

	 this.goodActions    += other.goodActions;
	 this.failedActions  += other.failedActions;
	 this.goodRequests   += other.goodRequests;
	 this.failedRequests += other.failedRequests;
      }

   public void dumpBuckets ( String reportDir ) 
       throws IOException
      {
	  FileWriter output = new FileWriter( reportDir + "user_data.tab" );
	  output.write("# Index\tStart\tEnd \tGood\tBad \n");
	  output.write("#      \t(ms) \t(ms)\tReqs\tReqs\n#\n#\n");

	  int limit;
	  for (limit = numBuckets-1; 
	       limit>=0 && goodResponses[limit]==0 && badResponses[limit]==0 ; 
	       limit--);

	  for (int i=0 ; i <= limit ; i++)
	  {
	      String line = i + "\t";
	      long bucketStartTime = i*bucketWidth;
	      line += bucketStartTime + ".0\t";
	      line += (bucketStartTime + bucketWidth) + ".0\t";
	      line += goodResponses[i] + "\t";
	      line += badResponses[i]  + "\t";
	      output.write(line+"\n");
	      bucketStartTime++;
	  }
	  output.close();
      }

   /**
    * Generate the Gnuplot script for plotting the requests data.
    *
    * @param reportDir  Directory where to place the Gnuplot script
    * @param numClients Number of concurrent clients in this expt
    *
    */
   private void genPlotScript ( String reportDir, int numClients )
      {
         try {
	    String plot = "";
	    FileWriter gp = new FileWriter( reportDir + "plot.gp" );
	    
	    gp.write("set title '" + numClients + " clients, " +
		     "bucket="  + bucketWidth/1000  + " sec, " +
		     "requests: " + this.goodRequests + " OK / " + this.failedRequests + " bad, " +
		     "actions: "  + this.goodActions  + " OK / " + this.failedActions  + " bad' \n");
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


   /*---------------------------------------------------------------------------*/

   class UserAction
   {
      private long opEndTime[]; // opEndTime[i] is the time (in msec) at which the i-th operation completed
      private int  nextOp;      // index into opEndTime indicating where the next op should be put in
      private int  maxNumOps;   // max. # of operations allowed in this user action

      public UserAction( int maxOps )
         {
            nextOp = 0;
            maxNumOps = maxOps;
            opEndTime = new long[maxNumOps];
         }

      public void startNewUserAction()
         {
            nextOp = 0;
         }

      public void addOp()
         {
            opEndTime[nextOp] = System.currentTimeMillis();
            nextOp++;

            if (nextOp >= maxNumOps)
               throw (new RuntimeException("UserAction:addOp -- exceeded max # of reqs allowed in user action"));
         }

      /**
       * Distribute data for one user action into buckets.
       *
       * @param  buckets     array of destination buckets
       * @param  t0          time (in msec) of buckets[0]
       * @param  bucketWidth time(buckets[i+1]) - time(buckets[i]) in msec
       * @return number of ops distributed from this user action
       *
       */
      public int distributeData( int buckets[],
				 long t0,      
				 int bucketWidth )
         {
	    if ( !printBoundsWarning )  // ignore from now on
		return 0;

	    int totalOps=0;
	    int idx=0;
	    try {
		for (int i=0 ; i < nextOp ; i++)
		{
		    idx = (int) ((opEndTime[i] - t0) / bucketWidth);
		    buckets[idx]++;
		    totalOps++;
		}
	    }
	    catch( ArrayIndexOutOfBoundsException e ) {
		if ( printBoundsWarning )
		{
		    System.err.println( "WARNING: idx=" + idx + " is not a valid index" );
		    System.err.println( "         ignoring future distributeData and recordFailedAttempt calls" );
		    printBoundsWarning = false;
		}
	    }

	    return totalOps;
         }
   
   }
}
