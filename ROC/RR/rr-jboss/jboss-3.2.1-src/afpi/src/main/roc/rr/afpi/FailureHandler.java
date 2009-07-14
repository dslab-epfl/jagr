/* 
 * $Id: FailureHandler.java,v 1.4 2004/08/18 20:12:39 candea Exp $ 
 *
 */
package roc.rr.afpi;

import java.util.*;
import java.io.*;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import org.jboss.deployment.*;
import org.jboss.logging.Logger;
import roc.rr.afpi.util.*;

public class FailureHandler 
{
    private static Logger log = Logger.getLogger( FailureHandler.class );

    private   RecoveryService recoSvc = null; // recovery svc used for rebooting
    
    protected HashMap _servletToEJBsMap  = new HashMap(); // servlet --> list of EJBs
    protected HashMap _ejbToThresholdMap = new HashMap(); // EJB --> threshold value
    protected HashMap _ejbToCounterMap   = new HashMap(); // EJB --> failures
    protected HashMap _ejbToJARMap = new HashMap();       // EJB --> JAR name

    PrintWriter _writer = new PrintWriter( System.out ); // to be removed

    // Hard-coded "servlet --> EJBs" relation for microreboots
    protected static String SERVLET_TO_EJB_RELATION_MICRO = 
	"[AboutMe]\n" +
	"SB_AboutMe\n" +
	"User\n" +
	"Item\n" +
	"BuyNow\n" +
	"Comment\n" +
	"Bid\n" +             //added this from experiments although it is not in Shinichi's graph
	"[AboutMe]\n" +
	
	"[BrowseCategories]\n" +
	"SB_BrowseCategories\n" +
	"Region\n" +
	"Category\n" +
	"[BrowseCategories]\n" +
	
	"[BrowseRegions]\n" +
	"SB_BrowseRegions\n" +
	"Region\n" +
	"[BrowseRegions]\n" +
	
	"[BuyNow]\n" +
	"SB_BuyNow\n" +
	"Item\n" +
	"[BuyNow]\n" +
	
	"[Login]\n" +
	"SB_Auth\n" +
	"User\n" +
	"[Login]\n" +
	
	"[PutBid]\n" +
	"SB_PutBid\n" +
	"SB_ViewItem\n" +
	"Item\n" +
	"Bid\n" +
	"[PutBid]\n" +
	
	"[PutComment]\n" +
	"SB_PutComment\n" +
	"User\n" +
	"[PutComment]\n" +
	
	"[RegisterItem]\n" +
	"SB_RegisterItem\n" +
	"Item\n" +
	"IDManager\n" +
	"[RegisterItem]\n" +
	
	"[RegisterUser]\n" +
	"SB_RegisterUser\n" +
	"Region\n" +
	"User\n" +
	"IDManager\n" +
	"[RegisterUser]\n" +
	
	"[SearchItemsByCategory]\n" +
	"SB_SearchItemsByCategory\n" +
	"Category\n" +
	"Item\n" +
	"[SearchItemsByCategory]\n" +
	
	"[SearchItemsByRegion]\n" +
	"SB_SearchItemsByRegion\n" +
	"Category\n" +
	"Item\n" +
	"[SearchItemsByRegion]\n" +
	
	"[StoreBid]\n" +
	"SB_StoreBid\n" +
	"Bid\n" +
	"Item\n" + //It appeared in StoreBid error, even though this is not in Sinichi's table.
	"IDManager\n" +
	"[StoreBid]\n" +
	
	"[StoreBuyNow]\n" +
	"SB_StoreBuyNow\n" +
	"Item\n" +
	"BuyNow\n" +
	"IDManager\n" +
	"[StoreBuyNow]\n" +
	
	"[StoreComment]\n" +
	"SB_StoreComment\n" +
	"Comment\n" +
	"User\n" +
	"IDManager\n" +
	"[StoreComment]\n" +
	
	"[ViewBidHistory]\n" +
	"SB_ViewBidHistory\n" +
	"Item\n" +
	"Bid\n" +
	"[ViewBidHistory]\n" +
	
