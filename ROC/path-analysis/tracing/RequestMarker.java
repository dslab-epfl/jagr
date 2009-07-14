package tracing;

import java.util.*;

/**
 * Id and properties associated with a request.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: RequestMarker.java,v 1.3 2002/11/26 00:07:01 mikechen Exp $
 */ 


public class RequestMarker {
    String requestId;
    int seqNum = -1;
    Map    map  = new HashMap();
    List   list = new ArrayList();
    
    public String getRequestId() {
	return requestId;
    }
    
    public void setRequestId(String requestId) {
	this.requestId = requestId;
    }
    
    public void setSeqNum(int seqNum) {
	this.seqNum = seqNum;
    }
    
    public int incrementSeqNum() {
	seqNum++;
	return seqNum;
    }
    
    public int getSeqNum() {
	return seqNum;
    }
    
    public Map getRequestMap() {
	return map;
    }
    
    public List getRequestList() {
	return list;
    }
}


