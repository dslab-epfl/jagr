//
// $Id: RMFIUtil.java,v 1.5 2003/12/12 21:12:18 steveyz Exp $
//

package roc.rr.rm;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

// provides the doMicroReboot and scheduleFault services

public class RMFIUtil
{
    // options
    protected int invoke_max_retries = 3;
    protected String jmx_console_server = "localhost";
    protected int jmx_console_port = 8080;
    protected String jmx_console_invoke_url = "/jmx-console/HtmlAdaptor";

    protected int rb_methodIndex = 1;
    protected int fi_methodIndex = 6;

    protected int loadgen_port = 5623; 

    protected boolean trace = true; // output messages or not


    public RMFIUtil()
    {
    }

    public RMFIUtil(int port)
    {
        loadgen_port = port;
    }

    public RMFIUtil(int retries, String server, int port, String url)
    {
        invoke_max_retries = retries;
        jmx_console_server = server;
        jmx_console_port = port;
        jmx_console_invoke_url = url;
    }

    public void enableTrace()
    {
        trace = true;
    }
    
    public void disableTrace()
    {
        trace = false;
    }
    
    public boolean isTraceEnabled()
    {
        return trace;
    }

    protected void traceMsg(String s)
    {
        if(trace)
        {
            System.out.print(s);
        }
    }
    
