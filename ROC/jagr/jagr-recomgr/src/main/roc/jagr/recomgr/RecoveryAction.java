package roc.jagr.recomgr;

public abstract class RecoveryAction {

    protected RecoveryManager mgr;

    public RecoveryAction( RecoveryManager mgr ) {
	this.mgr = mgr;
    }

    public abstract void doAction() throws Exception;
    
}
