/**
 * $Id: MicrorebootAction.java,v 1.3 2004/07/24 03:02:31 candea Exp $
 **/

package roc.jagr.recomgr.action;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;

import roc.jagr.recomgr.RecoveryManager;
import roc.jagr.recomgr.RecoveryAction;

import org.apache.log4j.Logger;

public class MicrorebootAction extends RecoveryAction {

    static Logger log = Logger.getLogger( "MicrorebootAction" );

    Collection               badComponents; // components that need to be uRB-ed
    AppletRemoteMBeanInvoker invoker=null;  // invoker used in talking to JBoss
    ObjectName               service=null;  // name of the JBoss svc used for uRB-ing

    /**
     * Constructor.
     *
     * @param mgr           recovery manager taking the uRB action
     * @param badComponents components that need to be recovered
     * @throws MalformedURLException        if the target hostname is invalid
     * @throws MalformedObjectNameException if the service name is invalid
     **/

    public MicrorebootAction( RecoveryManager mgr, Collection badComponents ) 
	throws MalformedURLException, MalformedObjectNameException
    {
	super( mgr );

	assert badComponents!=null;
	assert !badComponents.isEmpty();
	assert invoker==null;

	this.badComponents = badComponents;
	invoker = new AppletRemoteMBeanInvoker( "http://" + 
						mgr.getTargetHostname() + 
						":8080/web-console/Invoker" );
	service = new ObjectName( "jboss.system:service=MainDeployer" );
    }

    /**
     * Ask JBoss to uRB all the components we think are bad.
     *
     * @throws Exception if something went wrong during the invocation
     **/
    public void doAction() 
	throws Exception
    {
	assert !badComponents.isEmpty();

	Iterator iter = badComponents.iterator();
	while( iter.hasNext() )
	{
	    String ejb = (String) iter.next();
	}

	try {
	    log.debug( "Microrebooting " + badComponents );
	    invoker.invoke( service, "microreboot",
			    new Object[] { (Object) badComponents },
			    new String[] { "java.util.Set" } );
	}
	catch( Exception e )
	{
	    log.error( "Couldn't microreboot " + badComponents );
	    log.error( "Got " + e );
	}
    }
}
