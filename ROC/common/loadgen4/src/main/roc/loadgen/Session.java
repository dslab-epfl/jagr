package roc.loadgen;

import java.util.Map;

public abstract class Session {

	private String id;
	protected Map args;
	protected Engine engine;

	public abstract Arg[] getArguments();

	public final void init( String id, Map args, Engine engine) {
		this.id = id;
		this.args = args;
		this.engine = engine;
	}
	
	public void start() throws AbortSessionException {
	  // do nothing in base class	
	}
	
	public String getId() {
		return id;
	}

    /**
     * reset any session state, and return true if it is ok to continue
     * with more requests.   return false if we should no longer use this
     *  session object to send more requests.
     **/
	public abstract boolean resetSession();

	public abstract Request getNextRequest() throws AbortSessionException;

	public abstract void processResponse(Request req, Response resp)
		throws AbortSessionException;

}
