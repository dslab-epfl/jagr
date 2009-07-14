/*
 * $Id: RecordResponseInterceptor.java,v 1.6 2004/08/26 04:23:54 candea Exp $
 */

package roc.loadgen.interceptors;

import roc.loadgen.*;
import roc.loadgen.http.*;
import org.apache.log4j.Logger;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;

/**
 * Record responses to disk.
 *
 * @version <tt>$Revision: 1.6 $</tt>
 * @author <a href="mailto:skawamo@stanford.edu">Shinichi Kawamoto</a>
 * @author <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class RecordResponseInterceptor extends RequestInterceptor {

    private static Logger log = Logger.getLogger( "interceptors.RecordResponseInterceptor" );

    public static final String ARG_ONLY_FAILED = "onlyfailed";

    Arg[] argDefinitions = {
	new Arg( ARG_ONLY_FAILED,
		 "if true, record only failed responses; if false, record everything",
		 Arg.ARG_BOOLEAN,
		 false,
		 "false" )
    };

    /*
        log files will be created on ./log/2004..../ 
    */
    private static String currentDirStr = System.getProperty("user.dir");
    private static String logDirStr = currentDirStr + "/log";
    private static String baseDirStr = logDirStr + "/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
    private static String indexStr = "index.html";

    // The common log file into which we put all errors
    private static FileOutputStream allErrors = null;

    private static int sequenceNo  = 1;
    private String dirStr   = null;

    private static boolean failuresOnly = false;

    public RecordResponseInterceptor() 
	throws FileNotFoundException
    {
	/*
	 * create directories
	 */
	File logDir = new File(logDirStr);
 	if ( ! logDir.exists() ) {
	    logDir.mkdir();
	}
	File baseDir = new File(baseDirStr);
	if ( ! baseDir.exists() ) {
	    baseDir.mkdir();
	}
	if( allErrors == null )
	    allErrors = new FileOutputStream( baseDirStr + "/all_errors.html" );
    }
    
    public void start() throws roc.loadgen.InitializationException 
    {
	Object obj = args.get( ARG_ONLY_FAILED );
	if( obj != null )
	    failuresOnly = ((Boolean) obj).booleanValue();
    }

    public void stop()
    {
	try {
	    allErrors.close();
	}
	catch( IOException ioe ) {
	    ioe.printStackTrace();
	}
    }

    /**
     * @see roc.loadgen.RequestInterceptor#getArguments()
     */
    public Arg[] getArguments() {
        return argDefinitions;
    }

    public Response invoke(Request req)
        throws AbortRequestException {

	HttpResponse resp = (HttpResponse)invokeNext(req);

	if( failuresOnly  &&  resp.isOK() )
	    return resp; // skip the logging

	// check if initialization is needed; if yes, do it
	int id = req.getEngineId();
	if ( dirStr == null ) {
	    dirStr = baseDirStr+"/"+id;
	    File dir = new File(dirStr);
	    if (!dir.exists()) {
		dir.mkdir();
	    }
	    try {
		FileOutputStream fos = new FileOutputStream(dirStr+"/"+indexStr);
		String message = "<H1>Activity of #"+id+" thread</H1>\n<ol type=\"1\">\n";
		fos.write(message.getBytes());
		fos.close();
	    } catch (Exception e) {
		throw new AbortRequestException(e);
	    }
	}

	String respHTML = resp.getRespStr();
	if( respHTML == null )
	    respHTML = "<null>";

	/*
	 *  write response to file
	 */
	try {
	    String fileName = dirStr+"/"+sequenceNo+".html";
	    FileOutputStream resultFOS = new FileOutputStream(fileName);
	    resultFOS.write( respHTML.getBytes() );
	    resultFOS.close();

	    HttpResponse refResp = resp.getReferenceResponse();
	    if( refResp != null && refResp.isOK() )
	    {
		String refFileName = dirStr + "/" + sequenceNo + "-ref.html";
		FileOutputStream refFOS = new FileOutputStream( refFileName );
		String res = refResp.getRespStr();
		refFOS.write( res.getBytes() );
		refFOS.close();
	    }

	} catch (Exception e) {
	    throw new AbortRequestException(e);
	}

	/*
	 * add link of this response to index.html
	 */ 
	String link = "<li>"+ "<a href=\""+sequenceNo+".html\">"
	    +req.toString()+"</a></li>\n";

	try {
	    FileOutputStream indexFOS = new FileOutputStream(dirStr+"/"+indexStr,true);
	    indexFOS.write(link.getBytes());
	    indexFOS.close();
	    sequenceNo++;
	} catch (Exception e) {
	    throw new AbortRequestException(e);
	}

	// write the error HTML out to the "all errors" file
	try {
	    allErrors.write( ("<p><table border=10 width=100%><tr><td><font color=blue>Thread #" + id).getBytes() );
	    allErrors.write( ("</font>&nbsp; got &nbsp;<font color=red>" + resp ).getBytes() );

	    // find boundaries of the text we're interested in
	    if( respHTML == null )
	    {
		allErrors.write( ("</font></td></tr><tr><td>&lt;null&gt;").getBytes() );
		allErrors.write( "</td></tr></table></p>\n\n".getBytes() );
	    }
	    else if( respHTML.length() == 0 )
	    {
		allErrors.write( ("</font></td></tr><tr><td>&lt;empty document&gt;").getBytes() );
		allErrors.write( "</td></tr></table></p>\n\n".getBytes() );
	    }
	    else // respHTML != null )
	    {
		int end = respHTML.lastIndexOf("<hr>") - 1;
		if( end < 0 )
		    end = respHTML.length() - 1;

		String sub = respHTML.substring( 0, end );
		int start = 4 + sub.lastIndexOf("<hr>");
		if( start < 0 )
		    start = 0;
		
		allErrors.write( ("</font></td></tr><tr><td>" + respHTML.substring(start,end) ).getBytes() );
	    }

	    allErrors.write( "</td></tr></table></p>\n\n".getBytes() );

	} catch( Exception e ) {
	    throw new AbortRequestException( e );
	}

        return (Response)resp;
    }

}
