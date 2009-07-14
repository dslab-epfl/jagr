package roc.jagr.recomgr.policy;

import roc.jagr.recomgr.*;

public class NullPolicy implements RecoveryPolicy {

    RecoveryManager mgr;

    public NullPolicy() {
    }

    public void setManager( RecoveryManager mgr ) {
	this.mgr = mgr;
    }

    public RecoveryAction processEvent( Event event ) {
	mgr.logInfo( this, "Ignoring Event: " + event );	
	return null;
    }

}
