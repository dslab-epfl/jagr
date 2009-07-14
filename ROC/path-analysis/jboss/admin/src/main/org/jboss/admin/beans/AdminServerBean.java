/*
 * Class AdminServerBean.java
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
 * $Id: AdminServerBean.java,v 1.1.1.1 2002/11/16 03:16:41 mikechen Exp $
 */     
package org.jboss.admin.beans;

// standard imports
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MalformedObjectNameException;
 
// JBoss classes
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.admin.mbean.AdminServerMBean;

/**
 * ???
 *
 * @ejb:bean type="Stateful" name="AdminServer" jndi-name="admin/AdminServer"
 * @ejb:security-role-ref role-name="new_install" role-link="temp"
 * @ejb:permission role-name="root"
 * @ejb:permission role-name="temp"
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @version $Revision: 1.1.1.1 $
 */     
public class AdminServerBean implements SessionBean {
    
    /** Security role reference used to identify a newly installed server instance */
    private final static String NEW_INSTALL = "new_install";

    // serializable fields (for passivation)
    private SessionContext ctx     = null;
    private AdminServerMBean admin = null;
     
     
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     

    /**
     * @ejb:create-method
     * @ejb:permission role-name="root"
     * 
     */
    public void ejbCreate() throws CreateException { 
        try {
            admin = createAdminProxy();
        }
        catch (MalformedObjectNameException e) {
            throw new CreateException("Unable to create an MBean proxy to " + AdminServerMBean.OBJECT_NAME + " due to malformed object name.");
        }
    }
    
    /**
     * @ejb:create-method
     * @ejb:permission role-name="temp"
     */
    public void ejbCreate(String user, char[] password) throws CreateException {
        try {
            admin = createAdminProxy();
            admin.createRoot(user, password);
        
            // User root = admin.createUser(user, password);
            // Role role = admin.createRole("root");
        
            // this constructor should be accessible only once
            // admin.deleteUser("jboss");
            // admin.deleteRole("temp");
        }
        catch (MalformedObjectNameException e) {
            throw new CreateException("Unable to create an MBean proxy to " + AdminServerMBean.OBJECT_NAME + " due to malformed object name.");
        }
    }
    
    /**
     * @ejb:interface-method type="remote"
     * @ejb:permission role-name="root"
     * @ejb:permission role-name="temp"
     */
    public boolean isAdminServerEnabled() throws NoRootException {
        
        if (ctx.isCallerInRole(NEW_INSTALL))
           throw new NoRootException("Root not installed");
           
        return true;
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
    
    public void ejbActivate()  {    }

    public void ejbPassivate() {    }
    
    public void ejbRemove() { }

/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */     

    /*
     * returns proxy implementing the mbean interface (proxy should be serializable)
     */
    private AdminServerMBean createAdminProxy() throws MalformedObjectNameException {
        return (AdminServerMBean)MBeanProxy.create(
                AdminServerMBean.class, AdminServerMBean.OBJECT_NAME);                                     
    }
    
    
}
