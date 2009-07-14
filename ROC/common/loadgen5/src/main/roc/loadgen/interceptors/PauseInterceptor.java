package roc.loadgen.interceptors;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

/**
 * TODO: someone needs to set the attribute "ENGINE_ATTR_IS_PAUSED" in the
 *       engine to tell the PauseInterceptor to actually pause
 *
 */
public class PauseInterceptor extends RequestInterceptor {

	public static final String ARG_SLEEP_INTERVAL = "sleepinterval";
	public static final String ARG_MAX_STALL = "maxstall";

	public static final String DEFAULT_SLEEP_INTERVAL = "100";
	public static final String DEFAULT_MAX_STALL = Integer.toString( Integer.MAX_VALUE );

	int sleepInterval;
	int maxStall;


	Arg[] argDefinitions = {
		new Arg( ARG_SLEEP_INTERVAL, 
		         "how long to sleep at once while pausing",
				 Arg.ARG_INTEGER,
		         false,
		         DEFAULT_SLEEP_INTERVAL ),
		new Arg( ARG_MAX_STALL,
				 "maximum stall before letting request continue",
				 Arg.ARG_INTEGER,
				 false,
				 DEFAULT_MAX_STALL )
	};

	/* (non-Javadoc)
	 * @see roc.loadgen.RequestInterceptor#getArguments()
	 */
	public Arg[] getArguments() {
		return argDefinitions;
	}
	

	public void start() {
		sleepInterval = ((Integer)args.get( ARG_SLEEP_INTERVAL )).intValue();
		maxStall = ((Integer)args.get( ARG_MAX_STALL )).intValue();
	}

	public boolean isPaused() {
		return Boolean.TRUE.equals( engine.getAttr( "ENGINE_ATTR_IS_PAUSED") );
	}

	public void pause( int id ) {

		long currTime = System.currentTimeMillis();
		long startTime = currTime;

		engine.logStats( "(" + id + ") PauseInterceptor: paused " + startTime );

		while (isPaused()) {
			try {
				Thread.sleep(sleepInterval);
			} catch (InterruptedException ignore) {
			}

			// Make sure we're not stalling for too long
			currTime = System.currentTimeMillis();
			if (currTime + sleepInterval >= startTime + maxStall)
				break;
		}

		engine.logStats( "(" + id + ") PauseInterceptor: unpaused " + currTime );
		engine.logStats( "(" + id + ") PauseInterceptor: stalled for " + ( currTime - startTime ));
	}

	public Response invoke(Request req, Object src)
		throws AbortRequestException, AbortSessionException {

		// If we're being paused, then wait for at most our own
		// delay amount of time before retrying
		if (isPaused()) {
			pause( req.getId() );
		}

		return invokeNext(req, src);
	}

}

