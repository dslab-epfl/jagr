
package roc.pinpoint.analysis.plugins2.amazon;

import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

public class QueryLogsToUnified implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String VALID_KEYS_ARG = "validKeys";

    PluginArg[] args = {
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
        new PluginArg( VALID_KEYS_ARG,
                       "keys in query log to pay attention to",
                       PluginArg.ARG_LIST,
                       true,
                       null )
    };

    /** In Amazon's query logs, valid keys are:
     *
     * SERVER_PORT,PATH_INFO,RequestId,SERVER_NAME,ProcessID,Hostname,REQUEST_METHOD,QUERY_STRING,Session,Timing,StatusCode
     * 
     * in the service logs:
     *
     * Program,ProcessId,Hostname,Operation,ClientProgram,ClientProcessId,ClientHost,ClientUser,ClientTask,ClientRequestId
     *
     * other keys that occur quite often (but aren't potential component names)
     *   include StatusCode,Time,SystemTime,UserTime,EndTime,StartTime,OracleTime,Size,Info
     *
     */

    RecordCollection inputCollection;
    RecordCollection outputCollection;

    List validKeys;
    Timer timer;

    public PluginArg[] getPluginArguments() {
        return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
        throws PluginException {

        inputCollection = (RecordCollection)
            args.get( INPUT_COLLECTION_NAME_ARG );
        outputCollection = (RecordCollection)
            args.get( OUTPUT_COLLECTION_NAME_ARG );
    
        validKeys = (List)
            args.get( VALID_KEYS_ARG );
 
        inputCollection.registerListener( this );
        timer = new Timer();
        timer.schedule(new MyCheckReady(), 0, 1000);
    }


    public void stop() {
        timer.cancel();
        inputCollection.unregisterListener( this );
    }

    
    public void addedRecord( String collectionName, Record datarec ) {
        Map data = (Map) datarec.getValue();
            
        addComponents( data );
    }

    void addComponents( Map data ) {

        ArrayList relatedComponents = new ArrayList();

        Iterator iter = validKeys.iterator();
        while( iter.hasNext() ) {
            String key = (String) iter.next();

            Object d = data.get( key );

            if( d instanceof List ) {
                List dlist = (List)d;

                // for each item in the list, add a new component
                // to relatedComponents set.
                // let the ComponentID be 
                Iterator iter2 = dlist.iterator();
                while( iter2.hasNext() ) {
                    // todo: figure out a way to keep the key around
                    //   --> perhaps setup a Map to make sure key names like
                    //   processid and clientprocessid get mapped to the same keyname

                    relatedComponents.add( (String)iter2.next() );
                }

            }
            else if( d instanceof Map ) {
                Map dmap = (Map)d;

                Iterator iter2 = dmap.values().iterator();
                while( iter2.hasNext() ) {
                    // todo: figure out a way to keep the multiple keys around
                    //   --> perhaps setup a Map to make sure key names like
                    //   processid and clientprocessid get mapped to the same keyname

                    relatedComponents.add( (String)iter2.next() );
                }
            }
            else if( d instanceof String ) {
                relatedComponents.add( (String)d );
            }
            else if( d == null ) {
                //   System.err.println( "ACK! d is NULL!; key =" + key );
            }
            else {
                throw new RuntimeException( "Don't know what this is: [" + d.getClass().toString() + "; " + d + "]" );
            }
        }

        //System.err.println( "QueryLogsToUnified: relatedComponents.size() = " + relatedComponents.size() );

        int size = relatedComponents.size();

        Record[] componentRecords = new Record[ size ];
        for( int i=0; i<size;i++ ) {
            componentRecords[i] = getComponentBehaviorRecord( (String)relatedComponents.get(i),size-1);
        }

        // for all items in relatedComponents, add links between all of them!
        for( int i=0; i<size; i++ ) {
            String a=(String)relatedComponents.get(i);
            WeightedSimpleComponentBehavior aCB = (WeightedSimpleComponentBehavior)componentRecords[i].getValue();
	    aCB.addOverallWeight(1.0);

            for( int j=i+1;j<size; j++ ) {

                String b=(String)relatedComponents.get(j);
                WeightedSimpleComponentBehavior bCB = (WeightedSimpleComponentBehavior)componentRecords[j].getValue();

                aCB.addWeightToUndirected(b,1.0);
                bCB.addWeightToUndirected(a,1.0);

            }
        }
             

        for( int i=0; i<size;i++ ) {
            outputCollection.setRecord( (String)relatedComponents.get(i), componentRecords[i] );
        }   
    }


    Record getComponentBehaviorRecord( String comp, int size ) {
        Record ret = outputCollection.getRecord( comp );
        if (ret == null) {
	    WeightedSimpleComponentBehavior cb = new WeightedSimpleComponentBehavior( comp, size );
	    ret = new Record( cb );
	}    
	return ret;
    }


    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }

    class MyCheckReady extends TimerTask {

        public void run() {
	    //System.err.println( "GeneratePaths.TimerTask" );
            String isReady = (String) inputCollection.getAttribute("isReady");
            if ((isReady == null) || (!isReady.equals("true"))) {
                // not ready
                return;
            }

            outputCollection.setAttribute("isReady", "true");
            timer.cancel();
        }
    }

}
