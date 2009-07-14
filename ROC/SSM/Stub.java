/**
 * @author bling
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.util.Vector;
import java.util.Random;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.net.*;
import java.io.*;

public class Stub {
	int _id;
	int _numLiveBricks;
	Hashtable _bricks;

	int _WQ;
	int _W;
	int _R;

	// the time we are normally willing to wait for a read/write request roundtrip
	long _timeout;
	Random _r;

	// the maximum time we are willing to wait for a read/write before we give up
	// and declare the system overloaded
	long _MAXtimeout;


	Hashtable _waitQ;
	Hashtable _inbox;

	Hashtable BrickIDtoOOMap = new Hashtable(50);

	static boolean DEBUG = false;


	public Stub(int id, int WQ, int W, int R, long timeout) {
		_id = id;

		_bricks = new Hashtable(20);		

		_numLiveBricks = 0;

		_WQ = WQ;
		_W = W;
		_R = R;
		_timeout = timeout;
		_MAXtimeout = 20000;
		_r = new Random();
		_waitQ = new Hashtable(100);
		_inbox = new Hashtable(100);
		
		HeartbeatListener hbl = new HeartbeatListener(this);
		hbl.setName("HeartbeatListnerThread");
		hbl.start();
	}
	
	private void PrintBricks() {
/*		String cur = "Current bricks: ";
		for (int i = 0; i < _numLiveBricks; i++) {
			cur += (_liveBricks[i] + " ");	
		}	
		
		String fut = _numNewLiveBricks + "NewLiveBricks: ";
		for (int k = 0; k < _numNewLiveBricks; k++) {
			fut += (_newLiveBricks[k] + " ");	
		}
	
		System.out.println(cur);
		System.out.println(fut);
*/		
	}

	public Vector Write(long key, Object val, long exp) throws SystemOverloadedException, InsufficientBricksAvailableException {
		Msg request = new Msg();
		request.setSender(_id);

		// 1. Calculate checksum for object and expiration time.
		int check = val.hashCode();
		
		// 2. Create a list of bricks L, initially the empty set.
		int[] repliedbricks = new int[_WQ];
	
		// Make the write request write of {key, object, checksum, and expiry} to each brick.
		request.MakeWriteReq(key, val, check, exp);

		// create a temporarily variable to house the replies
		//Hashtable temp = new Hashtable(_WQ);
		
		// 4. Wait for WQ of the bricks to return with success messages, 
		// or until t elapsed.  When each brick replies, add its identifier to the set L.
		Hashtable h = null;
		try {
			long cur = System.currentTimeMillis();

			boolean tryagain = true;
			h = null;
			while (tryagain) {
				try {
					h = publishRequest(request, repliedbricks);
					tryagain = false;
				}
				catch (BrickInfoIsStaleException biise) {
				}
			}			
			long aft = System.currentTimeMillis();
			//System.out.println("TIME FOR RT " + (aft-cur));
		}
		catch (RequestTimedOutException ee) 
		{
			//ee.printStackTrace();
			throw new SystemOverloadedException();
		}
		
		String out = "replied bricks ";
		Enumeration e = h.elements();
		for (int k = 0; k < _WQ; k++) {
			repliedbricks[k] = ((Msg) e.nextElement()).getSender();
			//out += repliedbricks[k];
		}
		
		//System.out.println(out);


		// 6. Create a cookie consisting of H, the identifiers of the WQ 
		// bricks that acknowledged the write, and the expiry, and 
		// calculate a checksum for the cookie.
		Vector v = new Vector(4);
		v.addElement(new Long(key));
		v.addElement(repliedbricks);
		v.addElement(new Long(exp));
		v.addElement(new Integer(repliedbricks.hashCode()));

		// 7. Return the cookie to the caller.
		return v;
	}

	public Object read(Vector v) throws SystemOverloadedException, 
	RequestTimedOutException, BadChecksumException, InsufficientBricksAvailableException {
		long key = ( (Long) v.elementAt(0) ).longValue();
		int[] repliedbricks = (int[]) v.elementAt(1);
		long exp = ( (Long) v.elementAt(2) ).longValue();
		int hash = ( (Integer) v.elementAt(3) ).intValue();
	
		// check for exp time
		if (exp < System.currentTimeMillis()) {
			System.out.println("THE STATE HAS EXPIRED");
			return null;
		}

		// Verify the checksum on the cookie
		if (repliedbricks.hashCode() != hash) {
			System.out.println("THE COOKIE HAS BEEN ALTERED! RETURNING NULL");
			return null;
		}

		Msg request = new Msg();
		request.setSender(_id);
		request.MakeReadReq(key, exp);

		long cur = System.currentTimeMillis();
		
		boolean tryagain = true;
		Hashtable h = null;
		while (tryagain) {
			try {
				h = publishRequest(request, repliedbricks);
				tryagain = false;
			}
			catch (BrickInfoIsStaleException biise) {
			}
		}

		long aft = System.currentTimeMillis();
		//System.out.println("the read took " + (aft-cur));

		Enumeration e = h.elements();

		Msg m = (Msg) e.nextElement();

		// Verify checksum and expiration.  If checksum is invalid, repeat step 2. Otherwise continue.
		Object val = m.getData();
		int check = m.getChecksum();
		if (check != val.hashCode()) {
			throw new BadChecksumException();
		}
		// return object to the caller.
		return val;
	}

	public Hashtable publishRequest(Msg r, int[] repliedbricks) throws SystemOverloadedException, 
	RequestTimedOutException, InsufficientBricksAvailableException, BrickInfoIsStaleException {
		boolean isWrite = false;
		if (r.getType() == Msg.WRITE_REQ) {
			isWrite = true;
		}


		// have to set the timeout period
		r.setTimeOut(_timeout);
		
		//Object o = new Object();
		//_waitQ.put(r.getRequestID(), o);
		
		Hashtable h = new Hashtable();
		_inbox.put(r.getRequestID(), h);

		// have to set the receivers
		int[] targetbricks; 
		if (!isWrite) {
			String out = "the potential recipient list is ";
			for (int w = 0; w < repliedbricks.length; w++) {
				out += repliedbricks[w] + " ";	
			}

			targetbricks = new int[_R];
			int offset = _r.nextInt();
			//System.out.println("offset is " + offset);
			int index = (offset%_WQ)+_WQ;
			//System.out.println("index is " + index);
			
			for (int i = 0; i < _R; i++) {
				int brickID = repliedbricks[(index+i)%repliedbricks.length];
				targetbricks[i] = brickID;
			}
			
			out += " and chose " + targetbricks[0];
			//System.out.println(out);
			
		}
		else {
			targetbricks = new int[_W];
			int size = _bricks.size();
			//System.out.println("numbricks " + size );			
			if (size < _W) {
				throw new Stub.InsufficientBricksAvailableException();	
			}
			
			Enumeration e = _bricks.keys();
			int index = ((_r.nextInt()%size)+size)%size;
			//System.out.println("size, index is " + + size + " " +index);

			Object[] obtemp = new Object[size];
			int m = 0;
			while (e.hasMoreElements()) {
				obtemp[m] = e.nextElement();
				m++;	
			}
	
			/*String out = "obtemp is ";
			for (int k = 0; k < obtemp.length; k++) {
				out += " " + obtemp[k];	
			}
			System.out.println(out);
*/
			int numchosen = 0;
			for (int i = 0; numchosen < _W && i < obtemp.length; i++) {
				BrickInfo bi = (BrickInfo) _bricks.get((Integer) obtemp[(i+index)%size]);
				if (bi.goodToSend()) {
					targetbricks[i] = bi.brickid;
					numchosen++;
				}
			}
			
			if (numchosen < _W) {
				//System.out.println("I failed here");
				throw new Stub.InsufficientBricksAvailableException();	
			}
		}

/*		String out = " target bricks: ";
		for (int p = 0; p < targetbricks.length; p++) {
			out += targetbricks[p] + " " ;
		}
		//System.out.println(out);
*/
		boolean force = false;
		
		for (int p = 0; p < targetbricks.length; p++) {
			// now let's publish them
			//System.out.println("Trying to publish to " + targetbricks[p]);
			BrickInfo bi = (BrickInfo) _bricks.get(new Integer(targetbricks[p]));
			try {
				if (bi != null) {
					synchronized(bi) {
						bi.publishRequest(r, force);
						force = true;
					}
					/*if (this._id == 5) {
						System.out.println("STUB: I PUBLISHED RID " + r.getRequestID() + " of type " + r.getType());
					}*/
				}
				else {
					throw new BrickInfoIsStaleException();	
				}
			}
			catch (BrickInfo.BrickTooBusyException btbe) {
				throw new SystemOverloadedException();	
			}
			catch (BrickInfo.BrickDeadException bde) {
				//bde.printStackTrace();
				// shut down the brick
				// and remove it from the list of bricks that are living
				_bricks.remove(new Integer(targetbricks[p]));
				//System.out.println("Removing brick " + targetbricks[p]);
				bi.shutdown();
			}

		}

/*		synchronized (o) {
			int size;
			synchronized(h) {
				size = h.size();
			}
			if (isWrite && size < _WQ || !isWrite && size < 1) {
				try {
					o.notifyAll();
					o.wait(_timeout);
				}
				catch (Exception e) 
				{
				}
			}
		}
*/
		try {
			synchronized (h) {
			int size = h.size();
				if (isWrite && size < _WQ || !isWrite && size < 1) {
					h.wait(_timeout);	
				}
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
		}

//		Object o2 = _waitQ.remove(r.getRequestID());
		Hashtable h2 = (Hashtable) _inbox.remove(r.getRequestID());

/*		if (this._id == 5) {
				System.out.println(r.getRequestID() + " is here");	
		}
*/
		if (!h.equals(h2)) {
			System.out.println("ERROR!  the hashtable i put in is not the same as i got out!");	
		}

		if (isWrite && h.size() < _WQ || h.size() == 0) {
/*			if (this._id == 5) {
				System.out.println(r.getRequestID() + " didn't get a timely response");	
			}*/
			// if the request was a write request and we didn't get enough replies
			// or if we didn't get any replies from a read, then we have timed out	
			String out = "timed out target bricks: ";
			for (int p = 0; p < targetbricks.length; p++) {
				out += targetbricks[p] + " " ;
			}

			out += " bricks that replied: ";
			// todo keep track of who timed out

			int[] repliedb = new int[h.size()];
			Enumeration e1 = h.elements();
			int count = 0;
			while (e1.hasMoreElements()) {
				repliedb[count] = ((Msg) e1.nextElement()).getSender();
				out += repliedb[count] + " ";				
				count++;
			}

			for (int p = 0; p < targetbricks.length; p++) {
				boolean replied = false;
				for (int m = 0; m < repliedb.length; m++) {
					if (targetbricks[p] == repliedb[m]) {
						replied = true;
					}	
				}
				if (!replied) {
					BrickInfo bi = (BrickInfo) _bricks.get(new Integer(targetbricks[p]));					
					bi.timedout(r.getRequestID().intValue());
				}
			}
			
			
		/*	if (this._id == 5) { 
				System.out.println(out);
			}*/
//			System.out.println(r.getRequestID() + " is the request ID that timed out, for object " + r.getKey());
			throw new RequestTimedOutException();				
		}		
		
		else {
	/*		if (this._id == 5) {
				System.out.println("I was successful at " + r.getRequestID());	
			}*/
			return h;			
		}
	}


	public class HeartbeatListener extends Thread {
		//System.out.println("In on message");
		//System.out.println(_id+"Got Heartbeat From Brick " + sender);

		Stub _s;
		
		public HeartbeatListener(Stub s) {
			_s = s;	
		}

		public void run() {
		
			try {
				
				InetAddress group = InetAddress.getByName(Brick.MC_GROUP);
	            MulticastSocket mcs = new MulticastSocket(Brick.MC_PORT);
	            mcs.joinGroup(group);
				int msgLen = 50;
	
		
		        while (true) {
	                byte[] buf = new byte[msgLen];
	                DatagramPacket msg = new DatagramPacket(buf, msgLen);
	//				System.out.println("Listening for heartbeats");
	                mcs.receive(msg);
	//				System.out.println("Got a heartbeats");
					

	                buf = msg.getData();
	                
//	                System.out.println("buf len " + buf.length);
					String tempstring = new String(buf);
					
//					System.out.println("tempstring len " + tempstring.length());
//					System.out.println("E1243WRWER");
					
					StringTokenizer strtok = new StringTokenizer(tempstring, "#");
//					System.out.println("EWRWER");
	
					if (strtok.countTokens() < 3) {
//						System.out.println("Got a bad heartbeat");
						return;
					}
//					System.out.println("AFTERSTROK");
					int length = Integer.parseInt(strtok.nextToken());
//					System.out.println("the length of string is " + length);

					int sender = Integer.parseInt(strtok.nextToken());

//					System.out.println("Sender is " + sender);
					int port = Integer.parseInt(strtok.nextToken());                 
//					System.out.println("Port is " + port);
        	        String host = strtok.nextToken();
//					System.out.println("Host is " + host);

//							System.out.println("HERE1");
		
					BrickInfo bi = (BrickInfo) _bricks.get(new Integer(sender));
					if (bi != null) {
					}
					else {
						try {
							System.out.println("HERE");
							// we need to add the brick to the list of bricks	
							// have to connect to the new live brick
							//System.out.println("PORT NUMBER IS " + newmsg.getPort());
							Socket s = new Socket(host, port);	
						
							// create new input and output streams
					        OutputStream os = s.getOutputStream();
					        ObjectOutputStream oos = new ObjectOutputStream(os);
			
							Msg initmsg = new Msg();
							initmsg.setSender(_id);
							initmsg.MakeReadReq(0,0);
							oos.writeObject(initmsg);
						    InputStream is = s.getInputStream();
					        ObjectInputStream ois = new ObjectInputStream(is);
			
							// create a listener thread to listen on the output stream
							StubListenerThread l = new StubListenerThread(s, ois, _s);
							l.setName("ListenerThread-Brick" + sender);
							l.start();
							System.out.println("Started a new listener thread for host " + host);
							bi = new BrickInfo(oos, s, sender, _timeout);
							_bricks.put(new Integer(sender), bi);				
						}
						catch (Exception e) {
							System.out.println("Exception encountered while setting up new brick for host " + host);	
						}
					}
		        }
			}
			catch (Exception e) {
				System.out.println("exception");
				e.printStackTrace();
			}

		}
		public void finalize() {
			System.out.println("I'm dying");	
			
		}

	}
		
	public void processMessage(Msg newmsg) {
		//System.out.println("Processing Request");
		int type = newmsg.getType();
		int sender = newmsg.getSender();

		BrickInfo bi = (BrickInfo) _bricks.get(new Integer(sender));
		bi.ack(newmsg.getInResponseTo().intValue());

/*		Object l = _waitQ.get(newmsg.getInResponseTo());
		if (l == null) {
		}
		*/
		
			
		// otherwise, add it to the inbox
		Hashtable retval = (Hashtable) _inbox.get(newmsg.getInResponseTo());
		if (retval == null) {
			// this means that the reply came too late and that
			//System.out.println ("response too late for " + newmsg.getInResponseTo() + " from brick " + newmsg.getSender());
 
			return;
		}
		synchronized(retval) {
			retval.put(new Integer(sender), newmsg);

			if (type == Msg.WRITE_REPLY) {
				if (retval.size() >= _WQ) {
					retval.notify();
				}
			}
			else if (type == Msg.READ_REPLY) {
				retval.notify();
			}
		}
	}



	public class StubListenerThread extends Thread {
		private Socket _socket;
		private ObjectInputStream _ois;
		private Stub _b;

		public StubListenerThread(Socket c, ObjectInputStream ois, Stub b) {
			_socket = c;
			_ois = ois;
			_b = b;
		}

		public void run() {
			while (true) {
				try {
			        Msg req = (Msg) _ois.readObject();
					//System.out.println("GOT A REPLY FROM brick " + req.getSender() + " for msg " + req.getInResponseTo());
					// call processMessage
					_b.processMessage(req);
			
				}
				catch (Exception e) {
					//e.printStackTrace();
					break;
				}

			}
		}
		protected void finalize() {
			try {
				//System.out.println("THE LISTENER IS DYING!");
				_ois.close();
				_socket.close();
			}
			catch (Exception e) {
				//System.out.println("Exception encountered while cleaning up stub");
				//e.printStackTrace();	
				
			}
		}
		
	}
 	public class BrickInfoIsStaleException extends Exception {
 		public BrickInfoIsStaleException() {
 			System.out.println("BRICKINFOSTALEEXCEPTION");
 		}
 	}
 	
	public class RequestTimedOutException extends Exception {
		public RequestTimedOutException () {
 			//System.out.println("RequestTimedOutException ");
 		}
	}
	
	public class InsufficientBricksAvailableException extends Exception {
		public InsufficientBricksAvailableException() {
 			//System.out.println("InsufficientBricksAvailableException  ");
 		}
	}
	
	public class BadChecksumException extends Exception {
		public BadChecksumException () {
 			System.out.println("BadChecksumException");
 		}
	
	}


}	