	"[ViewItem]\n" +
	"SB_ViewItem\n" +
	"Item\n" +
	"Bid\n" +
	"[ViewItem]\n" +

	"[ViewUserInfo]\n" +
	"SB_ViewUserInfo\n" +
	"User\n" +
	"Comment\n" +
	"[ViewUserInfo]";

    // Hard-coded "servlet --> EJBs" relation for full reboots
    protected static String SERVLET_TO_EJB_RELATION_FULL = 
	"[AboutMe]\nRUBIS\n[AboutMe]\n" +
	"[BrowseCategories]\nRUBIS\n[BrowseCategories]\n" +
	"[BrowseRegions]\nRUBIS\n[BrowseRegions]\n" +
	"[BuyNow]\nRUBIS\n[BuyNow]\n" +
	"[Login]\nRUBIS\n[Login]\n" +
	"[PutBid]\nRUBIS\n[PutBid]\n" +
	"[PutComment]\nRUBIS\n[PutComment]\n" +
	"[RegisterItem]\nRUBIS\n[RegisterItem]\n" +
	"[RegisterUser]\nRUBIS\n[RegisterUser]\n" +
	"[SearchItemsByCategory]\nRUBIS\n[SearchItemsByCategory]\n" +
	"[SearchItemsByRegion]\nRUBIS\n[SearchItemsByRegion]\n" +
	"[StoreBid]\nRUBIS\n[StoreBid]\n" +
	"[StoreBuyNow]\nRUBIS\n[StoreBuyNow]\n" +
	"[StoreComment]\nRUBIS\n[StoreComment]\n" +
	"[ViewBidHistory]\nRUBIS\n[ViewBidHistory]\n" +
	"[ViewItem]\nRUBIS\n[ViewItem]\n" +
	"[ViewUserInfo]\nRUBIS\n[ViewUserInfo]";
    
    // Hard-coded "EJB --> JARs" relation for microreboots
    protected static String EJB_TO_JAR_RELATION_MICRO = 
	"[BuyNow]\n" +
	"BuyNow.jar\n" +
	"[BuyNow]\n" +
	
	"[Comment]\n" +
	"Comment.jar\n" +
	"[Comment]\n" +
	
	"[IDManager]\n" +
	"IDManager.jar\n" +
	"[IDManager]\n" +
	
	"[OldItem]\n" +
	"OldItem.jar\n" +
	"[OldItem]\n" +
	
	"[SB_AboutMe]\n" +
	"SB_AboutMe.jar\n" +
	"[SB_AboutMe]\n" +
	
	"[SB_Auth]\n" +
	"SB_Auth.jar\n" +
	"[SB_Auth]\n" +
	
	"[SB_BrowseCategories]\n" +
	"SB_BrowseCategories.jar\n" +
	"[SB_BrowseCategories]\n" +
	
	"[SB_BrowseRegions]\n" +
	"SB_BrowseRegions.jar\n" +
	"[SB_BrowseRegions]\n" +
	
	"[SB_BuyNow]\n" +
	"SB_BuyNow.jar\n" +
	"[SB_BuyNow]\n" +
	
	"[SB_PutBid]\n" +
	"SB_PutBid.jar\n" +
	"[SB_PutBid]\n" +
	
	"[SB_PutComment]\n" +
	"SB_PutComment.jar\n" +
	"[SB_PutComment]\n" +
	
	"[SB_RegisterItem]\n" +
	"SB_RegisterItem.jar\n" +
	"[SB_RegisterItem]\n" +
 
	"[SB_RegisterUser]\n" +
	"SB_RegisterUser.jar\n" +
	"[SB_RegisterUser]\n" +
	
	"[SB_SearchItemsByCategory]\n" +
	"SB_SearchItemsByCategory.jar\n" +
	"[SB_SearchItemsByCategory]\n" +
 
	"[SB_SearchItemsByRegion]\n" +
	"SB_SearchItemsByRegion.jar\n" +
	"[SB_SearchItemsByRegion]\n" +
 
	"[SB_StoreBid]\n" +
	"SB_StoreBid.jar\n" +
	"[SB_StoreBid]\n" +
	
