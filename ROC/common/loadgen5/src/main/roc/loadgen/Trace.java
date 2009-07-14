package roc.loadgen;

import java.util.ArrayList;

import roc.loadgen.SessionAction;
import roc.loadgen.TraceReader;

public abstract class Trace {

    protected String filename;
    protected ArrayList trace;

    protected abstract void loadTrace();

    public SessionAction getAction(int index)
    {
	return (SessionAction)trace.get(index);
    }

    public TraceReader newReader()
    {
	return new TraceReader(this);
    }

    public void setFilename(String filename)
    {
	this.filename = filename;
    }

    public int getLength()
    {
	return trace.size();
    }
}
