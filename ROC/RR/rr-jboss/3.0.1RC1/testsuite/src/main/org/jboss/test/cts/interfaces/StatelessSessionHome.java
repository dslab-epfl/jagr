package org.jboss.test.cts.interfaces;



import java.rmi.*;
import javax.ejb.*;


/**
 *
 *   @see <related>
 *   @author $Author: candea $
 *   @version $Revision: 1.1.1.1 $
 */

public interface StatelessSessionHome
   extends EJBHome
{

   /**
    * Method create
    *
    *
    * @return
    *
    */

   public StatelessSession create ()
      throws RemoteException, CreateException;

   /* The following included will not deploy as per the
      EJB 1.1 spec: [6.8] "There can be no other create methods
      in the home interface.
   public StatelessSession create( String aString);
   */
}


/*------ Formatted by Jindent 3.23 Basic 1.0 --- http://www.jindent.de ------*/
