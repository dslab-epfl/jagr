/*
 * $Id: Session.java,v 1.3 2004/09/14 05:59:17 candea Exp $
 */

package roc.loadgen;

import java.util.Random;
import org.apache.log4j.Logger;

/**
 * Provides the notion of a user session.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 */

public class Session
{
    // Log output
    private static Logger log = Logger.getLogger( "Session" );

    // Keeps track of session's affinity for a given server
    private String preferredServer = null;
    
    /**
     * Constructor
     **/
    public Session()
    {
	log.debug( "New session: " + toString() );
    }

    /**
     * Indicate that this session wants to be be bound to the given
     * server.
     *
     * @param server <host:port> of preferred server (e.g., localhost:8080)
     **/
    public void setPreferredServer( String server )
    {
	this.preferredServer = server;
    }

    /**
     * Get name of server to which this session wants to be bound.  If
     * null, then session is not bound.
     **/
    public String getPreferredServer()
    {
	return preferredServer;
    }

    public String toString()
    {
	if( preferredServer != null )
	    return "[prefer: " + preferredServer + "]";
	else
	    return "[]";
    }
}

