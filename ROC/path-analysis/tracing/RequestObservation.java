package tracing;

import java.io.*;
import java.util.*;
import javax.jms.*;

/**
 * A JMS message that contains the basic observations about a request.
 * No complex data structures are used to reduce serialization overhead.   
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: RequestObservation.java,v 1.3 2002/11/26 00:07:01 mikechen Exp $
 */ 


public class RequestObservation implements Serializable {
    public String  requestId;
    public int     seqNum;
    public int     returnSeqNum = -1;
    public String  location;
    public String  name;
    public long    timestamp;
    public long    latency = -1;
    public boolean success;
    
    public RequestObservation() {
    }

    public RequestObservation(String requestId, int seqNum, String location,
			      String name, 
			      long timestamp, long latency, boolean success) {
	this.requestId = requestId;
	this.seqNum    = seqNum;
	this.location  = location;
	this.name      = name;
	this.timestamp = timestamp;
	this.latency   = latency;
	this.success   = success;
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("RequestObservation: [\n");
	buf.append("    requestId: " + requestId + ",");
	buf.append("    \tseqNum:    " + seqNum + "\treturnSeqNum:    " + returnSeqNum + " ");
	buf.append("    location:  " + location + ",");
	buf.append("    \tname:      " + name + " \n");
	buf.append("    timestamp: " + timestamp + " \n");
	if (latency >= 0) {
	    buf.append("    latency:   " + latency + " \n");
	    buf.append("    success:   " + success + " \n");
	}
	buf.append("]");
	return buf.toString();
    }


    //Map    map  = new HashMap();
    //List   list = new ArrayList();
    
    /*
    public Map getRequestMap() {
	return map;
    }
    
    public List getRequestList() {
	return list;
    }
    */
}


