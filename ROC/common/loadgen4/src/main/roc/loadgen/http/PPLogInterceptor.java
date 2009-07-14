/*
 * Created on Apr 12, 2004
 * 
 * To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package roc.loadgen.http;

import java.util.Map;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;
import roc.loadgen.http.HttpRequest;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PPLogInterceptor extends RequestInterceptor {

    Arg[] argDefinitions = {
    };

    public Arg[] getArguments() {
        return argDefinitions;
    }

    public Response invoke(Request req)
        throws AbortRequestException, AbortSessionException {

        Response resp = invokeNext(req);

	Map headers = ((HttpResponse) resp).getHeaders();
	if(headers == null ) {
	    engine.logWarning( "No headers found in HTTP response: " 
			       + resp.toString() );
	}

        engine.logInfo(
            "PPLOG: requestid="
  	        + ((headers == null)?"NONE":headers.get("PP-Request"))
                + " ;response="
                + ((HttpResponse) resp).getRespCode() + " ;url=" +
	    ((HttpRequest)req).getUrl() + " ; stringid=" +
	    ((HttpRequest)req).getStringID() );
        return resp;
    }

}