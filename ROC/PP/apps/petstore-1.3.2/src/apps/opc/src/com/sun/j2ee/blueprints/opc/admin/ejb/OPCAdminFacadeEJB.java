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

package com.sun.j2ee.blueprints.opc.admin.ejb;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.rmi.RemoteException;

import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocal;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItem;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocal;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocalHome;
import com.sun.j2ee.blueprints.processmanager.manager.ejb.ManagerLocal;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;

/**
 * This is the Facade that OPC gives to admin client requests
 */
public class OPCAdminFacadeEJB implements SessionBean {

    private String PURCHASE_ORDER_EJB = "java:comp/env/ejb/PurchaseOrder";
    private String PROCMGR_ORDER_EJB = "java:comp/env/ejb/ProcessManager";
    private SessionContext sc;
    private PurchaseOrderLocalHome poLocalHome;
    private ProcessManagerLocal processManagerLocal;

    public OPCAdminFacadeEJB() {}

    public void ejbCreate() throws CreateException {
      try {
         ServiceLocator serviceLocator = new ServiceLocator();
         poLocalHome = (PurchaseOrderLocalHome) serviceLocator.getLocalHome(PURCHASE_ORDER_EJB);
         ProcessManagerLocalHome processManagerLocalHome = (ProcessManagerLocalHome)serviceLocator.getLocalHome(PROCMGR_ORDER_EJB);
         processManagerLocal = processManagerLocalHome.create();
      } catch(ServiceLocatorException se) {
          throw new EJBException(se);
      } catch (CreateException ce) {
          throw new EJBException(ce);
      }
    }

    public void ejbPostCreate() throws CreateException {}

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    /**
     * Private helper method that looks up the purchase order EJB
     * @return <code>PurchaseOrderLocalHome</code>
     * @throws <code>NamingException</code>
     */
    private PurchaseOrderLocalHome getPO() {
      return poLocalHome;
    }

    /**
     * Private helper method that looks up the process manager EJB and gets it
     * @return <code>ProcessManagerLocal</code>
     * @throws <code>NamingException</code>
     * @throws <code>CreateException</code>
     */
    private ProcessManagerLocal getProcMgr() {
      return processManagerLocal;
    }

    /**
     * Business method in the remote interface; Returns a collection of
     * of orders that are in the give status.
     * @param status   Represents the status of orders that are requested
     * @return <code>OrdersTO</code>
     * @throws <code>OPCAdminFacadeException</code> for all errors
     */

    public OrdersTO getOrdersByStatus(String status)
                                              throws OPCAdminFacadeException {
        OrdersTO.MutableOrdersTO retVal = new OrdersTO.MutableOrdersTO();
        PurchaseOrderLocal po;
        ProcessManagerLocal mgr = getProcMgr();

        try {
            PurchaseOrderLocalHome pohome = getPO();
            Collection orders = mgr.getOrdersByStatus(status);
            Iterator it = orders.iterator();
            while((it!= null) && (it.hasNext())) {
                ManagerLocal mgrEjb = (ManagerLocal) it.next();
                po = pohome.findByPrimaryKey(mgrEjb.getOrderId());
                Date gotDate = new Date(po.getPoDate());
                String podate = (gotDate.getMonth()+1) + "/" +
                    gotDate.getDate() + "/" + (gotDate.getYear()+1900);
                retVal.add(new OrderDetails(po.getPoId(), po.getPoUserId(),
                                            podate, po.getPoValue(), status));
            }
        } catch(FinderException fe) {
            System.err.println("finder Ex while getOrdByStat :" + fe.getMessage());
            throw new OPCAdminFacadeException("Unable to find PurchaseOrders"+
                                              " of given status : " +
                                              fe.getMessage());
        }
        return(retVal);
    }

    /**
     * Business method in remote interface. Given start, end dates, this
     * returns a <code>HashMap</code> of revenue / orders in the given period
     * @param request  Represents the type of request - ORDERS/REVENUE
     * @param start    Represents the start date in mm/dd/yyyy format
     * @param end      Represents the end date in mm/dd/yyyy format
     * @param categ    Represents the sub-classification
     * @return <code>HashMap</code> of the required details
     * @throws <code>OPCAdminFacadeException</code> for all errors
     */
    public Map getChartInfo(String request,
                            Date start,
                            Date end,
                            String categ)
        throws OPCAdminFacadeException {
        String id;
        Float tmpValue;
        Integer tmpQty;
        PurchaseOrderLocal po;

        Map chartDetails = new HashMap();

        try {
            PurchaseOrderLocalHome pohome = getPO();
            Collection poColl =
                pohome.findPOBetweenDates(start.getTime(), end.getTime());

            Iterator it = poColl.iterator();
            while((it!= null) && (it.hasNext())) {
                po = (PurchaseOrderLocal) it.next();
                Collection liColl = po.getAllItems();
                Iterator lit = liColl.iterator();
                while((lit!=null) && (lit.hasNext())) {
                    LineItem loc = (LineItem) lit.next();
                    if(request.equals("REVENUE")) {
                        if(categ == null) {
                            id = loc.getCategoryId();
                            if(chartDetails.containsKey(id)) {
                                tmpValue = (Float) chartDetails.get(id);
                                chartDetails.put(id,
                                     new Float(tmpValue.floatValue() +
                                     (loc.getQuantity()*loc.getUnitPrice())));
                            } else {
                                chartDetails.put(id, new Float((loc.getQuantity()*
                                                        loc.getUnitPrice())));
                            }
                        } else {
                            if(loc.getCategoryId().equals(categ)) {
                                id = loc.getItemId();
                                if(chartDetails.containsKey(id)) {
                                    tmpValue = (Float) chartDetails.get(id);
                                    chartDetails.put(id,
                                      new Float(tmpValue.floatValue() +
                                      (loc.getQuantity()*loc.getUnitPrice())));
                                } else {
                                    chartDetails.put(id,
                                     new Float(loc.getQuantity()*loc.getUnitPrice()));
                                }
                            }
                        }
                    } else {
                        if(categ == null) {
                            id = loc.getCategoryId();
                            if(chartDetails.containsKey(id)) {
                                tmpQty = (Integer) chartDetails.get(id);
                                chartDetails.put(id,
                                     new Integer(tmpQty.intValue() +
                                                 loc.getQuantity()));
                            } else {
                                chartDetails.put(id, new
                                                 Integer(loc.getQuantity()));
                            }
                        } else {
                            if(loc.getCategoryId().equals(categ)) {
                                id = loc.getItemId();
                                if(chartDetails.containsKey(id)) {
                                    tmpQty = (Integer) chartDetails.get(id);
                                    chartDetails.put(id,
                                            new Integer(tmpQty.intValue() +
                                            loc.getQuantity()));
                                } else {
                                    chartDetails.put(id,
                                           new Integer(loc.getQuantity()));
                                }
                            }
                        }
                    }
                }
            }
        } catch(FinderException fe) {
            System.err.println("finder Ex while getChart :" +
                    fe.getMessage());
            throw new OPCAdminFacadeException("Unable to find PurchaseOrders"+
                                              " in given period : " +
                                              fe.getMessage());
        }
        return(chartDetails);
    }
}
