package roc.pinpoint.analysis.plugins2.amazon;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import swig.util.StringHelper;
import roc.pinpoint.analysis.*;


public class LoadQueryLogs implements Plugin {

    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String FILENAME_ARG = "filename";
    public static final String MAXQUERIES_ARG = "maxqueries";

    PluginArg[] args = {
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( FILENAME_ARG,
		       "input file.  this plugin will load query log records from the file name specified in this argument",
		       PluginArg.ARG_LIST,
		       true,
		       null ),
	new PluginArg( MAXQUERIES_ARG,
		       "max queries to load; -1 means load all from file",
		       PluginArg.ARG_INTEGER,
		       false,
		       "-1" )
    };


    Thread worker;

    RecordCollection outputRecordCollection;
    List filenames;
    int currFile = 0;
    int maxqueries;

    LineNumberReader lnr;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
	throws PluginException {
	try {
	    outputRecordCollection = (RecordCollection) args.get( OUTPUT_COLLECTION_NAME_ARG );
	    filenames = (List) args.get( FILENAME_ARG );
            maxqueries = ((Integer)args.get( MAXQUERIES_ARG )).intValue();

            initNextFile();

	    worker = new Thread( new Worker() );
	    worker.start();
	}
	catch( Exception e ) {
            e.printStackTrace();
	    throw new PluginException( e );
	}

	
    }

    void initNextFile() throws IOException {
        if( currFile < filenames.size() ) {
            String fn = (String)filenames.get(currFile);
            currFile++;
            initFile( fn );
        }
        else {
            lnr = null;
        } 
    }

    void initFile( String filename ) throws IOException {
        if( lnr != null ) {
            lnr.close();
        }

        System.err.println( "LOADING QUERY LOG: " + filename );
        GZIPInputStream gzis = 
            new GZIPInputStream( new FileInputStream( filename ));
        lnr = new LineNumberReader( new InputStreamReader( gzis ));
    }

    public void stop() throws PluginException {

    }
    
    
    class Worker implements Runnable {

	public void run() {
	    Map data = new HashMap();
	    int i=0;
	    
	    try {

	    while( true ) {
                String line;
                try {
                    line = lnr.readLine();
		}
                catch( EOFException ex ) {
                    line = null;
                }

		if( line == null ) {
                    initNextFile();
                    if( lnr == null )
                        break;
                    else
                        continue;
                }

		if( line.startsWith( "EOE" )) {
                    if( data.size() > 0 ) {
                        outputRecordCollection.setRecord( Integer.toString( i++ ),
                                                          new Record( data ));
                        if( i % 50 == 0 ) {
//                            System.err.println( "last record loaded = " + data );
                            System.err.println( "loaded " + i + " queries" );
                        }
                        data = new HashMap();
                    }
                    continue;
		}
                else if( line.startsWith( "----" )) {
                    // ignore this line
                    if( data.size() > 0 ) {
                        outputRecordCollection.setRecord( Integer.toString( i++ ),
                                                          new Record( data ));
                        if( i % 50 == 0 ) {
//                            System.err.println( "last record loaded = " + data );
                            System.err.println( "loaded " + i + " queries" );
                        }
                        data = new HashMap();
                    }
                    continue;
                }
                else if( line.length() == 0 ) {
                    continue;
                }

                if( maxqueries!=-1 && i>maxqueries ) {
                    break;
                }


		int eqIdx = line.indexOf( "=" );
		if( eqIdx == -1 ) {
                    System.err.println( "ACK! no '=' found in line:\n[" + 
                                        line + "]\n\n" );
                }
                else {
                    String k = line.substring( 0, eqIdx );
                    Object o = parseValue( k, line.substring( eqIdx+1 ));
                    data.put( k, o );
                }
	    }
	    }
	    catch( IOException ignore ) {
		ignore.printStackTrace();
	    }
            
            System.err.println( "DONE LOADING QUERY LOGS" );
            outputRecordCollection.setAttribute( "isReady", "true" );
	}

        Object parseTiming( String timing ) {
            // special case for the "Timing" key

            List items = StringHelper.SeparateStrings( timing, ',' );
            List ret = new ArrayList( items.size() );

            Iterator iter = items.iterator();
            while( iter.hasNext() ) {
                String d = (String)iter.next();
                int col_idx = d.indexOf( ":" );
                if( col_idx == -1 ) {
                    ret.add( d );
                    continue;
                }
                String name = d.substring( 0, col_idx );
                
                // todo: parse out the counts of how often each name is used.
                // format in query log is "Timing=[name]:[time]/[count],..."
                // where [name] is the thing being used, [time] is how many ms
                // it took, and [count] is how many times it was called.

                ret.add( name );
            }

            return ret;
        }

	Object parseValue( String k, String v ) {
	    Object ret;

            if( "Timing".equals( k )) {
                // Special-case
                ret = parseTiming( v );
            }
	    else if( v.indexOf( "=" ) != -1 ) {
		List items = StringHelper.SeparateStrings( v, ',' );
		if( (((String)items.get(0)).indexOf( "=" ) == -1 ) ||
                    ( "QUERY_STRING".equals( k )) ||
                    ( "PATH_INFO".equals( k )) ||
                    ( "Redirect".equals( k ))) {
                    ret = items;
		}
		else {
                    try {
                        Map map = new HashMap();
                        Iterator iter = items.iterator();
                        while( iter.hasNext() ) {
                            String i = (String)iter.next();
                            int idx = i.indexOf( "=" );
                            if( "sigpipe".equals( i )) {
                                map.put( i, i );
                            }
                            else if( idx == -1 ) {
                                System.err.println( "ACK2! no '=' found in line:\n" + 
                                                    i + "\n whole value is: " + v + "\n" );
                            }
                            else {
                                map.put( i.substring(0,idx),
                                         i.substring(idx+1));
                            }
                        }
                        ret = map;
                    }
                    catch( StringIndexOutOfBoundsException e ) {
                        ret = v;
                    }
		}
	    }
	    else {
		ret = v;
	    }

	    return ret;
	}

    }

}
