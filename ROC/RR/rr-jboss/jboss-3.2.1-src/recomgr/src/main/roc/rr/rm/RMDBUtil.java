//
// $Id: RMDBUtil.java,v 1.5 2003/12/12 21:12:18 steveyz Exp $
//

/** 
 * Class used by Recovery Manager (TheBrain.java) to interact with the afpi
 *     database
 *
 *    @author  steveyz@cs.stanford.edu
 *    @version $Revision: 1.5 $
 */

package roc.rr.rm;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class RMDBUtil
{
    /* Connection to database */
    public Connection db = null;
    protected static String db_drivername = "com.mysql.jdbc.Driver";
    protected boolean traceFlag = false;

    // depPeriod_ms represents the time (in milliseconds) that two faults needs
    // to occur within to be considered possibiliy correlated
    protected long depPeriod_ms = 10000; // 10 secs
    // minConf is basically how correlated two faults have to be to be
    // considered as correlated (e.g. 0.5 means that 50% of the time when
    // comp A fails, comp B must fail soon after for it to be considered a 
    // dependency between the two
    // minSupport is the number of times B must fail (to ensure that it's not
    // a fluke
    protected double minConf = 0.5;
    protected long minSupport = 3;

    // utility classes

    public class FaultInfo extends Object
    {
        public FaultInfo() 
        {
        }
        
        public String component; //name
        public String method; // method of component that generated fault
        public long occurred;
        public String description;
        public String source; // afpi or not
        public String notes;
        public long rebootID;
        public String monitorID; // E2E or ExcMon or Pinpoint
    }

    public class CompInfo extends Object
    {
        public CompInfo()
        {
        }

        public String UID;
        public String name;
        public String type;
        public String parent;
        public String methods;
    }

    public class DBUtilException extends Exception
    {
        public DBUtilException(String s)
        {
            super(s);
        }
        
        public DBUtilException(String s, Throwable cause)
        {
            super(s, cause);
        }
    }

    // methods for debug tracing

    protected void traceMsg(String str)
    {
        if(traceFlag)
        {
            System.out.print(str);
        }
    }
    
    public void enableTrace()
    {
        traceFlag = true;
    }
    
    public void disableTrace()
    {
        traceFlag = false;
    }

    // methods to set configuration parameters
    public void setDepPeriod_ms(long period)
    {
        depPeriod_ms = period;
    }
    
    public void setMinConf(double level)
    {
        minConf = level;
    }
    
    public void setMinSupport(long support)
    {
        minSupport = support;
    }
    

    // constructors

    public RMDBUtil (String hostname, String dbname,
                     String username, String password) throws DBUtilException
    {
        initDB(hostname, dbname, username, password);
    }

    public RMDBUtil (String hostname, String dbname,
                     String username, String password, boolean trace) throws DBUtilException
    {
        // set tracing
        traceFlag = trace;
        initDB(hostname, dbname, username, password);
    }

    protected void initDB(String hostname, String dbname, String username, 
                          String password) throws DBUtilException
    {
	/* Set up database */
        try
        {
            traceMsg("+++++ Opening connection to DB +++++\n");            
            Class.forName(db_drivername).newInstance(); 
            String url = "jdbc:mysql://" + hostname + "/" + dbname + "?user=" + 
                username + "&password=" + password;
            traceMsg("Connecting to database '" + dbname + "' on host '"
                         + hostname + "' ... ");
            db = DriverManager.getConnection(url);
            traceMsg(" success!\n");
        }
        catch (Exception ex)
        {
            if(ex instanceof SQLException)
            {
                throw new DBUtilException("Failed to connect to database!", ex);
            }
            else
            {
                throw new DBUtilException("Failed to load database driver!", ex);
            }
        }
        // verify database has correct schema
        if(!checkTables())
        {
            throw new DBUtilException("Database has incorrect schema!");
        }
    }

    /* check to see that the necessary tables exist */
    private boolean checkTables()
    {
        // returns false if the requirements are not met
        boolean found_components = false;
        boolean found_faults = false;
        boolean found_reboots = false;

        traceMsg("Verifying database schema ...\n");

        // check to see if the necessary tables are there
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW tables");
            while(rs.next())
            {
                if(rs.getString(1).trim().compareToIgnoreCase("components") == 0)
                {
                    found_components = true;
                }
                else if(rs.getString(1).trim().compareToIgnoreCase("faults") == 0)
                {
                    found_faults = true;
                }
                else if(rs.getString(1).trim().compareToIgnoreCase("reboots") == 0)
                {
                    found_reboots = true;
                }
            }
        }
        catch(Exception e)
        {
            handleException(e);
            return false; // verifying tables failed!
        }

        if((!found_components) || (!found_faults))
        {
            // a required table does not exist
            traceMsg("Components and/or faults table not found in database!\n");
            return false;
        }
        
        // check to see if faults table has column for RebootID and MonitorID
        try
        {
            boolean foundRebootID = false;
            boolean foundMonitorID = false;
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW FIELDS FROM faults");
            while(rs.next())
            {
                if(rs.getString(1).trim().compareToIgnoreCase("RebootID") == 0)
                {
                    foundRebootID = true;
                }
                if(rs.getString(1).trim().compareToIgnoreCase("MonitorID") == 0)
                {
                    foundMonitorID = true;
                }
            }

            if(!foundRebootID)
            {
                traceMsg("faults table does not have RebootID column!");
                return false;
            }
            if(!foundMonitorID)
            {
                traceMsg("faults table does not have MonitorID column!");
                return false;
            }
        }
        catch(Exception e)
        {
            handleException(e);
            return false; 
        }

        // if the reboot table does not exist, create it
        if(!found_reboots)
        {
            traceMsg("Reboots table not found, attempting to create ...");
            try
            {
                Statement stmt = db.createStatement();  
                stmt.executeUpdate("CREATE TABLE reboots (" +
                                   "RebootID VARCHAR(255)," +
                                   "CompUID VARCHAR(255)," + // component UID
                                   "RebootTime VARCHAR(255) )");
                stmt.close();
            }
            catch (Exception e)
            {
                handleException(e);
                traceMsg("\nError creating 'reboots' table!\n");
                return false;
            }
            traceMsg(" success!\n");
        }
        else
        {
            // check to see that the reboot table has the right schema
            try
            {
                Statement stmt = db.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW FIELDS FROM reboots");

                if(rs.next() && 
                   (rs.getString(1).trim().compareToIgnoreCase("RebootID") == 0))
                {
                    if(rs.next() && 
                       (rs.getString(1).trim().compareToIgnoreCase("CompUID") == 0))
                    {
                        if(rs.next() && 
                           (rs.getString(1).trim().compareToIgnoreCase("RebootTime") == 0))
                        {
                            if(rs.next()) // shouldn't be anymore columns
                            {
                                traceMsg("'reboots' has wrong schema!");
                                return false;
                            }
                        }
                        else
                        {
                            traceMsg("'reboots' has wrong schema!");
                            return false;
                        }
                    }
                    else
                    {
                        traceMsg("'reboots' has wrong schema!");
                        return false;
                    }
                }
                else
                {
                    traceMsg("'reboots' has wrong schema!");
                    return false;
                }
            }
            catch(Exception e)
            {
                handleException(e);
                traceMsg("\nError Verifying 'reboots' table!\n");
                return false;
            }
        }

        return true;
    }

    /* if there's no faults in the database, this method will return
       the result of currentTimeMillis() */
    public long timeSinceLastFault_ms() throws DBUtilException {
        long lastFault = 0;
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(occurred) FROM faults");
            ResultSetMetaData rsmd = rs.getMetaData();
            if(rsmd.getColumnCount() != 1)
            {
                throw new DBUtilException("ResultSet had invalid number of columns!");
            }

            if(rs.next())
            {
                if(rs.next())
                {
                    throw new DBUtilException("ResultSet had too many rows!");
                }
                lastFault = rs.getLong(1);
            }
            
            // no rows = no faults, let lastFault remain 0
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DBUtilException ("timeSinceLastFault_ms() failed!", e);
        }
    
        return (System.currentTimeMillis() - lastFault);
    }

    /* returns the UID(s) of any component(s) that match the name,
       returns ArrayList of String objects */
    /* TODO: implement these funcs later */
    public List getUID(String name) throws DBUtilException
    {
        ArrayList uids = new ArrayList();
        
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT UID FROM components WHERE Name = '" + name + "'");
            while(rs.next())
            {
                uids.add(rs.getString(1));
            }
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getUID() failed!", e);
        }        
            
        return uids;
    }

    /* returns the parent's UID, or null if no parent */
    public String getParent(String UID) throws DBUtilException
    {
        String parentUID = null;
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Parent FROM components WHERE UID='" + UID + "'");
            if(rs.next())
            {
                if(rs.next())
                {
                    throw new DBUtilException("getParent() returned too many rows!");
                }
                parentUID = rs.getString(1);
                if(parentUID == "")
                {
                    // no parent
                    return null;
                }
            }
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getUID() failed!", e);
        }    

        return parentUID;
    }

    public CompInfo getCompInfo(String UID) throws DBUtilException
    {
        CompInfo info = null;
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT UID,Name,Type,Parent,Methods FROM components WHERE UID='" + UID + "'");
            if(rs.next())
            {
                if(rs.next())
                {
                    throw new DBUtilException("getCompInfo() returned too many rows!");
                }
                info = new CompInfo();
                info.UID = rs.getString(1);
                info.name = rs.getString(2);
                info.type = rs.getString(3);
                info.parent = rs.getString(4);
                info.methods = rs.getString(5);
            }
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getUID() failed!", e);
        }    
        
        return info;
    }    

    public Map getParentMap() throws DBUtilException
    {
        // get the mapping for UID->Parent for any component that has a parent
        HashMap map = new HashMap();

        try
        {            
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT UID,Parent FROM components WHERE Parent != ''");
            while(rs.next())
            {
                map.put(rs.getString(1), rs.getString(2));
            }
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getParentMap() failed!", e);
        }
        
        return map;
    }

    public long getNumFaultsSince(long time) throws DBUtilException
    {
        // get # of faults since a certain time (measured in milliseconds since
        // UTC 1/1/1970)
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM faults WHERE Occurred > " + time);
	    rs.next();
	    return rs.getLong(1);
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getNumFaultsSince() failed!", e);
        }    
    }    


    // generates a new rebootID
    private long getNewRebootID() throws DBUtilException
    {
        long newID = 1;
        
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(RebootID) FROM reboots");
            if(rs.next())
            {
                if(rs.next())
                {
                    throw new DBUtilException("getNewRebootID: ResultSet had too many rows!");
                }
                newID = Long.parseLong(rs.getString(1)) + 1;
                if(newID < 1)
                {
                    // should not happen
                    newID = 1;
                }
            }
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getNewRebootID() failed!", e);
        }

        traceMsg("getNewRebootID() invoked, newID = " + newID + "\n");

        return newID;
    }

    // returns the rebootID of the new reboot action
    public long newReboot(long timestamp, List components, List faults) throws DBUtilException
    {
        traceMsg("newReboot() invoked, timestamp = " + timestamp + "\n   components = "
                 + components.toString() + "\nfaults = " + faults.toString() + "\n");
        
        // getNewRebootID() throws exception if it fails
        long newID = getNewRebootID();
        
        try
        {
            // insert new entrys to reboots table
            Iterator cIter = components.iterator();
            while(cIter.hasNext())
            {       
                String compUID = (String) cIter.next();
                Statement stmt = db.createStatement();
                stmt.executeUpdate("INSERT INTO reboots (RebootID, CompUID, RebootTime) "
                                   + "VALUES(" + newID + ", " + compUID + ", " + faults + ")");
                stmt.close();
            }
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException("newReboot() failed to insert new recors into reboots table!", e);
        }
        
        // update the rebootID field of the faults table
        Iterator fIter = faults.iterator();
        while(fIter.hasNext())
        {
            Long fault_occur = (Long)fIter.next();
            setRebootID(fault_occur.longValue(), newID);
        }

        return newID;
    }

    public void setRebootID(long occurred, long rebootID) throws DBUtilException
    {
        // set the fault that occurred at time 'occurred' to the rebootID
        // valid rebootIDs are integers 1 and up, 0 indicates no reboot for that
        // fault

        traceMsg("setRebootID() invoked, occurred = " + occurred + "  rebootID = "
                 + rebootID + "\n");

        try
        {
            Statement stmt = db.createStatement();
            stmt.executeUpdate("UPDATE faults SET RebootID=" + rebootID + 
                               "WHERE occurred=" + occurred);
            stmt.close();
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException ("setRebootID() failed, RebootID="
                                       + rebootID + ", occurred=" + occurred, e);
        }
    }

    // returns array of fault info objects
    private ArrayList readFaults(ResultSet rs) throws Exception
    {
        ArrayList faults = new ArrayList();

        ResultSetMetaData rsmd = rs.getMetaData();
        if(rsmd.getColumnCount() != 9)
        {
            throw new DBUtilException("ResultSet had invalid number of columns!");
        }

        while(rs.next())
        {
            FaultInfo info = new FaultInfo();
            info.component = rs.getString(1);
            info.method = rs.getString(2);
            try
            {
                info.occurred = Long.parseLong(rs.getString(3));
            }
            catch(NumberFormatException e)
            {
                System.err.println("Could not parse 'occurred' field, ignoring fault: " 
                                   + rs.getString(3));
            }
            info.description = rs.getString(5);
            info.source = rs.getString(6);
            info.notes = rs.getString(7);
            try
            {
                info.rebootID = Long.parseLong(rs.getString(8));
            }
            catch(NumberFormatException e)
            {
                //if blank, just store 0
                info.rebootID = 0;
            }
            info.monitorID = rs.getString(9);
            faults.add(info); // add to tail of list
        }

        return faults;
    }

    public ArrayList getFaultsSince(long time) throws DBUtilException
    {
        // get all faults since a certain time (measured in milliseconds since
        // UTC 1/1/1970)
        ArrayList faults = null;
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM faults WHERE Occurred>" 
                                             + time + " ORDER BY Occurred ASC");
            faults = readFaults(rs);
        }
        catch (DBUtilException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getFaultsSince() failed!", e);
        }    

        return faults;
    }    

    public ArrayList getPrevCompFaults(String compUID, long beforetime) throws DBUtilException
    {
        ArrayList list = null;
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM faults WHERE Occurred<"
                                             + beforetime + " AND Component='" +
                                             compUID + "' ORDER BY Occurred DESC");
            list = readFaults(rs);
        }
        catch (DBUtilException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DBUtilException ("getPrevCompFaults() failed!", e);
        }

        return list;
    }

    // start_time and end_time are both exclusive (i.e. start_time < fault time
    // < end_time)
    public boolean compRbBetween(String UID, long start_time, long end_time) throws DBUtilException
    {
        // returns true if the specified component has been rebooted between the
        // specified times
        
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM reboots WHERE CompUID='" + UID
                                             + "' AND RebootTime > " + start_time 
                                             + " AND RebootTime < " + end_time);
            if(rs.next()) // has entry, meaning there has been a reboot
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DBUtilException ("compRbSince() failed!", e);
        }
    }

    public boolean compRbSince(String UID, long time) throws DBUtilException
    {
        // returns true if the specified component has been rebooted since the
        // specified time
        
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM reboots WHERE CompUID='" + UID
                                             + "' AND RebootTime > " + time);
            if(rs.next()) // has entry, meaning there has been a reboot
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            handleException(e);
            throw new DBUtilException ("compRbSince() failed!", e);
        }
    }
    
    public long getRbTime(long rebootID) throws DBUtilException
    {
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT RebootTime FROM reboots WHERE RebootID = "
                                             + rebootID);
            if(rs.next())
            {
                return Long.parseLong(rs.getString(1));
            }
            else
            {
                return -1;
            }
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException("getRbTime() failed!", e);
        }
    }

    public ArrayList getRbComps (long rebootID) throws DBUtilException
    {
        // get the list of component UID that were rebooted for the specified
        // rebootID
        ArrayList list = new ArrayList();

        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CompUID FROM reboots WHERE RebootID = "
                                             + rebootID);
            while(rs.next())
            {
                list.add(rs.getString(1));
            }
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException("getRbComps() failed!", e);
        }

        return list;
    } 

    public class DepMapInfo extends Object
    {
        public DepMapInfo (int fOccur, int rOccur)
        {
            faultOccurs = fOccur;
            rebootOccurs = rOccur;
        }

        // # of times component failed within depPeriod_ms of source failure
        int faultOccurs;
        // # of times component pre-emptively rebooted because of source failure
        int rebootOccurs;
    }

    public List getDepList(String compUID) throws DBUtilException
    {
        List dList = new ArrayList();
        Map depMap = getCompDepMap(compUID);

        Integer compOccur = (Integer)depMap.get(compUID);
        if(compOccur == null)
        {
            throw new DBUtilException("getDepList() failed, getCompDepMap() did not retrieve # occurrances"
                                      + " of CompUID faults");
        }

        Set depMapSet = depMap.keySet();
        depMapSet.remove(compUID);
        Iterator dpIter = depMapSet.iterator();
        
        while(dpIter.hasNext())
        {
            String eff_comp = (String)dpIter.next();
            DepMapInfo info = (DepMapInfo)depMap.get(eff_comp);
            if(info == null)
            {
                throw new DBUtilException("getDepList() failed, depMap corrupt!");
            }
            if(((info.faultOccurs)/(compOccur.intValue() - info.rebootOccurs) >= minConf)
                && (info.faultOccurs >= minSupport))
            {
                dList.add(eff_comp);
            }
        }

        traceMsg("getDepList() invoked for '" + compUID + "'\n   Result = "
            + dList.toString() + "\n");

        return dList;
    }

    // find dependencies/correlations between components
    public Map getCompDepMap(String compUID) throws DBUtilException
    {
        // returns components that often fail when 'compUID' fails
        HashMap depMap = new HashMap();

        try
        {
            HashMap curDepMap = null;
            HashMap curRbMap = null;
            Statement stmt = db.createStatement();

            // first find out how many times compUID has failed
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM faults WHERE Component = '" 
                                             + compUID + "'");
            if(!rs.next())
            {
                throw new DBUtilException("getCompDepMap failed(), could not get # occurs of CompUID faults");
            }
            depMap.put(compUID, new Integer(rs.getInt(1)));

            // now find other faults that happen within a given time after this fault    
            rs = stmt.executeQuery("SELECT f1.Component, f1.Occurred, f2.Component, f2.Occurred, f1.rebootID " +
                                   "FROM faults AS f1, faults AS f2 " +
                                   "WHERE f1.Component = '" + compUID + "' AND " +
                                   "f1.Occurred < f2.Occurred AND f2.Occurred - f1.Occurred < "
                                   + depPeriod_ms + " AND f2.Source NOT LIKE '%afpi%'"
                                   + " AND f1.MonitorID NOT LIKE '%E2EMon%'" 
                                   + " AND f2.MonitorID NOT LIKE '%E2EMon%'"
                                   + " ORDER BY f1.Occurred ASC, f2.Occurred ASC");
            long curOccur = -1;
            long occurred;
            
            while(rs.next())
            {
                occurred = Long.parseLong(rs.getString(2));
                String comp = rs.getString(1);
                String depComp = rs.getString(3);
                long rebootID = Long.parseLong(rs.getString(5));
                
                boolean rsNext = true;
                while(comp.compareTo(depComp) == 0)
                {
                    // ignore the rest of the faults that correspond to this
                    // occurrance
                    while((rsNext = rs.next()) == true)
                    {
                        occurred = Long.parseLong(rs.getString(2));
                        if(occurred != curOccur) // at a new occurrance
                        {
                            comp = rs.getString(1);
                            depComp = rs.getString(3);
                            break;
                        }
                    }
                    if(!rsNext)
                    {
                        // finished
                        break;
                    }
                }
                if(!rsNext)
                {
                    break;
                }

                if(occurred != curOccur)
                {
                    // we are examining a new base fault occurrance
                    curOccur = occurred;
                    if(curRbMap != null && !curRbMap.isEmpty())
                    {
                        Set crbSet = curRbMap.keySet();
                        Iterator crbIter = crbSet.iterator();
                        while(crbIter.hasNext())
                        {
                            String crbComp = (String)crbIter.next();
                            DepMapInfo info = (DepMapInfo)depMap.get(crbComp);
                            if(info == null)
                            {
                                depMap.put(crbComp, new DepMapInfo(0,1));
                            }
                            else
                            {
                                info.rebootOccurs++;
                            }
                        }
                    }
                       
                    if(curDepMap != null && !curDepMap.isEmpty())
                    {
                        Set cdpSet = curDepMap.keySet();
                        Iterator cdpIter = cdpSet.iterator();
                        while(cdpIter.hasNext())
                        {
                            String cdpComp = (String)cdpIter.next();
                            DepMapInfo info = (DepMapInfo)depMap.get(cdpComp);
                            if(info == null)
                            {
                                depMap.put(cdpComp, new DepMapInfo(1,0));
                            }
                            else
                            {
                                info.faultOccurs++;
                            }
                        }
                    }

                    // reset the reboot map and load it with all comps that were
                    // rebooted as a result of this fault (except for the source
                    // failure component itself)
                    curRbMap = new HashMap();
                    Iterator rbCompIter = getRbComps(rebootID).iterator();
                    while(rbCompIter.hasNext())
                    {
                        curRbMap.put((String)rbCompIter.next(), null);
                    }
                    curRbMap.remove(comp);
                    
                    // reset cur dep map
                    curDepMap = new HashMap();
                }

                // remove effect failure comp from reboot map, add to dep map
                String e_faultComp = rs.getString(3);
                curRbMap.remove(e_faultComp);
                curDepMap.put(e_faultComp, null);
            }
        }
        catch(DBUtilException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            handleException(e);
            throw new DBUtilException("getCompDepMap() failed!", e);
        }

        return depMap;
    }

    // debug method
    public void addFault(String name, long occurred, String source, String monitorID) 
        throws DBUtilException
    {
        try
        {
            Statement stmt = db.createStatement();
            stmt.executeUpdate("INSERT INTO faults (Component,Occurred,Source,MonitorID) VALUES('" 
                             + name + "', '" + occurred + "', '" + source + "', '" + monitorID + "')");
        }
        catch( Exception e)
        {
            handleException(e);
            throw new DBUtilException("addFault() failed!", e);
        }
    }

    // just a test method (not used normally)
    public void showAllFaults() {
        try
        {            
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM faults");

            System.out.println("Here are the current faults in the database: ");

            System.out.println("  Component  |  Method  |  Occurred  |  Source  |  Description   ");
            
            while(rs.next())
            {
                System.out.println(rs.getString(1) + "   " + rs.getString(2)
                    + "   " + rs.getString(3) + "   " + rs.getString(6) + 
                    "   " + rs.getString(5));
            }
        
            System.out.println("----- End of faults -----");
        }
        catch( Exception e)
        {
            handleException(e);
        }
    }

    /** 
     * Handles an exception by printing out information about it:
     * detailed SQL info if it's a <code>SQLException</code>, or just
     * a stack trace if it's any other kind of exception.
     *
     * @param  ex          the <code>Exception</code> to handle
     *
     * */
    private static void handleException( Exception ex )
    {
	ex.printStackTrace();

	if ( ex instanceof SQLException )
	{
	    SQLException sqlEx = (SQLException) ex;
	    System.out.println("==> SQL Exception: ");
	    while (sqlEx != null) 
	    {
		System.out.println("Message:   " + sqlEx.getMessage ());
		System.out.println("SQLState:  " + sqlEx.getSQLState ());
		System.out.println("ErrorCode: " + sqlEx.getErrorCode ());
		sqlEx = sqlEx.getNextException();
		System.out.println("");
	    }
	}
    }
}
