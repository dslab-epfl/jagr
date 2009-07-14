package roc.loadgen;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Collections;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.*;

public class Engine 
{

    //private inner classes

    private class CampaignEvent implements Comparable
    {

	protected String eventID;
        protected String sessionID;
        protected String tracefile;
        protected int clients;
        protected int delay;
	
        public CampaignEvent(String eventID, String sessionID, String trace, int clients, int delay)
        {
            this.eventID = eventID;
            this.sessionID = sessionID;
            tracefile = trace;
            this.clients = clients;
            this.delay = delay;
        }

        public int compareTo(Object obj)
        {
            int i = ((CampaignEvent)obj).getDelay();
            if(delay == i)
                return 0;
            return delay <= i ? -1 : 1;
        }

        public int getDelay(){ return delay; }
        public int getClients(){ return clients; }
        public String getID(){ return eventID; }
        public String getSession(){ return sessionID; }
        public String getTracefile(){ return tracefile; }
        public String toString(){ return eventID; }
    }

    private class SessionData
    {
	public String id;
        public String classname;
        public Map args;

        public SessionData(String id, String classname, Map args)
        {
            this.id = id;
            this.classname = classname;
            this.args = args;
        }

	public SessionData()
        {
            id = null;
            classname = null;
            args = null;
        }

        public void addArg(String key, String value){ args.put(key, value); }

        public String toString()
	{ 
	    return "id: " + id + " class: " + classname + " Args: " + args + "\n";
        }
    }

    public class TraceInfo
    {

        public String filename;
        public String classname;

        public TraceInfo(String s, String s1)
        {
            filename = s;
            classname = s1;
        }
    }

    private class SessionLauncher extends TimerTask
    {

	private CampaignEvent ev;

        public SessionLauncher(CampaignEvent campaignevent)
        {
            ev = campaignevent;
        }

        public void run()
        {
            int i = ev.getClients();
            String s = ev.getSession();
            String s1 = ev.getTracefile();
	    Map args;

            SessionData sessiondata = (SessionData)sessionInfoList.get(s);
	    args = sessiondata.args;

            args.put("filename", s1);
            System.out.println("[SCHEDULER] Beginning " + ev);

            for(int j = 0; j < i; j++)
		{
		    try
			{
			    startSession(s, sessiondata.classname, sessiondata.args);
			}
		    catch(Exception exception)
			{
			    exception.printStackTrace();
			}
		}
        }
    }

    //member attributes

    protected Map attrs;
    protected RequestInterceptor headRequestInterceptor;
    protected List threadList;

    protected int debug;
    protected TraceManager traceManager;

    private Map traceInfoList;
    private Map sessionInfoList;
    private ArrayList campaign;
    private int threadnum;

    //constructors

    public Engine()
    {
	threadnum = 0;
        attrs = new HashMap();
        threadList = new LinkedList();
        traceManager = new TraceManager();
        campaign = new ArrayList();
    }

    //accessors

    public TraceReader getTraceReader(String s)
    {
        return traceManager.getTraceReader(s);
    }

    //functions

    public void begin()
    {
        Timer timer = new Timer();
        scheduleEvents(timer);
    }
    
    public void scheduleEvents(Timer timer)
    {
        CampaignEvent event;

        Iterator iterator = campaign.iterator(); 
	while(iterator.hasNext())
        {
            event = (CampaignEvent)iterator.next();
            timer.schedule(new SessionLauncher(event), event.getDelay());
	    System.out.println("[SCHEDULER] Scheduled " + event + " delay " + event.getDelay());
        }

    }

    public void configure(String cmdLineArgs[]) throws AbortSessionException {

	/*	if( cmdLineArgs.length == 0 || 
	    cmdLineArgs[0].equals( "--help" ) || 
	    cmdLineArgs[0].equals( "-H") ) {
	    System.err.println( "Usage: roc.loadgen.Engine configfile [option1=value [option2=value]...]" );
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

        loadXMLConfiguration(configurationFileName, false);*/

	loadConfig(cmdLineArgs[0]);
	loadCampaign(cmdLineArgs[1]);
	loadTraces();
    }

