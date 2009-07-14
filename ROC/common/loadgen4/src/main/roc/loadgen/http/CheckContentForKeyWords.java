package roc.loadgen.http;

import java.util.List;
import java.util.Iterator;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

public class CheckContentForKeyWords extends RequestInterceptor {

    public static final String ARG_KEYWORD_LIST = "keywords";

    Arg[] argDefinitions = {
	new Arg( ARG_KEYWORD_LIST,
		 "a list of keywords to search for",
		 Arg.ARG_LIST,
		 false,
		 "" ) 
    };

    public Arg[] getArguments() {
	return argDefinitions;
    }

    List keywords;

    public void start() {
	keywords = (List)args.get( ARG_KEYWORD_LIST );
    }

    public Response invoke(Request req)
	throws AbortRequestException, AbortSessionException {
	
	HttpRequest httpreq = (HttpRequest) req;
	HttpResponse httpresp = (HttpResponse) invokeNext(req);

	byte[] buf = httpresp.getRespBuf();

	List ppreqlist = (List)httpresp.getHeaders().get("PP-Request");
	String pprequestid = 
	    (ppreqlist != null && ppreqlist.size()>0)?
	    (String)ppreqlist.get(0):"NOREQUESTID";

	checkContentSize( buf, 
			  httpreq, httpresp,
			  pprequestid );
	
	checkContentForKeywords( buf, httpreq,httpresp,pprequestid);

	return httpresp;
    }


    public void checkContentSize( byte[] buf,
				  HttpRequest httpreq,
				  HttpResponse httpresp,
				  String pprequestid ) {

	int length = buf.length;

	if( length > 100 ) {
	    engine.logStats("CONTENTSIZE(PASS): request " +
			    httpreq.toString() +
			    " passed content size check (" +
			    length + " for '" +
			    httpreq.getStringID() + "' PP-Requestid=" +
			    pprequestid );
	}
	else {
	    engine.logStats("CONTENTSIZE(FAIL): request " +
			    httpreq.toString() +
			    " FAILED content size check (" +
			    length + " for '" +
			    httpreq.getStringID() + "' PP-Requestid=" +
			    pprequestid );
            engine.logData(
                new String[] {
                    "contentsize-failure",
                    httpreq.getStringID(),
                    pprequestid},
                httpreq.url.getFile(),
                buf);
	}

    }

    public void checkContentForKeywords( byte[] buf, HttpRequest httpreq,
					 HttpResponse httpresp,
					 String pprequestid ) {
	int count = 0;

	if( httpresp.contentType.startsWith("text")) {
	    String content = new String( buf );

	    Iterator iter = keywords.iterator();
	    while( iter.hasNext() ) {
		String kw = (String)iter.next();
		
		int idx=0;
		while( idx >= 0 ) {
		    idx = content.indexOf( kw, idx );
		    if( idx > 0 )
			count++;
		}
	    }
	}
	
	if( count == 0 ) {
	    engine.logStats("KEYWORDCHECK(PASS): request " +
			    httpreq.toString() +
			    " passed content keyword check (0) for '" +
			    httpreq.getStringID() + "' PP-Requestid=" +
			    pprequestid );
	}
	else {
	    engine.logStats("KEYWORDCHECK(FAIL): request " +
			    httpreq.toString() +
			    " FAILED content keyword check (" +
			    count + ") for '" +
			    httpreq.getStringID() + "' PP-Requestid=" +
			    pprequestid );
            engine.logData(
                new String[] {
                    "keyword-failure",
                    httpreq.getStringID(),
                    pprequestid},
                httpreq.url.getFile(),
                buf);
	}

    }


}
