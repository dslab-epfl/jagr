/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: RemoteAdaptorInvocationHandler.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.jmx.adaptor;

import org.jboss.net.jmx.MBeanInvocationHandler;
import java.net.URL;

/**
 * An example client that accesses the JMXConnector via SOAP.
 * @created 1. October, 2001
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class RemoteAdaptorInvocationHandler {
        
    public static RemoteAdaptor createRemoteAdaptor(URL targetURL)  {
        return (RemoteAdaptor) MBeanInvocationHandler.createMBeanService(RemoteAdaptor.class,targetURL);
    }
    
    public static RemoteAdaptor createRemoteAdaptor(MBeanInvocationHandler handler) {
        return (RemoteAdaptor) MBeanInvocationHandler.createMBeanService(RemoteAdaptor.class,handler);
    }
    
}
