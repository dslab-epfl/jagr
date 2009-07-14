// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: SessionManager.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.jetty.servlet;

import java.io.Serializable;
import java.util.EventListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.mortbay.util.LifeCycle;

    
/* --------------------------------------------------------------------- */
/**
 *
 * @version $Id: SessionManager.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins
 */
public interface SessionManager extends LifeCycle, Serializable
{
    /* ------------------------------------------------------------ */
    public final static String __SessionId  = "JSESSIONID";
    public final static String __SessionUrlPrefix = ";"+__SessionId+"=";

    /* ------------------------------------------------------------ */
    /** Session Domain.
     * If this property is set as a ServletContext InitParam, then it is
     * used as the domain for session cookies. If it is not set, then
     * no domain is specified for the session cookie.
     */
    public final static String __SessionDomain=
        "org.mortbay.jetty.servlet.SessionDomain";
    
    /* ------------------------------------------------------------ */
    /** Session Path.
     * If this property is set as a ServletContext InitParam, then it is
     * used as the path for the session cookie.  If it is not set, then
     * the context path is used as the path for the cookie.
     */
    public final static String __SessionPath=
        "org.mortbay.jetty.servlet.SessionPath";
    
    /* ------------------------------------------------------------ */
    /** Session Max Age.
     * If this property is set as a ServletContext InitParam, then it is
     * used as the max age for the session cookie.  If it is not set, then
     * a max age of -1 is used.
     */
    public final static String __MaxAge=
        "org.mortbay.jetty.servlet.MaxAge";
    
    /* ------------------------------------------------------------ */
    public void initialize(ServletHandler handler);
    
    /* ------------------------------------------------------------ */
    public HttpSession getHttpSession(String id);
    
    /* ------------------------------------------------------------ */
    public HttpSession newHttpSession(HttpServletRequest request);

    /* ------------------------------------------------------------ */
    public void setMaxInactiveInterval(int seconds);

    /* ------------------------------------------------------------ */
    /** Add an event listener.
     * @param listener An Event Listener. Individual SessionManagers
     * implemetations may accept arbitrary listener types, but they
     * are expected to at least handle
     *   HttpSessionActivationListener,
     *   HttpSessionAttributeListener,
     *   HttpSessionBindingListener,
     *   HttpSessionListener
     * @exception IllegalArgumentException If an unsupported listener
     * is passed.
     */
    public void addEventListener(EventListener listener)
        throws IllegalArgumentException;
    
    /* ------------------------------------------------------------ */
    public void removeEventListener(EventListener listener);
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public interface Session extends HttpSession
    {
        /* ------------------------------------------------------------ */
        public boolean isValid();

        /* ------------------------------------------------------------ */
        public void access();
    }
    
}
