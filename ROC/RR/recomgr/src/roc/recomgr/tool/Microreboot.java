/**
 *  Microreboot is a utility class. It simply microreboot components  
 *
 * $Id: Microreboot.java,v 1.1 2004/08/28 07:04:50 skawamo Exp $
 **/

package roc.recomgr.tool;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;

import roc.recomgr.RecoveryManager;
import roc.recomgr.RecoveryAction;

public class Microreboot {

    public static void main(String[] arg) 
    {
	// invoker used in talking to JBoss
	AppletRemoteMBeanInvoker invoker=null;  
	// name of the JBoss svc used for uRB-ing
	ObjectName               service=null;  

	// argument check 
	if ( arg.length < 2 ) {
	    System.out.println("\nNeed arguments: <hostname> <uRB_target1> [<uRB_target2> ... ]\n");
	    return;
	}

	// hostname running the Jboss server we're talking to
	String targetHost = arg[0];

	// microreboot targets components 
	HashSet componentSet = new HashSet();
	for(int i=1;i<arg.length;i++) {
	    componentSet.add(arg[i]);
	}

	try {
	    invoker = new AppletRemoteMBeanInvoker( "http://" + 
						    targetHost+
						    ":8080/web-console/Invoker" );
	    service = new ObjectName( "jboss.system:service=MainDeployer" );
	    invoker.invoke( service, "microreboot",
			    new Object[] { (Object) componentSet },
			    new String[] { "java.util.Set" } );
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

}
