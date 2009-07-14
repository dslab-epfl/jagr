/*
 * Created on Apr 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package roc.loadgen.http;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Parser;
import javax.swing.text.html.parser.TagElement;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CheckURLReachability extends RequestInterceptor {

    public static final String ARG_PARTIAL_URLS_TO_ACCEPT = "partialURLs";


    Arg[] argDefinitions = {
            new Arg( ARG_PARTIAL_URLS_TO_ACCEPT, 
                    "a list of partial urls to accept without checking for reachability",
                    Arg.ARG_LIST,
                    false,
                    "" )
    };

    
    List alwaysAcceptableURLs;
    String lastpage;
    String laststringid;
    String lastpinpointid;
    List lastAcceptableURLs;	// urls that occurred on the last page we saw
    
    
    /* (non-Javadoc)
     * @see roc.loadgen.RequestInterceptor#getArguments()
     */
    public Arg[] getArguments() {
        return argDefinitions;
    }

    public void start() {
        alwaysAcceptableURLs = (List)args.get( ARG_PARTIAL_URLS_TO_ACCEPT );
    }
    

    /* (non-Javadoc)
     * @see roc.loadgen.RequestInterceptor#invoke(roc.loadgen.Request)
     */
    public Response invoke(Request req) throws AbortRequestException, AbortSessionException {

        HttpRequest httpreq = (HttpRequest)req;
        
	if( !httpreq.getMetadata().containsKey("start-session") ) {
	    boolean isAcceptable =
		checkIsAcceptableURL(alwaysAcceptableURLs, httpreq.url);
	    if(!isAcceptable) {
		boolean isReachable = checkIsAcceptableURL(lastAcceptableURLs, httpreq.url);
	        
		if( !isReachable ) {
		    engine.logStats( "CheckURLReachAble FAILED: lastpage=" + 
				     laststringid + "[" + lastpinpointid + 
				     "] does not link to " + 
				     httpreq.url );
		    throw new AbortSessionException("URL: "+httpreq.url +" is not reachable");
		}
	    }
	}
	        
        HttpResponse resp = (HttpResponse)getNextRequestInterceptor().invoke(httpreq);

        try {
            rememberResponsePage(httpreq,resp);
        }
        catch(Exception e ) {
            System.err.println("EMKDEBUG: WHOA!what's going on! bad html??! bad parser?!");
            e.printStackTrace();
        }
            
        return resp;
    }

    /**
     * @param url
     * @return
     */
    private boolean checkIsAcceptableURL(List checklist, URL url) {
        boolean ret = false;

	if( checklist == null )
	    return true;

        String path = url.getPath();
	path = path.trim();

        Iterator iter = checklist.iterator();
        while (iter.hasNext()) {
            String okpath = (String) iter.next();
            if(( okpath == null && path == null ) ||
               ( okpath != null && okpath.equals(path) )) {
                ret = true;
                break;
            }
        }

        return ret;
    }
    
    private List parseOutAvailableURLs( URL baseurl, String page ) throws IOException, MalformedURLException {
        
        MyParser myparser = new MyParser(baseurl);
        myparser.parse( new StringReader(page) );
        
        List urls = myparser.getAvailableURLs();
	
	List ret = new ArrayList( urls.size() );
	Iterator iter = urls.iterator();
	while( iter.hasNext() ) {
	    String path = ((URL)iter.next()).getPath();
	    ret.add( path );
	}

	return ret;
    }

    /**
     * @param resp
     */
    private void rememberResponsePage(HttpRequest req, HttpResponse resp) throws IOException, MalformedURLException {
        if(resp.contentType.startsWith("text")) {
            lastpage = resp.respStr;
	    laststringid = req.getStringID();
	    Map headers = resp.getHeaders();
	    lastpinpointid = ((headers == null)?"NONE":String.valueOf( headers.get("PP-Request")));


	    engine.logInfo("remembering urls from response page " + resp.toString() );

            lastAcceptableURLs = parseOutAvailableURLs(req.url,lastpage );
        }
    }

    class MyParser extends Parser {

        
        URL baseurl;
        List availableURLs;
        
        public MyParser( URL baseurl ) throws IOException {
            super(DTD.getDTD("html32"));
            availableURLs = new LinkedList();
            this.baseurl = baseurl;
        }
 
        protected void handleEmptyTag(TagElement tag ) {
            checkTagForURL(tag);
        }

        protected void handleStartTag( TagElement tag ) {
            checkTagForURL(tag);
        }
        
        protected void checkTagForURL( TagElement tag ) {
            Tag htmlTag = tag.getHTMLTag();
            if(htmlTag == Tag.A) {
                SimpleAttributeSet attrs = getAttributes();
                String u = (String)attrs.getAttribute(HTML.Attribute.HREF);
                availableURLs.add(u);
            }
            else if(htmlTag == Tag.FORM) {
                SimpleAttributeSet attrs = getAttributes();
                String u = (String)attrs.getAttribute(HTML.Attribute.ACTION);
                availableURLs.add(u);
            }
            else if(htmlTag == Tag.IMG) {
                SimpleAttributeSet attrs = getAttributes();
                String u = (String)attrs.getAttribute(HTML.Attribute.SRC);
                availableURLs.add(u);
            }
            else if(htmlTag == Tag.INPUT) {
                SimpleAttributeSet attrs = getAttributes();
                String u = (String)attrs.getAttribute(HTML.Attribute.SRC);
                availableURLs.add(u);
            }
            
        }
        
        public List getAvailableURLs() throws MalformedURLException {
            List ret = new ArrayList( availableURLs.size());
            Iterator iter = availableURLs.iterator();
            while (iter.hasNext()) {
                String u = (String) iter.next();
		URL url = new URL( baseurl, u );
		ret.add(url);
            }
            return ret;
        }
    }
    
}
