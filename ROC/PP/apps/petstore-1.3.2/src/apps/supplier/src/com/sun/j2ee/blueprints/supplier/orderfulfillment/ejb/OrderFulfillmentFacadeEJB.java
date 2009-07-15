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

package com.sun.j2ee.blueprints.supplier.orderfulfillment.ejb;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.jms.JMSException;

import com.sun.j2ee.blueprints.xmldocuments.tpa.TPAInvoiceXDE;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.supplierpo.ejb.SupplierOrder;
import com.sun.j2ee.blueprints.supplierpo.ejb.SupplierOrderLocal;
import com.sun.j2ee.blueprints.supplierpo.ejb.SupplierOrderLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocal;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItem;
import com.sun.j2ee.blueprints.supplier.inventory.ejb.InventoryLocal;
import com.sun.j2ee.blueprints.supplier.inventory.ejb.InventoryLocalHome;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.processmanager.ejb.OrderStatusNames;

/**
 * Facade used by Supplier Order MDB - called everytime the supplier gets a PO
 * and will persist a PO, try to fulfill an order, create an Invoice for
 * a shipped order, and any other activities needed to do the work to
 * fulfill an order when a  purchase order arrives.
 */

public class OrderFulfillmentFacadeEJB implements SessionBean {

  private TPAInvoiceXDE invoiceXDE;
  private TPASupplierOrderXDE supplierOrderXDE;
  private SupplierOrderLocalHome supplierOrderLocalHome;
  private InventoryLocalHome invHome;
  private SessionContext sc;

  public OrderFulfillmentFacadeEJB() {}

