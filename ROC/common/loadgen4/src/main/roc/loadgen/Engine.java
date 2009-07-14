package roc.loadgen;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import swig.util.ByteHelper;
import swig.util.XMLException;
import swig.util.StringHelper;
import swig.util.XMLHelper;
import swig.util.XMLStructs;

public class Engine implements Runnable {

    String name;
    int enginenum;

    Map attrs;
    Session session;
    RequestInterceptor headRequestInterceptor;

    List subEngines;

    boolean debug = false;

    public Engine() {
	enginenum=0;
	attrs = new HashMap();
        subEngines = new LinkedList();
    }

    public int getEngineNum() {
	return enginenum;
    }

    public void configure(String cmdLineArgs[]) throws AbortSessionException {

	if( cmdLineArgs.length == 0 || 
	    cmdLineArgs[0].equals( "--help" ) || 
	    cmdLineArgs[0].equals( "-H") ) {
	    System.err.println( "Usage: roc.loadgen.Engine configfile [log=dir] [option1=value [option2=value]...]" );
	    System.exit(0);
	}

        String configurationFileName = cmdLineArgs[0];

        // parse options
        for (int i = 1; i < cmdLineArgs.length; i++) {
            int idx = cmdLineArgs[i].indexOf("=");
            String key = cmdLineArgs[i].substring(0, idx);
            String value = cmdLineArgs[i].substring(idx + 1);
            attrs.put(key, value);
        }

	// special-case the log attr
	String log = (String)attrs.get("log");
	if( log == null )
	    attrs.put("log","logs");

	if( attrs.containsKey( "debug" ))
	    debug = true;
	
	logInfo("Log Directory is: " + log );

        loadXMLConfiguration(configurationFileName, false);
    }

    void loadXMLConfiguration(Element config, boolean sessionrequired )
        throws XMLException, IllegalPluginException, AbortSessionException {

        NodeList loadElements = XMLHelper.GetChildrenByTagName(config, "load");
        for (int i = 0; i < loadElements.getLength(); i++) {
            Element loadConfig = (Element)loadElements.item(i);
            // create a new child engine; configure it.

	    name = loadConfig.getAttribute("name");

            String sNumCopies = loadConfig.getAttribute("copies");
            int numCopies = 1;

            if (sNumCopies != null) {
		String val = sNumCopies;

		if( sNumCopies.startsWith("$") ) {
		    String key = sNumCopies.substring(1);
		    val = (String)getAttr(key);
		    if( val == null ) {
			throw new AbortSessionException( "Required command-line argument '" + key + "' was not specified" );
		    }
		}

                numCopies = Integer.parseInt(val);
            }

	    // initialize numCopies engines
            for (int c = 0; c < numCopies; c++) {
                Engine eng = new Engine();
		eng.enginenum=c;
		eng.attrs.putAll( attrs );
                eng.loadXMLConfiguration(loadConfig,true);
                subEngines.add(eng);
            }
        }

        Element sessionEl = XMLHelper.GetChildElement(config, "session");
	if( sessionEl != null ) {
	    String sessionId = 
		XMLHelper.GetChildText(sessionEl, "id");
	    String sessionClass = 
		XMLHelper.GetChildText(sessionEl, "classname");
	    Map sessionArgs = XMLStructs.ParseMap(sessionEl, "arg");
	    loadSession(sessionId, sessionClass, sessionArgs);
	}
	else if( sessionrequired ) {
	    throw new AbortSessionException( "<session> missing in engine configuration!" );
	}

        NodeList interceptorElements =
            XMLHelper.GetChildrenByTagName(config, "interceptor");
        for (int i = 0; i < interceptorElements.getLength(); i++) {
            Element interceptorEl = (Element)interceptorElements.item(i);
            String interceptorId = XMLHelper.GetChildText(interceptorEl, "id");
            String interceptorClass =
                XMLHelper.GetChildText(interceptorEl, "classname");
            Map interceptorArgs = XMLStructs.ParseMap(interceptorEl, "arg");
            loadInterceptor(interceptorId, interceptorClass, interceptorArgs);
        }

    }

