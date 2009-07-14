//
// $Id: LoadGen3.java,v 1.17 2003/04/13 23:11:01 emrek Exp $
//

//
// compile:
//   javac -classpath ../jargs/lib/jargs.jar:$JBOSS_TOP/server/output/classes LoadGen3.java
//
// run:
//   java -cp .:../jargs/lib/jargs.jar:$JBOSS_TOP/server/output/classes LoadGen3
//

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import jargs.gnu.CmdLineParser;
import org.jboss.RR.FailureReport;
import sun.net.www.content.text.PlainTextInputStream;
import java.text.SimpleDateFormat;

public class LoadGen3 
{
    static int msglport = 1313; // default port to listen for pause/unpause messages
    static boolean pauseClient = false;
    static boolean doPausing = false; // should we even bother with pausing ?

    static int numClients = 1;                    // number of load generating clients
    static String webServer = "localhost";        // destination host for requests
    static int webPort = 8080;                    // destination port for requests
    static int maxRetries = Integer.MAX_VALUE;    // max. # retries before giving up
    static int workloadLoops = Integer.MAX_VALUE; // # times to repeat workload trace
    static boolean thinkTime = false;             // if true, introduce think time
    static int retryTimeout = 10;                 // # sec to wait between retries
    static int vLevel = 0;                        // verbosity level
    static int maxStall = Integer.MAX_VALUE;      // max. # of millisec to stall
    static private InetAddress brainAddr;         // the reco mgr's IP address
    static int spawnDelay=0;                      // # of msec inbetween client spawns

    public static void main (String args[]) throws Exception 
    {
	System.getProperties().setProperty("sun.net.client.defaultReadTimeout", "30000");

	//
	// Parse command-line arguments
	//
	Object o;
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option oHost      = parser.addStringOption ('h', "host");
        CmdLineParser.Option oPort      = parser.addIntegerOption('p', "port");
        CmdLineParser.Option oRetries   = parser.addIntegerOption('m', "max");
        CmdLineParser.Option oLoops     = parser.addIntegerOption('l', "loops");
        CmdLineParser.Option oThink     = parser.addBooleanOption('t', "think");
        CmdLineParser.Option oTimeout   = parser.addDoubleOption ('i', "interval");
        CmdLineParser.Option oVerbosity = parser.addIntegerOption('v', "verbosity");
        CmdLineParser.Option oPausing   = parser.addBooleanOption('a', "pausing");
        CmdLineParser.Option oDelay     = parser.addIntegerOption('d', "delay");
        CmdLineParser.Option oBrain     = parser.addStringOption ('b', "brain");
        CmdLineParser.Option oMaxStall  = parser.addStringOption ('s', "stall");

        try 
	{
            parser.parse(args);
        }
        catch ( Exception e ) 
	{
            System.err.println(e.getMessage());
            printUsage();
            System.exit(2);
        }

	if ( (o=parser.getOptionValue(oHost))      != null ) { webServer = (String)o;                     }
	if ( (o=parser.getOptionValue(oPort))      != null ) { webPort = ((Integer)o).intValue();         }
	if ( (o=parser.getOptionValue(oRetries))   != null ) { maxRetries = ((Integer)o).intValue();      }
	if ( (o=parser.getOptionValue(oLoops))     != null ) { workloadLoops = ((Integer)o).intValue();   }
	if ( (o=parser.getOptionValue(oThink))     != null ) { thinkTime = true;                          }
	if ( (o=parser.getOptionValue(oTimeout))   != null ) { retryTimeout = 
								   (int)(1000*((Double)o).doubleValue()); }
	if ( (o=parser.getOptionValue(oVerbosity)) != null ) { vLevel = ((Integer)o).intValue();          }
	if ( (o=parser.getOptionValue(oPausing))   != null ) { doPausing = true;                          }
	if ( (o=parser.getOptionValue(oDelay))     != null ) { spawnDelay = ((Integer)o).intValue();      }
	if ( (o=parser.getOptionValue(oMaxStall))  != null ) { maxStall = 1000 * ((Integer)o).intValue(); }

	try 
	{
	    o = parser.getOptionValue(oHost);
	    if ( o != null ) 
	    { 
		brainAddr = InetAddress.getByName((String)o);
	    }
	    else
		brainAddr = InetAddress.getLocalHost();
	}
	catch ( Exception e )
	{
	    e.printStackTrace();
	    System.exit(1);
	}

	//
        // Get the trace file(s) from the command line
	//
        String[] otherArgs = parser.getRemainingArgs();
	numClients = otherArgs.length;

	if ( numClients <= 0 )
	{
	    printUsage();
            System.exit(2);
	}

	printArgs();

	//
	// Start the thread listening for commands, if needed
	//
	if ( doPausing )
	{
	    try
	    {
		Thread dpthread = new CommandThread();
		dpthread.start();
	    }
	    catch ( SocketException e )
	    {
		System.err.println("Could not bind to message listen port = " + msglport);
		System.exit(1);
	    }
	}
	
	//
	// Parse the trace file name(s) from the command line
	//
	System.out.print("No. of clients : " + otherArgs.length);
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss_EEE");
	String dateString = fmt.format(new Date());
	long now = System.currentTimeMillis();
        for ( int i = 0; i < otherArgs.length; i++ ) 
	{
	    String tracefile = otherArgs[i];
	    String fileName = "timeprofile_" + dateString + "_" + i;

	    ClientReader client = 
		new ClientReader( i, tracefile, "log." + i, fileName, brainAddr, now);

	    Thread t = new Thread( client );
	    t.start();

	    // Introduce a delay (can be zero) between spawning clients
	    try { 
		Thread.sleep(LoadGen3.spawnDelay);
	    }
	    catch (Exception e)
	    {}  // ignore
	    
	    // client.dumpStats();
	}
	System.out.println("");
    }

