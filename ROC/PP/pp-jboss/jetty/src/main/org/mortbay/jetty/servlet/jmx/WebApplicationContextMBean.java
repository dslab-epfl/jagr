// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: WebApplicationContextMBean.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.jetty.servlet.jmx;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

/* ------------------------------------------------------------ */
/** Web Application MBean.
 * Note that while Web Applications are HttpContexts, the MBean is
 * not derived from HttpContextMBean as they are managed differently.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class WebApplicationContextMBean extends ServletHttpContextMBean
{
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     * @exception InstanceNotFoundException 
     */
    public WebApplicationContextMBean()
        throws MBeanException
    {}

    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();

        defineAttribute("displayName",false);
        defineAttribute("defaultsDescriptor",true);
        defineAttribute("deploymentDescriptor",false);
        defineAttribute("WAR",true);
        defineAttribute("extractWAR",true);
    }
    
}
