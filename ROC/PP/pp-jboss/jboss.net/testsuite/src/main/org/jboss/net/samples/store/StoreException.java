/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: StoreException.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.samples.store;

/**
 * Exception indicating problems with managing items.
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * </ul>
 * @created 22.03.2002
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class StoreException extends Exception {

   /**
    * Constructor for ItemException.
    */
   public StoreException() {
      super();
   }

   /**
    * Constructor for ItemException.
    * @param arg0
    */
   public StoreException(String arg0) {
      super(arg0);
   }

}