    private void loadTraces()
    {
        Iterator iter = campaign.iterator(); 

	while(iter.hasNext())
        {
            CampaignEvent event = (CampaignEvent)iter.next();
            
            String filename = event.getTracefile();
            String classname = ((TraceInfo)traceInfoList.get(filename)).classname;
            if(classname == null)
            {
                System.err.println("Campaign " + event.getID() + " uses undefined tracefile.\n");
                iter.remove();
            } else
            {
                traceManager.loadNewTrace(filename, classname);
            }
        }
    }

   //XML Loading

    private Document loadXMLFile(String filename)
    {
	try
	    {
		Document document;
		File file = new File(filename);
		
		DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory.newInstance();
		documentbuilderfactory.setValidating(false);
		
		DocumentBuilder documentbuilder = null;
		document = null;
		
		try
		    {
			documentbuilder = documentbuilderfactory.newDocumentBuilder();
		    }
		catch(ParserConfigurationException parserconfigurationexception)
		    {
			parserconfigurationexception.printStackTrace();
		    }
		
		document = documentbuilder.parse(file);
		return document;
	    }
	catch(Exception e){ e.getMessage(); }

        return null;
    }

    private void loadConfig(String configfile)
    {
        traceInfoList = new HashMap();
        sessionInfoList = new HashMap();

        Document document = loadXMLFile(configfile);

        if(document == null)
	    {
		System.err.println("The specified configuration file is invalid.");
		System.exit(-1);
	    }

        Element element = document.getDocumentElement();
        loadTraceData(element.getElementsByTagName("trace"));
        loadSessionData(element.getElementsByTagName("session"));
        loadInterceptors(element.getElementsByTagName("interceptor"));
    }

    private void loadTraceData(NodeList nodelist)
    {
	for(int i=0; i < nodelist.getLength(); i++) {
	    NodeList elements = nodelist.item(i).getChildNodes();
	    String filename = null;
	    String classname = null;
	    
	    for(int j=0; j < elements.getLength(); j++) {
		Node elem = elements.item(j);

		if(elem.getNodeName().equalsIgnoreCase("filename"))
		    filename = elem.getFirstChild().getNodeValue();
		else if (elem.getNodeName().equalsIgnoreCase("classname"))
		    classname = elem.getFirstChild().getNodeValue();
	    }

	    traceInfoList.put(filename, new TraceInfo(filename, classname));
	}
    }

    private void loadSessionData(NodeList nodelist)
    {
        String id = null;
        String classname = null;

        for(int i = 0; i < nodelist.getLength(); i++)
        {
            NodeList elements = nodelist.item(i).getChildNodes();
            HashMap args = new HashMap();

            for(int j = 0; j < elements.getLength(); j++)
            {
                Node elem = elements.item(j);
                if(elem.getNodeName().equalsIgnoreCase("id"))
                    id = elem.getFirstChild().getNodeValue();
                else if(elem.getNodeName().equalsIgnoreCase("classname"))
                    classname = elem.getFirstChild().getNodeValue();
                else if(elem.getNodeName().equalsIgnoreCase("arg"))
                {
                    String key = elem.getAttributes().getNamedItem("key").getNodeValue();
                    args.put(key, elem.getFirstChild().getNodeValue());
                }
            }
            sessionInfoList.put(id, new SessionData(id, classname, args));
        }
    }

   private void loadInterceptors(NodeList nodelist)
    {
        String id = null;
        String classname = null;
        Object obj = null;
        for(int i = 0; i < nodelist.getLength(); i++)
        {
            NodeList elements = nodelist.item(i).getChildNodes();
            HashMap args = new HashMap();

            for(int j = 0; j < elements.getLength(); j++)
            {
                Node node = elements.item(j);

                if(node.getNodeName().equalsIgnoreCase("id"))
                    id = node.getFirstChild().getNodeValue();
                else if(node.getNodeName().equalsIgnoreCase("classname"))
                    classname = node.getFirstChild().getNodeValue();
                else if(node.getNodeName().equalsIgnoreCase("arg"))
                {
                    String key = node.getAttributes().getNamedItem("key").getNodeValue();
                    args.put(key, node.getFirstChild().getNodeValue());
                }
            }
            try
            {
                loadInterceptor(id, classname, args);
            }
            catch(IllegalPluginException illegalpluginexception)
            {
                System.err.println("Attempted to load bad interceptor.\n");
            }
            catch(AbortSessionException abortsessionexception)
            {
                System.err.println("Abort session!\n");
            }
        }
    }

