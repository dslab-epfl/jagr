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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.system.ServiceMBeanSupport;

import roc.pinpoint.injection.FaultConfig;
import roc.pinpoint.injection.FaultGenerator;
import roc.pinpoint.injection.FaultTrigger;
import swig.util.XMLException;

public class FaultInjectionController 
    extends ServiceMBeanSupport
    implements FaultInjectionControllerMBean {

    public FaultInjectionController() {
    }

    public void startService() throws Exception {
    }

    public void stopService() {
    }

    
    public void addFaultTrigger( FaultTrigger ft ) {
	FaultConfig fc = FaultGenerator.getFaultConfig();
	fc.addFaultTrigger( ft );
    }
    
    public void addFaultTrigger( String sFaultType,
				 String faultArg,
				 Set triggerComponents ) {
	int faultType = FaultTrigger.ParseFaultType( sFaultType );
	FaultTrigger ft = new FaultTrigger( faultType, faultArg, triggerComponents );
	addFaultTrigger( ft );
    }
    
    public void addFaultTrigger( String faultType,
				 String faultArg,
				 String csTriggerComponent ) {
	// TODO: parse a Map of component defining attributes from
	//   the csTriggerComponent
	Map component = null;
	Set triggerComponents = Collections.singleton( component );
	addFaultTrigger( faultType, faultArg, triggerComponents );
    }

    public void removeAllFaultTriggers() {
	FaultConfig fc = FaultGenerator.getFaultConfig();
	fc.removeAllFaultTriggers();
    }

    public void loadFaultConfigFile( String filename ) 
	throws XMLException, IOException {
	File file = new File( filename );
	FaultConfig fc = FaultConfig.ParseFaultConfig( file );
	FaultGenerator.setFaultConfig( fc );
    }

}
