/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.security.test;

import javax.security.auth.login.Configuration;

import org.apache.log4j.Logger;

import org.jboss.logging.XLevel;
import org.jboss.security.auth.login.XMLLoginConfig;

/** Tests of the LoginModule classes using the XMLLoginConfig implementation
 of the JAAS login module configuration.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 1.1.1.1 $
 */
public class XMLLoginModulesUnitTestCase extends LoginModulesUnitTestCase
{

   public XMLLoginModulesUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      // Install the custom JAAS configuration
      XMLLoginConfig config = new XMLLoginConfig();
      config.setConfigResource("login-config.xml");
      config.start();
      Configuration.setConfiguration(config);

      // Turn on trace level logging
      Logger root = Logger.getRootLogger();
      root.setLevel(XLevel.TRACE);
      root.setPriority(XLevel.TRACE);
   }

   
}
