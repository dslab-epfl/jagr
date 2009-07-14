// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: OutputStreamLogSinkMBean.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.util.jmx;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanOperationInfo;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.mortbay.util.Code;
import org.mortbay.util.Log;
import org.mortbay.util.LogSink;
import org.mortbay.util.LifeCycle;
import org.mortbay.util.OutputStreamLogSink;

/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class OutputStreamLogSinkMBean extends LogSinkMBean
{
    private boolean _formatOptions;

    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     */
    public OutputStreamLogSinkMBean()
        throws MBeanException
    {
        _formatOptions=true;
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     */
    public OutputStreamLogSinkMBean(OutputStreamLogSink sink)
        throws MBeanException
    {
        super(sink);
        _formatOptions=true;
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param formatOptions 
     * @exception MBeanException 
     */
    public OutputStreamLogSinkMBean(boolean formatOptions)
        throws MBeanException
    {
        _formatOptions=formatOptions;
    }

    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param formatOptions 
     * @exception MBeanException 
     */
    public OutputStreamLogSinkMBean(OutputStreamLogSink sink,boolean formatOptions)
        throws MBeanException
    {
        super(sink);
        _formatOptions=formatOptions;
    }
    
    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        if (_formatOptions)
        {
            defineAttribute("logDateFormat");
            defineAttribute("logTimezone");
            defineAttribute("logTimeStamps");
            defineAttribute("logLabels");
            defineAttribute("logTags");
            defineAttribute("logStackSize");
            defineAttribute("logStackTrace");
            defineAttribute("logOneLine");
        }
        
        defineAttribute("append");
        defineAttribute("filename");
        defineAttribute("datedFilename");
        defineAttribute("retainDays");
        defineAttribute("flushOn");
    }
    
}


