// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpContextMBean.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.mortbay.http.HttpServer;
import org.mortbay.http.HttpContext;
import org.mortbay.util.Code;
import org.mortbay.util.Log;
import org.mortbay.util.LifeCycle;
import org.mortbay.util.LogSink;
import org.mortbay.util.jmx.LifeCycleMBean;
import org.mortbay.util.jmx.LifeCycleMBean;

import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextMembershipEvent;

import java.util.Iterator;

/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class HttpContextMBean extends LifeCycleMBean
{
    private HttpContext _httpContext;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     * @exception InstanceNotFoundException 
     */
    public HttpContextMBean()
        throws MBeanException
    {}
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();

        defineAttribute("virtualHosts");
        defineAttribute("contextPath");
        
        defineAttribute("handlers",false);

        defineAttribute("classPath");
        
        defineAttribute("realm");
        defineAttribute("realmName");
        
        defineAttribute("redirectNullPath");
        defineAttribute("resourceBase");
        defineAttribute("maxCachedFileSize");
        defineAttribute("maxCacheSize");
        defineAttribute("dirAllowed");
        defineOperation("getResource",
                        new String[] {STRING},
                        IMPACT_ACTION);
        
        defineAttribute("welcomeFiles");
        defineOperation("addWelcomeFile",
                        new String[] {STRING},
                        IMPACT_INFO);
        defineOperation("removeWelcomeFile",
                        new String[] {STRING},
                        IMPACT_INFO);
        
        defineAttribute("mimeMap");
        defineOperation("setMimeMapping",new String[] {STRING,STRING},IMPACT_ACTION);

        defineAttribute("statsOn");
        defineAttribute("statsOnMs");
        defineOperation("statsReset",IMPACT_ACTION);
        defineAttribute("requests");
        defineAttribute("requestsActive");
        defineAttribute("requestsActiveMax");
        defineAttribute("responses1xx");
        defineAttribute("responses2xx");
        defineAttribute("responses3xx");
        defineAttribute("responses4xx");
        defineAttribute("responses5xx");
        
        defineOperation("stop",new String[] {"java.lang.Boolean.TYPE"},IMPACT_ACTION);
        
        defineOperation("destroy",
                        IMPACT_ACTION);
        
        defineOperation("setInitParameter",
                        new String[] {STRING,STRING},
                        IMPACT_ACTION);
        defineOperation("getInitParameter",
                        new String[] {STRING},
                        IMPACT_INFO);
        defineOperation("getInitParameterNames",
                        NO_PARAMS,
                        IMPACT_INFO);
        
        defineOperation("setAttribute",new String[] {STRING,OBJECT},IMPACT_ACTION);
        defineOperation("getAttribute",new String[] {STRING},IMPACT_INFO);
        defineOperation("getAttributeNames",NO_PARAMS,IMPACT_INFO);
        defineOperation("removeAttribute",new String[] {STRING},IMPACT_ACTION);
        
        defineOperation("addHandler",new String[] {INT,"org.mortbay.http.HttpHandler"},IMPACT_ACTION);
        defineOperation("removeHandler",new String[] {INT},IMPACT_ACTION);
        

        _httpContext=(HttpContext)getManagedResource();
    }
    
    
    /* ------------------------------------------------------------ */
    protected ObjectName newObjectName(MBeanServer server)
    {
        ObjectName oName=super.newObjectName(server);
        String context=_httpContext.getContextPath();
        if (context.length()==0)
            context="/";
        try{oName=new ObjectName(oName+",context="+context);}
        catch(Exception e){Code.warning(e);}
        return oName;
    }
    
    /* ------------------------------------------------------------ */
    public void postDeregister()
    {
        _httpContext=null;
        super.postDeregister();
    }
}