  public void ejbCreate() throws CreateException {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      invoiceXDE
        = new TPAInvoiceXDE(serviceLocator.getUrl(JNDINames.XML_ENTITY_CATALOG_URL),
                            serviceLocator.getBoolean(JNDINames.XML_XSD_VALIDATION_INVOICE));
      supplierOrderXDE
        = new TPASupplierOrderXDE(serviceLocator.getUrl(JNDINames.XML_ENTITY_CATALOG_URL),
                                  serviceLocator.getBoolean(JNDINames.XML_VALIDATION_SUPPLIER_ORDER),
                                  serviceLocator.getString(JNDINames.XML_SUPPLIER_ORDER_SCHEMA));
      supplierOrderLocalHome
        = (SupplierOrderLocalHome) serviceLocator.getLocalHome(JNDINames.PO_EJB);
      invHome
        = (InventoryLocalHome) serviceLocator.getLocalHome(JNDINames.INV_EJB);
    } catch (XMLDocumentException xe) {
      throw new EJBException(xe);
    } catch (ServiceLocatorException se) {
      throw new EJBException(se);
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
   * if items are in stock then take them out of inventory to full order
   *
   * @return false if number of items in inventory is less than
   *         number in the line item of po; else return true.
   */
  private boolean checkInventory(LineItemLocal item)  {
    try {
      InventoryLocal inv = invHome.findByPrimaryKey(item.getItemId());
      if(inv.getQuantity() < item.getQuantity())
        return(false);
      inv.reduceQuantity(item.getQuantity());
    } catch (FinderException fe) {
      // swallow the finder exception because this means
      // supplier has not been populated; So we cant fulfill
      // this part of the order now
      return(false);
    }
    return(true);
  }

  /**
   * Builds an Invoice XML document containing all the items that
   *  can be shipped from inventory
   */
  private String createInvoice(SupplierOrderLocal po, HashMap newLis)
    throws XMLDocumentException {
      invoiceXDE.newDocument();
      invoiceXDE.setOrderId(po.getPoId());
      invoiceXDE.setUserId("Dear PetStore Customer");
      Date poDate = new Date(po.getPoDate());
      invoiceXDE.setOrderDate(poDate);
      Date curDate = new Date();
      invoiceXDE.setShippingDate(curDate);
      Collection items = po.getLineItems();
      Iterator it = items.iterator();
      while((it != null) && (it.hasNext())) {
        LineItemLocal anItem = (LineItemLocal)it.next();
        if(newLis.containsKey(anItem.getItemId())) {
          invoiceXDE.addLineItem(anItem.getCategoryId(),
                                 anItem.getProductId(),
                                 anItem.getItemId(),
                                 anItem.getLineNumber(),
                                 anItem.getQuantity(),
                                 anItem.getUnitPrice());
        }
      }//end while

      String invDoc = null;
      invDoc = invoiceXDE.getDocumentAsString();
      return(invDoc);
  }

  /**
   * Tries to fullfill an order with items in inventory
   */
  private String processAnOrder(SupplierOrderLocal po)
    throws XMLDocumentException  {
      boolean allItemsAvailable = true;
      boolean invoiceReqd = false;
      String invoiceXml = null;

      HashMap items = new HashMap();
      Collection liColl = po.getLineItems();
      Iterator liIt = liColl.iterator();
      while((liIt != null) && (liIt.hasNext())) {
        LineItemLocal li = (LineItemLocal) liIt.next();
        if(li.getQuantity() == li.getQuantityShipped())
          continue;
        if(!checkInventory(li)) {
          allItemsAvailable = false;
          continue;
        }
        li.setQuantityShipped(li.getQuantity());
        items.put(li.getItemId(), OrderStatusNames.COMPLETED);
        invoiceReqd = true;
      }//end while
      if(allItemsAvailable)
        po.setPoStatus(OrderStatusNames.COMPLETED);
      if(invoiceReqd) {
        try {
          invoiceXml = (createInvoice(po, items));
        } catch (XMLDocumentException xe) {
          //so order wont be fullfilled but po is persisted
          //and can be fullfilled later.
          System.out.println("OrderFulfillmentFacade**" + xe);
          return null;
        }
      }
      return invoiceXml;
  }

  /**
   * This method processes the incoming PO received by the supplier. It
   * persists the order, and then it tries to fullfill the order with items
   * in Inventory and packages and ships as much of the order as possible to
   * the customer and returns an Invoice indicating the shipment
   *
   * @param String Serialized XML document which represents the PO
   * @return String Serialized XML document that is the invoice and
   *          may return null if the invoice has no items or nothing
   *          could be shipped now
   */
  public String processPO(String poXmlDoc) throws CreateException,
      XMLDocumentException {
    String invoiceXml = null;
    SupplierOrderLocal order = null;

    // Convert Po in XML to PO as Java Obj
    SupplierOrder supplierOrder = null;
    supplierOrderXDE.setDocument(poXmlDoc);
    supplierOrder = supplierOrderXDE.getSupplierOrder();
    //persist order
    order = supplierOrderLocalHome.create(supplierOrder);
    //fullfil order
    invoiceXml = processAnOrder(order);
    return invoiceXml;
  }

  /**
   * Called by the web tier when new inventory arrives, so we want to try
   * and fulfill any orders that are still pending
   *
   * @return a <code>Collection</code> of Invoices that are generated by
   *         fulfilling pending orders with the items that have just arrived
   *         in inventory
   */
  public Collection processPendingPO() throws FinderException {
    ArrayList invoices = new ArrayList();

    Collection coll =
      supplierOrderLocalHome.findOrdersByStatus(OrderStatusNames.PENDING);
    if(coll != null) {
      Iterator it = coll.iterator();
      while((it!=null) && (it.hasNext())) {
        SupplierOrderLocal order = (SupplierOrderLocal) it.next();
        String newInvoice = null;
        try {
          newInvoice = processAnOrder(order);
        } catch (XMLDocumentException xe) {
          // ignore since means we cant fulfill this order now
          System.out.println("OrderFulfillmentFacade:" + xe);
        }
        if(newInvoice != null) {
          invoices.add(newInvoice);
        }
      }//end while
    }
    return invoices;
  }
}
