/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: Jetty.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $

// A Jetty HttpServer with the interface expected by JBoss'
// J2EEDeployer...

//------------------------------------------------------------------------------

package org.jboss.jetty;

//------------------------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jboss.deployment.DeploymentException;
import org.jboss.jetty.xml.JettyResolver;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
import org.jboss.web.WebApplication;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.util.MultiException;
import org.mortbay.xml.XmlConfiguration;
import org.w3c.dom.Element;

//------------------------------------------------------------------------------

/**
 * <description>
 *
 * @author <a href="mailto:jules_gosnell@yahoo..com">Julian Gosnell</a>
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20011201 andreas:</b>
 * <ul>
 * <li>Fixed fixURL() because it is to "Unix" centric. Right now the
 *     method looks for the last part of the JAR URL (file:/...) which
 *     should be the JAR file name and add a "/." before them. Now this
 *     should work for Windows as well (the problem with windows was that
 *     after "file:" came the DRIVE LETTER which created a wrong URL).
 * </ul>
 **/
public class Jetty
  extends org.mortbay.jetty.Server
{
  static DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();

  DocumentBuilder               _parser;
  Logger                        _log     = Logger.getLogger("org.jboss.jbossweb");
  JettyService                  _service;

  // the XML snippet
  String _xmlConfigString = null;

  // the XML snippet as a DOM element
  Element _configElement = null;

  Jetty(JettyService service)
  {
    super();

    _service=service;

    // resolver should be populated from a configuration file.
    JettyResolver resolver = new JettyResolver();

    // populate dtd resolver
    URL stdWeb22=findResourceInJar("javax/servlet/resources/web-app_2_2.dtd");
    resolver.put("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN", stdWeb22);
    URL stdWeb23=findResourceInJar("javax/servlet/resources/web-app_2_3.dtd");
    resolver.put("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN", stdWeb23);

    URL jbossWeb2=findResourceInJar("org/jboss/metadata/jboss-web.dtd");
    resolver.put("-//jBoss//DTD Web Application 2.2//EN", jbossWeb2);
    resolver.put("-//JBoss//DTD Web Application 2.2//EN", jbossWeb2);

    URL jbossWeb3=findResourceInJar("org/jboss/metadata/jboss-web_3_0.dtd");
    resolver.put("-//JBoss//DTD Web Application 2.3//EN", jbossWeb3);

    try
    {
      _parser=_factory.newDocumentBuilder();
      _parser.setEntityResolver(resolver);
      //      _parser.setErrorHandler();
    }
    catch (Exception e)
    {
      _log.error("problem building descriptor parser", e);
    }

    // check support for JSP compilation...
    if (findResourceInJar("com/sun/tools/javac/v8/resources/javac.properties")==null)
      _log.warn("JSP compilation requires $JAVA_HOME/lib/tools.jar on your JBOSS_CLASSPATH");
  }

  //----------------------------------------
  // class loader delegation policy property
  //----------------------------------------
  boolean _loaderCompliance = true;

  /**
   * @param loaderCompliance if true, Jetty delegates class loading
   *to parent class loader first, false implies servlet spec 2.3 compliance
   */
  public synchronized void
    setJava2ClassLoadingCompliance (boolean loaderCompliance)
  {
    _loaderCompliance = loaderCompliance;
  }

  /**
   * @return true if Java2 style class loading delegation, false if
   *servlet2.3 spec compliance
   */
  public synchronized boolean
    getJava2ClassLoadingCompliance ()
  {
    return _loaderCompliance;
  }

  //----------------------------------------
  // unpackWars property
  //----------------------------------------

  boolean _unpackWars=false;

  public synchronized void
    setUnpackWars(boolean unpackWars)
  {
    _unpackWars=unpackWars;
  }

  public synchronized boolean
    getUnpackWars()
  {
    return _unpackWars;
  }

  //----------------------------------------
  // webDefault property
  //----------------------------------------

  String _webDefault;

  /** If a webdefault.xml file has been specified in
   * jboss-service.xml then we try and use that.
   *
   * If we cannot find it, then we will use the one
   * shipped as standard with Jetty and issue a warning.
   *
   * If the jboss-service.xml file does not specify a
   * custom one, then we again default to the standard one.
   * @param webDefault
   */
  public synchronized void
    setWebDefault(String webDefault)
  {
    if (webDefault != null)
    {
      URL webDefaultURL = findResourceInJar(webDefault);
      if (webDefaultURL != null)
	_webDefault=fixURL(webDefaultURL.toString());
      else
	_webDefault = null;
      _log.warn ("Cannot find resource for "+webDefault+": using default");
    }
    else
      _webDefault = null;

    if (_log.isDebugEnabled())
      _log.debug ("webdefault specification is: "+_webDefault);
  }

  public synchronized String
    getWebDefault()
  {
    return _webDefault;
  }

  //----------------------------------------
  // distributedStore property
  //----------------------------------------

  String _storageStrategy;

  public synchronized void
    setHttpSessionStorageStrategy(String storageStrategy)
  {
    _storageStrategy=storageStrategy;
  }

  public synchronized String
    getHttpSessionStorageStrategy()
  {
    return _storageStrategy;
  }

  //----------------------------------------
  // snapshotFrequency property
  //----------------------------------------

  String _snapshotFrequency;

  public synchronized void
    setHttpSessionSnapshotFrequency(String snapshotFrequency)
  {
    _snapshotFrequency=snapshotFrequency;
  }

  public synchronized String
    getHttpSessionSnapshotFrequency()
  {
    return _snapshotFrequency;
  }

  //----------------------------------------
  // snapshotNotificationPolicy property
  //----------------------------------------

  String _snapshotNotificationPolicy;

  public synchronized void
    setHttpSessionSnapshotNotificationPolicy(String snapshotNotificationPolicy)
  {
    _snapshotNotificationPolicy=snapshotNotificationPolicy;
  }

  public synchronized String
    getHttpSessionSnapshotNotificationPolicy()
  {
    return _snapshotNotificationPolicy;
  }

  //----------------------------------------
  // subjectAttributeName property
  //----------------------------------------

  String _subjectAttributeName;

  public synchronized void
    setSubjectAttributeName(String subjectAttributeName)
  {
    _subjectAttributeName=subjectAttributeName;
  }

  public synchronized String
    getSubjectAttributeName()
  {
    return _subjectAttributeName;
  }

  //----------------------------------------
  // configuration property
  //----------------------------------------

  public Element
    getConfigurationElement()
  {
    return _configElement;
  }

  /**
   * @param configElement XML fragment from jboss-service.xml
   */
  public void
    setConfigurationElement(Element configElement)
  {

    // convert to an xml string to pass into Jetty's normal
    // configuration mechanism
    _configElement = configElement;

    try
    {
      DOMSource source = new DOMSource(configElement);

      ByteArrayOutputStream stream = new ByteArrayOutputStream();

      StreamResult result = new StreamResult (stream);

      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      transformer.transform (source, result);

      _xmlConfigString = stream.toString();

      // get rid of the first line, as this will be prepended by
      // the XmlConfiguration
      int index = _xmlConfigString.indexOf("?>");
      if ( index >= 0)
      {
	index += 2;

	while ((_xmlConfigString.charAt(index) == '\n')
	       ||
	       (_xmlConfigString.charAt(index) == '\r'))
	  index++;
      }

      _xmlConfigString = _xmlConfigString.substring(index);

      _log.debug ("Passing xml config to jetty:\n"+_xmlConfigString);

      setXMLConfiguration (_xmlConfigString);

    }
    catch (TransformerConfigurationException tce)
    {
      _log.error ("Can't transform config Element -> xml:", tce);
    }
    catch (TransformerException te)
    {
      _log.error ("Can't transform config Element -> xml:", te);
    }
    catch (Exception e)
    {
      _log.error("Unexpected exception converting configuration Element -> xml", e);
    }
  }

  /* Actually perform the configuration
   * @param xmlString
   */
  private void
    setXMLConfiguration(String xmlString)
  {

    try
    {
      XmlConfiguration xmlConfigurator = new XmlConfiguration (xmlString);
      xmlConfigurator.configure(this);
    }
    catch (Exception e)
    {
      _log.error("problem configuring Jetty:", e);
    }
  }

  //----------------------------------------------------------------------------
  // 'deploy' interface
  //----------------------------------------------------------------------------

  Hashtable _deployed = new Hashtable(); // use Hashtable because is is synchronised

  public WebApplication
    deploy(WebApplication wa, String warUrl, WebDescriptorParser descriptorParser)
    throws DeploymentException
  {
    String contextPath = wa.getMetaData().getContextRoot();
    try
    {
      wa.setURL(new URL(warUrl));

      // check whether the context already exists... - a bit hacky,
      // could be nicer...
      if (getContext(null, contextPath, 0)!=null)
	_log.warn("A WebApplication is already deployed in context '"+contextPath+"' - proceed at your own risk.");

      // deploy the WebApp
      WebApplicationContext app=
	new JBossWebApplicationContext(this,
				       descriptorParser,
                                       wa,
                                       _parser,
				       warUrl);
      app.setContextPath(contextPath);


      // configure whether the context is to flatten the classes in
      // the WAR or not
      app.setExtractWAR (getUnpackWars());


      // if a different webdefaults.xml file has been provided, use it
      if (getWebDefault() != null)
	app.setDefaultsDescriptor (getWebDefault());


      String virtualHost=wa.getMetaData().getVirtualHost();
      addContext(virtualHost, app);

      // keep track of deployed contexts for undeployment
      _deployed.put(warUrl, app);

      try
      {
	// finally start the app
	app.start();
	_log.info("successfully deployed "+warUrl+" to "+contextPath);
      }
      catch (MultiException me)
      {
	_log.warn("problem deploying "+warUrl+" to "+contextPath);
	for (int i=0; i<me.size(); i++)
	{
	  Exception e=me.getException(i);
	  _log.warn(e, e);
	}
      }

    }
    catch (DeploymentException e)
    {
      undeploy(warUrl);
      throw e;
    }
    catch (Exception e)
    {
      undeploy(warUrl);
      throw new DeploymentException(e);
    }

    return wa;
  }

  public void
    undeploy(String warUrl)
    throws DeploymentException
  {
    // find the WebApp Context in the repository
    JBossWebApplicationContext app = (JBossWebApplicationContext)_deployed.get(warUrl);

    if (app==null)
    {
      _log.warn("app ("+warUrl+") not currently deployed");
    }
    else
    {
      try
      {
	app.stop(app.getStopGracefully());
	removeContext(app);
	app=null;

	_log.info("Successfully undeployed "+warUrl);
      }
      catch (Exception e)
      {
	throw new DeploymentException(e);
      }
    }

    _deployed.remove(warUrl);
  }

  public boolean
    isDeployed(String warUrl)
  {
    return (_deployed.get(warUrl)!=null);
  }

  //----------------------------------------------------------------------------
  // Utils
  //----------------------------------------------------------------------------

  public URL
    findResourceInJar(String name)
  {
    URL url=null;

    try
    {
      url=getClass().getClassLoader().getResource(name);
    }
    catch (Exception e)
    {
      _log.error("Could not find resource: "+name, e);
    }

    return url;
  }

  // work around broken JarURLConnection caching...
  static String
    fixURL(String url)
  {
    // Get the separator of the JAR URL and the file reference
    int index = url.indexOf( '!' );
    if( index >= 0 ) {
      index = url.lastIndexOf( '/', index );
    } else {
      index = url.lastIndexOf( '/' );
    }
    // Now add a "./" before the JAR file to add a different path
    if( index >= 0 ) {
      return
        url.substring( 0, index ) +
        "/." +
        url.substring( index );
    } else {
      // Now forward slash found then there is severe problem with
      // the URL but here we just ignore it
      return url;
    }
  }

  public String[]
    getCompileClasspath(ClassLoader cl)
  {
    return _service.getCompileClasspath(cl);
  }

  /** Override service method to allow ditching of security info
   * after a request has been processed
   * @param request
   * @param response
   * @return
   * @exception IOException
   * @exception HttpException
   */
  public HttpContext
    service(HttpRequest request,HttpResponse response)
    throws IOException, HttpException
  {
    try
    {
      return super.service(request,response);
    }
    finally
    {
      // Moved to JBossUserRealm.deAuthenticate(UserPrincipal);
      // SecurityAssociation.setPrincipal(null);
      // SecurityAssociation.setCredential(null);
    }
  }

  //----------------------------------------------------------------------------
  // DistributedHttpSession support
  //----------------------------------------------------------------------------

  protected String _distributableHttpSessionManagerClass;
  public void setDistributableHttpSessionManagerClass(String managerClass) {_distributableHttpSessionManagerClass=managerClass;}
  public String getDistributableHttpSessionManagerClass() {return _distributableHttpSessionManagerClass;}

  protected String _distributableHttpSessionStoreClass;
  public void setDistributableHttpSessionStoreClass(String storeClass) {_distributableHttpSessionStoreClass=storeClass;}
  public String getDistributableHttpSessionStoreClass() {return _distributableHttpSessionStoreClass;}

  protected List _distributableHttpSessionInterceptorClasses;
  public void setDistributableHttpSessionInterceptorClasses(List interceptorClasses) {_distributableHttpSessionInterceptorClasses=interceptorClasses;}
  public List getDistributableHttpSessionInterceptorClasses() {return _distributableHttpSessionInterceptorClasses;}

  protected int _httpSessionMaxInactiveInterval=-1;	// never time out
  public void setHttpSessionMaxInactiveInterval(int i) {_httpSessionMaxInactiveInterval=i;}
  public int getHttpSessionMaxInactiveInterval() {return _httpSessionMaxInactiveInterval;}

  protected int _httpSessionActualMaxInactiveInterval=60*60*24*7; // a week
  public void setHttpSessionActualMaxInactiveInterval(int i) {_httpSessionActualMaxInactiveInterval=i;}
  public int getHttpSessionActualMaxInactiveInterval() {return _httpSessionActualMaxInactiveInterval;}

  protected int _localHttpSessionScavengePeriod=60*10; // 10 mins
  public void setLocalHttpSessionScavengePeriod(int i) {_localHttpSessionScavengePeriod=i;}
  public int getLocalHttpSessionScavengePeriod() {return _localHttpSessionScavengePeriod;}

  protected int _distributableHttpSessionScavengePeriod=60*60; // 1 hour
  public void setDistributableHttpSessionScavengePeriod(int i) {_distributableHttpSessionScavengePeriod=i;}
  public int getDistributableHttpSessionScavengePeriod() {return _distributableHttpSessionScavengePeriod;}

  protected int _distributableHttpSessionScavengeOffset=(int)(_localHttpSessionScavengePeriod*1.5); // 15 mins
  public void setDistributableHttpSessionScavengeOffset(int i) {_distributableHttpSessionScavengeOffset=i;}
  public int getDistributableHttpSessionScavengeOffset() {return _distributableHttpSessionScavengeOffset;}

  protected boolean _distributableHttpSession=false;
  public boolean getDistributableHttpSession() {return _distributableHttpSession;}
  public void setDistributableHttpSession(boolean distributable) {_distributableHttpSession=distributable;}

  //----------------------------------------------------------------------------

  protected boolean _stopWebApplicationsGracefully=false;
  public boolean getStopWebApplicationsGracefully() {return _stopWebApplicationsGracefully;}
  public void setStopWebApplicationsGracefully(boolean graceful) {_stopWebApplicationsGracefully=graceful;}
}