    //---------------------------------------------------------------------------

    private static void printUsage() 
    {
	System.err.println("\nCommand line options:");
	System.err.println("\t[ --host <JBoss server hostname> ]");
	System.err.println("\t[ --port <JBoss server port> ]");
	System.err.println("\t[ --brain <recovery manager hostname> ]");
	System.err.println("\t[ --loops <# loops to repeat workload> ]");
	System.err.println("\t[ --max <max # retries before skipping to next workload> ]");
	System.err.println("\t[ --interval <retry interval in seconds> ]");
	System.err.println("\t[ --think ]");
	System.err.println("\t[ --pausing ]");
	System.err.println("\t[ --stall <upper bound on # of sec to stall> ]");
	System.err.println("\t[ --verbosity <verbosity level> ]");
	System.err.println("\t[ --delay <# msec inbetween successive client spawns> ]");
	System.err.println("\t  tracefile1 [ tracefile2 ... ]\n" );
    }

    //---------------------------------------------------------------------------

    private static void printArgs()
    {
	System.out.println("destination    : http://" + webServer + ":" + webPort + "/");
	System.out.println("recovery mgr   : " + brainAddr.getHostAddress() + " (" + brainAddr.getHostName() + ")");
	System.out.println("retry every    : " + retryTimeout + " msec (max " + maxRetries + " times)");
	System.out.println("repeat workload: " + workloadLoops + " times");
	System.out.println("client pausing : " + (doPausing ? "ENABLED (max " + maxStall + " msec)" : "DISABLED"));
	System.out.println("TPC-W delay    : " + (thinkTime ? "ON" : "OFF"));
	System.out.println("spawning delay : " + spawnDelay + " msec inbetween clients");
	System.out.println("verbosity level: " + vLevel);
    }

    //---------------------------------------------------------------------------

    private static class CommandThread extends Thread
    {
        // this is to listen for pause/unpause message
        DatagramSocket socket = null;
        public CommandThread() throws SocketException
	{
	    socket = new DatagramSocket(msglport);
	    if ( vLevel > 1 )
	    {
		System.out.println("Pause thread listening for commands...");
	    }
	}
        
