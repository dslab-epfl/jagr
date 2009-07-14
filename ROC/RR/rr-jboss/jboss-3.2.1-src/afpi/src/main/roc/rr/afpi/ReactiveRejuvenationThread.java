/* $Id: ReactiveRejuvenationThread.java,v 1.1 2004/05/08 00:10:08 skawamo Exp $ */

/*
 *  ReactiveRejuvenationThread
 *     microreboot a series of EJBs reacting the usage of memory
 *
 */

package roc.rr.afpi;

import java.util.*;
import java.sql.*;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import org.jboss.deployment.*;
import org.jboss.logging.Logger;
import roc.rr.afpi.util.*;

public class ReactiveRejuvenationThread extends Thread {
    private MBeanServer server;
    private Logger log;
    private int startThreshold; // reactive rejuvenation start threshold in MB
    private int stopThreshold;  // reactive rejuvenation stop threshold in MB
    private int watchInterval;       // watch interval in seconds
    private int microRebootInterval; // micro reboot interval in seconds
    private TreeMap rebootOrder = null;
    private String rebootMode;   // either the value of RejuvenationService.REBOOTMODE_FULL or RejuvenationService.REBOOTMODE_MICRO

    private String[] jarList
	= { "SB_AboutMe.jar",
	    "SB_Auth.jar",
	    "SB_BrowseCategories.jar",
	    "SB_BrowseRegions.jar",
	    "SB_BuyNow.jar",
	    "SB_PutBid.jar",
	    "SB_PutComment.jar",
	    "SB_RegisterItem.jar",
	    "SB_RegisterUser.jar",
	    "SB_SearchItemsByCategory.jar",
	    "SB_SearchItemsByRegion.jar",
	    "SB_StoreBid.jar",
	    "SB_StoreBuyNow.jar",
	    "SB_StoreComment.jar",
	    "SB_ViewBidHistory.jar",
	    "SB_ViewItem.jar",
	    "SB_ViewUserInfo.jar",
	    "BuyNow.jar",
	    "Comment.jar",
	    "IDManager.jar",
	    "OldItem.jar",
	    "User-Item.jar" };
    
    private String ear = "rubis.ear";

    //
    // constructor
    // 
    public ReactiveRejuvenationThread(MBeanServer server,
				      Logger log,
				      int startThreshold,
				      int stopThreshold,
				      int watchInterval,
				      int microRebootInterval,
				      String rebootMode)
    {
	this.server = server;
	this.log = log;
	this.startThreshold = startThreshold;
	this.stopThreshold = stopThreshold;
	this.watchInterval = watchInterval;
	this.microRebootInterval = microRebootInterval;
	this.rebootMode = rebootMode;

	// check rebootMode and initialize rebootOrder
	rebootOrder = new TreeMap();
	if (rebootMode.equals(RejuvenationService.REBOOTMODE_FULL)){
	    // put ear in rebootOrder
	    rebootOrder.put(new Integer(0),ear);
	}  else {  
	    // put all jarList entries in rebootOrder as the order of jarList
	    for(int i=0;i<jarList.length;i++){
		rebootOrder.put(new Integer(i+1000),jarList[i]); 
	    }
	}
    }


    //
    // microreboot a series of EJB reacting available memory
    //
    public void run() {

	String message = null;
	int availMB = 0;

	while(true) {
	    try {
		this.sleep(watchInterval*1000);

		// Check the available memory and if 
		// it is lower than startThreshold then 
		// start microrebooting a series of EJB
		availMB = getAvailableMBytes();
		log.info("available memory : "+availMB);  // debug
		if ( availMB < startThreshold ) { 
		    TreeMap newRebootOrder = new TreeMap(); // for reorder 
		    
		    int ineffectivenessOffset = 0;
		    log.info("[Reactive Rejuvenation] start microrebooting ejbs");
		    while (!rebootOrder.isEmpty()) {

			// extract first entry from rebootOrder
			Object key = rebootOrder.firstKey();
			String target = (String)rebootOrder.get(key);
			rebootOrder.remove(key);

			// reboot it
			reboot(target);
			
			// check available memory again
			int availMBafteruRB = getAvailableMBytes();
			log.info("available memory : "+availMBafteruRB); // debug

			// calculate ineffectiveness of microreboot
			//   basically ineffectiveness = 1000 
			//                 - amount of reclaimed memory
			// Because ineffectiveness is used as the key 
			// of TreeMap, we must ensure not to duplicate 
			// the same value
			int ineffectiveness;
			if ( availMBafteruRB - availMB > 50 ) {
			    ineffectiveness = 1000 
				- (availMBafteruRB - availMB);

			    // ensure not to produce same key
			    while ( newRebootOrder.containsKey(new Integer(ineffectiveness)) ) {
				ineffectiveness++;
			    } 
			}  else {
			    ineffectiveness = 1000 + ineffectivenessOffset;
			    ineffectivenessOffset++;
			}

			// set entry to newRebootOrder
			newRebootOrder.put(new Integer(ineffectiveness),
					   target);
			
			// If available memory is larger than stopThreshold
			// then stop microrebooting a series of EJB.
			if ( availMBafteruRB > stopThreshold ) {
			    log.info("[Reactive Rejuvenation] stop microrebooting ejbs"); 

			    // copy rest of the entries of rebootOrder to newRebootOrder
			    ineffectiveness = 1000 + ineffectivenessOffset;
			    while (! rebootOrder.isEmpty() ){
				// extract entry from rebootOrder
				key = rebootOrder.firstKey();
				target = (String)rebootOrder.get(key);
				rebootOrder.remove(key);

				// set entry to newRebootOrder
				newRebootOrder.put(new Integer(ineffectiveness),
						   target);
				ineffectiveness++;
			    }
			    break;
			}
			availMB = availMBafteruRB;
			
			this.sleep(microRebootInterval*1000);
		    }

		    // replace rebootOrder with newRebootOrder
		    rebootOrder = newRebootOrder;
		    log.info("RebootOrder: "+rebootOrder); // debug

		}
	    } catch (Exception e) {
		e.printStackTrace();
		log.info("Error occured in ReactiveRejuvenationThred: "+e);
	    }
	}
    }


    // ---- auxiliary methods ---- //

    //
    // micro reboot target 
    //
    private void reboot(String target){
	String UID = null;

	try {
	    Information info = new Information(server);
	    if ( rebootMode.equals(RejuvenationService.REBOOTMODE_FULL) ) {
		UID = Information.getCompleteFileName(target);
	    } else {
		UID = Information.getCompleteFileName(ear,target);
	    }
	    ObjectName deployerSvc 
		= new ObjectName("RR:service=RecoveryControl");
	    server.invoke(deployerSvc, "microrebootAndInjectFault",
			  new Object[] { UID }, 
			  new String[] { "java.lang.String" });
	    log.info(target+" was rebooted by rejuvenation manager");
	} catch (Exception e) {
	    log.info("Can't microreboot "+UID+" : "+e);
	}

	System.gc();
    }


    //
    // get available memory in Mega Bytes
    //
    private int getAvailableMBytes(){
	int availMBytes=0;
	try {
	    ObjectName serverInfo 
		= new ObjectName("jboss.system:type=ServerInfo");
	    long availBytes 
		= ((Long) server.getAttribute(serverInfo, "FreeMemory")).longValue();
	    availMBytes = (int)availBytes/(1024*1024);
	} catch (Exception e) {
	    log.info("Can't get available memory : "+e);
	}
	
	return availMBytes;
    }
}
