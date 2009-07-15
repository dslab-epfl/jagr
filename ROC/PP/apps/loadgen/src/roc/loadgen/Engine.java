/*
 * $Id: Engine.java,v 1.8 2004/09/08 18:18:01 candea Exp $
 */

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

/**
 * Main driver for the simulated RUBiS clients.
 *
 * @version <tt>$Revision: 1.8 $</tt>
 * @author  <a href="mailto:emrek@cs.stanford.edu">Emre Kiciman</a>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * With architecture ideas from <a href="http://www.jboss.org">JBoss</a>.
 */

public class Engine implements Runnable 
{
    // Log output
    private static Logger log = Logger.getLogger( "Engine" );

    // A list of individual engines (one per simulated client)
    static List subEngines = Collections.synchronizedList( new LinkedList() );

    // Configuration attributes
    static Map attrs = null;

    // Time (in msec) at which all engines must stop
    static long stopTime;

    // This engine's identifier
    private int enginenum;

    // This engine's first request interceptor
    private RequestInterceptor headRequestInterceptor;

    // The user object simulated by this engine
    private User user;


    /**
     * Constructor.
     *
     * @param engineId an int that will become the new engine's ID
     **/
    public Engine( int engineId ) 
    {
	this.enginenum = engineId;
    }


    /**
     * Getter for engine number.
     **/
    public int getEngineNum()  { return enginenum; }


    /**
     * Parse the options from the config file and place them in the
     * global 'attrs' list, configure the stopTime, and then configure
     * all engines.  This function runs only once.
     *
     * @param cmdLineArgs array of command-line arguments
     **/
    public static void configure( String cmdLineArgs[] ) 
	throws InitializationException 
    {
	assert attrs==null;
	attrs = new HashMap();

	if( cmdLineArgs.length==0  ||  cmdLineArgs[0].equals( "--help" )  ||  cmdLineArgs[0].equals( "-H") ) 
	{
	    System.out.println( "Usage: roc.loadgen.Engine configfile [log=dir] [option1=value [option2=value]...]" );
	    System.exit(0);
	}

        String configurationFileName = cmdLineArgs[0];

        // parse options
        for (int i = 1; i < cmdLineArgs.length; i++) 
	{
            int idx = cmdLineArgs[i].indexOf("=");
            String key = cmdLineArgs[i].substring(0, idx);
            String value = cmdLineArgs[i].substring(idx + 1);
            attrs.put(key, value);
        }

        // special-case handling for "runtime"
        String runtime = (String)attrs.get("runtime");
        if( runtime == null ) 
	{
            runtime = "5000000"; // default runtime is 5 min
        }

        stopTime = System.currentTimeMillis() + Long.parseLong( runtime );
        loadXmlConfiguration( configurationFileName );
    }


