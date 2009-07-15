package roc.pinpoint.analysis.plugins2.amazon;

import java.util.*;
import roc.pinpoint.analysis.*;

public class FilterLogs implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String FILTER_KEYS_ARG = "filterKeys";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will save filtered log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
        new PluginArg( FILTER_KEYS_ARG,
                       "keys to accept --- anything else gets filtered out",
                       PluginArg.ARG_LIST,
                       true,
                       null )
    };

    
    Timer timer;

    RecordCollection inputCollection;
    RecordCollection outputCollection;
    Set keySet;
    int count=0;
    

    public PluginArg[] getPluginArguments() {
        return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
        throws PluginException {

        int count = 0;

        inputCollection = (RecordCollection)
            args.get( INPUT_COLLECTION_NAME_ARG );
        outputCollection = (RecordCollection)
            args.get( OUTPUT_COLLECTION_NAME_ARG );

        keySet = new HashSet( (List)args.get( FILTER_KEYS_ARG ));
    
        inputCollection.registerListener( this );
    }


    public void stop() {
        inputCollection.unregisterListener( this );
        // TODO: stop worker
    }
    

    public void addedRecord( String collectionName, Record datarec ) {
        Map data = (Map) datarec.getValue();
        Map filteredData = filterMap( data );
            
        outputCollection.setRecord( Integer.toString( count++ ),
                                    new Record( filteredData ));
    }


    Map filterMap( Map in ) {
        Map ret = new HashMap();
        Iterator iter = in.keySet().iterator();
        while( iter.hasNext() ) {
            Object k = iter.next();
            if( keySet.contains( k )) {
                ret.put( k, in.get(k));
            }
        }
        return ret;
    }
    

    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }

}
