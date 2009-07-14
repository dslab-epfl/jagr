package org.jboss.test.cmp2.commerce;

import java.util.Collection;
import java.util.Iterator;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import org.apache.log4j.Category;

public class TxTesterBean implements SessionBean
{
   private SessionContext ctx;
   private OrderHome orderHome;
   private LineItemHome lineItemHome;

   public void ejbCreate() throws CreateException {
      try {
         InitialContext jndiContext = new InitialContext();

         orderHome = (OrderHome) jndiContext.lookup("commerce/Order"); 
         lineItemHome = (LineItemHome) jndiContext.lookup("commerce/LineItem"); 
      } catch(Exception e) {
         throw new CreateException("Error getting OrderHome and " +
               "LineItemHome: " + e.getMessage());
      }
   }

   public void accessCMRCollectionWithoutTx() throws Exception {
      Order o = orderHome.create();
      LineItem l1 = lineItemHome.create();
      LineItem l2 = lineItemHome.create();
      
      // this should work
      l1.setOrder(o);


      // this should throw an IllegalStateException
      Collection c = o.getLineItems();
      c.add(l2);
   }

   public void setSessionContext(SessionContext ctx)
   {
      ctx = ctx;
   }

   public void ejbActivate() { }

   public void ejbPassivate() { }

   public void ejbRemove() { }
}
