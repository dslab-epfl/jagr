/**
 * @author bling
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
package roc.rr.ssm;

import java.util.Random;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.lang.Integer;
import java.lang.Long;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.net.*;
import java.io.*;



public class Brick {
    static boolean PINPOINT_ON = false;
    static boolean trashing = true;
    GenerationalHash _hash;
    int _id;
    Vector _inbox;
    int _capacity;
    int[] _receiver;
    static boolean DEBUG = false;
	

    // is this brick to be a slow brick?
    boolean _slow;
    long _delay;
	
    boolean _recover;
	
    // statistic variables
    long _numRequestsHandled;

    // port information
    String _hostname;
    int _port;
    ServerSocket server;

    // heartbeat mechanism
    MulticastSocket mc;
    public static int MC_PORT = 7099;

    // current multicast group allocation:
    //
    //   "226.1.1.1" (Shinichi)
    //   "226.1.1.2" (Yuichi)
    //   "226.1.1.3" (Geo)
    //   "226.1.1.4" (Greg)
    //   "226.1.1.5" (Emre)
    public static String MC_GROUP = <DESIRED_MULTICAST_GROUP>;
    public static    int TTL = 100;


    int _MAXtimeout;	
    static public Hashtable stubIDtoOutputStreamMap = new Hashtable(50);

    int _numreadreq;

    // for log message 
    SimpleDateFormat dateFormat
	= new SimpleDateFormat("MM/dd/yy HH:mm:ss,SSS", Locale.US);


    public void init(int id, int cap, boolean slow, long delay, String h, int p, int maxtime, boolean rec) {
	_hash = new GenerationalHash();
	_id = id;
	_capacity = cap;
	_inbox = new Vector(_capacity);
	_receiver = new int[1];
	_slow = slow;
	if (slow) {
	    _delay = delay;
	}
	else _delay = 0;

	_recover = rec;
				
	_hostname = h;
	_port = p;
	_MAXtimeout = maxtime;
		
	_numreadreq = 0;

	while (server == null) {
	    try{
		server = new ServerSocket(_port); 
		AcceptorThread ac = new AcceptorThread(server, _inbox);
		ac.start();

		mc = new MulticastSocket(MC_PORT);
		mc.setTimeToLive(TTL);	
	    } catch (IOException e) {
		System.out.println("Could not listen on port " +_port +", trying next port");
		_port++;
	    }
	}

	

    }


    public void go() {
	try {
	    Object o = new Object();
	
	    o.wait(5000);
	}
	catch (Exception e) {}

	SendHeartBeat();
    

	long lastheartbeattime = System.currentTimeMillis();		
	long timezero = lastheartbeattime;
	long curtime = 0;

	// statistics variables
	int numcycles = 1;
	int totalQ = 0;
	int totalnumrequestshandled = 0;
	int trashed = 0;
	Map originInfo = new HashMap();
	Map rawDetails = new HashMap();
	Map otherMap = new HashMap();

	originInfo.put(StatisticsInterface.BrickID, String.valueOf(_id));
	originInfo.put(StatisticsInterface.BrickHost, _hostname);
	originInfo.put(StatisticsInterface.BrickPort, String.valueOf(_port));
	originInfo.put(StatisticsInterface.StartTime, String.valueOf(System.currentTimeMillis()));

	if (_slow) {
	    System.out.println("Brick " + _id + " is a SLOW brick!");
	}

	while (true) {
	    // if there's something in the inbox
	    // process it

	    Msg toprocess  = null;
	    try {
		synchronized (_inbox) {
		    if (_inbox.size() > 0) {
			toprocess = (Msg) _inbox.remove(0);

			if (trashing) {
			    while (_inbox.size() > 0 && toprocess.getTime() + toprocess.getTimeOut() < System.currentTimeMillis()) {
				toprocess = (Msg) _inbox.remove(0);
				trashed++;
				//System.out.println("Trashing toprocess' time is " + toprocess.getTime() + " timeout is " + toprocess.getTimeOut() + " curtime="+System.currentTimeMillis());	
			    }
			}
		    }
					
		    else {
			_inbox.wait(50);
		    }
		    // changed BL 
		    _inbox.notify();

		}
	    }
	    catch (Exception e) { System.out.println(e); }

	    // if there's a message, process it
	    if (toprocess != null) {
		try{
		    processMessage(toprocess);
		}

		catch(MemoryCorruptedException mce) {
		    return;
		}
	    }

	    curtime = System.currentTimeMillis();
	    if (lastheartbeattime < curtime - 1000) {
		long freemem = Runtime.getRuntime().freeMemory()/1024;
		long totalmem = (Runtime.getRuntime().totalMemory()/1024);								
		totalnumrequestshandled += _numRequestsHandled;

				// pinpoint statistics				
		try {
				// pinpoint statistics
		    if (PINPOINT_ON) {
			rawDetails.put(StatisticsInterface.TimeSinceStartup, new Long(curtime-timezero));
			rawDetails.put(StatisticsInterface.NumDropped, new Integer(trashed));
			rawDetails.put(StatisticsInterface.NumReadProcessed, new Integer(_numreadreq));
			rawDetails.put(StatisticsInterface.NumWriteProcessed, new Long (_numRequestsHandled - _numreadreq));
			rawDetails.put(StatisticsInterface.NumElements, new Integer(_hash.size()));
			rawDetails.put(StatisticsInterface.MemoryUsed, new Long(totalmem - freemem));
			rawDetails.put(StatisticsInterface.TimeInterval, new Long(curtime-lastheartbeattime));
			rawDetails.put(StatisticsInterface.InboxSize, new Integer(_inbox.size()));
			roc.pinpoint.tracing.Observation PP = new roc.pinpoint.tracing.Observation(roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE, "BRICK", 0, originInfo, rawDetails, /* add meta attributes for this observation later if necessary, e.g. class info */ null);
			roc.pinpoint.tracing.GlobalObservationPublisher.Send(PP);
		    }
		}
		catch (Exception ee) {
		    ee.printStackTrace();
		}

