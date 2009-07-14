/*
 * $Id: FixedThinkTimeInterceptor.java,v 1.1 2004/08/20 04:19:24 candea Exp $
 */

package roc.loadgen.interceptors;

import java.util.Random;
import roc.loadgen.*;
import org.apache.log4j.Logger;

/**
 * Delay user requests for a fixed amount of time ("think time").
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class FixedThinkTimeInterceptor extends RequestInterceptor 
{
    // Log output
    private static Logger log = Logger.getLogger( "interceptors.FixedThinkTimeInterceptor" );

    // Number of seconds for think time
    private static int thinkTime = 0;

    public void start() 
    {
	thinkTime = ((Integer) args.get( ARG_SECONDS )).intValue();
    }

    public Response invoke(Request req)
	throws AbortRequestException {

	try {
	    Thread.sleep( 1000 * thinkTime );
	} catch (InterruptedException e) {
	    throw new AbortRequestException( e );
	}

	return invokeNext(req);
    }

    /*----------------------------------------------------------------------*/

    public static final String ARG_SECONDS  = "seconds";

    Arg[] argDefs = { 
	new Arg( ARG_SECONDS, "# of seconds to think", Arg.ARG_INTEGER, 
		 false, "2" )
    };

    public Arg[] getArguments() 
    { 
	return argDefs;
    }
}
