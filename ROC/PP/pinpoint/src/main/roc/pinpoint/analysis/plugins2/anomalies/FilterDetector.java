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
package roc.pinpoint.analysis.plugins2.anomalies;

import java.io.*;
import java.util.*;

import roc.pinpoint.tracing.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;



/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FilterDetector implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String ID_LIST_ARG = "idList";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String ONLINE_ARG = "online";

    PluginArg[] args =
    {
	new PluginArg(
		      INPUT_COLLECTION_ARG,
		      "input collection.  this plugin compares the paths in the input collection against a PCFG",
		      PluginArg.ARG_RECORDCOLLECTION,
		      true,
		      null),
	new PluginArg(
		      OUTPUT_COLLECTION_ARG,
		      "output collection. this plugin will place anomalous paths into this record collection",
		      PluginArg.ARG_RECORDCOLLECTION,
		      true,
		      null),
	new PluginArg(
		      ID_LIST_ARG,
		      "list of request ids to filter as 'bad'",
		      PluginArg.ARG_STRING,
		      true,
		      null),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
		       
    };
    
    RecordCollection inputCollection;
    RecordCollection outputCollection;
    
    String idListFile;

    Set badids;

    AnalysisEngine engine;
    private boolean online;

    private Timer timer;
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#getPluginArguments()
     */
    public PluginArg[] getPluginArguments() {
	return args;
    }
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#start(java.lang.String, java.util.Map, roc.pinpoint.analysis.AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
	throws PluginException {
	inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
	outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
	
	idListFile = (String)args.get( ID_LIST_ARG );
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();

	try {
	    badids = loadRequestIds( new File( idListFile ));
	}
	catch( IOException e ) {
	    e.printStackTrace();
	    System.exit(-1);
	}

	this.engine = engine;
	
	inputCollection.registerListener( this );

	timer = new Timer();
	timer.schedule( new MyCheckReady(), 0, 1000 );
    }


    public Set loadRequestIds( File f ) throws IOException {
        LineNumberReader lnr = 
	    new LineNumberReader( new FileReader( f ));

	HashSet ret = new HashSet();
	
	while( true ) {
	    String l = lnr.readLine();
	    if( l == null )
		break;
	    ret.add( l );
	}

	return ret;	
    }
     


    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
	inputCollection.unregisterListener( this );
	timer.cancel();
    }
    


    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String, java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {

	    Path p = (Path) rec.getValue();

	    System.err.println( "filterdetector: analyzing path: " + p.getRequestId() );

	    if( badids.contains( p.getRequestId() ) ) {

		System.err.println( "filterdetector: FOUND ERROR: " + p.getRequestId() );
		Observation err = new Observation();
		err.eventType = Observation.EVENT_ERROR;
		err.rawDetails.put( "errordescr", 
				    "path shape probability fell below threshold" );
		p.addError( err );
	    }
	    
	    Record outrec = outputCollection.getRecord( "pcfganomalies" );
	    if( outrec == null ) {
		outrec = new Record( new TreeSet() );
	    }
	    
	    SortedSet s = (SortedSet)outrec.getValue();
	    
	    RankedObject ro = new RankedObject( 0.0, p );
	    
	    s.add( ro );
	    
	    outputCollection.setRecord( "pcfganomalies", outrec );

	
    }
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String, java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
	
    }


    class MyCheckReady extends TimerTask {

	public void run() {
	    //System.err.println( "FilterDetector.TimerTask" );
	    String isReady = (String)inputCollection.getAttribute( "isReady" );
	    if(( isReady == null ) || (!isReady.equals( "true" ))) {
		// not ready
		return;
	    }

	    outputCollection.setAttribute( "isReady", "true" );
	    timer.cancel();
	}
    }    
}
