/**********************************************************
 * RMIntf.java - Remote interface for the Recovery Manager
 *
 * $Id: RMIntf.java,v 1.1 2003/09/20 04:24:08 steveyz Exp $
 **********************************************************/

package roc.rr.rm;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIntf extends Remote 
{
    // returns time (in milliseconds) since the last "real" fault occured
    long timeSinceLastFault_ms() throws RemoteException;
}
 
