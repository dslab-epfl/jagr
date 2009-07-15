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
package roc.pinpoint.analysis.plugins2.jagr;

import java.util.*;
import java.io.IOException;
import java.net.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

import org.apache.log4j.Logger;

/**
 * This plugin expects its input collection to include a SortedSet of
 * RankedObjects, each containing a ComponentBehavior or Component.
 * All of these objects are then sent to the Failure Listener in the
 * recovery manager, which will decide whether or not to reboot these
 * components.
 *
 * @author emrek@cs.stanford.edu
 *
 */
public class JagrConnector implements Plugin, RecordCollectionListener {

    static Logger log = Logger.getLogger( "JagrConnector" );

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String RECOVERY_MGR_HOST_ARG = "recoveryManagerHost";
    public static final String RECOVERY_MGR_PORT_ARG = "recoveryManagerPort";
    

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for (Identifiable) possible anomalies in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( RECOVERY_MGR_HOST_ARG,
		       "hostname to send failure reports to",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( RECOVERY_MGR_PORT_ARG,
		       "the port on the recovery mgr to send failure reports to",
		       PluginArg.ARG_INTEGER,
		       true,
		       null )
    };

    private RecordCollection inputCollection;
    private String recoveryMgrHost;
    private int recoveryMgrPort;

    AnalysisEngine engine;

    private DatagramSocket socket = null;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start( String id, Map args, AnalysisEngine engine ) throws PluginException {
	inputCollection = (RecordCollection) args.get( INPUT_COLLECTION_ARG );
	recoveryMgrHost = (String) args.get( RECOVERY_MGR_HOST_ARG );
	recoveryMgrPort = ((Integer)args.get( RECOVERY_MGR_PORT_ARG )).intValue();
	this.engine = engine;

	try {
	    socket = new DatagramSocket();
	}
	catch( Exception e ) {
	    throw new PluginException( e );
	}
	inputCollection.registerListener( this );
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
	inputCollection.unregisterListener( this );
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(String,
     * List)
     */
    public void addedRecord( String collectionName, Record rec ) {

        SortedSet components = (SortedSet) rec.getValue();
        log.debug( "addedRecords: 2" );
        if( components.size() > 0 ) {
            sendFailureReport( components );
        }
	
    }

    public void removedRecords( String collectionName, List items ) {
    }

    private void sendFailureReport( SortedSet components ) {

	String report = "ejb;";
	Iterator iter = components.iterator();
	while( iter.hasNext() ) {
	    RankedObject ro = (RankedObject)iter.next();
	    Object val = ro.getValue();
	    String name;
	    if( val instanceof Identifiable ) {
		name = (String)((Identifiable)val).getId().get( "name" );
		if( name == null ) {
		    log.error( "ACK! 'name' of component is null.  fix this in JagrConnector!" );
		}
	    }
	    else {
		log.error( "unrecognized object type in anomalies list: " + val );
		continue;
	    }

	    report += name + ",";
	}

	sendFailureReport( report );
    }

    private void sendFailureReport( String report ) {
	try {
	    byte[] reportbytes = report.getBytes();
	    InetSocketAddress sockAddr = new InetSocketAddress( recoveryMgrHost, recoveryMgrPort );
	    DatagramPacket packet = new DatagramPacket( reportbytes, reportbytes.length, sockAddr );

	    log.info( "Sending Failure Report: " + report );
	    socket.send( packet );
	
	}
	catch( SocketException e ) {
	    log.error( "Failed to bind to UDP port" );
	    e.printStackTrace();
	}
	catch( IOException e ) {
	    log.error( "I/O Error reporting failure" );
	    e.printStackTrace();
	}
    }

}
