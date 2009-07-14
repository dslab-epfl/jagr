//
// $Id: TheBrain.java,v 1.7 2003/12/12 21:12:18 steveyz Exp $
//

package roc.rr.rm;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.text.SimpleDateFormat;

public class TheBrain 
{
    protected TheBrainThread brainThread = null;
    protected int brainPort = 2374; // A-F-P-I on the telephone pad :-)

    // check the DB for new faults every 3 secs
    protected int checkDBTimeout = 3000; // in milliseconds

    // Used to send data out on port
    protected DatagramSocket restartAgentSocket;

    // restart agent port on the localhost
    protected int restartAgentPort = 1234;
    protected int delayProxyPort = 1313; // port to send pause/unpause messages to
    protected int loadGenPort = 5623; // L-O-A-D on telephone pad

    // enabling/disabling sending message to restart agent
    protected boolean enableRestartMessage = true;

    // when false restarts only failed node, does not try to infer other nodes
    // that may be affected
    protected boolean enableSmartRestart = true;

    // enable restarts without getting an end to end check (e.g., just
    //  after getting an associated failure from ExcMon)
    protected boolean enableEagerRestart = false;

    // options for running with Vanilla JBoss
    protected boolean vanilla = false;
    protected ArrayList appdirlist = null; // full paths
    protected int fullappRBthresh = 4;
    
    // how long to wait for undeployment, redeployment
    protected long waitUndeploy_ms = 7000;
    protected long waitRedeploy_ms = 15000;

    protected int waitVanilla_sec = 120; /* how long to wait to simulate human
                                          * intervention on a none self
                                          recovering setp */

    // rmi configuration parameters
    protected String rmi_host = "localhost"; // hostname where
                                                    // rmiregistry is running
    protected int rmi_port = 1089; // port for rmiregistry (1099 is taken by
                                      // JNDI)
    protected String rmi_name = "RMIntf"; // rmi name to bind to

    // DB server parameters
    protected String db_host = "localhost";
    protected String db_name = "afpi";
    protected String db_user = "afpi";
    protected String db_pass = "afpi";

    // RMDBUtil class contains all the SQL processing stuff
    RMDBUtil dbUtil = null;

    // RMFIUtil class contains code necessary to invoke microReboot and
    // FaultInjection procedure via the jmx-console
    RMFIUtil fiUtil = null;

    // other configuration parameters
    protected long rr_thresh_ms = 60000;

    // tracing
    public boolean traceFlag = false;

    protected void traceMsg(String msg)
    {
        if(traceFlag)
        {
            System.out.print(msg);
        }
        
    }

    public void enableTrace()
    {
        traceFlag = true;
        if(dbUtil != null)
        {
            dbUtil.enableTrace();
        }
    }
    
    public void disableTrace()
    {
        traceFlag = false;
        if(dbUtil != null)
        {
            dbUtil.disableTrace();
        }
    }

    // default constructor
    public TheBrain() 
    {
        waitConf = new HashMap();
   
	try
        {
	    restartAgentSocket = new DatagramSocket();
	}
        catch(SocketException e)
        {
            System.err.println("TheBrain: restartAgentSocket failed to bind to a UDP port!");            
	    e.printStackTrace();
	}
    }

    // start periodically checking failure reports
    public boolean StartBrainServices ()
    {
        try
        {
            brainThread = new TheBrainThread();
            brainThread.start();
            return true;
        }
        catch (SocketException e)
        {
            System.out.println("Could not bind to UDP port # " + brainPort);
            return false;
        }
    }

    // stop checking
    public void StopBrainServices ()
    {
        brainThread.stop = true;
        brainThread = null;
    }

    // remote services

    public long timeSinceLastFault_ms()
    {
        long ms = -1;
        try
        {
            ms = dbUtil.timeSinceLastFault_ms();
            System.out.println("++++++ timeSinceLastFault_ms() invoked, result = " +
                                ms + "++++++");
        }
        catch (Exception e)
        {
            System.out.println("+++++ timeSinceLastFault_ms() invocation failed, database error +++++");
        }
    
        return ms;
    }

    // internal functions

    protected long lastFaultChecked = 0; // the time of the last fault that we
                                         // saw when we last checked the db
    protected HashMap waitConf = null;

