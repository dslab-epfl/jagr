/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: JBossAuthenticationHandler.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.axis.server;

import org.apache.axis.AxisFault;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.MessageContext;

import org.jboss.security.SimplePrincipal;
import org.jboss.security.NobodyPrincipal;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SubjectSecurityManager;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.security.Principal;
import javax.security.auth.Subject;

/**
 * AuthenticationHandler that interacts with a given JBoss autentication
 * manager via default simple principals and passchars from the HTTP Basic Authentication. 
 * Derived from org.apache.axis.handlers.SimpleAuthenticationHandler.
 * Note that this is somehow redundant to the WebContainer security, but we want
 * to be able to install different such handlers for different 
 * web servcies behind a single entry-point.
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * <li> jung, 15.03.2002: Added security domain option. </li>
 * </ul>
 * <br>
 * <h3>To Do</h3>
 * <ul>
 * <li> jung, 14.03.2002: Cache simple principals. Principal factory for
 * interacting with various security domains.
 * </ul>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @created 14.03.2002
 * @version $Revision: 1.1.1.1 $
 */

public class JBossAuthenticationHandler extends BasicHandler {

   //
   // Attributes
   //

   /** whether this handler has been initialized already */
   protected boolean isInitialised;

   /** 
    * this is the authentication manager that is responsible for our security domain 
    * if that is null, this authenticationhandler will block any call, rather deactivate
    * the handler, then, or run against a NullSecurityManager
    */
   protected SubjectSecurityManager authMgr;

   //
   // Constructors
   //

   /** default, all options are set afterwards */
   public JBossAuthenticationHandler() {
   }

   //
   // Protected helpers
   //

   /** 
    * initialize this authenticationhandler lazy, after the options have been
    * set.
    */
   protected void initialise() throws AxisFault {
      isInitialised = true;
      authMgr=null;
      String securityDomain = (String) getOption(Constants.SECURITY_DOMAIN_OPTION);
      if (securityDomain != null) {
         try {
            // bind against the jboss security subsystem
            authMgr =
               (SubjectSecurityManager) new InitialContext().lookup(securityDomain);
         } catch (NamingException e) {
            throw new AxisFault(
               "Could not lookup associated security domain " + securityDomain,
               e);
         }
      }
   }

   /** 
    * creates a new principal belonging to the given username,
    * override to adapt to specific security domains.
    */
   protected Principal getPrincipal(String userName) {
      if (userName == null) {
         return NobodyPrincipal.NOBODY_PRINCIPAL;
      } else {
         return new SimplePrincipal(userName);
      }
   }

   /** validates the given principal with the given password */
   protected void validate(Principal userPrincipal, String passwd) throws AxisFault {
      // build passchars
      char[] passChars = passwd != null ? passwd.toCharArray() : null;
      // have to use pointer comparison here, but it´s a singleton, right?
      if (userPrincipal != NobodyPrincipal.NOBODY_PRINCIPAL
         && !authMgr.isValid(userPrincipal, passChars)) {
         throw new AxisFault("Could not authenticate user " + userPrincipal.getName());
      }
   }

   /** associates the call context with the given info */
   protected Subject associate(Principal userPrincipal, String passwd) {
      // pointer comparison, again	      
      if (userPrincipal != NobodyPrincipal.NOBODY_PRINCIPAL) {
         SecurityAssociation.setPrincipal(userPrincipal);
         SecurityAssociation.setCredential(passwd.toCharArray());
      } else {
         // Jboss security does not like nobody:null
         SecurityAssociation.setPrincipal(null);
         SecurityAssociation.setCredential(null);
      }
      return authMgr.getActiveSubject();
   }

   //
   // API
   //

   /**
    * Authenticate the user and password from the msgContext. Note that
    * we do not disassociate the subject here, since that would have
    * to be done by a separate handler in the response chain and we
    * currently expect Jetty or the WebContainer to do that for us
    */

   public void invoke(MessageContext msgContext) throws AxisFault {

      // double check does not work on multiple processors, unfortunately
      if (!isInitialised) {
         synchronized (this) {
            if (!isInitialised) {
               initialise();
            }
         }
      }

      if (authMgr == null) {
         throw new AxisFault("No security domain associated.");
      }

      // we take the id out of the        
      String userID = msgContext.getUsername();
      // convert into a principal
      Principal userPrincipal = getPrincipal(userID);
      // the password that has been provided
      String passwd = msgContext.getPassword();
      // validate the user
      validate(userPrincipal, passwd);
      // associate the context 
      Subject subject = associate(userPrincipal, passwd);
      // with the security subject
      msgContext.setProperty(MessageContext.AUTHUSER, subject);
   }

}