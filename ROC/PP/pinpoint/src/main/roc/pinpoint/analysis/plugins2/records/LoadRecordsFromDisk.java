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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;

import org.apache.log4j.Logger;

/**
 *
 *  This plugin loads previously saved Records from a file on disk into the
 *  output record collection.
 *
 *
 * @author emrek
 *
 */
public class LoadRecordsFromDisk implements Plugin {

    static Logger log = Logger.getLogger( "LoadRecordsFromDisk" );

    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String FILENAME_ARG = "filename";

    PluginArg[] args = {
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will load records into the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations"),
	new PluginArg( FILENAME_ARG,
		       "input file.  this plugin will load records from the file name specified in this argument",
		       PluginArg.ARG_STRING,
		       true,
		       "log.observations" )
    };

    String filename;

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

        filename = (String) args.get(FILENAME_ARG);

        try {
	    System.err.println( "[DEBUG] LoadRecordsFromDisk: Loading " + filename );
            ois =
                new ObjectInputStream(
                    new FileInputStream(new File(filename)));
        }
        catch (FileNotFoundException e) {
            throw new PluginException(
                "Unable to open input file " + filename);
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
            
            log.debug( "Beginning to Load Records!!!!!!");
            
            try {
		int count=0;
                while( true ) {
                    Record r = (Record)ois.readObject();
		    Object key = r.getAttribute( "key" );

		    if( key == null )
			key = "" + recordnum;

		    outputRecordCollection.setRecord( key, r );
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
		/*
		System.err.println( "****************************************" );
		System.err.println( "ERROR: !!!!! ABORTING RUN BAD FILE !!!!!" );
		System.err.println( "****************************************" );		System.exit(-1);
		*/
		log.warn( "Stream was corrupted.  continuing..." );
		outputRecordCollection.setAttribute( "isReady", "true" );
            }

        }

    }

}
