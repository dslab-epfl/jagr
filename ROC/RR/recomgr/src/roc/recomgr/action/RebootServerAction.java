/**
 * $Id: RebootServerAction.java,v 1.2 2004/09/12 20:44:11 candea Exp $
 **/

package roc.recomgr.action;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import swig.util.*;

import javax.management.*;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;

import org.apache.log4j.Logger;

import roc.recomgr.*;
import roc.loadgen.interceptors.loadbalancer.*;

/**
 * Simple action to report a failure to the load balancer.
 *
 * @version <tt>$Revision: 1.2 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class RebootServerAction extends RecoveryAction 
{
    // Log output
    private static final Logger log = Logger.getLogger( "action.RebootServerAction" );

    public RebootServerAction( RecoveryManager mgr, FailureReport report ) 
    {
	super( mgr );
	setAffectedServer( report.getServer() );
    }

    public void doAction() 
	throws Exception
    {
	String srv = getAffectedServer();

	// reboot the remote JBoss server SYNCHRONOUSLY (which means
	// we can't do multiple reboots at the same time)
	log.debug( "Remotely rebooting JBoss on " + srv );
	String pathToScript = System.getProperty("recomgr.bindir");
	Process p = Runtime.getRuntime().exec( pathToScript + "/remote-jboss-reboot.sh " + srv );
	log.debug( "Waiting for reboot process..." );
	p.waitFor();
	log.debug( "Reboot completed; returning" );
    }
}
