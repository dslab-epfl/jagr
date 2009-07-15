/*
 * $Id: HostUpdate.java,v 1.1 2004/08/27 00:11:17 candea Exp $
 */

package roc.loadgen.interceptors.loadbalancer;

import org.apache.log4j.Logger;

/**
 * A report containing host up/down notifications.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class HostUpdate implements java.io.Serializable
{
    public String hostname;
    public boolean up;

    public HostUpdate( String host, boolean isUp )
    {
	hostname = host;
	up = isUp;
    }

    public String toString() 
    {
	return "[host=" + hostname + ", status=" + (up ? "UP" : "DOWN") + "]";
    }
}
