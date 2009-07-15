package org.jboss.test.deadlock.interfaces;


import javax.ejb.*;
import java.rmi.*;



public interface EnterpriseEntityLocal extends EJBLocalObject {

  public String callBusinessMethodA();
  public String callBusinessMethodB();
  public String callBusinessMethodB(String words);
  public void setOtherField(int value);
  public int getOtherField();
  public void callAnotherBean(BeanOrder beanOrder);
  public EnterpriseEntity createEntity(String newName);

}
