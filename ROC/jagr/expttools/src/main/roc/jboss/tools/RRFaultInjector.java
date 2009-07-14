/**
 * $Id: RRFaultInjector.java,v 1.2 2004/07/29 01:59:57 candea Exp $
 **/
package roc.jboss.tools;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.management.*;

import org.jboss.console.remote.AppletRemoteMBeanInvoker;


public class RRFaultInjector {

    public static void main( String[] argv ) {

	if( argv.length != 2 )
	{
	    System.out.println( "\nNeed arguments: <hostname> <faultload>\n" );
	    System.exit(-1);
	}
        
        // hostname running the JBoss server we're talking to
        String targetHost = argv[0];

        // filename --- this can be either an absolute filename, or a
        // filename relative to $JBOSS_HOME/server/default/conf/
	String faultfile = argv[1];

	try {
	    // invoker used in talking to JBoss
	    AppletRemoteMBeanInvoker invoker= new AppletRemoteMBeanInvoker( "http://" + targetHost +
									    ":8080/web-console/Invoker" );
	    
	    // name of the JBoss svc used for fault injection
	    ObjectName service = new ObjectName( "RR:service=FaultInjection" ); 
						
	    
	    invoker.invoke( service, "startFaultInjectionCampaign",
                            new Object[] { (Object) faultfile },
			    new String[] { "java.lang.String" } );
	}
	catch( Exception ex ) {
	    ex.printStackTrace();
	}

    }

}