/*
		System.out.println("Number of ops during last second: " +  _numRequestsHandled);
		System.out.println("Number of reads/writes: " + _numreadreq + "/" + (_numRequestsHandled - _numreadreq));
		System.out.println("The inbox size is " + _inbox.size());
		System.out.println("Free memory/total mem/used " + freemem + " / " + totalmem + " / " + (totalmem-freemem));
*/
		SendHeartBeat();
		lastheartbeattime = curtime;
		_numRequestsHandled = 0;
		_numreadreq = 0;
		numcycles++;
		trashed = 0;

	    }
	}
    }

    private void SendHeartBeat() {

	/* S.Kawamoto
	if (DEBUG) {
	    System.out.println("Sending heartbeat from brick " + _id);
	}
	*/
		
	String msg = _id + "#" + _port + "#" + _hostname + "#garbage#";
	msg = msg.length() + "#" + msg;

	try {
	    InetAddress group = InetAddress.getByName(MC_GROUP);
	    DatagramPacket beacon = new DatagramPacket(msg.getBytes(),
						       msg.getBytes().length,
						       group, MC_PORT);
	    mc.send(beacon);
	}
	catch (Exception e) {
	    System.out.println("error sending heartbeat");
	}

    }

    private void processMessage(Msg m) throws MemoryCorruptedException{
	if (DEBUG) {
	    System.out.println("starting processMessage " + m.getSender());
	}
	Msg reply = new Msg();
	reply.setSender(_id);
	_receiver[0] = m.getSender();
	reply.setReceiver(_receiver);
	reply.setInResponseTo(m.getRequestID());
	long key = m.getKey();
	if (DEBUG) {
	    System.out.println("GOT A REQUEST FROM STUB " + m.getSender());
	}

	if (m.getType() == Msg.WRITE_REQ) {
	    if (Runtime.getRuntime().freeMemory() < 10000000) {
		System.out.println("I'm out of memory");
		return;	
	    }
	    if (DEBUG) {
		System.out.println("Handling write of key " + key + "firstone");
	    }
	    Object data = m.getData();
	    int check = m.getChecksum();
	    long exp = m.getExpiry();
	    Vector v = new Vector(3);
	    v.addElement(data);
	    v.addElement(new Integer(check));
	    v.addElement(new Long(exp));

	    try {
		_hash.put(key, exp, v);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    // successfully write data on this brick
	    logMessage("write data: "+v);



	    reply.MakeWriteReply(key);
	    _numRequestsHandled++;
	    if (DEBUG) {
		System.out.println("Handling write of key " + key);
	    }
	}		

	if (m.getType() == Msg.READ_REQ) {
            //System.out.println("I got this brick!! " + _id);		
	    if (DEBUG) {
		System.out.println("Handling READ of key " + key);
	    }
	    // if it is a read, then fetch it from the hash 

	    Vector v = (Vector) _hash.get(key, m.getExpiry());
			
	    // if nothing is found, then do nothing
	    if (v == null) {
		System.out.println("I GOT A READ REQUEST THAT I DON'T HAVE DATA FOR! " + key);
		return;
	    }
	    
	    Object data = v.elementAt(0);
	    int check = ((Integer) v.elementAt(1)).intValue();
	    long exp = ((Long) v.elementAt(2)).longValue();

	    
	    // successfully read data from this brick
	    logMessage("read data: "+v);

	    /* S.Kawamoto
	    if (data.hashCode() != check) {
		System.out.println("******************************");
		System.out.println("Hash code for key " + key + " does not match what is stored.  Exiting");
		System.out.println("******************************");
		// data has been corrupted
		throw new MemoryCorruptedException();
	    }
	    */

	    reply.MakeReadReply(key, data, check, exp);		
	    _numreadreq++;
	    _numRequestsHandled++;
	}

	//System.out.println("About to publish reply");
	// publish the reply
	/*		Object o = new Object();
			synchronized(o) {
			try {
			o.wait(_delay);
			}
			catch (Exception e) {}		
				//System.out.println("I waited for " + _delay);				
				}	
	*/		

	double f = 3.333333;
	for (int i = 0; i < _delay; i++) {
	    f=f*f;
	    f=f/f;
	}

	if (_delay > 0 && _recover) {
	    _delay -= 1000;
	    if (_delay % 100000 == 0) {
		System.out.println("delay " + _delay);
	    }

	}
		
	publishMsg(reply);
    }

    public void publishMsg(Msg r) {
	ObjectOutputStream oos = null;
		
	try {
	    if (r.getType() == Msg.HEART) {
		Enumeration e = Brick.stubIDtoOutputStreamMap.elements();	
		while (e.hasMoreElements()) {
		    oos = (ObjectOutputStream) e.nextElement();
		    oos.writeObject(r);
		    oos.flush();
		}
		return;
	    }



	    int[] rec = r.getReceiver();
			
	    Integer i = new Integer(rec[0]);

	    oos = (ObjectOutputStream) Brick.stubIDtoOutputStreamMap.get(i);
	    oos.writeObject(r);
	    oos.flush();
	    oos.reset();
	    //System.out.println("Replied " + r.getInResponseTo() + " to stub " + i);

	}
	catch (Exception e) {
	    try {
		if (oos != null) {
		    oos.close();	
		}
	    }
	    catch (Exception ee ) {}
	    //e.printStackTrace();
	}
    }

    public class AcceptorThread extends Thread {
	ServerSocket ss;
	Vector _inbox;
	public AcceptorThread(ServerSocket s, Vector v) {
	    ss = s;
	    _inbox = v;
	    this.setName("AcceptorThread");
	}
	public void run() {
	    while(true){
		listenerThread w;
		try{
		    w = new listenerThread(server.accept(), _inbox);
		    System.out.println("\t\tInitted listenerthread");
		    w.start();
		} 
		catch (IOException e) {
		    System.out.println("Accept failed: 4444");
		    System.exit(-1);
		}
	    }

			
	}
		
    }
    public class listenerThread extends Thread {
	private Vector _v;
	private Socket _socket;
	private ObjectInputStream _ois;
	private ObjectOutputStream _oos;

	public listenerThread(Socket c, Vector v) {
	    _socket = c;
	    _v = v;
	}

	public void run() {
	    try {
				// create the streams
		InputStream is = _socket.getInputStream();
		_ois = new ObjectInputStream(is);
		        
		OutputStream os = _socket.getOutputStream();
		_oos = new ObjectOutputStream(os);
		        
		Msg req = (Msg) _ois.readObject();
		        
		// create an entry in the map for this newly created connection
		synchronized (Brick.stubIDtoOutputStreamMap) {
		    Enumeration e = Brick.stubIDtoOutputStreamMap.keys();
		    String output = "Keys before adding mapping : ";
		    while (e.hasMoreElements()) {
			output += e.nextElement();	
		    }
		    //System.out.println(output); 

		    //System.out.println("Added mapping for stub " + req.getSender());
		    Brick.stubIDtoOutputStreamMap.put(new Integer (req.getSender()), _oos);
			        
		    e = Brick.stubIDtoOutputStreamMap.keys();
		    output = "Keys after adding mapping : ";
		    while (e.hasMoreElements()) {
			output += e.nextElement();	
		    }
		    //System.out.println(output); 
		    stubIDtoOutputStreamMap.notifyAll();
		}
		        
		this.setName("ListenerThread-Stub" + req.getSender());
		synchronized(_v) {
		    req.setTime();
		    _v.addElement(req);	
		    //changed BL 
		    _v.notify();
		}
	    }		        
			
	    catch (Exception e) {
		try {
		    _ois.close();
		    _oos.close();
		    _socket.close();
		}
		catch (Exception ee) {
		    //e.printStackTrace();
		}
	    }
	    while (true) {
		try {
		    //System.out.println("Try to get a new request");
		    Msg req = (Msg) _ois.readObject();
		    System.out.println("GOT A NEW REQUEST: "+req);
		    // copy the item into the inbox
		    synchronized (_v) {
			req.setTime();
			_v.addElement(req);	
			//changed BL
			_v.notify();
		    }	
		    //System.out.println("Received " + req.getRequestID());
		}
		catch (EOFException eofe) {
		    logMessage("Connection closed. Stub might be terminated.");
		    break;
		}
		    
		catch (Exception e) {
		    e.printStackTrace();
		    break;
		}
	    }
	}

	protected void finalize() {
	    try {
		_ois.close();
		_oos.close();
		_socket.close();
	    }
	    catch (Exception e) {
		System.out.println("Exception encountered while cleaning up thread");
	    }
	}
	
    }
    public class FaultyThread extends Thread {
	public long interval;
	public long _delay;
		
	public FaultyThread(long i, long delay) {
	    interval = i;	
	    _delay = delay;
	}
		
	public void run() {
	    System.out.println("RUNNING FT");
	    Object o = new Object();
	    while (true) {
		try {
		    synchronized(o) {
			double f = 3.333333;
			for (int i = 0; i < _delay; i++) {
			    System.out.println(i+"");
			    f=f*f;
			    f=f/f;
			}	
			System.out.println("waiting");
			o.wait(250);
		    }
		}
		catch (Exception e) {}
	    }	
			
			
	}
		
    }

    public class GenerationalHash {
	private long basetime;
	private long interval;
	private Vector hashvector;
	private int size;

	public GenerationalHash() {
	    interval = 1 * 10 * 1000; // 10 sec 	
	    size     = 1000;          // 10 sec * 1000 = 10000 sec 
	    hashvector = new Vector(size);
	    for (int i = 0; i < size; i++) {
		hashvector.add(new Hashtable(size));	
	    }
	    basetime = System.currentTimeMillis();
	    // 
	    // Random r = new Random(System.currentTimeMillis());
	    // interval = ((r.nextInt() % size) + size) % size;
	}

	public int size() {
	    int sz = 0;
	    Enumeration e = hashvector.elements();
	    while (e.hasMoreElements()) {
		Hashtable h = (Hashtable) e.nextElement();
		sz += h.size();	
	    }
	    return sz;
	}

	public void put(long key, long exp, Object o) {
	    long curtime = System.currentTimeMillis();

	    /*
	     *  gabage collection for expired data
	     *     Mar/12/2004 S.Kawamoto
	     */
	    logMessage("Collect Garbage: "
		       +(curtime-basetime)/interval
		       +" units are reclaimed");
	    while ( ((curtime - basetime) / interval) > 0 ) {
		hashvector.removeElementAt(0);
		hashvector.add(new Hashtable(size));
		basetime += interval;
	    }

	    long offset = (exp - basetime) / interval;
	    if (offset < 0 || offset >= size) {
		System.out.println("GenerationalHash.put ERROR: offset is negative.  Are clocks synced between stub and brick?");
		return;
	    }
	    //System.out.println("OFFSET IS " + offset);
	    Hashtable h = (Hashtable) hashvector.elementAt((int)offset);			
	    h.put(new Long(key), o);
	}

		
	public Object get(long key, long exp) {
	    long curtime = System.currentTimeMillis();
	    if (exp < curtime) {
		return null;
	    }
			
	    /*
	     * garbage collection for expired data
	     *   Mar/12/2004 S.Kawamoto
	     */

	    logMessage("Collect Garbage: "
		       +(curtime-basetime)/interval
		       +" units are reclaimed");
	    while (((curtime - basetime) / interval) > 0) {
		// this means an epoch has passed
		hashvector.removeElementAt(0);
		hashvector.add(new Hashtable(1000));
		basetime = basetime + interval;
	    }
			
	    long offset = (exp - basetime) / interval;
	    if (offset < 0 || offset >= size) {
		System.out.println("GenerationalHash.get ERROR: offset is invalid for key " + key + " and exp value " + exp + ". Are clocks synced between stub and brick?");
		return null;
	    }

	    Hashtable h = (Hashtable) hashvector.elementAt((int)offset);
			
	    return h.get(new Long(key));	
	}		
    }

    public void logMessage(String s){
	Date date = new Date(System.currentTimeMillis());
	System.out.println(dateFormat.format(date)+"  "+s);
    }


    public class MemoryCorruptedException extends Exception {}
}
