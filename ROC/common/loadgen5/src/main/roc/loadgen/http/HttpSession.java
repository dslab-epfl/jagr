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

import roc.loadgen.AbortSessionException;
import roc.loadgen.AbortRequestException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.Response;
import roc.loadgen.Session;
import roc.loadgen.TraceReader;

public class HttpSession extends Session {

    public static final String ARG_FILENAME = "filename";
    public static final String ARG_SERVER_NAME = "servername";
    public static final String ARG_SERVER_PORT = "serverport";
    public static final String ARG_NUM_LOOPS = "numloops";

    String serverName;
    int serverPort;

    int numLoops;

    Map cookieJar;

    protected String currentUserId;
    protected String currentPassword;

    TraceReader reader;

    static Random rand = new Random();
    
    public HttpSession() {
        cookieJar = new HashMap();
    }
    
    Arg[] argDefinitions = {
	new Arg( ARG_FILENAME, 
		 "name of the traces file to load",
		 Arg.ARG_STRING,
		 true,
		 null ),
	new Arg( ARG_SERVER_NAME,
		 "name of the server to generate load against",
		 Arg.ARG_STRING,
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
     * @see roc.loadgen.Session#getArguments()
     */
    public Arg[] getArguments() {
	return argDefinitions;
    }
    
    public void run()
    {
	int loops = 0;

	while(true)
	    {
		try {
		    while (reader.hasNext()) {
			Request req = getNextRequest();
			Response resp = null;
			try {
			    resp = engine.getHeadRequestInterceptor().invoke(req, this);
			}
			catch (AbortRequestException ex) {
			    engine.logError(ex.getMessage(), this);
			}
			processResponse(req, resp);
		    }
		    engine.logInfo("FINISHED LOOP " + loops + " of " + numLoops, this);
		    if(++loops >= numLoops) 
			{
			    engine.logInfo("TERMINATING", this);
			    return;
			}
		}
		catch (AbortSessionException ex) {
		    engine.logError(ex.getMessage(), this);
		}
		catch (Exception ex) {
		    engine.logError(ex.getMessage(), this);
		}
	    }
    }
    

    public void config()
    {
        String tracefile = (String)args.get(ARG_FILENAME);
	serverName = (String)args.get( ARG_SERVER_NAME );
	serverPort = ((Integer)args.get( ARG_SERVER_PORT )).intValue();
	numLoops = ((Integer)args.get( ARG_NUM_LOOPS )).intValue();
	reader = engine.getTraceReader(tracefile);
    }

    public void resetSession() {
        cookieJar.clear();
        reader.reset();
    }
    
    /**
     * replace all instances of 'search' in 's' with 'replace'
     *
     */
    protected String replaceString(String s, String search, String replace) {
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
    protected String doVariableReplacement(String s) {
	if (s.indexOf("$NEWUSER") != -1) {
	    currentUserId = "randomUser" + rand.nextInt();
            s = replaceString(s, "$NEWUSER", currentUserId);
        }

        if (s.indexOf("$NEWPASSWORD") != -1) {
            currentPassword = "genericpassword";
            s = replaceString(s, "$NEWPASSWORD", currentPassword);
        }

        if (s.indexOf("$USER") != -1) {
            s = replaceString(s, "$USER", currentUserId);
        }

        if (s.indexOf("$PASSWORD") != -1) {
            s = replaceString(s, "$PASSWORD", currentPassword);
        }

        return s;
    }

    protected void readCookieHeaders(Map headers) {

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

    protected String generateCookieHeader() {
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

    protected void handleCommands(List commands)
    {
	Iterator iter = commands.iterator();

	while(iter.hasNext())
	    {
		String command = (String)iter.next();
		handleCommand(command);
	    }
    }

    protected void handleCommand(String command)
    {
	if(command.equalsIgnoreCase("$resetcookies"))
	    {
		cookieJar.clear();
		engine.logInfo("cleared cookie jar");
	    }
	else if (command.equalsIgnoreCase("$resetsession"))
	    {
		resetSession();
	    }
    }
    
    private void specifyData(HttpSessionAction action) {

	Map sessionSpecific = action.getSessionData();
	Iterator iter = sessionSpecific.keySet().iterator();
	
	while(iter.hasNext())
	    {
		String key = (String)iter.next();
		if(key.equalsIgnoreCase("postdata"))
		    {
			String pdata = doVariableReplacement((String)sessionSpecific.get(key));
			action.setPostData(pdata);
		    }
		else if (key.equalsIgnoreCase("file"))
		    {
			String file = doVariableReplacement((String)sessionSpecific.get(key));
			action.setFile(file);
		    }
		else if (key.equalsIgnoreCase("cookie"))
		    {
			action.addHeader("cookie", generateCookieHeader());
		    }
	    }
    }

    public Request getNextRequest() throws AbortSessionException {

	HttpSessionAction action = (HttpSessionAction)reader.getNextAction();
	Request req;

	do {
	    handleCommands(action.getCommands()); 
	    if(action.containsRequest())
		{
		    specifyData(action);
		    req = action.getRequest(serverName, serverPort);
		    
		    return req;
		}
	    action = (HttpSessionAction)reader.getNextAction();
	}
	while(!action.containsRequest());

	return null;
    }

    public void processResponse(Request req, Response resp) {
        HttpResponse httpResp = (HttpResponse)resp;
        readCookieHeaders(httpResp.getHeaders());
        // add more processing here as required...
    }
}
