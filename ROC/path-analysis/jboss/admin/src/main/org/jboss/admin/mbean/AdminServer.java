/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.admin.mbean;

// standard imports

// JBoss imports
import org.jboss.system.ServiceMBeanSupport;


/**
 *   ... 
 *      
 *   @author Juha Lindfors (jplindfo@helsinki.fi)
 *   @version $Revision: 1.1.1.1 $
 */
public class AdminServer extends ServiceMBeanSupport implements AdminServerMBean {

    // Constructors --------------------------------------------------
    public AdminServer() {}

    
    // Method Overrides ----------------------------------------------
    public String getName() {
        return "Admin Server";    
    }
    
    public void startService() {
    }
    
    public void stopService() {
    }
    
    public void destroyService() {
    }
    
    // Management Interface ------------------------------------------
    public void createRoot(String user, char[] pw) {
        
    }

}

