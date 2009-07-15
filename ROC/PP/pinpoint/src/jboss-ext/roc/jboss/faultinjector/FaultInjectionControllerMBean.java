/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.jboss.faultinjector;

// marked for release 1.0

import java.io.IOException;
import swig.util.XMLException;
import roc.pinpoint.injection.*;

public interface FaultInjectionControllerMBean
  extends org.jboss.system.ServiceMBean {

    public void addFaultTrigger( FaultTrigger ft );

    public void addFaultTrigger( String faultType, String faultArg, 
				 String csTriggerComponent );
    public void removeAllFaultTriggers();
    public void loadFaultConfigFile( String filename )
	throws XMLException, IOException;
}
