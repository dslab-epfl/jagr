package roc.loadgen;

import java.io.Serializable;

public abstract class Request implements Serializable {

    int id;
    int engineId;
    boolean newSession;

    protected Request() {
        newSession = false;
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

    public boolean isNewSession() {
        return newSession;
    }

    public void setNewSession( boolean newSession ) {
        this.newSession = newSession;
    }

    // empty for now... 

    public abstract String toString();

}
