package roc.rr.ssm;

public interface StatisticsInterface {
    /* this interface defines the keys to the different hashmaps
       required for Observation.java in the pinpoint package */

    /* the following are keys for the OriginInfo Hash */
    static String BrickID = "BrickID";
    static String BrickHost = "BrickHost";
    static String BrickPort = "BrickPort";
    // the time that this brick was started
    static String StartTime = "StartTime";


    // the length of interval during which the statistics are collected from
    // this brick, in milliseconds 
    static String TimeInterval = "TimeInterval"; 

    /* the following are keys for the rawDetails Hash */
    /* these are delta statistics, for the last time interval */

    static String NumDropped = "LastIntervalNumDropped";
    static String NumReadProcessed = "LastIntervalNumReadProcessed";
    static String NumWriteProcessed = "LastIntervalNumWriteProcessed";
    // the number of queued, pending requests in the inbox
    static String InboxSize = "LastIntervalInboxSize";


    /* these are aggregate statistics since startup */
    static String TimeSinceStartup = "TimeSinceStartup";
    static String NumElements = "TotalNumElements";
    static String MemoryUsed = "TotalMemoryUsed";



}
