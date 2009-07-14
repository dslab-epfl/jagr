package roc.rr.afpi;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import roc.rr.*;

/** 
 * A generic interceptor used by RR code.  All actual interceptors
 * should extend this one
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.1 $ 
 *
 **/

public class Interceptor
{
    /* DB agent */
    protected static DBUtil DB = null;

    /* Component, method, and fault name to be scheduled (null if none) */
    private static String badComp = null; // DEPRECATED
    private static String compUrl = null;
    private static String badMeth = null;
    private static boolean throwException = false;
    private static boolean throwError     = false;
    private static boolean microReboot    = false;
   
    public static String badComp1 = null;
    public static String badComp2 = null;
    public static String badComp3 = null;

    /** 
     * Connects to the database and initializes local prepared statements.  
     *
     **/
    public Interceptor()
    {
	DB = new DBUtil();
    }


    public boolean toReboot()
    {
	return microReboot;
    }

    public String getBadComp()
    {
	return badComp;
    }

    /** 
     * Adds an entry in the 'components' table in the database.
     *
     * @param UID        universal ID of component being registered
     * @param methods    list of strings representing the method names
     *
     **/
    public static void addComponent( String UID, String name, List methods, String type )
    {
	Component component = new Component( UID, name, methods, type, null );
	addComponent( component );
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
	Component component = new Component( UID, null, methods, type, null );
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


    public static void addComponent( String UID, String name, String type )
    {
	Component component = new roc.rr.Component( UID, name, type, (String)null );
	addComponent( component );
    }


    public static void addComponent( Component comp )
    {
	DB.storeComponent( comp );
    }

    /** 
     * Removes an entry from the 'components' table in the database.
     *
     * @param UID        universal ID of component being unregistered
     *
     **/
    public void removeComponent( String UID )
    {
	DB.removeComponent( UID );
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
     * Schedules a fault for injection in a given component.  All
     * subsequent calls to that component will fail.
     *
     * @param UID        universal ID of component that should fail
     * @param type       how to fail (can be <i>Exception</i> or <i>Error</i>)
     *
     **/
    public synchronized static void scheduleFaultByUrl( String url, String type )
    {
	compUrl = url;
	badMeth = null;

	if ( type.toLowerCase().equals("error") ) 
	{
	    throwError     = true;
	    throwException = false;
	    microReboot    = false;
	}
	else {
	    throwError     = false;
	    throwException = true;
	    microReboot    = false;
	}
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
	    microReboot    = false;
	}
	else {
	    throwError     = false;
	    throwException = true;
	    microReboot    = false;
	}
    }


    /** 
     * Schedules a microreboot for a given component.  The next call
     * to that component will result in the component being
     * microrebooted.
     *
     * @param UID        universal ID of component that should fail
     * @param type       how to fail (can be <i>Exception</i> or <i>Error</i>)
     *
     **/
    public synchronized static void scheduleMicroReboot( String UID )
    {
	/* FIXME: validate that the requested component and method exist */
	badComp = UID;
	badMeth = null;

	throwError     = false;
	throwException = false;
	microReboot    = true;
    }

    public synchronized static void multiScheduleMicroReboot3(
       String comp1, String comp2, String comp3 )
    {
       if (comp1.length() > 0)
          badComp1 = comp1;
       if (comp2.length() > 0)
          badComp2 = comp2;
       if (comp3.length() > 0)
          badComp3 = comp3;

       badMeth = null;
       
       throwError     = false;
       throwException = false;
       microReboot    = true;
    }

    /** 
     * Cancels a scheduled microreboot.
     *
     **/
    public synchronized static void cancelMicroReboot()
    {
	microReboot = false;
	badComp     = null;
	badMeth     = null;
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
    public void preInvoke( String url, String opName )
    {
	// System.out.println("~~~~~ Interceptor:preInvoke on UID " + UID + " (method " + opName + ")");
	if ( compUrl!=null && compUrl.equals(url) )
	{
	    if ( badMeth==null || badMeth.equals(opName) )
	    {
		if ( throwException ) {
		    throw new RuntimeException("AFPI-induced");
		}
		else if ( throwError ) {
		    throw new Error("AFPI-induced");
		}
		else { // microreboot
		    System.out.println("++++++++++ Pretending to microreboot " + url);
		    // Still need to do the uRB here...
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