    /**
     * Load in the engine configuration from an XML file.
     *
     * @param configurationFileName name of config file
     **/
    public static void loadXmlConfiguration( String configurationFileName )
        throws InitializationException 
    {
        try {
            String configurationString = StringHelper.loadString( new FileInputStream(new File(configurationFileName)) );
            Element configXML = XMLHelper.GetDocumentElement( configurationString );
            processXmlConfiguration( configXML );
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


    /**
     * Read in number of copies and setup the individual Engines
     * according to the XML config file.  At the end print out
     * information on which interceptors have been configured.
     *
     * @param config the top-level element in the XML config file
     **/
    static void processXmlConfiguration( Element config )
        throws XMLException, IllegalPluginException, InitializationException 
    {
        NodeList loadElements = XMLHelper.GetChildrenByTagName(config, "load");
	assert loadElements.getLength()==1;

	Element engineConfig = (Element)loadElements.item( 0 ); // the one and only element

	// Find out how many copies of the Engine we need
	int numCopies = Integer.parseInt( engineConfig.getAttribute("copies") );
	
	// Find classname that implements user and tell each Engine about it
	Element userEl = XMLHelper.GetChildElement(engineConfig, "user");
	assert userEl!=null;
	String userId = XMLHelper.GetChildText(userEl, "id");
	String userClass = XMLHelper.GetChildText(userEl, "classname");
	Map userArgs = XMLStructs.ParseMap(userEl, "arg");

	// Find the descriptor of interceptors
        NodeList interceptorElements = XMLHelper.GetChildrenByTagName(engineConfig, "interceptor");

	// initialize numCopies Engines
	log.info( "Forking " + numCopies + " clients" );
	Engine eng=null;
	for( int n = 0; n < numCopies; n++ ) 
	{
	    eng = new Engine( n );
	    log.debug( "Adding to subEngines engine #" + n );
	    subEngines.add( eng );
	    eng.loadUser( userId, userClass, userArgs );
	    eng.initializeInterceptorChain( interceptorElements );
	}

	// print information about interceptor configuration
	for( RequestInterceptor inter = eng.headRequestInterceptor ; inter != null ; )
	{
	    log.info( "'" + inter.getId() + "' configured" );
	    RequestInterceptor aux = inter.getNextRequestInterceptor();
	    if( aux == null ) // we have a fork, or the end of the chain
	    {
		inter = inter.getLeftInterceptor();
		if( inter != null )  // it's a fork
		    log.info( "<fork>" );
	    }
	    else
	    {
		inter = aux;
	    }
	}
    }


    /**
     * Tell an engine to initialize its interceptor chain from a list
     * of XML nodes (from the config file).
     *
     * @param elements the list of XML nodes
     **/
    public void initializeInterceptorChain( NodeList elements )
	throws XMLException, IllegalPluginException, InitializationException
    {
	headRequestInterceptor = initInterceptorChainRecursively( elements, 0 );
    }


    /**
     * Create the chain of interceptors based on a list of XML nodes.
     * This is a recursive function.
     *
     * @param elements the list of XML nodes
     * @param idx index (within the list of XML nodes) at which to start
     **/
    private RequestInterceptor initInterceptorChainRecursively( NodeList elements, int idx )
	throws XMLException, IllegalPluginException, InitializationException
    {
	// base case
	if( idx >= elements.getLength() )
	    return null;

	// extract information about this interceptor
	Element interceptorEl = (Element)elements.item(idx);
	String interceptorId    = XMLHelper.GetChildText(interceptorEl, "id");
	String interceptorClass = XMLHelper.GetChildText(interceptorEl, "classname");
	String forkStr          = XMLHelper.GetChildText(interceptorEl, "fork");
	Map interceptorArgs     = XMLStructs.ParseMap(interceptorEl, "arg");

	RequestInterceptor thisInterceptor = loadInterceptor( interceptorId, interceptorClass, interceptorArgs );
	
	// connect the interceptor into the chain
	if( forkStr == null )  // linear interceptor chain
	{
	    RequestInterceptor nextInterceptor = initInterceptorChainRecursively( elements, idx+1 );
	    if( nextInterceptor != null )
	    {
		thisInterceptor.setNextRequestInterceptor( nextInterceptor );
		nextInterceptor.setPrevRequestInterceptor( thisInterceptor );
	    }
	}
	else // forked interceptor chain
	{
	    RequestInterceptor leftInterceptor  = initInterceptorChainRecursively( elements, idx+1 );
	    RequestInterceptor rightInterceptor = initInterceptorChainRecursively( elements, idx+1 );
	    if( leftInterceptor != null )
	    {
		assert rightInterceptor!=null;
		thisInterceptor.setNextInterceptors( leftInterceptor, rightInterceptor );
		leftInterceptor.setPrevRequestInterceptor( thisInterceptor );
		rightInterceptor.setPrevRequestInterceptor( thisInterceptor );
	    }
	}

	// start this interceptor
	thisInterceptor.start();

	return thisInterceptor;
    }


    /**
     * Load and initialize an interceptor.
     *
     * @param id  a freeform identifier for the interceptor
     * @param classname  the Java class that implements the interceptor
     * @param args  the arguments to the interceptor
     **/
    RequestInterceptor loadInterceptor( String id, String classname, Map args )
        throws IllegalPluginException, InitializationException 
    {
	RequestInterceptor interceptor;

        try {
            Class interceptorBase  = Class.forName("roc.loadgen.RequestInterceptor");
            Class interceptorClass = Class.forName(classname);
	    
            if( ! interceptorBase.isAssignableFrom(interceptorClass) ) 
	    {
                throw new IllegalPluginException( classname + " is not a roc.loadgen.RequestInterceptor " );
            }

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
	    for( int i=0; (argDefs != null) && (i<argDefs.length); i++ ) 
	    {
		String n = argDefs[i].name;
		Object v = argDefs[i].parseArgument( (String)args.get( n ), this );
		args.put( n, v );
	    }
	    
            interceptor.init( id, args, this );
        }
        catch (IllegalPluginException ex) 
	{
            throw ex;
        }
        catch (InitializationException ex) 
	{
            throw ex;
        }
        catch (Exception ex) 
	{
	    log.fatal( "Got exception while loading interceptor" );
	    ex.printStackTrace();
            throw new IllegalPluginException( ex );
        }
	
	return interceptor;
    }

    
    /**
     * Load and initialize a user.
     *
     * @param id  a freeform identifier for the user
     * @param classname  the Java class that implements the user
     * @param args  the arguments to the user
     **/
    void loadUser( String id, String classname, Map args )
        throws IllegalPluginException, InitializationException 
    {
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

    public static void setAttr( String key, String val )  { attrs.put(key, val); }
    public static String getAttr(String key)  { return (String)attrs.get(key); }

    public synchronized void setHeadRequestInterceptor(RequestInterceptor ri) {
        headRequestInterceptor = ri;
    }

    public synchronized RequestInterceptor getHeadRequestInterceptor() {
        return headRequestInterceptor;
    }

    void detach_myself() 
    {
        Thread w = new Thread( this );
        w.start();
    }

    static void startChildren() 
    {
        Iterator children = subEngines.iterator();
        while( children.hasNext() ) 
	{
	    Engine e = (Engine) children.next();
	    log.debug( "--- Found engine " + e.getEngineNum() );
            e.detach_myself();
	    log.info( "#" + e.getEngineNum() + " started" );
        }
    }

    boolean timeToStop() {
        long currTime = System.currentTimeMillis();
        return ( currTime > stopTime );
    }

    public void stopAllInterceptors() 
    {
        for( RequestInterceptor ri = getHeadRequestInterceptor() ; ri != null ; ri = ri.getNextRequestInterceptor() ) 
	{
            ri.stop();
        }
    }

    /**
     * An engine's main loop: as long as it's not time to stop, 
     **/
    public void run() 
    {
	assert user != null;
	assert getHeadRequestInterceptor() != null;

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


    /**
     * The main() body.
     **/
    public static void main( String[] args ) 
    {
        try {
	    String log4jcfg = System.getProperty( "env.log4j" ); // temporary hack
	    assert !log4jcfg.equals( "null" );
	    PropertyConfigurator.configure( log4jcfg );

            Engine.configure(args);
	    Engine.startChildren();

	    Runtime.getRuntime().addShutdownHook( new Thread( new Shutdown() ) );
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * Hook to clean up upon Ctrl-C interruption.
     **/
    private static class Shutdown implements Runnable
    {
	public void run()
	{
	    log.info( "Shutdown hook invoked. " );
	    for( Iterator it = subEngines.iterator(); it.hasNext() ; )
	    {
		((Engine) it.next()).stopAllInterceptors();
	    }
	}
    }
    
}
