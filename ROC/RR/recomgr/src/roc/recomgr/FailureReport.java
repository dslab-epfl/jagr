/*
 * $Id: FailureReport.java,v 1.5 2004/08/27 22:22:41 candea Exp $
 */

package roc.recomgr;

import java.io.*;
import java.util.*;

public class FailureReport implements Serializable 
{
    String server;
    String path;
    int    code;

    public FailureReport( String server, String path, int code )
    {
	this.server = server;
	this.path = path;
	this.code = code;
    }

    public String getServer() { return server; }
    public String getPath() { return path; }
    public int    getCode() { return code; }

    public String toString() 
    {
	return "[code=" + code + " server=" + server + ":8080, path=" + path + "]";
    }

    public static FailureReport fromBytes( byte[] buf, int offset, int length ) 
    {
	FailureReport ret=null;
	try {
	    ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( buf, offset, length ));
	    ret = (FailureReport)ois.readObject();
	}
	catch( Exception e ) {
	    System.err.println( "SHOULD NEVER HAPPEN" );
	    e.printStackTrace();
	}
	return ret;
    }

    public byte[] getBytes() 
    {
	byte[] ret = null;
	try {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( this );
	    oos.flush();
	    ret = baos.toByteArray();
	}
	catch( Exception e ) 
	{
	    System.err.println( "SHOULD NEVER HAPPEN" );
	    e.printStackTrace();
	}
	return ret;
    }
}
