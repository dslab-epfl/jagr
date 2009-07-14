// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ThreadPoolMBean.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.util.jmx;

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.mortbay.util.Code;
import org.mortbay.util.ThreadPool;
import org.mortbay.util.LifeCycle;


/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class ThreadPoolMBean extends LifeCycleMBean
{
    /* ------------------------------------------------------------ */
    public ThreadPoolMBean()
        throws MBeanException
    {
        super();
    }
    
    /* ------------------------------------------------------------ */
    public ThreadPoolMBean(ThreadPool object)
        throws MBeanException
    {
        super(object);
    }
    
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        defineAttribute("name");
        defineAttribute("threads");
        defineAttribute("idleThreads");
        defineAttribute("minThreads");
        defineAttribute("maxThreads");
        defineAttribute("maxIdleTimeMs");
        defineAttribute("maxStopTimeMs");
    }    
}
