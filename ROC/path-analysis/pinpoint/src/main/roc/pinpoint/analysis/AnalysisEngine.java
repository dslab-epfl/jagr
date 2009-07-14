package roc.pinpoint.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import swig.util.StringHelper;
import swig.util.XMLException;
import swig.util.XMLHelper;
import swig.util.XMLStructs;

/**
 * @author emrek
 *
 *  AnalysisEngine is the Pinpoint's core on-line analysis server.
 *  It keeps track of RecordCollections (which store arbitrary Java
 *  objects, including Observations and analysis results), and Plugins
 *  (which manipulate record collections and basically do whatever they
 *  want). 
 * 
 *  Configuration file is specified at the command line.
 * 
 *  Core plugins include:
 *    - The JMS Observation Listener.  Retrieves observations from
 *       client programs
 *    - AgeThreshold eviction plugin.  Cleans out old records from 
 *      record collections
 *    - Http Front End plugin.  Allows inspection of plugins and record
 *      collections via an http/html interface
 *    - 
 */
public class AnalysisEngine {

    Map engineArguments;

    private Map plugins;

    private Map recordCollections;

    private List engineListeners;


    /**
     * Default Constructor.
     */
    public AnalysisEngine() {
        plugins = new HashMap();
        recordCollections = new HashMap();
	engineListeners = new LinkedList();
	engineArguments = new HashMap();
    }

    public synchronized void addAnalysisEngineListener( AnalysisEngineListener l ) {
	engineListeners.add( l );
    }

    public synchronized void removeAnalysisEngineListener( AnalysisEngineListener l ) {
	engineListeners.remove( l );
    }

    /**
     * 
     * @param name  The name of the new record collection
     * @param attrs    Any attributes that should be associated with the record
     * collection (e.g., which eviction policy to use, etc.)
     * @return RecordCollection The newly created record collection
     */
    public RecordCollection createRecordCollection(String name, Map attrs) {
        RecordCollection ret = new RecordCollection(name, attrs);
        recordCollections.put(name, ret);

	Iterator iter = engineListeners.iterator();
	while( iter.hasNext() ) {
	    AnalysisEngineListener l = (AnalysisEngineListener)iter.next();
	    try {
		l.recordCollectionCreated( this, name );
	    }
	    catch( Throwable t ) {
		t.printStackTrace();
		System.err.println( "[AnalysisEngine]: Exception ignored. Continuing..." );
	    }
	}

        return ret;
    }

    /**
     * @return Map  an umodifiable map of all record collections, indexed by
     * name
     */
    public Map getAllRecordCollections() {
        return Collections.unmodifiableMap(recordCollections);
    }

    /**
     * @param name   name of a record collection
     * @return RecordCollection  the record collection, null if collection is
     * not found
     */
    public RecordCollection getRecordCollection(String name) {
        return (RecordCollection) recordCollections.get(name);
    }

