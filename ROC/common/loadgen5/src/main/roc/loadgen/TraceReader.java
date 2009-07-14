package roc.loadgen;

import roc.loadgen.Trace;
import roc.loadgen.SessionAction;

public class TraceReader {

    private Trace trace;
    private int index;

    public TraceReader(Trace trace)
    {
	this.trace = trace;
	index = 0;
    }

    public SessionAction getNextAction()
    {
	return trace.getAction(index++);
    }

    public void reset()
    {
	index = 0;
    }

    public boolean hasNext()
    {
	return (index < trace.getLength());
    }

    public int getLength() { return trace.getLength(); }
}
