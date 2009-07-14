/*
 * Class SystemMonitorBean
 * Copyright (C) 2001  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * This package and its source code is available at www.jboss.org
 * $Id: SystemMonitorBean.java,v 1.1.1.1 2002/11/16 03:16:41 mikechen Exp $
 */

package org.jboss.admin.beans;

// standard imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

// non-standard class dependencies
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.admin.mbean.SystemMonitorMBean;


/**
 * ...
 *
 * @ejb:bean type="Stateful" name="SystemMonitor" jndi-name="admin/SystemMonitor"
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @version $Revision: 1.1.1.1 $
 */        
public class SystemMonitorBean implements SessionBean {

    // serializable fields (for passivation)
    private SessionContext ctx  = null;
    private String agentID      = null;
    
    // Proxy instance implementing the MBean interface
    private SystemMonitorMBean monitor = null;
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     
 
    /**
     * @ejb:create-method
     */
    public void ejbCreate() throws CreateException {
        try {
            monitor = (SystemMonitorMBean)MBeanProxy.create(
                            SystemMonitorMBean.class,
                            SystemMonitorMBean.OBJECT_NAME
                      );                                     
        }
        catch (Exception e) {
            throw new CreateException(e.getMessage());
        }
    }

    /**
     * @ejb:create-method
     */
    public void ejbCreate(String agentID) throws CreateException {
        throw new CreateException("Not Yet Implemented");
        
        // This constructor is required for the monitoring of system
        // resources in a cluster node. In a clustered environment we need
        // to specify the node this client session is monitoring (via AgentID?)
        // and find the correct MBeanServer instance. This will require changes
        // in the MBeanProxy implementation which currently looks for the first
        // MBeanServer instance in the same JVM.
        //                                                                 [JPL]
        
/*        
        this.agentID = agentID;
        mbeanServer = findMBeanServer(agentID);
*/
    }
    
    /**
     *
     * @ejb:interface-method type="remote"
     */
    public boolean isMemoryMonitorEnabled() {
        return monitor.isMemoryMonitorEnabled();
    }
    
    /**
     *
     * @ejb:interface-method type="remote"
     */
    public boolean isThreadMonitorEnabled() {
        return monitor.isThreadMonitorEnabled();
    }
    
    /**
     *
     * @ejb:interface-method type="remote"
     */
    public void setMemoryMonitorEnabled(boolean b) {
        monitor.setMemoryMonitorEnabled(b);
    }
    
    /**
     *
     * @ejb:interface-method type="remote"
     */
    public void setThreadMonitorEnabled(boolean b) {
        monitor.setThreadMonitorEnabled(b);
    }
    
/*
 *************************************************************************
 *
 *      IMPLEMENTS  SESSION_BEAN  INTERFACE
 *
 *************************************************************************
 */ 

    /**
     * Stores the session context upon creation.
     *
     * @param   ctx     session context object
     */
    public void setSessionContext(SessionContext ctx) {
        this.ctx = ctx;    
    }
    
    public void ejbActivate()  { }


    public void ejbPassivate() { }
    
    public void ejbRemove() { }
    
/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */ 
    
    /**
     *
     * @throws EJBExcetption
     */
    private MBeanServer findMBeanServer(String agentID) {
        try {
            ArrayList list = MBeanServerFactory.findMBeanServer(agentID);
            return ((MBeanServer)list.iterator().next());
        }
        catch (NoSuchElementException e) {
            // This occurs if no MBeanServer instance is found in the JVM.
            // We'll throw an EJBException and tell the container it's
            // free to discard this bean instance.
            throw new EJBException("Unable to find an MBeanServer");
        }
    }
}

