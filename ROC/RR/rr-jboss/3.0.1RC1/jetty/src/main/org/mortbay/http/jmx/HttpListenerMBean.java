// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpListenerMBean.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http.jmx;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.mortbay.http.HttpServer;
import org.mortbay.http.HttpListener;
import org.mortbay.util.Code;
import org.mortbay.util.Log;
import org.mortbay.util.LifeCycle;
import org.mortbay.util.jmx.ThreadedServerMBean;

import java.util.Iterator;
import org.mortbay.util.ThreadPool;

/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class HttpListenerMBean
    extends ThreadedServerMBean
{
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     */
    public HttpListenerMBean()
        throws MBeanException
    {}
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        defineAttribute("defaultScheme");
        defineAttribute("lowOnResources",false);
        defineAttribute("outOfResources",false);
        defineAttribute("confidentialPort");
        defineAttribute("confidentialScheme");
        defineAttribute("integralPort");
        defineAttribute("integralScheme"); 
        defineAttribute("bufferSize");  
        defineAttribute("bufferReserve"); 
    }    
}
