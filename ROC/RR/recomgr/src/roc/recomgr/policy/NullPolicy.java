package roc.recomgr.policy;

import roc.recomgr.*;

public class NullPolicy implements RecoveryPolicy {

    RecoveryManager mgr;

    public NullPolicy() {
    }

    public void setManager( RecoveryManager mgr ) {
	this.mgr = mgr;
    }

    public RecoveryAction processEvent( Event event ) {
	return null;
    }

}
