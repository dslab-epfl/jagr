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
package roc.pinpoint.analysis.plugins2.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.structure.ComponentBehavior;
import roc.pinpoint.analysis.structure.RankedObject;
import swig.util.StringHelper;


/**
 * @author emrek
 *
 */
public class SaveRankedComponents implements Plugin {

    public static final String INPUT_COLLECTION_NAME_ARG = "input";
    public static final String FILENAME_ARG = "filename";
    public static final String DIRECTORY_ARG = "directory";
    public static final String ID_ARG = "id";


    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will save records that get placed in the collection specified by this argument.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations"),
	new PluginArg( FILENAME_ARG,
		       "output file.  this plugin will save records to the file name specified in this argument",
		       PluginArg.ARG_STRING,
		       true,
		       "log.observations" ),
	new PluginArg( ID_ARG,
		       "id something or other for dynamic filenames - hack",
		       PluginArg.ARG_STRING,
		       false,
		       null ),
	new PluginArg( DIRECTORY_ARG,
		       "output directory.  if specified, this plugin will save each record to its own file (named by the recordid of the record) in this directory",
		       PluginArg.ARG_STRING,
		       false,
		       null )
    };

    String filename;
    String directory;
    Timer timer;

    RecordCollection inputRecordCollection;
    PrintWriter pw;

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
	directory = (String)args.get(DIRECTORY_ARG );
	id = (String)args.get( ID_ARG );

	if( id != null ) {
	    filename = makeFilename( filename + id );
	    if( directory != null ) {
		filename = directory + File.separator + filename;
		directory = null;
	    }
	}

        try {
	    if( directory == null ) {
		pw = new PrintWriter( new FileOutputStream(new File(filename)));
	    }
        }
        catch (FileNotFoundException e) {
            throw new PluginException(
                "Unable to open or create output file " + filename);
        }


	timer = new Timer();
	timer.schedule( new MySaveRecords(), 0, 1000 );
    }

    class MySaveRecords extends TimerTask {
	public void run() {
	    //System.err.println( "SaveRankedComponents.TimerTask" );
	    String isReady = (String)inputRecordCollection.getAttribute( "isReady" );
	    if( (isReady == null ) || (!isReady.equals( "true" ))) {
		// not ready
		return;
	    }

	    System.out.println( "SaveRecords: Starting to Save..." );

	    try {
	    
		synchronized( inputRecordCollection ) {
		    Map m = inputRecordCollection.getAllRecords();
		    Iterator iter = m.values().iterator();
		    while( iter.hasNext() ) {
			saveRecord( (Record)iter.next() );
			System.out.println( "." );
		    }
		}
		
		System.out.println( "SaveRecords: Finished saving." );
		stop();
	    }
	    catch( PluginException ignore ) {
		ignore.printStackTrace();
	    }
	}
    }

    String makeFilename( String k ) {
	k = StringHelper.ReplaceAll( k, File.separator, "_" );
	k = StringHelper.ReplaceAll( k, " ", "_" );
	k = StringHelper.ReplaceAll( k, "(", "_" );
	k = StringHelper.ReplaceAll( k, ")", "_" );
	k = StringHelper.ReplaceAll( k, "{", "_" );
	k = StringHelper.ReplaceAll( k, "}", "_" );
	k = StringHelper.ReplaceAll( k, "[", "_" );
	k = StringHelper.ReplaceAll( k, "]", "_" );
	
	return k;	
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
	timer.cancel();
	pw.close();
    }

    public void saveRecord( Record rec ) throws PluginException {
	try {
	    if( pw != null ) {
		System.err.println( "SaveRecordToDisk -> saved record..." );
		saveRecord( pw, rec );
		pw.flush();
	    }
	    else {
		String k = makeFilename( filename + rec.getAttribute( "key" ).toString() );
		File f = new File( directory + File.separator + k );
		System.err.println( "SaveRecordToDisk.addedRecords: writing to " + f );
		PrintWriter pwtmp = new PrintWriter( new FileOutputStream( f ));
		saveRecord( pwtmp, rec );
		pwtmp.flush();
		pwtmp.close();
	    }
	}
        catch( IOException e ) {
            throw new PluginException( "I/O Exception while closing output file " + filename );
        }

    }

    private void saveRecord( PrintWriter w, Record rec ) {
	Collection c = (Collection)rec.getValue();
	Iterator iter = c.iterator();
	while( iter.hasNext() ) {
	    RankedObject ro = (RankedObject)iter.next();
	    double d = ro.getRank();
	    ComponentBehavior cb = (ComponentBehavior)ro.getValue();
	    Map id = cb.getId();
	    String compid = id.toString();
	    w.println( d + " " + compid );
	}
    }


}

