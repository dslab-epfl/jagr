package roc.loadgen;

import java.util.*;
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

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

public class Engine implements Runnable 
{
    private static boolean configPrinted = false;
    static Logger log = Logger.getLogger( "Engine" );
    static List engines = Collections.synchronizedList( new LinkedList() );

    String name;
    int enginenum;

    Map attrs;
    User user;
    RequestInterceptor headRequestInterceptor;

    long stopTime;

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

    public void configure(String cmdLineArgs[]) throws InitializationException {

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

	// special-case the logg attr
	String logg = (String)attrs.get("log");
	if( logg == null )
	    attrs.put("log","logs");

        // special-case handling for "runtime"
        String runtime = (String)attrs.get("runtime");
        if( runtime == null ) {
            runtime = "3600000"; // default runtime is a huge 60 min
        }

        stopTime = System.currentTimeMillis() + 
            Long.parseLong( runtime );
	

	if( attrs.containsKey( "debug" ))
	    debug = true;
	
	if( logg != null)
	    log.info("Log Directory is: " + logg );

        loadXMLConfiguration(configurationFileName, false);
    }

    void loadXMLConfiguration(Element config, boolean userrequired )
        throws XMLException, IllegalPluginException, InitializationException {

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
			throw new InitializationException( "Required command-line argument '" + key + "' was not specified" );
		    }
		}

                numCopies = Integer.parseInt(val);
		log.info( "Forking " + numCopies + " clients" );
            }

	    // initialize numCopies engines
            for (int c = 0; c < numCopies; c++) {
                Engine eng = new Engine();
		eng.stopTime = this.stopTime;
		eng.enginenum=c;
		eng.attrs.putAll( attrs );
                eng.loadXMLConfiguration(loadConfig,true);
                subEngines.add(eng);
            }
        }

        Element userEl = XMLHelper.GetChildElement(config, "user");
	if( userEl != null ) {
	    String userId = 
		XMLHelper.GetChildText(userEl, "id");
	    String userClass = 
		XMLHelper.GetChildText(userEl, "classname");
	    Map userArgs = XMLStructs.ParseMap(userEl, "arg");
	    loadUser(userId, userClass, userArgs);
	}
	else if( userrequired ) {
	    throw new InitializationException( "<user> missing in engine configuration!" );
	}

        NodeList interceptorElements =
            XMLHelper.GetChildrenByTagName(config, "interceptor");

	boolean doPrint = false;
	if( !configPrinted )
	{
	    doPrint = true;
	    configPrinted = true;
	}

        for (int i = 0; i < interceptorElements.getLength(); i++) {
            Element interceptorEl = (Element)interceptorElements.item(i);
            String interceptorId = XMLHelper.GetChildText(interceptorEl, "id");
            String interceptorClass =
                XMLHelper.GetChildText(interceptorEl, "classname");
            Map interceptorArgs = XMLStructs.ParseMap(interceptorEl, "arg");
            loadInterceptor(interceptorId, interceptorClass, interceptorArgs);
	    if( doPrint )
		log.info( "Using " + interceptorClass );
        }

    }

    public void loadXMLConfiguration(String configurationFileName,
				     boolean userrequired )
        throws InitializationException {

        try {
            String configurationString =
                StringHelper.loadString(
                    new FileInputStream(new File(configurationFileName)));

            Element configXML =
                XMLHelper.GetDocumentElement(configurationString);

            loadXMLConfiguration(configXML, userrequired);
        }
        catch (XMLException ex) {
            throw new InitializationException("Error parsing XML", ex);
        }
        catch (IOException ex) {
            throw new InitializationException("IO Error", ex);
        }
        catch (IllegalPluginException ex) {
            throw new InitializationException("Could not load plugin", ex);
        }
    }

    void loadUser(String id, String classname, Map args)
        throws IllegalPluginException, InitializationException {
        try {
            Class userBase = Class.forName("roc.loadgen.User");
            Class userClass = Class.forName(classname);

            if (!(userBase.isAssignableFrom(userClass))) {
                throw new IllegalPluginException(
                    classname + " is not a roc.loadgen.User");
            }

            user = (User)userClass.newInstance();

	    // parse arguments according to arg definitions
	    Arg[] argDefs = user.getArguments();
	    for( int i=0; (argDefs != null) && (i<argDefs.length); i++ ) {
		String n = argDefs[i].name;
		Object v = argDefs[i].parseArgument( (String)args.get( n ),
						     this );
		args.put( n, v );
	    }

            user.init(id, args, this);
            user.start();
        }
        catch (IllegalPluginException ex) {
            throw ex;
        }
        catch (InitializationException ex) {
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
        throws IllegalPluginException, InitializationException {

        try {
            Class interceptorBase =
                Class.forName("roc.loadgen.RequestInterceptor");
            Class interceptorClass = Class.forName(classname);

            if (!(interceptorBase.isAssignableFrom(interceptorClass))) {
                throw new IllegalPluginException(
                    classname + " is not a roc.loadgen.RequestInterceptor ");
            }

	    RequestInterceptor interceptor;
	    try {
		interceptor = (RequestInterceptor)interceptorClass.newInstance();
	    }
	    catch( Exception e )
	    {
		log.fatal( "Could not instantiate interceptor" );
		e.printStackTrace();
		throw e;
	    }

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
        catch (InitializationException ex) {
            throw ex;
        }
        catch (Exception ex) {
	    log.fatal( "Got exception while loading interceptor" );
	    ex.printStackTrace();
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
		log.warn("command-line argument 'log' directory missing.  using default './log'");
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
            log.error("Could not save log data to disk", e );
        }
    }
    
    private void ensureLogDirExists(String dir) throws IOException {
	log.info("Ensuring that '" + dir + "' exists" );
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

    boolean timeToStop() {
        long currTime = System.currentTimeMillis();
        return ( currTime > stopTime );
    }

    void stopAllInterceptors() {
        RequestInterceptor ri = getHeadRequestInterceptor();
        while( ri != null ) {
            ri.stop();
            ri = ri.getNextRequestInterceptor();
        }
    }

    public void run() {

	engines.add( (Object) this );

        startChildren();

	if( user == null )
	    return;

        if (getHeadRequestInterceptor() == null) {
            log.error("No RequestInterceptor is set! we're not configured correctly; ABORT!");
            return;
        }

	do {

	    try {
		while ( !timeToStop() ) {

		    Request req = user.getNextRequest();
		    req.setEngineId(getEngineNum());
		    Response resp = null;
		    try {
			resp = getHeadRequestInterceptor().invoke(req);
		    }
		    catch (AbortRequestException ex) {
			log.error("Abort Request Exception:" + ex.getMessage());
			ex.printStackTrace();
		    }
		    user.processResponse(req, resp);
		}
	    }
	    catch (Exception ex) {
		log.error("Got Exception: "+ex.getMessage());
		ex.printStackTrace();
	    }
	}
	while( !timeToStop() ); // while( user.resetSession() );

        stopAllInterceptors();

    }


    public static void main(String[] args) {

        try {
	    String roctop = System.getProperty( "ROC_TOP" ); // temporary hack
	    PropertyConfigurator.configure( roctop + "/jagr/loadgen/conf/log4j.cfg" );
            Engine engine = new Engine();
	    Runtime.getRuntime().addShutdownHook( new Thread( new Shutdown() ) );
            engine.configure(args);
            engine.fork();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    // The following code cleans up upon Ctrl-C etc.
    private static class Shutdown implements Runnable
    {
	public void run()
	{
	    log.info( "Shutdown hook invoked. " );
	    for( Iterator it = engines.iterator(); it.hasNext() ; )
	    {
		((Engine) it.next()).stopAllInterceptors();
	    }
	}
    }
    
}
