//==============================================================================
//===   PortListener.java   ===========================================================

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * <PRE>
 * PortListener.java - listens on the specified port and prints the incoming data 
 *                     to stdout.
 *
 * Created:     Dec 11, 1997
 * Author:      Mike Chen  (mikechen@cs.berkeley.edu)
 *
 * @version $Id: PortListener.java,v 1.1 2002/11/15 02:36:11 mikechen Exp $      
 * </PRE>
 *
 * @author <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A> 
 *		(<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @since   1.1.4
 * @version Version 0.1.0, 11-05-1997
 */

public class PortListener { 
  /************************************************************/
  /***********************  Constants  ************************/
  /************************************************************/
private static final String CLASS_NAME       	       	= "PortListener";
private static final String VERSION       	       	= "v1.0a1";
private static final String TITLE       	       	= CLASS_NAME + " " + VERSION;
private static final String AUTHOR       	       	= "Mike Chen";

  /************************************************************/
  /***********************  Variables  ************************/
  /************************************************************/

private ServerSocket ss;
private Socket s;
private int port;

  //===========================================================================
  //===   CONSTRUCTORS   ======================================================

  /**
   * Default constructor.  Reads from specified files.  Not used or maintained.
   */
public PortListener() {
  this.port = 8888;
}

public PortListener(int port) {
  this.port = port;
}

  /**
   * Listen for messages and process the requests.
   */

public void run() {
  System.out.println("listening on port: " + port);
  try {
    ss = new ServerSocket(port);
    while (true) {
      s = ss.accept();
      new ListenerThread(s).start();
    }
  }
  catch (Exception e) {
    System.err.println(e);      
  }
  
}

static String parseQueryWords(String header) {
  StringTokenizer parser = new StringTokenizer(header);
  // get rid of "GET"
  parser.nextToken();
  String url = parser.nextToken();
  int i = url.indexOf("=") + 1;
  return (url.substring(i).replace('+', ' '));

  /* sample request
     GET /?MT=hello+mike+ya+ya HTTP/1.0
     Referer: http://HTTP.CS.Berkeley.EDU/~mikechen/cha-cha/
     Connection: Keep-Alive
     User-Agent: Mozilla/4.03 [en] (X11; I; SunOS 5.5.1 sun4u)
     Host: u91.cs.berkeley.edu:8888
     Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg
     Accept-Language: en
     Accept-Charset: iso-8859-1,*,utf-8
     
     */
  
}

public static void main(String args[]) {
  System.out.println("===========================================");
  System.out.println("Starting " + TITLE);
  System.out.println("===========================================");
  PortListener p; 
  if (args.length == 1) {
    p = new PortListener(Integer.parseInt(args[0]));
    p.run();
  }
  else {
    p = new PortListener();
    p.run();    
  }
}        

}

class ListenerThread extends Thread {
    Socket s;

    ListenerThread(Socket s) {
	this.s = s;
    }

    public void run() {

	try {
	    System.out.println("connected... " + new Date() + "..." + s);
	    //Thread.sleep(100);
	    InputStream in = s.getInputStream();
	    byte[] buf = new byte[256];
	    int length = 0;
	    while ((length = in.read(buf)) != -1) {
		System.out.println("length: " + length);
		System.out.println("aaaaaaaaaaaasssssssscccccciiiiiiiiiiiii");
		System.out.println(new String(buf, 0, length));
		byte[] hex = new byte[length];
		System.arraycopy(buf, 0, hex, 0, length);
		System.out.println("hhhhhhhhhhhhhhhheeeeeeexxxxxxxxx\n" + toHex(hex));
	    }

	    /*
	    PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
	    System.out.println("start sending..");
	    for (int i = 0; i < 10; i++) {
		out.print("GET / HTTP/1.0\r\n\r\n");
		out.flush();
		sleep(100);
		System.out.println(i);
	    }
	    System.out.println("done sending..");
	    */

	    /*
	      while ((length = in.read(buf)) != -1) {
	      System.out.println("length: " + length);
	      System.out.println(new String(buf));
	      System.out.print(ninja.utils.Hex.fromByteArray(buf));
	      }
      */
	    in.close();
	    s.close();
	    System.out.println("disconnected...");
	    
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

public static String toHex(byte[] a) {
	StringBuffer b = new StringBuffer(3*a.length);
	for (int i=0; i<a.length; i++) {
	    if ((i&31) == 0 && i>0)
		b.append("\\\n");
	    if ((i&3) == 0 && (i&31) != 0)
		b.append(' ');
	    b.append(Integer.toHexString(a[i]+512).substring(1));
	}
	return new String(b);
    }

}

