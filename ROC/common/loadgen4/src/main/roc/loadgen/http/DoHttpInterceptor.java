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

public class DoHttpInterceptor extends RequestInterceptor {

	public static final String DEFAULT_TCP_TIMEOUT = "30000";

	Arg[] args = {};

        static {
	        // initialize some global TCP and HTTP settings
                HttpURLConnection.setFollowRedirects( false );
		System.getProperties().setProperty( "sun.net.client.defaultReadTimeout",
			DEFAULT_TCP_TIMEOUT );
        }
	
	/**
	 * @see roc.loadgen.RequestInterceptor#getArguments()
	 */
	public Arg[] getArguments() {
		return args;
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
		httpConn.connect();

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

	HttpResponse getResponse(HttpURLConnection httpConn) throws IOException {
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

		return new HttpResponse(
			headers,
			respCode,
			contentType,
			respBuf,
			respStr);
	}

	Response createExceptionResponse(Exception ex) {
		return new HttpResponse(ex);
	}

	public Response invoke(Request req) {

		HttpRequest httpReq = (HttpRequest)req;
		Response resp;

		try {
			HttpURLConnection httpConn = initConnection(httpReq);
			startConnection(httpConn, httpReq);
			resp = getResponse(httpConn);
		} catch (Exception ex) {
		    engine.logError( "got exception: " + ex.getMessage() );
		    ex.printStackTrace();
			resp = createExceptionResponse(ex);
		}

		return resp;
	}

}
