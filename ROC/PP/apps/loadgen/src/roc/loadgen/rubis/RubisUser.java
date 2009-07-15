/*
 * $Id: RubisUser.java,v 1.3 2004/09/07 02:17:01 candea Exp $
 */

package roc.loadgen.rubis;

import java.net.URL;
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import roc.loadgen.*;

/**
 * A simulated user of the RUBiS application.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public class RubisUser
    extends roc.loadgen.User
{
    public static final String ARG_CONFIG = "config_file";

    private static Logger log = Logger.getLogger( "RubisUser" );

    // Configuration for this user
    private static Configuration conf=null;

    // The entity that generates our requests
    private static RequestGenerator generator=null;

    // The current request this user is trying to get answered
    RubisUserRequest currentRequest;

    // A stack of previous requests, to resubmit on "Back" click
    Stack previousRequests;

    /**
     * Start the RUBiS user.
     */
    public void start() 
	throws InitializationException
    {
	if( conf == null )
	    conf = new Configuration( (String)args.get(ARG_CONFIG) );

	if( generator == null )
	{
	    try {
		generator = new RequestGenerator( conf.workloadFile );
	    }
	    catch( Exception e ) 
	    {
		throw new InitializationException("Cannot start RUBiS user with " + conf.workloadFile, e);
	    }
	}

	currentRequest = generator.firstRequestInSession();
	previousRequests = new Stack();
    }

    /**
     * Get the next request this RUBiS user wants to perform.
     */
    public Request getNextRequest()
    {
	assert currentRequest != null;
	log.debug( "next user req: " + currentRequest );

	return currentRequest; 
    }

    /**
     * Process the response received to a submitted request.  The
     * simulated user determines in this method the next step it wants
     * to take.  This next step will be submitted the next time the
     * user is asked.
     *
     * @param submittedRequest the last-submitted request
     * @param receivedResponse response received from server to 
     * 
     */
    public void processResponse( Request submittedRequest, Response receivedResponse )
    {
	assert currentRequest==submittedRequest : "currentRequest=" + currentRequest + "\n" +
	                                          "submittedRequest=" + submittedRequest;

	RubisUserRequest nextRequest = generator.nextRequest( currentRequest, receivedResponse );

	if( nextRequest == RequestGenerator.REQ_BACK )
	{
	    // discard currentRequest; go to the request before it
	    currentRequest = popLastRequest();
	}
	else if( nextRequest == RequestGenerator.REQ_ABORT )
	{
	    // discard currentRequest and session; go to HOME
	    terminateCurrentSession();
	    currentRequest = generator.firstRequestInSession();
	}
	else
	{
	    // save currentRequest on the stack; go to the next one
	    previousRequests.push( currentRequest );
	    currentRequest = nextRequest;
	}
    }

    /**
     * Return the request user executed just before clicking the Back
     * button.  This works like a stack -- if user keeps clicking the
     * Back button, it keeps going back in time.
     *
     * @return state user was in prior to current state
     *
     */
    private RubisUserRequest popLastRequest()
    {
	assert !previousRequests.empty();

	return (RubisUserRequest) previousRequests.pop();
    }

    /**
     * Abandon the in-progress session.
     *
     * @return next state after having abandoned the in-progress session
     *
     */
    private void terminateCurrentSession()
    {
	currentRequest = null;  // abandoning session means go to undefined state
	previousRequests.clear();
    }


    /**
     * The arguments this User implementation expects from the environment.
     */
    public Arg[] getArguments() 
    {
	Arg[] argDefinitions = { 
	    new Arg( ARG_CONFIG, "name of RUBiS config file", Arg.ARG_STRING, true, null ),
	};

	return argDefinitions;
    }
}

