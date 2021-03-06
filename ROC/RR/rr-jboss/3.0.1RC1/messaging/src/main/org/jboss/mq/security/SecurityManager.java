/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.mq.security;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.security.Principal;
import java.security.acl.Group;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.InvalidAttributeValueException;
 
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityPolicy;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;

import org.jboss.system.ServiceMBeanSupport;

import org.jboss.mq.ConnectionToken;
import org.jboss.mq.server.JMSServerInterceptor;
import org.jboss.mq.server.jmx.InterceptorMBeanSupport;
/**
 * A JAAS based security manager for JBossMQ.
 *
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @version $Revision: 1.1.1.1 $
 */

public class SecurityManager 
   extends InterceptorMBeanSupport
           implements SecurityManagerMBean
{

   /**
    * Cached info on subject, to speed lookups.
    */
   class SubjectInfo {
      Subject subject;
      Principal principal;
      Group roles;

      public String toString() {
         return "SubjectInfo {subject="+subject+";principal="+principal+";roles="+roles.toString();
      }
   }

   private ObjectName name;
   String securityDomainName  = "java:/jaas/jbossmq";
   Context securityCtx;
   HashMap authCache = new HashMap(32);
   HashMap securityConf = new HashMap(32);
   ServerSecurityInterceptor interceptor;
   SubjectSecurityManager sec;//FIXME
   SessionIDGenerator idGenerator;

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {

      this.name = name == null ? OBJECT_NAME : name;
      
      return this.name;
   }

   public JMSServerInterceptor getInvoker() {
      return interceptor;
   }

   // DEBUGGING METHOD: DANGEROUS
   public String printAuthCache() {
      return authCache.toString();
   }

   public void addDestination(String destName, org.w3c.dom.Element conf) throws Exception {
      SecurityMetadata m = new SecurityMetadata(conf);
      securityConf.put(destName,m);
   }
   public void addDestination(String destName, String conf) throws Exception {
      SecurityMetadata m = new SecurityMetadata(conf);
      securityConf.put(destName,m);
   }

   public void removeDestination(String destName) throws Exception {
      securityConf.remove(destName);
   }

   public SecurityMetadata getSecurityMetadata(String destName) {
      SecurityMetadata m = (SecurityMetadata)securityConf.get(destName);
      if (m == null) {
         // No SecurityManager was configured for the dest,
         // default to guest
         log.warn("No SecurityMetadadata was available for " + destName + " adding default security conf");
         m = new SecurityMetadata();
         securityConf.put(destName,m);
      }
      return m;
   }
   
   public void startService() throws Exception {
      // Get the JBoss security manager from the ENC context
      try
      {
         InitialContext iniCtx = new InitialContext();
         //securityCtx = (Context) iniCtx.lookup(securityDomainName);
         // FIXME, should we really lookit up here
         sec = (SubjectSecurityManager)iniCtx.lookup(securityDomainName);
      }
      catch(NamingException e)
      {
         // Apparently there is no security context?
      }
      interceptor = new ServerSecurityInterceptor(this);

      idGenerator = new SessionIDGenerator();
      
      super.startService();
   }
   
   public  void stopService() throws Exception
   {
      // Anything to do here?
   }
   
   public String authenticate(String user, String password) throws JMSException {
      /*
      try {
         o = securityCtx.lookup("securityMgr");
      }catch(NamingException ex) {
         throw new JMSException("Could not get a security context");
      }
      */
      boolean trace = log.isTraceEnabled();
      if (sec instanceof SubjectSecurityManager) {
         SubjectSecurityManager securityMgr =(SubjectSecurityManager)sec;
         SimplePrincipal principal = new SimplePrincipal(user);
         char[] passwordChars = null;
         if( password != null )
            passwordChars = password.toCharArray();
         if( securityMgr.isValid(principal, passwordChars) )
         {
            if (trace)
               log.trace("Username: "+user+" is authenticated");
/* Wrong. The security manager is only responsible for authentication.
It does not know enough to associate this security information with the
calling thread.
            // Put it in the assoc, so that the manager
            // login callback can get them from it
            SecurityAssociation.setPrincipal(principal);
            SecurityAssociation.setCredential(passwordChars);
*/
            
            Subject subject = securityMgr.getActiveSubject();
            String sessionId = generateId(subject);
            addId(sessionId,subject, principal);

            // Should we log it out since we do not use manager any more?
            return sessionId;
         }
         else
         {
            if (trace)
               log.trace("User: "+user+" is NOT authenticated");
            throw new JMSSecurityException("User: "+user+" is NOT authenticated");
         }
         
      } else {
         throw new IllegalStateException("SecurityManager only works with a SubjectSecurityManager");
      }
   }
   

   public boolean authorize(ConnectionToken token, Set rolePrincipals) throws JMSException 
   {
      //Unfortunately we can not reliably use the securityManager and its
      // subject, since can not guarantee that every connection is 
      // connected to a unique thread.
      // For now we implement the RealmMapping our self
      boolean trace = log.isTraceEnabled();
      boolean hasRole = false;
      
      SubjectInfo info = ( SubjectInfo)authCache.get(token.getSessionId());
      if (info == null)
         throw new JMSSecurityException("User session is not valid");
      
      if (trace)
         log.trace("Checking authorize on subjectInfo: " + info.toString() + " for rolePrincipals " + rolePrincipals.toString());
      
      Group group = info.roles;
      if( group != null )
      {
         Iterator iter = rolePrincipals.iterator();
         while( hasRole == false && iter.hasNext() )
         {
            Principal role = (Principal) iter.next();
            hasRole = group.isMember(role);
         }
         
      }
      return hasRole;
   }

   // Is this a security problem? May a bad user set this manually and log out other users?
   public void logout(ConnectionToken token) {
      if (token == null)
         return;// Not much we can do
      // FIXME - how do we clear the thread local in security manager?
      removeId(token.getSessionId());
   }


   private void addId(String id, Subject subject, Principal callerPrincipal) {
      boolean trace = log.isTraceEnabled();

      SubjectInfo info = new SubjectInfo();
      info.subject = subject;
      info.principal = callerPrincipal;

      Set subjectGroups = subject.getPrincipals(Group.class);
      Iterator iter = subjectGroups.iterator();
      while( iter.hasNext() )
      {
         Group grp = (Group) iter.next();
         String name = grp.getName();
         if( name.equals("CallerPrincipal") )
         {
            Enumeration members = grp.members();
            if( members.hasMoreElements() )
               info.principal = (Principal) members.nextElement();
         }
         else if( name.equals("Roles") ) {
            if (trace)
               log.trace("Adding group : " + grp.getClass() + " " + grp.toString());
            info.roles = grp;
         }
      }
      /* Handle null principals with no callerPrincipal. This is an indication
         of an user that has not provided any authentication info, but
         has been authenticated by the domain login module stack. Here we look
         for the first non-Group Principal and use that.
      */
      if( callerPrincipal == null && info.principal == null )
      {
         Set subjectPrincipals = subject.getPrincipals(Principal.class);
         iter = subjectPrincipals.iterator();
         while( iter.hasNext() )
         {
            Principal p = (Principal) iter.next();
            if( (p instanceof Group) == false )
               info.principal = p;
         }
      }
      
      synchronized(authCache) {
         authCache.put(id,info);
      }
   }
   
   private void removeId(String id) {
      synchronized(authCache) {
         authCache.remove(id);
      }
   }
   
   private String generateId(Subject subject) throws JMSException {
      try {
         return idGenerator.nextSessionId();
      }catch(Exception ex) {
         log.error("Could not generate a secure sessionID", ex);
         //Dont  show client the real reason
         throw new JMSSecurityException("Could not generate a secure sessionID");
      }
   }

   
   
   /**
    * @see InterceptorMBean#getInterceptor()
    */
   public JMSServerInterceptor getInterceptor()
   {
      return interceptor;
   }

} // SecurityManager
