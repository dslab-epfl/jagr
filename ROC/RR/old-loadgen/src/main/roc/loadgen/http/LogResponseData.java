/*
 * Created on Apr 14, 2004
 * 
 */
package roc.loadgen.http;

import java.util.List;
import java.util.Map;

import roc.loadgen.AbortRequestException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

/**
 * @author emrek
 * 
 */
public class LogResponseData extends RequestInterceptor {

    Arg[] argDefinitions = {};
    
    public Arg[] getArguments() {
        return argDefinitions;
    }

    public Response invoke(Request req)
        throws AbortRequestException {

        HttpRequest httpreq = (HttpRequest) req;
        HttpResponse httpresp = (HttpResponse) invokeNext(req);

        byte[] buf = httpresp.getRespBuf();
        String stringid = ((HttpRequest) req).getStringID();

	String pprequest = "NOREQUESTID";
	Map headers = httpresp.getHeaders();
	if( headers != null ) {
	    List ppreqlist = (List)headers.get("PP-Request");
	    pprequest = 
		(ppreqlist!=null && ppreqlist.size()>0)?
		(String)ppreqlist.get(0):"NOREQUESTID";
	}

        engine.logData(
            new String[] {
                "logresponse",
                httpreq.getStringID(),
                pprequest},
            httpreq.url.getFile(),
            buf);

        return httpresp;
    }

}
