//
// $Id: LoadGen2.java,v 1.12 2004/02/19 18:53:01 emrek Exp $
//

// Based on 'ebe' written by Eugene Fratkin <fratkin@cs.stanford.edu>

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;


public class LoadGen2 {

    public static final int MAX_RETRIES = 2;

    static int numClients = 1;
    static String webServer = "localhost";
    static int webPort = 8080;
    static final int time_elapsed=1000;
    static int total_req_number=0;

    public static void main (String args[]) throws Exception {

	if( args.length < 4 ) {
	    System.err.println( "Usage: java LoadGen2 numclients hostname port tracefiles... [# of repeats]" );
	    return;
	}

	numClients = Integer.parseInt(args[0]);
	webServer = (String) args[1];
	webPort = Integer.parseInt(args[2]);
	int repeats = Integer.MAX_VALUE;
	if ( args.length > numClients+3 )
	{
	   repeats = Integer.parseInt(args[numClients+3]);
	}
    
	String tracefile = null;
	for( int i=0; i<numClients; i++ ) {
	    if( args.length > 3+i )
		tracefile = args[3+i];
	    ClientReader2 client = new ClientReader2( "Client#" + i,
						      tracefile,
						      "log." + i,
						      repeats );
	    Thread t = new Thread( client );
	    t.start();
	    client.dumpStats();
	}

    }

}


class ClientReader2 implements Runnable
{
    boolean error=false;

    String clientId;
    String traceFilename;
    String logFilename;

    String currentUserId;
    String currentPassword;
    Map cookieJar = new HashMap();

    static Random rand = new Random();

    static int MAXRESP = 600;           // maximum HTTP response code (conservative)
    private int HttpRespCount[] = new int[MAXRESP]; // keep counters of response codes
    static int UNKNOWN_RESP = 0;        // index of counter for unknown read failures
    static int CONN_EXCEPTION = 1;
    static int IO_EXCEPTION = 2;
    static int RETRY_TIMEOUT = 10*1000; // 10 seconds
    static int repeats;

    public ClientReader2( String clientId, String tracefile, String outputfile,
			  int repeats ) {
	this.clientId = clientId;
	this.traceFilename = tracefile;
	this.logFilename = outputfile;
	this.repeats = repeats;
	for (int i=0 ; i < this.HttpRespCount.length ; this.HttpRespCount[i++] = 0);
    }


    public int getRandom() {
	synchronized( rand ) {
	    return rand.nextInt();
	}
    }

    public void run() {
	try {
           runLoad();
        } 
        catch (Exception e) 
        {
           System.err.println(e);
           e.printStackTrace();
	}
    }

    /**
     * print out the statistics
     *
     */
    void dumpStats () 
    {
       System.out.println("");
       for (int i=0 ; i < HttpRespCount.length ; i++)
       {
	  if ( HttpRespCount[i] > 0 )
	  {
	     if ( i == UNKNOWN_RESP )
	     {
		System.out.println(HttpRespCount[i] + " req's encountered an unknown error");
	     }
	     else if ( i == CONN_EXCEPTION )
	     {
		System.out.println(HttpRespCount[i] + " req's got connection errors");
	     }
	     else if ( i == IO_EXCEPTION )
	     {
		System.out.println(HttpRespCount[i] + " req's got I/O errors");
	     }
	     else
	     {
		System.out.println(HttpRespCount[i] + " req's returned HTTP code " + i);
	     }
	  }
       }
    }

    /**
     * replace all instances of 'search' in 's' with 'replace'
     *
     */
    String replaceString( String s, String search, String replace ) {
	int idx = s.indexOf( search );

	while( idx != -1 ) {
	    String first = s.substring( 0, idx );
	    String last = s.substring( idx + search.length());
	    s = first + replace + last;
	    idx = s.indexOf( search );
	}

	return s;
    }


