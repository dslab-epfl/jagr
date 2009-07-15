/*
 * $Id: Main.java,v 1.11 2004/09/21 22:40:43 candea Exp $
 */

package roc.loadgen.interceptors.loadbalancer;

import java.net.*;
import java.util.*;
import roc.loadgen.*;
import roc.loadgen.http.*;
import org.apache.log4j.Logger;

/**
 * Client-side load balancer.
 *
 * @version <tt>$Revision: 1.11 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class Main extends RequestInterceptor 
{
    // Log output
    private static Logger log = Logger.getLogger( "LoadBalancer" );

    // Indicate if clients want session affinity regardless of srv availability
    private static boolean doFailover;
    
    // List of servers across which we balance load
    private static ArrayList servers = new ArrayList();

    // The thread that manages failover
    private static FailoverManager fm;

    // A random number generator for all clients
    private static Random rand = new Random();

    private static int Routed[] = { 0, 0, 0, 0, 0, 0, 0, 0 };

    /**
     * Upon startup, initialize the list of servers, based on the
     * 'servers' argument (a comma-separated list of <host:port> names)
     * and decide whether to implement session affinity.
     **/
    public void start() 
    {
	if( !servers.isEmpty() )  // already initialized
	    return;

	synchronized( servers )
	{
	    if( !servers.isEmpty() )  // someone beat us to it
		return;

	    String arg = (String) args.get( ARG_SERVERS );
	    String[] srv = arg.split( "," );

	    for( int i=0 ; i < srv.length ; i++ )
		servers.add( (Object) srv[i] );

	    doFailover = ((Boolean) args.get( DO_FAILOVER )).booleanValue();

	    // Start a thread that listens for host up/down notifications
	    int port = ((Integer) args.get( ARG_LISTENPORT )).intValue();
	    fm = new FailoverManager( port );
	    fm.start();
	}
    }

    /**
     * Process an invocation by setting the full URL in the request,
     * based on load balancing requirements.  Each request in a
     * session goes to the same server to which the first request in
     * that session went, with one exception: if doFailover is set and
     * the preferred server is unavailable, we route the request to a
     * new server.
     *
     * @param req request to be executed
     **/
    public Response invoke( Request req )
	throws AbortRequestException
    {
	HttpRequest httpReq = (HttpRequest) req;

	String server;

	if( httpReq.firstInSession() ) // first req in sess gets new server
	{
	    server = findServer();
	    httpReq.getParentSession().setPreferredServer( server );
	}
	else
	{
	    server = httpReq.getParentSession().getPreferredServer();

	    if( isDown(server) )
	    {
		if( doFailover )
		{
		    String newServer = findServer();
		    httpReq.getParentSession().setPreferredServer( newServer );
		    log.debug( "Failing over from " + server + " to " + newServer );
		    server = newServer;
		}
		else
		{
		    log.debug( server + " is down, but not failing over (as requested)" );
		}
	    }
	}
	    
	httpReq.setDestination( server );

	return invokeNext( req );
    }

    // TODO: fix this horrible linear search
    private boolean isDown( String server )
    {
	for( int i=0 ; i < servers.size() ; i++ )
	{
	    if( server.equals( (String) servers.get(i) ))
		return false;
	}

	return true;
    }

    public static ArrayList getServerListCopy()  { return (ArrayList)servers.clone(); }

    public static void setServerList( ArrayList newServerList )  
    { 
	log.debug( "Updated server list: " + newServerList );
	servers = newServerList; 
    }

    /**
     * Choose a server to which we can send the next request; policy
     * is round-robin.
     **/
    private synchronized String findServer()
    {
	if( servers.size() == 0 )
	{
	    log.debug( "No servers are up right now; defaulting to rr7" );
	    return "rr7";
	}

	int next = rand.nextInt(Integer.MAX_VALUE) % servers.size();
	synchronized( Routed )
	{
	    Routed[next]++;
	}
	return (String)servers.get( next );
    }

    private static boolean printed=false;
    public void stop()
    {
	if( !printed )
	{
	    printed = true;
	    for( int i=0 ; i < 8 ; i++ )
		log.info( "Routed ~" + Routed[i] + " sessions to rr" + (7+i) + ": "  );
	}
    }

    /*----------------------------------------------------------------------*/

    public static final String ARG_SERVERS  = "servers";
    public static final String DO_FAILOVER = "do_failover";
    public static final String ARG_LISTENPORT = "listener_port";

    private static Arg[] argDefs = { 
	new Arg( ARG_SERVERS, "servers for balancing", Arg.ARG_STRING, true, null ),
        new Arg( DO_FAILOVER, "failover to good nodes", Arg.ARG_BOOLEAN, true, null ),
        new Arg( ARG_LISTENPORT, "listening port for host updates", Arg.ARG_INTEGER, true, null )
    };

    public Arg[] getArguments() 
    { 
	return argDefs;
    }
}
