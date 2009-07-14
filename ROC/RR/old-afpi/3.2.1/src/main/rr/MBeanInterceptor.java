package rr;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import javax.ejb.*;
import javax.management.*;

/** MBean interceptor
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.12 $
 */

public class MBeanInterceptor
    extends rr.Interceptor
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

