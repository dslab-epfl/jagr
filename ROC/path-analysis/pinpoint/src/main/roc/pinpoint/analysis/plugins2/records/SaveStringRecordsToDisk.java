package roc.pinpoint.analysis.plugins2.records;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import swig.util.StringHelper;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;


/**
 * @author emrek
 *
 */
public class SaveStringRecordsToDisk implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String DIRECTORY_ARG = "directory";
    public static final String BASE_FILENAME_ARG = "baseFilename";

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
		       null )
    };

    String directory;
    String baseFilename;

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
        baseFilename = (String) args.get(BASE_FILENAME_ARG);
	directory = (String) args.get(DIRECTORY_ARG);

	try {
	    if( directory == null ) {
		File f = new File( baseFilename );
		writer = new FileWriter( f );
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
    public void addedRecords(String collectionName, List items) {

        Iterator iter = items.iterator();
        while( iter.hasNext() ) {
	    Record rec = (Record)iter.next();
	    String s = rec.getValue().toString();
		
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
		    fWriter.write( s );
		    fWriter.flush();
		    fWriter.close();
		}


	    }
	    catch( Exception e ) {
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
