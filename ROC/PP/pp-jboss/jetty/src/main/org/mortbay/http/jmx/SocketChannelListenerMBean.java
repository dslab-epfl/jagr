// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SocketChannelListenerMBean.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.http.jmx;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import org.mortbay.util.jmx.ThreadPoolMBean;

/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class SocketChannelListenerMBean extends ThreadPoolMBean
{
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     * @exception InstanceNotFoundException 
     */
    public SocketChannelListenerMBean()
        throws MBeanException
    {
        super();
    }

    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        defineAttribute("host");
        defineAttribute("port");
        defineAttribute("maxReadTimeMs");
        defineAttribute("lingerTimeSecs");
        defineAttribute("lowOnResources");
        defineAttribute("outOfResources");
        defineAttribute("defaultScheme");
    }
}