    /** Replace a few special strings 
     *
     * $NEWUSER will be replaced with a newly
     *          generated, random userid.  This will
     *          become the current userid.
     *
     * $NEWPASSWORD will be replaced with a newly
     *              generated password, right now, just
     *              "password".
     *
     * $USER will be replaced with the current userid
     *
     * $PASSWORD will be replaced with current password
     *
     *
     * // later, add an $OLDUSER, and $OLDPASSWORD
     *
     **/
    String doVariableReplacement( String s ) {
	if( s.indexOf( "$NEWUSER" ) != -1 ) {
	    currentUserId = "randomUser" + getRandom();
	    s = replaceString( s, "$NEWUSER", currentUserId );
	}
	
	if( s.indexOf( "$NEWPASSWORD" ) != -1 ) {
	    currentPassword = "genericpassword";
	    s = replaceString( s, 
			       "$NEWPASSWORD",
			       currentPassword );
	}
	
	if( s.indexOf( "$USER" ) != -1 ) {
	    s = replaceString( s, "$USER", currentUserId );
	}
	
	if( s.indexOf( "$PASSWORD" ) != -1 ) {
	    s = replaceString( s, "$PASSWORD", 
			       currentPassword );
	}

	return s;
    }


    /**
     * returns a list of requests.  each request is a Map with two
     list of
     *  strings, where each string is one line of an HTTP request.
     */
    List loadTrace( String filename )  throws IOException {

        BufferedReader traceReader =
	    new BufferedReader(new FileReader( filename ));
        List traces = new LinkedList();

        //
        // Parse the recorded trace and store it in an in-memory vector
        //
	String line = null;
	List currentTrace = new LinkedList();
        do {
	    line = traceReader.readLine();
	    if( line != null ) {
		line = line.trim();
		if( line.length() > 0 ) {
		    currentTrace.add(line);
		}
		else if( currentTrace.size() > 0 ) {
		    traces.add( currentTrace );
		    currentTrace = new LinkedList();
		}
	    }
	}
	while( line != null );

	return traces;
    }


    /**
     * Negative exponential distribution w/a mean think time of 7sec.
     * used by TPC-W spec for
     * think time (clause 5.3.2.1) and USMD (clause 6.1.9.2).  This
     * function is borrowed from the RUBiS Client,
     *   edu.rice.rubis.client.TransitionTable.
     */
    private long TPCWthinkTime() {
	double r = rand.nextDouble();
	if( r < (double)4.54e-5)
	    return ((long) (r+0.5));
	return ((long) ((((double)-7000.0)*Math.log(r))+0.5));
    }


    public String generateCookieHeader( Map cookieJar ) {
	Iterator iter = cookieJar.keySet().iterator();
	String cookieLine = "";
	while( iter.hasNext() ) {
	    String key = (String)iter.next();
	    String val = (String)cookieJar.get( key );
	    cookieLine += key + "=" + val;
	    if( iter.hasNext() ) 
		cookieLine += "; ";
	}
	return cookieLine;
    }


    public void runLoad() throws Exception {

        PrintWriter logFileWriter = 
	    new PrintWriter(new FileWriter( logFilename ));

	List traces = loadTrace( traceFilename );
	logFileWriter.println( "runLoad: Loaded " + traces.size() + 
			       " requests from " + traceFilename );

	for ( ; repeats > 0 ; repeats-- )
	{
	   try {
	      runLoad( traces, logFileWriter );
	   }
	   catch( IOException e ) {
	      e.printStackTrace();
	   }
	}

	dumpStats();
	System.err.println( "done with trace! " + clientId + " is quitting" );
	logFileWriter.println( "done with trace! " + clientId + " is quitting" );
    }
	

