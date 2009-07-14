package roc.loadgen.interceptors;

import roc.loadgen.AbortRequestException;
import roc.loadgen.InitializationException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

import org.apache.log4j.Logger;

public class PerformanceLogInterceptor extends RequestInterceptor {

    private static Logger log = Logger.getLogger( "interceptors.PerformanceLogInterceptor" );

    Arg[] argDefinitions = { };

    static long startTime = -1;
    static long countRequests = 0;
    static long latency = 0;

    public PerformanceLogInterceptor() {
    }

    public Arg[] getArguments( ){
	return argDefinitions;
    }
    
    public Response invoke(Request req )
	throws AbortRequestException {

	long currStart = System.currentTimeMillis();

	if( startTime == -1 )
	    startTime = currStart;

	Response resp = invokeNext(req);

	long currEnd = System.currentTimeMillis();

	latency += currEnd - currStart;
	countRequests++;
	
	if( countRequests % 50 == 0 )
	    printStatistics(currEnd);

	return resp;
    }

    public void printStatistics( long now ) {
	log.info( "(total,Avg. Lat,TP/sec) = (" +
		  (countRequests) + "," +
		  (latency / countRequests) + "," + 
		  ((double)((double)1000 * countRequests / ((double)now - startTime))) + ")" );
    }


}
