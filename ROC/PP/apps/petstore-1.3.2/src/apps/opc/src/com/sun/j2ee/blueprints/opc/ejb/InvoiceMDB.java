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

import java.net.URL;

import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderHelper;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocalHome;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocal;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocalHome;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocal;
import com.sun.j2ee.blueprints.processmanager.ejb.OrderStatusNames;
import com.sun.j2ee.blueprints.opc.transitions.*;
import com.sun.j2ee.blueprints.processmanager.transitions.*;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;


/**
 * InvoiceMDB receives a JMS message containing an Invoice
 * for a user order. It updates the Purchase Order EJB based
 * on the invoice information.
 */
public class InvoiceMDB implements MessageDrivenBean, MessageListener {

  private Context context;
  private MessageDrivenContext mdc;
  private ProcessManagerLocal processManager;
  private PurchaseOrderLocalHome  poHome;
  private TPAInvoiceXDE invoiceXDE;
  private TransitionDelegate transitionDelegate;

  public InvoiceMDB() {
  }

  public void ejbCreate() {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      poHome = (PurchaseOrderLocalHome)serviceLocator.getLocalHome(JNDINames.PURCHASE_ORDER_EJB);
      ProcessManagerLocalHome pmlh = (ProcessManagerLocalHome)serviceLocator.getLocalHome(JNDINames.PROCESS_MANAGER_EJB);
      processManager = pmlh.create();
      URL entityCatalogURL = serviceLocator.getUrl(JNDINames.XML_ENTITY_CATALOG_URL);
      boolean validateXmlInvoice = serviceLocator.getBoolean(JNDINames.XML_VALIDATION_INVOICE);
      invoiceXDE = new TPAInvoiceXDE(entityCatalogURL, validateXmlInvoice,
                            serviceLocator.getBoolean(JNDINames.XML_XSD_VALIDATION));
      transitionDelegate = new InvoiceTD();
      transitionDelegate.setup();
    } catch (TransitionException te) {
        throw new EJBException(te);
    } catch (ServiceLocatorException se) {
        throw new EJBException(se);
    } catch (CreateException ce) {
        throw new EJBException(ce);
    } catch (XMLDocumentException xde) {
        throw new EJBException(xde);
    }
  }

  /**
   * InvoiceMDB receives a JMS message containing an Invoice
   * for a user order. If all the order items have been shipped
   * for the order, it updates the purchase order status to
   * COMPLETED to end the purchase order fulfillment process.
   * If the order is not yet all shipped, it updates the order
   * status based on the invoice.
   *
   * @param recvMsg is the JMS message contaning the xml content for the invoice
   */
  public void onMessage(Message recvMsg) {
    TextMessage recdTM = null;
    String recdText = null;
    try {
      recdTM = (TextMessage)recvMsg;
      recdText = recdTM.getText();
      String joinMessage = doWork(recdText);

      //if order completed so join condition is met
      if (joinMessage != null) {
        doTransition(joinMessage);
      }
    } catch(TransitionException te) {
      throw new EJBException(te);
    } catch(XMLDocumentException xde) {
      throw new EJBException(xde);
    } catch  (JMSException je) {
      throw new EJBException(je);
    } catch  (FinderException fe) {
      throw new EJBException(fe);
    }
  }

  public void setMessageDrivenContext(MessageDrivenContext mdc) {
    this.mdc = mdc;
  }

  public void ejbRemove() {
  }

  /**
   * update POEJB to reflect items shipped, and also update Process Manager
   * to completed or partially completed status based on  the items shipped
   * in the order's invoice. If the join condition is met and all items are
   * shipped, then send an order completed message to user
   *
   * @return orderMessage if order completed
   *         else null if NOT completed
   */
  private String doWork(String xmlInvoice) throws XMLDocumentException, FinderException {
    String completedOrder = null;
    PurchaseOrderHelper poHelper = new PurchaseOrderHelper();
    invoiceXDE.setDocument(xmlInvoice);
    PurchaseOrderLocal po = poHome.findByPrimaryKey(invoiceXDE.getOrderId());
    boolean orderDone = poHelper.processInvoice(po, invoiceXDE.getLineItemIds());

    //update process manager if this order is completely done, or partially done
    //for this purchase order
    if(orderDone) {
      processManager.updateStatus(invoiceXDE.getOrderId(), OrderStatusNames.COMPLETED);
      completedOrder = invoiceXDE.getOrderId();
    } else {
      processManager.updateStatus(invoiceXDE.getOrderId(), OrderStatusNames.SHIPPED_PART);
    }
    return completedOrder;
  }

   /**
   * Send a completed order message to the customer relations so that
   * a mail can be generated and sent to the user informing them that
   * their order has been completed
   */
  private void doTransition(String completedOrder) throws TransitionException {
      TransitionInfo info = new TransitionInfo(completedOrder);
      transitionDelegate.doTransition(info);
  }

}

