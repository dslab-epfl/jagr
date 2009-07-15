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
package roc.pinpoint.analysis.plugins2.records;

// marked for release 1.0

import java.util.*;
import roc.pinpoint.analysis.*;

/**
 * This plugin monitors all the dependencies of a record collection to
 * see if they are done (marked as "isReady").  ("done" means that there
 * are not going to be any more changes made to the record collection).
 * When this is happens, this plugin marks the monitored record collection
 * as being done ("isReady") too.
 *
 * The dependencies of a record collection must be set by the individual
 * plugin(s) that output to this record collection.
 *
 *
 */
public class CheckIsReady implements Plugin {

    public static final String MONITOR_COLLECTION_NAME_ARG = "monitor";


    PluginArg[] args = {
	new PluginArg( MONITOR_COLLECTION_NAME_ARG,
		       "monitor collection.  this plugin will check the dependencies of this record collection to see if the collection is ready",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null )
    };


    AnalysisEngine engine;
    RecordCollection monitoredcollection;
    private Timer timer;


    public PluginArg[] getPluginArguments() {
	return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) {
	this.engine = engine;

	monitoredcollection = (RecordCollection)
	    args.get( MONITOR_COLLECTION_NAME_ARG );

	timer = new Timer();
	timer.schedule( new MyCheckReady(), 0, 1000 );
    }

    public void stop() {
	timer.cancel();
    }


    class MyCheckReady extends TimerTask {

	public void run() {
	    //System.err.println( "CheckIsReady.TimerTask" );

	    List dep = (List)monitoredcollection.getAttribute( "dependency" );
	    if( dep == null )
		return;

	    boolean isReady = true;

	    Iterator iter = dep.iterator();
	    while( iter.hasNext() ) {
		RecordCollection depCollection = (RecordCollection)iter.next();
		
		String r = (String)depCollection.getAttribute( "isReady" );
		if(( r == null ) || (!r.equals( "true" ))) {
		    isReady = false;
		    break;
		}
	    }

	    if( isReady ) {
		monitoredcollection.setAttribute( "isReady", "true" );
		timer.cancel();
	    }

	}
    }
}
