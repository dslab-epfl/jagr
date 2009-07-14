package roc.rr.rm;

import java.lang.*;
import java.sql.*;
import java.util.*;

import roc.rr.rm.*;

/** Conducts AFPI fault injection experiments.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $$
 */

public class AFPIDriver
{
    private RMDBUtil DB;
    private RMFIUtil rmfi;

    public AFPIDriver ()
    {
	try {
	    DB = new RMDBUtil( "swig", "afpi", "afpi", "afpi");
	    rmfi = new RMFIUtil();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

    public AFPIDriver (RMDBUtil dbUtil, RMFIUtil fiUtil)
    {
        DB = dbUtil;
        rmfi = fiUtil;
    }


    /** 
     * Obtains from the DB the UID of the currently deployed J2EE application
     * (corresponds to the UID column in the 'components' table). 
     *
     * @return  name of J2EE application
     * @throw   Exception if more than one J2EE app deployed
     *
     **/
    public String deployedApp()
	throws Exception
    {
	Statement stmt = DB.db.createStatement();
	ResultSet rs = stmt.executeQuery("SELECT uid FROM components WHERE type like '%j2ee%'");
	
	/* Get UID of the app */
	rs.next();
	String ret = rs.getString(1);
	
	/* Make sure this is the only deployed J2EE app */
	if ( ! rs.isLast() )
	{
	    String moreApps = "";
	    while ( rs.next() ) {
		moreApps += " " + rs.getString(1);
	    }
	    throw new Exception("More than one J2EE app deployed: " + ret + moreApps);
	}
	
	return ret;
    }

    /** 
     * Obtains from the DB the UIDs of the leaves of the subtree of components
     * rooted at a given parent.  This is just a snapshot, and may not be
     * consistent with reality, especially if the application is being
     * deployed during this call.
     *
     * @param   parentUID  UID of parent component
     * @return  a list of UIDs, naming the leaves
     *
     **/
    public LinkedList getLeaves( String parentUID )
    {
	
	try {
	    Statement stmt = DB.db.createStatement();

	    /* First build up the entire subtree of components */
	    LinkedList subtree = new LinkedList();  /* subtree of components rooted at given parent */
	    LinkedList leaves = new LinkedList(); /* subtree components that don't have children */
	    subtree.add( (Object)parentUID );
	    
	    while ( subtree.size() > 0 )
	    {
		String comp = (String) subtree.removeFirst();
		String query = "SELECT uid FROM components WHERE parent='" + comp + "'";
		ResultSet rs = stmt.executeQuery(query);
		
		if( ! rs.next() ) {
		    /* No children were returned, so this component is a leaf */
		    leaves.add( comp );
		}
		else {
		    /* This component has children; add the kids to the list */
		    subtree.add( rs.getString(1) );
		    while( rs.next() ) {
			subtree.add( rs.getString(1) );
		    }
		}
	    }

	    return leaves;
	}
	catch( SQLException sqle ) {
	    sqle.printStackTrace();
	}

	return null;
    }

    /** 
     * Runs an FI experiment that results in populating the 'faults' table.
     *
     **/
    public void runExperiment ()
	throws Exception
    {
	// rmfi.disableTrace();
	
	/* Find the currently-deployed app and its injectable components */
	String j2eeApp = deployedApp();
	LinkedList comps = getLeaves( j2eeApp );
	
	System.out.println("+++ found components:");
	for ( ListIterator iter=comps.listIterator() ; iter.hasNext() ; ) {
	    System.out.println( (String)iter.next() );
	}
	
	doInjections( j2eeApp, comps, "error");
	doInjections( j2eeApp, comps, "exceptions");
    }

    /** 
     * Runs an FI experiment that results in populating the 'faults' table.
     *
     * @param  j2eeApp   the J2EE application under study
     * @param  compList  list of component UIDs to inject
     * @param  faultType can be "error" or "exception"
     *
     **/
    private void doInjections( String j2eeApp, LinkedList compList, String faultType )
	throws Exception
    {
	/* Iterate through components, doing java.lang.Error injections */
	for( ListIterator iter = compList.listIterator() ; iter.hasNext() ; ) 
	{
	    /* Reboot app, to clean up staleness */
	    System.out.println("Rebooting " + j2eeApp);
	    rmfi.doMicroReboot( j2eeApp );
		
	    /* Inject a fault */
	    long injTime = System.currentTimeMillis();
	    String compName = (String) iter.next();
	    System.out.println( "Injecting " + faultType + " into " + compName );
	    rmfi.scheduleFault( compName, faultType );
		
	    /* Start the load generator */
	    rmfi.startLoad();
		
	    /* Wait for a whole minute to go by with no reported fault
	       (FIXME: if we have repeat faults, this will be an endless loop) */
	    long cur = DB.getNumFaultsSince( injTime ) ; 
	    long prev = cur - 1;
	    System.out.println("Waiting for faults to stabilize...");
	    while( cur > prev )
	    {
		Thread.sleep(60 * 1000); // sleep for 60 seconds
		System.out.println("WOKE UP");
		prev = cur;
		cur = DB.getNumFaultsSince( injTime );
	    }
		
	    /* Tell the load generator to stop */
	    System.out.println( "Stopping load generation" );
	    rmfi.stopLoad();
	}
    }


    public static void main(String[] args)
    {
	try {
	    (new AFPIDriver()).runExperiment();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }
}
