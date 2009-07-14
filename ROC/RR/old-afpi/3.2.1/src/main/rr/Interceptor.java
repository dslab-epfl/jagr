package rr;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import javax.ejb.*;
import javax.management.*;

/** 
 * A generic interceptor used by RR code.  All actual interceptors
 * should extend this one
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.2 $ 
 *
 **/

public class Interceptor
{
    /* DB agent */
    protected static DBUtil DB = null;

    /* Component, method, and fault name to be scheduled (null if none) */
    private static String badComp = null;
    private static String badMeth = null;
    private static boolean throwException = false;
    private static boolean throwError     = false;

    /** 
     * Connects to the database and initializes local prepared statements.  
     *
     **/
    public Interceptor()
    {
	DB = new DBUtil();
    }


    /** 
     * Adds an entry in the 'components' table in the database.
     *
     * @param UID        universal ID of component being registered
     * @param methods    list of strings representing the method names
     *
     **/
    public static void addComponent( String UID, List methods, String type )
    {
	Component component = new Component( UID, methods, type, null );
	addComponent( component );
    }


    /** 
     * Adds an entry in the 'components' table in the database, when
     * the methods are unknown.
     *
     * @param UID        universal ID of component being registered
     *
     **/
    public static void addComponent( String UID, String type )
    {
	Component component = new Component( UID, type, null );
	addComponent( component );
    }


    public static void addComponent( Component comp )
    {
	try {
	    DB.storeComponent( comp );
	}
	catch( SQLException sqle ) {
	    sqle.printStackTrace();
	}
    }

    /** 
     * Removes an entry from the 'components' table in the database.
     *
     * @param UID        universal ID of component being unregistered
     *
     **/
    public void removeComponent( String UID )
    {
	try {
	    DB.removeComponent( UID );
	}
	catch( SQLException sqle ) {
	    sqle.printStackTrace();
	}
    }


    /** 
     * Schedules a fault for injection in a given component.  All
     * subsequent calls to that component will fail.
     *
     * @param UID        universal ID of component that should fail
     * @param type       how to fail (can be <i>Exception</i> or <i>Error</i>)
     *
     **/
    public synchronized static void scheduleFault( String UID, String type )
    {
	/* FIXME: validate that the requested component exists in the DB */
	scheduleFault( UID, null, type );
    }


    /** 
     * Schedules a fault for injection in a given method of a given
     * component.  All subsequent calls to that method will fail.
     *
     * @param UID        universal ID of component that should fail
     * @param opName     name of component's method that should fail
     * @param type       how to fail (can be <i>Exception</i> or <i>Error</i>)
     *
     **/
    public synchronized static void scheduleFault( String UID, String opName, String type )
    {
	/* FIXME: validate that the requested component and method exist */
	badComp = UID;
	badMeth = opName;

	if ( type.toLowerCase().equals("error") ) 
	{
	    throwError     = true;
	    throwException = false;
	}
	else {
	    throwError     = false;
	    throwException = true;
	}
    }


    /** 
     * Takes control over the invocation to the given method of the
     * given component.  Checks whether a fault has been scheduled for
     * this method and, if yes, throws is.  Otherwise does nothing.
     *
     * @param UID        name of component being intercepted
     * @param opName     name of method being intercepted
     *
     **/
    public void preInvoke( String UID, String opName )
    {
	if ( badComp!=null && badComp.equals(UID) )
	{
	    /* If badMeth is null, component should fail on every call. */
	    if ( badMeth==null || badMeth.equals(opName) )
	    {
		if ( throwException ) {
		    throw new RuntimeException("AFPI-induced");
		}
		else { // throwing a VM error
		    throw new Error("AFPI-induced");
		}
	    }
	}
    }


    /** 
     * Cancels any scheduled fault.
     *
     **/
    public synchronized static String cancelFault ()
    {
	String ret;
	if ( badComp == null ) {
	    ret = "No fault was scheduled; nothing cancelled.";
	} else if ( badMeth == null ) {
	    ret = "Cancelled total failure of component " + badComp;
	} else if ( throwException ) {
	    ret = "Cancelled Exception failure of component " + badComp + 
		  " on calls to " + badMeth;
	} else {
	    ret = "Cancelled Error failure of component " + badComp + 
		  " on calls to " + badMeth;
	}

	badComp = null;
	badMeth = null;
	throwException = false;
	throwError = false;

	return ret;
    }


    public void reportFault( String compName, String methodName, Throwable fault )
    {
        DB.addFault( compName, methodName, fault );
    }
}

