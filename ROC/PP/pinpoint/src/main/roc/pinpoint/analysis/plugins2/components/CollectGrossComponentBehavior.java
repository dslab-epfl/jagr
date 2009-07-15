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
package roc.pinpoint.analysis.plugins2.components;

import java.util.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

/**
 *
 * This plugin aggregates ComponentBehaviors into
 * GrossComponentBehavior objects.  It collects together all
 * ComponentBehaviors which map together to the same logical
 * component, and puts them into a single GrossComponentBehavior
 * instance.  The output collection holds one record for each
 * GrossComponentBehavior instance, keyed by the ID of the logical
 * component.
 *
 * The "definingAttributes" determines which attributes define the
 * identity of a logical component.  E.g., if "name" is the only
 * defining attribute, then all components with the same name,
 * regardless of any other attribute values they might have, will be
 * aggregated together into a single GrossComponentBehavior.
 *
 */
public class CollectGrossComponentBehavior implements Plugin {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";
    public static final String COLLECTION_PERIOD_ARG = "collectionPeriod";
    public static final String ONLINE_ARG = "online";


    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for componentbehaviors in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place the generated gross component behaviors into the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DEFINING_ATTRIBUTES_ARG,
		       "comma-separated component 'defining attributes'. the plugin uses these attributes to define logical components.",
		       PluginArg.ARG_LIST,
		       true,
		       null ),
	new PluginArg( COLLECTION_PERIOD_ARG,
		       "collection period.  this plugin will collect the component behaviors every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private Collection definingAttributes;
    private long collectionPeriod;

    private AnalysisEngine engine;
    private Timer timer;
    private boolean online;



    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        this.engine = engine;

        inputCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
        outputCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);

	definingAttributes = (List)args.get( DEFINING_ATTRIBUTES_ARG );
	collectionPeriod = ((Integer)args.get( COLLECTION_PERIOD_ARG )).intValue();
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();

	System.err.println( "CGCB: definingattributes = " + definingAttributes );

        timer = new Timer();
        timer.schedule(new MyCollectionTask(), 0, collectionPeriod);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
    }



    class MyCollectionTask extends TimerTask {

        public void run() {

            System.err.println("CollectGrossComponentBehavior Collection Plugin pass...");


	    synchronized( inputCollection ) {
		Map records = new HashMap(inputCollection.getAllRecords());
		outputCollection.clearAllRecords();
		Iterator iter = records.values().iterator();
		while( iter.hasNext() ) {
		    Record cbRec = (Record)iter.next();
		    ComponentBehavior cb = (ComponentBehavior)cbRec.getValue();
		    
		    Map id = IdentifiableHelper.ReduceMap( cb.getId(), definingAttributes );
		    
		    Record grossRec = outputCollection.getRecord( id );
		    if( grossRec == null ) {
			grossRec = new Record( new GrossComponentBehavior( new HashSet( definingAttributes )) );
		    }
		    
		    GrossComponentBehavior gcb = 
			(GrossComponentBehavior)grossRec.getValue();
		    
		    gcb.addComponentBehavior( cb );
		    outputCollection.setRecord( id, grossRec );
		}

		String isReady = (String)inputCollection.getAttribute( "isReady" );
		if( !online && ( isReady != null ) && (isReady.equals( "true" ))) {
		    outputCollection.setAttribute( "isReady", "true" );
		    timer.cancel();
		}

	    } // end synchronized

        }

    }


}
