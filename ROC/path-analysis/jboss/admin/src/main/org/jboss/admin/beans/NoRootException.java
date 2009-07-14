/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.admin.beans;


/**
 *   ... 
 *      
 *   @author Juha Lindfors (jplindfo@helsinki.fi)
 *   @version $Revision: 1.1.1.1 $
 */
public class NoRootException extends Exception {

    public NoRootException() {
        super();
    }
    
    public NoRootException(String msg) {
        super(msg);
    }
}
