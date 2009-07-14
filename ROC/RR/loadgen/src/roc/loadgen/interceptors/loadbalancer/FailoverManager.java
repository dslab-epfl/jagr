/*
 * $Id: FailoverManager.java,v 1.3 2004/08/27 22:16:34 candea Exp $
 */

package roc.loadgen.interceptors.loadbalancer;

import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import swig.util.UDP;

/**
 * This thread manages failover in the load balancer.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class FailoverManager extends Thread
{
    // Log output
    private static final Logger log = Logger.getLogger( "LoadBalancer.FailoverManager" );

    // UDP port on which we listen for updates
    private static int port;

    /**
     * Constructor.
     *
     * @param port The UDP port we should listen on for updates
     **/
    public FailoverManager( int port )
    {
	this.port = port;
    }

    /**
     * Main body of the failover manager.
     *
     **/
    public void run() 
    {
	log.info( "listening on port " + port );
	while( true ) 
	{
	    try {
		Object obj = UDP.receive( port );
		HostUpdate update = (HostUpdate)obj;
		processUpdate( update );
	    }
	    catch( Exception e ) {
		log.error( "Exception while receiving host update" );
		e.printStackTrace();
	    }
	}
    }

    /**
     * Process a received server up/down update.
     *
     * @param update The HostUpdate object to process
     **/
    private static void processUpdate( HostUpdate update )
    {
	ArrayList servers = Main.getServerListCopy();

	if( update.up ) // host is up, make sure it's in the list of hosts
	{
	    log.debug( "Received notification: \"" + update.hostname + " is UP\"" );

	    for( int i=0 ; i < servers.size() ; i++ )
	    {
		String srv = (String) servers.get( i );
		if( srv.equals( update.hostname ) )
		    return; // it's already in the list
	    }

	    servers.add( (Object) (update.hostname) );
	    Main.setServerList( servers );
	}
	else // host went down, remove it from the list of hosts
	{
	    log.debug( "Received notification: \"" + update.hostname + " is DOWN\"" );

	    for( int i=0 ; i < servers.size() ; i++ )
	    {
		String srv = (String) servers.get( i );
		if( srv.equals( update.hostname ) )
		    servers.remove( i ); // get rid of it
	    }

	    Main.setServerList( servers );
	}
    }
}
