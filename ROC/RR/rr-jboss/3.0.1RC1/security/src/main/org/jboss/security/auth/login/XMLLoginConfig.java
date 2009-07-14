
/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security.auth.login;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.security.auth.AuthPermission;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;

import org.jboss.logging.Logger;

/** An concrete implementation of the javax.security.auth.login.Configuration
 class that parses an xml configuration of the form:
 
 <policy>
    <application-policy name = "test-domain">
       <authentication>
          <login-module code = "org.jboss.security.plugins.samples.IdentityLoginModule"
             flag = "required">
          <module-option name = "principal">starksm</module-option>
          </login-module>
       </authentication>
    </application-policy>
 </policy>
 
 @see javax.security.auth.login.Configuration
 
 @author Scott.Stark@jboss.org
 @version $Revision: 1.1.1.1 $
 */
public class XMLLoginConfig extends Configuration
   implements XMLLoginConfigMBean
{
   private static final String DEFAULT_APP_CONFIG_NAME = "other";
   private static final AuthPermission REFRESH_PERM = new AuthPermission("refreshPolicy");
   private static Logger log = Logger.getLogger(XMLLoginConfig.class);

   /** A mapping of application name to AppConfigurationEntry[] */
   private HashMap appConfigs = new HashMap();
   private int state = XMLLoginConfigMBean.STOPPED;
   private URL xmlConfig;
   private Configuration parentConfig;
   private boolean validateDTD = true;

   public XMLLoginConfig()
   {
   }
   
   public void refresh()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(REFRESH_PERM);
   }
   
   public AppConfigurationEntry[] getAppConfigurationEntry(String appName)
   {
      AppConfigurationEntry[] entry = null;
      AuthenticationInfo authInfo = (AuthenticationInfo) appConfigs.get(appName);
      if( authInfo != null )
      {
         entry = authInfo.copyAppConfigurationEntry();
      }
      else
      {
         if( parentConfig != null )
            entry = parentConfig.getAppConfigurationEntry(appName);
         if( entry == null )
            authInfo = (AuthenticationInfo) appConfigs.get(DEFAULT_APP_CONFIG_NAME);
         if( authInfo != null )
         {
            if( log.isTraceEnabled() )
               log.trace("getAppConfigurationEntry, authInfo="+authInfo);
            entry = authInfo.copyAppConfigurationEntry();
         }
      }

      return entry;
   }
   
// --- Begin XMLLoginConfigMBean interface methods
   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public URL getConfigURL()
   {
      return xmlConfig;
   }
   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public void setConfigURL(URL xmlConfig)
   {
      this.xmlConfig = xmlConfig;
   }

   /** Set the resource name of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public void setConfigResource(String resourceName)
      throws IOException
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      xmlConfig = tcl.getResource(resourceName);
      if( xmlConfig == null )
         throw new IOException("Failed to find resource: "+resourceName);
   }

   /** Get whether the login config xml document is validated againsts its DTD
    */
   public boolean getValidateDTD()
   {
      return this.validateDTD;
   }
   /** Set whether the login config xml document is validated againsts its DTD
    */
   public void setValidateDTD(boolean flag)
   {
      this.validateDTD = flag;
   }

   /** Get the XML based configuration given the Configuration it should
    delegate to when an application cannot be found.
    */
   public Configuration getConfiguration(Configuration prevConfig)
   {
      parentConfig = prevConfig;
      return this;
   }

   /** Add an application configuration
    */
   public void addAppConfig(String appName, AppConfigurationEntry[] entries)
   {
      AuthenticationInfo authInfo = new AuthenticationInfo();
      authInfo.setAppConfigurationEntry(entries);
      appConfigs.put(appName, authInfo);
   }

   public void removeAppConfig(String appName)
   {
      appConfigs.remove(appName);
   }

   /** Display the login configuration for the given application.
    */
   public String displayAppConfig(String appName)
   {
      StringBuffer buffer = new StringBuffer("<h2>"+appName+" LoginConfiguration</h2>\n");
      AppConfigurationEntry[] config = getAppConfigurationEntry(appName);
      if( config == null )
         buffer.append("No Entry\n");
      else
      {
         for(int c = 0; c < config.length; c ++)
         {
            AppConfigurationEntry entry = config[c];
            buffer.append("LoginModule Class: "+entry.getLoginModuleName());
            buffer.append("\n<br>ControlFlag: "+entry.getControlFlag());
            buffer.append("\n<br>Options:<ul>");
            Map options = entry.getOptions();
            Iterator iter = options.entrySet().iterator();
            while( iter.hasNext() )
            {
               Entry e = (Entry) iter.next();
               buffer.append("<li>");
               buffer.append("name="+e.getKey());
               buffer.append(", value="+e.getValue());
               buffer.append("</li>\n");
            }
            buffer.append("</ul>\n");
         }
      }
      return buffer.toString();
   }
// --- End XMLLoginConfigMBean interface methods

// --- Begin ServiceMBean interface methods
   /**
    * create the service, do expensive operations etc
    */
   public void create() throws Exception
   {
      
   }
   
   public String getName()
   {
      return "XMLLoginConfig";
   }
   
   public int getState()
   {
      return state;
   }
   
   public String getStateString()
   {
      return XMLLoginConfigMBean.states[state];
   }
   
   /**
    * start the service, create is already called
    */
   public void start() throws Exception
   {
      if (getState() != XMLLoginConfigMBean.STOPPED && getState() != XMLLoginConfigMBean.FAILED)
         return;
      
      state = XMLLoginConfigMBean.STARTING;
      loadConfig(xmlConfig);
      state = XMLLoginConfigMBean.STARTED;
   }
   
   /**
    * stop the service
    */
   public void stop()
   {
      state = XMLLoginConfigMBean.STOPPING;
      state = XMLLoginConfigMBean.STOPPED;
   }
   
   /**
    * destroy the service, tear down
    */
   public void destroy()
   {
      appConfigs.clear();
   }

