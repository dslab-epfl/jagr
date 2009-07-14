/*****************************************************************
 * FailureReport.java - FailureReport objects are sent by restart agents
 *
 * $Id: FailureReport.java,v 1.1 2003/09/20 04:24:08 steveyz Exp $
 *
 *****************************************************************/

package roc.rr.rm;

import java.io.*;
import java.util.Date;

public class FailureReport implements java.io.Serializable
{
    int failureType; // point or correlated?
    static final int failureTypeMin = 0;
    static final int failureTypePointNode = 1;
    static final int failureTypeCorrelatedNodes = 2;
    static final int failureTypeEndToEnd = 3;
    static final int failureTypeMax = 4;

    String srcNode;
    String dstNode; // ignored for point failures
    Date timeStamp; // when was this failure noticed?

    public FailureReport()
    {
    }

    public FailureReport(Date _timeStamp)
    {
        failureType = failureTypeEndToEnd;
        timeStamp = _timeStamp;
    }
    
    public FailureReport(String node, Date _timeStamp)
    {
        failureType = failureTypePointNode;
        srcNode = node;
        timeStamp = _timeStamp;
    }
    
    public FailureReport(String _srcNode, String _dstNode, Date _timeStamp)
    {
        failureType = failureTypeCorrelatedNodes;
        srcNode = _srcNode;
        dstNode = _dstNode;
        timeStamp = _timeStamp;
    }

    public String toString()
    {
        if(failureType == failureTypePointNode)
        {
            return new String("Point Failure of Node " + srcNode + " at " 
                              + timeStamp.toString());
        }
        else if (failureType == failureTypeCorrelatedNodes)
        {
            return new String("Correlated Failure of Nodes " + srcNode
                              + " --> " + dstNode + " at " + timeStamp.toString());
        }
        else if (failureType == failureTypeEndToEnd)
        {
            return new String("End to End Failure at " + timeStamp.toString());
        }
        else
        {
            return new String("Invalid FailureReport!");
        }
    }
}
