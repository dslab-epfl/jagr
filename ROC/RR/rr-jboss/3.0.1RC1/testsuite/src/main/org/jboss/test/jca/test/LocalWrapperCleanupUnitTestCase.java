
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.test.jca.test;

import org.jboss.test.JBossTestCase;
import junit.framework.Test;
import org.jboss.test.jca.interfaces.LocalWrapperCleanupTestSessionHome;
import org.jboss.test.jca.interfaces.LocalWrapperCleanupTestSession;

/**
 * LocalWrapperCleanupUnitTestCase.java
 *
 *
 * Created: Thu May 23 17:20:54 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class LocalWrapperCleanupUnitTestCase extends JBossTestCase 
{
   LocalWrapperCleanupTestSessionHome sh;
   LocalWrapperCleanupTestSession s;


   public LocalWrapperCleanupUnitTestCase (String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      sh = (LocalWrapperCleanupTestSessionHome)getInitialContext().lookup("LocalWrapperCleanupTestSession");
      s = sh.create();
   }

   protected void tearDown() throws Exception
   {
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(
         getDeploySetup(LocalWrapperCleanupUnitTestCase.class, 
                        "jcatest.jar"),
         "hsqldb-singleconnection-service.xml");
   }


   public void testAutoCommitInReturnedConnection() throws Exception
   {
      s.testAutoCommitInReturnedConnection();
   }


   public void testAutoCommit() throws Exception
   {
      s.testAutoCommit();
   }


   /*This test requires a real database with actual transaction isolation, not hsqldb.
   public void testTxIsolationInReturnedConnection() throws Exception
   {
      s.testTxIsolationInReturnedConnection();
   }
   */

}// LocalWrapperCleanupUnitTestCase