    public void loadXMLConfiguration(String configurationFileName,
				     boolean sessionrequired )
        throws AbortSessionException {

        try {
            String configurationString =
                StringHelper.loadString(
                    new FileInputStream(new File(configurationFileName)));

            Element configXML =
                XMLHelper.GetDocumentElement(configurationString);

            loadXMLConfiguration(configXML, sessionrequired);
        }
        catch (XMLException ex) {
            throw new AbortSessionException("Error parsing XML", ex);
        }
        catch (IOException ex) {
            throw new AbortSessionException("IO Error", ex);
        }
        catch (IllegalPluginException ex) {
            throw new AbortSessionException("Could not load plugin", ex);
        }
    }

    void loadSession(String id, String classname, Map args)
        throws IllegalPluginException, AbortSessionException {
        try {
            Class sessionBase = Class.forName("roc.loadgen.Session");
            Class sessionClass = Class.forName(classname);

            if (!(sessionBase.isAssignableFrom(sessionClass))) {
                throw new IllegalPluginException(
                    classname + " is not a roc.loadgen.Session");
            }

            session = (Session)sessionClass.newInstance();

	    // parse arguments according to arg definitions
	    Arg[] argDefs = session.getArguments();
	    for( int i=0; (argDefs != null) && (i<argDefs.length); i++ ) {
		String n = argDefs[i].name;
		Object v = argDefs[i].parseArgument( (String)args.get( n ),
						     this );
		args.put( n, v );
	    }

            session.init(id, args, this);
            session.start();
        }
        catch (IllegalPluginException ex) {
            throw ex;
        }
        catch (AbortSessionException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new IllegalPluginException(ex);
        }
    }

    /**
     * loads an interceptor plugin, and prepends it to the chain of interceptors...
     * 
     * @param classname
     * @param args
     * @throws IllegalPluginException
     */
    void loadInterceptor(String id, String classname, Map args)
        throws IllegalPluginException, AbortSessionException {

        try {
            Class interceptorBase =
                Class.forName("roc.loadgen.RequestInterceptor");
            Class interceptorClass = Class.forName(classname);

            if (!(interceptorBase.isAssignableFrom(interceptorClass))) {
                throw new IllegalPluginException(
                    classname + " is not a roc.loadgen.RequestInterceptor ");
            }

            RequestInterceptor interceptor =
                (RequestInterceptor)interceptorClass.newInstance();

	    // parse arguments according to arg definitions
	    Arg[] argDefs = interceptor.getArguments();
	    for( int i=0; (argDefs != null) && (i<argDefs.length); i++ ) {
		String n = argDefs[i].name;
		Object v = argDefs[i].parseArgument( (String)args.get( n ),
						     this );
		args.put( n, v );
	    }

            interceptor.init(id, args, this);

	    if( headRequestInterceptor == null ) {
		headRequestInterceptor = interceptor;
	    }
	    else {
		// get the last interceptor in the chain
		RequestInterceptor tailInterceptor = headRequestInterceptor;
		while( tailInterceptor.getNextRequestInterceptor() != null ) {
		    tailInterceptor = tailInterceptor.getNextRequestInterceptor();
		}
		tailInterceptor.setNextRequestInterceptor( interceptor );
		interceptor.setPrevRequestInterceptor( tailInterceptor );
	    }

            interceptor.start();
        }
        catch (IllegalPluginException ex) {
            throw ex;
        }
        catch (AbortSessionException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new IllegalPluginException(ex);
        }

    }

    public String getAttr(String key) {
        return (String)attrs.get(key);
    }

    public void setAttr(String key, String val) {
        attrs.put(key, val);
    }


