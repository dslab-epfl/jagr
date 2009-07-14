/*
 * $Id: UserAction.java,v 1.1 2004/08/18 23:12:49 candea Exp $
 */

package roc.loadgen.rubis;

/**
 * Provides the notion of a user action, which is part of a user
 * session; a user action consists of multiple user requests.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public abstract class UserAction
    extends roc.loadgen.Request
{
    boolean lastInSession; // true, if last action in user session

    /**
     * Setters and getters
     */
    public void setLastInSession()   { lastInSession = true; }
    public boolean isLastInSession() { return lastInSession; }

}

