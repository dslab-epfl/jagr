package javax.management.j2ee;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
* Home Interface of the Management EJB
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public interface ManagementHome
   extends EJBHome
{

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------
   
   public Management create()
      throws
         CreateException,
         RemoteException;

}
