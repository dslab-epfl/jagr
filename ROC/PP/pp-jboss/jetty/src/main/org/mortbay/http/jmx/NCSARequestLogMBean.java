// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: NCSARequestLogMBean.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.http.jmx;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import org.mortbay.util.jmx.LifeCycleMBean;

/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class NCSARequestLogMBean extends LifeCycleMBean
{
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param sink 
     * @exception MBeanException 
     * @exception InstanceNotFoundException 
     */
    public NCSARequestLogMBean()
        throws MBeanException
    {}    

    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        defineAttribute("filename");
        defineAttribute("datedFilename");
        defineAttribute("logDateFormat");
        defineAttribute("logTimeZone");
        defineAttribute("retainDays");
        defineAttribute("extended");
        defineAttribute("append");
    }
}
