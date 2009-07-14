
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.test.jca.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.CachedConnectionSessionHome;
import org.jboss.test.jca.interfaces.CachedConnectionSession;


/**
 * CachedConnectionSessionUnitTestCase.java
 * Tests connection disconnect-reconnect mechanism.
 *
 * Created: Fri Mar 15 22:48:41 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class CachedConnectionSessionUnitTestCase extends JBossTestCase 
{

   private CachedConnectionSessionHome sh;
   private CachedConnectionSession s;

   public CachedConnectionSessionUnitTestCase (String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      sh = (CachedConnectionSessionHome)getInitialContext().lookup("CachedConnectionSession");
      s = sh.create();
      try 
      {
         s.dropTable();         
      }
      catch (Exception e)
      {
         //ignore, tables were missing.
      } // end of try-catch
      s.createTable();
   }

   protected void tearDown() throws Exception
   {
      if (s != null) 
      {
         s.dropTable();
      } // end of if ()
      
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(CachedConnectionSessionUnitTestCase.class, "jcatest.jar");
   }

   public void testCachedConnectionSession() throws Exception
   {
      s.insert(1L, "testing");
      assertTrue("did not get expected value back", "testing".equals(s.fetch(1L)));
   }
   
}// CachedConnectionSessionUnitTestCase
