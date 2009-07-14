/**
 * $Id: InjectSingleFault.java,v 1.3 2004/09/20 05:07:10 candea Exp $
 **/
package roc.faultinject;

import roc.rr.Action;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.management.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

/**
 * Inject a single fault into a JBoss app.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class InjectSingleFault 
{
    public static void main( String[] argv ) 
	throws Exception
    {
	String log4jcfg = System.getProperty( "env.log4j" ); // temporary hack
	assert !log4jcfg.equals( "null" );
	PropertyConfigurator.configure( log4jcfg );

	if( argv.length != 3 )
	    rtfm();
        
	String compName  = argv[1];
	int faultType=-1;

	String fault = argv[2];
	if( fault.equals( "exception" ) )
	    faultType = Action.INJECT_THROWABLE;
	else if( fault.equals( "data" ) )
	    faultType = Action.CORRUPT_DATA;
	else if( fault.equals( "tx" ) )
	    faultType = Action.SET_NULL_TXINT;
	else if( fault.equals( "jndi" ) )
	    faultType = Action.CORRUPT_JNDI;
	else
	{
	    System.out.println( "Unknown fault type " + fault );
	    System.exit( -1 );
	}

	FaultInjection inj = new FaultInjection( faultType, compName, null );

	FaultInjectionTask.doInjection( argv[0], inj );
    }

    //---------------------------------------------------------------------------

//     private static void injectThrowable() throws Exception
//     {
// 	invoker.invoke( service, "scheduleThrowable",
// 			new Object[] { (Object) comp, (Object) "HelloError" },
// 			new String[] { "java.lang.String", "java.lang.String" } );

// 	 )  ret += "Throwable";
// 	else if ( faultType == Action.MICROREBOOT )       ret += "Microreboot";
// 	else if ( faultType == Action.DEADLOCK )          ret += "Deadlock";
// 	else if ( faultType == Action.NO_ACTION )         ret += "Cancel";
// 	else if ( faultType == Action.FULL_REBOOT )       ret += "Full reboot";
// 	else if ( faultType == Action.END_OF_CAMPAIGN )   ret += "End of campaign";
// 	else if ( faultType == Action.INJECT_MEMLEAK )    ret += "Mem Leak (" + amount + " bytes/call)";
// 	else if ( faultType == Action.UNBIND_NAME )       ret += "Unbind Name" ;
// 	else if ( faultType == Action.SET_NULL_TXINT )    ret += "Null Map" ;
// 	else if ( faultType == Action.INFINITE_LOOP  )    ret += "Infinite Loop" ;
// 	else if ( faultType == Action.CORRUPT_JNDI )      ret += "JNDI Corruption / "+ctype;
// 	else if ( faultType == Action.CORRUPT_DATA )      ret += "DATA Corruption / "+ctype+" / "+ctime;

//     }

//     private static void injectDataCorruption() throws Exception
//     {
// 	assert comp.equals("BidPK")  || comp.equals("CategoryPK") || comp.equals("IDManagerPK") 
// 	    || comp.equals("UserPK") || comp.equals("BuyNowPK")   || comp.equals("CommentPK") 
// 	    || comp.equals("ItemPK") || comp.equals("RegionPK")   || comp.equals("SessionAttribute")
// 	    || comp.equals("SessionState");
	
// 	invoker.invoke( service, "scheduleDataCorruption",
// 			new Object[] { (Object) comp, (Object) "NULL", (Object) new Integer(1) },
// 			new String[] { "java.lang.String", "java.lang.String", "java.lang.Integer" } );
//     }

//     private static void injectNullTx() throws Exception
//     {
// 	invoker.invoke( service, "setNullInTxInterceptorCMT",
// 			new Object[] { (Object) comp },
// 			new String[] { "java.lang.String" } );
//     }

//     private static void injectJndiCorruption()	throws Exception
//     {
// 	invoker.invoke( service, "corruptJndiName",
// 			new Object[] { (Object) comp, (Object) "NULL" },
// 			new String[] { "java.lang.String", "java.lang.String" } );
//     }

    //---------------------------------------------------------------------------

    private static void rtfm()
    {
	System.err.println( "\nNeed arguments: <hostname> <component name> <fault>\n" );
	System.exit(-1);
    }
}

