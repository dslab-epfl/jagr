package org.jboss.RR;

import java.net.*;
import java.io.*;
import java.util.*;
import roc.pinpoint.tracing.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;
import org.jboss.RR.FailureReport;

public class PinpointPluginBrain
    implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String BRAIN_HOSTNAME_ARG = "brainHostname";
    public static final String BRAIN_PORT_ARG = "brainPort";
  
    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( BRAIN_HOSTNAME_ARG,
		       "name of the host where RR 'Brain' is running",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( BRAIN_PORT_ARG,
		       "UDP port number where RR 'Brain' is listening",
		       PluginArg.ARG_INTEGER,
		       false, 
		       "2374" )
    };  



    private RecordCollection inputRecordCollection;
    private String brainHostname;
    private int brainPort;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        inputRecordCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
	brainHostname = (String)
	    args.get(BRAIN_HOSTNAME_ARG );
	brainPort = ((Integer)args.get(BRAIN_PORT_ARG)).intValue();

        inputRecordCollection.registerListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
    }


    /**
     * 
     * @see roc.pinpoint.analysiOAs.RecordCollectionListener#addedRecords(
     *  String,  List)
     */
    public void addedRecords(String collectionName, List items) {

        Iterator iter = items.iterator();

	while (iter.hasNext()) {
	    Record compRecord = (Record) iter.next();
	    Object v = compRecord.getValue();
	    
	    notifyBrain( v );
	}
    }

    void notifyBrain( Object o ) {
	if( o instanceof Component ) {
	    notifyBrain( (Component)o );
	}
	else if( o instanceof Set ) {
	    // it's a set of rankedobjects
	    Set s = (Set)o;
	    Iterator iter2 = s.iterator();
	    while( iter2.hasNext() ) {
		RankedObject ro = (RankedObject)iter2.next();
		notifyBrain( (Object)ro.getValue() );
	    }
	}
	else if( o instanceof ComponentBehavior ) {
	    ComponentBehavior cb = (ComponentBehavior)o;
	    notifyBrain( (Component)cb.getComponent() );
	}
	else {
	    System.err.println( "[ERROR] PINPOINT BRAIN PLUGIN ***** unrecognized object type (" + o.getClass().toString() + "); can't forward to brain" );
	}
    }
    
    void notifyBrain( Component c ) {

	System.err.println( "PINPOINT IS NOTIFYING THE BRAIN of a failure in " + (String)c.getId().get( "name" ));

	try {
	    String name = (String)c.getId().get( "name" );
	    InetSocketAddress sockAddr = 
		new InetSocketAddress( brainHostname, brainPort );
	
	    DatagramSocket socket = new DatagramSocket();
	    FailureReport report = 
		new FailureReport( name, new Date() );
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( report );
	    DatagramPacket packet = new DatagramPacket( baos.toByteArray(),
							baos.size(),
							sockAddr );
	    socket.send( packet );
	}
	catch( Exception ignore ) {
	    ignore.printStackTrace();
	}
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *  String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing for now (maybe tell brain?)
    }

}