    private void loadCampaign(String filename)
    {
        campaign = new ArrayList();
        Document document = loadXMLFile(filename);

        if(document == null)
	    {
		System.err.println("The specified campaign file is invalid.");
		System.exit(-1);
	    }

        Element element = document.getDocumentElement();
        loadCampaignData(element.getElementsByTagName("event"));
    }

    private void loadCampaignData(NodeList nodelist)
    {
        String eventID = null;
        String sessionID = null;
        String tracefile = null;
        int clients = 0;
        int delay = 0;

        for(int k = 0; k < nodelist.getLength(); k++)
        {
            NodeList event = nodelist.item(k).getChildNodes();
	    delay = 0;
	    clients = 1;

            for(int l = 0; l < event.getLength(); l++)
            {
                Node node = event.item(l);
 
                if(node.getNodeName().equalsIgnoreCase("eventID"))
                    eventID = node.getFirstChild().getNodeValue();
                else
                if(node.getNodeName().equalsIgnoreCase("sessionID"))
                    sessionID = node.getFirstChild().getNodeValue();
                else
                if(node.getNodeName().equalsIgnoreCase("numClients"))
                    clients = Integer.parseInt(node.getFirstChild().getNodeValue());
                else
                if(node.getNodeName().equalsIgnoreCase("tracefile"))
                    tracefile = node.getFirstChild().getNodeValue();
                else
                if(node.getNodeName().equalsIgnoreCase("delay"))
                    delay = Integer.parseInt(node.getFirstChild().getNodeValue());
            }

            campaign.add(new CampaignEvent(eventID, sessionID, tracefile, clients, delay));
        }

        Collections.sort(campaign);
    }

    void startSession(String id, String classname, Map args)
        throws IllegalPluginException, AbortSessionException {

	Session session;

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
		Object v = argDefs[i].parseArgument( args.get( n ).toString(),
						     this );
		args.put( n, v );
	    }

	    String sname = "SESSION " + (++threadnum);

            session.init(sname, args, this);
	    session.config();
	    Thread t = new Thread(session);
	    threadList.add(t);
	    System.out.println("[ENGINE} Beginning " + sname);
	    t.start();
        }
        catch (IllegalPluginException ex) {
            throw ex;
        }
	/*        catch (AbortSessionException ex) {
            throw ex;
	    }*/
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

    public synchronized void logInfo(String s) {
        // do something nicer here...
        System.err.println("[INFO] " + s);
    }

    public synchronized void logInfo(String s, Object src) {
        // do something nicer here...
        System.err.println("[INFO " + src + "] " + s);
    }

    public synchronized void logWarning(String s) {
        // do something nicer here...
        System.err.println("[WARN] " + s);
    }

    public synchronized void logError(String s) {
        // do something nicer here...
        System.err.println("[ERROR] " + s);
    }

   public synchronized void logError(String s, Object obj) {
        // do something nicer here...
        System.err.println("[ERROR " + obj + "] " + s);
    }

    public synchronized void logStats(String s) {
        // do something nicer here...
        System.err.println("[STATS] " + s);
    }

    public synchronized void setHeadRequestInterceptor(RequestInterceptor ri) {
        headRequestInterceptor = ri;
    }

    public synchronized RequestInterceptor getHeadRequestInterceptor() {
        return headRequestInterceptor;
    }

    public static void main(String[] args) {

        try {
            Engine engine = new Engine();
            engine.configure(args);
            engine.begin();
        }
        catch (Exception ex) {
            System.err.println("FATAL ERROR");
            ex.printStackTrace();
        }
    }
}
