/**
 *  RetryInterceptor retry request when response is service unavailable
 *
 *
 */

package roc.loadgen.interceptors;

import java.util.*;
import java.io.*;

import roc.loadgen.*;
import roc.loadgen.rubis.*;
import roc.loadgen.http.HttpResponse;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class RetryInterceptor extends RequestInterceptor 
{
    private static Logger log = Logger.getLogger( "interceptors.RetryInterceptor" );
    private static boolean dumpedStats = false;
    int timeout      = 0;  // user level timeout in milisecond
    int numOfRetries = 0;  // number of retries within timeout
    int retryAfter   = 0;  // after waiting this time period retry again

    public static final String TIMEOUT = "timeout";
    public static final String NUM_OF_RETRY = "number of retries";

    private static Arg[] argDefinitions = {
        new Arg( TIMEOUT,
                 "time out in milisecond",
                 Arg.ARG_INTEGER,
                 true,
                 null ),
        new Arg( NUM_OF_RETRY,
                 "number of retries",
                 Arg.ARG_INTEGER,
                 true,
                 null )
     };

    public Arg[] getArguments() {
        return argDefinitions;
    }

    public RetryInterceptor() {
    }

    public void start() {
	Object objTimeout = args.get( TIMEOUT );
	if ( objTimeout != null ) 
	    timeout = ((Integer)objTimeout).intValue();

	Object objRetries = args.get( NUM_OF_RETRY );
	if ( objRetries != null ) 
	    numOfRetries = ((Integer)objRetries).intValue();

	log.debug( "Timeout: "+timeout+ " , Num_of_Retries: "+numOfRetries );
    }

    public void stop() {}

    public Response invoke( Request req )
        throws AbortRequestException 
    {
	int retry = 0;

	long reqTime = req.getReqTime(); // time when the request is issued
        Response resp = invokeNext(req);
	long respTime = System.currentTimeMillis(); // time when the response arrived
	HttpResponse respHttp = (HttpResponse)resp;

	if ( respHttp.isServiceUnavailableError() ) {
	    log.debug("got Service Unavailable Error (HTTP 503)");

	    while ( respHttp.isServiceUnavailableError() 
		    && ((RubisUserRequest)req).isIdempotent()
		    && retry < numOfRetries 
		    && (respTime - reqTime) < timeout ) {
		
		// extract value of Retry-After
		Map headerMap = respHttp.getHeaders();
		Object obj = headerMap.get("Retry-After");
		if ( obj != null ) {
		    List  list = (List)obj;
		    retryAfter = Integer.parseInt((String)list.get(0))*1000;
		    log.debug("Retry-After: "+retryAfter);
		}

		// sleep before retry
		try {
		    log.debug("sleep for "+retryAfter+" milisecond");
		    Thread.sleep(retryAfter);
		} catch (Exception e){}

		// retry sending request
		retry++;
		log.warn("retry "+retry+" : "+req);
		resp = invokeNext(req);
		respHttp = (HttpResponse)resp;
		respTime = System.currentTimeMillis();
	    }
	}
	
	resp.setRespTime(respTime);
	log.debug("ResponseTime: "+(respTime - reqTime)/1000.0);

        return resp;
    }
}