        public void run()
	{
	    while(true)
	    {
		try
		{
		    byte[] buf = new byte[1];
		    DatagramPacket packet = new DatagramPacket(buf, buf.length);
		    socket.receive(packet);
		    if(buf[0] == 'P') // pause
		    {
			if ( vLevel > 0 )
			{
			    System.out.println(" <PAUSE> ");
			}
			pauseClient = true;
		    }
		    else if(buf[0] == 'U') // unpause
		    {
			if ( vLevel > 0 )
			{
			    System.out.println(" <UN-PAUSE> ");
			}
			pauseClient = false;
		    }
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
	    }
	}
    }
}


class ClientReader extends Thread
{
    static int MAXBUF = 131072;
    byte[] buf = new byte[MAXBUF];

    private int numReqs=0;                         // number of requests made so far
    boolean error=false;

    String clientId;
    String traceFilename;
    String logFilename;
    String timeProfile;
    private int clientNum;

    String currentUserId;
    String currentPassword;
    Map cookieJar = new HashMap();
    Random rand = new Random();

    static int MAXRESP = 510; // maximum HTTP response code (conservative)
    private int HttpRespCount[] = new int[MAXRESP]; // keep counters of response codes
    static int UNKNOWN_RESP = 0;     // index of counter for unknown read failures
    static int CONN_EXCEPTION = 1;   // got a ConnectionException when trying to talk to svr
    static int IO_EXCEPTION   = 2;   // got an IOException when talking to server
    static int HTML_FAIL      = 3;   // found the FAIL keyword in returned HTML
    static int HTML_ERROR     = 4;   // found the ERRO keyword in returned HTML
    static int HTML_EMPTY     = 5;   // returned web page was empty
    static int READ_TIMEOUT   = 6;   // a read from the server timed out
    static int STALLED        = 7;   // the clients are being stalled
    static int sleepInterval  = 100; // no. of msec to sleep at a time when stalled
    static int prevMapCode    = 1;   // last status code returned by mapCode()

    static long firstTime=0;
    static InetAddress brainAddr;

    public ClientReader( int clientNum, String tracefile, String outputfile, String timeProfile, InetAddress brainAddr, long firstTime ) {
	this.clientNum = clientNum;
	this.clientId = "Client " + clientNum;
	this.traceFilename = tracefile;
	this.logFilename = outputfile;
	this.timeProfile = timeProfile;
	this.firstTime = firstTime;
	this.brainAddr = brainAddr;
	for (int i=0 ; i < this.HttpRespCount.length ; this.HttpRespCount[i++] = 0);
    }

    public int getRandom() {
	synchronized( rand ) {
	    return rand.nextInt();
	}
    }

