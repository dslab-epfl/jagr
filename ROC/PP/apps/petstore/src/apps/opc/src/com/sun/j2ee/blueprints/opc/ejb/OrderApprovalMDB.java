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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.net.URL;


import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.*;

import com.sun.j2ee.blueprints.xmldocuments.OrderApproval;
import com.sun.j2ee.blueprints.xmldocuments.ChangedOrder;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.xmldocuments.tpa.TPASupplierOrderXDE;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocalHome;
import com.sun.j2ee.blueprints.processmanager.ejb.ProcessManagerLocal;
import com.sun.j2ee.blueprints.processmanager.ejb.OrderStatusNames;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocal;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocalHome;
import com.sun.j2ee.blueprints.address.ejb.AddressLocal;
import com.sun.j2ee.blueprints.address.ejb.AddressLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocal;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItem;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocalHome;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocal;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.opc.transitions.*;
import com.sun.j2ee.blueprints.processmanager.transitions.*;


/**
 * OrderApprovalMDB gets a JMS message containing a list of
 * orders that has been updated. It updates the POEJB state
 * based on the status of the orders. For all approved orders
 * a supplier purchase order is built and sent to a supplier,
 * and a batch of order approval status notices to the customer
 * relations department to handle sending an email can to customers.
 */
public class OrderApprovalMDB implements MessageDrivenBean, MessageListener {

  private Context context;
  private MessageDrivenContext mdc;
  private TransitionDelegate transitionDelegate;
  private ProcessManagerLocal processManager;
  private TPASupplierOrderXDE supplierOrderXDE;
  private PurchaseOrderLocalHome  poHome;
  private URL entityCatalogURL;
  private boolean validateXmlOrderApproval;


  /** Inner class used as to hold the return
    * values for the doWork
   **/
  private class WorkResult {
    ArrayList supplierPoList;
    String xmlMailOrderApprovals;

    WorkResult(String  xmlMailOrderApprovals, ArrayList supplierPoList) {
      this.xmlMailOrderApprovals = xmlMailOrderApprovals;
      this.supplierPoList = supplierPoList;
    }
  }

  public OrderApprovalMDB() {
  }

