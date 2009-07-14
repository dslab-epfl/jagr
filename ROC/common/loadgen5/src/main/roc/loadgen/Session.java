package roc.loadgen;

import java.util.Map;

public abstract class Session implements Runnable {

    private String id;
    protected Map args;
    protected Engine engine;
    protected TraceReader reader;
    
    
    public abstract Arg[] getArguments();
    
    public final void init( String id, Map args, Engine engine) {
	this.id = id;
	this.args = args;
	this.engine = engine;
    }
    
    public abstract void run();
    
    public void config() {
	// do nothing in base class	
    }
    
    public String getId() {
	return id;
	}

    public String toString() { return id; }
    
    public abstract void resetSession();
    
    public abstract Request getNextRequest() throws AbortSessionException;
    
    public abstract void processResponse(Request req, Response resp)
	throws AbortSessionException;
    
}
