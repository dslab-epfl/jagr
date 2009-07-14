/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.cts.test;



import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import javax.ejb.*;
import javax.naming.*;
import javax.management.*;
import org.jboss.test.cts.interfaces.*;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;



/**
 *
 *   @see <related>
 *   @author Author: kimptoc 
 *   @author Author: d_jencks converted to JBossTestCase and logging.
 *   @version $Revision: 1.1.1.1 $
 */

public class StatelessSessionStressTestCase
   extends JBossTestCase
{

   // Constants -----------------------------------------------------
   // Attributes ----------------------------------------------------
   // Static --------------------------------------------------------
    //static boolean deployed = false;

   // Constructors --------------------------------------------------

   /**
    * Constructor Main
    *
    *
    * @param name
    *
    */

   public StatelessSessionStressTestCase (String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------






   //-------------------------------------------------------------------------
   // EJB 1.1 
   // 

   /**
    * Method testCallbacks
    * What is being tested here??? Is there some use to this test???
    *
    */

   public void testCallbacks ()
   {
      getLog().debug("Callback test");

      StatelessSession sessionBean[] = new StatelessSession[getBeanCount()];
      StatelessSessionHome home = null;
      int i = 0;

      try
      {
         getLog().debug("Obtain home interface");

         home =
            (StatelessSessionHome)getInitialContext().lookup("ejbcts/StatelessSessionBean");
      }
      catch (Exception Ex)
      {
         fail("Caught an Unknown Exception in lookup");
      }

      try
      {
         for (i = 0; i < getBeanCount(); i++)
         {
            sessionBean [i] = home.create();

            sessionBean [i].method1("test");
            Thread.sleep(500);
         }

         // Kill all the beans
         for (i = 0; i < getBeanCount(); i++)
            sessionBean [i].remove();
      }
      catch (Exception ex)
      {
         getLog().error("error", ex);
         fail("Caught an unknown exception");
      }
   }   // testCallbacks()

   public static Test suite() throws Exception
   {
      return getDeploySetup(StatelessSessionStressTestCase.class, "cts.jar");
   }


}


/*------ Formatted by Jindent 3.23 Basic 1.0 --- http://www.jindent.de ------*/
