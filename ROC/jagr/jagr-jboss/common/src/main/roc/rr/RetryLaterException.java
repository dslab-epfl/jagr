package roc.rr;

/**
 * This type of exception is thrown when a method call fails and
 * caller needs to retry later.
 *
 * @author <a href="candea@stanford.edu">George Candea</a>
 * @version $Revision: 1.1 $
 *
 */
public class RetryLaterException extends Exception
{
    protected int nanoseconds = 0;

    public RetryLaterException()
    {
	super();
    }

    public RetryLaterException( int waitPeriod )
    {
	super();
	this.nanoseconds = waitPeriod;
    }

    public int getWaitPeriod ()
    {
	return nanoseconds;
    }
}
