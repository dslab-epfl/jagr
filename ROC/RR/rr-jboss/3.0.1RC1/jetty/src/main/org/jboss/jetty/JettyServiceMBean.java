/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jetty;

import javax.management.ObjectName;
import org.jboss.deployment.DeploymentException;
import org.jboss.util.jmx.ObjectNameFactory;
import org.w3c.dom.Element;

/**
 * ???
 *
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.1.1.1 $
 */
public interface JettyServiceMBean
   extends org.jboss.web.AbstractWebContainerMBean
{
  ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.web:service=JBossWeb");

  public void setConfigurationElement (Element configElement);
  public Element getConfigurationElement ();

  public boolean getUnpackWars();
  public void setUnpackWars(boolean unpackWars);

  public String getWebDefault();
  public void setWebDefault(String webDefault);

  public String getHttpSessionStorageStrategy();
  public void setHttpSessionStorageStrategy(String storageStrategy);

  public String getHttpSessionSnapshotFrequency();
  public void setHttpSessionSnapshotFrequency(String snapshotFrequency);

  public String getHttpSessionSnapshotNotificationPolicy();
  public void setHttpSessionSnapshotNotificationPolicy(String snapshotNotificationPolicy);

  public String getSubjectAttributeName();
  public void setSubjectAttributeName(String subjectAttributeName);

  public boolean getJava2ClassLoadingCompliance ();
  public void setJava2ClassLoadingCompliance(boolean compliance);
}
