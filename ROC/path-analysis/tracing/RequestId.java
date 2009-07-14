package tracing;

import java.util.*;

/**
 * Generates unique IDs for requests.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: RequestId.java,v 1.1 2002/11/13 03:22:54 mikechen Exp $
 */ 


public class RequestId {
    static Random rand = new Random();

    public static String getId() {
	return System.currentTimeMillis() + "_" + Math.abs(rand.nextInt());
    }
}


