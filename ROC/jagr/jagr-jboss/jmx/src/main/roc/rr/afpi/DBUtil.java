package roc.rr.afpi;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import roc.rr.*;

/** 
 * Utilities for interacting with the MySQL database
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.1 $
 */

public class DBUtil
{
    /* Is AFPI enabled or not? */
    private static boolean EnableAFPI = false;

    /* Connection to the database (one connection for all instances of DBUtil) */
    private static Connection db=null;

    /* Prepared statements for adding stuff */
    private static PreparedStatement addComp;  // add a row to the 'components' table
    private static PreparedStatement addFault; // add a row to the 'faults' table

    /* Prepared statements for removing stuff */
    private static PreparedStatement remComp;  // removes a row from the 'components' table
    private static PreparedStatement remServlet; //removes a row from the 'servlets' table

    /** 
     * Connects to the database and initialize local prepared statements.  
     *
     **/

    public DBUtil()
    {
        if (! EnableAFPI)
           return;

	/* Initialize global connection to database */
	safeInit();

	/* Initialize this instance's prepared statements */
	try {
	    addComp  = db.prepareStatement(
		"INSERT INTO components(UID,Name,Type,Parent,Methods) VALUES(?,?,?,?,?)");
	    remComp  = db.prepareStatement(
		"DELETE FROM components WHERE UID=?");
	    addFault = db.prepareStatement(
		"INSERT INTO faults(Component,Method,Occurred,Description,Source,Notes,RebootID,MonitorID) VALUES(?,?,?,?,?,?,?,?)");
	}
	catch( Exception e ) { 
	    handleException(e); 
	}
    }
    

    /** 
     * Establishes a global connection to the database, if not already
     * established, and initializes the required tables in the
     * database.
     *
     **/
    synchronized private static void safeInit ()
    {
        /* Only the first instance of DBUtil performs this initialization */
	if (db != null) {
	    return;
	}
	
	/* Set up database */
	try { 
	    Class.forName("com.mysql.jdbc.Driver").newInstance(); 
	    String url = "jdbc:mysql://localhost/afpi?user=afpi&password=afpi";
	    db = DriverManager.getConnection(url);
	    initTables();
	}
	catch( Exception e ) { 
	    handleException(e); 
	}
    }


    /** 
     * Stores information about a component in the 'components' table.
     *
     * @param  compName        component to add to the DB
     * @throws SQLException    if the database update fails
     *
     **/
    synchronized static public void storeComponent( Component comp )
    {
        if (! EnableAFPI)
           return;

	try {
	    addComp.setString( 1, comp.UID );    // UID column
	    addComp.setString( 2, comp.name );   // NAME column
	    addComp.setString( 3, comp.type );   // TYPE column
	    if ( comp.parentUID != null ) {         // PARENT column
		addComp.setString( 4, comp.parentUID );
	    } else {
		addComp.setString( 4, "" );
	    }
	    addComp.setString( 5, comp.toDB() ); // METHODS column
	    
	    try {
		addComp.executeUpdate();
	    }
	    catch( SQLException sqle ) {
		removeComponent( comp.UID );
		addComp.executeUpdate();
	    }
	}
	catch( SQLException e ) {
	    e.printStackTrace();
	}
    }


    /** 
     * Removes a component from the 'components' table.
     *
     * @param  compName        name of the component to remove
     * @throws SQLException    if the database update fails
     *
     **/
    synchronized static public void removeComponent( String compName )
    {
        if (! EnableAFPI)
           return;

	try {
	    remComp.setString( 1, compName );
	    remComp.executeUpdate();
	}
	catch( SQLException e ) {
	    e.printStackTrace();
	}
    }


    /** 
     * Execute a query and return the result set.
     *
     * @param  query      query to execute
     *
     **/
    static public ResultSet executeQuery( String query )
    {
        if (! EnableAFPI)
           return null;

	ResultSet rs=null;
	Statement stmt;
	try {
	    stmt = db.createStatement();
	    rs = stmt.executeQuery( query );
	}
	catch( Exception e ) {
	    handleException(e);
	}
	finally { 
	    /* stmt.close() */
	}

	return rs;
    }


    /** 
     * Handles an exception by printing out information about it:
     * detailed SQL info if it's a <code>SQLException</code>, or just
     * a stack trace if it's any other kind of exception.
     *
     * @param  ex          the <code>Exception</code> to handle
     *
     * */
    private static void handleException( Exception ex )
    {
	ex.printStackTrace();

	if ( ex instanceof SQLException )
	{
	    SQLException sqlEx = (SQLException) ex;
	    System.err.println("==> SQL Exception: ");
	    while (sqlEx != null) 
	    {
		System.out.println("Message:   " + sqlEx.getMessage ());
		System.out.println("SQLState:  " + sqlEx.getSQLState ());
		System.out.println("ErrorCode: " + sqlEx.getErrorCode ());
		sqlEx = sqlEx.getNextException();
		System.out.println("");
	    }
	}
    }


