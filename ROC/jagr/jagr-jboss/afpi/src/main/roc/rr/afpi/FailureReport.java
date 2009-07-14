/* 
 * $Id: FailureReport.java,v 1.1 2004/06/08 13:33:04 emrek Exp $ 
 *
 * FailureReport.java.
 *
 */
package roc.rr.afpi;

import java.util.*;
import java.io.*;

public class FailureReport implements java.io.Serializable
{

    /*
     * servlet report -> reports what URL/servlet appears to be failing,
     *                   as seen from a client's perspective
     * ejb report -> reports what EJBs appear to be failing internally
     *
     */
    static final int TYPE_UNSPECIFIED_REPORT = 0;
    static final int TYPE_SERVLET_REPORT = 1;
    static final int TYPE_EJB_REPORT = 2;

    /*
     * string names of each type of report.  this is used by 
     * both the toString() method, and the read() method
     */
    static final String[] REPORT_TYPE_STRINGS = {
	"unspecified", "servlet","ejb" };

    /*
     * reportType specifies whether this failure report
     */
    int _reportType;

    /*
     * servletName specified in this FailureReport.
     */
    String _servletName = null;

    /*
     * error type specified in this FailureReport.
     */
    String _errorType = null;

    Set _ejbNames;

    /*
     * timeStamp when this FailureReport was received.
     */
    long _timeStamp = 0;

    /*
     * empty constructor.
     */
    public FailureReport()
    { 
	//do nothing 
    }

    /*
     * constructor. for later use.
     * 
     * @param servletName
     */
    public FailureReport(String servletName)
    {
	_servletName = servletName;
	_reportType = TYPE_SERVLET_REPORT;
    }

    /*
     * constructor. for later use.
     * 
     * @param servletName
     * @param errorType
     */
     public FailureReport(String servletName, String errorType)
    {
	_servletName = servletName;
	_errorType = errorType;
	_reportType = TYPE_SERVLET_REPORT;
    }
    

    public FailureReport( Set ejbNames ) {
	_ejbNames = ejbNames;
	_reportType = TYPE_EJB_REPORT;
    }

    public boolean isServletFailureReport() {
	return _reportType == TYPE_SERVLET_REPORT;
    }

    public boolean isEJBFailureReport() {
	return _reportType == TYPE_EJB_REPORT;
    }

    /* returns the name of the EJBs contained in this failure report
     */
    public Set getEJBNames() {
	return _ejbNames;
    }
    
    /*
     * reads from String failure report and reflects it to this
     * instance.
     *
     * @param contents String failure report. Should be "servletName;errorType" 
     *                 or "servletName" pattern.
     */
    public void read(String contents) throws IOException, ClassNotFoundException{
	StringTokenizer st = new StringTokenizer(contents, ";");
	String type;
	if(st.hasMoreTokens()) {
	    type = st.nextToken().trim();

	    if( type.equals( REPORT_TYPE_STRINGS[TYPE_SERVLET_REPORT] )) {
		_reportType = TYPE_SERVLET_REPORT;
		if(st.hasMoreTokens())
		    _servletName = st.nextToken().trim();
		if(st.hasMoreTokens())
		    _errorType = st.nextToken().trim();
	    }
	    else if( type.equals( REPORT_TYPE_STRINGS[TYPE_EJB_REPORT] )) {
		_reportType = TYPE_EJB_REPORT;
		_ejbNames = new HashSet();
		if(st.hasMoreTokens()) {
		    String ejbList = st.nextToken().trim();
		    StringTokenizer ejbst = new StringTokenizer(ejbList,",");
		    while( ejbst.hasMoreTokens() ) {
			_ejbNames.add( ejbst.nextToken().trim() );
		    }
		}
	    }
	    else {
		throw new IOException( "Unrecognized report type: " +  type );
	    }
	    
	    if(st.hasMoreTokens())
		throw new IOException("Too many tokens for failure report");
	}
	else {
	    throw new IOException( "no report type specified -- empty failure report!" );
	}
    }
    
    /*
     * add time stamp when this FaiureReport got reported.
     * 
     * @param timeStamp long value for timestamp.
     */
    public void addTimeStamp(long timeStamp){
	_timeStamp = timeStamp;
    }
    
    /**
     * getter method for servletName.
     */
    public String getServletName(){
	return _servletName;
    }
    
    /**
     * getter method for errorType.
     */
    public String getErrorType(){
	return _errorType;
    }

    /**
     * getter method for timestamp.
     */
    public long getTimeStamp(){
	return _timeStamp;
    }
    
    /**
     * toString() method for this class.
     */
    public String toString()
    {
	if(_errorType == null)
	    return new String("failure for servlet " + _servletName + " (tstamp = " + _timeStamp + ")");
	else
	    return new String("Report type " + REPORT_TYPE_STRINGS[_reportType] + "Failure type " + _errorType + " to the servlet " + _servletName
			      + " at " + _timeStamp);
    }
}

