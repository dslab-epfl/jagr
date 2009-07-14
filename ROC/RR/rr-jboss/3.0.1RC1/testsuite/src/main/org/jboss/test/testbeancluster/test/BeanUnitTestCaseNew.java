/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.test.testbeancluster.test;


import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.testbeancluster.interfaces.StatelessSession;
import org.jboss.test.testbean.interfaces.StatelessSessionHome;

import org.jboss.test.JBossClusteredTestCase;

/**
 * Test SLSB for load-balancing behaviour
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>12 avril 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class BeanUnitTestCaseNew extends JBossClusteredTestCase
{
   static boolean deployed = false;
   public static int test = 0;
   static Date startDate = new Date();
   
   protected final String namingFactory =
   System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
   
   protected final String providerURL =
   System.getProperty(Context.PROVIDER_URL);
   
   public BeanUnitTestCaseNew (String name) {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(BeanUnitTestCaseNew.class, "testbeancluster.jar");
      return t1;
   }

   public void testStatelessBeanLoadBalancing() 
   throws Exception
   {       
      getLog().debug(++test+"- "+"Trying the context...");
      
      Context ctx = new InitialContext();
      getLog().debug("OK");
      
      ///*
      getLog().debug("");
      getLog().debug("Test Stateless Bean load-balancing");
      getLog().debug("==================================");
      getLog().debug("");
      getLog().debug(++test+"- "+"Looking up the home nextgen.StatelessSession...");
      StatelessSessionHome  statelessSessionHome =
      (StatelessSessionHome) ctx.lookup("nextgen.StatelessSession");
      if (statelessSessionHome!= null ) getLog().debug("ok");
         getLog().debug(++test+"- "+"Calling create on StatelessSessionHome...");
      StatelessSession statelessSession =
      (StatelessSession)statelessSessionHome.create();
      assertTrue("statelessSessionHome.create() != null", statelessSession != null);
      getLog().debug("ok");
      
      getLog().debug(++test+"- "+"Calling getEJBHome() on StatelessSession...");
      assertTrue("statelessSession.getEJBHome() != null", statelessSession.getEJBHome() != null);
      getLog().debug("ok");
      
      getLog().debug(++test+"- "+"Reseting the number of calls made on beans (making 2 calls)... ");
      for (int i=0; i<6; i++)
      {
         getLog().debug(++test+"- "+" Reseting number... ");
         statelessSession.resetNumberOfCalls ();         
      }
      
      getLog().debug(++test+"- "+"Now making 20 calls on this remote... ");
      for (int i=0; i<20; i++)
      {
         getLog().debug(++test+"- "+" Calling remote... ");
         statelessSession.makeCountedCall ();         
      }
      
      getLog().debug(++test+"- "+"Getting the number of calls that have been performed on each bean... ");
      long node1 = statelessSession.getCallCount();
      getLog().debug(++test+"- "+"One node has received: " + node1);
      
      long node2 = statelessSession.getCallCount();
      getLog().debug(++test+"- "+"The other node has received: " + node2);
      
      if (node1 == node2 &&
          node1 == 10)
      {
         getLog().debug(++test+"- "+"Test is ok.");
      }
      else
      {
         getLog().debug(++test+"- "+"Something wrong has happened! Calls seems not to have been load-balanced.");
         fail ("Calls have not been correctly load-balanced on the SLSB remote interface.");
      }
      
      statelessSession.remove();
      getLog().debug("ok");
   }            
   
}
