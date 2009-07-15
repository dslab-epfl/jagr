/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

package com.sun.j2ee.blueprints.processmanager.ejb;

import java.util.Collection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.DuplicateKeyException;

import com.sun.j2ee.blueprints.processmanager.manager.ejb.ManagerLocal;
import com.sun.j2ee.blueprints.processmanager.manager.ejb.ManagerLocalHome;

public class ProcessManagerEJB implements SessionBean {

    private static final String MANAGER_HOME_ENV_NAME = "java:comp/env/ejb/Manager";

    private ManagerLocalHome mlh;
    private InitialContext ic;

    /**
     * Business method used for when new purchase orders are recieved
     * and want to start the workflow process to fullfil the order
     */
    public void createManager(String orderId, String status) throws CreateException {
      ManagerLocal manager = mlh.create(orderId, status);
    }

    /**
     * Business method used to keep track of an order in the workflow process
     */
    public void updateStatus(String orderId, String status) throws FinderException {
      ManagerLocal manager = mlh.findByPrimaryKey(orderId);
      manager.setStatus(status);
    }

    /**
     * Business method used to keep track of an order in the workflow process
     */
    public String getStatus(String orderId) throws FinderException {
      ManagerLocal manager = mlh.findByPrimaryKey(orderId);
      return manager.getStatus();
    }

    /**
     * Business method to get all orders of given status for the admin
     */
    public Collection getOrdersByStatus(String status) throws FinderException {
        return mlh.findOrdersByStatus(status);
    }

    public void ejbCreate() throws CreateException {
      try {
         ic = new InitialContext();
         mlh = (ManagerLocalHome) ic.lookup(MANAGER_HOME_ENV_NAME);
      } catch (NamingException ne) {
         throw new EJBException("Got naming exception! " + ne.getMessage());
      }
    }

    // Misc Method
    //=============
    //activate is empty for stateless session EJBs
    public void ejbActivate() { }

    //passivate is empty for stateless session EJBs
    public void ejbPassivate() { }

    public void setSessionContext(SessionContext c) { }
    public void ejbRemove() { }

}
