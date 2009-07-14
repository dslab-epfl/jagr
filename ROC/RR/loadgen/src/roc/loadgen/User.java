package roc.loadgen;

import java.util.Map;

public abstract class User {

	private String id;
	protected Map args;
	protected Engine engine;

	public abstract Arg[] getArguments();

	public final void init( String id, Map args, Engine engine) 
        {
		this.id = id;
		this.args = args;
		this.engine = engine;
	}
	
	public void start() throws InitializationException {
	  // do nothing in base class	
	}
	
	public String getId() {
		return id;
	}

	public abstract Request getNextRequest();

	public abstract void processResponse(Request req, Response resp);

}
