package roc.rr.afpi;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import roc.rr.*;

/** MBean interceptor
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.1 $
 */

public class MBeanInterceptor
    extends roc.rr.afpi.Interceptor
{
    /** 
     * Connects to the database and initializes local prepared statements.  
     *
     **/
    public MBeanInterceptor()
    {
	super();
    }
}

