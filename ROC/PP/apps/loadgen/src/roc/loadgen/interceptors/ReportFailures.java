/*
 * $Id: ReportFailures.java,v 1.3 2004/08/27 22:16:34 candea Exp $
 */

package roc.loadgen.interceptors;

import java.net.*;
import java.util.*;
import roc.loadgen.*;
import roc.loadgen.http.*;
import roc.recomgr.FailureReport;
import org.apache.log4j.Logger;

/**
 * Report failures to the recovery manager.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class ReportFailures extends RequestInterceptor 
{
    // Log output
    private static Logger log = Logger.getLogger( "interceptors.ReportFailures" );

    // The host and port where we can find the recovery manager
    String host;
    int port;

    // Socket through which we send the failure reports
    private DatagramSocket socket = null;

    /**
     * Startup: initialize recomgr hostname and port from arguments.
     **/
    public void start() 
    {
	host = (String) args.get( ARG_HOST );
	port = ((Integer) args.get( ARG_PORT )).intValue();
	try {
	    socket = new DatagramSocket();
	}
	catch( Exception e ) 
	{
	    e.printStackTrace();
	}
    }

    /**
     * Invoker passes request down the chain and checks whether the
     * response is OK or not.  If not, it reports a failure.
     *
     * @param req  The request being passed down the interceptor chain.
     **/
    public Response invoke( Request req )
	throws AbortRequestException
    {
	HttpResponse httpResp = (HttpResponse) invokeNext( req );

	if( ! httpResp.isOK() )
	    report( httpResp );
	    
	return httpResp;
    }

    /**
     * Report a failure to the recovery manager and include some
     * useful information with the report.
     *
     * @param httpResp  The HTTP response that is bad
     **/
    private void report( HttpResponse httpResp )
    {
	// extract host, port, servlet name, error code
	HttpRequest req = httpResp.getRequest();
	assert req != null;

	FailureReport rep = 
	    new FailureReport( req.getServer(), req.getPath(), httpResp.getRespCode() );

	log.debug( "sending failure report: " + rep );

	try {
	    byte[] bytes = rep.getBytes();
	    InetSocketAddress sockAddr = new InetSocketAddress( host, port );
	    DatagramPacket packet = new DatagramPacket( bytes, bytes.length, sockAddr );
	    socket.send( packet );
	}
	catch( Exception e ) 
	{
	    e.printStackTrace();
	}
    }

    /*----------------------------------------------------------------------*/

    public static final String ARG_HOST = "recomgr_host";
    public static final String ARG_PORT = "recomgr_port";

    Arg[] argDefs = { 
	new Arg( ARG_HOST, "recovery manager's hostname", Arg.ARG_STRING, true, null ),
	new Arg( ARG_PORT, "recovery manager's port", Arg.ARG_INTEGER, true, null )
    };

    public Arg[] getArguments() 
    { 
	return argDefs;
    }
}
