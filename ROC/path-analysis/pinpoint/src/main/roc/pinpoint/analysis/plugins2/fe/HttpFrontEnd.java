package roc.pinpoint.analysis.plugins2.fe;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
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
public class HttpFrontEnd implements Plugin, HttpExportedService {

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

    private AnalysisEngine engine;

    public PluginArg[] getPluginArguments() {
	return args;
    }


    /**
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String name, Map args, AnalysisEngine engine)
        throws PluginException {

        this.engine = engine;

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


    void deleteRecordCollection(String collectionname) {
        // not yet supported 
    }

    void deleteRecord(String collectionname, String recordid) {
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

    void sendRecords(PrintWriter pw, String recordCollectionName, int page) {

        RecordCollection rc = engine.getRecordCollection(recordCollectionName);

	synchronized( rc ) {
	    Map records = rc.getAllRecords();

	    ArrayList keyList = new ArrayList(records.keySet());
	    // Collections.sort(keyList);

	    pw.println("<p>");

	    int maxpage = 1 + (keyList.size() / numPerPage);

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
		pw.println( HtmlUtils.sanitizeHtml( key.toString() ) );
		pw.println("\n\n<pre>\n" + s + "\n</pre>");
		
		pw.println("<br><center><font size=\"-1\">");
		pw.println(
			   "[ " );
		/*
		// TODO: encoding problem with key
		pw.println( "<a href=\""
		+ REL_URL
		+ "?cmd=recorddetail&"
		+ "collectionname="
		+ HtmlHelper.URLEncodeText(recordCollectionName)
		+ "&recorddetail="
		+ HtmlHelper.URLEncodeText(key)
		+ "\">Record Details</a> ");
		*/
		/*
		// TODO: encoding problem with key
		pw.println(
                "| <a href=\""
		+ REL_URL
		+ "?cmd=removerecord&"
		+ "collectionname="
		+ HtmlHelper.URLEncodeText(recordCollectionName)
		+ "&recorddetail="
		+ HtmlHelper.URLEncodeText(key)
		+ "\">Remove Record</a> ");
		*/
		pw.println("]");
		pw.println("</font></center>");
	    }
	} // end synchronized
    }

    void sendRecordCollections(PrintWriter pw) {

        Map collections = engine.getAllRecordCollections();

        ArrayList keyList = new ArrayList(collections.keySet());
        Collections.sort(keyList);
        Iterator iter = keyList.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            RecordCollection rc = engine.getRecordCollection(key);
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
        //todo
        sendNotYetImplemented(pw, "Record Collection Details");
    }

    void sendRecordDetail(
        PrintWriter pw,
        String recordCollectionName,
        String recordKey) {
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
            "[ <A HREF=\""
                + REL_URL
                + "?cmd=plugins\">Plugins</a> | <A HREF=\""
                + REL_URL
                + "?cmd=collections\">Record Collections</a> ]");

    }

    void sendPageHeader(PrintWriter pw, int mode) {
        pw.println(
            "<HTML>\n<HEAD><TITLE>roc.pinpoint.analysis.plugins." 
                + "HttpFrontEnd</TITLE></HEAD>\n");
        pw.println("<BODY>");
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
        sendPageFooter(pw, MODE_FRONTPAGE);
    }

    void sendRecordCollectionDetailsPage(
        PrintWriter pw,
        String recordCollectionName) {
        sendPageHeader(pw, MODE_FRONTPAGE);
        sendRecordCollectionDetails(pw, recordCollectionName);
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
        String recordKey) {
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
            if (c.equals("plugins")) {
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
                sendRecordDetail(
                    pw,
                    (String) args.get("collectionname"),
                    (String) args.get("recordname"));
            }
            else if (c.equals("removerecord")) {
                deleteRecord(
                    (String) args.get("collectionname"),
                    (String) args.get("recordname"));
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