    public synchronized void logData(String[] id, String filename, byte[] data ) {
        try {
            String logdir = (String)attrs.get("log");

	    if(logdir==null) {
		logWarning("command-line argument 'log' directory missing.  using default './log'");
		logdir = "log";
	    }
        
	    for(int i=0; i<id.length; i++ ) {
		logdir += '/' + id[i];
	    }

        
	    ensureLogDirExists(logdir + "/" );

	    filename = sanitizeFilename(filename);

	    File file = new File( logdir + "/" + filename );
	    if( data != null ) {
		ByteHelper.SaveByteArray(data,file);
	    }
	    else {
		file.createNewFile();
	    }
        }
        catch( IOException e ) {
            logError("Could not save log data to disk", e );
        }
    }
    
    private void ensureLogDirExists(String dir) throws IOException {
	logInfo("Ensuring that '" + dir + "' exists" );
        File d = new File(dir);
        d.mkdirs();
    }

    private String sanitizeFilename( String filename ) {
	StringBuffer buf = new StringBuffer( filename );

	int qidx = buf.lastIndexOf( "?" );
	if( qidx >0)
	    buf.delete( qidx, buf.length() );

	while( true ) {
	    int idx = buf.indexOf( "/" );
	    if( idx == -1 )
		break;

	    buf.replace(idx,idx+1, "_" );
	}

	return buf.toString();
    }
    
    public synchronized void logDebug(String s) {
	if( debug )
	    System.err.println( "[INFO] (" + name + ":" + enginenum + ") " + s);
    }

    public synchronized void logInfo(String s) {
        // do something nicer here...
        System.err.println( "[INFO] (" + name + ":" + enginenum + ") " + s);
    }

    public synchronized void logWarning(String s) {
        // do something nicer here...
        System.err.println( "[WARN] (" + name + ":" + enginenum + ") " + s);
    }

    public synchronized void logError(String s) {
        // do something nicer here...
        System.err.println( "[ERROR] (" + name + ":" + enginenum + ") " + s);
	if( s == null || "null".equals(s)) {
	    (new Exception()).printStackTrace();
	}
    }
    
    public synchronized void logError(String s, Throwable t ) {
        // do something nicer here...
	System.err.println( "[ERROR] (" + name + ":" + enginenum + ") " + s);
        t.printStackTrace();
    }
    

    public synchronized void logStats(String s) {
        // do something nicer here...
	System.err.println( "[STATS] (" + name + ":" + enginenum + ") " + s);
    }

    public synchronized void setHeadRequestInterceptor(RequestInterceptor ri) {
        headRequestInterceptor = ri;
    }

    public synchronized RequestInterceptor getHeadRequestInterceptor() {
        return headRequestInterceptor;
    }

    void fork() {
        Thread w = new Thread(this);
        w.start();
    }

    void startChildren() {
        Iterator children = subEngines.iterator();
        while (children.hasNext()) {
            ((Engine)children.next()).fork();
        }
    }

    public void run() {

        startChildren();

	if( session == null )
	    return;

        if (getHeadRequestInterceptor() == null) {
            logError("No RequestInterceptor is set! we're not configured correctly; ABORT!");
            return;
        }

	do {

	    try {
		while (true) {
		    Request req = session.getNextRequest();
		    Response resp = null;
		    try {
			resp = getHeadRequestInterceptor().invoke(req);
		    }
		    catch (AbortRequestException ex) {
			logError("Abort Request Exception:" + ex.getMessage());
			ex.printStackTrace();
		    }
		    session.processResponse(req, resp);
		}
	    }
	    catch (AbortSessionException ex) {
		logError("Abort Session: " + ex.getMessage());
		ex.printStackTrace();
	    }
	    catch (Exception ex) {
		logError("Got Exception: "+ex.getMessage());
		ex.printStackTrace();
	    }
	}
	while( session.resetSession() );

    }

    public static void main(String[] args) {

        try {
            Engine engine = new Engine();
            engine.configure(args);
            engine.fork();
        }
        catch (Exception ex) {
            System.err.println("FATAL ERROR");
            ex.printStackTrace();
        }

    }

}
