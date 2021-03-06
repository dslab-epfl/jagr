/*
 * JBoss, the OpenSource J2EE server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.jmx.loading;

import org.jboss.system.ServiceMBean;

/** An mbean service for testing loading resource local to the sar
 *   
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 *
 */
public interface ResourceTstsMBean extends ServiceMBean
{
   public void setNamespace(String namespace);
}
