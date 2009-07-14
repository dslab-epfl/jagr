/*
 * Class Untitled-1
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
 * $Id: SystemLog.java,v 1.1.1.1 2002/10/03 21:06:52 candea Exp $
 */    
package org.jboss.admin.systemlog;

// standard imports
import java.io.Serializable;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.JMSException;
import javax.naming.NamingException;

// jboss imports
import org.jboss.admin.MetricsConnector;
import org.jboss.admin.dataholder.ThreadMonitorEntry;
import org.jboss.admin.dataholder.MemoryMonitorEntry;
import org.jboss.monitor.MetricsConstants;

/**
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class SystemLog implements MessageListener, MetricsConstants, Serializable {

/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    public SystemLog() {
        try {
            MetricsConnector connector = new MetricsConnector();
            connector.setTopic("topic/metrics");
            connector.setMessageSelector("JMSType='" + SYSTEM_METRICS +"'");
            connector.connect(this);
            
            try   { Thread.sleep(10000000); }
            catch (InterruptedException ignored) {}
            
        }
        catch (NamingException e) {
            System.err.println("Unable to connect to metrics topic:");
            System.err.println(e);
        }
        catch (JMSException e) {
            System.err.println("Messaging error:");
            System.err.println(e.getMessage());
        }
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    public void onMessage(Message msg) {
        
        try {
            if (msg.getStringProperty("TYPE").equals(THREAD_MONITOR)) {
                ThreadMonitorEntry entry  = new ThreadMonitorEntry(msg);
                System.out.println(entry);
            }
            else if (msg.getStringProperty("TYPE").equals(MEMORY_MONITOR)) {
                MemoryMonitorEntry entry  = new MemoryMonitorEntry(msg);
                System.out.println(entry);
            }
        }
        catch (JMSException e) {
            System.err.println("Unknown message: " + msg);
        }
    }
}
