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
package roc.pinpoint.analysis.plugins2.SSM;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.Timer;

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
    public static final String HEARTBEAT_THRESHOLD_ARG = "heartbeatThreshold";
    public static final String ANOMALY_TIMEOUT_ARG = "anomalyTimeout";
    public static final String RESTART_IMMUNITY_ARG = "restartImmunityTime";
    public static final String RESTART_PATH = "/project/cs/iram/c/home/guest/tmartell/ROC/SS/restart.sh";
    public static final int N_BRICKS = 25;
    public static boolean verbose = false;
    public static boolean announceHB = false;
    public static boolean silent = false;
    public static final int HEARTBEAT_DURATION=1050;

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
		       "10")
	    };
    
    private RecordCollection inputCollection;
    private RecordCollection outputCollection;
    protected long sweepTime;
    protected int restartThreshold;
    protected long anomalyTimeout;
    protected long heartbeatTimeout;
    protected long restartImmunity;

    private AnalysisEngine engine;
    
    public PluginArg[] getPluginArguments() {
	return args;
    }
    
    private class BrickManagerImp implements ActionListener {

	private class HeartbeatListener extends Thread {
	    BrickManagerImp listener;
	    
	    private int MC_PORT = 7099;
	    private String MC_GROUP = "228.1.1.1";
	    
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
		    
		    while (true) 
			{
			    byte[] buf = new byte[msgLen];
			    DatagramPacket msg = new DatagramPacket(buf, msgLen);
			    mcs.receive(msg);
			    buf = msg.getData();
			    String tempstring = new String(buf);
			    StringTokenizer strtok = new StringTokenizer(tempstring, "#");
			    if (strtok.countTokens() < 3) 
				{
				    return;
				}
			    
			    int length = Integer.parseInt(strtok.nextToken());
			    int sender = Integer.parseInt(strtok.nextToken());
			    int port = Integer.parseInt(strtok.nextToken());
			    String host = strtok.nextToken();
			    
			    listener.ReceiveHeartbeat(sender, host);
			}
		} catch(Exception e)
		    {
			System.err.println("Exception in heartbeat listener: " + e.getMessage());
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
	    private int NUM_STATS = 9;
	  	    
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
		table.put(category, new anomalyData(category, level, time));
		if(verbose)
		    System.err.println("Adding record to brick " + BrickID);
	    }

	    Set getFaults()
	    {
		return table.keySet();
	    }

	    void clearOld(long time)
	    {
		if(verbose)
		    System.err.println("Sweeping brick " + BrickID + " for old records - starting size: " + size());

		Set keys = table.keySet();
		Iterator i = keys.iterator();

		while(i.hasNext())
		    {
			String key = (String)i.next();
			if(((anomalyData)table.get(key)).getTime() + anomalyTimeout < time)
			    i.remove();
		    }
		if(verbose)
		    System.err.println("End size: " + size());
	    }
	}
	
	private BrickStatus[] table;
	private HeartbeatListener hb;
	private Hashtable heartbeats;
	private Hashtable hosts;
	private Hashtable startup;

	BrickManagerImp(int nBuckets, long sweepTime)
	{
	    this.nBuckets = nBuckets;
	    this.sweepTime = sweepTime;
  
	    table = new BrickStatus[nBuckets];
	    hosts = new Hashtable(nBuckets);
	    heartbeats = new Hashtable(nBuckets);
	    startup = new Hashtable(nBuckets);
	    
	    hb = new HeartbeatListener(this);

            timer = new Timer((int)sweepTime, this);
       	    hb.start();
	    timer.start();
	}
	
	int hash(int key) { return key % nBuckets; }
	
	void add(int BrickID, String category, String host, double level, long time)
	{
	    int key = hash(BrickID);
	    Integer ID = new Integer(BrickID);

	    if(table[key] == null)
		return;

	    if(((Long)heartbeats.get(ID)).longValue() == 0)
		{
		    System.err.println("Anomaly received for dead brick " + BrickID);
		    return;
		}
	    
	    hosts.put(new Integer(BrickID), host);
	    table[key].addAnomaly(category, level, time);
	}

	void stop()
	{
	    timer.stop();
	}

	public void actionPerformed(ActionEvent e)
	{
	    System.err.println("Brick Monitor Plugin pass");
	    for(int i=0; i < nBuckets; i++)
		{
		    if(table[i] != null)
			{
			    long cur = System.currentTimeMillis();
			    int ID = table[i].getID();
			    long hb = ((Long)heartbeats.get(new Integer(ID))).longValue();
			    if(hb + heartbeatTimeout < cur)
				{
				    if(!silent)
					System.err.println("Brick "+ i + " has missed too many heartbeats.");    
				    if(verbose)
					System.err.println("Cur: " + cur + " / Last HB: " + hb);
				    RestartBrick(ID);
				}
			    else
				{
				    table[i].clearOld(cur);
				    if(table[i].size() >= restartThreshold)
					{
					    if(((Long)startup.get(new Integer(ID))).longValue() + restartImmunity > System.currentTimeMillis())
						{
						    if(!silent)
							System.out.println("Attempted to restart " + ID + " but it was within its immunity period");
						}  
					    else
						{
						    if(!silent)
							{
							    System.err.println("Brick " + i + " has reached restart threshold.");
							    System.err.println("  Anomalies in: ");
							    ListErrors(i);
							}
						    RestartBrick(ID);
						}
					}
				}
			}
		}
	}

	void ListErrors(int BrickID)
	{
	    Set errors = table[hash(BrickID)].getFaults();
	    Iterator i = errors.iterator();
	    
	    while(i.hasNext())
		System.err.println("\t" + (String)i.next());
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

	void RestartBrick(int BrickID)
	{
	    Integer ID = new Integer(BrickID);
	    String hostname = (String)hosts.get(ID);

	    if(!silent)
		System.out.println("Restarting Brick " + BrickID + " : " + hostname);
	    
	    try{
		Runtime.getRuntime().exec(RESTART_PATH + " " + hostname + " " + BrickID);
	    } catch (IOException e) { System.err.println("Error! " + e.getMessage()); }
	    
	    table[hash(BrickID)] = null;
	    heartbeats.put(ID, new Long(0));
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
	int l = data.indexOf("BrickID");
	int r = data.indexOf(",", l);
	    
	if(l != -1)
	    {
		l += 8;
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
        if(data.length() > 10)
            {
                String category = (String)rec.getAttribute("key");
                if(verbose)
                    System.err.println(category + ": " + data);
                while(true)
                    {
                        int t = data.indexOf("value");
                        if(t == -1)
                            break;
                        data = data.substring(t + 5, data.length());
                        
                        host = ParseHost(data);
                        ID = ParseID(data);
                        level = ParseLevel(data);
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







