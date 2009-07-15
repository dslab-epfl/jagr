package roc.pinpoint.analysis.plugins2.amazon;

import java.util.*;
import roc.pinpoint.analysis.*;

public class CountKeys implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null )
    };

    
    Timer timer;

    RecordCollection inputCollection;
    RecordCollection outputCollection;
    Map keyCounts;
    int total;

    public PluginArg[] getPluginArguments() {
        return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
        throws PluginException {

        keyCounts = new HashMap();
        total = 0;

        inputCollection = (RecordCollection)
            args.get( INPUT_COLLECTION_NAME_ARG );
    
        inputCollection.registerListener( this );

        timer = new Timer();
        timer.schedule( new Worker(), 0, 3000 );
    }

    public void stop() {
        inputCollection.unregisterListener( this );
        // TODO: stop worker
    }
    
    public void addedRecord( String collectionName, Record datarec ) {
        Map data = (Map) datarec.getValue();
            
        addKeyInfo( data );
        
    }



    void addKeyInfo( Map data ) {
        Iterator iter = data.keySet().iterator();
        while( iter.hasNext() ) {
            Object k = iter.next();
//            System.err.println( "incrementing key count for " + k.toString( ));
            Integer iKeyCount = (Integer)keyCounts.get( k );
            int kc = 0;
            if( iKeyCount != null ) {
                kc = iKeyCount.intValue();
            }
            kc++;
            keyCounts.put( k, new Integer( kc ));            
        }
        total++;
    }

    void printKeyList() {

        System.err.println( "PRINTKEYLIST" );
        System.out.println( "TOTAL RECORDS = " + total );
        Iterator iter = keyCounts.keySet().iterator();
        while( iter.hasNext() ) {
            Object k = iter.next();
            Integer v = (Integer)keyCounts.get(k);
            System.out.println( "[INFO] Countkeys: " + k + " -> " + 
                                ((double) v.intValue() / total) );
        }
    }

    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }

    class Worker extends TimerTask {

        public void run() {
            synchronized(inputCollection) {
                System.err.println( "... testing countkeys" );
                String isReady = (String)inputCollection.getAttribute( "isReady" );
                if( (isReady == null ) || (!isReady.equals( "true" ))) {
                    // not ready yet
                    return;
                }

                printKeyList();
            }
            timer.cancel();
            System.err.println( "Done Counting Keys!" );
        }

    }

}
