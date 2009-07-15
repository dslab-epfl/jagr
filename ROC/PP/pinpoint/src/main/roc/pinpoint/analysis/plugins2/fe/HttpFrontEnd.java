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
package roc.pinpoint.analysis.plugins2.fe;

// marked for release 1.0

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.AnalysisEngineListener;
import roc.pinpoint.analysis.IllegalPluginException;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import swig.httpd.HttpExportedService;
import swig.httpd.HttpService;
import swig.util.HtmlHelper;
import swig.util.HtmlUtils;
import swig.util.HttpUtils;

/**
 * this plugin implements an HTTP/HTML front end, allowing users to inspect the
 * contents of record collections, and loaded plugins.
 * @author emrek
 *
 */
public class HttpFrontEnd 
    implements Plugin, HttpExportedService, AnalysisEngineListener {

    /** service name used for identifying this http service **/
    public static final String SERVICE_NAME = "httpfe";
    /** relative url to reach this service **/
    public static final String REL_URL = "/service/" + SERVICE_NAME;

    // define arguments: portnum, num items per page, etc.
    /** argument name for http port **/
    public static final String HTTP_PORT_ARG = "httpPort";
    /** argument name for the number of records/plugins to show on a page **/
    public static final String NUM_PER_PAGE_ARG = "numPerPage";
    /** argument name for the contact name to place in the html footer **/
    public static final String CONTACT_NAME_ARG = "contactName";
    /** argument name for the contact email addr, for the html footer **/
    public static final String CONTACT_EMAIL_ARG = "contactEmail";

    PluginArg[] args = {
	new PluginArg( HTTP_PORT_ARG,
		       "http port to listen on",
		       PluginArg.ARG_INTEGER,
		       false,
		       "8888" ),
	new PluginArg( NUM_PER_PAGE_ARG,
		       "max number of items to show on a single page",
		       PluginArg.ARG_INTEGER,
		       false,
		       "50" ),
	new PluginArg( CONTACT_NAME_ARG,
		       "contact name to place in the html footer",
		       PluginArg.ARG_STRING,
		       false,
		       null ),
	new PluginArg( CONTACT_EMAIL_ARG,
		       "contact email addr to place in the html footer",
		       PluginArg.ARG_STRING,
		       false,
		       null )
    };


    static final int MODE_FRONTPAGE = 0;
    static final int MODE_PLUGINS = 1;
    static final int MODE_RECORDCOLLECTIONS = 2;

    private int httpPort;
    private int numPerPage;
    private String contactName;
    private String contactEmail;

    private HttpService httpservice;

    private AnalysisEngine rootengine;
    private AnalysisEngine engine;
    private String currentNamespace;

    private Map/*<string,HttpRecordPlugin>*/ recordPlugins;

    public PluginArg[] getPluginArguments() {
	return args;
    }


    /**
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String name, Map args, AnalysisEngine engine)
        throws PluginException {

	this.rootengine = engine;
        this.engine = engine;
	this.currentNamespace = "/";

	recordPlugins = new HashMap();
	readInitialHttpRecordPlugins();
	engine.addAnalysisEngineListener( this );	

	httpPort = ((Integer)args.get(HTTP_PORT_ARG)).intValue();
        
	numPerPage = ((Integer)args.get(NUM_PER_PAGE_ARG)).intValue();

        String contactName = (String) args.get(CONTACT_NAME_ARG);
        String contactEmail = (String) args.get(CONTACT_EMAIL_ARG);

        httpservice = new HttpService(null, httpPort, "localhost");
        httpservice.registerService(SERVICE_NAME, this);
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        if (httpservice != null) {
            httpservice.destroy();
        }
    }


    private void readInitialHttpRecordPlugins() {
	Map allPlugins = engine.getAllPlugins();
	Iterator iter = allPlugins.entrySet().iterator();
	while( iter.hasNext() ) {
	    Map.Entry entry = (Map.Entry)iter.next();
	    String id = (String)entry.getKey();
	    Plugin p = (Plugin)entry.getValue();
	    if( p instanceof HttpRecordPlugin ) {
		registerHttpRecordPlugin( id, (HttpRecordPlugin)p );
	    }
	}
    }

    private void registerHttpRecordPlugin( String id, 
					   HttpRecordPlugin plugin ) {
	System.err.println( "[INFO] registering HttpRecordPlugin " + id );
	recordPlugins.put( id, plugin );
    }


    /** AnalysisEngineListener interface impl **/
   
    public void pluginLoaded( AnalysisEngine engine, String id ) {
	Plugin p = engine.getPlugin( id );
	if( p instanceof HttpRecordPlugin ) {
	    registerHttpRecordPlugin( id, (HttpRecordPlugin)p );
	}
    }

    public void pluginUnloaded( AnalysisEngine engine, String id ) {
	recordPlugins.remove( id );
    }

    public void recordCollectionCreated( AnalysisEngine engine, String id ) {
	// do nothing
    }

    public void recordCollectionRemoved( AnalysisEngine engine, String id ) {
	// do nothing
    }


    /** HTTP-related functions **/


    void deleteRecordCollection(String collectionname) {
        // not yet supported 
    }

    void clearRecordCollection(String collectionname) {
	RecordCollection rc = engine.getRecordCollection( collectionname );
	rc.clearAllRecords();
    }

    void deleteRecord(String collectionname, Object recordid) {
        RecordCollection rc = engine.getRecordCollection(collectionname);
        rc.removeRecord(recordid);
    }

    void createRecordCollection(String name, Map attrs) {
        engine.createRecordCollection(name, attrs);
    }

    
    void createRecord() {
        //todo
    }

    void loadPlugin(String id, String classname, Map args)
        throws IllegalPluginException, PluginException {
        engine.loadPlugin(id, classname, args);
    }

    void unloadPlugin(String id) throws PluginException {
        engine.unloadPlugin(id);
    }


    /* html to generate pages */

    void sendNotYetImplemented(PrintWriter pw, String fn) {
        pw.println("<h2>" + fn + " not yet implemented...</h2>");
    }

    void sendErrorPage(PrintWriter pw, Exception e) {
        pw.println("<h2>Error: " + e.getMessage() + "</h2>");
        pw.println("<P><pre>");
        e.printStackTrace(pw);
        pw.println("</pre><P>");
    }

    void sendRecordsPageList( PrintWriter pw, int page, int maxpage,
			      String recordCollectionName ) {
	pw.println("<p>");

	if (page > 1) {
	    pw.println(
		       "<a href=\""
		       + REL_URL
		       + "?cmd=recordlist&"
		       + "collectionname="
		       + HtmlHelper.URLEncodeText(recordCollectionName)
		       + "&page="
		       + (page - 1)
		       + "\">Prev</a> ");
	}
	else {
	    pw.println("Prev");
	}
	
	for (int i = 1; i <= maxpage; i++) {
	    if (i == page) {
		pw.println(i + " ");
	    }
	    else {
		pw.println(
			   "<a href=\""
			   + REL_URL
			   + "?cmd=recordlist&"
			   + "collectionname="
			   + HtmlHelper.URLEncodeText(recordCollectionName)
			   + "&page="
			   + i
			   + "\">"
			   + i
			   + "</a> ");
	    }
	}
	
	if (page < maxpage) {
	    pw.println(
		       "<a href=\""
		       + REL_URL
		       + "?cmd=recordlist&"
		       + "collectionname="
		       + HtmlHelper.URLEncodeText(recordCollectionName)
		       + "&page="
		       + (page + 1)
		       + "\">Next</a> ");
	}
	else {
	    pw.println("Next");
	}
	
	pw.println("<p>");
    }

    void sendRecords(PrintWriter pw, String recordCollectionName, int page) {

        RecordCollection rc = engine.getRecordCollection(recordCollectionName);

	synchronized( rc ) {
	    Map records = rc.getAllRecords();

	    ArrayList keyList = new ArrayList(records.keySet());
	    // Collections.sort(keyList);

	    int maxpage = 1 + (keyList.size() / numPerPage);
	
	    sendRecordsPageList( pw, page, maxpage, recordCollectionName );

	    for (int i = (page - 1) * numPerPage;
		 i < page * numPerPage && i < keyList.size();
		 i++) {
		Object key = keyList.get(i);
		Record rec = (Record) rc.getRecord(key);
		
		Object v = rec.getValue();
		String s = (v == null) ? "null" : v.toString();
		s = HtmlUtils.sanitizeHtml(s);
		
		pw.println("<hr width=\"65%\"><p>");
		
		pw.print("key = " );

		String shortKeyStr = key.toString();
		if( shortKeyStr.length() > 80 ) {
		    shortKeyStr = shortKeyStr.substring(0,80) + "...";
		} 

		pw.println( HtmlUtils.sanitizeHtml( shortKeyStr ));
		pw.println("\n\n<pre>\n" + s + "\n</pre>");
		
		pw.println("<br><center><font size=\"-1\">");
		pw.println(
			   "[ " );

		pw.println( "<a href=\""
			    + REL_URL
			    + "?cmd=recorddetail&"
			    + "collectionname="
			    + HtmlHelper.URLEncodeText(recordCollectionName)
			    + "&recordname="
			    + encodeKey(key)
			    + "\">Record Details</a> ");

		pw.println(
			   "| <a href=\""
			   + REL_URL
			   + "?cmd=removerecord&"
			   + "collectionname="
			   + HtmlHelper.URLEncodeText(recordCollectionName)
			   + "&recordname="
			   + encodeKey(key)
			   + "\">Remove Record</a> ");

		
		Iterator iter = recordPlugins.keySet().iterator();
		while( iter.hasNext() ) {
		    String pluginid = (String)iter.next();

		    pw.println(  "| <a href=\"" +
				 REL_URL +
				 "?cmd=plugincmd" +
				 "&collectionname=" +
				 HtmlHelper.URLEncodeText(recordCollectionName) +
				 "&plugin=" + HtmlHelper.URLEncodeText(pluginid) +
				 "&recordname=" +
				 encodeKey(key) +
				 "\">" + pluginid + "</a>" );
		}


		pw.println("]");
		pw.println("</font></center>");
	    }

	    sendRecordsPageList( pw, page, maxpage, recordCollectionName );

	} // end synchronized
    }

    Map keyMapping = new HashMap();
    ArrayList keyEncodings = new ArrayList();

    String encodeKey( Object key ) {
	if( !keyMapping.containsKey( key )) {
	    int idx = keyEncodings.size();
	    keyEncodings.add( key );
	    keyMapping.put( key, Integer.toString( idx ));
    	}
	return (String)keyMapping.get( key );
    }

    Object decodeKey( String encodedKey ) {
	int idx = Integer.parseInt( encodedKey );
	return keyEncodings.get( idx );
    }

    void sendRecordCollections(PrintWriter pw) {

        Map collections = engine.getAllRecordCollections();

        ArrayList keyList = new ArrayList(collections.keySet());
        Collections.sort(keyList);
        Iterator iter = keyList.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            RecordCollection rc = engine.getRecordCollection(key);

	    if( rc == null ) {
		pw.println( "<p><b>FAILURE: key '" + key + "' points to a null record collection?<p>" );
		continue;
	    }

            pw.println("<p>");
            pw.println(
                "<b>" + key + ":</b> Contains " + rc.size() + " records<br>");
            pw.println(
                "<a href=\""
                    + REL_URL
                    + "?cmd=recordlist&"
                    + "collectionname="
                    + HtmlHelper.URLEncodeText(key)
                    + "&"
                    + "page=1\">View contents</a>");
            pw.println(
                "<a href=\""
                    + REL_URL
                    + "?cmd=collectiondetail&"
                    + "collectionname="
                    + HtmlHelper.URLEncodeText(key)
                    + "\">"
                    + "Collection Details</a>");
            pw.println(
                "<a href=\""
                    + REL_URL
                    + "?cmd=clearcollection1&"
                    + "collectionname="
                    + HtmlHelper.URLEncodeText(key)
                    + "\">"
                    + "Clear Collection</a>");
            pw.println(
                "<a href=\""
                    + REL_URL
                    + "?cmd=removecollection1&"
                    + "collectionname="
                    + HtmlHelper.URLEncodeText(key)
                    + "\">"
                    + "Remove Collection</a>");
        }

    }

    void sendRecordCollectionDetails(
        PrintWriter pw,
        String recordCollectionName) {
        
	RecordCollection rc = this.engine.getRecordCollection( recordCollectionName );
	if( rc == null ) {
	    pw.println( "<i>Record Collection " + recordCollectionName + " not found!</i><p>" );
	}
	else {
	    pw.println( "Record Collection: <b>" + recordCollectionName + "</b><p>" );
	    Map attrs = rc.getAllAttributes();
	    pw.println( "Attributes:<br><pre>" );
	    Iterator iter = attrs.keySet().iterator();
	    while( iter.hasNext() ) {
		String key = (String)iter.next();
		String val = attrs.get( key ).toString();
		pw.println( key + " = " + val );
	    }
	    pw.println( "</pre>" );
	}

    }

    void doPluginCmd(
        PrintWriter pw,
	String pluginid,
        Object recordKey,
        String recordCollectionName ) {

        sendPageHeader(pw, MODE_FRONTPAGE);

	HttpRecordPlugin plugin = (HttpRecordPlugin)
	    recordPlugins.get( pluginid );

	if( plugin == null ) {
	    pw.println( "Could not find Plugin named '" +
			pluginid + "'" );
	}

	RecordCollection rc = 
	    engine.getRecordCollection( recordCollectionName );

	if( rc == null ) {
	    pw.println( "Could not find RecordCollection named '" +
			recordCollectionName + "'" );
	}

	Record rec = rc.getRecord( recordKey );

	if( rec == null ) {
	    pw.println( "Could not find record '" + recordKey + "'" );
	}


	try {
	    pw.println( plugin.doHttp( rc, rec ));
	}
	catch( Exception ex ) {
	    ex.printStackTrace( pw );
	}

        sendPageFooter(pw, MODE_FRONTPAGE);	
    }


    void sendRecordDetail(
        PrintWriter pw,
        String recordCollectionName,
        Object recordKey) {
        // todo, if the record's getValue() implements a particular interface,
        //    let it output whatever html it wants?
        sendNotYetImplemented(pw, "Record Detail");
    }



    void sendPluginList(PrintWriter pw) {
        Map plugins = engine.getAllPlugins();

        ArrayList keyList = new ArrayList(plugins.keySet());
        Collections.sort(keyList);
        Iterator iter = keyList.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Plugin p = engine.getPlugin(key);
            pw.println("<p>");
            pw.println(
                "<b>" + key + ":</b> class " + p.getClass().getName() + "<br>");
            pw.println(
                "<a href=\""
                    + REL_URL
                    + "?cmd=plugindetail&"
                    + "pluginname="
                    + HtmlHelper.URLEncodeText(key)
                    + "\">"
                    + "Plugin Details</a>");
            pw.println(
                "<a href=\""
                    + REL_URL
                    + "?cmd=unloadplugin1&"
                    + "pluginname="
                    + HtmlHelper.URLEncodeText(key)
                    + "\">"
                    + "Remove Plugin</a>");
        }

    }

    void sendPluginDetail(PrintWriter pw, String pluginName) {
        // todo if the plugin defines a particular interface, let it
        //    output whatever html it wants?

        sendNotYetImplemented(pw, "Plugin Detail");
    }

    void sendMenu(PrintWriter pw, int mode) {
        pw.println("<P>");

        pw.println(
            "[ <A HREF=\"" + REL_URL + "?cmd=plugins\">Plugins</a> " + 
	    "| <A HREF=\"" + REL_URL + "?cmd=collections\">Record Collections</a> " +
	    "| <A HREF=\"" + REL_URL + "?cmd=namespace\">Namespace</a> ]" );

    }

    void sendPageHeader(PrintWriter pw, int mode) {
        pw.println(
            "<HTML>\n<HEAD><TITLE>roc.pinpoint.analysis.plugins." 
                + "HttpFrontEnd</TITLE></HEAD>\n");
        pw.println("<BODY>");
	pw.println( "<b>Current Namespace</b> is " + this.currentNamespace );
        sendMenu(pw, mode);
        pw.println("<P><HR><P>");
    }

    void sendPageFooter(PrintWriter pw, int mode) {
        pw.println("<p><hr><p>");
        sendMenu(pw, mode);
        pw.println("<p><font size=-1>");
        if (contactName != null) {
            pw.println(contactName = ": ");
        }
        if (contactEmail != null) {
            pw.println(
                "<A HREF=\"" + contactEmail + "\">" + contactEmail + "</A>");
        }
        pw.println("</font></BODY></HTML>");
    }

    void sendFrontPage(PrintWriter pw) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendNamespacePage( PrintWriter pw ) {
	sendPageHeader( pw, MODE_FRONTPAGE );
	sendNamespaceList(pw);
	sendPageFooter( pw, MODE_FRONTPAGE );
    }

    void sendNamespaceList( PrintWriter pw ) {
	pw.println( "<p><hr><br>Go to child namespace:<br>" );
	Iterator iter = this.engine.getNameSpaceNames();
	if( !iter.hasNext() ) {
	    pw.println( "<i>No child namespaces</i><p>" );
	}
	else {
	    while( iter.hasNext() ) {
		String n = (String)iter.next();
		pw.println( "<a href=\"" + REL_URL + "?cmd=changenamespace&ns=" + n + "\">" + n + "</a><br>" );
	    }
	}
	pw.println( "<p><a href=\"" + REL_URL + "?cmd=resetnamespace\">Return to Root Namespace</a>" );
    }

    void changeCurrentNamespace( String ns ) {
	AnalysisEngine newNS = this.engine.getNameSpace( ns );
	if( newNS != null ) {
	    this.engine = newNS;
	    this.currentNamespace += ns + "/";
	}
    }

    void resetCurrentNamespace() {
	this.engine = this.rootengine;
	this.currentNamespace = "/";
    }

    void sendPluginsMainPage(PrintWriter pw) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendPluginList(pw);
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendPluginDetailsPage(PrintWriter pw, String pluginName) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendPluginDetail(pw, pluginName);
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendLoadPluginPage(PrintWriter pw) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        // todo
        sendNotYetImplemented(pw, "Create Plugin");
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendConfirmUnloadPluginPage(PrintWriter pw, String pluginName) {
        sendPageHeader(pw, MODE_FRONTPAGE);

        pw.println(
            "<p>Are you sure you want to remove the plugin "
                + pluginName
                + "?");
        pw.println(
            "<p><a href=\""
                + REL_URL
                + "?cmd=unloadplugin2&pluginname="
                + pluginName
                + "\">Unload plugin "
                + pluginName
                + "</a>");
        pw.println(
            "<p><a href=\""
                + REL_URL
                + "?cmd=plugins\">"
                + "Keep plugin "
                + pluginName
                + "</a>");
        pw.println("<p>");

        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendRecordCollectionsMainPage(PrintWriter pw) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendRecordCollections(pw);
	sendNamespaceList(pw);
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendRecordCollectionDetailsPage(
        PrintWriter pw,
        String recordCollectionName) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendRecordCollectionDetails(pw, recordCollectionName);
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendConfirmRecordCollectionClearPage(
        PrintWriter pw,
        String recordCollectionName) {
        sendPageHeader(pw, MODE_FRONTPAGE);

        pw.println(
            "<p>Are you sure you want to clear the collection "
                + recordCollectionName
                + "?");
        pw.println(
            "<p><a href=\""
                + REL_URL
                + "?cmd=clearcollection2&collectionname="
                + recordCollectionName
                + "\">Clear collection "
                + recordCollectionName
                + "</a>");
        pw.println(
            "<p><a href=\""
                + REL_URL
                + "?cmd=collections\">"
                + "Don't clear collection "
                + recordCollectionName
                + "</a>");
        pw.println("<p>");

        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendConfirmRecordCollectionRemovePage(
        PrintWriter pw,
        String recordCollectionName) {
        sendPageHeader(pw, MODE_FRONTPAGE);

        pw.println(
            "<p>Are you sure you want to remove the collection "
                + recordCollectionName
                + "?");
        pw.println(
            "<p><a href=\""
                + REL_URL
                + "?cmd=removecollection2&collectionname="
                + recordCollectionName
                + "\">Remove collection "
                + recordCollectionName
                + "</a>");
        pw.println(
            "<p><a href=\""
                + REL_URL
                + "?cmd=collections\">"
                + "Keep collection "
                + recordCollectionName
                + "</a>");
        pw.println("<p>");

        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendRecordListPage(
        PrintWriter pw,
        String recordCollectionName,
        int pageNum) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendRecords(pw, recordCollectionName, pageNum);
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendRecordDetailPage(
        PrintWriter pw,
        String recordCollectionName,
        Object recordKey) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendRecordDetail(pw, recordCollectionName, recordKey);
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void httpRespond(Map args, Map httpHeaders, PrintWriter pw)
        throws IOException {

        HttpUtils.sendHttpHeadersOK(pw, true);

        String c = (String) args.get("cmd");
        if (c == null) {
            c = "";
        }

        try {
	    if (c.equals("namespace")) {
		sendNamespacePage(pw);
	    }
	    else if (c.equals("changenamespace")) {
		changeCurrentNamespace((String)args.get("ns"));
		//sendNamespacePage(pw);
		sendRecordCollectionsMainPage(pw);
	    }
	    else if (c.equals("resetnamespace")) {
		resetCurrentNamespace();
		//sendNamespacePage(pw);
                sendRecordCollectionsMainPage(pw);
	    }
            else if (c.equals("plugins")) {
                sendPluginsMainPage(pw);
            }
            else if (c.equals("plugindetail")) {
                sendPluginDetailsPage(pw, (String) args.get("pluginname"));
            }
            else if (c.equals("loadplugin1")) {
                sendLoadPluginPage(pw);
            }
            else if (c.equals("loadplugin2")) {
                // todo
                //loadPlugin( (String)args.get( "pluginname" ),
                // /* parse args for plugin */ );
                sendPluginsMainPage(pw);
            }
            else if (c.equals("unloadplugin1")) {
                sendConfirmUnloadPluginPage(
                    pw,
                    (String) args.get("pluginname"));
            }
            else if (c.equals("unloadplugin2")) {
                unloadPlugin((String) args.get("pluginname"));
                sendPluginsMainPage(pw);
            }
            else if (c.equals("collections")) {
                sendRecordCollectionsMainPage(pw);
            }
            else if (c.equals("recordlist")) {
                sendRecordListPage(
                    pw,
                    (String) args.get("collectionname"),
                    Integer.parseInt((String) args.get("page")));
            }
            else if (c.equals("collectiondetail")) {
                sendRecordCollectionDetailsPage(
                    pw,
                    (String) args.get("collectionname"));
            }
            else if (c.equals("removecollection1")) {
                sendConfirmRecordCollectionRemovePage(
                    pw,
                    (String) args.get("collectionname"));
            }
            else if (c.equals("removecollection2")) {
                deleteRecordCollection((String) args.get("collectionname"));
                sendRecordCollectionsMainPage(pw);
            }
            else if (c.equals("clearcollection1")) {
                sendConfirmRecordCollectionClearPage(
                    pw,
                    (String) args.get("collectionname"));
            }
            else if (c.equals("clearcollection2")) {
                clearRecordCollection((String) args.get("collectionname"));
                sendRecordCollectionsMainPage(pw);
            }
	    else if (c.equals( "plugincmd" )) {
		doPluginCmd( pw,
			     (String)args.get( "plugin" ),
			     decodeKey( (String)args.get( "recordname" )),
			     (String)args.get( "collectionname" ));
	    }
            else if (c.equals("createcollection1")) {
                // todo sendCreateRecordCollectionsPage( pw );
            }
            else if (c.equals("createcollection2")) {
                // todo
                //createRecordCollection( (String)args.get( "collectionname" ),
                // /* parse args for collection */ );
                sendRecordCollectionsMainPage(pw);
            }
            else if (c.equals("recorddetail")) {
                sendRecordDetailPage(
                    pw,
                    (String) args.get("collectionname"),
                    decodeKey( (String) args.get("recordname")));
            }
            else if (c.equals("removerecord")) {
                deleteRecord(
                    (String) args.get("collectionname"),
                    decodeKey( (String) args.get("recordname")));
                sendRecordCollectionDetailsPage(
                    pw,
                    (String) args.get("collectionname"));
            }
            else if (c.equals("createrecord1")) {

            }
            else if (c.equals("createrecord2")) {
                // todo
                //createRecord( (String)args.get( "collectionname" ),
                // /* parse content for records */ );
                sendRecordCollectionDetailsPage(
                    pw,
                    (String) args.get("collectionname"));
            }
            else {
                sendFrontPage(pw);
            }
        }
        catch (Exception e) {
            sendErrorPage(pw, e);
        }

        pw.flush();
    }

    /**
     * 
     * @see swig.httpd.HttpExportedService#getURL(String, String, String,
     * OutputStream, InputStream)
     */
    public void getURL(
        String theURL,
        String urlData,
        String clientHostname,
        OutputStream os,
        InputStream is) {
        try {
            LineNumberReader r =
                new LineNumberReader(new InputStreamReader(is));
            Map headers = HttpUtils.getHttpHeaders(r);

            Map args = HttpUtils.parseURLEnc(urlData, 1, urlData.length() - 1);

            httpRespond(
                args,
                headers,
                new PrintWriter(new OutputStreamWriter(os)));
        }
        catch (Exception e) {
            System.out.println(
                "swig.util.DebugHttpService trapped exception=" + e);
            e.printStackTrace();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
            sendFrontPage(pw);
            pw.flush();
        }
    }

    /**
     * @see swig.httpd.HttpExportedService#postURL(String, String, String,
     * OutputStream, InputStream)
     */
    public void postURL(
        String theURL,
        String urlData,
        String clientHostname,
        OutputStream os,
        InputStream is) {
        // do nothing -- post not supported
    }

}
