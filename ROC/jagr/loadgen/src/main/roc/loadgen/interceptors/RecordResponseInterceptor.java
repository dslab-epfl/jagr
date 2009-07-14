/*
 * $Id: RecordResponseInterceptor.java,v 1.4 2004/07/29 02:05:16 candea Exp $
 */

package roc.loadgen.interceptors;

import roc.loadgen.AbortRequestException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;
import roc.loadgen.http.HttpResponse;
import org.apache.log4j.Logger;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Record responses to disk.
 *
 * @version <tt>$Revision: 1.4 $</tt>
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
    private static String indexStr = "Index.html";

    private int sequenceNo  = 1;
    private String dirStr   = null;

    private static boolean failuresOnly = false;

    public RecordResponseInterceptor() {
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

	
    }
    
    public void start() throws roc.loadgen.InitializationException 
    {
	Object obj = args.get( ARG_ONLY_FAILED );
	if( obj != null )
	    failuresOnly = ((Boolean) obj).booleanValue();
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

	byte[] results = resp.getRespBuf();

	/*
	 *  write response to file
	 */
	String fileName = dirStr+"/"+sequenceNo+".html";
	try {
	    FileOutputStream resultFOS = new FileOutputStream(fileName);
	    resultFOS.write(results);
	    resultFOS.close();
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

        return (Response)resp;
    }

}
