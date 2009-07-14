/*
 * $Id: DetectByComparison.java,v 1.4 2004/08/28 19:03:46 skawamo Exp $
 */

package roc.loadgen.interceptors;

import java.net.*;
import java.util.*;
import roc.loadgen.*;
import roc.loadgen.http.*;
import org.apache.log4j.Logger;

/**
 * Detect incorrect responses by comparing them to those of a
 * known-good server.
 *
 * @version <tt>$Revision: 1.4 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class DetectByComparison extends RequestInterceptor 
{
    // Log output
    private static Logger log = Logger.getLogger( "interceptors.DetectByComparison" );

    // The server whose responses we're testing
    private static String testServer;

    // The known-good server 
    private static String goodServer;

    public void start() 
    {
	testServer = (String) args.get( ARG_TEST );
	goodServer = (String) args.get( ARG_GOOD );
    }

    public Response invoke( Request req )
	throws AbortRequestException
    {
	// TODO: make this multi-threaded ??? seems silly...

	HttpRequest httpReq = (HttpRequest) req;
	HttpResponse httpResp, goodResp;

	httpReq.setDestination( testServer );
	httpResp = (HttpResponse) invokeLeft( httpReq );
	
	httpReq.setDestination( goodServer );
	goodResp = (HttpResponse) invokeRight( httpReq );

	if( httpResp.getRespCode() != goodResp.getRespCode() )
	{
	    log.info( "HTTP response codes don't match (good=" + goodResp.getRespCode() + ", bad=" + httpResp.getRespCode() + ")" );
	    httpResp.setIsError();
	    httpResp.setReferenceResponse( goodResp );
	}
	else if( !httpResp.getRespStr().equals( goodResp.getRespStr() ) )
	{
	    log.debug( "HTML doesn't match; doing regexp substitution" );

	    String respStr = httpResp.getRespStr();
	    String goodStr = goodResp.getRespStr();

	    String time = "\\d+:\\d+:\\d+";  // time
	    respStr = respStr.replaceAll( time, "" );
	    goodStr = goodStr.replaceAll( time, "" );

	    String date = "\\d\\d\\d\\d-\\d+-\\d+";  // date
	    respStr = respStr.replaceAll( date, "" );
	    goodStr = goodStr.replaceAll( date, "" );

	    if( respStr.equals( goodStr ) )
		return httpResp;

	    httpResp.setRespStr( respStr );
	    goodResp.setRespStr( goodStr );
	    log.info( "HTML content doesn't match" );
	    httpResp.setIsError();
	    httpResp.setReferenceResponse( goodResp );
	}

	return httpResp;
    }

    /*----------------------------------------------------------------------*/

    public static final String ARG_GOOD = "good_server";
    public static final String ARG_TEST = "test_server";

    Arg[] argDefs = { 
	new Arg( ARG_GOOD, "known-good server", Arg.ARG_STRING, true, null ),
        new Arg( ARG_TEST, "server under test", Arg.ARG_STRING, true, null )
    };

    public Arg[] getArguments() 
    { 
	return argDefs;
    }
}