	"[SB_StoreBuyNow]\n" +
	"SB_StoreBuyNow.jar\n" +
	"[SB_StoreBuyNow]\n" +
	
	"[SB_StoreComment]\n" +
	"SB_StoreComment.jar\n" +
	"[SB_StoreComment]\n" +
	
	"[SB_ViewBidHistory]\n" +
	"SB_ViewBidHistory.jar\n" +
	"[SB_ViewBidHistory]\n" +
	
	"[SB_ViewItem]\n" +
	"SB_ViewItem.jar\n" +
	"[SB_ViewItem]\n" +
	
	"[SB_ViewUserInfo]\n" +
	"SB_ViewUserInfo.jar\n" +
	"[SB_ViewUserInfo]\n" +
	
	"[User]\n" +
	"User-Item.jar\n" +
	"[User]\n" +
	
	"[Item]\n" +
	"User-Item.jar\n" +
	"[Item]\n" +
	
	"[Region]\n" +
	"User-Item.jar\n" +
	"[Region]\n" +
	
	"[Category]\n" +
	"User-Item.jar\n" +
	"[Category]\n" +
	
	"[Bid]\n" +
	"User-Item.jar\n" +
	"[Bid]";

    // Hard-coded "EJB --> JARs" relation for full reboots
    protected static String EJB_TO_JAR_RELATION_FULL = 
	"[RUBIS]\nrubis.ear\n[RUBIS]\n";

    //constructor
    public FailureHandler( boolean uRBmode, int threshold, RecoveryService recoSvc )
	throws Exception 
    {
	this.recoSvc = recoSvc;

	if ( uRBmode )
	{
	    initServletToEJBsMap( SERVLET_TO_EJB_RELATION_MICRO );
	    initEJBToJARMap( EJB_TO_JAR_RELATION_MICRO );
	}
	else
	{
	    initServletToEJBsMap( SERVLET_TO_EJB_RELATION_FULL );
	    initEJBToJARMap( EJB_TO_JAR_RELATION_FULL );
	}
	
	initEJBToThresholdMap( threshold );
	initEJBToCounterMap();
    }
	
    /*
     *initialize map of servlet --> ejbs-list.
     */
    private void initServletToEJBsMap( String relation ) throws IOException{
	
	//init
	_servletToEJBsMap = new HashMap();
	boolean readList = false;
	String servletName = null;
	HashSet ejbsSet = null;
	
	//read file
	BufferedReader in = new BufferedReader(new StringReader( relation ));
	String str_in;
	while((str_in = in.readLine()) != null){
	    str_in = str_in.trim();
	    if(str_in.length() == 0){
		continue;
	    }
	    
	    // if total line started from [, ended to ], then its either 
	    // start or end of block. String indicates servletName.
	    // else String indicates jar file which has relevance with the servlet.
	    if(str_in.charAt(0) == '[' && str_in.charAt(str_in.length()-1) == ']'){
		
		//if readList flag is false, then it's start of block.
		//else it's end of block.
		if(readList == false){
		    readList = true;
		    servletName = str_in.substring(1,str_in.length()-1);
		    ejbsSet = new HashSet();
		} else {
		    _servletToEJBsMap.put(servletName, ejbsSet);
		    readList = false;
		    servletName = null;
		    ejbsSet = null;
		}
	    } else {
		
		//if readList is true, then this is a jar file name to be read.
		if(readList == true){
		    ejbsSet.add(str_in);
		}
		continue;
	    }
	}
    }


    /*
     * initialize map of ejb --> jar map.
     */
    private void initEJBToJARMap( String relation ) throws IOException{

	//init
	_ejbToJARMap = new HashMap();
	boolean readList = false;
	String ejbName = null;
	String jarName = null;
	
	//read file
	BufferedReader in = new BufferedReader(new StringReader( relation ));
	String str_in;
	while((str_in = in.readLine()) != null){
	    str_in = str_in.trim();
	    if(str_in.length() == 0){
		continue;
	    }
	    
	    // if total line started from [, ended to ], then its either 
	    // start or end of block. String indicates ejbName.
	    // else String indicates jar file which has relevance with the servlet.
	    if(str_in.charAt(0) == '[' && str_in.charAt(str_in.length()-1) == ']'){
		
		//if readList flag is false, then it's start of block.
		//else it's end of block.
		if(readList == false){
		    readList = true;
		    ejbName = str_in.substring(1,str_in.length()-1);
		} else {
		    _ejbToJARMap.put(ejbName, jarName);
		    readList = false;
		    ejbName = null;
		    jarName = null;
		}
	    } else {
		
		//if readList is true, then this is a jar file name to be read.
		if(readList == true){
		    jarName = str_in;
		}
		continue;
	    }
	}
    }

