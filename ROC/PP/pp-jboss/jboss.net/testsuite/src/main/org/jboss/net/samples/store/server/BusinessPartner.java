/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: BusinessPartner.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.samples.store.server;

import org.jboss.net.samples.store.Address;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.util.Collection;

/**
 * Compact entity bean that is exposed as a value object
 * via Axis.
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * </ul>
 * @created 21.03.2002
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public interface BusinessPartner
   extends EJBObject, org.jboss.net.samples.store.BusinessPartner {

   // have to repeat that here for wsdl purposes
   public String getName();
   public void setName(String name);
   /**
    * @link aggregation
    * @associates org.jboss.net.samples.store.Address
    * @supplierCardinality 0..*
    * @clientCardinality 0..1
    */
   public Address getAddress();
   public void setAddress(Address address);

   /** home interface of the businesspartner entity bean */
   public interface Home extends EJBHome {
      public BusinessPartner create(String name) throws CreateException;
      public BusinessPartner findByPrimaryKey(String name) throws FinderException;
      public Collection findAll() throws FinderException;
   }

   /** server-side implementation */
   public static abstract class Bean
      implements EntityBean, org.jboss.net.samples.store.BusinessPartner {

      public String ejbCreate(String name) {
         setName(name);
         return null;
      }

      //
      // What follows is just CMP2.x mumbo-jumbo
      //

      transient private EntityContext ctx;

      public void ejbPostCreate(String name) {
      }

      public void setEntityContext(EntityContext ctx) {
         this.ctx = ctx;
      }

      public void unsetEntityContext() {
         this.ctx = null;
      }

      public void ejbActivate() {
      }

      public void ejbPassivate() {
      }

      public void ejbLoad() {
      }

      public void ejbStore() {
      }

      public void ejbRemove() {
      }
   }
}