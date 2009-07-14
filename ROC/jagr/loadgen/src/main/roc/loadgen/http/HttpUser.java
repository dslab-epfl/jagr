package roc.loadgen.http;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import roc.loadgen.InitializationException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.Response;
import roc.loadgen.User;

import org.apache.log4j.Logger;

public class HttpUser extends User {

    private static Logger log = Logger.getLogger( "HttpUser" );

    public static final String ARG_FILENAME = "filename";
    public static final String ARG_SERVER_NAME = "servername";
    public static final String ARG_SERVER_PORT = "serverport";
    public static final String ARG_NUM_LOOPS = "numloops";

    String serverName;
    int serverPort;

    int numLoops;

    List traces;
    int currTrace;

    Map cookieJar;
    String currentUserId;
    String currentPassword;

    String tracefilename;
    String shorttracefilename;
    Map metadata = new HashMap();
    
    static Random rand = new Random();

    public HttpUser() {
        cookieJar = new HashMap();
    }

	Arg[] argDefinitions = {
		new Arg( ARG_FILENAME, 
		         "name of the trace files to load",
		         Arg.ARG_LIST,
		         true,
		         null ),
		new Arg( ARG_SERVER_NAME,
				 "name of the server to generate load against",
				 Arg.ARG_LIST,
				 true,
				 null ),
		new Arg( ARG_SERVER_PORT,
				 "port on the server to connect to",
				 Arg.ARG_INTEGER,
				 true,
			 null ),
		new Arg( ARG_NUM_LOOPS,
			 "number of times to repeat the session (-1=loop forever)",
			 Arg.ARG_INTEGER,
			 false,
			 null )
	};

	/**
	 * @see roc.loadgen.User#getArguments()
	 */
	public Arg[] getArguments() {
		return argDefinitions;
	}



    public void start() throws InitializationException {
	List tracelist = (List)args.get(ARG_FILENAME);
	int enginenum = engine.getEngineNum();
	int traceidx = enginenum % tracelist.size();
	{
	    tracefilename = (String)tracelist.get(traceidx);
	    int idx=tracefilename.lastIndexOf('/');
	    shorttracefilename = tracefilename.substring(idx+1);
	}

	List serverlist = (List)args.get(ARG_SERVER_NAME);
	int numservers = serverlist.size();
	int serveridx = ((enginenum%numservers)+
			 ((int)(enginenum/numservers)))%numservers;
	serverName = (String)serverlist.get(serveridx);
	serverPort = ((Integer)args.get( ARG_SERVER_PORT )).intValue();

	numLoops = ((Integer)args.get( ARG_NUM_LOOPS )).intValue();
        try {
            loadTraces(tracefilename);
        }
        catch (IOException e) {
            throw new InitializationException(
                "Could not load trace file: " + tracefilename,
                e);
        }
    }

    public boolean resetSession() {
        cookieJar.clear();
        currTrace = 0;

	if( numLoops > 0 )
	    numLoops--;
	if( numLoops == 0 ) 
	    return false;
	
	return true;
    }

    public void loadTraces(String filename) throws IOException {
        BufferedReader traceReader =
            new BufferedReader(new FileReader(filename));
        traces = new ArrayList();

        String line = null;
        List currentTrace = new LinkedList();
        do {
            line = traceReader.readLine();
            if (line != null) {
                line = line.trim();
                if (line.length() > 0) {
                    currentTrace.add(line);
                }
                else if (currentTrace.size() > 0) {
                    traces.add(currentTrace);
                    currentTrace = new LinkedList();
                }
            }
        }
        while (line != null);

        currTrace = 0;
    }

