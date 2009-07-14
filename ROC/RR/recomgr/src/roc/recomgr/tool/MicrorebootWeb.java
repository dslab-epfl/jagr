/**
 *  Microreboot is a utility class. It simply microreboot components  
 *
 * $Id: MicrorebootWeb.java,v 1.1 2004/08/28 20:52:23 skawamo Exp $
 **/

package roc.recomgr.tool;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;

import roc.recomgr.RecoveryManager;
import roc.recomgr.RecoveryAction;

public class MicrorebootWeb {

    public static void main(String[] arg) 
    {
	// invoker used in talking to JBoss
	AppletRemoteMBeanInvoker invoker=null;  
	// name of the JBoss svc used for uRB-ing
	ObjectName               service=null;  

	// argument check 
	if ( arg.length < 2 ) {
	    System.out.println("\nNeed arguments: <hostname> <war_file>\n");
	    return;
	}

	// hostname running the Jboss server we're talking to
	String targetHost = arg[0];

	// microreboot targets components 
	String warName = arg[1];

	try {
	    invoker = new AppletRemoteMBeanInvoker( "http://" + 
						    targetHost+
						    ":8080/web-console/Invoker" );
	    service = new ObjectName( "RR:service=RecoveryControl" );
	    invoker.invoke( service, "microrebootArchiveFile",
			    new Object[] { (Object) warName },
			    new String[] { "java.lang.String" } );
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

}
