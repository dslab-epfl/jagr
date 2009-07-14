/**
 * $Id: RRFaultInjector.java,v 1.6 2004/09/20 05:07:10 candea Exp $
 **/
package roc.faultinject;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.management.*;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;
import roc.rr.Action;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

public class RRFaultInjector 
{
    // Log output
    static Logger log = Logger.getLogger( "RRFaultInjector" );

    // Path of fault injection configuration xml file
    //   ex. /home/skawamo/faultinject/conf 
    private static final String faultFilePath = "/home/candea/faultinject/conf/" ;


    public static void main( String[] argv ) 
    {
	String log4jcfg = System.getProperty( "env.log4j" ); // temporary hack
	assert !log4jcfg.equals( "null" );
	PropertyConfigurator.configure( log4jcfg );

	if( argv.length != 2 ) 
	{
	    log.fatal( "\nNeed arguments: <hostname> <faultload>\n" );
	    System.exit(-1);
	}
        
        // hostname running the JBoss server we're talking to
        String targetHost = argv[0];

        // filename --- this can be either an absolute filename, or a
        // filename relative to $JBOSS_HOME/server/default/conf/
	String faultfile = argv[1];


        // initialize or reset the timer (as needed)
        Timer faultInjectionTimer = new Timer();

        // get absolute path to configuration file
        String absoluteFileName; 
	absoluteFileName = faultFilePath+"/"+faultfile; 

        // parse XML config file and get a list of fault injection actions
        List injections = null;
        try {
            XMLParser parser = XMLParser.getInstance();
            injections = parser.parseFaultInjection( absoluteFileName );
        } catch ( Exception e ) {
            faultInjectionTimer = null;
            e.printStackTrace();
	    System.exit(-1);
	}

        // schedule a timer task for each fault
        FaultInjection injection=null;
        FaultInjectionTask task=null;
        Date lastDate = new Date();
        Date date;
        Iterator it = injections.iterator();
        while ( it.hasNext() )
	{
	    injection = (FaultInjection) it.next();
	    task = new FaultInjectionTask( injection, targetHost );
	    date = injection.getDate();
	    if ( date.compareTo(lastDate) > 0 ) {
		lastDate = date;
	    }
	    faultInjectionTimer.schedule( task, date );
	}

	// schedule an "end of campaign" event after the last fault
	Date newDate  = new Date( 100 + lastDate.getTime() ); // 100 msec later
	injection = new FaultInjection( Action.END_OF_CAMPAIGN, 
					"", newDate);
	task = new FaultInjectionTask( injection, targetHost );
	faultInjectionTimer.schedule( task, newDate );
    }
}
