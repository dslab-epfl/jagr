import java.util.*;
import java.net.*;

/**
 * Binds to a range of server sockets.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: BindServerPort.java,v 1.1 2002/11/15 01:17:33 mikechen Exp $
 */ 


public class BindServerPort {
    
    public BindServerPort(int port) throws Exception{
	ServerSocket ss = new ServerSocket(port);
    }

    public static void usage() {
	System.out.println("usage:   java BindServerPort <starting port> <ending port>");
	System.out.println("example: java BindServerPort 2000 2020");
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 2) {
	    usage();
	    System.exit(-1);
	}


	System.out.println("starting!");

	//// remember the server instances so they don't get garbage collected
	List servers = new ArrayList();
	int startingPort = Integer.parseInt(args[0]);
	int endingPort   = Integer.parseInt(args[1]);
	System.out.println("binding port: " + startingPort + " to " + endingPort);

	for (int i = startingPort; i < endingPort; i++) {
	    System.out.println("binding port: " + i);
	    ServerSocket ss = new ServerSocket(i);
	    servers.add(ss);
	    System.out.println("# of server sockets: " + servers.size());
	    Thread.sleep(300);
	}
	System.out.println("waiting for 10s before exiting");
	Thread.sleep(10000);
	System.out.println("done!");
    }
}
