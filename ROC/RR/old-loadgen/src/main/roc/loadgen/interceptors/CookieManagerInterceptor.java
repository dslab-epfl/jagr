package roc.loadgen.interceptors;

import java.util.*;

import roc.loadgen.*;
import roc.loadgen.http.*;
import org.apache.log4j.Logger;

public class CookieManagerInterceptor extends RequestInterceptor {
    private static Logger log = Logger.getLogger("interceptors.CookieManagerInterceptor");

    HttpCookieManager cookieManager = new HttpCookieManager();

    Arg[] argDefinitions = { };

    public CookieManagerInterceptor() {
    }

    public Arg[] getArguments() {
        return argDefinitions;
    }

    public Response invoke( Request req ) 
        throws AbortRequestException {

        HttpRequest httpReq = (HttpRequest)req;

        if( httpReq.isNewSession() ) {
            cookieManager.emptyCookieJar();
        } else {
	    // set cookies to http request
	    cookieManager.setCookies(httpReq);
	}

        HttpResponse httpResp = (HttpResponse)invokeNext( httpReq );

	// get cookies from http response
        cookieManager.getCookies( httpResp );
    
        return httpResp;
    }
}
