package roc.loadgen;

import java.io.Serializable;

public abstract class Request implements Serializable 
{
    int id;
    int engineId;
    boolean firstInSession=false;
    long reqTime;  // request issued time in milisecond

    // The user session to which this request belongs
    Session session=null;

    protected Request() {
        firstInSession = false;
    }

    /**
     *  other classes should use this id in logs when printing
     *  request-specific information.
     */
    public int getId() {
	return id;
    }

    public void setId( int id ) {
	this.id = id;
    }

    public int getEngineId() {
	return engineId;
    }

    public void setEngineId( int id ) {
	this.engineId = id;
    }

    public boolean firstInSession() {
        return firstInSession;
    }

    public void setFirstInSession() 
    {
	this.session = new Session();
        this.firstInSession = true;
    }

    public long getReqTime() {
	return reqTime;
    }

    public void setReqTime(long reqTime) {
	this.reqTime = reqTime;
    }

    public void setParentSession( Session s ) { this.session = s; }
    public Session getParentSession()         { return session;   }

    public abstract String toString();

}
