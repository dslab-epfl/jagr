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

package com.sun.j2ee.blueprints.supplier.inventory.web;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.ServletOutputStream;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import java.util.Enumeration;
import java.util.Collection;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;

import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.HeuristicMixedException;

import com.sun.j2ee.blueprints.supplier.orderfulfillment.ejb.OrderFulfillmentFacadeLocal;
import com.sun.j2ee.blueprints.supplier.orderfulfillment.ejb.OrderFulfillmentFacadeLocalHome;
import com.sun.j2ee.blueprints.supplier.inventory.ejb.InventoryLocal;
import com.sun.j2ee.blueprints.supplier.inventory.ejb.InventoryLocalHome;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;

import com.sun.j2ee.blueprints.supplier.inventory.web.JNDINames;

import com.sun.j2ee.blueprints.servicelocator.web.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

import com.sun.j2ee.blueprints.processmanager.transitions.*;

/**
 * This servlet processes requests from receiver of the supplier component
 * The user is expected to have logged in as "supplier"
 */
public class RcvrRequestProcessor extends HttpServlet {
  boolean fromDoGet = false;
  private TransitionDelegate transitionDelegate;
  private InventoryLocalHome inventoryHomeRef = null;
  private OrderFulfillmentFacadeLocalHome orderFacadeHomeRef = null;
  private OrderFulfillmentFacadeLocal procPO = null;

  public void init() {
    try {
      ServiceLocator serviceLocator =  ServiceLocator.getInstance();
      inventoryHomeRef = (InventoryLocalHome)
          serviceLocator.getLocalHome(JNDINames.INV_EJB);
      orderFacadeHomeRef = (OrderFulfillmentFacadeLocalHome)
          serviceLocator.getLocalHome(JNDINames.ORDERFACADE_EJB);
      procPO = orderFacadeHomeRef.create();
    } catch(ServiceLocatorException se) {
      se.printStackTrace();
    } catch(CreateException ce) {
      ce.printStackTrace();
    }
  }

  /**
   * Method builds an arraylist of items whose quantity has to be updated
   * @param <Code>HttpServletRequest</Code> the request that came in
   */
  private void updateInventory(HttpServletRequest req) {

    String itemId;
    InventoryLocal inventory;

    try {
      Enumeration e = req.getParameterNames();
      while ((e != null) && e.hasMoreElements()) {
        String param = ((String)e.nextElement()).trim();
        if ((param != null) && param.startsWith("item_")) {
          // get the item id number from the parameter
          itemId = param.substring("item_".length(),param.length());
          if(itemId != null) {
            String newQty = req.getParameter("qty_" + itemId);
            if((newQty == null) || (newQty.length() <= 0))
              continue;
            Integer qty = new Integer(newQty);
            if(qty.intValue() >= 0 ) {
              inventory = inventoryHomeRef.findByPrimaryKey(itemId);
              inventory.setQuantity(qty.intValue());
            }
          }
        }
      }
    } catch (FinderException ne) {
      ne.printStackTrace();
    }
  }

  private void sendInvoices(Collection invoices) {
    try {
      ServiceLocator serviceLocator = ServiceLocator.getInstance();
      String tdClassName = serviceLocator.getString(JNDINames.TRANSITION_DELEGATE__SUPPLIER_ORDER);
      TransitionDelegateFactory tdf = new TransitionDelegateFactory();
      transitionDelegate = tdf.getTransitionDelegate(tdClassName);
      transitionDelegate.setup();
      Iterator it = invoices.iterator();
      while((it!=null) && (it.hasNext())) {
        String anInvoice = (String) it.next();
        TransitionInfo info = new TransitionInfo(anInvoice);
        transitionDelegate.doTransition(info);
      }
    } catch(ServiceLocatorException se) {
      se.printStackTrace();
    } catch(TransitionException te) {
      te.printStackTrace();
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws java.io.IOException, javax.servlet.ServletException {
      fromDoGet = true;
      doPost(req, resp);
      fromDoGet = false;
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws java.io.IOException, javax.servlet.ServletException {

      if(fromDoGet) {
        getServletConfig().getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
      } else {
        String curScreen = req.getParameter("currentScreen").trim();
        if(curScreen.equals("logout")) {
          getServletConfig().getServletContext().getRequestDispatcher("/logout.jsp").forward(req, resp);
        }
        if(curScreen.equals("displayinventory")) {
          getServletConfig().getServletContext().getRequestDispatcher("/displayinventory.jsp").forward(req, resp);
        }
        if(curScreen.equals("updateinventory")) {
          UserTransaction ut = null;
          try {
            InitialContext ic = new InitialContext();
            ut = (UserTransaction) ic.lookup(JNDINames.USER_TRANSACTION);
            ut.begin();        // start the xaction.
            updateInventory(req);
            Collection invoices = procPO.processPendingPO();
            sendInvoices(invoices);
            ut.commit();        // end xaction
            getServletConfig().getServletContext().getRequestDispatcher("/back.jsp").forward(req, resp);
          } catch (FinderException fe) {
            fe.printStackTrace();
          } catch (NamingException ne) {
            ne.printStackTrace();
          } catch (NotSupportedException nse) {
            nse.printStackTrace();
          } catch (IllegalStateException re) {
            re.printStackTrace();
          } catch (RollbackException re) {
            re.printStackTrace();
          } catch (HeuristicMixedException hme) {
            hme.printStackTrace();
          } catch (HeuristicRollbackException hre) {
            hre.printStackTrace();
          } catch (SystemException se) {
            se.printStackTrace();
          }
        }
      }
  }
}

