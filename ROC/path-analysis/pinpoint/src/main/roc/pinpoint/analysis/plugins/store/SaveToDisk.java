package roc.pinpoint.analysis.plugins.store;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;

/**
 * @author emrek
 *
 */
public class SaveToDisk implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String FILENAME_ARG = "filename";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will save records that get placed in the collection specified by this argument.",
		       PluginArg.ARG_STRING,
		       true,
		       "observations"),
	new PluginArg( FILENAME_ARG,
		       "output file.  this plugin will save records to the file name specified in this argument",
		       PluginArg.ARG_STRING,
		       true,
		       "log.observations" )
    };

    String filename;

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
        filename = (String) args.get(FILENAME_ARG);

        try {
            oos =
                new ObjectOutputStream(
                    new FileOutputStream(new File(filename)));
        }
        catch (FileNotFoundException e) {
            throw new PluginException(
                "Unable to open or create output file " + filename);
        }
        catch (IOException e) {
            throw new PluginException(
                "I/O Exception while opening output file " + filename);
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
            throw new PluginException( "I/O Exception while closing output file " + filename );
        }
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String, java.util.List)
     */
    public void addedRecords(String collectionName, List items) {
        Iterator iter = items.iterator();
        while( iter.hasNext() ) {
            try {
                oos.writeObject( iter.next() );
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String, java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
    }

}
