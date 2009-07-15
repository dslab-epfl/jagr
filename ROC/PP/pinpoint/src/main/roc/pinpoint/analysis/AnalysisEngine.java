/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.analysis;

// marked for release 1.0

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

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
 *  <p>
 *  Configuration file is specified at the command line.
 *  
 */
public class AnalysisEngine {

    static Logger log = Logger.getLogger( "AnalysisEngine" );

    Map engineArguments;

    private Map plugins;

    private Map recordCollections;

    private List engineListeners;

    private String name; // name of this AnalysisEngine
    private Map namespaces; // Map of embedded AnalysisEngines :)


    URL configRoot;

    /**
     * Default Constructor.
     * @param name an identifier for this engine
     */
    public AnalysisEngine( String name ) {
	this.name = name;
        plugins = new HashMap();
        recordCollections = new HashMap();
        engineListeners = new LinkedList();
        engineArguments = new HashMap();
        namespaces = new HashMap();
    }

    public String getName() {
	return name;
    }


    /**
     * @return the URL where the configuration for this anlaysis
     * engine is located.  If this analysis engine was created to
     * represent a namespace within another analysis engine and does
     * not have its own configuration file, it may return the URL to
     * its parent's configuration
     */
    public URL getConfigRoot() {
	return configRoot;
    }

    /**
     * register a listener for changes in this namespace.  Listeners will
     * be notified when record collections are created and deleted.
     *
     */
    public synchronized void addAnalysisEngineListener(AnalysisEngineListener l) {
        engineListeners.add(l);
    }

    /**
     * unregisters a listener for changes in this namespace.
     */
    public synchronized void removeAnalysisEngineListener(AnalysisEngineListener l) {
        engineListeners.remove(l);
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
        synchronized( this ) {
            recordCollections.put(name, ret);
        }

        Iterator iter = engineListeners.iterator();
        while (iter.hasNext()) {
            AnalysisEngineListener l = (AnalysisEngineListener) iter.next();
            try {
                l.recordCollectionCreated(this, name);
            }
            catch (Throwable t) {
                t.printStackTrace();
		log.warn("Exception ignored. Continuing...");
            }
        }

        return ret;
    }

    /**
     * binds an existing record collection to a given name in this
     * analysis engine's namespace
     */
    public void bindRecordCollection(String name, RecordCollection rc) {
        synchronized( this ) {
            recordCollections.put(name, rc);
        }
        // todo: notify enginelisteners
    }

    /**
     * @return Map  a (copy) map of all record collections, indexed by
     * name
     */
    public Map getAllRecordCollections() {
        // return a c
        return new HashMap( recordCollections );
    }

