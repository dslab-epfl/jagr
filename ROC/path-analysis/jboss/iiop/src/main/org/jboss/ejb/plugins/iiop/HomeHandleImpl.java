/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.iiop;

import java.rmi.RemoteException;
import java.util.Properties;
import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;
import javax.rmi.PortableRemoteObject;

import org.jboss.iiop.CorbaORB;

/**
 * A CORBA-based EJB home handle implementation.
 *      
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>.
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 1.1.1.1 $
 */
public class HomeHandleImpl
      implements HomeHandle 
{

   /**
    * This handle encapsulates an stringfied CORBA reference for an 
    * <code>EJBHome</code>. 
    */
   private String ior;
   
   /**
    * Constructs a <code>HomeHandleImpl</code>.
    *
    * @param ior     An stringfied CORBA reference for an <code>EJBHome</code>.
    */
   public HomeHandleImpl(String ior) 
   {
      this.ior = ior;
   }
   
   /**
    * Constructs a <tt>HomeHandleImpl</tt>.
    *
    * @param home    An <code>EJBHome</code>.
    */
   public HomeHandleImpl(EJBHome home) 
   {
      this((org.omg.CORBA.Object)home);
   }
   
   /**
    * Constructs a <tt>HomeHandleImpl</tt>.
    *
    * @param home    A CORBA reference for an <code>EJBHome</code>.
    */
   public HomeHandleImpl(org.omg.CORBA.Object home) 
   {
      this.ior = CorbaORB.getInstance().object_to_string(home);
   }
   
   // Public --------------------------------------------------------
   
   // Handle implementation -----------------------------------------
   
   /**
    * Obtains the <code>EJBHome</code> represented by this home handle.
    *
    * @return  a reference to an <code>EJBHome</code>.
    *
    * @throws RemoteException
    */
   public EJBHome getEJBHome() 
         throws RemoteException 
   {
      try {
         return (EJBHome)PortableRemoteObject.narrow(
                                 CorbaORB.getInstance().string_to_object(ior),
                                 EJBHome.class);
      }
      catch (Exception e) {
         throw new RemoteException("Could not get EJBHome from HomeHandle");
      }
   }
   
}
