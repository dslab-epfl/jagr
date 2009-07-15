// ========================================================================
// Copyright (c) 2002,2003 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ServletHandlerMBean.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.jetty.servlet.jmx;

import javax.management.MBeanException;
import org.mortbay.http.jmx.HttpHandlerMBean;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.SessionManager;
import org.mortbay.http.PathMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.ObjectName;


/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class ServletHandlerMBean extends HttpHandlerMBean  
{
    /* ------------------------------------------------------------ */
    private ServletHandler _servletHandler;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     */
    public ServletHandlerMBean()
        throws MBeanException
    {}
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        defineAttribute("usingCookies"); 
        defineAttribute("servlets",READ_ONLY,ON_MBEAN);
        defineAttribute("sessionManager",READ_ONLY,ON_MBEAN);
        _servletHandler=(ServletHandler)getManagedResource();
    }

    /* ------------------------------------------------------------ */
    public ObjectName getSessionManager()
    {
        SessionManager sm=_servletHandler.getSessionManager();
        if (sm==null)
            return null;
        ObjectName[] on=getComponentMBeans(new Object[]{sm},null);
        return on[0];
    }
    
    /* ------------------------------------------------------------ */
    public String[] getServlets()
    {
        PathMap sm = _servletHandler.getServletMap();
        String[] s = new String[sm.size()];
        int i=0;
        Iterator iter = sm.entrySet().iterator();
        while(iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();
            s[i++]=entry.toString();
        }
        
        return s;
    }
    
    /* ------------------------------------------------------------ */
    public void postRegister(Boolean ok)
    {
        super.postRegister(ok);
        if (ok.booleanValue())
            getSessionManager();
    }
}