    /* 
     * initilize map of ejb --> threshold
     * should get called after initEJBToJARMap() has been called.
     */
    private void initEJBToThresholdMap( int threshold ) throws IOException{
	
	Set ejbs = _ejbToJARMap.keySet();
	Iterator it = ejbs.iterator();
	//You can tweak these numbers. according to data. 
	//inside () attached to EJBs are the numbers of failures appeared in 1.5 minutes fault injections.
	while(it.hasNext()){
	    String ejbName = (String)it.next();
	    if(ejbName.equals("Comment")){
		_ejbToThresholdMap.put(ejbName, new Integer(2));
		//ViewUserInfo(14,16)
		//AboutMe(20,16)
		//StoreComment(0,0)
	    }
	    else if(ejbName.equals("Item")){
		_ejbToThresholdMap.put(ejbName, new Integer(4)); //requires aggressive recovery due to frequent access.
		//SearchItemsByCategory(70,88) --> not in Shinichi's table.
		//SearchItemsByRegion(33,33) --> not in Shinichi's table.
		//AboutMe(15,17)
		//ViewItem(11,8)
		//PutBid(2,4)
		//StoreBid(5,1) --> not in Shinichi's table. secondary.
		//BuyNow(1,1)
		//StoreBuyNow(1,1) --> secondary.
		//ViewBidHistory(0,1) 
		//RegisterItem(0,0)

	    }
	    else if(ejbName.equals("Bid")){
		_ejbToThresholdMap.put(ejbName, new Integer(4));//there comes no StoreBid from experiment.
		//AboutMe(11,11) --> is not in Shinichi's table, but appears in experiments. (This is possible)
		//ViewBidHistory(4,3)
		//ViewItem(0,2)
		//PutBid(0,4)
		//StoreBid(0,0) --> secondary
	    }
	    else if(ejbName.equals("Region")){
		_ejbToThresholdMap.put(ejbName, new Integer(3));
		//BrowseRegions(52,63)
		//RegisterUser(1,4)
		//BrowseCategories(18,20)
	    }
	    else if(ejbName.equals("User")){
		_ejbToThresholdMap.put(ejbName, new Integer(4)); //requires aggressive recovery
		//Login(284,303)
		//RegisterUser(19,8)
		//ViewUserInfo(15,15)
		//AboutMe(3,3)
		//PutComment(0,1)    
		//StoreComment(0,1) --> secondary
	    }
	    else if(ejbName.equals("BuyNow")){ // deal it with microrejuvnation.
		_ejbToThresholdMap.put(ejbName, new Integer(2));
		//AboutMe(1,2)
		//StoreBuyNow(0,0)
	    }
	    else if(ejbName.equals("Category")){
		_ejbToThresholdMap.put(ejbName, new Integer(3));
		//BrowseCategories(189,205)
		//SearchItemsByRegion(4,3)
		//SearchItemsByCategory(11,10)
	    }
	    else if(ejbName.equals("IDManager")){
		_ejbToThresholdMap.put(ejbName, new Integer(5));
		//StoreBid(28,29)
		//RegisterItem(35,42)
		//StoreBuyNow(13,12)
		//RegisterUser(4,5)
		//StoreComment(0,1)
	    }
	    else
		_ejbToThresholdMap.put(ejbName, new Integer(threshold));
	}
    }

