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
package roc.pinpoint.analysis.plugins2.observations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.analysis.structure.GrossStatistics;
import roc.pinpoint.analysis.structure.IdentifiableHelper;
import roc.pinpoint.analysis.structure.Statistics;
import roc.pinpoint.analysis.timeseries.TimeSeries;
import roc.pinpoint.tracing.Observation;

public class ParseStatisticsObservations implements Plugin, RecordCollectionListener {


    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String TIMESERIES_OUTPUT_COLLECTION_NAME_ARG = "timeseriesCollection";

    public static final String COMPONENT_DEF_ARG = "componentDefinition";


    public static final String INTERVAL_STAT_PREFIX_ARG = "intervalStatPrefix";
    public static final String INTERVAL_KEY_ARG = "intervalKey";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( TIMESERIES_OUTPUT_COLLECTION_NAME_ARG,
		       "time series statistics go to this collection.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( COMPONENT_DEF_ARG,
		       "define component by given comma-seperated attributes.",
		       PluginArg.ARG_LIST,
		       true,
		       null ),
	new PluginArg( INTERVAL_STAT_PREFIX_ARG,
		       "statistics to be normalized by a time interval should have begin their keyname with this prefix",
		       PluginArg.ARG_STRING,
		       false,
		       "LastInterval" ),
	new PluginArg( INTERVAL_KEY_ARG,
		       "the key name of the statistic representing the time interval since the last observation",
		       PluginArg.ARG_STRING,
		       false,
		       "TimeInterval" )	
    };

    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;
    private RecordCollection timeSeriesRecordCollection;
    private List componentDefinition;

    private String intervalKey;
    private String intervalStatPrefix;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        inputRecordCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
        outputRecordCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);
        timeSeriesRecordCollection = (RecordCollection)
	    args.get(TIMESERIES_OUTPUT_COLLECTION_NAME_ARG);
	componentDefinition = (List)
	    args.get(COMPONENT_DEF_ARG );

	intervalStatPrefix = (String)args.get(INTERVAL_STAT_PREFIX_ARG);
	intervalKey = (String)args.get( INTERVAL_KEY_ARG );

        inputRecordCollection.registerListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
    }


    public void addedRecord( String collectionName, Record obsRecord ) {
	
	    Observation obs = (Observation) obsRecord.getValue();
	    
	    Map comp = IdentifiableHelper.ReduceMap( obs.originInfo, 
					    componentDefinition );

	    Number timeInterval = (Number)obs.rawDetails.get( intervalKey );
	    double t = 1.0;
	    if( timeInterval != null ) {
		t = timeInterval.doubleValue() / 1000.0;
		obs.rawDetails.remove( intervalKey );
	    }

	    Iterator statsKeys = obs.rawDetails.keySet().iterator();
	    while( statsKeys.hasNext() ) {
		String k = (String)statsKeys.next();
		Object o = obs.rawDetails.get( k );

		if( o instanceof Number ) {
		    try {
			double d = ((Number)o).doubleValue();

			/**
			if( k.startsWith( intervalStatPrefix )) {
			    d/=t;
			}
			addTimeSeriesStatistic( comp, k, d );
			**/

			if( k.startsWith( intervalStatPrefix )) {
			    d /= t;
			    addStatistic( comp, k, d );
			}
			else {
			    // add the others to the time series
			    addTimeSeriesStatistic( comp, k, d );
			}
			

		    }
		    catch( Exception ignore ) {
			ignore.printStackTrace();
		    }
		}
	    }		


    }

    public void addTimeSeriesStatistic( Map comp, String k, double d ) {
	Record rec = timeSeriesRecordCollection.getRecord( k );
	if( rec == null ) {
	    HashMap map = new HashMap();
	    rec = new Record( map );
	}
	Map map = (Map)rec.getValue();
		
	TimeSeries ts = (TimeSeries)map.get( comp );
	if( ts == null ) {
	    ts = new TimeSeries( new double[ 10 ], 0, 0 );
	    map.put( comp, ts );
	}

	// add statistic to a circular buffer in the timeseries.
	ts.addValue( d );
	
	timeSeriesRecordCollection.setRecord( k, rec );
    }


    public void addStatistic( Map comp, String k, double d ) {
	
	Record rec = outputRecordCollection.getRecord( k );
	if( rec == null ) {
	    GrossStatistics gs = new GrossStatistics();
	    rec = new Record( gs );
	}
	GrossStatistics gs = (GrossStatistics)rec.getValue();

	//System.err.println( "adding statistic: " + comp + ": " + k + "=" + d );
	//System.err.println( "\tsize = " + gs.size() );
 
	Statistics s = new Statistics();
	s.addValue( d, null );	

	//gs.replaceStatistics( s, comp );
	gs.addStatistics( s, comp );

	outputRecordCollection.setRecord( k, rec );
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *  String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }


}