    /** 
     * Adds a new fault to the database.
     *
     * @param  compName   UID of the component that failed
     * @param  opName     operation it was performing when it failed
     * @param  fault      observed fault
     *
     * */
    public void addFault( String compName, String opName, Throwable fault )
    {
	// temporarily disable this function
	if (1 > 0)
	    return;

	try {
	    /* COMPONENT column */
	    addFault.setString(1, compName);

	    /* METHOD column */
	    addFault.setString(2, opName);

	    /* OCCURRED column */
	    addFault.setString(3, String.valueOf(System.currentTimeMillis()));

	    /* DESCRIPTION column  */
	    addFault.setString(4, fault.getMessage());

	    /* SOURCE column */
	    if ( fault.getCause() != null ) {
		addFault.setString(5, fault.getCause().toString());
	    } else {
		addFault.setString(5, "NULL");
	    }

	    /* NOTES column: dump the chain of Throwables into the DB */
	    String notes="";
	    while ( fault != null )
	    {
		/* dump this fault's stack trace */
		StackTraceElement[] stack = fault.getStackTrace();
		for (int i=0 ; i < stack.length ; i++) {
		    notes += "\n" + stack[i];
		}

		/* find this fault's cause */
		fault = fault.getCause();
		if ( fault != null ) {
		    notes += "\n\nCAUSED BY\n";
		}
	    }
	    addFault.setString(6, notes);
	    
            /* RebootID column: default is 0 (no reboot yet) */
            addFault.setString(7, "0");
            
            /* MonitorID column: this is the exeception monitor, ID is
             * "ExceptionMon" */
            addFault.setString(8, "ExceptionMon");

	    /* perform the insert */
	    addFault.executeUpdate();
	}
	catch (Exception e) {
	    handleException(e);
	}
    }


    /** 
     * Initializes the tables used by AFPI.
     *
     * @throws Exception    If a non-SQL-related exception occurred
     *
     **/
    private static void initTables()
    {
	/* Table for all components (services, MBeans, EJBs, servlets, etc.) */
	createTable("components",
		    "CREATE TABLE components ("                     +
		              "UID     VARCHAR(255) PRIMARY KEY," +    
		              "Name    VARCHAR(255),"             +
		              "Type    VARCHAR(255),"             +
		              "Parent  VARCHAR(255),"             +
                              "Methods TEXT)");

	/* Table for reported faults; has historical persistence */
	createTable("faults", 
		    "CREATE TABLE faults (" +
		              "Component VARCHAR(255),"            +    
		              "Method VARCHAR(255),"               +       
		              "Occurred VARCHAR(255) PRIMARY KEY," +       
		              "Recorded TIMESTAMP(14),"            +
                              "Description VARCHAR(255),"          +
		              "Source VARCHAR(255),"               +
	                      "Notes TEXT,"                        +
                              "RebootID VARCHAR(255),"             +
                              "MonitorID VARCHAR(255) )");
    }


    /** 
     * Initializes a table with the given name and given SQL code;
     * overwrites any existing table with that name.
     *
     * @param  tabName       Name of the table to create
     * @param  tabSql        SQL that will create the table
     * @throws Exception     If something went wrong :-)
     *
     **/
    private static void createTable( String tabName, String tabSQL )
    {
	Statement stmt=null;

	try {
	    stmt = db.createStatement(); 
	    
	    /* Get rid of a potentially stale table */
	    dropTable(stmt, tabName); 

	    /* Create the table anew */
	    stmt.executeUpdate(tabSQL);
	    stmt.close(); 
	}
	catch( Exception e ) {
	    handleException(e);
	}
    }


    /** 
     * Drops a table from the database.
     *
     * @param  stmt            the <code>Statement</code> to use for dropping
     * @param  tabName         name of the table to drop
     * @throws SQLException    If an exception occurred other than "table not found"
     *
     * */
    private static void dropTable( Statement stmt, String tabName )
	throws SQLException
    {
	try {
	    stmt.executeUpdate("DROP TABLE " + tabName); 
	}
	catch (SQLException e) {
	    if (e.getErrorCode() != 1051) // code for "table not found"
		throw e;
	}
    }
}
