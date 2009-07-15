
//Title:        telkel
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       Marc Fleury
//Company:      telkel
//Description:  Your description

package org.jboss.test.deadlock.bean;

import java.rmi.*;

import java.util.Arrays;

import javax.ejb.*;

import org.jboss.ejb.plugins.lock.ApplicationDeadlockException;
import org.jboss.test.deadlock.interfaces.BeanOrder;
import org.jboss.test.deadlock.interfaces.EnterpriseEntityHome;
import org.jboss.test.deadlock.interfaces.EnterpriseEntity;

public class EnterpriseEntityBean implements EntityBean 
{
   org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
   
  private EntityContext entityContext;
  public String name;
  public int otherField = 0;

  public String ejbCreate(String name) throws CreateException {

       this.name = name;
	   return null;
  }

  public void ejbPostCreate(String name) throws CreateException {

  }

  public void ejbActivate() {
  }

  public void ejbLoad() {
  }

  public void ejbPassivate() {

  }

  public void ejbRemove() throws RemoveException {
  }

  public void ejbStore() {
	  
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

  public void callAnotherBean(BeanOrder beanOrder)
  {
     // Force the bean lock to go through one wait cycle?
/*     try
     {
        Thread.currentThread().sleep(6000);
     }
     catch (Exception e)
     {
     }
*/
     // End of the chain
     if (beanOrder.next == beanOrder.order.length-1)
        return;

     // Call the next in the chain
     try
     {
        EnterpriseEntityHome home = (EnterpriseEntityHome)entityContext.getEJBObject().getEJBHome();
        beanOrder.next++;
        EnterpriseEntity nextBean = home.findByPrimaryKey(beanOrder.order[beanOrder.next]);
        try
        {
           nextBean.callAnotherBean(beanOrder);
        }
        finally
        {
           beanOrder.next--;
        }
     }
     catch (Exception e)
     {
        ApplicationDeadlockException a = ApplicationDeadlockException.isADE(e);
        if (a == null)
        {
           log.error("Error next=" + beanOrder.next + " order=" + Arrays.asList(beanOrder.order), e);
           throw new EJBException("callAnotherBean failed " + e.toString());
        }
        else
           throw new EJBException(a);
     }
  }
  
  public EnterpriseEntity createEntity(String newName) {

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
        throw new EJBException("create entity did not work check messages");   
    }
     
     return newBean;
  }
  
  public void setEntityContext(EntityContext context) {
     entityContext = context;
  }

  public void unsetEntityContext() {
    entityContext = null;
  }
}
