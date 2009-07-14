package roc.loadgen;

import java.util.Map;

public abstract class RequestInterceptor {

	private RequestInterceptor nextRI;
	private RequestInterceptor prevRI;

	private String id;
	protected Map args;
	protected Engine engine;

	public abstract Arg[] getArguments();

	public final void init( String id, Map args, Engine engine) {
		this.id = id;
		this.args = args;
		this.engine = engine;
	}
	
	/**
	 * interceptors should subclass this method to handle their own initialization...
	 *
	 */
	public void start() throws AbortSessionException {
		// do nothing in base class
	}

	public String getId() {
		return id;
	}

	public void setNextRequestInterceptor(RequestInterceptor ri) {
		nextRI = ri;
	}

	public RequestInterceptor getNextRequestInterceptor() {
		return nextRI;
	}

	public void setPrevRequestInterceptor(RequestInterceptor ri) {
		prevRI = ri;
	}

	public RequestInterceptor getPrevRequestInterceptor() {
		return prevRI;
	}


	protected Response invokeNext(Request req)
		throws AbortRequestException, AbortSessionException {
		Response ret = null;

		if (nextRI != null) {
			ret = nextRI.invoke(req);
		}

		return ret;
	}

	public abstract Response invoke(Request req)
		throws AbortRequestException, AbortSessionException;

}
