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
package com.sun.j2ee.blueprints.purchaseorder.ejb;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocal;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocalHome;
import com.sun.j2ee.blueprints.address.ejb.AddressLocal;
import com.sun.j2ee.blueprints.address.ejb.AddressLocalHome;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocal;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocal;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItem;
import com.sun.j2ee.blueprints.purchaseorder.ejb.JNDINames;

import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

/**
 * This is the main Entity Bean class for PurchaseOrderEJB
 * It has a one-many relatioship with the LineItemEJB, and one-to-one
 * relationship between ContactinfoEJB and CreditCardEJB.
 */

public abstract class PurchaseOrderEJB implements EntityBean {

  private EntityContext context = null;

  /**
   * Accessor method for Purchasr Order ID
   * @return String   PO id
   */
  public abstract String getPoId();

  /**
   * Setter method for Purchasr Order ID
   * @param String   PO id
   */
  public abstract void setPoId(String id);

  /**
   * Accessor method for Purchasr Order user ID
   * @return String   PO user id
   */
  public abstract String getPoUserId();

  /**
   * setter method for Purchasr Order user ID
   * @param String   PO user id
   */
  public abstract void setPoUserId(String id);

  /**
   * Accessor method for Purchasr Order email ID
   * @return String   PO email id
   */
  public abstract String getPoEmailId();

  /**
   * Setterr method for Purchasr Order email ID
   * @param String   PO email id
   */
  public abstract void setPoEmailId(String id);

  /**
   * Accessor method for Purchasr Order date
   * @return long   PO date - time form epoch
   */
  public abstract long getPoDate();

  /**
   * Setter method for Purchasr Order date
   * @param long   PO date - time form epoch
   */
  public abstract void setPoDate(long orderDate);

  /**
   * Accessor method for Purchasr Order locale
   * @return String   PO locale
   */
  public abstract String getPoLocale();

  /**
   * Setterr method for Purchasr Order locale
   * @param String   PO locale
   */
  public abstract void setPoLocale(String loc);

  /**
   * Accessor method for Purchasr Order total value
   * @return float   PO total value
   */
  public abstract float getPoValue();

  /**
   * setter method for Purchasr Order total value
   * @param float   PO total value
   */
  public abstract void setPoValue(float amount);

  /**
   * Accessor method for Purchasr Order contact address
   * @return <Code>ContactInfoLocal</Code>   PO contact address
   */
  public abstract ContactInfoLocal getContactInfo();

  /**
   * setter method for Purchasr Order contact address
   * @param <Code>ContactInfoLocal</Code>   PO contact address
   */
  public abstract void setContactInfo(ContactInfoLocal addr);

  /**
   * Accessor method for Purchasr Order - credit card info
   * @return <Code>CreditCardLocal</Code>   PO credit card info
   */
  public abstract CreditCardLocal getCreditCard();

  /**
   * Setter method for Purchasr Order - credit card info
   * @param <Code>CreditCardLocal</Code>   PO credit card info
   */
  public abstract void setCreditCard(CreditCardLocal ccInfo);

  /**
   * Accessor method for Purchasr Order - line items
   * @return <code>Collection</Code>   PO line items
   */
  public abstract Collection getLineItems();

  /**
   * Setter method for Purchasr Order - line items
   * @param <code>Collection</Code>   PO line items
   */
  public abstract void setLineItems(Collection litems);


  /**
   * Method helps to add a line item into the CMR field
   * @param <Code>LineItemLocal</Code> the local interface of line item
   */
  public void addLineItem(LineItemLocal lItem){
        getLineItems().add(lItem);
  }


  /**
   * the ejb create method
   * @param <Code>purchaseOrder</Code> the Purchase Order details
   */
  public String ejbCreate(PurchaseOrder purchaseOrder) throws CreateException {
        setPoId(purchaseOrder.getOrderId());
        setPoUserId(purchaseOrder.getUserId());
        setPoEmailId(purchaseOrder.getEmailId());
        setPoDate(purchaseOrder.getOrderDate().getTime());
        setPoLocale(purchaseOrder.getLocale().toString());
        setPoValue(purchaseOrder.getTotalPrice());
        return null;
  }

  /**
   * the ejb create method
   * @param <Code>purchaseOrder</Code> the Purchase Order details
   */
  public void ejbPostCreate(PurchaseOrder purchaseOrder)
                                         throws CreateException {
      try {
          ServiceLocator serviceLocator = new ServiceLocator();
          ContactInfoLocalHome cinforef = (ContactInfoLocalHome)
              serviceLocator.getLocalHome(JNDINames.CINFO_EJB);
          ContactInfoLocal cinfoloc = (ContactInfoLocal)
              cinforef.create(purchaseOrder.getShippingInfo());
          setContactInfo(cinfoloc);
          CreditCardLocalHome cardref = (CreditCardLocalHome)
              serviceLocator.getLocalHome(JNDINames.CARD_EJB);
          CreditCardLocal cardloc = (CreditCardLocal)
              cardref.create(purchaseOrder.getCreditCard());
          setCreditCard(cardloc);
          LineItemLocalHome lineItemref = (LineItemLocalHome)
              serviceLocator.getLocalHome(JNDINames.LI_EJB);
          Collection litems = purchaseOrder.getLineItems();
          Iterator it = litems.iterator();
          while((it != null) && (it.hasNext())) {
              LineItem li = (LineItem) it.next();
              LineItemLocal lineItemloc = (LineItemLocal)
                  lineItemref.create(li, 0);
              addLineItem(lineItemloc);
          }
      } catch(ServiceLocatorException ne) {
          throw new CreateException("ServiceLocator Ex while persisting PO CMR :" + ne.getMessage());
      }
  }

  /**
   * This gets all line items for this po and returns a collection of
   * Value objects. This is required because managed objects cant be accessed
   * outside transaction bounsaries
   * @return <Code>Collection</Code> of <Code>LineItem</Code> value objects
   */
  public Collection getAllItems() {
      Collection liColl = getLineItems();
      if(liColl == null)
          return(null);
      ArrayList retVal = new ArrayList();
      Iterator it = liColl.iterator();
      while((it!=null) && (it.hasNext())) {
          LineItemLocal loc = (LineItemLocal) it.next();
          retVal.add(loc.getData());
      }
      return(retVal);
  }

  public PurchaseOrder getData() {
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setOrderId(getPoId());
    purchaseOrder.setUserId(getPoUserId());
    purchaseOrder.setEmailId(getPoEmailId());
    purchaseOrder.setOrderDate(new Date(getPoDate()));
    purchaseOrder.setLocale(getPoLocale());
    purchaseOrder.setTotalPrice(getPoValue());
    purchaseOrder.setBillingInfo(getContactInfo().getData());
    purchaseOrder.setShippingInfo(purchaseOrder.getBillingInfo()); // XXX
    purchaseOrder.setCreditCard(getCreditCard().getData());
    Collection lineItems = getLineItems();
    for (Iterator iterator = lineItems.iterator(); iterator.hasNext();) {
      LineItemLocal lineItem = (LineItemLocal) iterator.next();
      purchaseOrder.addLineItem(lineItem.getData());
    }
    return purchaseOrder;
  }

  /**
   * other ejb lifecycle method
   */
  public void setEntityContext(EntityContext c){ context = c; }
  public void unsetEntityContext(){}
  public void ejbRemove() throws RemoveException {}
  public void ejbActivate() {}
  public void ejbPassivate() {}
  public void ejbStore() {}
  public void ejbLoad() {}
}

