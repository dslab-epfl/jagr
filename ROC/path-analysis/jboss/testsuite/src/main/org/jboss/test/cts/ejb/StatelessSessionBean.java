package org.jboss.test.cts.ejb;

import org.jboss.test.util.ejb.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.*;

import org.apache.log4j.Category;
import org.jboss.test.cts.interfaces.*;
import org.jboss.test.util.ejb.SessionSupport;

/** The stateless session bean implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class StatelessSessionBean
   extends SessionSupport
{
    private static Category log = Category.getInstance(StatelessSessionBean.class);

   public void ejbCreate ()
      throws CreateException
   {
   }

   public String method1 (String msg)
   {
      return msg;
   }

   public void loopbackTest ()
      throws java.rmi.RemoteException
   {
      try
      {
        InitialContext ctx = new InitialContext();
        StatelessSessionHome home =( StatelessSessionHome ) ctx.lookup("ejbcts/StatelessSessionBean");
        StatelessSession sessionBean;
        try 
        {
           sessionBean = home.create();
        } 
        catch (CreateException ex) 
        {
           log.debug("Loopback CreateException: " + ex);
           throw new EJBException(ex);
        } 
        sessionBean.loopbackTest(sessionCtx.getEJBObject());
      }
      catch (javax.naming.NamingException nex)
      {
         log.debug("Could not locate bean instance");
      }
   }

   public void loopbackTest (EJBObject obj)
      throws java.rmi.RemoteException
   {
      // This should throw an exception. 
      StatelessSession bean = ( StatelessSession ) obj;
      bean.method1("Hello");
   }

   public void callbackTest(ClientCallback callback, String data)
      throws java.rmi.RemoteException
   {
      callback.callback(data);
   }

    public void npeError()
    {
       Object obj = null;
       obj.toString();
    }
}
