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
package roc.pinpoint.analysis.plugins2.unified;

import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;
import roc.pinpoint.analysis.clustering.*;

public class ClusterComponents implements Plugin {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String PERIOD_ARG = "period";
    public static final String DISTANCE_ARG = "distance";
    public static final String ONLINE_ARG = "online";

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
	new PluginArg( PERIOD_ARG,
		       "period.  this plugin will run the clustering every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( DISTANCE_ARG,
		       "period.  this plugin will run the clustering every period...  unit is milliseconds.",
		       PluginArg.ARG_DOUBLE,
		       false,
		       "0.2" ),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
    };

    RecordCollection inputCollection;
    RecordCollection outputCollection;
    int period;
    double distance;
    private boolean online;

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
        period = ((Integer)args.get( PERIOD_ARG )).intValue();
        distance = ((Double)args.get( DISTANCE_ARG )).doubleValue();
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();

        timer = new Timer();
        timer.schedule(new MyTask(), 0, period );
    }


    public void stop() {
    }

    public class MyTask extends TimerTask {

        public void run() {
            String icIsReady = (String)inputCollection.getAttribute( "isReady" );
            if( !online && (icIsReady == null ) || (!icIsReady.equals("true" ))) {
                System.err.println( "ClusterComponents: inputCollection isn't ready yet" );
                return;
            }
            System.err.println( "ClusterComponents: doing Clustering!" );


            HashSet tmpComponents;
            HashSet currClusters = GenerateClusters( inputCollection );
            HashSet mergedClusters = null;
            int passnum = 0;
            
            while( true ) {
                System.err.println( "Pass#" + passnum );
                int lastSize = currClusters.size();
                mergedClusters = Cluster.DoClustering( currClusters,
                                                       distance );


/*
                TreeSet debugClusters = new TreeSet( mergedClusters );
                outputCollection.setRecord( "cluster-pass#" + passnum,
                                            new Record( debugClusters ));
*/

                tmpComponents = CollapseComponents( mergedClusters );

                if( tmpComponents.size() == lastSize ) {
                    break;
                }

                currClusters = GenerateClusters( tmpComponents );
                passnum++;
            }

            TreeSet sortedClusters = new TreeSet( mergedClusters );

            System.err.println( "done with clustering" );

            outputCollection.setRecord( "final-cluster", new Record( sortedClusters ));

            if( !online ) {
                timer.cancel();
            }
        }

        public HashSet GenerateClusters( RecordCollection inputCollection ) {
            synchronized( inputCollection ) {
                Map records = inputCollection.getAllRecords();
                int size = records.size();
                ArrayList nodes = new ArrayList( size );

                Iterator iter = records.values().iterator();
                while( iter.hasNext() ) {
                    Record rec = (Record)iter.next();
                    WeightedSimpleComponentBehavior cb = (WeightedSimpleComponentBehavior)rec.getValue();
                    nodes.add( cb.lockValues() );
                }

                HashSet initialClusters = Cluster.BuildClusters( nodes );

                return initialClusters;
            }
   
        }


        public HashSet GenerateClusters( HashSet componentSet ) {
            int size = componentSet.size();
            ArrayList nodes = new ArrayList( size );

            Iterator iter = componentSet.iterator();
            HashSet ret = new HashSet( componentSet.size() );

            while( iter.hasNext() ) {
                WeightedSimpleComponentBehavior wscb = 
                    (WeightedSimpleComponentBehavior)iter.next();
                LockedComponentBehavior lcb = wscb.lockValues();
                nodes.add( lcb );
                ret.add( new Cluster( lcb, wscb.getElements() ));
            }

            return ret;
        }



        public HashSet CollapseComponents( HashSet mergedClusters ) {

            Map translationTable = new HashMap();

            // setup a translation table
            Iterator iter = mergedClusters.iterator();
            while( iter.hasNext() ) {
                Cluster c = (Cluster)iter.next();
                String cname = "Cluster#" + c.getID();

                Iterator elIter = c.getElements().iterator();
                while( elIter.hasNext() ) {
                    LockedComponentBehavior lcb = (LockedComponentBehavior)elIter.next();
                    translationTable.put( lcb.getComponentName(), cname );
                }
            }

            HashSet ret = new HashSet();

            iter = mergedClusters.iterator();
            while( iter.hasNext() ) {
                Cluster c = (Cluster)iter.next();
                
                WeightedSimpleComponentBehavior scb = new WeightedSimpleComponentBehavior(
                    "Cluster#" + c.getID() );
                
                LockedComponentBehavior lcb = c.getSummaryComponentBehavior();
                lcb.appendTo( scb, translationTable );
                scb.addElements( c.getAllElements() );
                ret.add( scb );              
            }

            return ret;
        }

    }

    

}
