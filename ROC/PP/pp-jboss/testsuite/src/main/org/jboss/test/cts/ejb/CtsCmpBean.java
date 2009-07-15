package org.jboss.test.cts.ejb;

import java.rmi.RemoteException;
import javax.naming.*;
import javax.ejb.*;

import org.jboss.test.cts.keys.AccountPK;

public class CtsCmpBean
   implements EntityBean
{
   org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
   
   EntityContext ctx = null;

   // cmp fields
   public AccountPK pk;
   public String personsName;

   public AccountPK ejbCreate (AccountPK pk, String personsName)
      throws CreateException, DuplicateKeyException, EJBException,
             RemoteException
   {
      log.debug("entry ejbCreate, pk="+pk);
      this.pk = pk;
      this.personsName = personsName;
      return null;
   }

   public void ejbPostCreate (AccountPK pk, String personsName)
      throws CreateException, DuplicateKeyException, EJBException,
             RemoteException
   {
      log.debug("ejbPostCreate (AccountPK, String) called");
   }

   public void ejbLoad ()
      throws EJBException, RemoteException
   {
      log.debug("ejbLoad () called");

   }

   public void ejbStore ()
      throws EJBException, RemoteException
   {
      log.debug("ejbStore () called");

   }

   public void ejbRemove ()
      throws EJBException, RemoteException
   {
      log.debug("ejbRemove () called");

   }

   public void ejbActivate ()
      throws EJBException, RemoteException
   {
      log.debug("ejbActivate () called");
   }

   public void ejbPassivate ()
      throws EJBException, RemoteException
   {
      log.debug("ejbPassivate () called");
   }

   public void setEntityContext (EntityContext ctx)
      throws EJBException, RemoteException
   {
      log.debug("setEntityContext ('" + ctx.getPrimaryKey() + "') called");
      this.ctx = ctx;
   }

   public void unsetEntityContext ()
      throws EJBException, RemoteException
   {
      log.debug("unsetEntityContext () called");

      ctx = null;
   }

   public void setPersonsName (String personsName)
      throws RemoteException
   {
      this.personsName = personsName;
   }

   public String getPersonsName ()
      throws RemoteException
   {
      return this.personsName;
   }

}

/*------ Formatted by Jindent 3.23 Basic 1.0 --- http://www.jindent.de ------*/
