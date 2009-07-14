/**
 *  FullReboot is a utility class. It simply full reboot application
 *
 * $Id: FullReboot.java,v 1.1 2004/08/28 07:04:50 skawamo Exp $
 **/

package roc.recomgr.tool;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;

import roc.recomgr.RecoveryManager;
import roc.recomgr.RecoveryAction;

public class FullReboot {

    public static void main(String[] arg) 
    {
	// invoker used in talking to JBoss
	AppletRemoteMBeanInvoker invoker=null;  
	// name of the JBoss svc used for uRB-ing
	ObjectName               service=null;  

	// argument check 
	if ( arg.length != 2 ) {
	    System.out.println("\nNeed arguments: <hostname> <applicaion_name>\n");
	    return;
	}

	// hostname running the Jboss server we're talking to
	String targetHost = arg[0];

	// application name
	String application = arg[1];

	try {
	    invoker = new AppletRemoteMBeanInvoker( "http://" + 
						    targetHost+
						    ":8080/web-console/Invoker" );
	    service = new ObjectName( "RR:service=RecoveryControl" );
	    invoker.invoke( service, "fullRebootBySimpleArg",
			    new Object[] { (Object) application },
			    new String[] { "java.lang.String" } );
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

}
