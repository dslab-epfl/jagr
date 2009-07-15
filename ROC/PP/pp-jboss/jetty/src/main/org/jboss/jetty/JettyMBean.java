/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: JettyMBean.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $

package org.jboss.jetty;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.mortbay.jetty.jmx.ServerMBean;

public class JettyMBean
  extends ServerMBean
{
  public static final String JBOSS_DOMAIN = "jboss.web";

  static
  {
    setDefaultDomain (JBOSS_DOMAIN);
  }

  public JettyMBean(Jetty jetty)
    throws MBeanException, InstanceNotFoundException
  {
    super(jetty);
  }
}
