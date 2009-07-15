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

package com.sun.j2ee.blueprints.opc.ejb;

import java.util.Locale;
import java.net.URL;

import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.*;

import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrder;
import com.sun.j2ee.blueprints.xmldocuments.ChangedOrder;
import com.sun.j2ee.blueprints.xmldocuments.OrderApproval;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocalHome;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocal;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocalHome;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocal;
import com.sun.j2ee.blueprints.processmanager.ejb.OrderStatusNames;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.opc.transitions.*;
import com.sun.j2ee.blueprints.processmanager.transitions.*;


/**
 * Message Driven Bean receives a JMS message containing a purchase order
 * from a customer placing an order at Java Pet Store. It creates the
 * POEJB to begin the process of fulfilling the order.
 */
public class PurchaseOrderMDB implements MessageDrivenBean, MessageListener {

  private Context context;
  private MessageDrivenContext mdc = null;
  private TransitionDelegate transitionDelegate;
  private ProcessManagerLocal     processManager;
  private PurchaseOrderLocalHome  poHome;
  private URL entityCatalogURL;
  private boolean validateXmlPurchaseOrder;


  public PurchaseOrderMDB() {
  }

  public void ejbCreate() {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      poHome = (PurchaseOrderLocalHome)serviceLocator.getLocalHome(JNDINames.PURCHASE_ORDER_EJB);
      ProcessManagerLocalHome pmlh = (ProcessManagerLocalHome)serviceLocator.getLocalHome(JNDINames.PROCESS_MANAGER_EJB);
      processManager = pmlh.create();
      entityCatalogURL = serviceLocator.getUrl(JNDINames.XML_ENTITY_CATALOG_URL);
      validateXmlPurchaseOrder = serviceLocator.getBoolean(JNDINames.XML_VALIDATION_PURCHASE_ORDER);
      transitionDelegate = new PurchaseOrderTD();
      transitionDelegate.setup();
    } catch (TransitionException te) {
          throw new EJBException(te);
    } catch (ServiceLocatorException se) {
          throw new EJBException(se);
    } catch (CreateException ce) {
          throw new EJBException(ce);
    }
  }

  /**
   * Process a purchase order that was placed at the Java Pet Store
   * It creates the PuchaseOrder EJB for the order, and then for small
   * orders directly approves the order and sends the approval to the
   * OrderApproval MDB queue, but for larger orders, it jst puts them
   * in the PurchaseOrder EJB, and the workflow process just waits for
   * the Administrator to appove or deny the orders.
   *
   * @param  a JMS message containing an PurchaseOrder
   */
  public void onMessage(Message recvMsg) {
    TextMessage recdTM = null;
    String recdText = null;
    String approval = null;
    try {
      recdTM = (TextMessage)recvMsg;
      recdText = recdTM.getText();
      approval = doWork(recdText);

      //if order approve/deny list !empty, send it to customer relations
      if(approval != null) {
        doTransition(approval);
      } //else wait for admin to approve/deny orders
    } catch(XMLDocumentException de) {
        throw new EJBException(de);
    } catch(CreateException ce) {
        throw new EJBException(ce);
    } catch(TransitionException te) {
        throw new EJBException(te);
    } catch (JMSException je) {
        throw new EJBException(je);
    }
  }

  public void setMessageDrivenContext(MessageDrivenContext mdc) {
    this.mdc = mdc;
  }

  public void ejbRemove() {
  }

  /**
   * Process a purchase order from the petstore. This is the
   * beginning of the order fulfillment process.
   *
   * @param xmlMessage is a Purchase Order from the petstore
   */
  private String doWork(String xmlMessage) throws XMLDocumentException, CreateException {
    String ret = null;
    PurchaseOrder purchaseOrder = PurchaseOrder.fromXML(xmlMessage, entityCatalogURL, validateXmlPurchaseOrder);
    poHome.create(purchaseOrder);
    //update process manager to start workflow for this purchase order
    processManager.createManager(purchaseOrder.getOrderId(), OrderStatusNames.PENDING);

    if(canIApprove(purchaseOrder)) {
      ChangedOrder co = new ChangedOrder(purchaseOrder.getOrderId(), OrderStatusNames.APPROVED);
      OrderApproval oa = new OrderApproval();
      oa.addOrder(co);
      ret = oa.toXML();
    }
    return ret;
  }

  /**
   * Send an order approval to the OrderApproval Queue, since the work
   * for this step in the order fullfillment process is done.
   */
  private void doTransition(String xmlOrderApproval) throws TransitionException {
    if (xmlOrderApproval != null) {
      TransitionInfo info = new TransitionInfo(xmlOrderApproval);
      transitionDelegate.doTransition(info);
    }
  }

  /**
   * Just a stub for converting currency. This method is used for
   * demonstrating the petstore, so that smaller orders can flow through
   * the system without having to fire up the admin Gui to approve orders
   */
  private boolean canIApprove(PurchaseOrder purchaseOrder) {
    Locale locale = purchaseOrder.getLocale();
    if (locale.equals(Locale.US)) {
      if (purchaseOrder.getTotalPrice() < 500) return true;
    } else if (locale.equals(Locale.JAPAN))  {
      if (purchaseOrder.getTotalPrice() < 50000) return true;
    }
    return false;
  }
}