    protected void checkFaults ()
    {
        // periodically check for new failures in the DB
        ArrayList newFaults = null;
        
        try
        {
            Map pMap = dbUtil.getParentMap();
            newFaults = dbUtil.getFaultsSince(lastFaultChecked);
            Iterator iter = newFaults.iterator();

            while(iter.hasNext())
            {
                RMDBUtil.FaultInfo fInfo = (RMDBUtil.FaultInfo)iter.next();
                lastFaultChecked = fInfo.occurred;
                traceMsg("*****\nNew fault in database found!\n");
                traceMsg("Comp = " + fInfo.component + ", Occurred = " + fInfo.occurred
                         + ", Monitor = " + fInfo.monitorID + "\n");
                if(fInfo.monitorID.compareTo("E2EMon") == 0)
                {
                    // end to end failure
                    processE2EFailure();
                    continue;
                }
                if(dbUtil.compRbSince(fInfo.component, fInfo.occurred))
                {
                    // the failing component has been rebooted since this fault occurred
                    // so just ignore this error
                    traceMsg("Failing component was rebooted after 'occurred' time, ignoring!\n");
                    continue;
                }
                // let's see what we did for all the previous instances of this
                // fault (note list is ordered by latest fault first)
                ArrayList prevCompFaults = dbUtil.getPrevCompFaults(fInfo.component, fInfo.occurred);
                Iterator pf_iter = prevCompFaults.iterator();
                if(pf_iter.hasNext())
                {
                    // for now, we only check what we did for the last occurrance of
                    // this fault
                    RMDBUtil.FaultInfo pfInfo = (RMDBUtil.FaultInfo)pf_iter.next();
                    if(pfInfo.rebootID > 0)
                    {
                        // previous fault triggered a reboot
                        // if that reboot is less than a minute ago, we assume
                        // that it failed, and we move up to the next level
                        // else, we just reboot the same components again
                        traceMsg("previous failure of this component triggered a reboot!\n");
                        ArrayList prevRbComps = dbUtil.getRbComps(pfInfo.rebootID);
                        ArrayList causes = new ArrayList();
                        causes.add(new Long(fInfo.occurred));

                        if((System.currentTimeMillis() - dbUtil.getRbTime(pfInfo.rebootID))
                           < rr_thresh_ms)
                        {
                            List rbComps = addAllDeps(prevRbComps);
                            rbComps = getNextLevel(rbComps, pMap);
                            rebootComps(rbComps, causes);
                        }
                        else
                        {
                            rebootComps(prevRbComps, causes);
                        }
                    }
                    else
                    {
                        // previous instance of this fault did not trigger a reboot
                        // check to see if fault confirms a previous fault
                        RMDBUtil.FaultInfo wcInfo = (RMDBUtil.FaultInfo)waitConf.get(fInfo.component);
                        if(wcInfo != null)
                        {
                            traceMsg("new fault confirmed previous fault, rebooting!\n");
                            // trigger restart
                            ArrayList rbComps = new ArrayList();
                            ArrayList causes = new ArrayList();
                            causes.add(new Long(fInfo.occurred));
                            causes.add(new Long(wcInfo.occurred));
                            rbComps.add(fInfo.component);
                            rbComps = addAllDeps(rbComps);
                        }
                        else
                        {
                            traceMsg("adding fault to waitConf list\n");
                            // add fault to the waitConf list
                            waitConf.put(fInfo.component, fInfo);
                        }
                    }
                }
                else
                {
                    // this comp has never failed before, so we just ignore it for
                    // now and wait for confirmation from another failure detector
                    // add it to the waitconf list
                    traceMsg("adding fault to waitConf list\n");
                    waitConf.put(fInfo.component, fInfo);
                }
            }

        }
        catch(RMDBUtil.DBUtilException e)
        {
            System.out.println("checkFaults() failed!");
            System.out.println(e.getMessage());
            return;
        }
    }

