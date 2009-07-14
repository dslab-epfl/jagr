/**
 * $Id: Information.java,v 1.3 2004/05/09 02:52:32 candea Exp $
 * 
 */
package roc.rr.afpi.util;

import java.io.*;
import javax.management.*;
import org.jboss.logging.Logger;

public class Information
{
    private static Logger log = Logger.getLogger( Information.class );
    private static MBeanServer server=null;

    /**
     * Constructor
     *
     */
    public Information( MBeanServer srv )
    {
	this.server = srv;
    }

    /**
     * returns complete jar file name (ex: "file:/home/~fjk/.../SB_Auth.jar")
     * if there is no correspondent value, returns null.
     *
     * @param ear_name ear package name, in which this jar file locates.
     * @param jar_name JAR file name which you want to find the complete 
     *                 file name corresponding to (can be WAR file too).
     * @param server   MBeanServer instance for this pacakage.
     *
     * @return complete file name corresponding to specified jar file.
     */
    public static String getCompleteFileName(String ear_name, String jar_name) throws Exception{
	
	String components = null;
	String ret = null;
	try{
	    ObjectName InfoSvc = new ObjectName("RR:service=Information");
            components = (String) server.invoke(InfoSvc, "listDeployedComponents", null, null);
	} catch (Exception e) {
	    throw e;
	}

	BufferedReader reader = new BufferedReader(new StringReader(components));
	String line;
	while((line = reader.readLine()) != null){
	    if(line.indexOf(ear_name) != -1 && line.indexOf("<b>" + jar_name + "</b>") != -1){
		int begin_index = line.indexOf("[")+1;
		int end_index = line.indexOf("]");
		ret = line.substring(begin_index, end_index);
		break;
	    }
	}
	return ret;
    }

    /**
     * returns complete ear package file name (ex: "file:/home/~fjk/.../rubis.ear")
     *
     * @param ear_name ear package name which you want to find the complete file name corresponding to.
     * @param server   MBeanServer instance for this pacakage.
     *
     * @return complete file name corresponding to specified ear file.
     */
    public static String getCompleteFileName(String ear_name) throws Exception{

	String serverHomeDir = null;
	String protocol = "file:";
	String ret = null;
	try {
	    ObjectName InfoSvc = new ObjectName("jboss.system:type=ServerConfig");
	    
            serverHomeDir = (String) ((File)server.getAttribute(InfoSvc, "ServerHomeDir")).getAbsolutePath();
	    serverHomeDir = protocol.concat(serverHomeDir);
	} catch (Exception e) {
	    throw e;
	}
	
	ret = serverHomeDir.concat("/deploy/"+ear_name);
	return ret;
    }

    public static void stackToLog( Exception e, Logger log )
    {
	StackTraceElement[] els = e.getStackTrace();
	log.error( e );
	for ( int i=0 ; i < els.length ; i++)
	    log.error( "    at " + els[i] );
    }
}

