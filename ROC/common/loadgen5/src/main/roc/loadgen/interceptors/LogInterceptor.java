package roc.loadgen.interceptors;

import java.util.Random;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

public class LogInterceptor extends RequestInterceptor {

    public static final String ARG_DEBUG_LEVEL = "debug";

    Random rand;
    boolean debug;
    
    public LogInterceptor() {
	rand = new Random();		
    }
    
    Arg[] argDefinitions = {
	new Arg( ARG_DEBUG_LEVEL,
		 "level of debug printouts to make",
		 Arg.ARG_BOOLEAN,
		 false,
		 "false")
	    };

    /**
     * @see roc.loadgen.RequestInterceptor#getArguments()
     */
    public Arg[] getArguments() {
	return argDefinitions;
    }

    public void start() {
	debug = ((Boolean) args.get(ARG_DEBUG_LEVEL)).booleanValue();
    }
    
    public Response invoke(Request req, Object src)
	throws AbortRequestException, AbortSessionException {
	
	if(debug)
	    engine.logInfo( "MAKING REQUEST: " + req.toString(), src );
	
	Response resp = invokeNext(req, src);
	
	if(debug)
	    engine.logInfo( "REQUEST RESPONSE: " + resp.toString(), src );
	
	return resp;
    }   
}
