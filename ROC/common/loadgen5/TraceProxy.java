import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Proxy2, rewritten trace generator, passes through images and
 * binary data better than original Proxy.java
 *
 */
public class TraceProxy {

    int proxyPort;

    String webServer;
    int webPort;

    boolean recording = false;
    boolean done = false;

    ServerSocket serverSocket;
    PrintWriter outputFileWriter;
    String outputfile;

    TraceCleaner cleaner;

     public TraceProxy( int proxyPort,
		   String webServer, int webPort,
		   String outputfile ) throws IOException {

	this.proxyPort = proxyPort;
	this.webServer = webServer;
	this.webPort = webPort;
	this.outputfile = outputfile;
	cleaner = new TraceCleaner(outputfile, this);

	serverSocket = new ServerSocket( proxyPort );
	outputFileWriter = 
	    new PrintWriter( new FileWriter( cleaner.TEMP_FILE, true ));

	System.err.println( "Waiting for client to connect..." );
    }

    public TraceCleaner getCleaner() { return cleaner; }

    synchronized void saveHeaders( String headers ) {
	outputFileWriter.print( headers + "\n\n" );
	outputFileWriter.flush();
    }

    public boolean isRecording() { return recording; }
    public void setDone(boolean d) { done = d; }
    public void closeWriter() { outputFileWriter.close(); } 
  
    public void runProxy() {
	
	while( !done ) {

	    try {
		Socket client = serverSocket.accept();
		Socket web = new Socket( webServer, webPort );		

		if(done) 
		    break;

		ProxyConnection pc = 
		    new ProxyConnection( client, web );

		new Thread( pc ).start();
	    }
	    catch( IOException e ) {
		e.printStackTrace();
	    }
	}	
    }

    
    class ProxyConnection implements Runnable { 
	
	Socket client;
	Socket web;

	ProxyConnection( Socket client, Socket web ) {
	    this.client = client;
	    this.web = web;
	}

	public void run() {
	    try {
		recording = true;
		String headers = "";

		System.err.println( "Connected to Client and Web server (" + this.hashCode() + ")" );

		// forward Http request to server.
		// store requests in 'headers' string.
		InputStream clientIs = client.getInputStream();
		DataInputStream clientDis = new DataInputStream( clientIs );
		OutputStream serverOs = web.getOutputStream();
		DataOutputStream serverDos = new DataOutputStream( serverOs );

		System.err.println( "[" + this.hashCode() + "] reading request and headers..." );

		int contentlen=0;

		StringTokenizer st;
		boolean done = false;
		do { 
		    String s = clientDis.readLine();
		    System.err.println( "["+ this.hashCode() + "] got header: " + s );
		    headers += s + "\n";
		    serverDos.writeBytes( s + "\r\n" );
		    if( s.startsWith( "Content-Length:" )) {
			String cl = s.substring( "Content-Length:".length() ).trim();
			try {
			    contentlen = Integer.parseInt( cl );
			}
			catch( Exception e ) {
			    System.err.println( "Could not parse Content-Length: " + cl );
			}
		    }

		    st = new StringTokenizer (s);
		    if (st.countTokens() == 0)
			done = true;
		}
		while( !done );
	
		System.err.println( "[" + this.hashCode() + "] done reading header" );

		byte[] buf = new byte[ 65536 ];

		// read POST arguments, if needed.
		if( contentlen > 0 ) {
		    done = false;
		    int total=0, c=0;
		    String body = "";
		    do {
			c = clientIs.read( buf );
			if( c > 0 ) {
			    total += c;
			    serverOs.write( buf, 0, c );
			    body += new String( buf, 0, c );
			}
		    }
		    while(( total < contentlen ) && ( c != -1 ));
		    headers += body;
		    System.err.println( "[" + this.hashCode() + "] done reading POST msg body" );
		}

		// forward Http response to client
		InputStream serverIs = web.getInputStream();
		OutputStream clientOs = client.getOutputStream();
		
		System.err.println( "[" + this.hashCode() + "] forwarding server response..." );

		int c;
		do {
		    c = serverIs.read( buf );
		    if( c > 0 ) 
			clientOs.write( buf, 0, c );
		}
		while( c != - 1 );

		System.err.println( "[" + this.hashCode() + "] done forwarding server response" );

		saveHeaders( headers );
	    }
	    catch( IOException e ) {
		e.printStackTrace();
	    }
	    finally {
		System.err.println( "Closing connection (" + this.hashCode() + ")" );
		try { client.close(); }
		catch( IOException ignore ) { }

		try { web.close(); }
		catch( IOException ignore ) { }
	    }

	    recording = false;
	}
	
    }

    public static void PrintUsage() {
	System.out.println( "Usage: java Proxy port serverhost " + 
			    "serverport outputfile" );
	System.out.println( "\tProxy will listen to http requests " + 
			    "coming to 'port', record them, and " + 
			    "forward the http requests to serverhost " + 
			    "and serverport" );
    }
    

    public static void main( String args[] ) {
	try {
	    if( args.length != 4 ) {
		PrintUsage();
		return;
	    }

	    int proxyPort;
	    String webServer;
	    int webPort;
	    String outputfile;

	    try {
		proxyPort = Integer.parseInt(args[0]);
		webServer= args[1];
		webPort= Integer.parseInt(args[2]);
		outputfile = args[3];
	    } catch (Exception e) {
		PrintUsage();
		return;
	    }	    

	    TraceProxy proxy = new TraceProxy( proxyPort,
				    webServer, webPort,
				    outputfile );	    

	    Runtime.getRuntime().addShutdownHook(proxy.getCleaner());

	    proxy.runProxy();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}

    }


}
