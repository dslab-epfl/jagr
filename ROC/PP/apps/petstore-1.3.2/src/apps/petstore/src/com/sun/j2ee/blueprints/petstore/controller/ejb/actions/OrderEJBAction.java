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

package com.sun.j2ee.blueprints.petstore.controller.ejb.actions;


import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.util.Locale;

// J2EE imports
import javax.ejb.CreateException;
import javax.naming.NamingException;

// WAF imports
import com.sun.j2ee.blueprints.waf.event.Event;
import com.sun.j2ee.blueprints.waf.event.EventResponse;
import com.sun.j2ee.blueprints.waf.event.EventException;
import com.sun.j2ee.blueprints.waf.controller.ejb.action.EJBActionSupport;

// po component imports
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrder;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItem;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.petstore.controller.events.OrderEvent;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfo;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCard;

// async component imports
import com.sun.j2ee.blueprints.asyncsender.ejb.AsyncSenderLocalHome;
import com.sun.j2ee.blueprints.asyncsender.ejb.AsyncSender;

// unidue id generator imports
import com.sun.j2ee.blueprints.uidgen.ejb.UniqueIdGeneratorLocal;
import com.sun.j2ee.blueprints.uidgen.ejb.UniqueIdGeneratorLocalHome;

// shoppingcart component imports
import com.sun.j2ee.blueprints.cart.ejb.ShoppingCartLocal;
import com.sun.j2ee.blueprints.cart.model.CartItem;

// service locator imports
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

// petstore imports
import com.sun.j2ee.blueprints.petstore.util.JNDINames;
import com.sun.j2ee.blueprints.petstore.util.PetstoreKeys;
import com.sun.j2ee.blueprints.petstore.controller.ejb.ShoppingClientFacadeLocal;
import com.sun.j2ee.blueprints.petstore.controller.events.OrderEvent;
import com.sun.j2ee.blueprints.petstore.controller.events.OrderEventResponse;
import com.sun.j2ee.blueprints.petstore.controller.exceptions.ShoppingCartEmptyOrderException;

public class OrderEJBAction extends EJBActionSupport {

  public EventResponse perform(Event e) throws EventException {
    OrderEvent oe = (OrderEvent)e;
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    ContactInfo billTo = oe.getBillTo();
    ContactInfo shipTo = oe.getShipTo();
    CreditCard creditCard = oe.getCreditCard();
    String orderIdString = null;

    // get the UniqueIdGenerator EJB
    UniqueIdGeneratorLocal uidgen = null;
    try {
      ServiceLocator sl = new ServiceLocator();
      UniqueIdGeneratorLocalHome home =
        (UniqueIdGeneratorLocalHome)sl.getLocalHome(JNDINames.UIDG_EJBHOME);
      uidgen = home.create();
    } catch (javax.ejb.CreateException cx) {
      cx.printStackTrace();
    } catch (ServiceLocatorException slx) {
      slx.printStackTrace();
    }
    orderIdString = uidgen.getUniqueId("1001");
    // get ther userId
    ShoppingClientFacadeLocal scf = null;
    scf = (ShoppingClientFacadeLocal)machine.getAttribute(PetstoreKeys.SHOPPING_CLIENT_FACADE);
    String userId = scf.getUserId();
    purchaseOrder.setOrderId(orderIdString);
    purchaseOrder.setUserId(userId);
    purchaseOrder.setEmailId(billTo.getEmail());
    purchaseOrder.setOrderDate(new Date());
    purchaseOrder.setShippingInfo(shipTo);
    purchaseOrder.setBillingInfo(billTo);
    purchaseOrder.setCreditCard(creditCard);
    int lineItemCount = 0;
    float totalCost = 0;
    // Add the items from the shopping cart
    ShoppingCartLocal cart = scf.getShoppingCart();
    Locale locale = (Locale)machine.getAttribute(PetstoreKeys.LOCALE);
    purchaseOrder.setLocale(locale);
    Collection items = cart.getItems();
    // if the cart is empty throw an exception saying so
    if (items.size() == 0) {
      throw new ShoppingCartEmptyOrderException("Shopping cart is empty");
    }
    Iterator it = items.iterator();
    while (it.hasNext()) {
      CartItem item = (CartItem)it.next();

      float cost = new Float(item.getUnitCost()).floatValue();
      totalCost += (cost*item.getQuantity());
      purchaseOrder.addLineItem(new LineItem(item.getCategory(),
                                             item.getProductId(),
                                             item.getItemId(),
                                             (lineItemCount++) + "",
                                             item.getQuantity(),
                                             cost));
    }
    purchaseOrder.setTotalPrice(totalCost);
    try {
      ServiceLocator sl = new ServiceLocator();
      AsyncSenderLocalHome home = (AsyncSenderLocalHome)
        sl.getLocalHome(JNDINames.ASYNCSENDER_LOCAL_EJB_HOME);

      AsyncSender  sender= home.create();
      sender.sendAMessage(purchaseOrder.toXML());

    } catch (ServiceLocatorException sle) {
      sle.printStackTrace();
      // throw new AdminBDException(sle.getMessage());
    } catch (XMLDocumentException xde) {
      xde.printStackTrace();
      System.err.println(xde.getRootCause().getMessage());
      // throw new EventResponse    or whatever
    }  catch (CreateException ce) {
      //throw new AdminBDException(ce.getMessage());
    }

    // empty the shopping cart
    cart.empty();
    return new OrderEventResponse(billTo.getEmail(), orderIdString);
  }
}