  public void ejbCreate() {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      poHome = (PurchaseOrderLocalHome)serviceLocator.getLocalHome(JNDINames.PURCHASE_ORDER_EJB);
      ProcessManagerLocalHome pmlh = (ProcessManagerLocalHome)serviceLocator.getLocalHome(JNDINames.PROCESS_MANAGER_EJB);
      processManager = pmlh.create();
      entityCatalogURL = serviceLocator.getUrl(JNDINames.XML_ENTITY_CATALOG_URL);
      validateXmlOrderApproval = serviceLocator.getBoolean(JNDINames.XML_VALIDATION_ORDER_APPROVAL);
      String tdClassName = serviceLocator.getString(JNDINames.TRANSITION_DELEGATE_ORDER_APPROVAL);
      TransitionDelegateFactory tdf = new TransitionDelegateFactory();
      transitionDelegate = tdf.getTransitionDelegate(tdClassName);
      transitionDelegate.setup();
      supplierOrderXDE = new TPASupplierOrderXDE(entityCatalogURL,
                                                 serviceLocator.getBoolean(JNDINames.XML_XSD_VALIDATION));
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
   * Process a list of order status updates for customer orders
   *
   * @param  a JMS message containing an OrderApproval that
   *          contains a list of orders and the status updates,
   *          such as APPROVED or DENIED.
   */
  public void onMessage(Message recvMsg) {
    TextMessage recdTM = null;
    String recdText = null;
    WorkResult result = null;

    try {
      recdTM = (TextMessage)recvMsg;
      recdText = recdTM.getText();
      result  = doWork(recdText);
      doTransition(result.supplierPoList, result.xmlMailOrderApprovals);
    } catch(TransitionException te) {
      throw new EJBException(te);
    } catch(XMLDocumentException xde) {
      throw new EJBException(xde);
    }catch  (JMSException je) {
      throw new EJBException(je);
    } catch(FinderException ce) {
      throw new EJBException(ce);
    }
  }

  public void setMessageDrivenContext(MessageDrivenContext mdc) {
    this.mdc = mdc;
  }

  public void ejbRemove() {
  }


  /**
   * Process the list of order approvals and update database. Send a
   * PurchaseOrder to a supplier for each approved order. Also generate
   * a list of approved or denied orders.
   *
   * @return a list of valid order approvals/denys to be sent
   *         to customer relations. Or return null, if list empty
   *         or no valid orders AND also the list of Purchase
   *         Orders to send to supplier to fullfill the order
   */
  private WorkResult doWork(String xmlMessage) throws XMLDocumentException, FinderException  {

    ArrayList supplierPoList = new ArrayList();
    PurchaseOrderLocal po = null;
    OrderApproval approval = null;

    approval = OrderApproval.fromXML(xmlMessage, entityCatalogURL, validateXmlOrderApproval);

    //generate list of valid orders to return
    OrderApproval oaMailList = new OrderApproval();
    String xmlMailOrderApprovals = null;
    Collection coll = approval.getOrdersList();
    Iterator it = coll.iterator();

    while(it!= null && it.hasNext()) {
      ChangedOrder co = (ChangedOrder) it.next();
      // only PENDING orders can be updated
      //if order alreay APPROVED or DENIED or COMPLETED, then order
      //already processed so dont process again
      String curStatus = processManager.getStatus(co.getOrderId());
      if(!curStatus.equals(OrderStatusNames.PENDING)) {
        continue;
      }
      //generate list of valid orders to return
      //list contains orders to notify the customer of
      //order status changes. List is sent to customer
      //relations for emailing.
      oaMailList.addOrder(co);

      //update process manager
      //for this purchase order workflow
      processManager.updateStatus(co.getOrderId(), co.getOrderStatus());

      //for all approved orders, send a PO to supplier
      if(co.getOrderStatus().equals(OrderStatusNames.APPROVED)) {
        po = poHome.findByPrimaryKey(co.getOrderId());
        String xmlPO = getXmlPO(po, supplierOrderXDE);
        supplierPoList.add(xmlPO);
      }
    }//end while

    xmlMailOrderApprovals = oaMailList.toXML();
    return new WorkResult(xmlMailOrderApprovals, supplierPoList);
  }

  /**
   * Send Purchase Orders to supplier to fulfill a customer order and
   * also send a list of approvals/denials to customer service to send emails
   *
   * @param supplierPoList is the list of Purchase Orders to send to supplier
   * @param xmlMailOrderApprovals is the list of approvals/denials to send to customers
   */
  private void doTransition(Collection supplierPoList, String xmlMailOrderApprovals) throws TransitionException {
    TransitionInfo info = new TransitionInfo(xmlMailOrderApprovals, supplierPoList);
    transitionDelegate.doTransition(info);
  }


  /**
   * Given a PO, its gets all info and builds the Supplier PO
   * @param the po
   * @returns the Supplier PO in xml format
   */
  private String getXmlPO(PurchaseOrderLocal po, TPASupplierOrderXDE supplierOrderXDE) throws XMLDocumentException{
    supplierOrderXDE.newDocument();
    supplierOrderXDE.setOrderId(po.getPoId());
    Date tmpDate = new Date(po.getPoDate());
    supplierOrderXDE.setOrderDate(tmpDate);
    ContactInfoLocal cinfo = po.getContactInfo();
    AddressLocal addr = cinfo.getAddress();
    supplierOrderXDE.setShippingAddress(cinfo.getGivenName(), cinfo.getFamilyName(),
                                        addr.getStreetName1(), addr.getCity(),
                                        addr.getState(), addr.getCountry(),
                                        addr.getZipCode(), cinfo.getEmail(), cinfo.getTelephone());
    //Collection liColl = po.getAllItems();
    Collection liColl = po.getData().getLineItems();
    Iterator liIt = liColl.iterator();
    while((liIt != null) && (liIt.hasNext())) {
      LineItem li = (LineItem) liIt.next();
      supplierOrderXDE.addLineItem(li.getCategoryId(), li.getProductId(), li.getItemId(),
                                   li.getLineNumber(), li.getQuantity(), li.getUnitPrice());
    }
    return supplierOrderXDE.getDocumentAsString();
  }

}

