/*
 * $Id: FaultInjection.java,v 1.2 2004/09/20 05:07:10 candea Exp $
 */
package roc.faultinject;

import java.util.*;
import roc.rr.Action;

import org.apache.log4j.Logger;

public class FaultInjection
{
    static Logger log = Logger.getLogger( "FaultInjection" );
    
    private int     faultType; // fault type of this instance
    private String  compName;  // target component name of this fault injection
    private Date    date;      // scheduled date of this fault injection
    private Integer amount;    // number of bytes to memleak
    private String  ctype;     // data corruption type
    private Integer ctime;     // data corruption time

    /**
     * constructor
     *
     * @param faultType  fault type to be injected
     * @param compName   target component for this fault injection
     * @param date       date at which to inject the fault
     *
     */
    public FaultInjection(int faultType, String compName, Date date)
    {
	this( faultType, compName, date, new Integer(-1) );
    }

    /**
     * constructor (special for memory leaks, which take 2 params)
     *
     * @param faultType  fault type to be injected. 
     * @param ejbName    target EJB for this memleak fault injection
     * @param date       date at which to inject the fault
     * @param amount     amount of memory to leak (bytes)
     */
    public FaultInjection(int faultType, String ejbName, Date date, Integer amount ) 
    {
	this.faultType = faultType;
	this.compName  = ejbName;
	this.date      = date;
	this.amount    = amount;
    }


    /**
     * constructor (special for memory leaks, which take 2 params)
     *
     * @param faultType  fault type to be injected. 
     * @param target     target 
     * @param date       date at which to inject the fault
     * @param ctype      data corruption type
     * @param ctime      data corruption time
     */
    public FaultInjection( int faultType, String target, Date date, 
			   String ctype, Integer ctime)
    {
	this.faultType = faultType;
	this.compName  = target;
	this.date      = date;
	this.ctype     = ctype;
	this.ctime     = ctime;
    }


    /**
     * getters
     */
    public int     getFaultType() { return this.faultType; }
    public String  getCompName()  { return this.compName;  }
    public Date    getDate()      { return this.date;      }
    public Integer getAmount()    { return this.amount;    }
    public String  getCtype()     { return this.ctype;     }
    public Integer getCtime()     { return this.ctime;     }

    /**
     * convert instance to a String
     */
    public String toString() 
    {
	String ret = "Scheduled ";

	if (      faultType == Action.INJECT_THROWABLE )  ret += "Throwable";
	else if ( faultType == Action.MICROREBOOT )       ret += "Microreboot";
	else if ( faultType == Action.DEADLOCK )          ret += "Deadlock";
	else if ( faultType == Action.NO_ACTION )         ret += "Cancel";
	else if ( faultType == Action.FULL_REBOOT )       ret += "Full reboot";
	else if ( faultType == Action.END_OF_CAMPAIGN )   ret += "End of campaign";
	else if ( faultType == Action.INJECT_MEMLEAK )    ret += "Mem Leak (" + amount + " bytes/call)";
	else if ( faultType == Action.UNBIND_NAME )       ret += "Unbind Name" ;
	else if ( faultType == Action.SET_NULL_TXINT )    ret += "Null Map" ;
	else if ( faultType == Action.INFINITE_LOOP  )    ret += "Infinite Loop" ;
	else if ( faultType == Action.CORRUPT_JNDI )      ret += "JNDI Corruption / "+ctype;
	else if ( faultType == Action.CORRUPT_DATA )      ret += "DATA Corruption / "+ctype+" / "+ctime;
	else return "Unknown";
	ret += "/" + compName;
	ret += "/" + date;

	return ret;
    }
}
