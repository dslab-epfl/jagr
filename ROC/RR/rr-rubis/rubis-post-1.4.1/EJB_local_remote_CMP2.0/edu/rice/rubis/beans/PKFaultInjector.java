package edu.rice.rubis.beans;
import javax.naming.InitialContext;
import java.util.HashMap;
import java.io.Serializable;
import roc.rr.afpi.ApplicationFaultInjector;
import edu.rice.rubis.beans.Debug;

/**
 *  PKFaultInjector corrupts PK value. It refers to JNDI variables, 
 *   BidPK, BuyNowPK, CategoryPK, CommentPK, IDManagerPK, ItemPK, 
 *   RegionPK, or UserPK according to the argument pkClass. If it
 *   is registerd to JNDI name space, then PKFaultInjector returns 
 *   the corrupted primary key. 
 *
 * @author <a href="mailto:skawamo@stanford.edu">Shinichi Kawamoto</a>
 *
 */
public class PKFaultInjector implements ApplicationFaultInjector,
    Serializable {
    static private InitialContext initialContext = null;
    static private HashMap typeMap = null;
    static private HashMap countMap = null;

    // initialize PKFaultInjector
    static {
	typeMap = new HashMap();
	countMap = new HashMap();
	try {
	    initialContext = new InitialContext();
	} catch (Exception e) {
	    System.out.println("Can't initialize Context");
	}
    
	// initialize maps
	for(int i=0;i<TARGET_PKS.length;i++) {
	    typeMap.put(TARGET_PKS[i], TYPE_NONE);
	    countMap.put(TARGET_PKS[i], new Integer(0));
	}

	PKFaultInjector pkfi = new PKFaultInjector();
	try {
	    initialContext.bind("PKFaultInjector", pkfi);
	} catch (Exception e) {
	    try {
		initialContext.rebind("PKFaultInjector", pkfi);
	    } catch (Exception en) {
		System.out.println("[PKFaultInjector] Failed to register PKFaultInjector: "+en);
	    }
	}
    }

    // implementation of set method 
    public void set(String target, String type, int numOfCorruptions) {
	typeMap.put(target, type);
	countMap.put(target, new Integer(numOfCorruptions));
    }

    // implementation of get method
    public String get(String target) {
	return (String)typeMap.get(target);
    }

    // generate faulty primary key 
    public static synchronized Integer getId (String target, Integer id) {
	Integer value;
	Integer countObj = (Integer)countMap.get(target);
	int counter = countObj.intValue();
	if (counter > 0) {
	    String faultType = (String)typeMap.get(target);
	    if ( faultType == null || faultType.equals(TYPE_NONE) ) {
		value = id;
	    } else if ( faultType.equals(TYPE_NULL) ) {
		value = null;
	    } else if ( faultType.equals(TYPE_BOGUS) ) {
		value = new Integer(100000000);
	    } else if ( faultType.equals(TYPE_INCREMENT) ) {
		value = new Integer(id.intValue()+1);
	    } else if ( faultType.equals(TYPE_DECREMENT) ) {
		value = new Integer(id.intValue()-1);
	    } else {
		System.out.println("[PKFaultInjector] Not supported type: "+faultType);
		value = id;
	    }

	    --counter;
	    countMap.put(target,new Integer(counter));
	    if ( counter == 0 ) {
		typeMap.put(target,TYPE_NONE);
	    }
	} else {
	    value = id;
	}
	
	Debug.println("[PKFaultInjector] Change PK from "+id+" to "+value+". Rest of count: "+counter);

	return value;
    }
}
