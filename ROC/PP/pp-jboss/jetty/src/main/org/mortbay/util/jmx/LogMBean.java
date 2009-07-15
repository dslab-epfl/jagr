// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: LogMBean.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.util.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.ModelMBean;
import org.mortbay.util.Code;
import org.mortbay.util.Log;
import org.mortbay.util.LogSink;


public class LogMBean extends ModelMBeanImpl
{
    Log _log;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     * @exception InstanceNotFoundException 
     */
    public LogMBean()
        throws MBeanException, InstanceNotFoundException
    {
        super(Log.instance());
        _log=(Log)getManagedResource(); 
    }

    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     * @exception InstanceNotFoundException 
     */
    public LogMBean(Log log)
        throws MBeanException, InstanceNotFoundException
    {
        super(log);
        _log=(Log)getManagedResource(); 
    }

    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();

        defineAttribute("logSinks",READ_ONLY,ON_MBEAN);
        defineOperation("add",
                        new String[]{STRING},
                        IMPACT_ACTION);
        defineOperation("add",
                        new String[]{"org.mortbay.util.LogSink"},
                        IMPACT_ACTION);
        defineOperation("disableLog",
                        NO_PARAMS,
                        IMPACT_ACTION);
        defineOperation("message",
                        new String[]{STRING,STRING},
                        IMPACT_ACTION);
        
    }
    
    /* ------------------------------------------------------------ */
    public void postRegister(Boolean ok)
    {
        super.postRegister(ok);
        if (ok.booleanValue())
            getLogSinks();
    }
    
    /* ------------------------------------------------------------ */
    public void postDeregister()
    {
        super.postDeregister();
        _log=null;
    }
    
    /* ------------------------------------------------------------ */
    public ObjectName[] getLogSinks()
    {
        return getComponentMBeans(_log.getLogSinks(),null);
    }
}
