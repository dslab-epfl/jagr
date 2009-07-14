
//Title:        telkel
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       Marc Fleury
//Company:      telkel
//Description:  Your description

package org.jboss.test.deadlock.bean;

import java.rmi.*;
import javax.ejb.*;

import org.jboss.test.deadlock.interfaces.EnterpriseEntityHome;
import org.jboss.test.deadlock.interfaces.EnterpriseEntity;

public class EnterpriseEntityBean implements EntityBean 
{
   org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
   
  private EntityContext entityContext;
  public String name;
  public int otherField = 0;
                                                            

  public String ejbCreate(String name) throws RemoteException, CreateException {

       this.name = name;
	   return null;
  }

  public void ejbPostCreate(String name) throws RemoteException, CreateException {

	   
	   EJBObject ejbObject = entityContext.getEJBObject();
	   
	   if (ejbObject == null) {
		   log.debug("******************************* NULL EJBOBJECT in ejbPostCreate");
	   }
	   else {
			log.debug("&&&&&&&&&&&&&&&& EJBObject found in ejbPostCreate id is "+ejbObject.getPrimaryKey());   
	   }

  }

  public void ejbActivate() throws RemoteException {
  }

  public void ejbLoad() throws RemoteException {
  }

  public void ejbPassivate() throws RemoteException {

  }

  public void ejbRemove() throws RemoteException, RemoveException {
  }

  public void ejbStore() throws RemoteException {
	  
  }

  public String callBusinessMethodA() {

     return "EntityBean.callBusinessMethodA() called, my primaryKey is "+
            entityContext.getPrimaryKey().toString();
  }
  
  public String callBusinessMethodB() {

     
	 EJBObject ejbObject = entityContext.getEJBObject();
	 
	 if (ejbObject == null) 
	 	return "NULL EJBOBJECT";
	 
	 else 
	 	return ejbObject.toString();
  }
  
  
   public String callBusinessMethodB(String words) {
    
     
	 EJBObject ejbObject = entityContext.getEJBObject();
	 
	 if (ejbObject == null) 
	 	return "NULL EJBOBJECT";
	 
	 else 
	 	return ejbObject.toString()+ " words "+words;
  
	}
  public void setOtherField(int value) {
      
    otherField = value;
  }
  
  public int getOtherField() {
     return otherField;
 }
  
  public EnterpriseEntity createEntity(String newName) throws RemoteException {

    EnterpriseEntity newBean;
    try{
		EJBObject ejbObject = entityContext.getEJBObject();
		if (ejbObject == null) 
		log.debug("************************** NULL EJBOBJECT");
		else
        log.debug("************************** OK EJBOBJECT");
		
		EnterpriseEntityHome home = (EnterpriseEntityHome)entityContext.getEJBObject().getEJBHome();
	    newBean = (EnterpriseEntity)home.create(newName);

    
	}catch(Exception e)
    {
		log.debug("failed", e);
        throw new RemoteException("create entity did not work check messages");   
    }
     
     return newBean;
  }
  
  public void setEntityContext(EntityContext context) throws RemoteException {
     entityContext = context;
  }

  public void unsetEntityContext() throws RemoteException {
    entityContext = null;
  }
}