    /**
     * @param name   name of a record collection
     * @return RecordCollection  the record collection, null if collection is
     * not found
     */
    public RecordCollection getRecordCollection(String name) {
        synchronized( this ) {
            return (RecordCollection) recordCollections.get(name);
        }
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
            for (int i = 0;
                (pluginArgs != null) && i < pluginArgs.length;
                i++) {
                String n = pluginArgs[i].name;
                Object v =
                    pluginArgs[i].parseArgument((String) args.get(n), this);
                args.put(n, v);
            }

            plugin.start(id, args, this);

            Iterator iter = engineListeners.iterator();
            while (iter.hasNext()) {
                AnalysisEngineListener l = (AnalysisEngineListener) iter.next();
                try {
                    l.pluginLoaded(this, id);
                }
                catch (Throwable t) {
                    t.printStackTrace();
		    log.warn("Exception ignored. Continuing...");
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
        while (iter.hasNext()) {
            AnalysisEngineListener l = (AnalysisEngineListener) iter.next();
            try {
                l.pluginUnloaded(this, pluginId);
            }
            catch (Throwable t) {
                t.printStackTrace();
		log.warn("Exception ignored. Continuing...");
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
    public void configure(String[] cmdLineArgs)
        throws XMLException, IOException, AnalysisException {

        String configurationFileName = cmdLineArgs[0];

	log.info( "Loading Configuration file: " + configurationFileName );

        // parse options
        for (int i = 1; i < cmdLineArgs.length; i++) {
            int idx = cmdLineArgs[i].indexOf("=");
            String key = cmdLineArgs[i].substring(0, idx);
            String value = cmdLineArgs[i].substring(idx + 1);
            engineArguments.put(key, value);
        }

        loadXMLConfiguration(configurationFileName);
    }

    /**
     * initializes this analysis engine with the configuration
     * located at the given filename
     * @param configFilename filename to load
     *
     */
    public void loadXMLConfiguration(String configFilename)
        throws IOException, XMLException, AnalysisException {
        loadXMLConfiguration((new File(configFilename)).toURL());
    }

    /**
     *  initializes this analysis engine  with the configuration
     *  located at the given URL
     * @param configURL url to load configuration file from
     *  
     */
    public void loadXMLConfiguration(URL configURL)
        throws IOException, XMLException, AnalysisException {

        configRoot = configURL;

        String configurationString =
            StringHelper.loadString(configRoot.openStream());

        Element configXML = XMLHelper.GetDocumentElement(configurationString);

        loadSubConfigXMLConfiguration(configXML);
        loadCollectionsXMLConfiguration(configXML);
        loadPluginsXMLConfiguration(configXML);
        loadSubAnalysisXMLConfiguration(configXML);
    }

    /**
     * 
     * @param configXML
     */
    private void loadSubConfigXMLConfiguration(Element configXML)
        throws XMLException {

	log.info("Loading Subconfigurations...");

        NodeList subconfigElements =
            XMLHelper.GetChildrenByTagName(configXML, "subconfig");
        for (int i = 0; i < subconfigElements.getLength(); i++) {
            Element subconfigElement = (Element) subconfigElements.item(i);
            String filename = subconfigElement.getAttribute("file");
            String l = "    loading subconfig: " + filename;
            int llength = l.length();

            try {
                URL saveOrigConfigRoot = this.configRoot;
                this.loadXMLConfiguration(new URL(configRoot, filename));
                this.configRoot = saveOrigConfigRoot;
                if (l.length() > 60) {
                    System.err.println("");
                    llength = 0;
                }
                for (int j = llength; j < 60; j++) {
                    System.out.print(" ");
                }
                System.out.println("[ OK ]");
            }
            catch (Exception e) {
                System.out.println("[ FAILED: " + e.getMessage() + "]");
                // a failure creating the first plugins is fatal
                System.exit(-1);
            }
        }
    }

    /**
     * @param configXML
     */
    private void loadSubAnalysisXMLConfiguration(Element configXML)
        throws XMLException, MalformedURLException, IOException {

	log.info("Loading subanalysis...");

        NodeList subanalysisElements =
            XMLHelper.GetChildrenByTagName(configXML, "subanalysis");
        for (int i = 0; i < subanalysisElements.getLength(); i++) {
            Element subanalysisElement = (Element) subanalysisElements.item(i);
            String name = subanalysisElement.getAttribute("name");
            String filename = subanalysisElement.getAttribute("file");

            String l =
                "    loading subanalysis: " + name + "(" + filename + ")";
            int llength = l.length();
	    log.debug(l);

            try {
                Map subargs = XMLStructs.ParseMap(subanalysisElement, "arg");
                Iterator subargIter = subargs.keySet().iterator();
                while (subargIter.hasNext()) {
                    Object k = subargIter.next();
                    String v = (String) subargs.get(k);
                    if (v.startsWith("$")) {
                        String varName = v.substring(1);
                        Object varVal = engineArguments.get(varName);
                        if (varVal == null)
                            throw new AnalysisException(
                                "variable not found: " + v);
                        subargs.put(k, varVal);
                    }
                }

                AnalysisEngine subengine = createNameSpace(name, subargs);

                Map bindings =
                    XMLStructs.ParseMap(subanalysisElement, "bindrecord");
                Iterator bindingsIter = bindings.keySet().iterator();
                while (bindingsIter.hasNext()) {
                    String key = (String) bindingsIter.next();
                    String value = (String) bindings.get(key);
                    RecordCollection collection =
                        this.getRecordCollection(value);
                    subengine.bindRecordCollection(key, collection);
                }

                URL subengineConfigURL = new URL(configRoot, filename);
                //System.out.println( "DEBUG: root=" + configRoot.toString() + "; filename=" + filename + " -> " + subengineConfigURL );
                subengine.loadXMLConfiguration(subengineConfigURL);

		/*
		if (l.length() > 60) {
		    System.err.println("");
		    llength = 0;
		}
		for (int j = llength; j < 60; j++) {
		    System.out.print(" ");
		}
		System.out.println("[ OK ]");
		*/
            }
            catch (AnalysisException e) {
                System.out.println("[ FAILED: " + e.getMessage() + "]");
                // a failure creating the first plugins is fatal
                System.exit(-1);
            }

	    log.debug("    done loading subanalysis: " + name + "(" + filename + ")");
        }
    }

    private void loadPluginsXMLConfiguration(Element configXML)
        throws XMLException {
	log.info("Loading plugins...");

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

	    log.debug( l );

            try {
                loadPlugin(name, classname, args);

		/*
		  if (l.length() > 60) {
		  System.err.println("");
		  llength = 0;
		  }
		  for (int j = llength; j < 60; j++) {
		  System.out.print(" ");
		  }
		  System.out.println("[ OK ]");
		*/
            }
            catch (PluginException e) {
                System.out.println("[ FAILED: " + e.getMessage() + "]");
                // a failure creating the first plugins is fatal
                e.printStackTrace();
                System.exit(-1);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("[ FAILED: " + e.getMessage() + "]");
                // a failure creating the first plugins is fatal
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private void loadCollectionsXMLConfiguration(Element configXML) {
	log.info("Loading collections...");

        NodeList collectionElements =
            XMLHelper.GetChildrenByTagName(configXML, "collection");
        for (int i = 0; i < collectionElements.getLength(); i++) {
            Element collectionElement = (Element) collectionElements.item(i);
            String name = collectionElement.getAttribute("name");
            Map attrs = XMLStructs.ParseMap(collectionElement, "attribute");

            String l = "    creating collection: " + name;

	    /*
            System.out.print(l);

	      if( !log4j )
	      {
		int llength = l.length();
		if (l.length() > 60) {
		    System.err.println("");
		    llength = 0;
		}
		for (int j = llength; j < 60; j++) {
		    System.out.print(" ");
		}
	      }
	    */
	    try {
		createRecordCollection(name, attrs);
		log.debug(l);
	    }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("[ FAILED: " + e.getMessage() + "]");
                // a failure creating the first records is fatal
                System.exit(-1);
            }
        }
    }


    /**
     * creates a new namespace within the analysis engine, and returns
     * a new analysis engine instance that is scoped to the new namespace.
     * @param name an identifier for the namespace
     * @param arguments arguments to pass to the new namespace
     *
     */
    public AnalysisEngine createNameSpace(String name, Map a)
        throws AnalysisException {
        if (namespaces.containsKey(name))
            throw new AnalysisException(
                "ACK! can't create two namespaces with the same name: " + name);

	Map arguments = new HashMap( a );
	Iterator iter = arguments.keySet().iterator();
	while( iter.hasNext() ) {
	    Object k = iter.next();
	    String v = (String)arguments.get(k);
	    if( v.startsWith("$")) {
		String varName = v.substring(1);
		Object varVal = engineArguments.get(varName);
		if( varVal == null )
		    throw new AnalysisException( "could not create namespace.  Variable not found: " + varName );
		arguments.put(k,varVal);
	    }
	}

        AnalysisEngine ns = new AnalysisEngine( name );
        ns.engineArguments.putAll(arguments);

        namespaces.put(name, ns);

        return ns;
    }

    /**
     * get the analysis engine that is scoped to the given namespace
     */
    public AnalysisEngine getNameSpace(String name) {
        return (AnalysisEngine) namespaces.get(name);
    }

    /**
     * returns an iterator over the string names of all namespaces
     * within this analysis engine.
     */
    public Iterator getNameSpaceNames() {
        return namespaces.keySet().iterator();
    }

    /**
     * Main function.  Initializes AnalysisEngine and configures it with a
     * configuration file, whose name is passed in via the command line
     * @param args command line arguments
     */
    public static void main(String[] args) {

	try {
	    String roctop = System.getProperty( "ROC_TOP" ); // temporary hack
	    PropertyConfigurator.configure( roctop + "/PP/pinpoint/conf/log4j.cfg" );
	}
	catch( Exception e ) {
	    e.printStackTrace();
	    System.exit( -1 );
	}

        try {
            if (args.length < 1) {
                System.err.println(
                    "usage: roc.pinpoint.analysis.AnalysisEngine"
                        + " [configuration file] [options]\n"
                        + "\n\n\t"
                        + "options are configuration-specific key-value pairs\n"
                        + "\te.g., key=value key2=value2\n");
                return;
            }

            AnalysisEngine engine = new AnalysisEngine( "root" );
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
