/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jboss.test.idgen.interfaces;

import java.util.*;
import java.rmi.*;
import javax.ejb.*;

/**
 *      
 *   @see <related>
 *   @author $Author: candea $
 *   @version $Revision: 1.1.1.1 $
 */
public interface IdCounterHome
   extends EJBHome
{
   // Constants -----------------------------------------------------
   public static final String COMP_NAME = "java:comp/env/ejb/IdCounter";
   public static final String JNDI_NAME = "IdCounter";
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public IdCounter create(String beanName)
      throws RemoteException, CreateException;
   
   public IdCounter findByPrimaryKey(String beanName)
      throws RemoteException, FinderException;
      
   public Collection findAll()
      throws RemoteException, FinderException;
}
