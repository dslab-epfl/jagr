package roc.pinpoint.analysis.plugins.anomaly;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author emrek
 *
 */
class ComponentInfo {
    Map attrs;

    // indexed by <Map of sinkAttrs>.  values are StatInfo.
    Map links;
    
    // indexed by <Map of srcExtraAttrs>, values are Map <Map of sinkAttrs, StatInfo>.
    Map subComponents;

    ComponentInfo() {
	links = new HashMap();
	subComponents = new HashMap();
    }

    void addLinkInfo(Map srcExtraAttrs, Map sinkAttrs, StatInfo statInfo ) {
        // update the gross component statistics
        StatInfo majorStats = (StatInfo)links.get( sinkAttrs );
	if( majorStats == null ) {
	    majorStats = new StatInfo();
	    links.put( sinkAttrs, majorStats );
	}

	/**
	System.err.println( "BEFORE ADDLINKINFO: majorStats = " 
			    + majorStats.toString() );
	System.err.println( "BEFORE ADDLINKINFO: statInfo = " 
			    + statInfo.toString() );
	**/

        majorStats.addStatInfo( statInfo );

	/*
		System.err.println( "AFTER ADDLINKINFO: majorStats = "
			    + majorStats.toString() );
	*/
			    
        Map subComponentStats = (Map)subComponents.get( srcExtraAttrs );

	if( subComponentStats == null ) {
	    subComponentStats = new HashMap();
	    subComponents.put( srcExtraAttrs, subComponentStats );
	}

        StatInfo minorStats = (StatInfo)subComponentStats.get( sinkAttrs );
 	if( minorStats == null ) {
	    minorStats = new StatInfo();
	    subComponentStats.put( sinkAttrs, minorStats );
	}
       minorStats.addStatInfo( statInfo );
    }


}
