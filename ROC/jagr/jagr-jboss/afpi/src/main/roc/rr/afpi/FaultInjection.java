/*
 * $Id: FaultInjection.java,v 1.2 2004/07/23 05:54:14 candea Exp $
 */
package roc.rr.afpi;

import java.util.*;
import roc.rr.EJBInterceptor;

public class FaultInjection
{
    public static final Object END_OF_CAMPAIGN = new Object();
    public static final Object FULL_REBOOT     = new Object();
    public static final Object UNBIND_NAME     = new Object();

    private Object  faultType; // fault type of this instance
    private String  compName;  // target component name of this fault injection
    private Date    date;      // scheduled date of this fault injection
    private int     amount;    // number of bytes to memleak

    /**
     * constructor
     *
     * @param faultType  fault type to be injected
     * @param compName   target component for this fault injection
     * @param date       date at which to inject the fault
     *
     */
    public FaultInjection( Object faultType, String compName, Date date )
    {
	this( faultType, compName, date, -1 );
    }

    /**
     * constructor (special for memory leaks, which take 2 params)
     *
     * @param faultType  fault type to be injected. 
     * @param ejbName    target EJB for this memleak fault injection
     * @param date       date at which to inject the fault
     * @param amount     amount of memory to leak (bytes)
     */
    public FaultInjection( Object faultType, String ejbName, Date date, int amount ) 
    {
	this.faultType = faultType;
	this.compName  = ejbName;
	this.date      = date;
	this.amount    = amount;
    }

    /**
     * getters
     */
    public Object  getFaultType() { return this.faultType; }
    public String  getCompName()  { return this.compName;   }
    public Date    getDate()      { return this.date;      }
    public int     getAmount()    { return this.amount;    }

    /**
     * convert instance to a String
     */
    public String toString() 
    {
	String ret = "Fault: ";

	if (      faultType == EJBInterceptor.INJECT_THROWABLE )  ret += "Throwable";
	else if ( faultType == EJBInterceptor.MICROREBOOT )       ret += "Microreboot";
	else if ( faultType == EJBInterceptor.DEADLOCK )          ret += "Deadlock";
	else if ( faultType == EJBInterceptor.NO_ACTION )         ret += "Cancel";
	else if ( faultType == FULL_REBOOT )                      ret += "Full reboot";
	else if ( faultType == END_OF_CAMPAIGN )                  ret += "End of campaign";
	else if ( faultType == EJBInterceptor.INJECT_MEMLEAK )    ret += "Mem Leak (" + amount + " bytes/call)";
	else if ( faultType == UNBIND_NAME )                      ret += "Unbind Name" ;
	else if ( faultType == EJBInterceptor.SET_NULL_TXINT )    ret += "Null Map" ;
	else if ( faultType == EJBInterceptor.INFINITE_LOOP  )    ret += "Infinite Loop" ;
	else                                                      return "Unknown";
	ret += " / Comp: " + compName;
	ret += " / Date: " + date;

	return ret;
    }
}
