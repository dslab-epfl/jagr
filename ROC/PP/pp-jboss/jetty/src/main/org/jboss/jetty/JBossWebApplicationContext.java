/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: JBossWebApplicationContext.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $

// A Jetty HttpServer with the interface expected by JBoss'
// J2EEDeployer...

//------------------------------------------------------------------------------

package org.jboss.jetty;

//------------------------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Method;
import javax.xml.parsers.DocumentBuilder;
import org.jboss.jetty.security.JBossUserRealm;
import org.jboss.logging.Logger;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
import org.jboss.web.WebApplication;
import org.mortbay.http.ContextLoader;
import org.mortbay.http.HttpServer;
import org.mortbay.http.handler.SecurityHandler;
import org.mortbay.j2ee.J2EEWebApplicationContext;
import org.mortbay.j2ee.J2EEWebApplicationHandler;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.SessionManager;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.util.Resource;
import org.mortbay.xml.XmlParser;

//------------------------------------------------------------------------------

public class
  JBossWebApplicationContext
  extends J2EEWebApplicationContext
{
  Logger              _log;
  Jetty               _jetty;
  WebDescriptorParser _descriptorParser;
  WebApplication      _webApp;
  DocumentBuilder     _parser;
  String              _subjAttrName="j_subject";
  JBossUserRealm      _realm=null;

  public
    JBossWebApplicationContext(Jetty jetty,
			       WebDescriptorParser descriptorParser,
			       WebApplication webApp,
			       DocumentBuilder parser,
			       String warUrl)
    throws IOException
    {
      super(warUrl);

      _log              = Logger.getLogger(getClass().getName());
      _jetty            = jetty;
      _descriptorParser = descriptorParser;
      _webApp           = webApp;
      _parser           = parser;
      _subjAttrName     = jetty.getSubjectAttributeName();

      // other stuff
      _stopGracefully                             =_jetty.getStopWebApplicationsGracefully();

      // we'll add this when we figure out where to get the TransactionManager...
      //      addHandler(new JBossWebApplicationHandler());
    }

  /* ------------------------------------------------------------ */

  public void
    setContextPath(String contextPathSpec)
    {
      _log  = Logger.getLogger(getClass().getName()+"#" + contextPathSpec);
      super.setContextPath(contextPathSpec);
    }

  /* ------------------------------------------------------------ */

  // avoid Jetty moaning about things that it doesn't but AbstractWebContainer does do...
  protected void
    initWebXmlElement(String element, org.mortbay.xml.XmlParser.Node node)
    throws Exception
    {
      // this is ugly - should be dispatched through a hash-table or introspection...

      // these are handled by AbstractWebContainer
      if ("resource-ref".equals(element) ||
	  "resource-env-ref".equals(element) ||
	  "env-entry".equals(element) ||
	  "ejb-ref".equals(element) ||
	  "ejb-local-ref".equals(element) ||
	  "security-domain".equals(element))
      {
	//_log.info("Don't moan : "+element);
      }
      else if ("distributable".equals(element))
      {
	setDistributable(true);
      }
      else if ("login-config".equals(element))
      {
	// we need to get hold of the real-name...
	super.initWebXmlElement(element, node); // Greg has now consumed it

	String realmName=getRealmName();
	_log.debug("setting Realm: "+realmName);
	_realm=new JBossUserRealm(realmName, _subjAttrName); // we init() it later
	setRealm(_realm); // cache and reuse ? - TODO
      }
      // these are handled by Jetty
      else
	super.initWebXmlElement(element, node);
    }

  // this is a hack - but we need the session timeout - in case we are
  // going to use a distributable session manager....
  protected boolean _timeOutPresent=false;
  protected int _timeOutMinutes=0;

  protected void
    initSessionConfig(XmlParser.Node node)
    {
      XmlParser.Node tNode=node.get("session-timeout");
      if (tNode!=null)
      {
	_timeOutPresent=true;
	_timeOutMinutes=Integer.parseInt(tNode.toString(false,true));
      }

      // pass up to our super class so they can do all this again !
      super.initSessionConfig(node);
    }

  // hack our class loader to be Java2 compliant - i.e. always
  // delegate upwards before looking locally. This will be changed to
  // a non-compliant strategy later when JBoss' new ClassLoader is
  // ready.
  protected void initClassLoader(boolean forceContextLoader)
    throws java.net.MalformedURLException, IOException
    {
      // force the creation of a context class loader for JBoss
      // web apps
      super.initClassLoader(true);

      ClassLoader _loader=getClassLoader();
      if (_loader instanceof org.mortbay.http.ContextLoader)
	((org.mortbay.http.ContextLoader)_loader).setJava2Compliant(_jetty.getJava2ClassLoadingCompliance());
    }

  String _separator=System.getProperty("path.separator");

  public String
    getFileClassPath()
    {
      List list=new ArrayList();
      getFileClassPath(getClassLoader(), list);

      String classpath="";
      for (Iterator i=list.iterator(); i.hasNext();)
      {
	URL url=(URL)i.next();

	url=org.jboss.net.protocol.njar.Handler.njarToFile(url);

	if (!url.getProtocol().equals("file")) // tmp warning
	{
	  _log.warn("JSP classpath: non-'file' protocol: "+url);
	  continue;
	}

 	try
 	{
 	  Resource res = Resource.newResource (url);
 	  if (res.getFile()==null)
 	    _log.warn("bad classpath entry: "+url);
 	  else
 	  {
 	    String tmp=res.getFile().getCanonicalPath();
	    //	    _log.info("JSP FILE: "+url+" --> "+tmp+" : "+url.getProtocol());
	    classpath+=(classpath.length()==0?"":_separator)+tmp;
 	  }
 	}
 	catch (IOException ioe)
 	{
 	  _log.warn ("JSP Classpath is damaged, can't convert path for :"+url, ioe);
 	}
      }

      _log.trace("JSP classpath: "+classpath);
      return classpath;
    }

  public void
    getFileClassPath(ClassLoader cl, List list)
    {
      if (cl==null)
         return;

      URL[] urls=null;

       try
       {
          Class[] sig = {};
          Method getAllURLs = cl.getClass().getMethod("getAllURLs", sig);
          Object[] args = {};
          urls = (URL[]) getAllURLs.invoke(cl, args);
       }
       catch(Exception ignore)
       {
       }
       if (urls == null && cl instanceof java.net.URLClassLoader)
          urls=((java.net.URLClassLoader)cl).getURLs();

      //      _log.info("CLASSLOADER: "+cl);
      //      _log.info("URLs: "+(urls!=null?urls.length:0));

       if (urls != null)
       {
          for (int i = 0; i < urls.length; i++)
          {
             URL url = urls[i];

             if (url != null)
             {
                String path = url.getPath();
                if (path != null)
                {
                   File f = new File(path);

                   if (f.exists() &&
                         f.canRead() &&
                         (f.isDirectory() ||
                         path.endsWith(".jar") ||
                         path.endsWith(".zip")) &&
                         (!list.contains(url))
                   )
                      list.add(url);
                }
             }
          }
       }

      getFileClassPath(cl.getParent(), list);
    }

  // given a resource name, find the jar file that contains that resource...
  protected String
    findJarByResource(String resource)
    throws Exception
    {
      String path=getClass().getClassLoader().getResource(resource).toString();
      // lose initial "jar:file:" and final "!/..."
      return path.substring("jar:file:".length(),path.length()-(resource.length()+2));
    }

  protected void
    startHandlers()
    throws Exception
    {
      if (getDistributable() && getDistributableSessionManager()!=null)
	setUpDistributableSessionManager();

      setUpENC();

      if (_realm!=null)
	_realm.init();

      super.startHandlers();
    }


  protected void
    setUpDistributableSessionManager()
    {
      try
      {
	SessionManager sm=getDistributableSessionManager();

	if (_timeOutPresent)
	  sm.setMaxInactiveInterval(_timeOutMinutes*60);

	getServletHandler().setSessionManager(sm);
	_log.info("using Distributable HttpSession Manager: "+sm);
      }
      catch (Exception e)
      {
	_log.error("could not set up Distributable HttpSession Manager - using local one", e);
      }
    }

  protected void
    setUpENC() throws Exception
    {
      _webApp.setClassLoader(Thread.currentThread().getContextClassLoader());
      _webApp.setName(getDisplayName());
      _webApp.setAppData(this);

      _log.debug("setting up ENC...");
      _descriptorParser.parseWebAppDescriptors(_webApp.getClassLoader(),
					       _webApp.getMetaData());
      _log.debug("setting up ENC succeeded");
    }


  //----------------------------------------

  // lose this when new J2EEWebApplicationContext goes in...

  protected boolean _stopGracefully=false;

  public boolean getStopGracefully() { return _stopGracefully; }
  public void setStopGracefully(boolean stopGracefully) { _stopGracefully=stopGracefully; }
}
