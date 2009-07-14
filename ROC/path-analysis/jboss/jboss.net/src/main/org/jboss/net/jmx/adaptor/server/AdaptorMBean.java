/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: AdaptorMBean.java,v 1.1.1.1 2002/11/16 03:16:50 mikechen Exp $

package org.jboss.net.jmx.adaptor.server;

import javax.management.MBeanServer;

import org.jboss.system.ServiceMBean;

/**
 * just for compliance purposes, we have to
 * provide an empty mbean interface
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @created October 2, 2001
 * @version $Revision: 1.1.1.1 $
 * Change History:
 * <ul>
 * <li> jung, 6.2.2002: Added axis deployer name configuration. </li>
 * </ul>
 */

public interface AdaptorMBean extends ServiceMBean, MBeanServer {
  public String getWebServiceDeployerName();
  public void setWebServiceDeployerName(String name);
}