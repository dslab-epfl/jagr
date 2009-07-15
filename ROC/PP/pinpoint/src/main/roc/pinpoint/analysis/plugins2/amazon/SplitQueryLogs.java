package roc.pinpoint.analysis.plugins2.amazon;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import swig.util.StringHelper;
import roc.pinpoint.analysis.*;

public class SplitQueryLogs implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String DIR_ARG = "directory";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will load query log records from this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DIR_ARG,
		       "output directory",
		       PluginArg.ARG_STRING,
		       true,
		       null )
    };

    Thread worker;

    RecordCollection inputRecordCollection;
    
    PrintStream[] streams;

    public PluginArg[] getPluginArguments() {
        return args;
    }

    public void start( String id, Map args, AnalysisEngine engine )
        throws PluginException {

        inputRecordCollection = (RecordCollection) args.get( INPUT_COLLECTION_NAME_ARG );
        
        String dir = (String)args.get( DIR_ARG );

        try {
            initPrintStreams( dir );
        }
        catch( IOException e ) {
            throw new PluginException( e );
        }

        inputRecordCollection.registerListener( this );
    }
    
    void initPrintStreams( String dir ) throws IOException {
        streams = new PrintStream[61];
        for( int i=0; i<61; i++ ) {
            streams[i] = new PrintStream( new GZIPOutputStream( new FileOutputStream( new File( dir + "/" + Integer.toString( i ) + ".gz" ))));
        }
    }
    
    public void stop() throws PluginException {
        
    }


    public void addedRecord( String collectionName, Record dataRecord ) {
        Map data = (Map) dataRecord.getValue();
        
        if( data.containsKey( "StartTime" ) ) {
            String sst = (String)data.get("StartTime");
            double dst = Double.parseDouble(sst);
            int ist = (int)dst;
	    ist = ist / 60;  // get rid of seconds...
            int min = ist % 60;  // get rid of hours, days, etc.
            writeData( streams[min], data );
        }
        else {
            System.err.println( "[WARN] Found data record without StartTime value" );
            writeData( streams[60], data );
        }
    }

    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }
    
    void writeData( PrintStream ps, Map data ) {
        
        Iterator iter = data.keySet().iterator();
        while( iter.hasNext() ) {
            String k = (String)iter.next();
            Object v = data.get(k);
            String vs;
            if( v instanceof Map ) {
                vs = MapToString( (Map)v );
            }
            else if( v instanceof List ) {
                vs = ListToString( (List)v );
            }
            else if( v instanceof String ) {
                vs = (String)v;
            }
            else {
                throw new RuntimeException( "ACK! What is this? value is: " + v );
            }

            ps.println( k + "=" + vs );
        }

        ps.println( "------------------------------------------------------------------------" );

        ps.flush();
    }

    String MapToString( Map m ) {
        StringBuffer ret = new StringBuffer();
        Iterator iter = m.keySet().iterator();
        while( iter.hasNext() ) {
            String k = (String)iter.next();
            String v = (String)m.get(k);
            ret.append( k ).append("=").append( v );
            if( iter.hasNext() ) {
                ret.append( "," );
            }                
        }        
        return ret.toString();
    }

    String ListToString( List l ) {
        StringBuffer ret = new StringBuffer();
        Iterator iter = l.iterator();
        while( iter.hasNext() ) {
            String s = (String)iter.next();
            ret.append( s );
            if( iter.hasNext() ) {
                ret.append( "," );
            }
        }

        return ret.toString();
    }

}
