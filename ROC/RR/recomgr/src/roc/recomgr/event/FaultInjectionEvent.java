/*
 * $Id: FaultInjectionEvent.java,v 1.1 2004/08/27 22:25:39 candea Exp $
 */

package roc.recomgr.event;

import roc.recomgr.*;

/**
 * An event that notifies the recovery manager of a fault injection.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class FaultInjectionEvent extends Event 
{
    public static final String TYPE = "InjectionNotification";

    public FaultInjectionEvent() { }

    public String getType() { return TYPE;  }

}
