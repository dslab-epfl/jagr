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

package com.sun.j2ee.blueprints.opc.customerrelations.ejb;


/**
 * This class is the central location to store the internal
 * JNDI names of various entities. Any change here should
 * also be reflected in the deployment descriptors.
 */
public class JNDINames {

  private JNDINames() { } //Prevents instantiation

  public static final String PURCHASE_ORDER_EJB =
                        "java:comp/env/ejb/PurchaseOrder";

  /**
   * JNDI name for the environment variable for sending email
   * when an order is completed
   */
   public static final String SEND_CONFIRMATION_MAIL =
                     "java:comp/env/param/SendConfirmationMail";

   /**
    * JNDI name for the environment variable for sending email
    * when an order is approved or denied
    */
    public static final String SEND_APPROVAL_MAIL =
                        "java:comp/env/param/SendApprovalMail";

   /**
    * JNDI name for the environment variable for sending email
    * when an order is completed and all items have been shipped
    */
    public static final String SEND_COMPLETED_ORDER_MAIL =
                  "java:comp/env/param/SendCompletedOrderMail";

    public static final String XML_VALIDATION_INVOICE =
                  "java:comp/env/param/xml/validation/Invoice";

    public static final String XML_VALIDATION_ORDER_APPROVAL =
            "java:comp/env/param/xml/validation/OrderApproval";

    public static final String XML_XSD_VALIDATION =
                       "java:comp/env/param/xml/XSDValidation";

    public static final String XML_ENTITY_CATALOG_URL =
                          "java:comp/env/url/EntityCatalogURL";

}

