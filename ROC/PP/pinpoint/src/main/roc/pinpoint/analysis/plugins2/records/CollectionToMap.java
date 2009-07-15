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
package roc.pinpoint.analysis.plugins2.records;

// marked for release 1.0



import java.util.*;
import roc.pinpoint.analysis.*;

/**
 * This plugin takes all the Records stored in the inputCollection,
 * places them in a Map, and puts the resultant Map into the
 * outputRecordCollection.  
 *
 */
public class CollectionToMap implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "input";
    public static final String OUTPUT_COLLECTION_NAME_ARG ="output";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will save records that get placed in the collection specified by this argument.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations"),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations")
    };

    RecordCollection inputRecordCollection;
    RecordCollection outputRecordCollection;

    AnalysisEngine engine;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
	throws PluginException {

	this.engine = engine;

	inputRecordCollection = 
	    (RecordCollection) args.get( INPUT_COLLECTION_NAME_ARG );
	outputRecordCollection =
	    (RecordCollection) args.get( OUTPUT_COLLECTION_NAME_ARG );

	outputRecordCollection.appendToAttribute( "dependency", 
						  inputRecordCollection );

	inputRecordCollection.registerListener( this );
    }

    public void stop() throws PluginException {
	inputRecordCollection.unregisterListener( this );
    }

    public void addedRecord( String collectionName, Record ignore ) {
	// doesn't matter what changed, we're just going to export
	// the whole record collection contents as a single map in a record

	Map m = inputRecordCollection.getAllRecords();
	Record r = new Record( m );
	outputRecordCollection.setRecord( engine.getName(), r );
    }

    public void removedRecords( String collectionName, List items ) {
    }


}
