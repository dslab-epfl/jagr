/* $Id: FaultInjectionService.java,v 1.5 2004/07/29 01:58:47 candea Exp $ */

package roc.rr.afpi;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.naming.*;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.*;
import org.jboss.invocation.*;

import roc.rr.*;
import roc.rr.afpi.util.*;

/** 
 * MBean for the AFPI fault injection service.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.5 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 **/

public class FaultInjectionService
   extends ServiceMBeanSupport
   implements FaultInjectionServiceMBean
{
    static LinkedList gobbledMem=null;
    static ObjectName serverInfo=null;

    /*================================================================================
     * MBean attributes to control activities done during MBean startup.  They can be
     * controlled from afpi.sar/META-INF/jboss-service.xml
     */

    /* If != "", start fault injection campaign.
     * Use file $JBOSS_HOME/server/default/conf/'faultLoadFile'  */
    private String faultloadFile = null; // 

    /**
     * @jmx:managed-attribute
     **/
    public String getfaultloadFile () { return faultloadFile; }
    
    /**
     * @jmx:managed-attribute
     **/
    public void setfaultloadFile ( String v ) { faultloadFile = v; }
    
    //=================================================================================

    //
    // MBean attributes for memory leak 
    //   leakXTarget: memory leak target EJB in JNDI name
    //   leakXByte:   amount of memory leak in byte
    //
    static String     leak1Target=null;
    static int        leak1Byte=0;
    static String     leak2Target=null;
    static int        leak2Byte=0;
    static String     leak3Target=null;
    static int        leak3Byte=0;
    static String     leak4Target=null;
    static int        leak4Byte=0;
    static String     leak5Target=null;
    static int        leak5Byte=0;

    /* Timer object for automatic fault injection */
    static Timer faultInjectionTimer = null;


    //
    // getter and setter for MBean attributes
    //
    
    /**
     *  getter for leak1Target
     *
     * @jmx:managed-attribute description="JNDI name of memory leak target EJB"
     **/
    public String getLeak1Target(){
	return leak1Target;
    }

    /**
     *  setter for leak1Target
     *
     * @jmx:managed-attribute
     **/
    public void setLeak1Target(String leak1Target){
	this.leak1Target = leak1Target;
    }
    
    /**
     *  getter for leak1Byte
     *
     * @jmx:managed-attribute
     **/
    public int getLeak1Byte(){
	return leak1Byte;
    }

    /**
     *  setter for leak1Byte
     *
     * @jmx:managed-attribute
     **/
    public void setLeak1Byte(int leak1Byte){
	this.leak1Byte = leak1Byte;
    }
    
    /**
     *  getter for leak2Target
     *
     * @jmx:managed-attribute
     **/
    public String getLeak2Target(){
	return leak2Target;
    }

    /**
     *  setter for leak2Target
     *
     * @jmx:managed-attribute
     **/
    public void setLeak2Target(String leak2Target){
	this.leak2Target = leak2Target;
    }
    
    /**
     *  getter for leak2Byte
     *
     * @jmx:managed-attribute
     **/
    public int getLeak2Byte(){
	return leak2Byte;
    }

    /**
     *  setter for leak2Byte
     *
     * @jmx:managed-attribute
     **/
    public void setLeak2Byte(int leak2Byte){
	this.leak2Byte = leak2Byte;
    }
    
    /**
     *  getter for leak3Target
     *
     * @jmx:managed-attribute
     **/
    public String getLeak3Target(){
	return leak3Target;
    }

    /**
     *  setter for leak3Target
     *
     * @jmx:managed-attribute
     **/
    public void setLeak3Target(String leak3Target){
	this.leak3Target = leak3Target;
    }
    
    /**
     *  getter for leak3Byte
     *
     * @jmx:managed-attribute
     **/
    public int getLeak3Byte(){
	return leak3Byte;
    }

    /**
     *  setter for leak3Byte
     *
     * @jmx:managed-attribute
     **/
    public void setLeak3Byte(int leak3Byte){
	this.leak3Byte = leak3Byte;
    }
    
    /**
     *  getter for leak4Target
     *
     * @jmx:managed-attribute
     **/
    public String getLeak4Target(){
	return leak4Target;
    }

    /**
     *  setter for leak4Target
     *
     * @jmx:managed-attribute
     **/
    public void setLeak4Target(String leak4Target){
	this.leak4Target = leak4Target;
    }
    
    /**
     *  getter for leak4Byte
     *
     * @jmx:managed-attribute
     **/
    public int getLeak4Byte(){
	return leak4Byte;
    }

    /**
     *  setter for leak4Byte
     *
     * @jmx:managed-attribute
     **/
    public void setLeak4Byte(int leak4Byte){
	this.leak4Byte = leak4Byte;
    }
    
    /**
     *  getter for leak5Target
     *
     * @jmx:managed-attribute
     **/
    public String getLeak5Target(){
	return leak5Target;
    }

    /**
     *  setter for leak5Target
     *
     * @jmx:managed-attribute
     **/
    public void setLeak5Target(String leak5Target){
	this.leak5Target = leak5Target;
    }
    
    /**
     *  getter for leak5Byte
     *
     * @jmx:managed-attribute
     **/
    public int getLeak5Byte(){
	return leak5Byte;
    }

    /**
     *  setter for leak5Byte
     *
     * @jmx:managed-attribute
     **/
    public void setLeak5Byte(int leak5Byte){
	this.leak5Byte = leak5Byte;
    }


    //=================================================================================

    //
    // MBean attributes for dead lock 
    //   deadlockTarget: dead lock target EJB in JNDI name
    //
    static String deadlockTarget = null;
    static double lockProbability  = 1.0;
    static int sleepTime         = 500;
    static int deadlockCounterThreashold = 1;
    

    /**
     *  getter for deadlockTarget
     *
     * @jmx:managed-attribute
     **/
    public String getDeadlockTarget(){
	return deadlockTarget;
    }

    /**
     *  setter for deadlockTarget
     *
     * @jmx:managed-attribute
     **/
    public void setDeadlockTarget(String deadlockTarget){
	this.deadlockTarget = deadlockTarget;
    }

    /**
     *  getter for deadlockProbability
     *
     * @jmx:managed-attribute
     **/
    public double getLockProbability(){
	return lockProbability;
    }

    /**
     *  setter for deadlockProbability
     *
     * @jmx:managed-attribute
     **/
    public void setLockProbability(double lockProbability){
	this.lockProbability = lockProbability;
    }

    /**
     *  getter for sleepTime
     *
     * @jmx:managed-attribute
     **/
    public int getSleepTime(){
	return sleepTime;
    }

    /**
     *  setter for sleepTime
     *
     * @jmx:managed-attribute
     **/
    public void setSleepTime(int sleepTime){
	this.sleepTime = sleepTime;
    }

   /**
     *  getter for deadlockCounterThreashold
     *
     * @jmx:managed-attribute
     **/
     public int getDeadlockCounterThreashold(){
	return deadlockCounterThreashold;
    }

   /**
     *  setter for deadlockCounterThreashold
     *
     * @jmx:managed-attribute
     **/
    public void setDeadlockCounterThreashold( int deadlockCounterThreashold) {
	this.deadlockCounterThreashold = deadlockCounterThreashold;
    }

    //===========================================================================//

    public FaultInjectionService()
	throws javax.management.MalformedObjectNameException
    {
	serverInfo = new ObjectName("jboss.system:type=ServerInfo");
    }


    /**
     * Schedule a leak for the given amount of memory on every call.
     *
     * @jmx:managed-operation
     **/
    public String scheduleMemoryLeak()
	throws Exception
    {
	String message = null;

	/*
	 * If leakXTarget is not null and leakXByte is larger than 0, 
	 * inject memory leak
	 *
	 * 04/20/2004 S.Kawamoto
	 */

	// leak1
	if ( (leak1Target != null) && !leak1Target.equals("") 
	     && (leak1Byte > 0) ) {
	    try {
		sendAction( leak1Target, EJBInterceptor.INJECT_MEMLEAK,
			    EJBInterceptor.MEMLEAK_BYTES_PER_CALL, 
			    new Integer(leak1Byte) );
	    } catch (Exception e){
		return "Can't schedule memory leak (" 
		    + leak1Byte + " bytes/call) in " 
		    + leak1Target + " : " + e
		    + "<br>Please check server.log";
	    }
	    message = "\n"+leak1Byte+" Byte per "+leak1Target+" call";
	}

	// leak2
	if ( (leak2Target != null) && !leak2Target.equals("") 
	     && (leak2Byte > 0) ) {
	    try {
		sendAction( leak2Target, EJBInterceptor.INJECT_MEMLEAK,
			    EJBInterceptor.MEMLEAK_BYTES_PER_CALL, 
			    new Integer(leak2Byte) );
	    } catch (Exception e){
		return "Can't schedule memory leak (" 
		    + leak2Byte + " bytes/call) in " 
		    + leak2Target + " : " + e
		    + "<br>Please check server.log";
	    }
	    message += "\n"+leak2Byte+" Byte per "+leak2Target+" call";
	}

	// leak3
	if ( (leak3Target != null) && !leak3Target.equals("") 
	     && (leak3Byte > 0) ) {
	    try {
		sendAction( leak3Target, EJBInterceptor.INJECT_MEMLEAK,
			    EJBInterceptor.MEMLEAK_BYTES_PER_CALL, 
			    new Integer(leak3Byte) );
	    } catch (Exception e){
		return "Can't schedule memory leak (" 
		    + leak3Byte + " bytes/call) in " 
		    + leak3Target + " : " + e
		    + "<br>Please check server.log";
	    }
	    message += "\n"+leak3Byte+" Byte per "+leak3Target+" call";
	}

	// leak4
	if ( (leak4Target != null) && !leak4Target.equals("") 
	     && (leak4Byte > 0) ) {
	    try {
		sendAction( leak4Target, EJBInterceptor.INJECT_MEMLEAK,
			    EJBInterceptor.MEMLEAK_BYTES_PER_CALL, 
			    new Integer(leak4Byte) );
	    } catch (Exception e){
		return "Can't schedule memory leak (" 
		    + leak4Byte + " bytes/call) in " 
		    + leak4Target + " : " + e
		    + "<br>Please check server.log";
	    }
	    message += "\n"+leak4Byte+" Byte per "+leak4Target+" call";
	}

	// leak5
	if ( (leak5Target != null) && !leak5Target.equals("") 
	     && (leak5Byte > 0) ) {
	    try {
		sendAction( leak5Target, EJBInterceptor.INJECT_MEMLEAK,
			    EJBInterceptor.MEMLEAK_BYTES_PER_CALL, 
			    new Integer(leak5Byte) );
	    } catch (Exception e){
		return "Can't schedule memory leak (" 
		    + leak5Byte + " bytes/call) in " 
		    + leak5Target + " : " + e
		    + "<br>Please check server.log";
	    }
	    message += "\n"+leak5Byte+" Byte per "+leak5Target+" call\n";
	}

	return "Requested memory leak:" + message;
    }

    /**
     * Schedule a memory leak based on a given argument.
     *
     * @param  ejbJndiName  JNDI name of the target bean
     * @param  numBytes     number of bytes to leak per call
     *
     * @jmx:managed-operation
     **/
    public String scheduleMemoryLeakByArg( String ejbJndiName, int amount )
	throws Exception
    {
	if ( leak1Target == null ) 
	{
	    leak1Target = ejbJndiName;
	    leak1Byte = amount;
	    return scheduleMemoryLeak();
	}

	if ( leak2Target == null ) 
	{
	    leak2Target = ejbJndiName;
	    leak2Byte = amount;
	    return scheduleMemoryLeak();
	}

	if ( leak3Target == null ) 
	{
	    leak3Target = ejbJndiName;
	    leak3Byte = amount;
	    return scheduleMemoryLeak();
	}

	if ( leak4Target == null ) 
	{
	    leak4Target = ejbJndiName;
	    leak4Byte = amount;
	    return scheduleMemoryLeak();
	}

	if ( leak5Target == null ) 
	{
	    leak5Target = ejbJndiName;
	    leak5Byte = amount;
	    return scheduleMemoryLeak();
	}

	return "ERROR: No empty slots found for scheduling";
    }


    /**
     * Cancel memory leak in specified EJB
     *
     * @param  ejbJndiName  JNDI name of the target bean
     *
     * @jmx:managed-operation
     **/
    public String cancelMemoryLeakByArg( String ejbJndiName )
    {
	if ( leak1Target != null && leak1Target.equals(ejbJndiName) )
	{
	    leak1Target = null;
	    leak1Byte = 0;
	    return "Memory Leak in "+ejbJndiName+" is canceled.";
	}

	if ( leak2Target != null && leak2Target.equals(ejbJndiName) )
	{
	    leak2Target = null;
	    leak2Byte = 0;
	    return "Memory Leak in "+ejbJndiName+" is canceled.";
	}

	if ( leak3Target != null && leak3Target.equals(ejbJndiName) )
	{
	    leak3Target = null;
	    leak3Byte = 0;
	    return "Memory Leak in "+ejbJndiName+" is canceled.";
	}

	if ( leak4Target != null && leak4Target.equals(ejbJndiName) )
	{
	    leak4Target = null;
	    leak4Byte = 0;
	    return "Memory Leak in "+ejbJndiName+" is canceled.";
	}

	if ( leak5Target != null && leak5Target.equals(ejbJndiName) )
	{
	    leak5Target = null;
	    leak5Byte = 0;
	    return "Memory Leak in "+ejbJndiName+" is canceled.";
	}

	return "ERROR: No memory leak in "+ejbJndiName;
    }


    /**
     * Schedule deadlock for an EJB.
     *
     * @jmx:managed-operation
     **/
    public String scheduleDeadlock()
	throws Exception
    {
	String message = null;

	/*
	 * If deadlockeTarget is not null
	 * schedule deadlock
	 *
	 */

	Vector params = new Vector();
	params.add( new Integer(deadlockCounterThreashold) );
	params.add( new Double(lockProbability) );
	params.add( new Integer(sleepTime) );

	if ( (deadlockTarget != null) && !deadlockTarget.equals("") ) {
	    try {
		sendAction( deadlockTarget, EJBInterceptor.DEADLOCK, 
			    EJBInterceptor.DEADLOCK_PARAMS, params );
	    } catch (Exception e){
		return "Can't schedule deadlock in " 
		    + deadlockTarget  + " within " + deadlockCounterThreashold
		    + " times of access. <br>Please check server.log";

	    }
	    message = deadlockTarget + " within "
		+ deadlockCounterThreashold + " times of access";

	}

	return "scheduled deadlock :" + message;
    }


    /**
     * Schedule a deadlock based on a given argument.
     *
     * @param  ejbJndiName  JNDI name of the target bean
     *
     * @jmx:managed-operation
     **/
    public String scheduleDeadlockByArg( String ejbJndiName )
	throws Exception
    {
	if ( deadlockTarget == null ) 
	{
	    deadlockTarget = ejbJndiName;
	    return scheduleDeadlock();
	}

	return "ERROR: No empty slots found for scheduling";
    }


    /**
     * Cancel deadlock
     *
     * @jmx:managed-operation
     **/
    public String cancelDeadlock()
    {

	if ( deadlockTarget != null )
	{
	    deadlockTarget = null;
	    deadlockCounterThreashold = 0;
	    try 
	    {
		String ret = cancelScheduledInjection( deadlockTarget );
		if ( ret.startsWith("Canceled") )
		    return "Deadlock in "+ deadlockTarget +" is canceled.";
	    } 
	    catch ( Exception e ) {}
	}

	return "ERROR: No deadlock in "+deadlockTarget;
    }

    /**
     * Schedule infinite loop for an EJB.
     *
     * @param ejbName   target EJB name
     * @param maxCount  maximum number of getting into infinite loop
     * @jmx:managed-operation
     **/
    public String scheduleInfiniteLoopByArg(String ejbJNDIName, int maxILoop)
	throws Exception
    {
	try {
	    sendAction( ejbJNDIName, EJBInterceptor.INFINITE_LOOP, 
			EJBInterceptor.MAX_INFINITE_LOOP, 
			new Integer(maxILoop));
	} catch (Exception e){
		return "Can't schedule infinite loop in " 
		    + ejbJNDIName  + "<br>Please check server.log";
	}
    
	return ("scheduled infinite loop : "+ejbJNDIName);
    }



    /**
     * Schedule an throwable to be thrown on every subsequent call.
     *
     * @param  ejbJndiName    JNDI name of the bean whose calls should fail
     * @param  exceptionName  name of throwable
     *
     * @jmx:managed-operation
     **/
    public String scheduleThrowable( String ejbJndiName, String throwableName ) 
	throws Exception
    {
	try {
	    sendAction( ejbJndiName, EJBInterceptor.INJECT_THROWABLE, 
			EJBInterceptor.THROWABLE_NAME, throwableName );
	}
	catch ( Exception e ) {
	    return "Injection scheduling failed: " + e;
	}

	return "Requested exception throw (" + throwableName + ") for " + ejbJndiName;
    }

    /**
     * Schedule an data corruption in specified field value
     *
     * @param ejbJndiName  JNDI name of the bean
     * @param fieldName    field name whose value will be corrupted
     * @jmx:managed-operation
     */
    public String scheduleDataCorruption(String ejbJndiName, String fieldName)
	throws Exception
    {
	try {
	    sendAction( ejbJndiName, EJBInterceptor.CORRUPT_DATA,
			EJBInterceptor.FIELD_NAME, fieldName);
	} 
	catch (Exception e)
	{
	    return "Failed to corrupt data: "+((javax.management.MBeanException)e).getTargetException();
	}

	return "Succeded to schedule corrupt data for field, "+fieldName
	    + " in "+ejbJndiName + ".";
    }
	


    /**
     * Microreboot right now.
     *
     * @param  ejbJndiName    JNDI name of the bean to microreboot
     *
     * @jmx:managed-operation
     **/
    public String microrebootNow( String ejbJndiName ) 
	throws Exception
    {
	reportReboot(true);
	ObjectName deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
	String jarURL;
	if ( ejbJndiName.endsWith(".jar")  ||  ejbJndiName.endsWith(".war") )
	    jarURL = Information.getCompleteFileName( "rubis.ear", ejbJndiName );
	else
	    jarURL = Information.getCompleteFileName( "rubis.ear", ejbJndiName + ".jar" );

	long tStart = System.currentTimeMillis();
	server.invoke( deployerSvc, "microrebootJar", 
		       new Object[] { jarURL }, 
		       new String[] { "java.lang.String" } );
	long tEnd = System.currentTimeMillis();
	reportReboot(false);

	return "Microrebooted " + ejbJndiName + " in " + (tEnd - tStart) + " msec";
    }


    /**
     * Takes the name of an app and reboots it.
     *
     * @param  appName  name of the app to be rebooted
     *
     * @jmx:managed-operation
     */
    public String rebootApplication( String appName ) 
	throws Exception
    {
	reportReboot(true);
	long tStart=0, tEnd=0;
	String pathToEar = Information.getCompleteFileName( appName + ".ear" );
	ObjectName deployerSvc = new ObjectName("jboss.system:service=MainDeployer");

	tStart = System.currentTimeMillis();
	server.invoke(deployerSvc, "redeploy", 
		      new Object[] { pathToEar }, 
		      new String[] { "java.lang.String" });
	tEnd = System.currentTimeMillis();
	reportReboot(false);

	return "Rebooted " + appName + " in " + (tEnd - tStart) + " msec";
    }


    /**
     * Sets the methodTX table in the TxInterceptorCMT interceptor to null (for this bean).
     *
     * @param  ejbJndiName  JNDI name of the bean whose injection should be canceled
     *
     * @jmx:managed-operation
     **/
    public String setNullInTxInterceptorCMT( String ejbJndiName ) 
	throws Exception
    {
	try {
	    sendAction( ejbJndiName, EJBInterceptor.SET_NULL_TXINT, null, null );
	}
	catch ( Exception e ) {
	    return "Injecting Null failed: " + e;
	}

	return "Set Null in TxInterceptorCMT for " + ejbJndiName;
    }

    /**
     * Cancels the fault injection scheduled for this bean.
     *
     * @param  ejbJndiName  JNDI name of the bean whose injection should be canceled
     *
     * @jmx:managed-operation
     **/
    public String cancelScheduledInjection( String ejbJndiName ) 
	throws Exception
    {
	try {
	    sendAction( ejbJndiName, EJBInterceptor.NO_ACTION, null, null );
	}
	catch ( Exception e ) {
	    return "Unable to cancel injection for " + ejbJndiName + "<br>Please check server.log";
	}

	return "Canceled injection for " + ejbJndiName;
    }


    /**
     * Generic path to send action requests to bean containers.
     *
     * @param  ejbName      name of the bean whose calls should leak (see JMX console)
     * @param  actionName   name of requested action
     * @param  paramKey     key for the parameter we're passing down
     * @param  paramValue   value of parameter we're passing down
     *
     **/
    private void sendAction( String ejbName, Object actionName, Object paramKey, Object paramValue ) 
	throws Exception
    {
	/* Create a fake invocation to send down the interceptor chain
	 * to notify the right EJB container */
	Invocation invocation = serviceInvocation( actionName );

	/* Put in the fake invocation the parameter */
	if ( paramKey != null )
	    invocation.setValue(paramKey, paramValue, PayloadKey.PAYLOAD);

	/* Send the fake invocation to the EJB's container */
	ObjectName container = ComponentMap.getContainerName( ejbName );
	if( container == null )
	    throw new MissingResourceException( ejbName + " is not deployed", "", "" );

	Object foo = server.invoke(container, "invoke",
				   new Object[] { invocation },
				   new String[] { "org.jboss.invocation.Invocation" });
    }


    /* Create a fake invocation to send down the interceptor chain to
     * notify the right EJB container */
    private static Invocation serviceInvocation ( Object serviceAction )
    {
	Invocation inv = new Invocation( EJBInterceptor.SERVICE_CALL, null, null, null, null, null );
	inv.setValue(EJBInterceptor.SERVICE_ACTION, serviceAction, PayloadKey.PAYLOAD);
	return inv;
    }


    /**
     * Gobble up approximately the given amount of memory.
     *
     * @param  mbytes  number of MB to gobble up
     * @param  kbytes  number of KB (beyond mbytes) to gobble up
     *
     * @jmx:managed-operation
     **/
    public String gobbleMemory( int mbytes, int kbytes ) 
	throws Exception
    {
	if ( gobbledMem == null )
	    gobbledMem = new LinkedList();

	long preMem = ((Long) server.getAttribute(serverInfo, "FreeMemory")).longValue();
	long postMem = preMem;
	String note = "";
	
	try 
	{
	    for ( ; mbytes>0 ; mbytes--) 
		gobbledMem.add( (Object) (new int[256*1024]) );

	    for ( ; kbytes>0 ; kbytes-- )
		gobbledMem.add( (Object) (new int[256]) );
	}
	catch (OutOfMemoryError e) {
	    note = "<br><br>(stopped due to <i>OutOfMemoryError</i>; wanted to gobble " + 
		   mbytes + " MB + " + kbytes + " KB more, but couldn't)";
	}

	postMem = ((Long) server.getAttribute(serverInfo, "FreeMemory")).longValue();
	return "Pre-gobbling:  Free mem = " + preMem/1024 +
	       " KB<br>Post-gobbling:  Free mem = " + postMem/1024 + " KB<br>" + note;
    }

    /**
     * Free all the memory that was gobbled up by the fault injector.
     *
     * @jmx:managed-operation
     **/
    public String freeGobbledMemory() 
    {
	gobbledMem = null;
	Runtime r = Runtime.getRuntime();
	r.gc();
	return "OK";
    }

    /**
     * Start the automatic fault injection service. Read faultload
     * from the XML configuration file and schedule the injections.
     *
     * @param  fileName   XML file to read in.
     *
     * @jmx:managed-operation
     **/
    public String startFaultInjectionCampaign( String fileName ) 
    {
	// initialize or reset the timer (as needed) 
	stopFaultInjectionCampaign();
	faultInjectionTimer = new Timer();

	// get absolute path to configuration file
	String absoluteFileName;
	if( fileName.startsWith( "/" )) {
	    // it's already a 
	    absoluteFileName = fileName;
	}
	else {
	    try {
		ObjectName InfoSvc = new ObjectName( "jboss.system:type=ServerConfig" );
		String serverHomeDir = (String) ( (File) server.getAttribute(InfoSvc, "ServerHomeDir") ).getAbsolutePath();
		absoluteFileName = serverHomeDir + "/conf/" + fileName;
	    } 
	    catch ( Exception e ) {
		e.printStackTrace();
		faultInjectionTimer = null;
		return "ERROR: Cannot find configuration file";
	    }
	}

	// parse XML config file and get a list of fault injection actions
	List injections = null;
	try {
	    XMLParser parser = XMLParser.getInstance();
	    injections = parser.parseFaultInjection( absoluteFileName );
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	    faultInjectionTimer = null;
	    return "ERROR: Cannot parse configuration file " + absoluteFileName;
        }

	// schedule a timer task for each fault
	FaultInjection injection=null;
	FaultInjectionTask task=null;
	Date lastDate = new Date();
	Date date;
	Iterator it = injections.iterator();
	while ( it.hasNext() ) 
	{
	    injection = (FaultInjection) it.next();
	    task = new FaultInjectionTask( injection, this, log );
	    date = injection.getDate();
	    if ( date.compareTo(lastDate) > 0 ) {
		lastDate = date;
	    }
	    faultInjectionTimer.schedule( task, date );
	}

	// schedule an "end of campaign" event after the last fault
	if ( injection != null )
	{
	    Date newDate  = new Date( 100 + lastDate.getTime() ); // 100 msec later
	    injection = new FaultInjection( FaultInjection.END_OF_CAMPAIGN, "", newDate );
	    task = new FaultInjectionTask( injection, this, log );
	    faultInjectionTimer.schedule( task, newDate );
	}
	
	log.info( "User started fault injection campaign" );
	return "Successfully started fault injection campaign.";
    }

    /**
     * cancel automatic injected faults.
     *
     * @jmx:managed-operation
     **/
    public String stopFaultInjectionCampaign() 
    {
	if ( faultInjectionTimer == null )
	    return "There is no fault injection campaign in progress";

	faultInjectionTimer.cancel();
	log.info( "Fault injection campaign stopped" );
	return "Successfully stopped fault injection campaign";
    }


    /**
     * Unbind the given name from JNDI.
     *
     * @param name  JNDI name to unbind
     * @return String describing the result
     * @jmx:managed-operation
     **/
    public String unbindJndiName( String jndiName )
    {
	try 
	{
	    InitialContext ic = new InitialContext();
	    ic.unbind( jndiName );
	    return "Successfully unbound " + jndiName + " from JNDI";
	}
	catch( Exception e ) 
	{
	    e.printStackTrace();
	    return "Couldn't unbind " + jndiName + " from JNDI (see server log)";
	}
    }


    /** 
     * report reboot incident to the client host.
     *
     * @param isStart if this is start of the reboot, then true,
     *                if this is end of the reboot, then false.
     */
    public void reportReboot(boolean isStart)
    {
	try
	{
	    DatagramSocket socket = new DatagramSocket();
	    //string to byte stream
	    DatagramPacket packet = new DatagramPacket(new byte[64], 64);
	    String contents = null;
	    if (isStart)
	    {
		contents = "starting reboot";
	    }
	    else
	    {
		contents = "reboot finished";
	    }
	    byte[] byteContents = contents.getBytes();
	    packet = new DatagramPacket(byteContents, byteContents.length, InetAddress.getByName(RecoveryService.clientHostName), RecoveryService.clientPort);
	    //send
	    socket.send(packet);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }


    public void start() 
	throws Exception
    {
	if ( faultloadFile==null  ||  faultloadFile.equals("") )
	    return;
	
	startFaultInjectionCampaign( faultloadFile );
    }
		
    public void stop()
    { 
	stopFaultInjectionCampaign(); 
    }

    public void create() throws Exception   {}
    public void destroy()                   {}
}
