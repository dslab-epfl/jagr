/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.analysis.plugins2.DStore;

//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;

/**
 * this plugin manages the SSM brick connections, determining which bricks need  * to be restarted.
 *
 **/

public class BrickManager implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String SWEEP_TIME_ARG = "sweepTime";
    public static final String RESTART_THRESHOLD_ARG = "restartThreshold";
    public static final String TREND_THRESHOLD_ARG = "trendThreshold";
    public static final String HEARTBEAT_THRESHOLD_ARG = "heartbeatThreshold";
    public static final String ANOMALY_TIMEOUT_ARG = "anomalyTimeout";
    public static final String RESTART_IMMUNITY_ARG = "restartImmunityTime";
    public static final String SYSTEM_IMMUNITY_ARG = "systemImmunityTime";
    public static final String RESTART_PATH = "/work/ach/destor_v2/restartbrick.sh";
    public static final int N_BRICKS = 40;
    public static boolean verbose = true;
    public static boolean announceHB = false;
    public static boolean silent = false;
    public static final int HEARTBEAT_DURATION=5000;

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection. this plugin will monitor the bricks used by SSM to store state and determine which require restarting due to anomalous behavior",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin places the failed bricks into a record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( SWEEP_TIME_ARG,
		       "sweep time. how often the plugin will sweep to check fo r anomalous bricks (in microseconds)",
		       PluginArg.ARG_INTEGER,
		       false,
		       "5000" ),
	new PluginArg( RESTART_THRESHOLD_ARG,
		       "restart threshold. how many anomalies need to be present before a brick is restarted",
		       PluginArg.ARG_INTEGER,
		       false,
		       "4" ),
	new PluginArg( TREND_THRESHOLD_ARG,
		       "trend threshold. how many consecutive anomalies need to be present before a brick is restarted",
		       PluginArg.ARG_INTEGER,
		       false,
		       "4" ),
	new PluginArg( HEARTBEAT_THRESHOLD_ARG,
		       "heartbeat threshold. how many heartbeats a brick can miss before being restarted",
		       PluginArg.ARG_INTEGER,
		       false,
		       "4" ),
	new PluginArg( ANOMALY_TIMEOUT_ARG,
		       "anomaly timeout. how many milliseconds before an old anomaly is removed from the list of active anomalies",
		       PluginArg.ARG_INTEGER,
		       false,
		       "15000"),
	new PluginArg( RESTART_IMMUNITY_ARG,
		       "restart immunity. how long new bricks have before being open to subsequent restarts",
		       PluginArg.ARG_INTEGER,
		       false,
		       "90000"),
	new PluginArg( SYSTEM_IMMUNITY_ARG,
		       "restart immunity. how long entire system has before being open to subsequent restarts",
		       PluginArg.ARG_INTEGER,
		       false,
		       "90000")
	    };
    
    private RecordCollection inputCollection;
    private RecordCollection outputCollection;
    protected long sweepTime;
    protected int restartThreshold;
    protected int trendThreshold;
    protected long anomalyTimeout;
    protected long heartbeatTimeout;
    protected long restartImmunity;
    protected long systemImmunity;

    protected long lastBrickRestartTime;

    private AnalysisEngine engine;
    
    public PluginArg[] getPluginArguments() {
	return args;
    }
    
      private class BrickManagerImp {

	private class HeartbeatListener extends Thread {
	    BrickManagerImp listener;
	    
	    private int MC_PORT = 8075;
	    private String MC_GROUP = "228.1.1.2";
	    
	    public HeartbeatListener(Object o) 
	    {
		listener = (BrickManagerImp)o;
	    }
	    
	    public void run() {
		
		try {
		    InetAddress group = InetAddress.getByName(MC_GROUP);
		    MulticastSocket mcs = new MulticastSocket(MC_PORT);
		    mcs.joinGroup(group);
		    int msgLen = 50;
		    
		    while (true) {
			byte[] buf = new byte[msgLen];
			DatagramPacket msg = new DatagramPacket(buf, msgLen);
			mcs.receive(msg);
			buf = msg.getData();
			String tempstring = new String(buf);
			StringTokenizer strtok = new StringTokenizer(tempstring, "#");
			if (strtok.countTokens() < 3) {
			    return;
			}
			    
			String hostPort = strtok.nextToken();
			int index = hostPort.indexOf(':');
			String ipStr = hostPort.substring(0, index);
			
			String host = InetAddress.getByName(ipStr).getHostName();;
			    
			int id = ParseID(host);
			listener.ReceiveHeartbeat(id, host);
		    }
		} catch(Exception e) {
		    System.err.println("Exception in heartbeat listener: " + 
				       e.getMessage());
		    e.printStackTrace();
		}
	    }
	    
	    public void finalize() {
		System.out.println("I'm dying");	
		
	    }    
	}
	
	private int nBuckets;
	protected long sweepTime;
  	private Timer timer;
	
	private class BrickStatus {
	    private int NUM_STATS = 50;
	  	    
	    int BrickID;
	    Hashtable table;
	  
	    private class anomalyData {
		protected String category;
		protected double level;
		protected long time;	

		anomalyData(String category, double level, long time)
		{
		    this.category = category;
		    this.level = level;
		    this.time = time;
		}

		String getCategory() { return category; }
		double getLevel() { return level; }
		long getTime() { return time; }
	    }

	    BrickStatus()
	    {
		BrickID = -1;
	    }

	    BrickStatus(int BrickID) 
	    { 
		this.BrickID = BrickID; 
		table = new Hashtable(NUM_STATS);
	    }

	    int size() { return table.size(); }

	    int getID() { return BrickID; }

	    void addAnomaly(String category, double level, long time)
	    {
		synchronized(table) {
		    table.put(category, new anomalyData(category, level, time));
		    if(verbose)
			System.err.println("Adding record to brick " + BrickID);
		}
	    }

	    Set getFaults()
	    {
		return table.keySet();
	    }

	    void clearOld(long time)
	    {
		if(verbose)
		    System.err.println("Sweeping brick " + BrickID + " for old records - starting size: " + size());

		synchronized(table) {
		    Set keys = table.keySet();
		    Iterator i = keys.iterator();

		    synchronized(i) {
			while(i.hasNext()) {
			    String key = (String)i.next();
			    if(((anomalyData)table.get(key)).getTime() + anomalyTimeout < time)
				i.remove();
			}
			if(verbose)
			    System.err.println("End size: " + size());
		    }
		}
	    }
	}
	
	private BrickStatus[] table;
	private Hashtable trendTable;
	private HeartbeatListener hb;
	private Hashtable heartbeats;
	private Hashtable hosts;
	private Hashtable startup;

	BrickManagerImp(int nBuckets, long sweepTime)
	{
	    this.nBuckets = nBuckets;
	    this.sweepTime = sweepTime;
  
	    table = new BrickStatus[nBuckets];
	    trendTable = new Hashtable(nBuckets);
	    hosts = new Hashtable(nBuckets);
	    heartbeats = new Hashtable(nBuckets);
	    startup = new Hashtable(nBuckets);
	    
	    hb = new HeartbeatListener(this);

	    timer = new Timer();
	    timer.schedule(new BrickMonPass(), 0, sweepTime);
	    
       	    hb.start();
	}
	
	int hash(int key) { return key % nBuckets; }
	
	void add(int BrickID, String category, String host, double level, 
		 long time)
	{
	    int key = hash(BrickID);
	    Integer ID = new Integer(BrickID);

	    if(table[key] == null) {
		System.err.println("not adding because of null");
		return;
	    }

	    if(((Long)heartbeats.get(ID)).longValue() == 0) {
		System.err.println("Anomaly received for dead brick " + 
				   BrickID);
		return;
	    }
	    
	    hosts.put(new Integer(BrickID), host);
	    System.out.println("anomaly: " + key + " " + category + " " +
			       level);
	    table[key].addAnomaly(category, level, time);

//  	    String trendTableID = String.valueOf(BrickID) + ":" +
//  		String.valueOf(category.hashCode());
//  	    Integer countObj = (Integer)trendTable.get(trendTableID);
//  	    if (countObj == null) {
//  		trendTable.put(trendTableID, new Integer(0));
//  	    }
//  	    else {
//  		int newCount = countObj.intValue() + 1;
//  		if (newCount > trendThreshold) {
//  		    if(!silent)
//  			System.err.println("Brick "+ BrickID + " has reached trend threshold for anomaly: " + category);    
//  		    RestartBrick(BrickID);
//  		    trendTable.put(trendTableID, new Integer(0));
//  		}
//  		else {
//  		    trendTable.put(trendTableID, new Integer(newCount));
//  		}
//  	    }
	}

	void stop()
	{
  	    timer.cancel();
	}


	  class BrickMonPass extends TimerTask {
	      public void run()
	  {
	    System.err.println("Brick Monitor Plugin pass");
	    for(int i=0; i < nBuckets; i++) {
		if(table[i] != null) {
		    long cur = System.currentTimeMillis();
		    int ID = table[i].getID();
		    long hb = ((Long)heartbeats.get(new Integer(ID))).longValue();
		    if(hb + heartbeatTimeout < cur) {
			if(!silent)
			    System.err.println("Brick "+ i + " has missed too many heartbeats.");    
			if(verbose)
			    System.err.println("Cur: " + cur + " / Last HB: " + hb);
//  			RestartBrick(ID);
		    }
		    else {
			System.out.print("Anomalies-" + table[i].BrickID);
			table[i].clearOld(cur);
			if(table[i].size() >= restartThreshold) {
			    System.err.println("  Anomalies in: ");
			    ListErrors(i);

			    if(((Long)startup.get(new Integer(ID))).longValue() + restartImmunity > System.currentTimeMillis())	{
				if(!silent)
				    System.out.println("Attempted to restart " + ID + " but it was within its immunity period");
			    }
			    else if (cur < lastBrickRestartTime + systemImmunity) {
				if(!silent)
				    System.out.println("Attempted to restart " + ID + " but system is within immunity period");
			    }
			    else {
				if(!silent)	{
				    System.err.println("Brick " + i + " has reached restart threshold.");
				}
				RestartBrick(ID);
				lastBrickRestartTime = cur;
			    }
			}
			else {
			    ListErrorsCompact(i);
			}
		    }
		}
	    }
	  }

	  void ListErrors(int BrickID)
	{
	    BrickStatus bs = table[hash(BrickID)];
	    Set errors = bs.getFaults();
	    Iterator i = errors.iterator();

	    synchronized(i) {
		while(i.hasNext())
		    System.err.println("\t" + (String)i.next());
	    }
	}

	  void ListErrorsCompact(int BrickID)
	{
	    BrickStatus bs = table[hash(BrickID)];
	    Set errors = bs.getFaults();
	    Iterator i = errors.iterator();

	    synchronized(i) {
		System.out.print(" [");
		while(i.hasNext())
		    System.err.print(" " + (String)i.next() + " ");
		System.out.println("]");
	    }
	}

	void RestartBrick(int BrickID)
	{
	    Integer ID = new Integer(BrickID);
	    String hostname = (String)hosts.get(ID);

//  	    int len = table.length;
//  	    int i;
//  	    for (i = 0; i < len; i++) {
//  		if (((Long)heartbeats.get(i)).longValue() == 0) {
//  		    System.out.println("*** Hack: restart later ***");
//  		    return;
//  		}
//  	    }

	    if(!silent)
		System.out.println("*** Restarting Brick " + BrickID + " : " + hostname + " ***");
	    
    	    try{
//    		System.out.println("!!! actual brick restart commented out");
    		Runtime.getRuntime().exec(RESTART_PATH + " " + hostname);
    	    } catch (IOException e) { System.err.println("Error! " + e.getMessage()); }
	    
	    table[hash(BrickID)] = null;
	    heartbeats.put(ID, new Long(0));
	}
	  }

	void ReceiveHeartbeat(int BrickID, String host)
	{	
	    int key = hash(BrickID);
	    Integer ID = new Integer(BrickID);

	    if(table[key] == null)
		{
		    table[key] = new BrickStatus(BrickID);
		    startup.put(ID, new Long(System.currentTimeMillis()));
		}
	    hosts.put(ID, host);
	    
	    if(announceHB)
		System.err.println("Heartbeat Received: Brick " + BrickID + " from " + (String)hosts.get(ID));

	    heartbeats.put(ID, new Long(System.currentTimeMillis()));
	}

    }
    
    BrickManagerImp manager;
    
    public void start(String id, Map args, AnalysisEngine engine) {
	this.engine = engine;

	inputCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
	outputCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);
	sweepTime = ((Integer)
		     args.get(SWEEP_TIME_ARG)).intValue();
	anomalyTimeout = ((Integer)
			  args.get(ANOMALY_TIMEOUT_ARG)).intValue();
	heartbeatTimeout = HEARTBEAT_DURATION * ((Integer)
			      args.get(HEARTBEAT_THRESHOLD_ARG)).intValue();
	restartThreshold = ((Integer)
			    args.get(RESTART_THRESHOLD_ARG)).intValue();
	restartImmunity = ((Integer)
			   args.get(RESTART_IMMUNITY_ARG)).intValue();
	systemImmunity = ((Integer)
			   args.get(SYSTEM_IMMUNITY_ARG)).intValue();
	manager = new BrickManagerImp(N_BRICKS, sweepTime);
	inputCollection.registerListener(this);
    }

    String ParseHost(String data)
    {
	int l = data.indexOf("BrickHost");
	int r = data.indexOf(".", l);

	if(l != -1)
	    {
		l+=10;
		return data.substring(l, r);
	    }
	return "";
    }

    double ParseLevel(String data)
    {
	int l = data.indexOf("rank");
	int r = data.indexOf(",", l);

	if(l != -1)
	    {
		l += 5;
		return Double.valueOf(data.substring(l, r)).doubleValue();
	    }
	return 0;
    }

    int ParseID(String data)
    {
	int l = data.indexOf("x") + 1;
	int r = data.indexOf(".", l);
	    
	if(l != -1)
	    {
		return Integer.parseInt(data.substring(l, r));
	    }
	return -1;
    }
    
    public void addedRecord(String collectionName, Record rec)
    {
	long time = System.currentTimeMillis();
	String host;
	int ID;
	double level;
	
        String data = rec.getValue().toString();
        if(data.length() > 10) {
            String category = (String)rec.getAttribute("key");
            level = 0.0;
//  		level = ((Double)rec.getAttribute("rank")).doubleValue();
//  		System.out.println(rec.getAttribute("rank"));
            if(verbose)
                System.err.println(category + ": " + data);
            while(true) {
                int t = data.indexOf("value");
                if(t == -1)
                    break;
                data = data.substring(t + 5, data.length());
                
                host = ParseHost(data);
                ID = ParseID(data);
                manager.add(ID, category, host, level, time);
            }
        }
    
    }
    
    public void removedRecords(String collectionName, List items) {}

    public void stop()
    {
	manager.stop();
    }
}







