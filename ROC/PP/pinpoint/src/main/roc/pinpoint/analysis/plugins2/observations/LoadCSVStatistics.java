package roc.pinpoint.analysis.plugins2.observations;

import java.io.*;
import java.util.*;
import swig.util.StringHelper;
import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.analysis.structure.AbstractStatistics;
import roc.pinpoint.analysis.structure.Statistics;
import roc.pinpoint.analysis.structure.GrossStatistics;

public class LoadCSVStatistics implements Plugin {

    public static final String FILENAME_ARG = "filename";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( FILENAME_ARG,
		       "name of file containing csv statistics to load",
		       PluginArg.ARG_STRING,
		       true,
		       null )
	};


    private RecordCollection outputRecordCollection;
    private String filename;
    private Thread worker;

    private AnalysisEngine engine;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start( String id, Map args, AnalysisEngine engine ) 
	throws PluginException {
	outputRecordCollection = (RecordCollection)
	    args.get( OUTPUT_COLLECTION_NAME_ARG);
	filename = (String)args.get( FILENAME_ARG );

	LineNumberReader input;

	try {
	    input = new LineNumberReader( new FileReader( new File( filename )));
	}
	catch( IOException e ) {
	    throw new PluginException( "Unable to open input file " + filename , e );
	}

	worker = new Thread( new Worker(input) );
	worker.start();
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */    
    public void stop() throws PluginException {
	// todo: stop worker thread
    }


    class Worker implements Runnable {

	LineNumberReader input;

	public Worker( LineNumberReader input ) {
	    this.input = input;
	}

	public void run() {
	    int statCount = 0;

	    System.err.println( "[INFO]  LoadCSVStatistics: Beginning to load statistics..." );

	    try {
		while( true ) {
		    String line = input.readLine();
		    if( line == null )
			break;
		    try {
			if( parseLine( line ) ) {
			    statCount++;
			}
		    }
		    catch( Exception ex ) {
			System.err.println( "[ERROR]  LoadCSVStatistics: could not parse line '" + line + "'" );
			ex.printStackTrace();
		    }
		}

	    }
	    catch( Exception e ) {
		System.err.println( "[ERROR]  LoadCSVStatistics: got exception while loading CSV Statistics!!!" );
		e.printStackTrace();
	    }
	    finally {
		System.err.println( "[INFO] LoadCSVStatistics: Loaded " + statCount + " statistics" );
		try {
		    input.close();
		}
		catch( Exception ignore ) { }
		      
		outputRecordCollection.setAttribute( "isReady", "true" );
	    }
	}

	
	/**
	 * parses the line, assuming its in the format
	 *
	 * time,machinename,key,value
	 * 
	 * the separator is a comma, fields can be quoted, but are otherwise
	 *  trimmed of preceding and succeeding whitespace.
	 *
	 * lines beginning with '#' are treated as comments.
	 * empty lines are ignored.
	 *
	 * returns true if a statistic was found in the line, false otherwise
	 */
	boolean parseLine( String line ) {

	    if( line.startsWith( "#" ) || (line.trim().length() == 0)) {
		return false;
	    }

	    List l = separateStrings( line, ',', '"' );

	    String time= (String)l.get(0);  
	    String machinename = (String)l.get(1);
	    String key = (String)l.get(2);
	    String sValue = (String)l.get(3);
	    double value = Double.parseDouble( sValue );

	    // this assumes that all the statistics are activity statistics,
	    //   and none require time-series analysis.
	    addStatistic( machinename, key, value, time );
	    return true;
	}


	/**
	 * take the statistic information and put into the correct place in
	 * the output collection
	 */
	void addStatistic( String componentname, String key, double value, 
			   String timestamp ) {
	    Record rec = outputRecordCollection.getRecord( key );
	    if( rec == null ) {
		GrossStatistics gs = new GrossStatistics();
		rec = new Record( gs );
	    }
	    GrossStatistics gs = (GrossStatistics)rec.getValue();
	    
	    Statistics s = (Statistics)gs.getStatistics( componentname );
	    if( s == null ) {
		s = new Statistics();
	    }

	    s.addValue( value, timestamp );

	    gs.replaceStatistics( s, componentname );
	    outputRecordCollection.setRecord( key, rec );
	}
	
	/**
	 * reads a line of comma-separated values into a list.
	 * Handles quoted cells, but does not correctly handle quoting
	 * of quote characters.  E.g., "foo","bar" works, but not "foo\"","bar"
	 */
	ArrayList separateStrings( String s, char sep, char quote ) {
	    ArrayList ret = new ArrayList();
	    
	    String quoteStr = new String( new char[] {quote} );

	    int fieldstart = 0;
	    int fieldend=0;
	    
	    while( fieldstart < s.length() ) {
		int nextSep = s.indexOf( sep, fieldstart );
		int nextQuote = s.indexOf(quote, fieldstart );

		if(( nextQuote != -1 ) && ( nextQuote < nextSep )) {
		    int endQuote = s.indexOf( quote, nextQuote+1 );
		    nextSep = s.indexOf( sep, endQuote+1 );
		}

		if( nextSep == -1 ) {
		    fieldend = s.length();
		}
		else {
		    fieldend = nextSep;
		}

		String fieldvalue = s.substring( fieldstart, fieldend );
		fieldvalue = fieldvalue.trim();
		fieldvalue = StringHelper.ReplaceAll( fieldvalue, quoteStr, "" );
		ret.add( fieldvalue );

		fieldstart = fieldend+1;
	    }

	    return ret;
	}

    }

}
