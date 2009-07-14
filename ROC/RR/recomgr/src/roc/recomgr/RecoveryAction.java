package roc.recomgr;

public abstract class RecoveryAction 
{
    protected RecoveryManager mgr;

    protected String affectedServer=null;

    public RecoveryAction( RecoveryManager mgr ) {
	this.mgr = mgr;
    }

    public abstract void doAction() throws Exception;
    
    public void setAffectedServer( String server )
    {
	affectedServer = server;
    }

    public String getAffectedServer()
    {
	return affectedServer;
    }

}