    protected void sendLoadGenMsg(boolean start) throws Exception
    {
        try
        {
            byte[] buf = new byte[1];
            if(start)
            {
                buf[0] = (byte)'S'; /* start */
            }
            else
            {
                buf[0] = (byte)'T'; /* stop */
            }
            
            DatagramSocket s = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, 
                                                       InetAddress.getLocalHost(),
                                                       loadgen_port);
            s.send(packet);
            if(start)
                traceMsg("START LOAD MESSAGE SENT TO LOADGEN!\n");
            else
                traceMsg("STOP LOAD MESSAGE SENT TO LOADGEN!\n");
        }
        catch(Exception e)
        {
            if(start)
                System.err.println("FAILED TO SEND START LOAD MESSAGE!");
            else
                System.err.println("FAILED TO SEND STOP LOAD MESSAGE!");
            throw e;
        }
    }

    /**
     * Tells the load generator to start applying load to the system.
     *
     * @throw Exception (change as needed)
     *
     */
    public void startLoad () throws Exception
    {
	sendLoadGenMsg(true);
    }
    
    /**
     * Tells the load generator to stop applying load to the system.
     *
     * @throw Exception (change as needed)
     *
     */
    public void stopLoad () throws Exception
    {
	sendLoadGenMsg(false);
    }
    
    // private utility classes
    public class NumRetriesExceededException extends Exception
    {
        public NumRetriesExceededException() {}
    }

    public class HTMLErrorException extends Exception
    {
        public HTMLErrorException(String s) {
            super(s);
        }
    }

    public class FIUtilMethodFailureException extends Exception
    {
        public FIUtilMethodFailureException(String s) {
            super(s);
        }

        public FIUtilMethodFailureException(String s, Throwable cause) {
            super(s, cause);
        }        
    }

    // internal functions
    protected void readResponse(String htmlContent) 
        throws FIUtilMethodFailureException, HTMLErrorException
    {
        if(htmlContent.indexOf("<span class='OpResult'>") != -1)
        {
            htmlContent = (htmlContent.substring(htmlContent.indexOf("<span class='OpResult'>") +
                                                 "<span class='OpResult'>".length())).trim();
            if(htmlContent.indexOf("<pre>") != -1)
            {
                htmlContent = htmlContent.substring(htmlContent.indexOf("<pre>") + "<pre>".length());
                if(htmlContent.indexOf("</pre>") != -1)
                {
                    htmlContent = (htmlContent.substring(0, htmlContent.indexOf("</pre>") - 1)).trim();
                    if(htmlContent.indexOf("FAILED") != -1)
                    {
                        throw new FIUtilMethodFailureException(htmlContent);
                    }
                    else
                    {
                        traceMsg("  Response from method: \"" + htmlContent + "\"\n");
                        return;
                    }
                }
                else
                {
                    throw new HTMLErrorException(htmlContent);
                } 
            }
            else
            {
                throw new HTMLErrorException(htmlContent);
            }  
        }
        else // error parsing content
        {
            throw new HTMLErrorException(htmlContent);
        }
    }

    protected String respToStr(HttpURLConnection httpConn) throws IOException
    {
        String htmlContent = null;
        int len = httpConn.getContentLength();
        traceMsg("Response Length = " + ((len == -1) ? "unknown":(Integer.toString(len))) + "\n");

        if(len != 0)
        {
            // parse the message
            len = 0;
            int MAXBUF = 4096; // we only need first little bit, don't worry
                               // about long pages
            byte[] buf = new byte[MAXBUF];
            InputStream is = httpConn.getInputStream();

            for(int nbytes = 0; nbytes != -1; len += ((nbytes<0)?0:nbytes))
            {
                nbytes = is.read(buf, len, MAXBUF-len);
                if(len == MAXBUF)
                {
                    break;
                }
            }

            htmlContent = new String(buf, 0, len);
        }

        return htmlContent;
    }

    public String generateCookieHeader( Map cookieJar ) 
    {
	Iterator iter = cookieJar.keySet().iterator();
	String cookieLine = "";
	while( iter.hasNext() ) 
        {
	    String key = (String)iter.next();
	    String val = (String)cookieJar.get( key );
	    cookieLine += key + "=" + val;
	    if( iter.hasNext() ) 
		cookieLine += "; ";
	}
	return cookieLine;
    }

    public void readCookieHeaders( HttpURLConnection httpConn, Map cookieJar) 
    {
	Map headers = httpConn.getHeaderFields();

	// search for cookie fields
	List cookies = (List)headers.get( "Set-Cookie" );
	if( cookies != null ) 
        {
	    Iterator iter = cookies.iterator();
	    while( iter.hasNext() ) 
            {
		String s = (String)iter.next();
		
		int eqlIdx = s.indexOf( "=" );
		int semiIdx = s.indexOf( ";" );
		String key = s.substring( 0, eqlIdx );
		String val = s.substring( eqlIdx + 1, semiIdx );
		cookieJar.put( key, val );
	    }
	}
    }

    // process url request 
    protected HttpURLConnection doUrlRequest(URL url, Map headers, Map cookieJar, String postData)
        throws NumRetriesExceededException
    {
        int retries = 0;
        HttpURLConnection httpConn = null;

        traceMsg("\nConnecting to '" + url.toString() + "'\n");

        while(retries++ < invoke_max_retries)
        {
            try
            {
                traceMsg("Attempt #" + retries + " ...");
                httpConn = (HttpURLConnection)url.openConnection();
                
                if(postData != null)
                {
                    httpConn.setRequestMethod("POST");
                    httpConn.setDoOutput(true);
                }

                Iterator iter = headers.entrySet().iterator();
                while(iter.hasNext())
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    httpConn.setRequestProperty((String)entry.getKey(),
                                                (String)entry.getValue());
                }
                
                // set cookie
                if(!cookieJar.isEmpty())
                {
                    httpConn.setRequestProperty("Cookie", generateCookieHeader(cookieJar));
                }
                
                // set content length (if POST)
                if(postData != null)
                {
                    httpConn.setRequestProperty("Content-Type", 
                                                "application/x-www-form-urlencoded");
                    httpConn.setRequestProperty("Content-Length", 
                                                Integer.toString(postData.length()));
                }                                                                   
                                                
                httpConn.connect();
                if(postData != null)
                {
                    PrintWriter pw = new PrintWriter(httpConn.getOutputStream());
                    pw.print(postData);
                    pw.flush();
                }
                
                if(httpConn.getResponseCode() == 200)
                {
                    // read and store cookies
                    readCookieHeaders(httpConn, cookieJar);
                    traceMsg("SUCCEEDED!\n");
                    return httpConn; // ready to process response
                }
                else
                {
                    traceMsg("FAILED (Code = " + httpConn.getResponseCode() + 
                             " " + httpConn.getResponseMessage() + ")\n");
                    httpConn.disconnect();
                }
            }
            catch(Exception e)
            {
                httpConn.disconnect();
                traceMsg("FAILED\n");
                System.err.print(e.toString());
            }
        }

        throw new NumRetriesExceededException();
    }
    
    public void scheduleFault(String component, String fault)
        throws FIUtilMethodFailureException
    {
        /* fault can either be "error" or "exception" */

        URL invoke_url = null;
        try
        {
            // inject a fault into a certain component via the JMX-console
            invoke_url = new URL("http", jmx_console_server, jmx_console_port, 
                                 jmx_console_invoke_url);
        }
        catch(MalformedURLException e)
        {
            throw new FIUtilMethodFailureException("Malformed URL!", e);
        }        

        String postData = "action=invokeOp&name=RR%3Aservice%3DFaultInjectionService&methodIndex=" 
            + fi_methodIndex + "&arg0=" + component + "&arg1=" + fault +"\r\n\n";
        Map headers = new HashMap();
        Map cookieJar = new HashMap();    

           // set up headers
        headers.put("Host", jmx_console_server + ":" + jmx_console_port);
        headers.put("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.2.1) Gecko/20030225");
        headers.put("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,video/x-mng,image/png,image/jpeg,image/gif;q=0.2,text/css,*/*;q=0.1");
        headers.put("Accept-Language", "en-us, en;q=0.50");
        headers.put("Accept-Encoding", "gzip, deflate, compress;q=0.9");
        headers.put("Accept-Charset", "ISO-8859-1, utf-8;q=0.66, *;q=0.66");
        headers.put("Keep-Alive", "300");
        headers.put("Connection", "keep-alive"); 

        // do request 
        HttpURLConnection httpConn = null;
        try
        {
            httpConn = doUrlRequest(invoke_url, headers, cookieJar, postData);
        }
        catch(NumRetriesExceededException e)
        {
            throw new FIUtilMethodFailureException("Number of retries exceeded!", e);
        }
        
        try
        {            
            String resp = respToStr(httpConn);
            httpConn.disconnect();
            if(resp == null)
            {
                throw new HTMLErrorException("HTTP Response from '" + invoke_url.toString() 
                                             + "' request has no content!");
            }
            readResponse(resp); // parses the response for a message, returns on
            // success, throws exception on error  
        }
        catch(HTMLErrorException e)
        {
            throw new FIUtilMethodFailureException("Error Parsing Response", e);
        }
        catch(IOException e)
        {
            throw new FIUtilMethodFailureException("IO Error", e);
        }
    }
    
    public void doMicroReboot(String component) throws FIUtilMethodFailureException
    {
        URL invoke_url = null;
        
        try
        {
            // reboot a certain component via the JMX-console
            invoke_url = new URL("http", jmx_console_server, jmx_console_port, 
                                 jmx_console_invoke_url);
        }
        catch(MalformedURLException e)
        {
            throw new FIUtilMethodFailureException("Malformed URL!", e);
        }
        
        String postData = "action=invokeOp&name=RR%3Aservice%3DFaultInjectionService&methodIndex=" 
            + rb_methodIndex + "&arg0=" + component + "\r\n\n";
        Map headers = new HashMap();
        Map cookieJar = new HashMap();

        // set up headers
        headers.put("Host", jmx_console_server + ":" + jmx_console_port);
        headers.put("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.2.1) Gecko/20030225");
        headers.put("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,video/x-mng,image/png,image/jpeg,image/gif;q=0.2,text/css,*/*;q=0.1");
        headers.put("Accept-Language", "en-us, en;q=0.50");
        headers.put("Accept-Encoding", "gzip, deflate, compress;q=0.9");
        headers.put("Accept-Charset", "ISO-8859-1, utf-8;q=0.66, *;q=0.66");
        headers.put("Keep-Alive", "300");
        headers.put("Connection", "keep-alive");

        // do request 
        HttpURLConnection httpConn = null;
        try
        {
            httpConn = doUrlRequest(invoke_url, headers, cookieJar, postData);
        }
        catch(NumRetriesExceededException e)
        {
            throw new FIUtilMethodFailureException("Number of retries exceeded!", e);
        }
        
        try
        {            
            String resp = respToStr(httpConn);
            httpConn.disconnect();
            if(resp == null)
            {
                throw new HTMLErrorException("HTTP Response from '" + invoke_url.toString() 
                                             + "' request has no content!");
            }
            readResponse(resp); // parses the response for a message, returns on
            // success, throws exception on error  
        }
        catch(HTMLErrorException e)
        {
            throw new FIUtilMethodFailureException("Error Parsing Response", e);
        }
        catch(IOException e)
        {
            throw new FIUtilMethodFailureException("IO Error", e);
        }
    }
}


