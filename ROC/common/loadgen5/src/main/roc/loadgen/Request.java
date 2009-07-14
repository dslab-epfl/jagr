package roc.loadgen;

import java.io.Serializable;

public abstract class Request implements Serializable {

    int id;

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

    // empty for now... 

    public abstract String toString();

}
