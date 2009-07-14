package org.jboss.test.cmp2.commerce;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.naming.InitialContext;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sourceforge.junitejb.EJBTestCase;
import org.jboss.test.JBossTestCase;

public class QueryTest extends EJBTestCase {
   public static Test suite() throws Exception {
		return JBossTestCase.getDeploySetup(
            QueryTest.class, 
            "cmp2-commerce.jar");
   }   

   public QueryTest(String name) {
      super(name);
   }

   private OrderHome getOrderHome() {
      try {
         InitialContext jndiContext = new InitialContext();

         return (OrderHome) jndiContext.lookup("commerce/Order"); 
      } catch(Exception e) {
         e.printStackTrace();
         fail("Exception in getOrderHome: " + e.getMessage());
      }
      return null;
   }

   public void test_queries() throws Exception {

      OrderHome oh = getOrderHome();

      oh.getStuff(
            "SELECT OBJECT(o) " +
            "FROM OrderX o, " +
            "   IN(o.lineItems) l, " +
            "   IN(l.product.productCategories) pc " +
            "WHERE o.ordernumber = ?1 and pc.name=?2",
            new Object[] { new Long(1), "stuff" });

      oh.getStuff(
            "SELECT OBJECT(u) " +
            "FROM user u " +
            "WHERE UCASE(u.userName) = ?1",
            new Object[] { "DAIN" });

      oh.getStuff(
            "SELECT OBJECT(u) " +
            "FROM user u " +
            "WHERE LCASE(u.userName) = ?1",
            new Object[] { "dain" });

      oh.getStuff(
            "SELECT OBJECT(o1) " +
            "FROM OrderX o1, OrderX o2 " +
            "WHERE o1.customer <> o2.customer AND o1.creditCard = o2.creditCard",
            new Object[] { });

      oh.getStuff(
            "SELECT OBJECT(o) " +
            "FROM OrderX o " +
            "WHERE o.creditCard = ?1",
            new Object[] { new Card() });

      oh.getStuff(
            "SELECT OBJECT(o) " +
            "FROM OrderX o " +
            "WHERE o.creditCard <> ?1",
            new Object[] { new Card() });
   }
}
