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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roc.pinpoint.analysis.*;


/**
 * @author emrek
 *
 */
public class SendRecords implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String HOSTNAME_ARG = "hostname";
    public static final String PORT_ARG = "port";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will save records that get placed in the collection specified by this argument.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations"),
	new PluginArg( HOSTNAME_ARG,
		       "hostname to send records to",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( PORT_ARG,
		       "port number to send records to",
		       PluginArg.ARG_INTEGER,
		       true,
		       null )
    };

    RecordCollection inputRecordCollection;
    ObjectOutputStream oos;

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

	String hostname = (String)args.get(HOSTNAME_ARG);
	int port = ((Integer)args.get(PORT_ARG)).intValue();

	inputRecordCollection.registerListener( this );

        try {
	    Socket s = new Socket( hostname, port );
	    OutputStream os = s.getOutputStream();
            oos = new ObjectOutputStream( os );
	}
        catch (IOException e) {
            throw new PluginException(
                "I/O Exception while opening output records to network " + hostname);
        }

    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
        inputRecordCollection.unregisterListener(this);
        try {
            oos.close();
        }
        catch( IOException e ) {
            throw new PluginException( "I/O Exception while closing output file " );
        }
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String, java.util.List)
     */
    public void addedRecord(String collectionName, Record rec ) {
        try {
            oos.writeObject( rec );
            oos.flush();
            oos.reset();
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
        
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String, java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
    }

}
