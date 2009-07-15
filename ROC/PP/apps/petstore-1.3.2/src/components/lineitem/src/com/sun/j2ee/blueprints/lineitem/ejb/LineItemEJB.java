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
package com.sun.j2ee.blueprints.lineitem.ejb;

import java.lang.Object;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import com.sun.j2ee.blueprints.pkgen.PKGen;

/**
 * This is the main Entity Bean class for LineItemEJB
 */

public abstract class LineItemEJB implements EntityBean {

  private EntityContext context = null;

  // primary key field
  public abstract Integer getId();
  public abstract void setId(Integer id); 

  /**
   * Accessor for line item's category id
   * @return String   the category id
   */
  public abstract String getCategoryId();

  /**
   * Setter for line item's category id
   * @param String      the category id
   */
  public abstract void setCategoryId(String id);

  /**
   * Accessor for line item's product id
   * @return String   the product id
   */
  public abstract String getProductId();

  /**
   * Setter for line item's product id
   * @param String      the product id
   */
  public abstract void setProductId(String id);

  /**
   * Accessor for line item's item id
   * @return String   the item id
   */
  public abstract String getItemId();

  /**
   * Setter for line item's item id
   * @param String      the item id
   */
  public abstract void setItemId(String id);

  /**
   * Accessor for line item's line number
   * @return String   the linenumber
   */
  public abstract String getLineNumber();

  /**
   * Setter for line item's line number
   * @param String      the line number
   */
  public abstract void setLineNumber(String num);

  /**
   * Accessor for line item's quantity
   * @return int   the quantity
   */
  public abstract int getQuantity();

  /**
   * Setter for line item's quantity
   * @param int      the quantity
   */
  public abstract void setQuantity(int qty);

  /**
   * Accessor for line item's unit price
   * @return float   the unit price
   */
  public abstract float getUnitPrice();

  /**
   * Setter for line item's unit price
   * @param float      the unit price
   */
  public abstract void setUnitPrice(float price);

  /**
   * Accessor for line item's qty that is shipped
   * @return int the qty already shipped
   */
  public abstract int getQuantityShipped();

  /**
   * Setter for line item's quantity that is already shipped
   * @param int the qty already shipped
   */
  public abstract void setQuantityShipped(int qty);

  /**
   * The ejb create method - returns object because there is primary key
   */
  public Object ejbCreate(String catId, String prodId, String itemId,
                          String lineNo, int qty, float price, int qtyShipped)
    throws CreateException {
      setId (PKGen.getNextKeyValue ("LineItemEJB"));
      setCategoryId(catId);
      setProductId(prodId);
      setItemId(itemId);
      setLineNumber(lineNo);
      setQuantity(qty);
      setUnitPrice(price);
      setQuantityShipped(qtyShipped);
      return null;
  }

  public void ejbPostCreate(String catId, String prodId, String itemId,
                            String lineNo, int qty, float price, int qtyShipped)
        throws CreateException{}

  public Object ejbCreate(LineItem lineItem, int qty) throws CreateException {
    setId (PKGen.getNextKeyValue ("LineItemEJB")); 
    setCategoryId(lineItem.getCategoryId());
    setProductId(lineItem.getProductId());
    setItemId(lineItem.getItemId());
    setLineNumber(lineItem.getLineNumber());
    setQuantity(lineItem.getQuantity());
    setUnitPrice(lineItem.getUnitPrice());
    setQuantityShipped(qty);
    return null;
  }

  public void ejbPostCreate(LineItem lineItem, int qty) throws CreateException{}

  public LineItem getData() {
    LineItem lineItem = new LineItem();
    lineItem.setCategoryId(getCategoryId());
    lineItem.setProductId(getProductId());
    lineItem.setItemId(getItemId());
    lineItem.setLineNumber(getLineNumber());
    lineItem.setQuantity(getQuantity());
    lineItem.setUnitPrice(getUnitPrice());
    return lineItem;
  }

  /**
   * Other life cycle methods
   */
  public void setEntityContext(EntityContext c){ context = c; }
  public void unsetEntityContext(){}
  public void ejbRemove() throws RemoveException {}
  public void ejbActivate() {}
  public void ejbPassivate() {}
  public void ejbStore() {}
  public void ejbLoad() {}
}

