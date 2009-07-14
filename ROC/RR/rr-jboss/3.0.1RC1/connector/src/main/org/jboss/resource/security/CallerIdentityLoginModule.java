/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.resource.security;

import java.security.acl.Group;
import java.security.Principal;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.AbstractServerLoginModule;
import org.jboss.security.SecurityAssociation;
import org.jboss.logging.Logger;
import org.jboss.util.jmx.MBeanServerLocator;

/**
 * A simple login module that simply associates the principal making the
 * connection request with the actual EIS connection requirements.
 *
 * The type of Principal class used is
 * <code>org.jboss.security.SimplePrincipal.</code>
 * <p>
 *
 * @see org.jboss.resource.security.ConfiguredIdentityLoginModule
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:dan.bunker@pbs.proquest.com">Dan Bunker</a>
 * @version $Revision: 1.1.1.1 $
 */
public class CallerIdentityLoginModule 
   extends AbstractPasswordCredentialLoginModule
{
   /**
    * The default username/principal to use for basic connections
    */
   private String userName;

   /**
    * The default password to use for basic connections
    */
   private String password;


   /**
    * Class logger
    */
   private static final Logger log = Logger.getLogger(CallerIdentityLoginModule.class);

   /**
    * Default Constructor
    */
   public CallerIdentityLoginModule(){
   }

   /**
    * The initialize method sets up some default connection information for
    * basic connections.  This is useful for container initialization connection
    * use or running the application in a non-secure manner.  This method is
    * called before the login method.
    *
    * @param subject
    * @param handler
    * @param sharedState
    * @param options
    */
   public void initialize(Subject subject, CallbackHandler handler, Map sharedState, Map options)
   {

      super.initialize(subject, handler, sharedState, options);

      userName = (String) options.get("userName");

      if( userName == null )
      {
         log.info("No default username supplied.");
      }

      password = (String) options.get("password");

      if( password == null )
      {
         log.info("No default password supplied.");
      }


      log.info("got default principal: " + userName + ", username: " + userName + ", password: " + password);

   }

   /**
    * Performs the login association between the caller and the resource for a
    * 1 to 1 mapping.  This acts as a login propagation strategy and is useful
    * for single-sign on requirements
    *
    * @return True if authentication succeeds
    * @throws LoginException
    */
   public boolean login() throws LoginException
   {
      log.trace("Caller Association login called");

      //setup to use the default connection info.  This will be overided if security
      //associations are found
      String username = userName;
      char [] pass = password != null ? password.toCharArray() : new char[0];

      //ask the security association class for the principal info making this request
      try 
      {
          Principal user = SecurityAssociation.getPrincipal();
          Object o = SecurityAssociation.getCredential();

          if(o != null)
          {
            pass = (char [])o;
          }

          if(user != null)
          {
             username = user.getName();
             if (log.isTraceEnabled()) 
             {
                log.trace("Current Calling principal is: " + username + " ThreadName: " + Thread.currentThread().getName());
             } // end of if ()
            
          }
      }
      catch(Throwable e)
      {
          throw new LoginException("Unable to get the calling principal or its credentials for resource association");
      }

      if( super.login() == true )
      {
         return true;
      }
      Principal principal = new SimplePrincipal(username);
      subject.getPrincipals().add(principal);

      // Put the principal name into the sharedState map
      sharedState.put("javax.security.auth.login.name", username);
      PasswordCredential cred = new PasswordCredential(username, pass);
      cred.setManagedConnectionFactory(getMcf());
      subject.getPrivateCredentials().add(cred);
      super.loginOk = true;

      return true;
   }

   protected Principal getIdentity()
   {
      log.trace("getIdentity called");
      Principal principal = new SimplePrincipal(userName);
      return principal;
   }


   protected Group[] getRoleSets() throws LoginException
   {
      log.trace("getRoleSets called");
      return new Group[] {};
   }
}
