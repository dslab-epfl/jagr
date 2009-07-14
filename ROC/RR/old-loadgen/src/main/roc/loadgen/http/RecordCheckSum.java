/*
 * Created on Apr 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package roc.loadgen.http;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import roc.loadgen.AbortRequestException;
import roc.loadgen.InitializationException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

import org.apache.log4j.Logger;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RecordCheckSum extends RequestInterceptor {

    public static final String ARG_OUTPUTFILE = "checksumfile";
    private static Logger log = Logger.getLogger( "RecordChecksum" );

    CheckSumHelper helper;

    Arg[] argDefinitions = {
	new Arg( ARG_OUTPUTFILE, 
		 "where to save all the checksum info that comes out",
		 Arg.ARG_STRING,
		 true,
		 null )
    };

    static PrintWriter out;

    public void initCheckSumWriter() throws IOException {
	log.debug("RecordCheckSum.java::initchecksumwriter");
	synchronized( RecordCheckSum.class ) {
	    if( out == null ) {
		String outputfile = (String)args.get(ARG_OUTPUTFILE);
		log.info("RecordCheckSum::initchecksumwriter: " + outputfile );
		out = new PrintWriter( new FileWriter( new File( outputfile )));
	    }
	}
    }

    public void writeCheckSum( String id, String md5 ) {
	synchronized( RecordCheckSum.class ) {
	    out.println( id + " " + md5 );
	}
    }

    public void start() throws InitializationException {

	try {
	    initCheckSumWriter();
	}
	catch( IOException e ) {
	    throw new InitializationException(e);
	}

	helper = new CheckSumHelper();
    }
    
    public Arg[] getArguments() {
        return argDefinitions;
    }
    
    /* (non-Javadoc)
     * @see roc.loadgen.RequestInterceptor#invoke(roc.loadgen.Request)
     */
    public Response invoke(Request req)
        throws AbortRequestException {

        HttpResponse resp = (HttpResponse)invokeNext(req);
        byte[] buf = resp.getRespBuf();
        
	CheckSumHelper.CheckSumResult checksum = 
	    helper.calculateCheckSum(engine,(HttpRequest)req,resp);

        log.info(
                "CHECKSUM: " + resp.toString() + " : " +
                checksum.digest );

	writeCheckSum(((HttpRequest)req).getStringID(),checksum.digest );
	out.flush();

	List ppreqlist = (List)((HttpResponse)resp).getHeaders().get("PP-Request");
	String pprequest = 
	    (ppreqlist!=null && ppreqlist.size()>0)?
	    (String)ppreqlist.get(0):"NOREQUESTID";
	engine.logData(
		       new String[] {
			   "checksum-digest",
			   ((HttpRequest)req).getStringID(),
			   pprequest},
		       ((HttpRequest)req).url.getFile(),
		       checksum.digestedBuf );


        return resp;
    }

}
