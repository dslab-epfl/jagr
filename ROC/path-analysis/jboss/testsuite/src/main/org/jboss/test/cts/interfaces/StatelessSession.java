/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.test.cts.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;


/** Interface for tests of stateless sessions
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public interface StatelessSession
   extends EJBObject
{
   public String method1 (String msg)
      throws RemoteException;

   public void loopbackTest ()
      throws RemoteException;

   public void loopbackTest(EJBObject obj)
      throws RemoteException;

   public void callbackTest(ClientCallback callback, String data)
      throws RemoteException;

   public void npeError() throws RemoteException;
}
