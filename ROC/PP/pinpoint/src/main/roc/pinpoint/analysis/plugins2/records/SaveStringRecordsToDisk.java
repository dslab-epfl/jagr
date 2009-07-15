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

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import swig.util.StringHelper;
import roc.pinpoint.analysis.*;


/**
 * @author emrek
 *
 */
public class SaveStringRecordsToDisk implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String DIRECTORY_ARG = "directory";
    public static final String BASE_FILENAME_ARG = "baseFilename";
    public static final String TIMESTAMP_ARG = "timestamp";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will save records that get placed in the collection specified by this argument.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DIRECTORY_ARG,
		       "output directory.  this plugin will save records to ascii files specified in this argument. If directory is null/not specified, then only a single file will be generated, with the name of basefilename",
		       PluginArg.ARG_STRING,
		       false,
		       null ),
	new PluginArg( BASE_FILENAME_ARG,
		       "base filename to use for ascii files",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( TIMESTAMP_ARG,
		       "print the record's timestamp",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "true" )
    };

    long starttime;
    String directory;
    String baseFilename;
    boolean timestamp;

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

	starttime = System.currentTimeMillis();

        inputRecordCollection =
	    (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        baseFilename = (String) args.get(BASE_FILENAME_ARG);
	directory = (String) args.get(DIRECTORY_ARG);
	timestamp = Boolean.TRUE.equals((Boolean)args.get(TIMESTAMP_ARG));
	
	try {
	    if( directory == null ) {
		File f = new File( baseFilename );
		writer = new FileWriter( f );
		if( timestamp )
		    writer.write( "INITIAL TIMESTAMP = " + starttime + "\n" );
	    }
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

	    String s = "";
	    if( timestamp ) { 
		Long time = (Long)rec.getAttribute( "timestamp" );
		s += ((time==null)?
		      "no timestamp" :
		      "timestamp=" + time.longValue() + "\n" );
	    }
	    s += rec.getValue().toString();
		
	    try {
		if( writer != null ) {
		    writer.write( s );
		    writer.flush();
		}
		else {
		    String a = rec.getAttribute( "key" ).toString();
		    a = StringHelper.ReplaceAll( a, " ", "_" );
		    a = StringHelper.ReplaceAll( a, "/", "" );


		    File f = new File( directory + File.separator + baseFilename + a );
		    Writer fWriter = new FileWriter( f );
		    if( timestamp ) {
			fWriter.write( "INITIAL TIMESTAMP = " + starttime + "\n" );
		    }
		    fWriter.write( s );
		    fWriter.flush();
		    fWriter.close();
		}


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
