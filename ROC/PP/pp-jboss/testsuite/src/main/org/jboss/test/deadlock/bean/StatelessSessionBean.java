
//Title:        Yo MAMA!
//Version:
//Copyright:    Copyright (c) 1999
//Author:       Bill Burke
//Description:  Your description

package org.jboss.test.deadlock.bean;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import org.jboss.test.deadlock.interfaces.*;
import org.jboss.ejb.plugins.lock.ApplicationDeadlockException;

public class StatelessSessionBean implements SessionBean 
{
   org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
   
  private SessionContext sessionContext;

   public void ejbCreate() throws RemoteException, CreateException 
   {
   }
   
   public void ejbActivate() throws RemoteException {
   }
   
   public void ejbPassivate() throws RemoteException {
   }
   
   public void ejbRemove() throws RemoteException {
   }
   
   public void setSessionContext(SessionContext context) throws RemoteException {
      sessionContext = context;
      //Exception e = new Exception("in set Session context");
      //log.debug("failed", e);
   }
   
   
   public void callAB() throws RemoteException
   {
      try
      {
	 log.info("****callAB start****");
	 EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgen.EnterpriseEntity");
	 EnterpriseEntity A = home.findByPrimaryKey("A");
	 EnterpriseEntity B = home.findByPrimaryKey("B");
	 A.getOtherField();
	 log.debug("callAB is sleeping");
	 Thread.sleep(1000);
	 log.debug("callAB woke up");
	 B.getOtherField();
	 log.debug("callAB end");
      }
      catch (RemoteException rex)
      {
         throw rex;
      }
      catch (Exception ex)
      {
         throw new RemoteException("Exception", ex);
      }
   }
   
   public void callBA() throws RemoteException
   {
      try
      {
	 log.info("****callBA start****");
	 EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgen.EnterpriseEntity");
	 EnterpriseEntity B = home.findByPrimaryKey("B");
	 EnterpriseEntity A = home.findByPrimaryKey("A");
	 B.getOtherField();
	 log.debug("callBA is sleeping");
	 Thread.sleep(1000);
	 log.debug("callBA woke up");
	 A.getOtherField();
	 log.debug("callBA end");
      }
      catch (RemoteException rex)
      {
         throw rex;
      }
      catch (Exception ex)
      {
         throw new RemoteException("Exception", ex);
      }
   }
   
   public void callABLocal() throws RemoteException
   {
      try
      {
	 log.info("****callABLocal start****");
	 EnterpriseEntityLocalHome home = (EnterpriseEntityLocalHome)new InitialContext().lookup("local/nextgen.EnterpriseEntity");
	 EnterpriseEntityLocal A = home.findByPrimaryKey("A");
	 EnterpriseEntityLocal B = home.findByPrimaryKey("B");
	 A.getOtherField();
	 log.debug("callABLocal is sleeping");
	 Thread.sleep(1000);
	 log.debug("callABLocal woke up");
	 B.getOtherField();
	 log.debug("callABLocal end");
      }
      catch (Exception ex)
      {
         throw new RemoteException("Exception", ex);
      }
   }
   
   public void callBALocal() throws RemoteException
   {
      try
      {
	 log.info("****callBALocal start****");
	 EnterpriseEntityLocalHome home = (EnterpriseEntityLocalHome)new InitialContext().lookup("local/nextgen.EnterpriseEntity");
	 EnterpriseEntityLocal B = home.findByPrimaryKey("B");
	 EnterpriseEntityLocal A = home.findByPrimaryKey("A");
	 B.getOtherField();
	 log.debug("callBALocal is sleeping");
	 Thread.sleep(1000);
	 log.debug("callBALocal woke up");
	 A.getOtherField();
	 log.debug("callBALocal end");
      }
      catch (Exception ex)
      {
         throw new RemoteException("Exception", ex);
      }
   }

   public void requiresNewTest(boolean first) throws RemoteException
   {
      try
      {
	 log.info("***requiresNewTest start***");
         InitialContext ctx = new InitialContext();
	 EnterpriseEntityHome home = (EnterpriseEntityHome)ctx.lookup("nextgen.EnterpriseEntity");
	 EnterpriseEntity C = home.findByPrimaryKey("C");

         C.getOtherField();
         if (first)
         {
            StatelessSessionHome shome = (StatelessSessionHome)ctx.lookup("nextgen.StatelessSession");
            StatelessSession session = shome.create();
            session.requiresNewTest(false);
         }
      }
      catch (RemoteException rex)
      {
         throw rex;
      }
      catch (Exception ex)
      {
         throw new RemoteException("failed");
      }
   }

}
