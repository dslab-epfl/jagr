
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.test.jmx.interfaces;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.*;

/**
 *      
 *   @see <related>
 *   @author $Author: candea $
 *   @version $Revision: 1.1.1.1 $
 */
public interface TestDataSource
   extends EJBObject
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   void testDataSource(String dsName)
      throws RemoteException;

   boolean isBound(String name) throws RemoteException;
   
}

