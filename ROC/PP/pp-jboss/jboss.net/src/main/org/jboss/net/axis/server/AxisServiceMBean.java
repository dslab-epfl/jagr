/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

// $Id: AxisServiceMBean.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $
 
package org.jboss.net.axis.server;

/**
 * Mbean interface to the AxisService 
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @created 27. September 2001
 * @version $Revision: 1.1.1.1 $
 */
public interface AxisServiceMBean 
   extends org.jboss.deployment.SubDeployer, org.jboss.system.ServiceMBean
{    
  public String getRootContext();
  public void setRootContext(String name) throws Exception;
  public String getSecurityDomain();
  public void setSecurityDomain(String name) throws Exception;
}
