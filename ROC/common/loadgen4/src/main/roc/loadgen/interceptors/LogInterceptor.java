package roc.loadgen.interceptors;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

public class LogInterceptor extends RequestInterceptor {

    Arg[] argDefinitions = {
    };

    public LogInterceptor() {
    }

    /**
     * @see roc.loadgen.RequestInterceptor#getArguments()
     */
    public Arg[] getArguments() {
        return argDefinitions;
    }

    public Response invoke(Request req)
        throws AbortRequestException, AbortSessionException {

        engine.logInfo("MAKING REQUEST: " + req.toString());

        Response resp = invokeNext(req);

        engine.logInfo("REQUEST RESPONSE: " + resp.toString());

        return resp;
    }

}
