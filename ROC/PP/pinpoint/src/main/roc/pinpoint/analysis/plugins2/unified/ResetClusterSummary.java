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
import roc.pinpoint.analysis.clustering.*;
import roc.pinpoint.analysis.structure.*;

public class ResetClusterSummary implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.", 
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
    };
    
    RecordCollection inputCollection;
    RecordCollection outputCollection;

    public PluginArg[] getPluginArguments() {
        return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
        throws PluginException {

        inputCollection = (RecordCollection)
            args.get( INPUT_COLLECTION_NAME_ARG );
        outputCollection = (RecordCollection)
            args.get( OUTPUT_COLLECTION_NAME_ARG );

        inputCollection.registerListener( this );
    }

    public void stop() {
        inputCollection.unregisterListener( this );
    }

    public void addedRecord( String collectionName, Record rec ) {

        Set clusters = (Set)rec.getValue();

        Set newClusters = resetClusters( clusters );
        
        TreeSet sortedClusters = new TreeSet( newClusters );

        outputCollection.setRecord( rec.getAttribute( "key" ), 
                                    new Record( sortedClusters ));
    }

    private Set resetClusters( Set clusters ) {
        Set ret = new HashSet( clusters.size() );
        
        Iterator iter = clusters.iterator();
        while( iter.hasNext() ) {
            Cluster c = (Cluster)iter.next();
            List allel = c.getAllElements();
            LockedComponentBehavior lcb = new LockedComponentBehavior( allel.toArray() );
            Cluster newC = new Cluster( lcb, (ArrayList)allel );
            ret.add( newC );
        }

        return ret;
    }

    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }
}
