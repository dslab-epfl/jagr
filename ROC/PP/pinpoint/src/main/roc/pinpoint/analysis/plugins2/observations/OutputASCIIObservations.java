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
package roc.pinpoint.analysis.plugins2.observations;

// marked for release 1.0

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roc.pinpoint.analysis.*;
import roc.pinpoint.tracing.*;


/**
 * @author emrek
 *
 */
public class OutputASCIIObservations implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String FILENAME_ARG = "filename";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will save records that get placed in the collection specified by this argument.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( FILENAME_ARG,
		       "filename to use for ascii files",
		       PluginArg.ARG_STRING,
		       true,
		       null )
    };

    String filename;

    RecordCollection inputRecordCollection;

    Writer writer = null;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#start(java.lang.String, java.util.Map, roc.pinpoint.analysis.AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
        throws PluginException {

        inputRecordCollection =
	    (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        filename = (String) args.get(FILENAME_ARG);
		
	try {
	    File f = new File( filename );
	    writer = new FileWriter( f );
	}
	catch( IOException ioe ) {
	    throw new PluginException( ioe );
	}

	inputRecordCollection.registerListener( this );
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
        inputRecordCollection.unregisterListener(this);
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String, java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {

	    Observation obs = (Observation)rec.getValue();
	    
	    String s = "";

	    s += obs.originTimestamp + " ";
	    if( obs.attributes != null ) {
		s += (String)obs.attributes.get("stage" ) + " ";
		s += (String)obs.attributes.get("observationLocation" ) + " ";
	    }
	    else {
		s += "null null";
	    }
	    if( obs.originInfo != null ) {
		s += "[" + obs.originInfo.get( "name" ) + "] ";
	    }
	    else {
		s += "null";
	    }


	    s += obs.requestId + " ";
	    s += obs.sequenceNum;
	    s += "\n";

	    try {
		writer.write( s );
		writer.flush();
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }


    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String, java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
    }

}
