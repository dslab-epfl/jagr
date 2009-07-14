// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: LogMBean.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.util.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ReflectionException;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBean;

import org.mortbay.util.Code;
import org.mortbay.util.Log;
import org.mortbay.util.LogSink;
import org.mortbay.util.OutputStreamLogSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class LogMBean extends ModelMBeanImpl
{
    Log _log;
    HashMap _sinks=new HashMap();
    
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

        defineAttribute("logSinks",false);
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
            rescanSinks();
    }    

    /* ------------------------------------------------------------ */
    public void preDeregister()
    {
        super.preDeregister();

        ArrayList sinkKeys= new ArrayList(_sinks.keySet());
        Iterator iter=sinkKeys.iterator();
        while(iter.hasNext())
        {
            try
            {
                LogSink sink=(LogSink)iter.next();
                LogSinkMBean bean=(LogSinkMBean)_sinks.remove(sink);
                getMBeanServer().unregisterMBean(bean.getObjectName());
            }
            catch(Exception e)
            {
                Code.warning(e);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public Object getAttribute(String name)
        throws AttributeNotFoundException,
               MBeanException,
               ReflectionException
    {
        if ("logSinks".equals(name))
            rescanSinks();
        return super.getAttribute(name);
    }
    
    /* ------------------------------------------------------------ */
    public Object invoke(String name, Object[] params, String[] signature)
        throws MBeanException,
               ReflectionException
    {
        Object o=super.invoke(name,params,signature);
        if ("add".equals(name))
            rescanSinks();
        return o;
    }
    
    /* ------------------------------------------------------------ */
    private synchronized void rescanSinks()
    {
        LogSink[] sinks = _log.getLogSinks();

        // Add new beans
        for(int i=0;i<sinks.length;i++)
        {
            LogSink sink=sinks[i];

            ModelMBean bean=(LogSinkMBean)_sinks.get(sink);
            if (bean==null)
            {
                try
                {
                    bean = mbeanFor(sink);
                    getMBeanServer().registerMBean(bean,new ObjectName(getObjectName()+",sink="+i));
                    _sinks.put(sink,bean);
                }
                catch(Exception e)
                {
                    Code.warning(e);
                }
            }
        }

        // delete old beans
        if (_sinks.size()!=sinks.length)
        {
            Iterator iter=_sinks.keySet().iterator();
        keys:
            while(iter.hasNext())
            {
                try
                {
                    LogSink sink=(LogSink)iter.next();
                    for(int i=0;i<sinks.length;i++)
                        if(sink==sinks[i]) continue keys;
                
                    LogSinkMBean bean=(LogSinkMBean)_sinks.remove(sink);
                    
                    getMBeanServer().unregisterMBean(bean.getObjectName());
                }
                catch(Exception e)
                {
                    Code.warning(e);
                }
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public void postDeregister()
    {
        _log=null;
        if (_sinks!=null)
            _sinks.clear();
        _sinks=null;
    }
    
}
