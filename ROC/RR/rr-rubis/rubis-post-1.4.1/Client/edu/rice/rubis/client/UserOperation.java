/* $Id: UserOperation.java,v 1.1 2004/05/18 21:18:01 candea Exp $ */

package edu.rice.rubis.client;

import java.util.*;
import java.io.*;
import java.net.*;

class UserOperation
{
    private static PrintStream data = null;  // this is where we output the data

    private static final int UNDETERMINED = 0;
    private static final int SUCCEEDED    = 1;
    private static final int FAILED       = 2;
    private              int status = UNDETERMINED;

    private Date   Tbegin;     // begin time for this operation
    private Date   Tend;       // end time for this operation
    private String sessionId;  // identifier for this operation's session
    private URL    url;        // the URL corresponding to this operation


    /**
     * Constructor gets called whenever we start a new user operation.
     *
     * @param sessionId String identifying this operation's session
     * @param url       URL for the servlet corresponding to this operation
     *
     */
    public UserOperation ( String sessionId, URL url )
    {
	assert data != null;

	this.Tbegin    = new Date();
	this.Tend      = null;
	this.sessionId = sessionId;
	this.url       = url;
	this.status    = UNDETERMINED;
    }


    /**
     * Operation has succeeded and should be written out to file.
     *
     */
    public void succeeded()
    {
	assert  Tbegin!=null;
	assert  Tend==null           : (Tbegin + "\n" + Tend);
	assert  status==UNDETERMINED : getStatus();

	Tend   = new Date();
	status = SUCCEEDED;
	dump();
    }


    /**
     * Operation has failed and should be written out to file.
     *
     */
    public void failed()
    {
	assert  Tbegin!=null;
	assert  Tend==null           : (Tbegin + "\n" + Tend);
	assert  status==UNDETERMINED : getStatus();

	Tend   = new Date();
	status = FAILED;
	dump();
    }


    /**
     * Get the current status of the operation.
     *
     * @return String representing the status
     *
     */
    private String getStatus()
    {
	if      (status == UNDETERMINED)  return "UNDETERMINED";
	else if (status == SUCCEEDED   )  return "OK ";
	else if (status == FAILED      )  return "BAD";
	else                              return "ILLEGAL";
    }


    /**
     * Dump out a line representing this operation's information.
     *
     */
    private void dump()
    {
    	data.print( sessionId + "\t" + Tbegin.getTime() + "\t" + Tend.getTime() + "\t" + getStatus() + "\t" + url + "\n" );
    }

    /**
     * Initialize the output file for writing operation data.
     *
     * @param fileName String representing name of the file
     *
     */
    public static void openOutput( String fileName )
	throws IOException
    {
	assert data==null;
	data = new PrintStream( new FileOutputStream( fileName ) );
	data.println( "#\n# Columns are:\n#   Thread_Session_UserID \n#     Started at \n#       Ended at \n#         Status \n#           URL \n#" );
    }


    /**
     * Close down the output file; no more writes after this point.
     *
     * @param output the PrintWriter we write to
     *
     */
    public static void closeOutput ()
    {
	data.close();
	data = null;
    }
}
