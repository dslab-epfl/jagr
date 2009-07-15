package roc.loadgen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map;

import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

import org.apache.log4j.Logger;

public class DoHttpInterceptor extends RequestInterceptor {

    private static Logger log = Logger.getLogger( "interceptors.DoHttpInterceptor" );

    public static final String DEFAULT_TCP_TIMEOUT = "300000";

    static {
	// initialize some global TCP and HTTP settings
	HttpURLConnection.setFollowRedirects( false );

	// Beware of bug #4772077 in Java 1.4.1 -- If the
	// server times out, Java will transparently retry the
	// HTTP request once; if it fails again, it will throw
	// the exception up to the application.
	//
	// System.getProperties().setProperty( "sun.net.client.defaultReadTimeout", DEFAULT_TCP_TIMEOUT );
	// System.getProperties().setProperty( "sun.net.client.defaultConnectTimeout", DEFAULT_TCP_TIMEOUT );
    }
	
    public DoHttpInterceptor() {
    }


    HttpURLConnection initConnection(HttpRequest req) throws IOException {

	HttpURLConnection httpConn =
	    (HttpURLConnection) req.getUrl().openConnection();

	if (req.getPostData() != null ) {
	    httpConn.setRequestMethod("POST");
	    httpConn.setDoOutput(true);
	}

	setHeaders(httpConn, req.getHeaders());

	return httpConn;
    }

    void setHeaders(HttpURLConnection httpConn, Map headers) {
	if (headers == null)
	    return;

	Iterator iter = headers.entrySet().iterator();
	while (iter.hasNext()) {
	    Map.Entry entry = (Map.Entry) iter.next();
	    httpConn.setRequestProperty(
					(String) entry.getKey(),
					(String) entry.getValue());
	}
    }

    void startConnection(HttpURLConnection httpConn, HttpRequest req) throws IOException {
	    
	try {
	    httpConn.connect();
	}
	catch( IOException e )
	    {
		log.error( "Could not connect to " + req.getServer() + ":" + req.getPort() );
		throw e;
	    }

	if (req.getPostData() != null) {
	    PrintWriter pw = new PrintWriter(httpConn.getOutputStream());
	    pw.print(req.getPostData());
	    pw.flush();
	}
    }

    public static byte[] LoadByteArray(InputStream is) throws IOException {
	ByteArrayOutputStream os = new ByteArrayOutputStream();

	byte[] buf = new byte[4096];
	int c = 0;
	int t = 0;

	do {
	    c = is.read(buf);

	    if (c > 0) {
		os.write(buf, 0, c);
		t += c;
	    }
	} while (c != -1);

	is.close();
	os.flush();
	os.close();

	return os.toByteArray();
    }

    HttpResponse getResponse( HttpURLConnection httpConn, HttpResponse response ) 
	throws IOException 
    {
	Map headers = httpConn.getHeaderFields();
	int respCode = httpConn.getResponseCode();

	String contentType = null;
	byte[] respBuf = null;

	try {
	    contentType = httpConn.getContentType();
	    InputStream is = httpConn.getInputStream();
	    respBuf = LoadByteArray(is);
	}
	catch( IOException ignore ) {
	}

	if( respBuf == null ) {
	    respBuf = new byte[0];
	}

	String respStr = null;
	if (contentType != null && contentType.startsWith("text")) {
	    if( respBuf.length > 0 ) {
		respStr = new String(respBuf);
	    }
	    else {
		respStr = "";
	    }
	}

	response.setHeaders( headers );
	response.setRespCode( respCode );
	response.setContentType( contentType );
	response.setRespBuf( respBuf );
	response.setRespStr( respStr );
	return response;
    }

    public Response invoke(Request req) 
    {
	log.debug( "invoke: " + req );
	
	HttpRequest httpReq = (HttpRequest)req;
	HttpResponse httpResp = new HttpResponse( httpReq );
	
	try {
	    HttpURLConnection httpConn = initConnection( httpReq );
	    startConnection( httpConn, httpReq );
	    getResponse( httpConn, httpResp );
	} 
	catch (Exception ex) 
	    {
		log.error( "got exception: " + ex.getMessage() );
		ex.printStackTrace();
		httpResp.setThrowable( ex );
	    }
	finally
	    {
	    
	    }
	
	return httpResp;
    }


    //---------------------------------------------------------------------------

    public Arg[] getArguments() {
	return args;
    }
	
    private static Arg[] args = {};
}
