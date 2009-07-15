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
import java.net.*;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;

/**
 * @author emrek
 *
 */
public class ReceiveRecords implements Plugin {

    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String PORT_ARG = "port";

    PluginArg[] args = {
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will load records into the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations"),
	new PluginArg( PORT_ARG,
		       "TCP port to listen on.",
		       PluginArg.ARG_STRING,
		       true,
		       null )
    };

    RecordCollection outputRecordCollection;
    ServerSocket serverSocket;
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
	int port = ((Integer)args.get(PORT_ARG)).intValue();

        try {
	    serverSocket = new ServerSocket( port );
        }
        catch (IOException e) {
            throw new PluginException(
                "I/O Exception while opening receive records from net" );
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

            try {

		Socket sock = serverSocket.accept();

		InputStream is = sock.getInputStream();
		ObjectInputStream ois = new ObjectInputStream( is );

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
		System.err.println( "ReceiveRecords: Done loading Observations" );
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
