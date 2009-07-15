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

package com.sun.j2ee.blueprints.creditcard.ejb;

import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import com.sun.j2ee.blueprints.pkgen.PKGen;

public abstract class CreditCardEJB implements javax.ejb.EntityBean {

  private EntityContext context = null;

  // getters and setters for CMP fields
  //====================================

  // primary key field
  public abstract Integer getId();
  public abstract void setId( Integer id );

  public abstract String getCardNumber();
  public abstract void setCardNumber(String cardNumber);

  public abstract String getCardType();
  public abstract void setCardType(String cardType);

  public abstract String getExpiryDate();
  public abstract void setExpiryDate(String expiryDate);


  // EJB create method
  //===================
  public Object ejbCreate(String cardNumber, String cardType,
                          String expiryDate) throws CreateException {
                            setId (PKGen.getNextKeyValue ("CreditCardEJB"));
                            setCardNumber(cardNumber);
                            setCardType(cardType);
                            setExpiryDate(expiryDate);
                            return null;
  }

  public void ejbPostCreate(String cardNumber, String cardType,
                            String expiryDate) throws CreateException { }

  public Object ejbCreate(CreditCard creditCard) throws CreateException {
    setId (PKGen.getNextKeyValue ("CreditCardEJB"));
    setCardNumber(creditCard.getCardNumber());
    setCardType(creditCard.getCardType());
    setExpiryDate(creditCard.getExpiryDate());
    return null;
  }

  public void ejbPostCreate(CreditCard creditCard) throws CreateException { }

  public Object ejbCreate() throws CreateException {
    setId (PKGen.getNextKeyValue ("CreditCardEJB"));
    return null;
  }

  public void ejbPostCreate() throws CreateException { }

  public String getExpiryMonth() {
    String dateString = getExpiryDate();
    if (dateString != null) {
      // get the slash and return the text before it
      int slashStart = dateString.indexOf("/");
      if (slashStart != -1) return dateString.substring(0,slashStart);
    }
    return "01";
  }

  public String getExpiryYear() {
    String dateString = getExpiryDate();
    if (dateString != null) {
      // get the slash and return the text after it
      int slashStart = dateString.indexOf("/");
      if (slashStart != -1) return dateString.substring(slashStart +1, dateString.length());

    }
    return "2010";
  }

  public CreditCard getData() {
    CreditCard creditCard = new CreditCard();
    creditCard.setCardNumber(getCardNumber());
    creditCard.setCardType(getCardType());
    creditCard.setExpiryDate(getExpiryDate());
    return creditCard;
  }

  // Misc Method
  //=============
  public void setEntityContext(EntityContext c) {
    context = c;
  }
  public void unsetEntityContext() {
    context = null;
  }
  public void ejbRemove() throws RemoveException { }
  public void ejbActivate() { }
  public void ejbPassivate() { }
  public void ejbStore() { }
  public void ejbLoad() { }
}
