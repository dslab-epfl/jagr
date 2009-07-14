/** $Id: ProductAttributeTag.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.taglib.list;

import com.sun.j2ee.blueprints.shoppingcart.catalog.model.Product;

/*
 * ProductAttributeTag
 * -------------------
 * Extension of ItemAttributeTag that handles a Product object.  It prints out
 * the name, id, or description of the product, depending on the attribute
 * specified, with "name" as the default.
 */
public class ProductAttributeTag extends ItemAttributeTag {

  protected String createText() {
    Product product = (Product) item;

    if ((attribute == null) ||
        (attribute.equalsIgnoreCase("name"))) {
      return (product.getName());
    } else if (attribute.equalsIgnoreCase("id")) {
      return (product.getId());
    } else if (attribute.equalsIgnoreCase("description")) {
      return (product.getDescription());
    } else return(null);
  }
}
