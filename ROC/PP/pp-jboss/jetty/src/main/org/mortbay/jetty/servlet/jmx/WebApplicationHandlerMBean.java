// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: WebApplicationHandlerMBean.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.jetty.servlet.jmx;

import javax.management.MBeanException;
import org.mortbay.http.jmx.HttpHandlerMBean;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.http.PathMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;


/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class WebApplicationHandlerMBean extends ServletHandlerMBean
{
    /* ------------------------------------------------------------ */
    private WebApplicationHandler _webappHandler;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     */
    public WebApplicationHandlerMBean()
        throws MBeanException
    {}
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        defineAttribute("acceptRanges"); 
        defineAttribute("filters",READ_ONLY,ON_MBEAN);
        _webappHandler=(WebApplicationHandler)getManagedResource();
    }

    /* ------------------------------------------------------------ */
    public String[] getFilters()
    {
        List l=_webappHandler.getFilters();
        String[] s = new String[l.size()];
        int i=0;
        Iterator iter = l.iterator();    
        while(iter.hasNext())
            s[i++]=iter.next().toString();
        return s;
    }
}