    /**
     * loads and starts a plugin
     * @param id    unique identifier to be used for this plugin 
     * @param classname  the plugin's classname
     * @param args  a map of arguments to pass to the plugin
     * @return Plugin  the newly created plugin
     * @throws IllegalPluginException the specified class is not a plugin
     * @throws PluginException plugin was unable to start
     */
    public Plugin loadPlugin(String id, String classname, Map args)
        throws IllegalPluginException, PluginException {
        try {
            Class pluginInterface =
                Class.forName("roc.pinpoint.analysis.Plugin");
            Class pluginClass = Class.forName(classname);

            if (!(pluginInterface.isAssignableFrom(pluginClass))) {
                throw new IllegalPluginException(classname + " not a plugin");
            }

            Plugin plugin = (Plugin) pluginClass.newInstance();

            plugins.put(id, plugin);

	    PluginArg[] pluginArgs = plugin.getPluginArguments();
	    for( int i=0; (pluginArgs != null) && i<pluginArgs.length; i++ ) {
		String n = pluginArgs[i].name;
	        Object v = pluginArgs[i].parseArgument( (String)args.get( n ), 
							this );
		args.put( n, v );
	    }

            plugin.start(id, args, this);

	    Iterator iter = engineListeners.iterator();
	    while( iter.hasNext() ) {
		AnalysisEngineListener l = (AnalysisEngineListener)iter.next();
		try {
		    l.pluginLoaded( this, id );
		}
		catch( Throwable t ) {
		    t.printStackTrace();
		    System.err.println( "[AnalysisEngine]: Exception ignored. Continuing..." );
		}
	    }

            return plugin;
        }
        catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            throw new IllegalPluginException(
                "Class " + classname + " not found");
        }
        catch (IllegalAccessException iae) {
            iae.printStackTrace();
            throw new IllegalPluginException("Illegal Access: " + classname);
        }
        catch (InstantiationException ie) {
            ie.printStackTrace();
            throw new IllegalPluginException(
                "Could not instantiate plugin " + classname);
        }
    }

    /**
     * 
     * @return Map an unmodifiable map of all plugins, indexed by identifier
     */
    public Map getAllPlugins() {
        return Collections.unmodifiableMap(plugins);
    }

    /**
     *  
     * @param pluginId  an identifier of a loaded plugin
     * @return Plugin the plugin requested, null if the plugin is not found.
     */
    public Plugin getPlugin(String pluginId) {
        return (Plugin) plugins.get(pluginId);
    }

    /**
     * stops and unloads a plugin.
     * @param pluginId  the identifier of a loaded plugin
     * @throws PluginException the plugin generated an error while stopping.
     */
    public void unloadPlugin(String pluginId) throws PluginException {
        Plugin plugin = (Plugin) plugins.remove(pluginId);
        if (plugin != null) {
            plugin.stop();
        }

	Iterator iter = engineListeners.iterator();
	while( iter.hasNext() ) {
	    AnalysisEngineListener l = (AnalysisEngineListener)iter.next();
	    try {
		l.pluginUnloaded( this, pluginId );
	    }
	    catch( Throwable t ) {
		t.printStackTrace();
		System.err.println( "[AnalysisEngine]: Exception ignored. Continuing..." );
	    }
	}
    }

    /**
     * 
     * @param args command-line arguments to AnalysisEngine
     * @throws IOException  an I/O error occurred while loading the
     * configuration file
     * @throws XMLException  an XMLException occured while parsing the
     * configuration file
     */
    public void configure( String[] cmdLineArgs )
        throws IOException, XMLException {

	String configurationFileName = cmdLineArgs[0];

	// parse options
	for( int i=1; i<cmdLineArgs.length; i++ ) {
	    int idx= cmdLineArgs[i].indexOf( "=" );
	    String key = cmdLineArgs[i].substring( 0, idx );
	    String value = cmdLineArgs[i].substring( idx+1 );
	    engineArguments.put( key, value );
	}

        String configurationString =
            StringHelper.loadString(
                new FileInputStream(new File(configurationFileName)));

        Element configXML = XMLHelper.GetDocumentElement(configurationString);

        System.out.println("Loading collections...");

        NodeList collectionElements =
            XMLHelper.GetChildrenByTagName(configXML, "collection");
        for (int i = 0; i < collectionElements.getLength(); i++) {
            Element collectionElement = (Element) collectionElements.item(i);
            String name = collectionElement.getAttribute("name");
            Map attrs = XMLStructs.ParseMap(collectionElement, "attribute");

            String l = "    creating collection: " + name;
            System.out.print(l);

            int llength = l.length();
            if (l.length() > 60) {
                System.err.println("");
                llength = 0;
            }
            for (int j = llength; j < 60; j++) {
                System.out.print(" ");
            }

            try {
                createRecordCollection(name, attrs);
                System.out.println("[ OK ]");
            }
            catch (Exception e) {
		e.printStackTrace();
                System.out.println("[ FAILED: " + e.getMessage() + "]");
            }
        }

        System.out.println("Loading plugins...");

        NodeList pluginElements =
            XMLHelper.GetChildrenByTagName(configXML, "plugin");
        for (int i = 0; i < pluginElements.getLength(); i++) {
            Element pluginElement = (Element) pluginElements.item(i);
            String name = pluginElement.getAttribute("name");
            String classname =
                XMLHelper.GetChildText(pluginElement, "classname");
            Map args = XMLStructs.ParseMap(pluginElement, "arg");

            String l = "    loading plugin: " + name + "(" + classname + ")";
            int llength = l.length();

            System.out.print(l);

            try {
                loadPlugin(name, classname, args);

                if (l.length() > 60) {
                    System.err.println("");
                    llength = 0;
                }
                for (int j = llength; j < 60; j++) {
                    System.out.print(" ");
                }
                System.out.println("[ OK ]");
            }
	    catch( PluginException e ) {
                System.out.println("[ FAILED: " + e.getMessage() + "]");
	    }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("[ FAILED: " + e.getMessage() + "]");
            }
        }
    }

    /**
     * Main function.  Initializes AnalysisEngine and configures it with a
     * configuration file, whose name is passed in via the command line
     * @param args command line arguments
     */
    public static void main(String[] args) {

        try {
            if (args.length < 1) {
                System.err.println(
                    "usage: roc.pinpoint.analysis.AnalysisEngine"
                    + " [configuration file] [options]\n" 
		    + "\n\n\t"  
		    + "options are configuration-specific key-value pairs\n"
		    + "\te.g., key=value key2=value2\n" );
                return;
            }

            AnalysisEngine engine = new AnalysisEngine();
            try {
                engine.configure(args);
            }
            catch (FileNotFoundException fnfe) {
                System.out.println(
                    "Could not find configuration file: " + fnfe.getMessage());
            }
            catch (IOException ioe) {
                System.out.println(
                    "IOException while reading configuration file : "
                        + ioe.getMessage());
            }
            catch (XMLException xmle) {
                System.out.println(
                    "Problem parsing XML configuration file: "
                        + xmle.getMessage());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
