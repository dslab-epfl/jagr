/*
  GlobalSSM class
    This class is an interface to SSM.
    It wrapes Stub class. 

                 Jan/11/2004  S.Kawamoto

    Usage:
      void GlobalSSM.initialize();
         Initialize stub. It must be called before reading and writing. 
      Object GlobalSSM.read(SSMCookie);
         Extract object from SSM, which is associated with SSMCookie.
      SSMCookie GlobalSSM.write(Object);
         Store object into SSM. Return value is SSMCookie. 

  $Id: GlobalSSM.java,v 1.7 2004/08/24 23:47:05 emrek Exp $
*/

package roc.rr.ssmutil;

import java.util.Vector;
import java.net.InetAddress;
import roc.rr.ssm.Stub;


public class GlobalSSM{
    static private Stub st=null;
    static private int key = 0;
    static private Object lock = new Object();

    public static synchronized void initialize(){
	if (st==null) {
	    Debug.println("[SSM] initialize SSM stub");
	    try {
		//get Stub id based on local host address.
		String localhost = InetAddress.getLocalHost().getHostAddress().replace('.','0');
		if( localhost.length() > 9 )
		    localhost = localhost.substring( localhost.length() - 9);
		int stubId = Integer.parseInt(localhost);

		Debug.println("[SSM] stub id is " + stubId);
		Stub stlocal = new Stub(stubId,2,3,2,30000); 
		Thread.sleep(2000);
		st = stlocal;
	    } catch (Exception e){
		System.out.println("[SSM] initialization failure");
		//		System.out.println(e);
		e.printStackTrace();
	    }
	}
    }
	
    public static synchronized void forceInitialize(){
	Debug.println("[SSM] initialize SSM stub");
	try {
	    Stub stlocal = new Stub(100,2,3,2,30000); 
	    Thread.sleep(5000);
	    st = stlocal;
	} catch (Exception e){
	    System.out.println("[SSM] initialization failure");
	    System.out.println(e);
	}
    }

    public static Object read(String cookie) throws SSMException {
	Object value=null;
	Vector ssmcookie = SSMCookie.httpCookie2ssmCookie(cookie);

	// If stub hasn't initialized yet, initialize it here
	if ( st == null ) {
	    initialize();
	}
	
	if (ssmcookie != null) {
	    Debug.println("[SSM] try to read with cookie: "+ssmcookie);
	    try {
		value=st.readWOCheck(ssmcookie);
		Debug.println("[SSM] read succeeded: "+value);
	    } catch (Exception e){
		Debug.println("[SSM] read failed: "+e);
		e.printStackTrace();
		throw new SSMException("read error");
	    }
	} else {
	    Debug.println("[SSM] cookie is null in read operation");
	}
	return value;
    }

    public static String write(Object value) throws SSMException {
	Vector newssmcookie=null;

	// If stub hasn't initialized yet, initialize it here
	if ( st == null ) {
	    initialize();
	}

	Debug.println("[SSM] try to write "+value);
	try {
	    synchronized (lock) {
		newssmcookie = st.Write(key++, (Object)value,
					System.currentTimeMillis()+1200000);
	    }
	    Debug.println("[SSM] write "+ value 
			  + " succeeded,  returned cookie: "
			  + newssmcookie);
	} catch (Exception e){
	    Debug.println("[SSM] write " + value + " failed");
	    Debug.println(e);
	    throw new SSMException("write error");
	}

	String ssmcookie = null;
	if ( newssmcookie != null ){
	    ssmcookie = SSMCookie.ssmCookie2HttpCookie(newssmcookie);
	}
	return ssmcookie;
    }
}

