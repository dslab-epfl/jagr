/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: JBossUserRealm.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $

package org.jboss.jetty.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserPrincipal;
import org.mortbay.http.UserRealm;

/** An implementation of UserRealm that integrates with the JBossSX
 * security manager associted with the web application.
 * @author  Scott_Stark@displayscape.com
 * @version $Revision: 1.1.1.1 $
 */

public class JBossUserRealm
  implements UserRealm		// Jetty API
{
  private final Logger                 _log;
  private final String                 _realmName;
  private final String                 _subjAttrName;
  private final boolean                _useJAAS;
  private final HashMap                _users        =new HashMap();
  private       AuthenticationManager  _authMgr      =null;
  private       RealmMapping           _realmMapping =null;
  private       SubjectSecurityManager _subjSecMgr   =null;

  class JBossUserPrincipal
    implements UserPrincipal	// Jetty API
  {
    private final SimplePrincipal _principal;	// JBoss API
    private String                _password;

    JBossUserPrincipal(String name)
    {
      _principal=new SimplePrincipal(name);

      if (_log.isDebugEnabled())
	_log.debug("created JBossUserRealm::JBossUserPrincipal: "+name);
    }

    private boolean
      isAuthenticated(String password)
    {
      boolean authenticated = false;

      if (password==null)
	password="";

      char[] passwordChars = password.toCharArray();
      if (_log.isDebugEnabled())
	_log.debug("authenticating: Name:"+_principal+" Password:****"/*+password*/);
      if(_authMgr!=null &&_authMgr.isValid(this, passwordChars))
      {
	if (_log.isDebugEnabled())
	  _log.debug("authenticated: "+_principal);

	// work around the fact that we are not serialisable - thanks Anatoly
	//	SecurityAssociation.setPrincipal(this);
	SecurityAssociation.setPrincipal(_principal);

	SecurityAssociation.setCredential(passwordChars);
	authenticated=true;
      }
      else
      {
	_log.warn("authentication failure: "+_principal);
      }

      return authenticated;
    }

    public boolean
      equals(Object o)
    {
      if (o==this)
	return true;

      if (o==null)
	return false;

      if (getClass()!=o.getClass())
	return false;

      String myName  =this.getName();
      String yourName=((JBossUserPrincipal)o).getName();

      if (myName==null && yourName==null)
	return true;

      if (myName!=null && myName.equals(yourName))
	return true;

      return false;
    }

    //----------------------------------------
    // SimplePrincipal - for JBoss

    public String
      getName()
    {
      return _principal.getName();
    }

    //----------------------------------------
    // UserPrincipal - for Jetty

    public boolean
      authenticate(String password, HttpRequest request)
    {
      _password=password;
      boolean authenticated=false;
      authenticated=isAuthenticated(_password);

      // This doesn't mean anything to Jetty - but may to some
      // Servlets - confirm later...
      if (_useJAAS && authenticated && _subjSecMgr!=null)
      {
	Subject subject = _subjSecMgr.getActiveSubject();
	if (_log.isDebugEnabled())
	  _log.debug("setting JAAS subjectAttributeName("+_subjAttrName+") : "+subject);
	request.setAttribute(_subjAttrName, subject);
      }

      return authenticated;
    }

    public boolean
      isAuthenticated()
    {
      return isAuthenticated(_password);
    }

    private UserRealm
      getUserRealm()
    {
      return JBossUserRealm.this;
    }

    public boolean
      isUserInRole(String role)
    {
      boolean isUserInRole = false;

      Set requiredRoles = Collections.singleton(new SimplePrincipal(role));
      if(_realmMapping!=null && _realmMapping.doesUserHaveRole(this, requiredRoles))
      {
	if (_log.isDebugEnabled())
	  _log.debug("JBossUserPrincipal: "+_principal+" is in Role: "+role);

	isUserInRole = true;
      }
      else
      {
	if (_log.isDebugEnabled())
	  _log.debug("JBossUserPrincipal: "+_principal+" is NOT in Role: "+role);
      }

      return isUserInRole;
    }

    public String
      toString()
    {
      return getName();
    }
  }

  public
    JBossUserRealm(String realmName, String subjAttrName)
  {
    _realmName    = realmName;
    _log          = Logger.getLogger(JBossUserRealm.class.getName() + "#" + _realmName);
    _subjAttrName = subjAttrName;
    _useJAAS      = (_subjAttrName!=null);
  }

  public void
    init()
  {
    _log.debug("initialising...");
    try
    {
      // can I get away with just doing this lookup once per webapp ?
      InitialContext iniCtx = new InitialContext();
      // do we need the 'java:comp/env' prefix ? TODO
      Context securityCtx  =(Context) iniCtx.lookup("java:comp/env/security");
      _authMgr      =(AuthenticationManager) securityCtx.lookup("securityMgr");
      _realmMapping =(RealmMapping)          securityCtx.lookup("realmMapping");
      iniCtx=null;

      if (_authMgr instanceof SubjectSecurityManager)
	_subjSecMgr = (SubjectSecurityManager) _authMgr;
    }
    catch (NamingException e)
    {
      _log.error("java:comp/env/security does not appear to be correctly set up", e);
    }
    _log.debug("...initialised");
  }

  // this is going to cause contention - TODO
  private synchronized JBossUserPrincipal
    ensureUser(String userName)
  {
    JBossUserPrincipal user = (JBossUserPrincipal)_users.get(userName);

    if (user==null)
    {
      user=new JBossUserPrincipal(userName);
      _users.put(userName, user);
    }

    return user;
  }

  public UserPrincipal
    authenticate(String userName, Object credential, HttpRequest request)
  {
    if (_log.isDebugEnabled())
      _log.debug("JBossUserPrincipal: "+userName);

    // until we get DigestAuthentication sorted JBoss side...
    String password=null;
    if (credential instanceof java.lang.String)
      password=(String)credential;
    else
    {
      _log.warn("digest authentication NYI - credential MUST be a String");
      return null;
    }

    JBossUserPrincipal user = ensureUser(userName);
    if (user.authenticate(password,request))
      return user;
    return null;
  }

  public void disassociate(UserPrincipal user)
  {
    SecurityAssociation.setPrincipal(null);
    SecurityAssociation.setCredential(null);
  }

  public UserPrincipal pushRole(UserPrincipal user, String role)
  {
    // Not implemented.
    // need to return a new user with the role added.
    return user;
  }

  public UserPrincipal popRole(UserPrincipal user)
  {
    // Not implemented
    // need to return the original user with any new role associations
    // removed from this thread.
    return user;
  }

  public String
    getName()
  {
    return _realmName;
  }
}
