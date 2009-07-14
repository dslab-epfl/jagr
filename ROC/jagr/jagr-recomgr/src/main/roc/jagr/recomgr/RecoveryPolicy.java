package roc.jagr.recomgr;

public interface RecoveryPolicy {

    public void setManager( RecoveryManager mgr );

    public RecoveryAction processEvent( Event event ) throws Exception;

}
