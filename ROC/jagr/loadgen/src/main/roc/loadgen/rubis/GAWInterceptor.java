package roc.loadgen.rubis;

import java.util.*;
import java.io.IOException;

import roc.loadgen.AbortRequestException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class GAWInterceptor extends RequestInterceptor {

    public static final String BUCKET_WIDTH = "bucketwidth";
    public static final String REPORT_DIR = "reportdir";

    private static Logger log = Logger.getLogger( "rubis.GAWInterceptor" );

    private GAWStats myStats;

    private static List allStats = Collections.synchronizedList( new ArrayList() );
    private static int countClients = 0;
    private static boolean dumpedStats = false;


    Arg[] argDefinitions = {
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

    public GAWInterceptor() {
    }

    public void start() {
	synchronized( roc.loadgen.rubis.GAWInterceptor.class ) {
	    countClients++;
	}
        int bucketWidth = ((Integer)args.get( BUCKET_WIDTH )).intValue();
        myStats = new GAWStats( bucketWidth );
        allStats.add( myStats );
    }


    public Arg[] getArguments() {
        return argDefinitions;
    }

    public Response invoke( Request req )
        throws AbortRequestException {
        RubisUserRequest rubisReq = (RubisUserRequest)req;

        Response resp = invokeNext(rubisReq);

        myStats.recordOperation();

	if( !resp.isOK()) {
	    myStats.abortUserAction();
	}
        else if( rubisReq.isLastInAction() ) {
	    myStats.commitUserAction();
	}
        

        return resp;
    }


    public void stop() {
        synchronized( GAWInterceptor.class ) {
            if( dumpedStats ) {
                return;
            }
            dumpedStats = true;

            GAWStats finalStats = new GAWStats( ((Integer)args.get(BUCKET_WIDTH )).intValue() );
            
            Iterator iter = allStats.iterator();
            while( iter.hasNext() ) {
                finalStats.mergeStats( (GAWStats)iter.next() );
            }

            try {
                finalStats.plotData( (String)args.get( REPORT_DIR ), countClients );
            }
            catch( IOException ex ) {
                ex.printStackTrace();
                log.log( Level.ERROR, "could not write out GAW Report", ex );
            }
        }
    }




}
