
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
import javax.transaction.UserTransaction;

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


   /**
    * The <code>testAutoCommitInReturnedConnection</code> method tests that
    * if you set autocommit off and return a connection to the pool, when you
    * get it back, autocommit is on again.
    *
    * @exception Exception if an error occurs
    */
   public void testAutoCommitInReturnedConnection() throws Exception
   {
      s.testAutoCommitInReturnedConnection();
   }


   /**
    * The <code>testAutoCommit</code> method tests that autocommit is really on
    * when connections are obtained from the pool.
    *
    * @exception Exception if an error occurs
    */
   public void testAutoCommit() throws Exception
   {
      s.testAutoCommit();
   }


   /**
    * The <code>testAutoCommitOffInUserTx</code> method tests that an
    * explicit tx started with usertx turns off autocommit.
    *
    * @exception Exception if an error occurs
    */
   public void testAutoCommitOffInUserTx() throws Exception
   {
      s.testAutoCommitOffInUserTx();
   }

   /**
    * The <code>testAutoCommitOffInUserTx2</code> method tests the same thing
    * with the connection re-obtained after the tx is started.
    *
    * @exception Exception if an error occurs
    */
   public void testAutoCommitOffInUserTx2() throws Exception
   {
      s.testAutoCommitOffInUserTx2();
   }

   /**
    * The <code>testAutoCommitOffInRemoteUserTx</code> method tests the same
    * operations but all called from here: thus the operations presumably occur
    * in different threads.  As of now, this fails: the insert is committed 
    * even though the underlying connection reports autocommit to be off.
    *
    * @exception Exception if an error occurs
    */
   public void testAutoCommitOffInRemoteUserTx() throws Exception
   {
      try 
      {
         s.createTable();
         UserTransaction ut = (UserTransaction)getInitialContext().lookup("UserTransaction");
         ut.begin();
         s.insertAndCheckAutoCommit();
         ut.rollback();
      }
      finally
      {
         s.checkRowAndDropTable();         
      } // end of try-catch

   }

   /*This test requires a real database with actual transaction isolation, not hsqldb.
   public void testTxIsolationInReturnedConnection() throws Exception
   {
      s.testTxIsolationInReturnedConnection();
   }
   */

}// LocalWrapperCleanupUnitTestCase
