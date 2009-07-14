package roc.loadgen.rubis;

public class Operation
{
    public String type;
    public long    start;
    public long    end;
    public boolean isOK;  // true iff operation was successful

    public Operation( String opType, long start, long end, boolean isOK )
    {
	this.type = opType;
	this.start = start;
	this.end   = end;
	this.isOK  = isOK;
    }

    public Operation( RubisUserState opType, long start, long end, boolean isOK )
    {
	this( opType.getName(), start, end, isOK );
    }

    public String toString ()
    {
	return "[type:" + type + " start:" + start + " end:" + end + (isOK ? " OK]" : " FAILED]");
    }
}