// --- End ServiceMBean interface methods
   
   private void loadConfig(URL xmlConfig) throws Exception
   {
      log.debug("Loading xmlConfig="+xmlConfig);
      Document doc = loadURL(xmlConfig);
      Element root = doc.getDocumentElement();
      NodeList apps = root.getElementsByTagName("application-policy");
      for(int n = 0; n < apps.getLength(); n ++)
      {
         Element appPolicy = (Element) apps.item(n);
         String appName = appPolicy.getAttribute("name");
         log.trace("Parsing application-policy="+appName);

         try
         {
            AuthenticationInfo authInfo = parseAuthentication(appPolicy);
            if( authInfo != null )
               appConfigs.put(appName, authInfo);
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }
   
   private Document loadURL(URL xmlConfig) throws Exception
   {
      InputStream is = xmlConfig.openStream();
      if( is == null )
         throw new IOException("Failed to obtain InputStream from url: "+xmlConfig);
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setValidating(validateDTD);
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      EntityResolver resolver = new LocalResolver(log);
      docBuilder.setEntityResolver(resolver);
      Document doc = docBuilder.parse(is);
      return doc;
   }
   
   /** Parse the application-policy/authentication element
    @param policy, the application-policy/authentication element
    */
   private AuthenticationInfo parseAuthentication(Element policy) throws Exception
   {
      // Parse the permissions
      NodeList authentication = policy.getElementsByTagName("authentication");
      if( authentication.getLength() == 0 )
      {
         return null;
      }

      Element auth = (Element) authentication.item(0);
      NodeList modules = auth.getElementsByTagName("login-module");
      ArrayList tmp = new ArrayList();
      for(int n = 0; n < modules.getLength(); n ++)
      {
         Element module = (Element) modules.item(n);
         parseModule(module, tmp);
      }
      AppConfigurationEntry[] entries = new AppConfigurationEntry[tmp.size()];
      tmp.toArray(entries);
      AuthenticationInfo info = new AuthenticationInfo();
      info.setAppConfigurationEntry(entries);
      return info;
   }
   private void parseModule(Element module, ArrayList entries) throws Exception
   {
      LoginModuleControlFlag controlFlag = LoginModuleControlFlag.REQUIRED;
      String className = module.getAttribute("code");
      String flag = module.getAttribute("flag");
      if( flag != null )
      {
         if( LoginModuleControlFlag.REQUIRED.toString().indexOf(flag) > 0 )
            controlFlag = LoginModuleControlFlag.REQUIRED;
         else if( LoginModuleControlFlag.REQUISITE.toString().indexOf(flag) > 0 )
            controlFlag = LoginModuleControlFlag.REQUISITE;
         else if( LoginModuleControlFlag.SUFFICIENT.toString().indexOf(flag) > 0 )
            controlFlag = LoginModuleControlFlag.SUFFICIENT;
         else if( LoginModuleControlFlag.OPTIONAL.toString().indexOf(flag) > 0 )
            controlFlag = LoginModuleControlFlag.OPTIONAL;
      }
      NodeList opts = module.getElementsByTagName("module-option");
      HashMap options = new HashMap();
      for(int n = 0; n < opts.getLength(); n ++)
      {
         Element opt = (Element) opts.item(n);
         String name = opt.getAttribute("name");
         String value = getContent(opt, "");
         options.put(name, value);
      }
      AppConfigurationEntry entry = new AppConfigurationEntry(className, controlFlag, options);
      entries.add(entry);
   }
   
   private static String getContent(Element element, String defaultContent)
   {
      NodeList children = element.getChildNodes();
      String content = defaultContent;
      if( children.getLength() > 0 )
      {
         content = "";
         for(int n = 0; n < children.getLength(); n ++)
         {
            if( children.item(n).getNodeType() == Node.TEXT_NODE ||
            children.item(n).getNodeType() == Node.CDATA_SECTION_NODE )
               content += children.item(n).getNodeValue();
            else
               content += children.item(n).getFirstChild();
         }
         return content.trim();
      }
      return content;
   }

   /** Local entity resolver to handle the security-policy DTD public id.
    */
   private static class LocalResolver implements EntityResolver
   {
      private static final String LOGIN_CIONFIG_PUBLIC_ID = "-//JBoss//DTD JBOSS Security Config 3.0//EN";
      private static final String LOGIN_CIONFIG_DTD_NAME = "/org/jboss/metadata/security_config.dtd";
      private Logger log;

      LocalResolver(Logger log)
      {
         this.log = log;
      }

      public InputSource resolveEntity(String publicId, String systemId)
      {
         InputSource is = null;
         if( publicId.equals(LOGIN_CIONFIG_PUBLIC_ID) )
         {
            try
            {
               InputStream dtdStream = getClass().getResourceAsStream(LOGIN_CIONFIG_DTD_NAME);
               is = new InputSource(dtdStream);
            }
            catch(Exception ex)
            {
               log.warn("Failed to resolve DTD publicId: "+publicId);
            }
         }
         return is;
      }
   }
}
