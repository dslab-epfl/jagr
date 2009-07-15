/*
 * $Id: RequestInterceptor.java,v 1.2 2004/08/25 04:15:09 candea Exp $
 */

package roc.loadgen;

import java.util.Map;

public abstract class RequestInterceptor 
{

    private RequestInterceptor nextRI=null;
    private RequestInterceptor prevRI=null;

    // Forked chains: left and right next interceptors
    private RequestInterceptor nextLeftRI=null;
    private RequestInterceptor nextRightRI=null;

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
    public void start() throws InitializationException {
	// do nothing in base class
    }

    public void stop() {
	// do nothing in base class
    }

    public String getId() {
	return id;
    }

    //==================== LINEAR INTERCEPTOR CHAINS ====================//
    public void setNextRequestInterceptor(RequestInterceptor ri) {
	nextRI = ri;
    }

    public RequestInterceptor getNextRequestInterceptor() {
	return nextRI;
    }

    //==================== FORKED INTERCEPTOR CHAINS ====================//
    public void setNextInterceptors( RequestInterceptor leftRI, RequestInterceptor rightRI )
    {
	assert nextRI==null : nextRI;
	nextLeftRI = leftRI;
	nextRightRI = rightRI;
    }

    public RequestInterceptor getLeftInterceptor() 
    {
	assert nextRI==null : nextRI;
	return nextLeftRI;
    }

    public RequestInterceptor getRightInterceptor() 
    {
	assert nextRI==null : nextRI;
	return nextRightRI;
    }

    //======================================================================

    public void setPrevRequestInterceptor(RequestInterceptor ri) {
	prevRI = ri;
    }

    public RequestInterceptor getPrevRequestInterceptor() {
	return prevRI;
    }

    protected Response invokeNext( Request req )
	throws AbortRequestException 
    {
	return invokeInterceptor( nextRI, req );
    }

    protected Response invokeLeft( Request req )
	throws AbortRequestException 
    {
	assert (nextLeftRI != null) && (nextRightRI != null);

	return invokeInterceptor( nextLeftRI, req );
    }

    protected Response invokeRight( Request req )
	throws AbortRequestException 
    {
	assert (nextLeftRI != null) && (nextRightRI != null);

	return invokeInterceptor( nextRightRI, req );
    }

    private Response invokeInterceptor( RequestInterceptor ri, Request req )
	throws AbortRequestException
    {
	if( ri != null )
	    return ri.invoke( req );
	else
	    return null;
    }

    public abstract Response invoke(Request req)
	throws AbortRequestException;

}
