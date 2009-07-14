package roc.loadgen.interceptors;

import java.util.Random;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

public class PeriodicPauseInterceptor extends RequestInterceptor {

    public static final String ARG_WAKE_PERIOD = "wakeperiod";
    public static final String ARG_SLEEP_PERIOD = "sleepperiod";

    long nextSleepTime = -1;
    int sleepPeriod;
    int wakePeriod;

    Arg[] argDefinitions = { 
	new Arg( ARG_WAKE_PERIOD,
		 "how long to stay awake and sending requests before going to sleep",
		 Arg.ARG_INTEGER,
		 true,
		 null ),
	new Arg( ARG_SLEEP_PERIOD,
		 "how long to sleep before waking up again",
		 Arg.ARG_INTEGER,
		 true,
		 null )
    };

    public PeriodicPauseInterceptor() {
    }

    public Arg[] getArguments() {
	return argDefinitions;
    }

    public void start() {
	wakePeriod = ((Integer)args.get( ARG_WAKE_PERIOD )).intValue();
	sleepPeriod = ((Integer)args.get( ARG_SLEEP_PERIOD )).intValue();
	nextSleepTime = System.currentTimeMillis();  // go to sleep immediately
    }
    
    public void stop() {
    }

    public Response invoke( Request req, Object src ) 
	throws AbortRequestException, AbortSessionException {
	
	try {
	    if( System.currentTimeMillis() >= nextSleepTime ) {
		Thread.sleep( sleepPeriod );
		nextSleepTime = System.currentTimeMillis() + wakePeriod;
	    }
	}
	catch( InterruptedException e ) {
	    throw new AbortRequestException( e );
	}

	return invokeNext(req, src);
    }

}
