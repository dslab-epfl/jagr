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
package roc.pinpoint.analysis.plugins2.eviction;

// marked for release 1.0

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.structure.ComponentBehavior;

import org.apache.log4j.Logger;

/**
 * this plugin implements a simple eviction policy.  it periodically
 * flushes the record collections.
 *
 * @author emrek
 *
 */
public class PeriodicComponentBehaviorFlush implements Plugin {

    static Logger log = Logger.getLogger( "PeriodicComponentBehaviorFlush" );

    /** the name of this eviction policy **/
    public static final String POLICY_NAME = "PeriodicCBFlush";

    public static final String PERIOD_ARG = "period";

    PluginArg[] args = {
	new PluginArg( PERIOD_ARG,
		       "the periodicity of our flushes. specified in milliseconds",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( EvictionPolicyConstants.IS_DEFAULT_EVICTION_POLICY_ARG,
		       "is this the default eviction policy",
		       PluginArg.ARG_BOOLEAN,
		       true,
		       "true" )
    };


    private long period;
    private boolean isDefaultEvictionPolicy;

    private AnalysisEngine engine;

    private Timer timer;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
        throws PluginException {
        this.engine = engine;

	period = ((Integer)args.get(PERIOD_ARG )).intValue();
	isDefaultEvictionPolicy = ((Boolean)args.get( EvictionPolicyConstants.IS_DEFAULT_EVICTION_POLICY_ARG )).booleanValue();
	
        timer = new Timer(true);
        timer.schedule(new MySweeper(), 0, period); // fixed-delay
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        timer.cancel();
    }

    class MySweeper extends TimerTask {

        void flushRecordCollection(RecordCollection recordCollection) {

	    log.info( "flushing " + recordCollection.toString() );

	    synchronized( recordCollection ) {
		Map records = recordCollection.getAllRecords();
		Iterator iter = records.values().iterator();
		while( iter.hasNext() ) {
		    Record rec = (Record)iter.next();
		    Object v = rec.getValue();
		    if( v instanceof ComponentBehavior ) {
			ComponentBehavior cb = (ComponentBehavior)v;

	// TODO: perhaps if there isn't much data in the component,
	// we should just forget about the removeOld?  Check the magnitude
			// of the linkNormalizer...

			cb.removeOld();
			cb.markBehaviorAsOld();
		    }
		}

	    }

        }

        public void run() {
	    //System.err.println( "PeriodicFlush.TimerTask" );

            Map collections = engine.getAllRecordCollections();

            Iterator iter = collections.values().iterator();

            while (iter.hasNext()) {
                RecordCollection rc = (RecordCollection) iter.next();
                String sEvictionPolicy =
                    (String) rc.getAttribute(
                        EvictionPolicyConstants.EVICTION_POLICY_ARG);
                if ((sEvictionPolicy == null && isDefaultEvictionPolicy)
                    || (POLICY_NAME.equals( sEvictionPolicy ))) {
                    flushRecordCollection( rc );
                }
            }

        }
    }

}
