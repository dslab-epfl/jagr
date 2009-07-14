/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jboss.test.bank.interfaces;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.*;

/**
 *      
 *   @see <related>
 *   @author $Author: mikechen $
 *   @version $Revision: 1.1.1.1 $
 */
public interface Bank
   extends EJBObject
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public String getId()
      throws RemoteException;
   
   public String createAccountId(Customer customer)
      throws RemoteException;
      
   public String createCustomerId()
      throws RemoteException;
}

/*
 *   $Id: Bank.java,v 1.1.1.1 2002/11/16 03:16:42 mikechen Exp $
 *   Currently locked by:$Locker:  $
 *   Revision:
 *   $Log: Bank.java,v $
 *   Revision 1.1.1.1  2002/11/16 03:16:42  mikechen
 *   Import of JBoss v3.0.3
 *
 *   Revision 1.2  2001/01/07 23:14:35  peter
 *   Trying to get JAAS to work within test suite.
 *
 *   Revision 1.1.1.1  2000/06/21 15:52:38  oberg
 *   Initial import of jBoss test. This module contains CTS tests, some simple examples, and small bean suites.
 *
 *
 *  
 */
