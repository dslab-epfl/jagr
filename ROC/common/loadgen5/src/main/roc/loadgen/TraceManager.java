package roc.loadgen;

import java.util.ArrayList;
import java.util.HashMap;

import roc.loadgen.TraceReader;
import roc.loadgen.Trace;

public class TraceManager 
{
    HashMap traces;

    TraceManager()
    {
	traces = new HashMap();
    }
    
    public TraceReader getTraceReader(String filename)
    {
	Trace retTrace = (Trace)traces.get(filename);
	return ((Trace)traces.get(filename)).newReader();
    }
    
    public void loadNewTrace(String filename, String traceclass)
    {
	if(traces.containsKey(filename)) return;
	try{
	    Class traceClass = Class.forName(traceclass);
	    Trace newTrace = (Trace)traceClass.newInstance();
	    newTrace.setFilename(filename);
	    newTrace.loadTrace();
	    traces.put(filename, newTrace);
	}
	catch(Exception e) { e.printStackTrace(); }
    }
}
