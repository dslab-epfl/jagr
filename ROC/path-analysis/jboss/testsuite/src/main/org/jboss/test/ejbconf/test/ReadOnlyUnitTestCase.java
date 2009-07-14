
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.test.ejbconf.test; // Generated package name

import junit.framework.*;
import org.jboss.test.JBossTestCase;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnly;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnlyHome;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnlyHelper;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnlyHelperHome;
import java.util.Iterator;
import java.util.Collection;


/**
 * ReadOnlyUnitTestCase.java
 *
 *
 * Created: Wed Jan 30 00:16:57 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class ReadOnlyUnitTestCase extends JBossTestCase 
{
   public ReadOnlyUnitTestCase (String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ReadOnlyUnitTestCase.class, "ejbconf-test.jar");
   }

   public void testReadOnly() throws Exception
   {
      ReadOnlyHelperHome rohh = (ReadOnlyHelperHome)getInitialContext().lookup("ReadOnlyHelper");
      ReadOnlyHelper roHelper = rohh.create();
      roHelper.setUp();
      ReadOnlyHome roh = (ReadOnlyHome)getInitialContext().lookup("ReadOnly");
      ReadOnly ro = roh.findByPrimaryKey(new Integer(1));
      assertTrue("ReadOnly didn't get correct initial value", ro.getValue().equals(new Integer(1)));
      ro.setValue(new Integer(2));
      assertTrue("ReadOnly changed value in db", roHelper.checkValue() == 1);
      //assertTrue("ReadOnly changed value", ro.getValue().equals(new Integer(1)));

   }
   
}// ReadOnlyUnitTestCase
