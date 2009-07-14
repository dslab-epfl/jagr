/**
 * $Id: MicrorebootAction.java,v 1.3 2004/09/14 03:09:14 candea Exp $
 **/

package roc.recomgr.action;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;

import roc.recomgr.RecoveryManager;
import roc.recomgr.RecoveryAction;

import org.apache.log4j.Logger;

public class MicrorebootAction extends RecoveryAction 
{
    // Log output
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

	log.debug( "Microrebooting " + badComponents + " on " + mgr.getTargetHostname() );

	invoker.invoke( service, "microreboot",
			new Object[] { (Object) badComponents },
			new String[] { "java.util.Set" } );
    }
}