    public void runLoad( List traces, PrintWriter logFileWriter ) throws Exception {
	Iterator tracesIter = traces.iterator();
	while( tracesIter.hasNext() ) {
	    // waiting for random amount of time before starting...
	    Thread.sleep( TPCWthinkTime() );

	    List trace = (List)tracesIter.next();

	    Map headers = new HashMap();
	    String file = null;
	    String postData = null;
	    
	    Iterator iter = trace.iterator();
	    while( iter.hasNext() ) {
		String line = (String)iter.next();

		// special command: erases all cookies we've saved.
		if( line.equals( "$RESETCOOKIES" )) {
		    cookieJar.clear();
		    logFileWriter.println( "CLEARED COOKIE JAR" );
		    continue;
		}

		if( line.startsWith( "GET" )) {
		    line = line.substring( "GET".length() ).trim();
		    if( line.endsWith( "HTTP/1.1" ));
		    line = line.substring( 0, 
					   line.length() - "HTTP/1.1".length() );
		    file = doVariableReplacement( line );
		}
		else if( line.startsWith("POST" )) {
		    line = line.substring( "POST".length() ).trim();
		    if( line.endsWith( "HTTP/1.1" ));
		    line = line.substring( 0, 
					   line.length() - "HTTP/1.1".length() );
		    file = doVariableReplacement( line );
		}
		else if( line.startsWith("Referer:")) {
		    URL refererUrl = 
			new URL( line.substring( "Referer:".length() ));
		    headers.put( "Referer", 
				 "http://" + LoadGen2.webServer + ":" + 
				 LoadGen2.webPort + refererUrl.getFile());
		}
		else if(line.startsWith("Host:")) {
		    headers.put( "Host", 
				 LoadGen2.webServer + ":" + LoadGen2.webPort );
		}
		else if(line.startsWith("Content-Length:" )) {
		    // ignore
		}
		else if(line.startsWith("Cookie:")) {
		    // ignore
		}
		else if(line.startsWith("LG-POSTDATA")) {
		    // everything following this line is POST data
		    postData = "";
		    while( iter.hasNext() ) {
			postData += (String)iter.next() + "\r\n";
		    }
		    postData += "\n";
		    postData = doVariableReplacement( postData );
		}
		else {
		    int idx = line.indexOf( ":" );
		    if( idx != -1 ) 
			headers.put( line.substring( 0, idx ),
				     line.substring( idx+1 ).trim() );
		}
	    }

	    String cookieHdr = generateCookieHeader( cookieJar );
	    headers.put( "Cookie", cookieHdr );
	    logFileWriter.println( "Cookie: " + cookieHdr );

	    if( postData != null ) {
		headers.put( "Content-Length", Integer.toString( postData.length() ));
	    }



	    if( file == null ) {
		System.err.println( "NO FILENAME SPECIFIED IN TRACE. IS THERE A GET OR A POST?!" );
		throw new RuntimeException( "ACK. Broken Trace?!" );
		// continue; // maybe we shouldn't abort
	    }

	    URL url = new URL( "http", 
			       LoadGen2.webServer,
			       LoadGen2.webPort,
			       file );
	    try {

	    doRequest( url, headers, cookieJar, postData,
		       logFileWriter );
	    }
	    catch( NumRetriesExceededException e ) {
		System.out.println("\n***** Reached max # of retries; skipping current trace. *****");
		return; // abandon the trace
	    }
	    finally {
		logFileWriter.flush();
	    }

  	    LoadGen2.total_req_number++;

//  	    {
//  		int reqs = LoadGen2.total_req_number;
//  		if ( reqs % 10 == 0 )
//  		    System.err.print('.');
//  	    }
	    System.err.println("Total number of Requests: " + LoadGen2.total_req_number );
	    logFileWriter.println("Total number of Requests: " + 
			      LoadGen2.total_req_number );
	    logFileWriter.flush();
        }
    }

