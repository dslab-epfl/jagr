/**
 *  FaultInjectionReport is sent to recovery manager from fault injector
 *  in order to nofity type and target component of fault injection.
 *  
 * $Id: FaultInjectionReport.java,v 1.2 2004/09/11 03:15:16 skawamo Exp $
 */

package roc.recomgr;

import java.io.Serializable;

public class FaultInjectionReport implements Serializable 
{
    String hostName;   // target hostname
    String target;     // target component (ejb) or data
    int    type;       // fault type  
    String subType;    // fault subtype for jndi and data corruption


    public FaultInjectionReport( String hostName, String target, int type ) {
	this.hostName = hostName;
	this.target   = target;
	this.type     = type;
	this.subType  = null;
    }

    public FaultInjectionReport( String hostName, String target, 
				 int type, String subType)
    {
	this.hostName = hostName;
	this.target   = target;
	this.type     = type;
	this.subType  = subType;
    }

    public String getHostName() { return hostName; }
    public String getTarget()   { return target;   }
    public int    getType()     { return type;     }
    public String getSubType()  { return subType;  }

    public String toString() 
    {
	return "[host=" + hostName+" target=" + target + " type=" + type  + ", sub type=" + subType + "]";
    }
}
