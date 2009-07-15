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

package com.sun.j2ee.blueprints.supplier.processpo.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.ejb.FinderException;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.xmldocuments.tpa.TPAInvoiceXDE;

import com.sun.j2ee.blueprints.supplier.orderfulfillment.ejb.OrderFulfillmentFacadeLocalHome;
import com.sun.j2ee.blueprints.supplier.orderfulfillment.ejb.OrderFulfillmentFacadeLocal;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.processmanager.transitions.*;

/**
 * This message driven bean that exposes Supplier functionality.
 * It receives purhase orders from the OPC component, processes the orders,
 * and, if orders are shipped, sends back the invoice.
 */
public class SupplierOrderMDB implements MessageDrivenBean, MessageListener {

  private Context context;
  private MessageDrivenContext mdc = null;
  private OrderFulfillmentFacadeLocal poProcessor = null;

  private TransitionDelegate transitionDelegate;

  public SupplierOrderMDB() {}

  public void ejbCreate() {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      OrderFulfillmentFacadeLocalHome ref = (OrderFulfillmentFacadeLocalHome)
                                        serviceLocator.getLocalHome(JNDINames.ORDERFACADE_EJB);
      poProcessor = ref.create();
      String tdClassName = serviceLocator.getString(JNDINames.TRANSITION_DELEGATE__SUPPLIER_ORDER);
      TransitionDelegateFactory tdf = new TransitionDelegateFactory();
      transitionDelegate = tdf.getTransitionDelegate(tdClassName);
      transitionDelegate.setup();
    } catch (CreateException ce) {
        throw new EJBException(ce);
    } catch (TransitionException te) {
        throw new EJBException(te);
    } catch (ServiceLocatorException se) {
        throw new EJBException(se);
    }
  }

  /**
   * receives the supplier purchase order from the order processing
   * center, opc, and tries to fulfill the order
   *
   * @param recvMsg is the JMS message containing the xml for
   *       the supplier Purchase Order.
   */
  public void onMessage(Message recvMsg) {
    try {
      String messageID = recvMsg.getJMSMessageID();
      TextMessage recdTM = (TextMessage)recvMsg;
      String recdText = recdTM.getText();

      // Do processing work on received message
      String invoice = doWork(recdText);

      // If orders were shipped, do the transition
      if(invoice!=null) {
        doTransition(invoice);
      } //else wait for the inventory to arrive at the inventory receiver
    } catch (TransitionException te) {
        throw new EJBException(te);
    } catch  (CreateException ce) {
      throw new EJBException(ce);
    } catch  (XMLDocumentException xe) {
      throw new EJBException(xe);
    } catch  (JMSException je) {
      throw new EJBException(je);
    }
  }

  public void setMessageDrivenContext(MessageDrivenContext mdc) {
    this.mdc = mdc;
  }

  public void ejbRemove() {}

  /**
   * This method processes the order received by the supplier
   * @param xmlMessage the purchase order XML received
   * @returns invoice the invoice, in XML format, for the purchase order
   * @throws <Code>NamingException</Code>
   * @throws <Code>FinderException</Code>
   */
  private String doWork(String xmlMessage) throws
                                    CreateException, XMLDocumentException  {
    String invoice = null;
    invoice = poProcessor.processPO(xmlMessage);
    return invoice;
  }

  /**
   * This method does the transition if the order was shipped.
   * Sends an invoice to the opc
   * @param xmlMessage the invoice, in XML, for the order received
   * @throws <Code>TransitionException</Code> for JMS failures
   */
  private void doTransition(String xmlMessage) throws TransitionException  {
    TransitionInfo info = new TransitionInfo(xmlMessage);
    transitionDelegate.doTransition(info);
  }
}

