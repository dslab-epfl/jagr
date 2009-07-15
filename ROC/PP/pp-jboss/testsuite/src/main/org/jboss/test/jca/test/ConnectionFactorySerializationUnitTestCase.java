
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
import org.jboss.test.jca.interfaces.ConnectionFactorySerializationTestSessionHome;
import org.jboss.test.jca.interfaces.ConnectionFactorySerializationTestSession;


/**
 * ConnectionFactorySerializationUnitTestCase.java
 *
 *
 * Created: Thu May 23 23:40:31 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class ConnectionFactorySerializationUnitTestCase extends JBossTestCase 
{
   ConnectionFactorySerializationTestSessionHome sh;
   ConnectionFactorySerializationTestSession s;


   public ConnectionFactorySerializationUnitTestCase (String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      sh = (ConnectionFactorySerializationTestSessionHome)getInitialContext().lookup("ConnectionFactorySerializationTestSession");
      s = sh.create();
   }

   protected void tearDown() throws Exception
   {
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ConnectionFactorySerializationUnitTestCase.class, 
                        "jcatest.jar");
   }

   public void testConnectionFactorySerialization() throws Exception
   {
      s.testConnectionFactorySerialization();
   }

   
}// ConnectionFactorySerializationUnitTestCase
