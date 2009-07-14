package edu.rice.rubis.beans.servlets;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.io.Serializable;
import roc.rr.afpi.ApplicationFaultInjector;

/**
 *  SessionFaultInjector corrupts session state. 
 *
 * @author <a href="mailto:skawamo@stanford.edu">Shinichi Kawamoto</a>
 */
public class SessionFaultInjector implements ApplicationFaultInjector,
    Serializable {
    static private InitialContext initialContext = null;
    static private HashMap typeMap = null;
    static private HashMap countMap = null;

    static {
	typeMap = new HashMap();
	countMap = new HashMap();
	try {
	    initialContext = new InitialContext();
	} catch (Exception e) {
	    System.out.println("Can't initialize Context");
	}
    
	// initialize maps
	for(int i=0;i<TARGET_SESSIONS.length;i++) {
	    typeMap.put(TARGET_SESSIONS[i], TYPE_NONE);
	    countMap.put(TARGET_SESSIONS[i], new Integer(0));
	}

	SessionFaultInjector sfi = new SessionFaultInjector();

	try {
	    initialContext.bind("SessionFaultInjector", sfi);
	} catch (Exception e) {
	    try {
		initialContext.rebind("SessionFaultInjector", sfi);
	    } catch (Exception ie) {
		System.out.println("[SessionFaultInjector] Failed to register SessionFaultInjector: "+ie);
	    }
	}
    }


    public void set(String target, String type, int numOfCorruptions) {
	
	typeMap.put(target, type);
	countMap.put(target, new Integer(numOfCorruptions));
	Debug.println("[SessionFaultInjector.set] type: "+typeMap+" "+target+"/"+type);
    }


    public String get(String target) {
	return (String)typeMap.get(target);
    }

    public static synchronized String getUserId (Integer id) {
	String value;
	Integer countObj = (Integer)countMap.get("SessionState");
	int counter = countObj.intValue();
	if (counter > 0) {
	    String faultType = (String)typeMap.get("SessionState");
	    if ( faultType == null || faultType.equals(TYPE_NONE) ) {
		value = id.toString();
	    } else if ( faultType.equals(TYPE_NULL) ) {
		value = null;
	    } else if ( faultType.equals(TYPE_BOGUS) ) {
		value = "-1";
	    } else if ( faultType.equals(TYPE_INCREMENT) ) {
		value = String.valueOf(id.intValue()+1);
	    } else if ( faultType.equals(TYPE_DECREMENT) ) {
		value = String.valueOf(id.intValue()-1);
	    } else {
		System.out.println("[SessionFaultInjector] Not supported type: "+faultType);
		value = id.toString();
	    }

	    --counter;
	    countMap.put("SessionState",new Integer(counter));
	    if ( counter == 0 ) {
		typeMap.put("SessionState",TYPE_NONE);
	    }
	} else {
	    value = id.toString();
	}
	
	Debug.println("[SessionFaultInjector.getUserId] Change SessionState from "+id+" to "+value+". Rest of count: "+counter);

	return value;
    }

    public static void corruptSessionAttribute(HttpSession session,
					       SessionState ss) {
	SessionState value;
	Integer countObj = (Integer)countMap.get("SessionAttribute");
	int counter = countObj.intValue();
	Debug.println("[corruptSessionAttribute] counter: "+counter
			   +" type: "+typeMap.get("SessionAttribute"));
	if (counter > 0) {
	    String faultType = (String)typeMap.get("SessionAttribute");
	    if ( faultType == null || faultType.equals(TYPE_NONE) ) {
		return;
	    } else if ( faultType.equals(TYPE_NULL) ) {
		value = null;
	    } else if ( faultType.equals(TYPE_BOGUS) ) {
		value = new SessionState();
		value.setUserId(new Integer(-1));
		value.setItemId("-1");
		value.setURL("bogus");
		value.setTo("-1");
	    } else if ( faultType.equals(TYPE_INCREMENT) ) {
		value = new SessionState();
		Integer id = ss.getUserId();
		value.setUserId(new Integer(id.intValue()+1));
	    } else if ( faultType.equals(TYPE_DECREMENT) ) {
		value = new SessionState();
		Integer id = ss.getUserId();
		value.setUserId(new Integer(id.intValue()-1));
	    } else {
		System.out.println("[SessionFaultInjector] Not supported type: "+faultType);
		return;
	    }

	    --counter;
	    countMap.put("SessionAttribute",new Integer(counter));
	    if ( counter == 0 ) {
		typeMap.put("SessionAttribute",TYPE_NONE);
	    }

	    session.setAttribute("SESSIONSTATE",value);
	    Debug.println("[SessionFaultInjector.corruptSessionAttribute] Change SessionAttribute from "+ss+" to "+value+". Rest of count: "+counter);

	}

	return;
    }
}
