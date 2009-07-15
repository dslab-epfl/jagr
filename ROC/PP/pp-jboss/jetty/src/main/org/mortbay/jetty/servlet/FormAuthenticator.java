// ===========================================================================
// Copyright (c) 1996-2002 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: FormAuthenticator.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ---------------------------------------------------------------------------

package org.mortbay.jetty.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.SecurityConstraint.Authenticator;
import org.mortbay.http.UserPrincipal;
import org.mortbay.http.UserRealm;
import org.mortbay.util.Code;
import org.mortbay.util.URI;

/* ------------------------------------------------------------ */
/** 
 * @version $Id: FormAuthenticator.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
 * @author Greg Wilkins (gregw)
 */
public class FormAuthenticator implements Authenticator
{
    /* ------------------------------------------------------------ */
    public final static String __J_URI="org.mortbay.jetty.URI";
    public final static String __J_AUTHENTICATED="org.mortbay.jetty.Auth";
    public final static String __J_SECURITY_CHECK="j_security_check";
    public final static String __J_USERNAME="j_username";
    public final static String __J_PASSWORD="j_password";

    private String _formErrorPage;
    private String _formLoginPage;
    
    /* ------------------------------------------------------------ */
    public String getAuthMethod()
    {
        return SecurityConstraint.__FORM_AUTH;
    }

    /* ------------------------------------------------------------ */
    public void setLoginPage(String path)
    {
        if (!path.startsWith("/"))
        {
            Code.warning("form-login-page must start with /");
            path="/"+path;
        }
        _formLoginPage=path;
    }

    /* ------------------------------------------------------------ */
    public String getLoginPage()
    {
        return _formLoginPage;
    }
    
    /* ------------------------------------------------------------ */
    public void setErrorPage(String path)
    {
        if (!path.startsWith("/"))
        {
            Code.warning("form-error-page must start with /");
            path="/"+path;
        }
        _formErrorPage=path;
    }

    /* ------------------------------------------------------------ */
    public String getErrorPage()
    {
        return _formErrorPage;
    }
    
    /* ------------------------------------------------------------ */
    /** Perform form authentication.
     * Called from SecurityHandler.
     * @return UserPrincipal if authenticated else null.
     */
    public UserPrincipal authenticated(UserRealm realm,
                                       String pathInContext,
                                       HttpRequest httpRequest,
                                       HttpResponse httpResponse)
        throws IOException
    {
        HttpServletRequest request =(ServletHttpRequest)httpRequest.getWrapper();
        HttpServletResponse response =(HttpServletResponse) httpResponse.getWrapper();

        // Handle paths
        String uri = pathInContext;

        // Setup session 
        HttpSession session=request.getSession(true);
        
        // Handle a request for authentication.
        if ( uri.substring(uri.lastIndexOf("/")+1).startsWith(__J_SECURITY_CHECK) )
        {
            // Check the session object for login info. 
            String username = request.getParameter(__J_USERNAME);
            String password = request.getParameter(__J_PASSWORD);
            
            UserPrincipal user = realm.authenticate(username,password,httpRequest);
            String nuri=(String)session.getAttribute(__J_URI);
            if (user!=null && nuri!=null)
            {
                Code.debug("Form authentication OK for ",username);
                httpRequest.setAuthType(SecurityConstraint.__FORM_AUTH);
                httpRequest.setAuthUser(username);
                httpRequest.setUserPrincipal(user);
                session.setAttribute(__J_AUTHENTICATED,user);
                response.sendRedirect(response.encodeRedirectURL(nuri));
            }
            else
            {
                Code.debug("Form authentication FAILED for ",username);
                if (_formErrorPage!=null)
                    response.sendRedirect(response.encodeRedirectURL
                                          (URI.addPaths(request.getContextPath(),
                                                        _formErrorPage)));
                else
                    response.sendError(HttpResponse.__403_Forbidden);
            }
            
            // Security check is always false, only true after final redirection.
            return null;
        }

        // Check if the session is already authenticated.
        UserPrincipal user = (UserPrincipal) session.getAttribute(__J_AUTHENTICATED);
        if (user != null)
        {
            if (user.isAuthenticated())
            {
                Code.debug("FORM Authenticated for ",user.getName());
                httpRequest.setAuthType(SecurityConstraint.__FORM_AUTH);
                httpRequest.setAuthUser(user.getName());
                httpRequest.setUserPrincipal(user);
                return user;
            }
        }
        
        // Don't authenticate authform or errorpage
        if (pathInContext!=null &&
            pathInContext.equals(_formErrorPage) || pathInContext.equals(_formLoginPage))
            return SecurityConstraint.__NOBODY;
        
        // redirect to login page
        if (httpRequest.getQuery()!=null)
            uri+="?"+httpRequest.getQuery();
        session.setAttribute(__J_URI, URI.addPaths(request.getContextPath(),uri));
        response.sendRedirect(response.encodeRedirectURL(URI.addPaths(request.getContextPath(),
                                           _formLoginPage)));
        return null;
    }

}
