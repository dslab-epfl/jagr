/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: JettyService.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $

//------------------------------------------------------------------------------

package org.jboss.jetty;

//------------------------------------------------------------------------------

import java.util.Iterator;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.jboss.deployment.DeploymentException;
import org.jboss.jetty.log.JBossLogSink;
import org.jboss.logging.Logger;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
import org.jboss.web.AbstractWebContainer;
import org.jboss.web.WebApplication;
import org.mortbay.util.Log;
import org.mortbay.util.MultiException;
import org.mortbay.util.jmx.CodeMBean;
import org.mortbay.util.jmx.LogMBean;
import org.w3c.dom.Element;

//------------------------------------------------------------------------------
/**
 * A service to launch jetty from JMX.
 *
 * @author <a href="mailto:jules_gosnell@yahoo..com">Julian Gosnell</a>
 * @version $Revision: 1.1.1.1 $
 */

public class JettyService
  extends AbstractWebContainer
  implements JettyServiceMBean, MBeanRegistration
{
  public static final String NAME = "JBossWeb";

  // delegate to Jetty
  protected MBeanServer _server       = null;
  protected Jetty       _jetty        = null;
  protected JettyMBean  _jettyBean    = null;
  protected CodeMBean   _codeBean     = null;
  protected LogMBean    _logBean      = null;
  protected Element     _jettyConfig  = null;

  public
    JettyService()
  {
    super();

    // sort out a JBossLogSink for use by Jetty. This will bridge the
    // API between Jetty and Log4J...
    JBossLogSink logSink = new JBossLogSink();
    try
    {
      logSink.initialize(Logger.getLogger("org.jboss.jbossweb"));
    }
    catch(Exception e)
    {
      log.error("could not connect JBoss and Jetty Log models", e);
    }

    logSink.start();
    Log.instance().disableLog(); // remove default logger
    Log.instance().add(logSink);

    log.debug("connected JBoss and Jetty Log models");

    _jetty = new Jetty(this);
  }

  //----------------------------------------------------------------------------
  // utils...
  //----------------------------------------------------------------------------

  /**
   * mex should implement a better printStackTrace...
   */
  protected void
    log(MultiException e)
  {
    log.error("multiple exceptions...");
    Iterator iter = e.getExceptions().iterator();
    while (iter.hasNext())
      log.error("exception", (Exception)iter.next());
  }

  //----------------------------------------------------------------------------

  public ObjectName
    preRegister(MBeanServer server, ObjectName name)
    throws Exception
  {
    super.preRegister(server,name);
    name = getObjectName(server, name);
    _server = server;

    return name;
  }

  public void
    postRegister(Boolean done)
  {
    super.postRegister(done);

    // this must be done before config is read otherwise configs
    // defined therein will not receive MBean peers. Since it must now
    // be done before JMX has a chance to configure us, I'm removing
    // the option not to have these MBeans built...
    try
    {
      _jettyBean    = new JettyMBean(_jetty);
      _server.registerMBean(_jettyBean, null);

      _codeBean     = new CodeMBean();
      _server.registerMBean(_codeBean, null);

      _logBean      = new LogMBean();
      _server.registerMBean(_logBean, null);
    }
    catch (Throwable e)
    {
      log.error("could not create MBean peers", e);
    }

    log.debug("created MBean peers");
  }

  //----------------------------------------------------------------------------
  // 'name' interface
  //----------------------------------------------------------------------------

  public ObjectName
    getObjectName(MBeanServer server, ObjectName name)
    throws MalformedObjectNameException
  {
    return OBJECT_NAME;
  }

  public String
    getName()
  {
    return NAME;
  }

  //----------------------------------------------------------------------------
  // 'service' interface
  //----------------------------------------------------------------------------

  public void
    createService()
    throws Exception
  {
    super.createService();
    if( _jettyConfig != null )
      _jetty.setConfigurationElement(_jettyConfig);
  }

  public void
    startService()
    throws Exception
  {
    super.startService();

    try
    {
      _jetty.start();
    }
    catch (MultiException e)
    {
      log(e);
    }
    catch (Exception e)
    {
      log.error("could not start Jetty", e);
    }
  }

  public void
    stopService() throws Exception
  {
    super.stopService();

    try
    {
      _jetty.stop();
    }
    catch (Exception e)
    {
      log.error("could not stop Jetty", e);
    }
  }

  public void
    destroyService()
    throws Exception
  {
    super.destroyService();

    // this is not symmetrical - these things are created in
    // postRegister, not createService()...
    try
    {
      _jetty.destroy();
      _jetty    =null;
      _jettyBean=null;
    }
    catch (Throwable e)
    {
      log.error("could not destroy Jetty", e);
    }

    try
    {
      _server.unregisterMBean(_codeBean.getObjectName());
      _codeBean=null;
      _server.unregisterMBean(_logBean.getObjectName());
      _logBean=null;
    }
    catch(Exception e)
    {
      log.error("could not tidy up MBeans", e);
    }
  }

  //----------------------------------------------------------------------------
  // 'deploy' interface
  //----------------------------------------------------------------------------

  public void
    performDeploy(WebApplication webApp, String warUrl, WebDescriptorParser parser)
    throws DeploymentException
  {
    _jetty.deploy(webApp, warUrl, parser);
  }

  public void
    performUndeploy(String warUrl)
    throws DeploymentException
  {
    _jetty.undeploy(warUrl);
  }

  //----------------------------------------------------------------------------
  // Jetty properties - delegated directly...
  //----------------------------------------------------------------------------

  //----------------------------------------
  // class loader delegation policy property
  //----------------------------------------

  /**
   * @param loaderCompliance if true, Jetty delegates class loading
   *to parent class loader first, false implies servlet spec 2.3 compliance
   */
  public void
    setJava2ClassLoadingCompliance(boolean loaderCompliance)
  {
    if (log.isDebugEnabled())
      log.debug("set Java2ClassLoadingCompliance to "+ loaderCompliance);

    _jetty.setJava2ClassLoadingCompliance(loaderCompliance);
  }

  /**
   * @return true if Java2 style class loading delegation, false if
   *servlet2.3 spec compliance
   */
  public boolean
    getJava2ClassLoadingCompliance()
  {
    return _jetty.getJava2ClassLoadingCompliance();
  }

  //----------------------------------------------------------------------------

  public boolean
    getUnpackWars()
  {
    return _jetty.getUnpackWars();
  }

  public void
    setUnpackWars(boolean unpackWars)
  {
    if (log.isDebugEnabled())
      log.debug("set UnpackWars to "+unpackWars);

    _jetty.setUnpackWars(unpackWars);
  }

  //----------------------------------------------------------------------------

  public String
    getWebDefault()
  {
    return _jetty.getWebDefault();
  }

  public void
    setWebDefault(String webDefault)
  {
    if (log.isDebugEnabled())
      log.debug("set WebDefault to "+webDefault);

    _jetty.setWebDefault(webDefault);
  }

  //----------------------------------------------------------------------------

  public String
    getHttpSessionStorageStrategy()
  {
    return _jetty.getHttpSessionStorageStrategy();
  }

  public void
    setHttpSessionStorageStrategy(String storageStrategy)
  {
    if (log.isDebugEnabled())
      log.debug("set HttpSessionStorageStrategy to "+storageStrategy);

    _jetty.setHttpSessionStorageStrategy(storageStrategy);
  }

  //----------------------------------------------------------------------------

  public String
    getHttpSessionSnapshotFrequency()
  {
    return _jetty.getHttpSessionSnapshotFrequency();
  }

  public void
    setHttpSessionSnapshotFrequency(String snapshotFrequency)
  {
    if (log.isDebugEnabled())
      log.debug("set HttpSessionSnapshotFrequency to "+snapshotFrequency);

    _jetty.setHttpSessionSnapshotFrequency(snapshotFrequency);
  }

  //----------------------------------------------------------------------------

  public String
    getHttpSessionSnapshotNotificationPolicy()
  {
    return _jetty.getHttpSessionSnapshotNotificationPolicy();
  }

  public void
    setHttpSessionSnapshotNotificationPolicy(String snapshotNotificationPolicy)
  {
    if (log.isDebugEnabled())
      log.debug("set HttpSessionSnapshotNotificationPolicy to "+snapshotNotificationPolicy);

    _jetty.setHttpSessionSnapshotNotificationPolicy(snapshotNotificationPolicy);
  }

  //----------------------------------------------------------------------------
  /** Get the extended Jetty configuration XML fragment
   * @return Jetty XML fragment embedded in jboss-service.xml
   */

  public Element
    getConfigurationElement()
  {
    //return _jetty.getConfigurationElement();
     return _jettyConfig;
  }

  /** Configure Jetty
   * @param configElement XML fragment from jboss-service.xml
   */
  public void
    setConfigurationElement(Element configElement)
  {
    log.debug("Saving Configuration to xml fragment");
    this._jettyConfig = configElement;
    // Don't apply this now as this element can be set more than during init
    //_jetty.setConfigurationElement (configElement);
  }

  //----------------------------------------------------------------------------

  public String
    getSubjectAttributeName()
  {
    return _jetty.getSubjectAttributeName();
  }

  public void
    setSubjectAttributeName(String subjectAttributeName)
  {
    if (log.isDebugEnabled())
      log.debug("set SubjectAttributeName to "+subjectAttributeName);

    _jetty.setSubjectAttributeName(subjectAttributeName);
  }

  //----------------------------------------------------------------------------
}
