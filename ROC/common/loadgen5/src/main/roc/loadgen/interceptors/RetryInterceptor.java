package roc.loadgen.interceptors;

import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Response;

public class RetryInterceptor extends RequestInterceptor {

	public static final String ARG_NUM_RETRIES = "numretries";
	public static final String ARG_ABORT_SESSION = "abortsession";

	int numRetries;
	boolean abortSession;

	Arg[] argDefinitions = {
		new Arg( ARG_NUM_RETRIES,
		         "number of times to retry before giving up",
		         Arg.ARG_INTEGER,
		         false,
		         "3" ),
		new Arg( ARG_ABORT_SESSION,
		         "set to true to abort the session, otherwise, we'll continue the rest of the trace",
		         Arg.ARG_BOOLEAN,
		         false,
		         "true" )
	};

	/**
	 * @see roc.loadgen.RequestInterceptor#getArguments()
	 */
	public Arg[] getArguments() {
		return argDefinitions;
	}
	
	public void start() {
		abortSession = ((Boolean) args.get(ARG_ABORT_SESSION)).booleanValue();
		numRetries = ((Integer) args.get(ARG_NUM_RETRIES)).intValue();
	}

	public Response invoke(Request req, Object src)
		throws AbortRequestException, AbortSessionException {

		int c = 0;
		Response resp;

		do {
			resp = invokeNext(req, src);

			if (!resp.isError()) {
				// we're done!
				break;
			}

			c++;
		}
		while( c <= numRetries );

		if (abortSession && c >= numRetries) {
			throw new AbortSessionException(
				"Exceeded " + numRetries + " retries");
		}

		// return a successful response; or the last error response
		return resp;
	}

}
