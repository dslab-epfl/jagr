
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
	 log.debug("callAB start");
	 EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgen.EnterpriseEntity");
	 EnterpriseEntity A = home.findByPrimaryKey("A");
	 EnterpriseEntity B = home.findByPrimaryKey("B");
	 A.getOtherField();
	 log.debug("callAB is sleeping");
	 Thread.sleep(10000);
	 log.debug("callAB woke up");
	 B.getOtherField();
	 log.debug("callAB end");
      }
      catch (Exception ex)
      {
	  log.debug("failed", ex);
	 throw new RemoteException("failed");
      }
   }
   
   public void callBA() throws RemoteException
   {
      try
      {
	 log.debug("callBA start");
	 EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgen.EnterpriseEntity");
	 EnterpriseEntity B = home.findByPrimaryKey("B");
	 EnterpriseEntity A = home.findByPrimaryKey("A");
	 B.getOtherField();
	 log.debug("callBA is sleeping");
	 Thread.sleep(10000);
	 log.debug("callBA woke up");
	 A.getOtherField();
	 log.debug("callBA end");
      }
      catch (Exception ex)
      {
	  log.debug("failed", ex);
	 throw new RemoteException("failed");
      }
   }

   public void requiresNewTest(boolean first) throws RemoteException
   {
      try
      {
	 log.debug("requiresNewTest start");
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
      catch (Exception ex)
      {
	  log.debug("failed", ex);
	 throw new RemoteException("failed");
      }
   }

}