    /**
     * replace all instances of 'search' in 's' with 'replace'
     *
     */
    String replaceString(String s, String search, String replace) {
        int idx = s.indexOf(search);

        while (idx != -1) {
            String first = s.substring(0, idx);
            String last = s.substring(idx + search.length());
            s = first + replace + last;
            idx = s.indexOf(search);
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
    String doVariableReplacement(String s) {
        if (s.indexOf("$NEWUSER") != -1) {
            currentUserId = "randomUser" + rand.nextInt();
            s = replaceString(s, "$NEWUSER", currentUserId);
            metadata.put("dynamicdata",currentUserId+","+currentPassword);
        }

        if (s.indexOf("$NEWPASSWORD") != -1) {
            currentPassword = "genericpassword";
            s = replaceString(s, "$NEWPASSWORD", currentPassword);
            metadata.put("dynamicdata",currentUserId+","+currentPassword);
        }

        if (s.indexOf("$USER") != -1) {
            s = replaceString(s, "$USER", currentUserId);
        }

        if (s.indexOf("$PASSWORD") != -1) {
            s = replaceString(s, "$PASSWORD", currentPassword);
        }

        return s;
    }

    void readCookieHeaders(Map headers) {

        if (headers == null)
            return;

        // search for cookie fields

        List cookies = (List)headers.get("Set-Cookie");

        if (cookies != null) {
            Iterator iter = cookies.iterator();
            while (iter.hasNext()) {
                String s = (String)iter.next();

                int eqlIdx = s.indexOf("=");
                int semiIdx = s.indexOf(";");
                String key = s.substring(0, eqlIdx);
                String val = s.substring(eqlIdx + 1, semiIdx);
                cookieJar.put(key, val);
            }
        }
    }

    String generateCookieHeader() {
        Iterator iter = cookieJar.keySet().iterator();
        String cookieLine = "";
        while (iter.hasNext()) {
            String key = (String)iter.next();
            String val = (String)cookieJar.get(key);
            cookieLine += key + "=" + val;
            if (iter.hasNext()) {
                cookieLine += "; ";
            }
        }
        return cookieLine;
    }

    public Request getNextRequest() {

	if( currTrace >= traces.size() ) {
	    if( ! resetSession() ) {
		throw new RuntimeException( "done looping" );
	    }
	}

	boolean sessionstart = ( currTrace == 0 );

        List nextTrace = (List)traces.get(currTrace++);

        Map headers = new HashMap();
        String file = null;
        String postData = null;
        Iterator iterWithinTrace = nextTrace.iterator();

        while (iterWithinTrace.hasNext()) {
            String line = (String)iterWithinTrace.next();

            // special cmd: erases all cookies we've saved
            if (line.equals("$RESETCOOKIES")) {
                cookieJar.clear();
                log.info("cleared cookie jar");
                continue;
            }

            if (line.startsWith("GET")) {
                line = line.substring("GET".length()).trim();
                if (line.endsWith("HTTP/1.1")) {
                    line =
                        line.substring(0, line.length() - "HTTP/1.1".length());
                }
		line = line.trim();
                file = doVariableReplacement(line);
            }
            else if (line.startsWith("POST")) {
                line = line.substring("POST".length()).trim();
                if (line.endsWith("HTTP/1.1")) {
                    line =
                        line.substring(0, line.length() - "HTTP/1.1".length());
                }
                file = doVariableReplacement(line);
            }
            else if (line.startsWith("Referer:")) {
                try {
                    URL refererUrl =
                        new URL(line.substring("Referer:".length()));
                    headers.put(
                        "Referer",
                        "http://"
                            + serverName
                            + ":"
                            + serverPort
                            + refererUrl.getFile());
                }
                catch (MalformedURLException ignore) {
                }
            }
            else if (line.startsWith(":")) {
                headers.put("Host", serverName + ":" + serverPort);
            }
            else if (line.startsWith("Content-Length:")) {
                // ignore	
            }
            else if (line.startsWith("Cookie:")) {
                // ignore            	
            }
            else if (line.startsWith("LG-POSTDATA")) {
                // everything following this line is POST data
                postData = "";
                while (iterWithinTrace.hasNext()) {
                    postData += (String)iterWithinTrace.next() + "\r\n";
                }
                postData += "\n";
                postData = doVariableReplacement(postData);
            }
            else {
                int idx = line.indexOf(":");
                if (idx != -1) {
                    headers.put(
                        line.substring(0, idx),
                        line.substring(idx + 1).trim());
                }
            }
        }

        String cookieHdr = generateCookieHeader();
        headers.put("Cookie", cookieHdr);

        if (postData != null) {
            headers.put("Content-Length", Integer.toString(postData.length()));
        }

		URL url;

		try {
		    log.debug( "Creating URL from: " +
				    "protocol=http" + 
				    "; serverName=" + serverName +
				    "; serverPort=" + serverPort +
				    "; file=" + file );
				    
        	url = new URL("http", serverName, serverPort, file);
		}
		catch( MalformedURLException ex ) {
			throw new RuntimeException( "ACK. Broken trace?", ex );
		}
		
		HttpRequest ret = new HttpRequest(url, headers, postData, getMetadata() );
		ret.setStringID( shorttracefilename + ":" + currTrace );

		if( sessionstart ) 
		    ret.getMetadata().put("start-session","true");

		return ret;
    }
    
    /**
     * @return
     */
    private Map getMetadata() {
        return metadata;
    }



    public void processResponse(Request req, Response resp) {
        HttpResponse httpResp = (HttpResponse)resp;
        readCookieHeaders(httpResp.getHeaders());
        // add more processing here as required...

        String page = new String( ((HttpResponse)resp).getRespBuf());
        // 
    }


}
