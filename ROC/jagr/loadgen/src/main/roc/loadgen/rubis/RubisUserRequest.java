/*
 * $Id: RubisUserRequest.java,v 1.3 2004/07/20 05:21:15 candea Exp $
 */

package roc.loadgen.rubis;

import java.net.URL;
import java.util.Map;

/**
 * Provide the notion of a user request, which is part of a UserAction.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public class RubisUserRequest 
    extends roc.loadgen.http.HttpRequest 
{
    private RubisUserState state;
    private boolean        lastInAction;

    /**
     * Constructor.
     *
     * @param url the URL for executing this request
     * @param lastInAction true iff this request is the last one in its user action
     * @param state the state of the user submitting this request
     */
    public RubisUserRequest(URL url, boolean lastInAction, RubisUserState state)
    {
	super( url, null, null );
	this.lastInAction = lastInAction;
	this.state = state;
    }

    /**
     * True, if this request is the last one in its user action, false
     * otherwise.
     */
    public boolean isLastInAction() 
    { 
	return lastInAction; 
    }

    /**
     * Getter for the user state associated with this request.
     */
    public RubisUserState getState()
    {
	return state;
    }
}

