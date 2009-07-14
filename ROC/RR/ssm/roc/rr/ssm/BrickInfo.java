package roc.rr.ssm;

import java.io.*;
import java.util.*;
import java.net.*;
//import Msg;

/**
 * @author bling
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BrickInfo {
	// the socket to write to the brick with
	ObjectOutputStream _oos;
	Socket _socket;
	public static boolean windowon = true;

	public long avgresponsetime;
	
	public long lastcuttime;

	public int curwindowsize;
	public int maxwindowsize;
	private long _timeout;

	public CircularBuffer cb;

	public int brickid;
	//public long wait;
	public Random r;


	public long lastprinttime;

	public BrickInfo(ObjectOutputStream o, Socket s, int bid, long t) {
		r = new Random();
		//wait = 0;
		_oos = o;	
		_socket = s;
		//curwindowsize = 50;
		//maxwindowsize = 500;
		curwindowsize = 250;
		maxwindowsize = 2500;
		cb = new CircularBuffer(maxwindowsize);
		lastcuttime = 0;
		brickid = bid;
		lastprinttime =0;
		
		_timeout = t;
	}
	
	public boolean goodToSend() {
		return (curwindowsize-5 > cb.size() || (lastcuttime + _timeout) < System.currentTimeMillis());
	}

	public void shutdown() {
		try {
			_oos.close();
			_socket.close();
		}
		catch (Exception e) {}	
	}	
	public void ack(int id) {
		synchronized(cb) {
			if (curwindowsize < maxwindowsize) {
				curwindowsize++;		
				//wait -= 1;
			}
			cb.ack(id);
			cb.notifyAll();
		}
	}

	public void timedout(int id) {
		synchronized(cb) {
			if (System.currentTimeMillis() > lastcuttime + (_timeout*1/2)) {
				if (windowon) {
					curwindowsize = 5*cb.size()/8;
				}
				lastcuttime = System.currentTimeMillis();
				if (curwindowsize < 1) {
					curwindowsize = 1;
				}
				
				//wait = System.currentTimeMillis() + 200 + ((r.nextInt()%200)+200)%200;

				//System.out.println(id + " timed out, window is " + curwindowsize + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");


			}
			cb.ack(id);
			//System.out.println(id + " timed out");
			cb.notifyAll();
		}
		//System.out.println(id + " timed out, window is " + curwindowsize + " for brick " + brickid);

	}
	
	public int getWindowSize() {
		return curwindowsize;
	}
	
	
	public void publishRequest(Msg m, boolean force) 
	    throws BrickTooBusyException, BrickDeadException {
	    /*if (force == true) {
	      System.out.println("i'm being forced!");
	      }*/
	    synchronized(cb) {
		if (cb.size() < curwindowsize || force) {
		    //System.out.println("current window size " + curwindowsize + " for brick id " + brickid + " cb size " + cb.size());
		    // then publish it	
		    try {
			if (_oos != null) {
			    _oos.writeObject(m);
			    _oos.flush();
			    _oos.reset();
			}
		    }
		    catch (Exception e) {
			try {
			    //e.printStackTrace();
			    _oos.close();
			    throw new BrickDeadException();
			}
			catch (Exception ee) {
			    //ee.printStackTrace();
			    throw new BrickDeadException();
			}
		    }
		    
		    try {			
			cb.add(m.getRequestID().intValue());
		    }
		    catch (CBFullException cbe) {} 
		} else {
		    System.out.println("[SSM BrickInfo] on the fly request overflow");
		    throw new BrickTooBusyException();	
		}
		cb.notifyAll();
	    }
	}
		

	public class BrickTooBusyException extends Exception {
	}

	public class BrickDeadException extends Exception {
	}


	public class CircularBuffer {
		int[] buf;
		
		// index of the first valid element
		int beg;
		
		// index of the first empty spot
		int end;
		int capacity;
		
		public CircularBuffer(int i) {
			buf = new int[i];
			beg = 0;
			end = 0;
			capacity = i;
		}	
		
		public int size() {
			return (end+capacity-beg)%capacity;	
		}
		
		public void add(int element) throws CBFullException {
			if (size() < capacity-1) {
				buf[end] = element;
				end = (end+1)%capacity;	
			}
			else {
				System.out.println("TOO FULL!!!!");
				throw new CBFullException();
			}
		}
	
		public void ack(int element) {
			for (int i = 0; i < size(); i++) {
				if (buf[(beg+i)%capacity] == element) {
					beg = (beg+i+1)%capacity;
					//System.out.println("acked id " + element);	
					return;
				}	
			}
			//System.out.println("ERROR, wasn't able to ack " + element);
			// for some reason we couldn't ack this ID
		}	

		public void dump() {
			System.out.println("beg, end, size " + beg + " " + end + " " + size());
			String o = "";
			for (int i = 0; i < capacity; i++) {
				o += buf[i] + " ";	
			}
			System.out.println("buffer has " + o);
		}

	}

	public class CBFullException extends Exception {	
	}


}
