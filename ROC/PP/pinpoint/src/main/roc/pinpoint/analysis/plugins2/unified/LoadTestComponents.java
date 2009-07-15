package roc.pinpoint.analysis.plugins2.unified;

import java.io.*;
import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

public class LoadTestComponents implements Plugin {

    public static final String INPUT_FILENAME_ARG = "inputfile";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_FILENAME_ARG,
		       "filename to load the test components from",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null )
    };

    public PluginArg[] getPluginArguments() {
	return args;
    }

    RecordCollection outputCollection;
	

    public void start( String id, Map args, AnalysisEngine engine ) 
	throws PluginException {

	String filename = (String) args.get( INPUT_FILENAME_ARG );
	outputCollection = (RecordCollection)
	    args.get( OUTPUT_COLLECTION_NAME_ARG );
	
	loadComponents( filename );
    }

    public void stop() {
    }
    

    private void loadComponents( String filename ) {

	try {
	    LineNumberReader lnr = 
		new LineNumberReader( new FileReader( filename ));

	    int i=0;

	    while( true ) {
		String line = lnr.readLine();
		if( line == null )
		    break;

		// check for comments
		if( line.startsWith( "#" ))
		    continue;

		String[] tokens = line.split( "[, \t\n]" );

		String compname = "Comp#" + (i+1);
		i++;

		WeightedSimpleComponentBehavior wscb =
		    new WeightedSimpleComponentBehavior( compname );

		for( int j=0; j<tokens.length; j++ ) {
		    System.err.println( compname + " [" + j + "] is '" + tokens[j] + "'" );

		    wscb.addWeightToUndirected( "Comp#" + (j+1), 
						Double.parseDouble(tokens[j]));
		}		

		outputCollection.setRecord( compname,
					    new Record( wscb ));
	    }

	    outputCollection.setAttribute( "isReady", "true" );
	}
	catch( IOException ex ) {
	    ex.printStackTrace();
	}

    }

}
