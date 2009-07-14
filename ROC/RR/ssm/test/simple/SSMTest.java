/* 
 * SSMTest is a test program for SSM.
 *   Compile it with -classpath .:../../ssm.jar
 *   Execute it with -classpath .:../../ssm.jar
 *
 *    Mar/17/2004  S.Kawamoto
 */

import roc.rr.ssmutil.*;


public class SSMTest {

    public static void main(String[] args) {
	try {
	    // initialize Stub
	    GlobalSSM.initialize();

	    String msg = "hello world!";
	    System.out.println("try to write data: "+msg);

	    // write data to SSM
	    String cookie = GlobalSSM.write(msg);

	    if ( cookie != null ) {
		System.out.println("write succeded! retuned cookie: "+cookie);
		System.out.println("try to read it");

		// read data from SSM
		String msg2 = (String)GlobalSSM.read(cookie);

		System.out.println("read data: "+msg2);
	    } else {
		System.out.println("failed to write data!");
	    }
	} catch (Exception e) {
	    System.out.println(e);
	}
	System.exit(0);
    }
}
