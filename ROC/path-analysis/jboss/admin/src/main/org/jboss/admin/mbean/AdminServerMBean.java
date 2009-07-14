/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.admin.mbean;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;

import org.jboss.system.ServiceMBean;


/**
 *   ... 
 *      
 *   @author Juha Lindfors (jplindfo@helsinki.fi)
 *   @version $Revision: 1.1.1.1 $
 */
public interface AdminServerMBean
   extends ServiceMBean
{

   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.admin:name=AdminServer");

   void createRoot(String user, char[] pw);
}
