//import Msg;
//import Stub;
//import Brick;
//import javax.jms.*;
import java.util.Vector;
//import com.ibm.mq.jms.*;

import java.net.*;
import java.util.Enumeration;

public class Start {

	static public String REQUESTS = "R";
	static public String REPLIES = "P";
	static public String HEART = "H";

    public static void main(String[] args) {


		boolean isBrick = false;
		int ID = 0;
		if (args.length > 0) {
			isBrick = args[0].equals("b");
			ID = Integer.parseInt(args[1]);
			//ID += 2;
		}
		int WQ = 2;
		int W = 3;
		int R = 2;
		// the following parameters are for faulty brick modeling
		boolean faultybrick = false;
		long delay = 1000;
		boolean recover = false;

		int statesize = 1000;
		int numthreads = 1;
		int numfaultybricks = 0;
		int timeout = 200;
		boolean windowon = true;
	
		String JMShost = "localhost:2506";
		if (args.length >2) {
			JMShost = args[2];
		}

		
	if (isBrick) {
		if (args.length >3) {
			faultybrick = args[3].equals("f");	
		}	

		if (args.length > 4) {
			delay = Long.parseLong(args[4]);
		}
		
		if (args.length > 5) {
			numfaultybricks = Integer.parseInt(args[5]);	
		}
		if (args.length > 6) {
			Brick.trashing = false;
			System.out.println("NOT TRASHING!!");
		}
	}
	else {
		if (args.length >3) {
			statesize = Integer.parseInt(args[3]);		
		}
		if (args.length > 4) {
			numthreads = Integer.parseInt(args[4]);
		}
		
		if (args.length > 5) {
			W = Integer.parseInt(args[5]);			
		}
		
		if (args.length > 6) {
			WQ = Integer.parseInt(args[6]);
		}
	
		if (args.length > 7) {
			R = Integer.parseInt(args[7]);		
		}			
		if (args.length > 8) {
			timeout = Integer.parseInt(args[8]);
		}
		
		if (args.length > 9) {
			windowon = args[9].equals("window");
			BrickInfo.windowon = windowon;
			if (windowon) {
				//System.out.println("Windowing is On");
			}
			else {
				System.out.println("Windowing is OFF");
			}
		}
	}
	
	Start st = new Start();
	
	if (isBrick) {
		try {
		
			String serverhost = InetAddress.getLocalHost().getHostName();
			System.out.println("starting brick on host " + serverhost + " on port " + (2507+ID));
			Brick b = new Brick();
			// set the brick's ID and the maximum queue len

			if (numfaultybricks > ID) { 
				b.init(ID, 400, faultybrick, delay, serverhost, 8507/*+ID*/, timeout, recover);
			}
			else {
				b.init(ID, 400, false, 0, serverhost, 8507, timeout, recover);	
			}
			 
			b.go();
		}
		catch (Exception e) { System.out.println(e); }
	}
	else {
		System.out.println("i'm a stub, W, WQ, R = " + W + " " + WQ + " " + R);
		System.out.println("number of threads = " + numthreads);
		Stub s = null;
		ReaderWriter rw = null;
		try {
				String serverhost = InetAddress.getLocalHost().getHostName();
				System.out.println("starting stub on host " + serverhost);
				st = new Start();
				//TopicSubscriber	heartSubscriber = st.topicSession.createSubscriber(st.heartTopic);

				s = new Stub(ID, WQ, W, R, timeout);
				/*if (ID > 60) {
				     synchronized(st) {
				     try {
				     		st.wait(15000);
				     }
				     catch (Exception e) {}	
					}
				}*/

				//heartSubscriber.setMessageListener(s);
				//s.setTopicSession(st.topicSession2);
		
				long currenttime = System.currentTimeMillis();

				Vector v = new Vector();

				for (int i = 1; i<= numthreads; i++) {
					if (i%40 == 0) {
						s = new Stub(ID*1000000+i*10000, WQ, W, R, timeout);	
					}
					rw = new ReaderWriter();
					rw.init(s, statesize, i*10000, v);
					rw.setName("RW"+i);
					rw.start();
				}
				
				long ctime = System.currentTimeMillis();
				long ntime;
				long logicaltime;
				int numreq = 0;
				boolean inf = true;
				v.addElement(new Integer(0));
				v.addElement(new Integer(0));
				while (inf) {
					try {
						synchronized(v) {
							ntime = System.currentTimeMillis();
							logicaltime = (ntime/1000) % 100;
							Integer in = (Integer) v.elementAt(0);
							Integer failed = (Integer) v.elementAt(1);
							numreq = in.intValue();
							int numfailed = failed.intValue();
							System.out.println("\t" + logicaltime + "\t" + numreq + "\t" + numfailed);
							v.setElementAt(new Integer(0), 0);
							v.setElementAt(new Integer(0), 1);
							v.wait(1000);
						}
					}
					catch (Exception e) {
						e.printStackTrace();	
					}
					
					
				}
				
				synchronized(v) {
					while (v.size() < numthreads) {
						v.notifyAll();
						v.wait();	
					}
					long endtime = System.currentTimeMillis();
					// otherwise calculate the statistics
					Enumeration e = v.elements();
					long numrequests = 0;
					long cumReadResponseTime = 0;
					long cumWriteResponseTime = 0;
					while (e.hasMoreElements()) {
						Vector v1 = (Vector) e.nextElement();
						numrequests += ((Long) v1.elementAt(0)).longValue();
						cumReadResponseTime += ((Long) v1.elementAt(1)).longValue();
						cumWriteResponseTime += ((Long) v1.elementAt(2)).longValue();
					}


					System.out.println("Total time elapsed " + (endtime-currenttime));
					System.out.println("Total number of R requests served " + (numrequests));
					System.out.println("Total number of W requests served " + (numrequests));
					System.out.println("Number of ops served per second " + (numrequests*2)/((endtime-currenttime)/1000.0));
					
					
					System.out.println("Average read response time: " + cumReadResponseTime/(numrequests));
					System.out.println("Average write response time " + cumWriteResponseTime/(numrequests));
					System.exit(0);
				}
				
		}
		catch (Exception e) { System.out.println(e); }
		
		
	}
}


}
