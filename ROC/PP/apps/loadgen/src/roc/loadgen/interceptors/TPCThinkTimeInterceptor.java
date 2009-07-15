package roc.loadgen.interceptors;

import java.util.Random;

import roc.loadgen.AbortRequestException;
import roc.loadgen.InitializationException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

import org.apache.log4j.Logger;

public class TPCThinkTimeInterceptor extends RequestInterceptor {

    private static Logger log = Logger.getLogger( "interceptors.TPCThinkTimeInterceptor" );

	Random rand;

	Arg[] argDefinitions = { };
	
	public TPCThinkTimeInterceptor() {
		rand = new Random();		
	}

	/**
	 * @see roc.loadgen.RequestInterceptor#getArguments()
	 */
	public Arg[] getArguments() {
		return argDefinitions;
	}

	/**
	 * Negative exponential distribution w/a mean think time of 7sec.
	 * used by TPC-W spec for
	 * think time (clause 5.3.2.1) and USMD (clause 6.1.9.2).  This
	 * function is borrowed from the RUBiS Client,
	 *   edu.rice.rubis.client.TransitionTable.
	 */
	private long TPCWThinkTime() {
		double r = rand.nextDouble();
		if (r < (double) 4.54e-5)
			return ((long) (r + 0.5));
		return ((long) ((((double) - 7000.0) * Math.log(r)) + 0.5));
	}

	public Response invoke(Request req)
		throws AbortRequestException {

		try {
			Thread.sleep(TPCWThinkTime());
		} catch (InterruptedException e) {
			throw new AbortRequestException( e );
		}

		return invokeNext(req);
	}

}
