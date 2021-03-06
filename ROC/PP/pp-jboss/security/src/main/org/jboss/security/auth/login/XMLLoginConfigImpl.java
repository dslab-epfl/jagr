package org.jboss.security.auth.login;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.AuthPermission;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

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
public class XMLLoginConfigImpl extends Configuration
{
   private static final String DEFAULT_APP_CONFIG_NAME = "other";
   private static final AuthPermission REFRESH_PERM = new AuthPermission("refreshPolicy");
   private static Logger log = Logger.getLogger(XMLLoginConfigImpl.class);
   /** A mapping of application name to AppConfigurationEntry[] */
   protected HashMap appConfigs = new HashMap();
   /** The URL to the XML or Sun login configuration */
   protected URL loginConfigURL;
   /** The inherited configuration we delegate to */
   protected Configuration parentConfig;
   /** A flag indicating if XML configs should be validated */
   private boolean validateDTD = true;

   // --- Begin Configuration method overrrides
   public void refresh()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(REFRESH_PERM);
      appConfigs.clear();
      loadConfig();
   }

   public AppConfigurationEntry[] getAppConfigurationEntry(String appName)
   {
      // If the config has not been loaded try to do so
      if( loginConfigURL == null )
      {
         loadConfig();
      }

      AppConfigurationEntry[] entry = null;
      AuthenticationInfo authInfo = (AuthenticationInfo) appConfigs.get(appName);
      if( authInfo == null )
      {
         if( parentConfig != null )
            entry = parentConfig.getAppConfigurationEntry(appName);
         if( entry == null )
            authInfo = (AuthenticationInfo) appConfigs.get(DEFAULT_APP_CONFIG_NAME);
      }

      if( authInfo != null )
      {
         if( log.isTraceEnabled() )
            log.trace("getAppConfigurationEntry, authInfo="+authInfo);
         // Make a copy of the authInfo object
         final AuthenticationInfo theAuthInfo = authInfo;
         PrivilegedAction action = new PrivilegedAction()
         {
            public Object run()
            {
               return theAuthInfo.copyAppConfigurationEntry();
            }
         };
         entry = (AppConfigurationEntry[]) AccessController.doPrivileged(action);
      }

      return entry;
   }
   // --- End Configuration method overrrides

   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public URL getConfigURL()
   {
      return loginConfigURL;
   }

   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public void setConfigURL(URL loginConfigURL)
   {
      this.loginConfigURL = loginConfigURL;
   }

   public void setConfigResource(String resourceName)
      throws IOException
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      loginConfigURL = tcl.getResource(resourceName);
      if( loginConfigURL == null )
         throw new IOException("Failed to find resource: "+resourceName);
   }

   public void setParentConfig(Configuration parentConfig)
   {
      this.parentConfig = parentConfig;
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

   public void clear()
   {

   }

   /** Called to try to load the config from the java.security.auth.login.config
    * property value when there is no loginConfigURL.
    */
   public void loadConfig()
   {
      // Try to load the java.security.auth.login.config property
      String loginConfig = System.getProperty("java.security.auth.login.config");
      if( loginConfig == null )
         loginConfig = "login-config.xml";

      // If there is no loginConfigURL build it from the loginConfig
      if( loginConfigURL == null )
      {
         try
         {
            // Try as a URL
            loginConfigURL = new URL(loginConfig);
         }
         catch(MalformedURLException e)
         {
            // Try as a resource
            try
            {
               setConfigResource(loginConfig);
            }
            catch(IOException ignore)
            {
               // Try as a file
               File configFile = new File(loginConfig);
               try
               {
                  setConfigURL(configFile.toURL());
               }
               catch(MalformedURLException ignore2)
               {
               }
            }
         }
      }

      if( loginConfigURL == null )
      {
         log.warn("Failed to find config: "+loginConfig);
         return;
      }

      // Try to load the config if found
      try
      {
         loadConfig(loginConfigURL);
      }
      catch(Exception e)
      {
         log.warn("Failed to load config: "+loginConfigURL, e);
      }
   }

   protected void loadConfig(URL config) throws Exception
   {
      log.debug("Try loading config as XML, url="+config);
      try
      {
         loadXMLConfig(config);
      }
      catch(Exception e)
      {
         log.trace("Failed to load config as XML", e);
         log.debug("Try loading config as Sun format, url="+config);
         loadSunConfig(config);
      }
   }

   private void loadSunConfig(URL sunConfig) throws Exception
   {
      InputStream is = sunConfig.openStream();
      if( is == null )
         throw new IOException("InputStream is null for: "+sunConfig);

      InputStreamReader configFile = new InputStreamReader(is);
      boolean trace = log.isTraceEnabled();
      SunConfigParser.doParse(configFile, this, trace);
   }

   private void loadXMLConfig(URL loginConfigURL) throws Exception
   {
      Document doc = loadURL(loginConfigURL);
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

   private Document loadURL(URL configURL) throws Exception
   {
      InputStream is = configURL.openStream();
      if( is == null )
         throw new IOException("Failed to obtain InputStream from url: "+configURL);

      // Get the xml DOM parser
      PrivilegedExceptionAction action = new PrivilegedExceptionAction()
      {
         public Object run() throws FactoryConfigurationError
         {
            return DocumentBuilderFactory.newInstance();
         }
      };

      DocumentBuilderFactory docBuilderFactory = null;
      try
      {
         docBuilderFactory = (DocumentBuilderFactory) AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException e)
      {
         throw e.getException();
      }

      docBuilderFactory.setValidating(validateDTD);
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      EntityResolver resolver = new LocalResolver(log);
      docBuilder.setEntityResolver(resolver);
      Document doc = docBuilder.parse(is);
      return doc;
   }

   /** Parse the application-policy/authentication element
    @param policy , the application-policy/authentication element
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
      AppConfigurationEntry.LoginModuleControlFlag controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
      String className = module.getAttribute("code");
      String flag = module.getAttribute("flag");
      if( flag != null )
      {
         if( AppConfigurationEntry.LoginModuleControlFlag.REQUIRED.toString().indexOf(flag) > 0 )
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
         else if( AppConfigurationEntry.LoginModuleControlFlag.REQUISITE.toString().indexOf(flag) > 0 )
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
         else if( AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT.toString().indexOf(flag) > 0 )
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
         else if( AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL.toString().indexOf(flag) > 0 )
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
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
