/**
 * $Id: SimpleMicrorebootPolicy.java,v 1.4 2004/07/29 02:00:59 candea Exp $
 **/

package roc.jagr.recomgr.policy;

import java.util.*;

import roc.jagr.recomgr.*;
import roc.jagr.recomgr.event.FailureReportEvent;
import roc.jagr.recomgr.action.MicrorebootAction;

import org.apache.log4j.Logger;

public class SimpleMicrorebootPolicy implements RecoveryPolicy 
{
    static private Logger log = Logger.getLogger( "SimpleMicrorebootPolicy" );

    RecoveryManager mgr;
    private long thresholdMillis;

    private Hashtable recoveredComps;

    public SimpleMicrorebootPolicy() 
    {
	recoveredComps = new Hashtable();
	this.thresholdMillis = 30*1000; // hardcoded 30 sec
	log.info( "started" );
    }

    public void setManager( RecoveryManager mgr ) 
    {
	this.mgr = mgr;
    }

    public RecoveryAction processEvent( Event event ) 
	throws Exception
    {
	log.debug( "Processing event " + event );

	if( event instanceof FailureReportEvent ) 
	{
	    FailureReportEvent fre = (FailureReportEvent)event;
	    FailureReport fr = fre.getReport();
	    
	    Set rebootTargets = new HashSet();
	    Iterator iter = fr.getSuspects().iterator();
	    while( iter.hasNext() ) 
	    {
		FailureReport.Suspect s = (FailureReport.Suspect)iter.next();
		String name = s.getName();

		Long lastReco = (Long) recoveredComps.get( name );
		long currentTime = event.getTimeStamp();
		if( lastReco != null )
		{
		    long lastRecoTime = lastReco.longValue();
		    if( lastRecoTime < currentTime - thresholdMillis )
		    {
			recoveredComps.remove( name );
			rebootTargets.add( name );
			recoveredComps.put( name, new Long(currentTime) );
		    }
		}
		else
		{
		    rebootTargets.add( name );
		    recoveredComps.put( name, new Long(currentTime) );
		}
	    }

	    if( rebootTargets.isEmpty() )
		return null;
	    else
		return new MicrorebootAction( mgr, rebootTargets );
	}
	else {
	    return null;
	}
    }

}
