import java.util.*;
import java.net.*;

/**
 * Parses simple SQL statements to extract the tables names. 
 * Currently supports SELECT and UPDATE. 
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: TcpClients.java,v 1.1 2002/11/15 02:36:11 mikechen Exp $
 */ 


public class TcpClients extends Thread {
    
    public TcpClients(String host, int port) throws Exception{
	Socket s = new Socket(host, port);
    }
    
    public void run() {
	System.out.println("client connected");
	try {
	    sleep(10000);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	System.out.println("client done");
    }

    public static void main(String[] args) throws Exception {
	System.out.println("starting!");

	//// remember the server instances so they don't get garbage collected
	List servers = new ArrayList();
	String host = args[0];
	int port   = Integer.parseInt(args[1]);
	int numClients = Integer.parseInt(args[2]);

	for (int i = 0; i < numClients; i++) {
	    new TcpClients(host, port).start();
	}
	System.out.println("main() done!");
    }
}
