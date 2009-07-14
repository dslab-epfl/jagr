package roc.loadgen.interceptors;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

public class PerformanceLogInterceptor extends RequestInterceptor {

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
	throws AbortRequestException, AbortSessionException {

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
	engine.logStats( "PerformanceLogInterceptor: (total,Avg. Lat,TP/sec) = (" +
			 (countRequests) + "," +
			 (latency / countRequests) + "," + 
			 ((double)((double)1000 * countRequests / ((double)now - startTime))) + ")" );
    }


}
