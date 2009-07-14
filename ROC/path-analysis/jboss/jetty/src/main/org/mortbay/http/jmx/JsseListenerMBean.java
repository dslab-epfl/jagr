// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: JsseListenerMBean.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http.jmx;

import javax.management.MBeanException;


/* ------------------------------------------------------------ */
/** 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class JsseListenerMBean extends SocketListenerMBean
{
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception MBeanException 
     * @exception InstanceNotFoundException 
     */
    public JsseListenerMBean()
        throws MBeanException
    {
        super();
    }

    /* ------------------------------------------------------------ */
    protected void defineManagedResource()
    {
        super.defineManagedResource();
        defineAttribute("needClientAuth");
    }
}
