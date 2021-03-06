package org.jboss.test.naming.interfaces;

import java.rmi.RemoteException;

import javax.ejb.EJBLocalObject;

/**

@author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian.Brock</a>
@version $Revision: 1.1.1.1 $
*/
public interface TestEjbLinkLocal extends EJBLocalObject
{
   /**
    * Call a bean with the passed jndi name bound using ejb-link
    * @param jndiName the name of the bean specified in ejb-ref
    * @return the result of the call or "Failed" on an error
    */
   public String testEjbLinkCallerLocal(String jndiName)
      throws RemoteException;

   /**
    * Called by a bean specified in ejb-link
    * @return the string "Works"
    */
   public String testEjbLinkCalled()
      throws RemoteException;
}
