package roc.loadgen.rubis;

import java.util.*;
import java.io.*;

import roc.loadgen.*;
import roc.loadgen.http.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class GAWInterceptor extends RequestInterceptor 
{
    private static Logger log = Logger.getLogger( "rubis.GAWInterceptor" );

    // collector class for statistics
    private GAWStats myStats;

    private static List allStats = Collections.synchronizedList( new ArrayList() );
    private static int countClients = 0;

    // true iff we already dumped the statistics to a file
    private static boolean dumpedStats = false;

    public GAWInterceptor() {
    }

    public void start() 
    {
	synchronized( GAWInterceptor.class ) 
	{
	    countClients++;
	}

        int bucketWidth = ((Integer)args.get( BUCKET_WIDTH )).intValue();
        myStats = new GAWStats( bucketWidth );
        allStats.add( myStats );
    }


    public Response invoke( Request req )
        throws AbortRequestException 
    {
        RubisUserRequest rubisReq = (RubisUserRequest) req;

	long tReq = System.currentTimeMillis();//time when the request is issued
	req.setReqTime(tReq); // tReq will be used by RetryInterceptor
        Response resp = invokeNext(rubisReq);

	// If retry interceptor is used, tResp has valid value.
	// If not, then mesure it here.
	long tResp = resp.getRespTime();       //time when the response arrived
	if ( tResp == 0 ) {
	    tResp = System.currentTimeMillis(); 
	}


	//System.out.println("GAW: res: "+tReq+" resp: "+tResp);
        myStats.recordOperation( new Operation( ((RubisUserRequest) req).getState(), tReq, tResp, resp.isOK()) );

	if( !resp.isOK() ) 
	{
	    myStats.abortUserAction();
	}
        else if( rubisReq.isLastInAction() )
	{
	    myStats.commitUserAction();
	}

        return resp;
    }


    public void stop() 
    {
        synchronized( GAWInterceptor.class ) {
            if( dumpedStats ) {
                return;
            }
            dumpedStats = true;

            GAWStats finalStats = new GAWStats( ((Integer)args.get(BUCKET_WIDTH )).intValue() );
            
            Iterator iter = allStats.iterator();
            while( iter.hasNext() ) 
	    {
		GAWStats s = (GAWStats)iter.next();
		s.stop();
                finalStats.mergeStats( s );
            }

            try {
		String reportDir = (String)args.get( REPORT_DIR ) + "/" + currentDateToString() + "/";
		File dir = new File(reportDir);
		dir.mkdirs();
                finalStats.plotData( reportDir, countClients );
            }
            catch( IOException ex ) {
                ex.printStackTrace();
                log.log( Level.ERROR, "could not write out GAW Report", ex );
            }
        }
    }


    private String currentDateToString()
    {
	GregorianCalendar d = new GregorianCalendar();

	String result = d.get(d.YEAR) + "-";

	int x = d.get( d.MONTH ) + 1;
	if( x < 10 )
	    result += "0";
	result += x + "-";

	x = d.get( d.DATE );
	if( x < 10 )
	    result += "0";
	result += x + "-";

	x = d.get( d.HOUR_OF_DAY );
	if( x < 10 )
	    result += "0";
	result += x + ":";

	x = d.get( d.MINUTE );
	if( x < 10 )
	    result += "0";
	result += x + ":";

	x = d.get( d.SECOND );
	if( x < 10 )
	    result += "0";
	result += x;

	return result;
    }

    public static final String BUCKET_WIDTH = "bucketwidth";
    public static final String REPORT_DIR = "reportdir";

    private static Arg[] argDefinitions = {
        new Arg( BUCKET_WIDTH,
                 "bucket width in millis",
                 Arg.ARG_INTEGER,
                 true,
                 null ),
        new Arg( REPORT_DIR,
                 "report directory for GAWStats",
                 Arg.ARG_STRING,
                 true,
                 null )
        // consider adding arguments to set an initial ignore count, and/or a ramp-up and ramp-down section to log separately
    };

    public Arg[] getArguments() {
        return argDefinitions;
    }


}
