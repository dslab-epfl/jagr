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

import javax.ejb.EJBLocalObject;

import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfoLocal;
import com.sun.j2ee.blueprints.creditcard.ejb.CreditCardLocal;
import com.sun.j2ee.blueprints.lineitem.ejb.LineItemLocal;

/**
 * This is the Public Local Interface for
 * the PurchaseOrderEJB. It does not have any Remote
 * interface.
 **/

public interface PurchaseOrderLocal extends EJBLocalObject {

  // getters and setters for PO CMP fields

  public String getPoId();
  public String getPoUserId();
  public String getPoEmailId();
  public long getPoDate();
  public String getPoLocale();
  public float getPoValue();

  // getters and setters for accessing address info for this PO

  public ContactInfoLocal getContactInfo();
  public void setContactInfo(ContactInfoLocal addr);

  // getters and setters for accessing credit card info for this PO

  public CreditCardLocal getCreditCard();
  public void setCreditCard(CreditCardLocal ccinfo);

  // getters and setters for accessing line items for this PO

  public Collection getLineItems();
  public void addLineItem(LineItemLocal li);

  // other business methods
  public Collection getAllItems();
  public PurchaseOrder getData();
}

