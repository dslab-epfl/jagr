package roc.rr.afpi;

/**
 *  Application Fault Injector Interface is used for injecting application specific faults
 *
 *  $Id: ApplicationFaultInjector.java,v 1.1 2004/08/25 19:50:48 skawamo Exp $
 */



public interface ApplicationFaultInjector {
    public String TYPE_NONE      = "NONE";
    public String TYPE_NULL      = "NULL";
    public String TYPE_BOGUS     = "BOGUS";
    public String TYPE_INCREMENT = "INCREMENT";
    public String TYPE_DECREMENT = "DECREMENT";
    public String TARGET_PKS[] 
	= { "BidPK", 
	    "BuyNowPK", 
	    "CategoryPK",
	    "CommentPK",
	    "IDManagerPK", 
	    "ItemPK", 
	    "RegionPK", 
	    "UserPK" };
    public String TARGET_SESSIONS[]
	= { "SessionAttribute",
	    "SessionState" };

    /**
     *  Set fault. 
     *
     *  @param target  fault injection target 
     *  @param type    fault type
     */
    public void set(String target, String type, int numOfCorruptions);

    /**
     *  Get fault 
     *
     *  @param target  fault injection target 
     *  @param type    fault type
     */
    public String get(String target);
}
