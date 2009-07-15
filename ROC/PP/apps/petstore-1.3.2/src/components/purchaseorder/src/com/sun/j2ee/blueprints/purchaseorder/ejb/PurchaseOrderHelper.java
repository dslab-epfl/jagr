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

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocal;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocalHome;

import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocal;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocalHome;
import com.sun.j2ee.blueprints.address.ejb.AddressLocal;
import com.sun.j2ee.blueprints.address.ejb.AddressLocalHome;

import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocal;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocal;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocalHome;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItem;

/**
 * Helper class to check for join conditions purchase order
 */

public class PurchaseOrderHelper {

   public PurchaseOrderHelper() {}

  /**
   * This method processes invoice information received from supplier by opc. Its
   * job is to update the LineItem fields for the received invoices.
   * Additionally it checks if all invoices of the given PO are shipped,
   * it will return true to indicate all invoices for a purchase order have
   * been joined together and the order is completely fulfilled.
   *
   * @param <Code>po</Code>
   * @param <Code>lineItemIds</Code>
   *
   * @return true or false to indicate if order is completely done
   */
  public boolean processInvoice(PurchaseOrderLocal po, Map lineItemIds) {

    //set the quantity of line items shipped in the PO
    Collection liColl = po.getLineItems();
    Iterator liIt = liColl.iterator();
    while((liIt != null) && (liIt.hasNext())) {
      LineItemLocal li = (LineItemLocal) liIt.next();
      if(lineItemIds.containsKey(li.getItemId())) {
                  Integer shipped = (Integer) lineItemIds.get(li.getItemId());
          li.setQuantityShipped(li.getQuantityShipped() + shipped.intValue());
          }
    }//end while

    // now loop through all LIs and see if all are shipped completely
    liColl = po.getLineItems();
    liIt = liColl.iterator();
    while((liIt != null) && (liIt.hasNext())) {
      LineItemLocal li = (LineItemLocal) liIt.next();
      if(li.getQuantity() != li.getQuantityShipped()) {
          return false;
          }
    }//end while
    return true;
  }

}