    /*
     * initialize _ejbToCounterMap. Should get called after initEJBToFARMap 
     * has been get called.
     */
    protected void initEJBToCounterMap() throws Exception{
	if(_ejbToJARMap.size() == 0)
	    throw new Exception("initEJBToJARMap() not initialized");
	
	Set ejbSet = _ejbToJARMap.keySet();
	Iterator it = ejbSet.iterator();
	while(it.hasNext()){
	    String ejbName = (String)it.next();
            // put 0 for the first value.
	    if ( isEntityBean(ejbName) ) {
                _ejbToCounterMap.put(ejbName, new HashSet());
            } else {
                _ejbToCounterMap.put(ejbName, new Integer(0));
            }
        }
    }


    /**
     * Processes a FailureReport and reboots whatever is needed.
     *
     * @param report failure report to process.
     */
    public void process( FailureReport report )
	throws Exception
    {
	List rebootList = getRebootList( report );
	if ( rebootList == null )
	    return; // nothing to reboot
	
	// Go through the list and perform the reboots
	Iterator it_reboot = rebootList.iterator();
	while( it_reboot.hasNext() )
	{
	    String ejbName = (String)it_reboot.next();
	    String jarName = (String)_ejbToJARMap.get(ejbName);
	    try {
		reboot( jarName ); 
		resetAllFailures();
	    }
	    catch( MicrorebootTooFrequentException urbE ) {
		// reboot did not happen, so no clearing
	    }
	}
    }
    

    /**
     * Return the names of suspected-failed JAR files that need
     * rebooting, based on the information of the incoming 'report'
     * and the history.  If no reboots are needed at this time,
     * returns an empty list.  The _ejbToCounterMap is updated with
     * the failure contents, under the assumption that the requested
     * reboots will be performed successfully.
     *
     * @param report failure report to process
     * @return ArrayList of suspected-failed JAR files
     */
    private ArrayList getRebootList( FailureReport report )
	throws Exception
    {
	Set compSet = null;
	
	String servletName;
	long   repTime     = report.getTimeStamp();   // Extract time of report

	if( report.isServletFailureReport() ) 
	{
	    servletName = report.getServletName(); // Extract servlet name
		
	    // Update failure information
	    compSet = (Set)_servletToEJBsMap.get( servletName );
	    if ( compSet == null )
	    {   // we got a servlet that does not map to any EJBs
		    // FIXME: keep track of web tier failures as well, and uRB it when needed
		    return null;
	    }
	}
	else if( report.isEJBFailureReport() ) 
	{
	    compSet = (Set)report.getEJBNames();
	    servletName = "pinpointPlaceHolderTODO";
	}
	else 
	{
	    throw new Exception( "ACK! unknown Failure report type: " + report.toString() );
	}

	Iterator it = compSet.iterator();
	while ( it.hasNext() )
	{
	    String compName = (String)it.next();

	    if( isValid( compName, repTime ) )
		addFailure( compName, servletName );
	}

	// Construct the list of EJBs to be rebooted
	ArrayList toBeRebooted = new ArrayList();
	it = compSet.iterator();
	while ( it.hasNext() )
	{
	    String compName = (String)it.next();

	    if ( failureCount(compName) >= rebootThreshold(compName) )
	    {
// 		log.info("Scheduling " + compName + " because fcount=" + 
// 			 failureCount(compName) + " and threshold=" + rebootThreshold(compName));
		toBeRebooted.add( compName );
	    }
	}

	return toBeRebooted;
    }

    private boolean isEntityBean( String ejbName )
    {
	if( ejbName.equals("IDManager")  || 
	    ejbName.equals("User")       || 
	    ejbName.equals("Comment")    || 
	    ejbName.equals("BuyNow")     ||
	    ejbName.equals("Bid")        ||
	    ejbName.equals("Item")       || 
	    ejbName.equals("Category")   ||
	    ejbName.equals("Region")       )
	    return true;
	else
	    return false;
    }

    private int failureCount( String compName )
    {
	if ( isEntityBean( compName ) )
	    return ((HashSet)_ejbToCounterMap.get( compName )).size();
	else
	    return ((Integer)_ejbToCounterMap.get( compName )).intValue();
    }

