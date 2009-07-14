/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.resource;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;

import org.jboss.deployment.SubDeployerMBean;

/**
 * Exposed management interface for the <code>RARDeployer</code> service.
 *
 * @author     Toby Allsopp (toby.allsopp@peace.com)
 * @version    $Revision: 1.1.1.1 $
 */
public interface RARDeployerMBean
   extends SubDeployerMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=RARDeployer");
}
