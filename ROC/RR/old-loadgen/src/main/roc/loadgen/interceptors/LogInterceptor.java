package roc.loadgen.interceptors;

import roc.loadgen.AbortRequestException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

import org.apache.log4j.Logger;

public class LogInterceptor extends RequestInterceptor {

    private static Logger log = Logger.getLogger( "interceptors.LogInterceptor" );

    Arg[] argDefinitions = {
    };

    public LogInterceptor() {
    }

    /**
     * @see roc.loadgen.RequestInterceptor#getArguments()
     */
    public Arg[] getArguments() {
        return argDefinitions;
    }

    public Response invoke(Request req)
        throws AbortRequestException {

        log.debug("MAKING REQUEST: " + req.toString());

        Response resp = invokeNext(req);

	if( !resp.isOK() )
	    log.info( " NEGATIVE RESPONSE " + resp + "; REQUEST was " + req.toString() );

        log.debug("REQUEST RESPONSE: " + resp.toString() + "; REQUEST was " + req.toString() );

        return resp;
    }

}