    private void addFailure( String compName, String servletName )
    {
	if ( isEntityBean( compName ) )
	{	 
	    HashSet set = (HashSet) _ejbToCounterMap.get( compName );
	    set.add( servletName );
	    _ejbToCounterMap.put( compName, set );
	}
	else
	{
	    int count = 1 + failureCount( compName );
	    _ejbToCounterMap.put(compName, new Integer(count));
	}
    }

    private int rebootThreshold( String compName )
    {
	return ((Integer)_ejbToThresholdMap.get(compName)).intValue();
    }

    private void resetFailures( String compName )
    {
	if ( isEntityBean( compName ) )
	    _ejbToCounterMap.put(compName, new HashSet());
	else
	    _ejbToCounterMap.put(compName, new Integer(0));
    }

    private void resetAllFailures ()
    {
	Set keySet = _ejbToCounterMap.keySet();
	Iterator it = keySet.iterator();
	while ( it.hasNext() )
	{
	    String ejbName = (String)it.next();

	    if ( isEntityBean( ejbName ) )
		_ejbToCounterMap.put(ejbName, new HashSet());
	    else
		_ejbToCounterMap.put(ejbName, new Integer(0));
	}
    }

    /*
     * examine if this report is valid. If report's time stamp is too close to
     * the time when corresponding jar has rebooted lately, then doesn't process
     * this report. To do so, this method returns false.
     * If the timestamp is valid then returns true.
     *
     * @return boolean value. If it's true, this instance continues to process 
     *         current FailureReport, but if it's false then skip current report.
     */
    private boolean isValid(String ejbName, long repTime)
	throws Exception
    {
	//get jar file name
	String jarName = (String)_ejbToJARMap.get(ejbName);
	int rebootDelay = recoSvc.getrebootDelaySeconds();

	//get last rebooted time of this jar file from RecoveryControl Mbean service.
	//if unable to get, then return false. (I don't know the reason but happens
        //sometimes...);
	long lastRebootStartedTime = recoSvc.mostRecentRebootStartTime( jarName );
	long lastRebootStoppedTime = recoSvc.mostRecentRebootEndTime( jarName );
	
	// if those data showed inconsistency, system.exit();
	if(lastRebootStartedTime > lastRebootStoppedTime) {
	    //Something weired has happened. Stop JVM.
	    log.error("last reboot seems to have failed.");
	    System.exit(-1);
	}
	
	// if values were 0, then it was initial access. so return true.
	// else if reptime was in the specified term, then return false.
	if(lastRebootStartedTime == 0 || lastRebootStoppedTime == 0){
		return true;
	}else if(repTime > lastRebootStartedTime && 
		 repTime <= lastRebootStoppedTime + rebootDelay){
	    return false;
	}
	return true;
    }


    /*
     * Reboot specified file (could be JAR or EAR)
     * 
     * @param jarFile jarFile name to reboot.
     *
     * @return message which indicates whether reboot has succeeded or not.
     */
    private void reboot( String fileName )
	throws Exception
    {
	String file;
	
	if ( fileName.equals( "rubis.ear" ) )
	{
	    file = Information.getCompleteFileName( "rubis.ear" );
	    recoSvc.fullReboot( file );
	}
	else
	{
	    file = Information.getCompleteFileName( "rubis.ear", fileName );
	    recoSvc.microrebootByUrl( file );
	}
    }
    
    /*
     * test method to watch how the _ejbToCounterMap evolves along with time.
     * useful to see how the counter is growing along with time.
     */
    private void printEjbsToCountMap(){
	Set keySet = _ejbToCounterMap.keySet();
	Iterator it = keySet.iterator();
	while(it.hasNext()){
	    String ejbName = (String)it.next();

	    if ( isEntityBean(ejbName) )
	    {
		_writer.print("[" + System.currentTimeMillis() + "]");
		_writer.println(ejbName);
		Set servletSet = (Set)_ejbToCounterMap.get(ejbName);
		Iterator it2 = servletSet.iterator();
		while(it2.hasNext())
		    _writer.println("    " + (String)it2.next());
	    } else {
		_writer.print("[" + System.currentTimeMillis() + "]");
		_writer.print(ejbName + " : ");
		_writer.println(((Integer)_ejbToCounterMap.get(ejbName)).intValue());
	    }
	    _writer.flush();
	}
    }

}


