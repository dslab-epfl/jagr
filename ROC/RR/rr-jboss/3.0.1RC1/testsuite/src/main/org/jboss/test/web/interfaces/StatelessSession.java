package org.jboss.test.web.interfaces;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

/** A trivial SessionBean interface.

@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
*/
public interface StatelessSession extends EJBObject
{
    /** A method that returns its arg */
    public String echo(String arg) throws RemoteException;
    /** A method that does nothing. It is used to test call optimization.
    */
    public void noop(ReferenceTest test, boolean optimized) throws RemoteException;
    /** Forward a request to another StatelessSession's echo method */
    public String forward(String echoArg) throws RemoteException;
}
