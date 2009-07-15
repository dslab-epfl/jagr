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
import java.util.*;

import swig.util.StringHelper;
import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;

import org.apache.log4j.Logger;

/**
 * @author emrek
 *
 */
public class LoadRecordsFromDiskId implements Plugin {

    static Logger log = Logger.getLogger( "LoadRecordsFromDiskId" );

    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DIR_ARG = "historicaldir";
    public static final String PREPEND_ARG = "prepend";
    public static final String ID_ARG = "id";

    PluginArg[] args = {
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will load records into the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations"),
	new PluginArg( DIR_ARG,
		       "input dir",
		       PluginArg.ARG_STRING,
		       true,
		       "log.observations" ),
	new PluginArg( ID_ARG,
		       "id: what are we reading?",
		       PluginArg.ARG_STRING,
		       true,
		       "log.observations" ),
	new PluginArg( PREPEND_ARG,
		       "prepend this to id to create filename",
		       PluginArg.ARG_STRING,
		       true,
		       "log.observations" )
    };

    String filename;
    String dir;
    String prepend;
    String id;

    RecordCollection outputRecordCollection;
    ObjectInputStream ois;
    Thread worker;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#start(java.lang.String, java.util.Map, roc.pinpoint.analysis.AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
        throws PluginException {

        outputRecordCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_NAME_ARG);

	prepend = (String) args.get(PREPEND_ARG);
	dir = (String) args.get(DIR_ARG);
	id = (String) args.get(ID_ARG);

	String k = prepend + id;
	k = StringHelper.ReplaceAll( k, File.separator, "_" );
	k = StringHelper.ReplaceAll( k, " ", "_" );
	k = StringHelper.ReplaceAll( k, "(", "_" );
	k = StringHelper.ReplaceAll( k, ")", "_" );
	k = StringHelper.ReplaceAll( k, "{", "_" );
	k = StringHelper.ReplaceAll( k, "}", "_" );
	k = StringHelper.ReplaceAll( k, "[", "_" );
	k = StringHelper.ReplaceAll( k, "]", "_" );
	filename = dir + File.separator + k;


        try {
            ois =
                new ObjectInputStream(
                    new FileInputStream(new File(filename)));
	    log.debug( "opened " + filename );
        }
        catch (FileNotFoundException e) {
	    log.error( "couldn't find " + filename );
	    /*            throw new PluginException(
                "Unable to open input file " + filename);
	    */
        }
        catch (IOException e) {
            throw new PluginException(
                "I/O Exception while opening input file " + filename);
        }

        worker = new Thread(new Worker());
        worker.start();
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
        // todo: stop worker thread
    }

    class Worker implements Runnable {

        public void run() {

            int recordnum = 0;

	    if( ois == null ) {
		log.error( "CANT LOAD HGCB!" );
		return;
	    }
            
            try {
                while( true ) {
                    Record r = (Record)ois.readObject();
		    Object key = r.getAttribute( "key" );
		    if( key == null )
			key = "" + recordnum;
		    outputRecordCollection.setRecord( key, r );
		    log.debug( "Done loading HGCB" );
		    recordnum ++;
                }
            }
            catch( EOFException e ) {
                // reached eof, that's ok.
		log.debug( "Done loading Observations" );
		outputRecordCollection.setAttribute( "isReady", "true" );
            }
            catch( ClassNotFoundException e ) {
                e.printStackTrace();
            }
            catch( IOException e ) {
                e.printStackTrace();
            }

        }

    }

}
