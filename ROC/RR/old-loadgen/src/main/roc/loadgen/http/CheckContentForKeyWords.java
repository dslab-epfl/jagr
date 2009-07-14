package roc.loadgen.http;

import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;

import roc.loadgen.AbortRequestException;
import roc.loadgen.InitializationException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

public class CheckContentForKeyWords extends RequestInterceptor {

    public static final String ARG_KEYWORD_LIST = "keywords";

    private static Logger log = Logger.getLogger( "interceptors.CheckContentForKeyWords" );

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
	throws AbortRequestException {
	
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
	    log.debug( new StringBuffer( "CONTENTSIZE(PASS): request ").append(
                           httpreq.toString() ).append( 
                               " passed content size check (" ).append(
                                   length ).append( 
                                       " for '" ).append( 
                                           httpreq.getStringID() ).append( 
                                               "' PP-Requestid=" ).append(
                                                   pprequestid ).toString() );
	}
	else {
	    log.warn( new StringBuffer( "CONTENTSIZE(FAIL): request " ).append(
                          httpreq.toString() ).append( 
                              " FAILED content size check (" ).append( 
                                  length ).append(
                                      " for '" ).append( 
                                          httpreq.getStringID() ).append(
                                              "' PP-Requestid=" ).append(
                                                  pprequestid ).toString() );

	    httpresp.setIsError();

            /*
            engine.logData(
                new String[] {
                    "contentsize-failure",
                    httpreq.getStringID(),
                    pprequestid},
                httpreq.url.getFile(),
                buf);
            */
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
		    if( idx > 0 ) {
			count++;
                        // HACK: dont bother looking for more
                        break;
                    }
		}
                
                // HACK: dont bother looking for more
                if( count > 0 )
                    break;
	    }
	}
	
	if( count == 0 ) {
	    log.debug( new StringBuffer( "KEYWORDCHECK(PASS): request " ).append( 
                           httpreq.toString() ).append( 
                               " passed content keyword check (0) for '" ).append( 
                                   httpreq.getStringID() ).append( 
                                       "' PP-Requestid=" ).append( 
                                           pprequestid ).toString() );
            
	}
	else {
	    log.warn( new StringBuffer( "KEYWORDCHECK(FAIL): request " ).append(
                          httpreq.toString() ).append( 
                              " FAILED content keyword check (" ).append( 
                                  count ).append(  ") for '" ).append( 
                                      httpreq.getStringID() ).append(
                                          "' PP-Requestid=" ).append( 
                                              pprequestid ).toString() );

	    httpresp.setIsError();
            /**
            engine.logData(
                new String[] {
                    "keyword-failure",
                    httpreq.getStringID(),
                    pprequestid},
                httpreq.url.getFile(),
                buf);
            */
	}

    }


}