    public void run() 
    {
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
       System.out.println("STATISTICS for " + clientId + ":");
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
	     else if ( i == HTML_FAIL )
	     {
		System.out.println(HttpRespCount[i] + " req's recv'd HTML with 'FAIL' keyword");
	     }
	     else if ( i == HTML_ERROR )
	     {
		System.out.println(HttpRespCount[i] + " req's recv'd HTML with 'ERRO' keyword");
	     }
	     else if ( i == HTML_EMPTY )
	     {
		System.out.println(HttpRespCount[i] + " req's recv'd an empty page");
	     }
	     else if ( i >= 100 )
	     {
		System.out.println(HttpRespCount[i] + " req's returned HTTP code " + i);
	     }
	     else
	     {
		System.out.println("** Unknown code " + i + ". This is a bug");
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
    String doVariableReplacement( String s ) 
    {
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

	s = s.trim();

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

	PrintWriter timeLogWriter = 
	    new PrintWriter(new FileWriter( timeProfile ));
	
	//
	// Write table header to timing profile file
	//
        timeLogWriter.println("#            web = http://" + LoadGen3.webServer + ":" + LoadGen3.webPort);
        timeLogWriter.println("# retry interval = " + LoadGen3.retryTimeout + " msec");
        timeLogWriter.println("#    max retries = " + LoadGen3.maxRetries);
        timeLogWriter.println("#          loops = " + LoadGen3.workloadLoops);
        timeLogWriter.println("#        pausing = " + (LoadGen3.doPausing ? "ENABLED" : "DISABLED"));
        timeLogWriter.println("#     think time = " + (LoadGen3.thinkTime ? "YES" : "NO") + "\n#");
	timeLogWriter.println("# Col 1: Time of request submission (in mseconds)");
	timeLogWriter.println("# Col 2: Time of request completion (in mseconds)");
	timeLogWriter.println("# Col 3: Outcome (1=success, 0=failure)");
	timeLogWriter.println("# Col 4: either HTTP code (>=100) or internal code (<100)");
	timeLogWriter.println("# Col 5: request URL\n#");

	List traces = loadTrace( traceFilename );
	logFileWriter.println( "runLoad: Loaded " + traces.size() + 
			       " requests from " + traceFilename );

	for (int iter=0 ; iter < LoadGen3.workloadLoops ; iter++ )
	{
	   try {
	      runLoad( traces, logFileWriter, timeLogWriter );
	   }
	   catch( IOException e ) {
	      e.printStackTrace();
	   }
	}

	dumpStats();
	System.err.println( "done with trace! " + clientId + " is quitting" );
	logFileWriter.println( "done with trace! " + clientId + " is quitting" );
    }
	

    public void runLoad ( List traces, 
			  PrintWriter logFileWriter,
			  PrintWriter timeLogWriter ) 
	throws Exception 
    {
	Iterator tracesIter = traces.iterator();

	timeLogWriter.println("# ----- " + clientId + " starting new trace -----");
	if ( LoadGen3.vLevel > 0 )
	{
	    System.out.println("\n ----- " + clientId + " starting new trace -----");
	}

	while( tracesIter.hasNext() ) 
	{
	    if ( LoadGen3.thinkTime ) 
	    {   // waiting for random amount of time before starting...
		Thread.sleep( TPCWthinkTime() );
	    }

	    List trace = (List)tracesIter.next();

	    Map headers = new HashMap();
	    String file = null;
	    String postData = null;
	    Iterator iterWithinTrace = trace.iterator();

	    while( iterWithinTrace.hasNext() ) 
	    {
		String line = (String)iterWithinTrace.next();

		// special cmd: erases all cookies we've saved.
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
				 "http://" + LoadGen3.webServer + ":" + 
				 LoadGen3.webPort + refererUrl.getFile());
		}
		else if(line.startsWith("Host:")) {
		    headers.put( "Host", 
				 LoadGen3.webServer + ":" + LoadGen3.webPort );
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
		    while( iterWithinTrace.hasNext() ) {
			postData += (String)iterWithinTrace.next() + "\r\n";
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
			       LoadGen3.webServer,
			       LoadGen3.webPort,
			       file );

	    try 
	    {
		doRequest(url, headers, cookieJar, postData, logFileWriter, timeLogWriter);
	    }
	    catch ( NumRetriesExceededException e )
	    {
		System.out.println("\n***** Reached max # of retries; skipping current trace. *****");
		return; // abandon the trace
	    }
	    catch ( Exception e )
	    {
		e.printStackTrace();
	    }
	    finally 
	    {
		logFileWriter.flush();
		timeLogWriter.flush();
	    }

  	    numReqs++;

	    if ( LoadGen3.vLevel == 1 )
  	    {
  		int reqs = numReqs;
  		if ( reqs % 10 == 0 )
		{
  		    System.err.print(clientNum);
		}
  	    }
	    else if ( LoadGen3.vLevel > 1 )
	    {
		System.out.println("Req " + numReqs + " " + url.toString());
	    }

	    logFileWriter.println("Total number of Requests: " + numReqs );
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

    /*-------------------- readContent ------------------------------*
     *                                                               *
     * Reads data from the server and tries to infer whether this is *
     * a successful response or not, by searching for relevant       *
     * keywords and checking whether it got a blank page.  If error  *
     * page is bigger than a threshold (currently 2K), we infer that *
     * it is an information page (e.g., a help page), rather than a  *
     * failure-related page.                                         *
     *---------------------------------------------------------------*/
    public int readContent( HttpURLConnection httpConn, PrintWriter log ) 
	throws IOException, HtmlFailException, 
	       HtmlErrorException, HtmlEmptyPageException
    {

	//
	// Read in the content.
	//
	int contentLength=0, index=0;
	InputStream is = httpConn.getInputStream();

	for (int nbytes=0 ; true ; contentLength += nbytes )
	{
	    index = contentLength % MAXBUF;
	    nbytes = is.read(buf, index, MAXBUF-index);
	    if ( nbytes == -1 )
		break;
	}

	// DEBUG
	/**
	System.err.println( "----------------------------" );
	System.err.println( "URL = " + httpConn.getURL().toString() );
	System.err.println( "Contentlength = " + contentLength );
	System.err.println( "responseCode = " + httpConn.getResponseCode() );
	System.err.println( "contentType = " + httpConn.getContentType() );
	System.err.println( "BEGIN CONTENT" );
	System.err.println( (new String(buf, 0, contentLength)).toUpperCase() );
	System.err.println( "END CONTENT" );
	**/

	//
	// If this page's URL contains "estore/control/white" we
	// ignore it.  For PetStore, a URL ending in /white returns a
	// page with zero bytes, but it does not signal a problem.
	//
	String url = httpConn.getURL().toString();
	if ( url.indexOf("estore/control/white") > -1 )
	{
	    return contentLength;
	}

	//
	// If the page is bigger than 100 KB, assume it's OK (a long
	// listing for RUBiS).
	//
	if ( contentLength > 100*1024 )
        {
	    return contentLength;
	}

	//
	// If it is a "redirect" response, then we ignore it, because
	// the workload trace should have the redirected URL.
	//
	int code = httpConn.getResponseCode();
	if ( code >= 300  &&  code < 400 )
	{
	    return contentLength;
	}

	//
	// If this page contains <100 bytes, assume it's empty.
	//
	if ( contentLength < 100 )
	{
	    throw new HtmlEmptyPageException();
	} 

	//
	// If we're getting anything else but a web page (e.g., a
	// GIF), we're done.  Just return its length.
	//
	String contentType = httpConn.getContentType();
	if ( contentType!=null && !contentType.toLowerCase().startsWith("text/html") )
	{
	    return contentLength;
	}

	//
	// Check if the HTML contains keywords FAIL or ERRO; this
	// covers FAILURE, FAILED, FAILING, ERROR, ERRONEOUS, etc.  If
	// the page is long (i.e., over 2KB), then I assume it is an
	// informational page (e.g., a help page, that is not really
	// an error.
	//
	String htmlContent = (new String(buf, 0, contentLength)).toUpperCase();

	if ( htmlContent.indexOf("FAIL") > -1 )
	{
	    throw new HtmlFailException(); // web page contained "FAIL"
	}
	
	// Need to special-case the help page for PetStore
	// EMK: Also need to special-case the sign-in error for invalid usernames
	if ( url.indexOf("estore/control/help") == -1  &&  
	     htmlContent.indexOf("ERRO") > -1 &&
	     htmlContent.indexOf("PLEASE CHECK YOUR USERNAME AND PASSWORD" ) == -1 )
	{
	    throw new HtmlErrorException(); // web page contained "ERRO"
	}

	//
	// Returned web page seems to be OK, so return its length
	//
	return contentLength;
    }

    /*-------------------- mapCode -------------------------*
     *                                                      *
     * Turns an internal code into a 0 if this indicates    *
     * a failure, or a 1 if it is a success.  If STALLED,   *
     * returns the code it returned on the last invocation. *
     *------------------------------------------------------*/
    private int mapCode ( int code )
    {
	if ( code == STALLED )
	{
	    return prevMapCode;
	}
	else if ( code < 100  ||  code >= 400 )
	{
	    return 0; // failure
	}
	else
	{
	    return 1; // success
	}
    }


    /*---------- doRequest --------------------------------------------------*
     *                                                                       *
     * Submit a request.                                                     *
     *-----------------------------------------------------------------------*/
    public void doRequest( URL url, 
			   Map headers, 
			   Map cookieJar,
			   String postData,
			   PrintWriter logFileWriter,
			   PrintWriter timeLogWriter ) 
	throws
	IOException, NumRetriesExceededException
    {
        boolean needToSend = true;
	int retries=0;

	while ( needToSend ) 
        {
	   int resp;

	   // If we're being paused, then wait for at most our own
	   // delay amount of time before retrying
	   if ( LoadGen3.doPausing && LoadGen3.pauseClient )
	   {
	       // Record the time when the stall starts
	       long curTime = System.currentTimeMillis();
	       long startTime = curTime;
	       timeLogWriter.print( startTime - firstTime + "\t" );
				      
	       while ( LoadGen3.doPausing && LoadGen3.pauseClient )
	       {
		   try 
		   {
		       Thread.sleep(sleepInterval);
		   }
		   catch ( InterruptedException e ) {} // ignore

		   // Make sure we're not stalling for too long
		   curTime = System.currentTimeMillis();
		   if ( curTime + sleepInterval >= startTime + LoadGen3.maxStall )
		       break;
	       }

	       timeLogWriter.print( curTime - firstTime + "\t" + // time of completion
				    mapCode(STALLED) + "\t" +    // system status
				    STALLED + "\t" +             // record STALLED code
				    url);                        // stalled req URL
	   }

	   HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();

	   if( postData != null ) 
	   {
	      httpConn.setRequestMethod( "POST" );
	      httpConn.setDoOutput( true );
	   }
	   
	   // set headers
	   Iterator iter = headers.entrySet().iterator();
	   while( iter.hasNext() ) 
	   {
	      Map.Entry entry = (Map.Entry)iter.next();
	      httpConn.setRequestProperty( (String)entry.getKey(), (String)entry.getValue() );
	   }
	   
	   logFileWriter.println( "doRequest: connecting... " );
	   timeLogWriter.print(System.currentTimeMillis() - firstTime + "\t"); 

	   try 
	   {
	      httpConn.connect();

	      if( postData != null ) 
	      {
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
	   catch ( SocketTimeoutException t )
	   {
	      resp = READ_TIMEOUT; // will force a retry
	   }

	   timeLogWriter.print(System.currentTimeMillis()-firstTime + "\t"); // time of reply

	   if ( resp < 0 || resp >= MAXRESP ) // keep response code in range
	   {
	      resp = UNKNOWN_RESP;
	   }
	   else if ( resp < 400 ) // code is within range; read the HTML content
	   {
	      try 
	      {
		 logFileWriter.println( "doRequest: response message is " + 
					httpConn.getResponseMessage() );
		 logFileWriter.println( "doRequest: content length is " + 
					httpConn.getContentLength() );
		 
		 int bytesRead = readContent( httpConn, logFileWriter );
		 logFileWriter.println( "doRequest: read " + bytesRead + " bytes" );
		 needToSend = false; // all's good, don't need to retry
	      } 
	      catch ( SocketTimeoutException t )
	      {
		  resp = READ_TIMEOUT;
	      }
	      catch ( HtmlFailException e ) 
	      {
		  resp = HTML_FAIL;     // the HTML had "FAIL" keyword
	      }
	      catch ( HtmlErrorException e ) 
	      {
		  resp = HTML_ERROR;    // the HTML had "ERRO" keyword
	      }
	      catch ( HtmlEmptyPageException e )
	      {
		  resp = HTML_EMPTY;    // returned page was empty
	      }
	      catch ( IOException e ) 
	      {
		  resp = IO_EXCEPTION;  // got an I/O error
	      }
	   } 

	   HttpRespCount[resp]++; // update the counter
	   timeLogWriter.println(mapCode(resp) + "\t" + resp + "\t" + url);
	   httpConn.disconnect();

	   if ( needToSend )
	   {
	       // I need to retry the request, which means there was
	       // an end-to-end failure.  First send a failure
	       // notification to the recovery manager (thus, the load
	       // generator also acts as an end-to-end checker).  Then
	       // sleep a little, and after that retry.
	       retries++;
	       sendFailureReport();

               // If I've exceeded the number of retries, then throw
               // an exception; this will force the caller to go back
               // to the beginning of the workload trace.  It emulates
               // a user that gives up and tries everything from
               // scratch (or user walks away and another client
               // starts the workload).
	       if ( retries > LoadGen3.maxRetries )
	       {
		   throw new NumRetriesExceededException();
	       }

	       describeFailure(url, httpConn, resp); // tell the console user what happened
	       
	       try // sleep for a while before retrying
	       { 
		   Thread.sleep(LoadGen3.retryTimeout);
	       }
	       catch (InterruptedException e)
	       {} // ignore
	   }
	}

	logFileWriter.println( "doRequest: finished." );
    }


    /*---------- describeFailure ---------------------------------------------*
     *                                                                        *
     * Print out a description of the failure we encountered.                 *
     *------------------------------------------------------------------------*/
    private void describeFailure (URL url, HttpURLConnection conn, int response)
	throws IOException
    {
	if ( LoadGen3.vLevel > 0 )
	{
	    if ( response > 100 )
	    {
		System.out.println("\n** " + clientId + " HTTP: " + conn.getResponseMessage() + " **");
	    }
	    else if ( response == HTML_FAIL )
	    {
		System.out.println("\n** " + clientId + " HTML: body indicated failure **");
	    }
	    else if ( response == HTML_ERROR )
	    {
		System.out.println("\n** " + clientId + " HTML: body indicated error **");
	    }
	    else if ( response == HTML_EMPTY )
	    {
		System.out.println("\n** " + clientId + " HTML: body was empty **");
	    }
	    else if ( response == IO_EXCEPTION )
	    {
		System.out.println("\n** " + clientId + " NETWORK: read failed **");
	    }
	    else if ( response == CONN_EXCEPTION )
	    {
		System.out.println("\n** " + clientId + " NETWORK: connect failed **");
	    }
	    else if ( response == READ_TIMEOUT )
	    {
		System.out.println("\n** " + clientId + " NETWORK: read timed out **");
	    }
	    else if ( response == UNKNOWN_RESP )
	    {
		System.out.println("\n** " + clientId + " Unknown error **");
	    }
	    else
	    {
		System.out.println("\n** " + clientId + " Unknown response code " + response + ". This is a bug. Goodbye. **");
		System.exit(3);
	    }
	    System.out.println("   will retry " + url.toString() + " after " + LoadGen3.retryTimeout + " msec...");
	}
    }


    /*---------- sendFailureReport -------------------------------------------*
     *                                                                        *
     * Tell the recovery manager that I've just encountered an end-to-end     *
     * failure on my most recent request.                                     *
     *------------------------------------------------------------------------*/
    private static void sendFailureReport ()
    {
	try
	{  
	    // FIXME: remove the hardcoded address and port
	    InetSocketAddress sockAddr = new InetSocketAddress(brainAddr, 2374);
	    DatagramSocket socket = new DatagramSocket();
	    FailureReport report = new FailureReport(new Date()); 
	    ByteArrayOutputStream bArray_out = new ByteArrayOutputStream();

	    ObjectOutputStream obj_out = new ObjectOutputStream(bArray_out);
	    obj_out.writeObject(report);

	    DatagramPacket packet = new DatagramPacket(bArray_out.toByteArray(), bArray_out.size(), sockAddr);
	    socket.send(packet);

	    if ( LoadGen3.vLevel > 1 )
	    {
		System.out.println("\n--- FailureReport sent to TheBrain ---");
	    }
	}
	catch (SocketException sockExp)
	{
	    System.err.println("Failed to bind to UDP port");
	    sockExp.printStackTrace();
	}
	catch (IOException ioExp)
	{
	    System.err.println("Error sending FailureReport");
	    ioExp.printStackTrace();
	}
    }

    /*----------------------------------------------------------------------*
     * An exception indicating that HTML contained the "FAIL" keyword       *
     * (this accounts for FAIL, FAILURE, FAILED, etc.).                     *
     *----------------------------------------------------------------------*/
    class HtmlFailException extends Exception 
    {
	public HtmlFailException() {}
    }
	
    /*----------------------------------------------------------------------*
     * An exception indicating that HTML contained the "ERRO" keyword       *
     * (this accounts for ERROR, ERRONEOUS, etc.).                          *
     *----------------------------------------------------------------------*/
    class HtmlErrorException extends Exception 
    {
	public HtmlErrorException() {}
    }

    /*----------------------------------------------------------------------*
     * An exception indicating that the returned HTML page was empty.       *
     *----------------------------------------------------------------------*/
    class HtmlEmptyPageException extends Exception 
    {
	public HtmlEmptyPageException() {}
    }

    /*----------------------------------------------------------------------*
     * An exception indicating that I retried the given request and         *
     * exceeded the maximum number of retries.                              *
     *----------------------------------------------------------------------*/
    class NumRetriesExceededException extends Exception 
    {
	public NumRetriesExceededException() {}
    }
}