    protected void rebootComps(List components, List cause_faults)
    {
        traceMsg("rebootComps invoked for components: " + components.toString()
                 + "\n    cause_faults: " + cause_faults + "\n\n");
        // will use the fiUtil to reboot the request components and also use
        // dbUtil to record this reboot in the dababase
        Iterator cIter = components.iterator();
        long restartTime = System.currentTimeMillis();
        // restart all the components
        try
        {
            // record it in the database
            dbUtil.newReboot(restartTime, components, cause_faults);

            while(cIter.hasNext())
            {
                fiUtil.doMicroReboot((String)cIter.next());
            }
        }
        catch(RMFIUtil.FIUtilMethodFailureException e)
        {
            if(e.getCause() == null)
            {
                System.out.println("Reboot Method Returned the following error: ");
                System.out.println("***** \"" + e.getMessage() + "\" *****");
            }
            else
            {
                System.out.println("doReboot failed: " + e.getMessage());
                System.out.println("Caused by: \n" + e.getCause().toString());
            }            
        }
        catch(RMDBUtil.DBUtilException e)
        {
            System.out.println("Error recording reboot to database: " + e.getMessage());
        }
    }

    protected List lastRebootComps = null;

    protected void processE2EFailure()
    {
        traceMsg("processE2EFailure() invoked!\n");
        try
        {
            // get all the pending faults in the waitConf map
            Set wc_entrySet = waitConf.entrySet();
            List faults = new ArrayList();
            Iterator wcs_iter = wc_entrySet.iterator();
            while(wcs_iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)wcs_iter.next();
                faults.add(entry.getValue());
            }    
            
            // now build the list of components that have failed that may need a reboot
            Iterator iter = faults.iterator();
            List rbComps = new ArrayList();
            Map pMap = dbUtil.getParentMap(); // map of all child->parent component relations
        
            waitConf.clear(); // reset the waitConf map

            while(iter.hasNext())
            {
                RMDBUtil.FaultInfo info = (RMDBUtil.FaultInfo) iter.next();
                if(dbUtil.compRbSince(info.component, info.occurred))
                {
                    // remove from the fault list since this comp has been rebooted
                    // already (after the occurred time of the fault)
                    iter.remove();
                }
                else
                {
                    rbComps.add(info.component);
                }
            }

            // add components that may be correlated with components on the list
            rbComps = addAllDeps(rbComps);

            // filter out components who's parents are in the list
            filterList(rbComps, pMap);

            // if we are trying do reboot the same set of components as last time,
            // try to restart one higher level up
            if((lastRebootComps != null) && isContained(rbComps, lastRebootComps, pMap)
               && isContained(lastRebootComps, rbComps, pMap))
            {
                rbComps = (ArrayList)getNextLevel(rbComps, pMap);
            }

            rebootComps(rbComps, faults);
            lastRebootComps = rbComps;
        }
        catch(RMDBUtil.DBUtilException e)
        {
            System.out.println("processE2EFailure() failed!");
            System.out.println(e.getMessage());
            return;
        }
    }

    public void reportFailure (FailureReport report) 
    { 
        traceMsg("FAILURE REPORT RECEIVED, TIMESTAMP = " + 
                 (new SimpleDateFormat("HH:mm:ss,S")).format(report.timeStamp) + "\n");

        if(report.failureType == FailureReport.failureTypeEndToEnd)
        {
            processE2EFailure();
        }
        else 
        {
            // invalid failure report (throw exception?)
            System.err.println("Invalid failure report object received!");
        }
    }

    protected List lastFaults = null; // last list of faults restarted
    
    private List getNextLevel(List list, Map parentMap)
    {
        // for each component, get the parent
        ArrayList newList = new ArrayList();
        
        Iterator iter = list.iterator();
        while(iter.hasNext())
        {
            String comp = (String)iter.next();
            String parent = (String)parentMap.get(comp);
            if((parent != null) && (newList.indexOf(parent) == -1))
            {
                // parent has not been added to list already
                newList.add(parent);
            }
        }
        
        filterList(newList, parentMap);
        
        return newList;
    }

    private ArrayList removeListDups(List list)
    {
        ArrayList newList = new ArrayList();
        Iterator iter = list.iterator();
        while(iter.hasNext())
        {
            Object o = iter.next();
            if(newList.indexOf(o) == -1)
            {
                newList.add(o);
            }
        }
        return newList;
    }

    private void filterList(List list, Map parentMap)
    {
        // filter out all components where that comp's ancestor is in the list
        // also removes duplicates
        Iterator iter = list.iterator();
        while(iter.hasNext())
        {
            String comp = (String)iter.next();
            String parent = (String) parentMap.get(comp);
            while(parent != null)
            {
                if(list.indexOf(parent) != -1)
                {
                    // remove comp from faults
                    list.remove(list.indexOf(comp));
                    break;
                }
                parent = (String) parentMap.get(parent);
            }
        }
    }

    private boolean isContained(List inner, List outer, Map parentMap)
    {
        // is 'inner' contained in 'outer'?
        // means that for every component in inner, that component or
        // one of it's ancestors is in outer
        if(inner == outer) // if they are the same list (or both null)
        {
            return true;
        }
        if((inner == null) || (outer == null))
        {
            return false;
        }
        
        boolean match = true;

        Iterator iter = inner.iterator();
        while(iter.hasNext())
        {
            String comp = (String)iter.next();
            boolean contain = false;
            while(comp != null)
            {
                if(outer.indexOf(comp) != -1)
                {
                    contain = true;
                    break;
                }
                comp = (String) parentMap.get(comp);
            }
            if(!contain)
            {
                match = false;
                break;
            }
        }

        return match;
    }

    protected ArrayList addAllDeps(List comps) throws RMDBUtil.DBUtilException
    {
        // for every components in the list comps, add all components that are
        // determined to be correlated to it (as determined by
        // dbUtil.getDepList())

        Map pMap = dbUtil.getParentMap();
        ArrayList allComps = new ArrayList();
        allComps.addAll(comps);
        Iterator iter = comps.iterator();
        while(iter.hasNext())
        {
            String comp = (String)iter.next();
            allComps.addAll(dbUtil.getDepList(comp));
        }
        allComps = removeListDups(allComps);
        filterList(allComps, pMap);

        return allComps;
    }

    protected void redeployFullApp(ArrayList pathlist) throws IOException, InterruptedException
    {
        Process p;
        String cmd;
        
        System.out.println("Undeploying application ...");
        for(int i = 0; i < pathlist.size(); i++)
        {                
            cmd = "/bin/mv " + pathlist.get(i) + " /tmp";
            //DEBUGMSG
            System.out.println("Undeploying file, cmd = \"" + cmd + "\"");
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor(); // wait for p to complete
            if(p.exitValue() != 0)
            {
                System.out.println("Undeploying file failed!, code = " + p.exitValue());
            }
        }
        
        Thread.sleep(waitUndeploy_ms); // wait for JBoss to undepoly app

        System.out.println("Redeploying application ...");
        for(int i = 0; i < pathlist.size(); i++)
        {
            String apppath = (String) pathlist.get(i);
            cmd = "/bin/mv /tmp/" + apppath.substring(apppath.lastIndexOf('/') + 1) +
                  " " + apppath;
            System.out.println("Redeploying file, cmd = \"" + cmd + "\"");
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor(); // wait for p to complete
            if(p.exitValue() != 0)
            {
                System.out.println("Redeploying file failed!, code = " + p.exitValue());
            }
        }
        
        Thread.sleep(waitRedeploy_ms); // wait for JBoss to redeploy app        
    }

    private class TheBrainThread extends Thread 
    {
	DatagramSocket socket = null;
        boolean stop = false;
        
        public TheBrainThread() throws SocketException
        {
	    socket = new DatagramSocket(brainPort);
            try
            {
                String addr = InetAddress.getLocalHost().toString();
                System.out.println("THE BRAIN IS LISTENING ON UDP PORT #" + brainPort 
                                   + " of " + addr);
            }
            catch(java.net.UnknownHostException e)
            {
                System.out.println("THE BRAIN IS LISTENING ON UDP PORT #" + brainPort);		      	
            }      
	}
    
	private FailureReport getFailureReport() throws IOException, SocketException, ClassNotFoundException
        {
	    byte[] buf = new byte[1024];
	    DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(checkDBTimeout);
	    socket.receive(packet);
	    ByteArrayInputStream bArray_in = new ByteArrayInputStream(buf);
	    ObjectInputStream obj_in = new ObjectInputStream(bArray_in);
	    FailureReport report = (FailureReport) obj_in.readObject();    
	    return report;
	}

        public void run() {
            // listen for failure messages
            while(stop == false) {
                // receive a packet
                FailureReport report;
                
                try 
                {
                    report = getFailureReport();
                    //System.out.println("THE BRAIN: DEBUG: Received Packet ... " + report.toString());
		}
                catch (SocketTimeoutException e)
                {
                    // times out every few seconds so that we can examine the
                    // database for new faults
                    checkFaults();
                    continue;
                }
                catch (Exception e)
                {
                    System.err.println("THE BRAIN: ERROR RECEIVING PACKET!");
                    continue;
		}

                reportFailure(report);
            }
        }
    }

    protected void sendProxyMessage(boolean pause)
    {
        try
        {
            byte[] buf = new byte[1];
            if(pause)
            {
                buf[0] = (byte)'P';
            }
            else
            {
                buf[0] = (byte)'U';
            }
            
            DatagramSocket s = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, 
                                                       InetAddress.getLocalHost(),
                                                       delayProxyPort);
            s.send(packet);
            if(pause)
                System.out.println("THE BRAIN: PAUSE MESSAGE SENT TO DELAYPROXY!");
            else
                System.out.println("THE BRAIN: UNPAUSE MESSAGE SENT TO DELAYPROXY!");
        }
        catch(Exception e)
        {
            if(pause)
                System.err.println("THE BRAIN: FAILED TO SEND PAUSE MESSAGE!");
            else
                System.err.println("THE BRAIN: FAILED TO SEND UNPAUSE MESSAGE!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        TheBrain brain = new TheBrain();
        String rmi_fullpath = null;

        // process command line arguments
        int argindex;
        for(argindex = 0; argindex < args.length; argindex++)
        {
            if(args[argindex].equalsIgnoreCase("-h") ||
               args[argindex].equalsIgnoreCase("-help"))
            {   // print help message
                System.out.println("Usage: java org.jboss.RR.TheBrain [options] [app dirs ...]");
                System.out.println("[app dirs ...] points to application deployment files, needed for -vanilla option or recursive restarts!");
                System.out.println("Available Options: ");
                System.out.println(" -h, -help          Displays this message");
                System.out.println(" -bp <port#>        Has the brain listen on port# (default = "     
                                   + brain.brainPort + ") ");
                System.out.println(" -rp <port#>        Send restart message to port# (default = "
                                   + brain.restartAgentPort + ") ");
                System.out.println(" -dp <port#>        Send pause message to (delay proxy) port# (default = "
                                   + brain.delayProxyPort + ") ");  
                System.out.println(" -lp <port#>        LoadGen port # (default = " 
                                   + brain.loadGenPort + ") ");              
                System.out.println(" -norestart         Starts with sending of restarted messages disabled");
                System.out.println(" -simplerestart     Restarts directly failed nodes only, does not consult failure history");
                System.out.println(" -vanilla           Run with unmodified jboss, needs path of app deploy file(s)");
                System.out.println(" -vanillawait <secs> number of seconds to wait before doing restart, default = " + 
                                   brain.waitVanilla_sec + ")");
                System.out.println(" -redpwait <ms>     Redeployment wait time in milliseconds (default = "
                                   + brain.waitRedeploy_ms + ") ");
                System.out.println(" -undpwait <ms>     Undeployment wait time in milliseconds (default = "
                                   + brain.waitUndeploy_ms + ") ");

                // DB options
                System.out.println(" -dbhost <host>     Host of db server process (default = "
                                   + brain.db_host + ") ");
                System.out.println(" -dbname <name>     Name of database to use (default = "
                                   + brain.db_name + ") ");
                System.out.println(" -dbuser <username> Username for logging into database (default = "
                                   + brain.db_user + ") ");
                System.out.println(" -dbpass <password> Password for logging into database (default = "
                                   + brain.db_pass + ") "); 

                // RMI options
                System.out.println(" -rmihost <host>    Hostname of rmiregistry (default = "
                                   + brain.rmi_host + ") ");
                System.out.println(" -rmiport <port#>   Port of rmiregistry (default = "
                                   + brain.rmi_port + ") ");
                System.out.println(" -rminame <name>    Name of rmi binding (default = "
                                   + brain.rmi_name + ") ");
                System.out.println(" -trace             Enable debug messages");
                return;
            }
            else if(args[argindex].equalsIgnoreCase("-bp"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -bp option!");
                    return;
                }
                try
                {
                    brain.brainPort = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-rp"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -rp option!");
                    return;
                }
                try
                {
                    brain.restartAgentPort = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-dp"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -dp option!");
                    return;
                }
                try
                {
                    brain.delayProxyPort = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-lp"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -lp option!");
                    return;
                }
                try
                {
                    brain.loadGenPort = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-norestart"))
            {
                brain.enableRestartMessage = false;
            }
            else if(args[argindex].equalsIgnoreCase("-simplerestart"))
            {
                brain.enableSmartRestart = false;
            }
            else if(args[argindex].equalsIgnoreCase("-vanilla"))
            {
                brain.vanilla = true;
            }
            else if(args[argindex].equalsIgnoreCase("-vanillawait"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Seconds required for -vanillawait option!");
                    return;
                }
                try
                {
                    brain.waitVanilla_sec = Integer.parseInt(args[argindex]);
                    if(brain.waitVanilla_sec < 0)
                    {
                        System.err.println("Wait seconds parameter must be >= 0");
                        return;
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-redpwait"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Milliseconds required for -redpwait option!");
                    return;
                }
                try
                {
                    brain.waitRedeploy_ms = Long.parseLong(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-undpwait"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Milliseconds required for -undpwait option!");
                    return;
                }
                try
                {
                    brain.waitUndeploy_ms = Long.parseLong(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-dbhost"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Parameter required for -dbhost option!");
                    return;
                }
                brain.db_host = args[argindex];
            }
            else if(args[argindex].equalsIgnoreCase("-dbname"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Parameter required for -dbname option!");
                    return;
                }
                brain.db_name = args[argindex];
            }
            else if(args[argindex].equalsIgnoreCase("-dbuser"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Parameter required for -dbuser option!");
                    return;
                }
                brain.db_user = args[argindex];
            }            
            else if(args[argindex].equalsIgnoreCase("-dbpass"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Parameter required for -dbpass option!");
                    return;
                }
                brain.db_pass = args[argindex];
            }
            else if(args[argindex].equalsIgnoreCase("-rmihost"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Parameter required for -rmihost option!");
                    return;
                }
                brain.rmi_host = args[argindex];
            }            
            else if(args[argindex].equalsIgnoreCase("-rminame"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Parameter required for -rminame option!");
                    return;
                }
                brain.rmi_name = args[argindex];
            }
            else if(args[argindex].equalsIgnoreCase("-rmiport"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -rmiport option!");
                    return;
                }
                try
                {
                    brain.rmi_port = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-trace"))
            {
                brain.traceFlag = true;
            }
            else
            {
                if(args[argindex].startsWith("-"))
                {   
                    System.err.println("Invalid option!  Use '-h' for help");
                    return;
                }
                else
                {
                    break;
                }
            }    
        }

        // process appdir paths, if any
        for(; argindex < args.length; argindex++)
        {
            if(brain.appdirlist == null)
            {
                brain.appdirlist = new ArrayList();
            }

            String path = args[argindex];
            if(path.endsWith("/"))
            {
                // chop off ending /, if necessary
                path = path.substring(0, path.length() - 1);
            }
            brain.appdirlist.add(path);
        }

        if(brain.vanilla && (brain.appdirlist == null))
        {
            System.err.println("-vanilla option requires application path(s) to be specified!");
            return;
        }

        // initialize the database
        try
        {
            brain.dbUtil = new RMDBUtil(brain.db_host, brain.db_name, 
                                        brain.db_user, brain.db_pass, brain.traceFlag);
        }
        catch (Exception ex)
        {
            System.err.println("Uh-oh: " + ex.getMessage());
            ex.getCause().printStackTrace();
            System.exit(0);
        }

        // initialize fiUtil
        brain.fiUtil = new RMFIUtil(brain.loadGenPort);        

        brain.StartBrainServices();
        System.out.println("Welcome to the Recovery Manager!");
        System.out.println("--------------------------------");
        System.out.println("Restart Agent Port: " + brain.restartAgentPort);
        System.out.println("Delay Proxy Message Port: " + brain.delayProxyPort);
        System.out.println("LoadGen Port: " + brain.fiUtil.loadgen_port);
        System.out.println("Database Connection Info: " + brain.db_user + ":" + 
                           brain.db_pass + "@" + brain.db_host + "/" + brain.db_name);
        System.out.print("Debug messages are ");
        if(brain.traceFlag)
        {
            System.out.print("ENABLED!\n");
        }
        else
        {
            System.out.print("DISABLED!\n");
        }

        if(brain.enableRestartMessage)
        {
            System.out.println("Sending of restart message is currently ENABLED!");
        }
        else
        {
            System.out.println("Sending of restart message is currently DISABLED!");
        }
        if(!(brain.enableSmartRestart))
        {
            System.out.println("Note: Using simple restart (not consulting f-map)!");
        }        

        if(brain.appdirlist != null)
        {
            if(brain.vanilla)
            {
                System.out.println("***Interacting with Vanilla JBoss!***");
            }
            else
            {
                System.out.println("***Using 2 level RECURSIVE restart logic!***");
            }

            String paths = null;
            for(int index = 0; index < brain.appdirlist.size(); index++)
            {
                if(paths == null)
                {
                    paths = (String)brain.appdirlist.get(index);
                }
                else
                {
                    paths = paths + "; " + brain.appdirlist.get(index);
                }
            }
            
            System.out.println("   Application File Path(s): " + paths);
            System.out.println("   Undeploy wait time (ms): " + brain.waitUndeploy_ms);
            System.out.println("   Redeploy wait time (ms): " + brain.waitRedeploy_ms);   
        }
        else
        {
            System.out.println("*** Not using recursive restart logic ***");
        }

        while(true)
        {
            System.out.println("\n\nPlease select from the following options:");
            if(brain.enableRestartMessage)
            {
                System.out.println("1) Disable sending of restart messages");
            }
            else
            {
                System.out.println("1) Enable sending of restart messages");
            }
            if(brain.enableSmartRestart)
            {
                System.out.println("2) Use simple restarts (do not consult failure history)");
            }
            else
            {
                System.out.println("2) Use normal restarts (consults failure history)");
            }
            System.out.println("3) Send Pause Message");
            System.out.println("4) Send Unpause Message");
            System.out.println("5) Inject End To End Failure (trigger restart!)");
            System.out.println("6) Vanilla wait to RB time (Current = " +
                               brain.waitVanilla_sec + " secs)");            
	    System.out.println("7) " + 
			       (brain.enableEagerRestart?"Disable":"Enable") + 
			       " Eager Restarts (don't wait for e2e failure)" );
            System.out.println("8) Exit");
            System.out.println("9) Invoke timeSinceLastFault_ms()");
            System.out.println("10) Reboot Component");
            System.out.println("11) Inject Fault");
            System.out.println("12) Send Start Load Message");
            System.out.println("13) Send Stop Load Message");
            System.out.println("14) START AFPI EXPERIMENT!");
            if(brain.traceFlag)
            {    
                System.out.println("15) DISABLE debug messages");
            }
            else
            {
                System.out.println("15) ENABLE debug messages");
            }
            System.out.println("16) Add Fault to DB");
            System.out.print("Command> ");

            /* process user request */
            try
            {
                String cmd = stdin.readLine();              
                if(Integer.parseInt(cmd) == 1)
                {
                    brain.enableRestartMessage = !brain.enableRestartMessage;
                    if(brain.enableRestartMessage)
                    {
                        System.out.println("*** Sending of restart messages now ENABLED ***");
                    }
                    else
                    {
                        System.out.println("*** Sending of restart messages now DISABLED ***");
                    }   
                }
                else if(Integer.parseInt(cmd) == 2)
                {
                    brain.enableSmartRestart = !brain.enableSmartRestart;
                    if(brain.enableSmartRestart)
                    {
                        System.out.println("*** Consulting failure history for restarts now ENABLED ***");
                    }
                    else
                    {
                        System.out.println("*** Consulting failure history for restarts now DISABLED (simple restarts)***");
                    }   
                }
                else if(Integer.parseInt(cmd) == 3)
                {
                    brain.sendProxyMessage(true);
                }
                else if(Integer.parseInt(cmd) == 4)
                {
                    brain.sendProxyMessage(false);
                }
                else if(Integer.parseInt(cmd) == 5)
                {   // trigger end to end failure
                    brain.reportFailure(new FailureReport(new Date()));
                }
                else if(Integer.parseInt(cmd) == 6)
                {                    
                    System.out.print("Enter vanilla wait to RB time (seconds): ");
                    String num = stdin.readLine();
                    try
                    {
                        if(Integer.parseInt(num) < 0) /* not allowed */
                        {
                            System.out.println("Error: Wait time must be >= 0 secs");
                        }
                        else
                        {
                            brain.waitVanilla_sec = Integer.parseInt(num);
                            System.out.println("*** Vanilla wait to RB time set to " + brain.waitVanilla_sec
                                + " secs ***");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("Error: Invalid number: " + num);
                    }
                }
		else if(Integer.parseInt(cmd) == 7) 
		{
		    brain.enableEagerRestart = !brain.enableEagerRestart;
		}
                else if(Integer.parseInt(cmd) == 8)
                {
                    break; // quit
                }
                /* do a database expression */
                else if(Integer.parseInt(cmd) == 9)
                {           
                    brain.timeSinceLastFault_ms();
                }
                else if(Integer.parseInt(cmd) == 10)
                {
                    System.out.print("Enter component name: ");
                    String comp = stdin.readLine();
                    try
                    {
                        brain.fiUtil.doMicroReboot(comp);
                        System.out.println("Success!");
                    }
                    catch(RMFIUtil.FIUtilMethodFailureException e)
                    {
                        if(e.getCause() == null)
                        {
                            System.out.println("Reboot Method Returned the following error:");
                            System.out.println("***** \"" + e.getMessage() + "\" *****");
                        }
                        else
                        {
                            System.out.println("doReboot failed: " + e.getMessage());
                            System.out.println("Caused by: \n" + e.getCause().toString());
                        }
                    }
                }
                else if(Integer.parseInt(cmd) == 11)
                {
                    System.out.print("Enter component name: ");
                    String comp = stdin.readLine();
                    try
                    {
                        brain.fiUtil.scheduleFault(comp,"exception");
                        System.out.println("Success!");
                    }
                    catch(RMFIUtil.FIUtilMethodFailureException e)
                    {
                        if(e.getCause() == null)
                        {
                            System.out.println("ScheduleFault Method Returned the following error:");
                            System.out.println("***** \"" + e.getMessage() + "\" *****");
                        }
                        else
                        {
                            System.out.println("scheduleFault failed: " + e.getMessage());
                            System.out.println("Caused by: \n" + e.getCause().toString());
                        }
                    }
                }
                else if(Integer.parseInt(cmd) == 12)
                {
                    try
                    {
                        brain.fiUtil.startLoad();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if(Integer.parseInt(cmd) == 13)
                {
                    try
                    {
                        brain.fiUtil.stopLoad();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if(Integer.parseInt(cmd) == 14)
                {
                    try {
                        (new AFPIDriver(brain.dbUtil, brain.fiUtil)).runExperiment();
                    }
                    catch( Exception e ) {
                        e.printStackTrace();
                    } 
                }
                else if(Integer.parseInt(cmd) == 15)
                {
                    if(brain.traceFlag)
                    {
                        System.out.println("*** Disabling debug messages! ***");
                        brain.disableTrace();
                    }
                    else
                    {
                        System.out.println("*** Enabling debug messages! ***");
                        brain.enableTrace();
                    }
                }
                else if(Integer.parseInt(cmd) == 16)
                {
                    // add fault to DB
                    System.out.print("Enter component name: ");
                    String comp = stdin.readLine();
                    long occurred;
                    while(true)
                    {
                        System.out.print("Enter occurred time (now = " + System.currentTimeMillis() + "): ");
                        try
                        {
                            occurred = Long.parseLong(stdin.readLine());
                        }
                        catch(NumberFormatException e)
                        {
                            System.out.println("Invalid integer!, try again");
                            continue;
                        }
                        break;
                    }
                    System.out.print("Enter source (afpi or not): ");
                    String source = stdin.readLine();
                    System.out.print("Enter Monitor ID (E2EMon, ExceptionMon, or Pinpoint): ");
                    String monitorID = stdin.readLine();
                    
                    try
                    {
                        brain.dbUtil.addFault(comp, occurred, source, monitorID);
                        System.out.println("New fault added!");
                    }
                    catch(RMDBUtil.DBUtilException e)
                    {
                        System.err.println("Failed to add new fault to DB!");
                    }                        
                }
                else
                {
                    System.out.println("Invalid option, please try again!");
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid option, please try again!");
            }
            catch (IOException e)
            {
                break; // just quit
            }   
        }
        System.out.println("\nGoodbye!");
        brain.StopBrainServices();

        System.exit(0);
    }
}


    


        