    public void readCookieHeaders( HttpURLConnection httpConn, 
				   Map cookieJar,
				   PrintWriter logFileWriter ) {

	Map headers = httpConn.getHeaderFields();

	// search for cookie fields

	List cookies = (List)headers.get( "Set-Cookie" );

	if( cookies != null ) {
	    Iterator iter = cookies.iterator();
	    while( iter.hasNext() ) {
		String s = (String)iter.next();
		
		int eqlIdx = s.indexOf( "=" );
		int semiIdx = s.indexOf( ";" );
		String key = s.substring( 0, eqlIdx );
		String val = s.substring( eqlIdx + 1, semiIdx );
		logFileWriter.println( "SET COOKIE " + key + "=" + val );
		cookieJar.put( key, val );
	    }
	}

    }

    public int readContent( HttpURLConnection httpConn ) throws IOException {
	
	BufferedInputStream is = 
	    new BufferedInputStream( httpConn.getInputStream());
	
	byte[] buf = new byte[ 65536 ];
	int c;
	int total = 0;
	while( true ) {
	    c = is.read( buf );
	    total += c;
	    //System.err.println( "readContent: read " + total + " bytes" );
	    if( c == -1 )
		break;
	}

	return total;
    }

	
    public void doRequest( URL url, Map headers, Map cookieJar,
			   String postData,
			   PrintWriter logFileWriter ) throws IOException, NumRetriesExceededException {

        boolean needToSend = true;
	int retries=0;

	logFileWriter.println( "doRequest: " + url.toString() );
	
	while ( needToSend ) 
        {
	   int resp;

	   HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();

	   if( postData != null ) {
	      httpConn.setRequestMethod( "POST" );
	      httpConn.setDoOutput( true );
	   }
	   
	   // set headers
	   Iterator iter = headers.entrySet().iterator();
	   while( iter.hasNext() ) {
	      Map.Entry entry = (Map.Entry)iter.next();
	      httpConn.setRequestProperty( (String)entry.getKey(), 
					   (String)entry.getValue() );
	   }
	   
	   logFileWriter.println( "doRequest: connecting... " );

	   try {
	      httpConn.connect();

	      if( postData != null ) {
		 logFileWriter.println( "doRequest: POST data is " + postData );
		 PrintWriter pw = new PrintWriter( httpConn.getOutputStream() );
		 pw.print( postData );
		 pw.flush();
	      }
	   
	      readCookieHeaders( httpConn, cookieJar, logFileWriter );
	      resp = httpConn.getResponseCode();
	   }
	   catch ( ConnectException e )
	   {
	      resp = CONN_EXCEPTION; // will force a retry
	   }

	   logFileWriter.println( "PINPOINTRESPONSE: requestid=" + 
				  httpConn.getHeaderFields().get( "PP-Request" ) + " ;response=" + resp );

	   if ( resp < 0 || resp >= MAXRESP ) // keep response code in range
	   {
	      resp = UNKNOWN_RESP;
	   }
	   else if ( resp < 400 ) // it's a good code
	   {
	      try {
		 logFileWriter.println( "doRequest: response message is " + 
					httpConn.getResponseMessage() );
		 logFileWriter.println( "doRequest: content length is " + 
					httpConn.getContentLength() );
		 int bytesRead = readContent( httpConn );
		 logFileWriter.println( "doRequest: read " + bytesRead + " bytes" );
		 needToSend = false; // all's good, don't need to retry
	      } 
	      catch ( IOException e ) 
	      {
		 resp = IO_EXCEPTION;
	      }
	   } 

	   HttpRespCount[resp]++; // update the counter
	   httpConn.disconnect();

	   if ( needToSend )
	   {
              retries++;

              if( retries > LoadGen2.MAX_RETRIES ) {
		  throw new NumRetriesExceededException();
	      }

              System.out.println("RETRY [ got " + resp + " for " + url.toString() +
				 "] Sleeping " + RETRY_TIMEOUT/1000 + " sec...");
	      try {
		 Thread.sleep(RETRY_TIMEOUT);
	      }
	      catch (InterruptedException e)
	      {} // ignore
	   }
	}

	logFileWriter.println( "doRequest: finished." );
    }

    class NumRetriesExceededException extends Exception {
	public NumRetriesExceededException() { }
    }
    
}




