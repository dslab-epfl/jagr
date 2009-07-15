/*
 * $Id: UserRequest.java,v 1.1 2004/08/18 23:31:39 candea Exp $
 */

package roc.loadgen.rubis;

/**
 * Provides the notion of a user request, which is part of a user
 * action.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public abstract class UserRequest 
    extends roc.loadgen.Request 
{
    private boolean lastInAction; // true, if this request is the last one in the user action
    private UserAction parentAction=null;

    /**
     * Setters and getters
     */
    public void    makeLastInAction()  { lastInAction = true; }
    public boolean isLastInAction()   { return lastInAction; }

}

