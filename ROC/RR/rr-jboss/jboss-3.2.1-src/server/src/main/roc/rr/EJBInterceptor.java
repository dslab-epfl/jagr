package roc.rr;

import java.net.*;
import java.util.*;
import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.rmi.ServerException;

import javax.ejb.*;
import javax.management.*;

import org.jboss.ejb.*;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.Container;
import org.jboss.metadata.*;
import org.jboss.invocation.*;
import org.jboss.logging.Logger;
import org.jboss.ejb.plugins.*;

/** 
 *  An EJB interceptor that executes various actions (e.g., fault
 *  injections) when requested by the FaultInjectionService.
 *
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.11 $
 */

public class EJBInterceptor 
    extends org.jboss.ejb.plugins.AbstractInterceptor
{
    private Logger Log;

    // The interceptor can be asked to perform actions by passing it
    // an Invocation object whose ID is SERVICE_CALL; the action is
    // associated with SERVICE_ACTION key in the Invocation hashmap.
    public final static Object SERVICE_CALL     = new Object();
    public final static Object SERVICE_ACTION   = new Object();

    // Action to take on the next intercepted invocation
    private int action = Action.NO_ACTION;

    // Auxiliary stuff
    private LinkedList LeakedMem      = null;  // "leaked" memory (goes away on uRB)
    private int        LeakNumBytes   = 0;     // # of bytes to leak on each call
    private String     ThrowableName  = null;  // name of Throwable to throw
    private int        MaxILoop       = 0;
    private int        ILoopCount     = 0;    // infinite loop counter

    // Auxiliary stuff for deadlock ==================================
    private Object     lock_a         = new Object(); //lock object
    private Object     lock_b         = new Object(); //lock object
    private boolean    locked         = false;        //whether lock_b is first gotten
    private double     LockProb      = 0;  //probability of getting into deadlock stack, after once getting deadlock.
    private int        SleepTime      = 0;  //ms. for this term waits for deadlock to happen.
    private int        DeadlockCounterThreashold = 0; //after this number of access to this interceptor, deadlock is highly possible to happen.
    private int        deadlockCounter = 0;
    // ================================================================
 
    // Any time we enter an invocation, we add the current thread to
    // this list; if at uRB time the list has any threads on it, we
    // stop them
    private List threadKillList = null;

    /**
     * Convert int id to Integer id 
     *
     * @param intid  action id or action argument id in int
     **/
    private Integer getId(int intId) { return new Integer(intId); }

    /** 
     * Intercept invocation and perform any requested actions.
     *
     * @param invocation  all info necessary to carry out invocation
     *
     **/
    public Object interceptInvocation( Invocation invocation ) 
	throws Exception 
    {
	if ( invocation.getId() == SERVICE_CALL )
	{
	    handleServiceCall( invocation );
	    return null; // don't propagate down the interceptor chain
	}
	else  // a regular invocation; check if we need to do anything
	{
	    if ( action != Action.NO_ACTION )
		doServiceAction();
	}

	return getNext().invoke( invocation );
    }

    /** 
     * Decypher the requested action from a SERVICE_CALL Invocation.
     *
     * @param invocation  all info necessary to carry out invocation
     *
     **/
    private synchronized void handleServiceCall( Invocation invocation ) 
	throws Exception 
    {
	action = ((Integer)invocation.getValue( SERVICE_ACTION )).intValue();
	    
	if ( action == Action.NO_ACTION ) 
	{
	    Log.info( "Canceled previously scheduled actions" );
	}
	else if ( action == Action.MICROREBOOT )
	{
	    Log.info( "uRB scheduling not currently supported" );
	}
	else if ( action == Action.INJECT_MEMLEAK )
	{
	    Integer bytes = (Integer) invocation.getValue(getId(Action.MEMLEAK_BYTES_PER_CALL));
	    LeakNumBytes = bytes.intValue();
	    Log.info( "Scheduled memory leak for " + LeakNumBytes + " bytes/call" );
	}
	else if ( action == Action.INJECT_THROWABLE )
	{
	    ThrowableName =  (String) invocation.getValue(getId(Action.THROWABLE_NAME ));
	    Log.info( "Scheduled injection of throwable " + ThrowableName );
	}
	else if ( action == Action.SET_NULL_TXINT )
	{
	    Interceptor securityInterceptor = getNext();
	    TxInterceptorCMT txInterceptorCMT = (TxInterceptorCMT) securityInterceptor.getNext();
	    txInterceptorCMT.methodTx = null;
	    Log.info( "Nulled out methodTX hashtable in SecurityInterceptor" );
	}
	else if ( action == Action.DEADLOCK )
	{
	    Vector v = (Vector) invocation.getValue(getId(Action.DEADLOCK_PARAMS));
	    DeadlockCounterThreashold = ( (Integer) v.get(0) ).intValue();
	    LockProb = ((Double)v.get(1)).doubleValue();
	    SleepTime = ((Integer)v.get(2)).intValue();
	    deadlockCounter = 0;
	    Log.info( "Scheduled dead lock. It should happen after "
		      + DeadlockCounterThreashold + " times of access. "
		      + "Depending on timing, there might be a case it won't happen." );
	}
	else if ( action == Action.INFINITE_LOOP )
	{
	    Integer max = (Integer) invocation.getValue(getId(Action.MAX_INFINITE_LOOP ));
	    MaxILoop = max.intValue();
	    Log.info( "Scheduled infinite loop for consecutive "+MaxILoop
		      +" calls" ); 
	}
	else if ( action == Action.CORRUPT_FIELD ) {
	    String logMessage = null;
	    action = Action.NO_ACTION;
	    String fieldName = (String)invocation.getValue(getId(Action.FIELD_NAME));

	    try {
		boolean result = setFieldNull(fieldName);
		if (result){
		    logMessage = "Scheduled data corruption for field, "+fieldName;
		} else {
		    logMessage = "Currently corrupt data fault injection isn't applicable to stateful session bean and entity bean";
		    throw new javax.resource.NotSupportedException(logMessage);
		}
	    } catch (NoSuchFieldException e) {
		logMessage = "Failed to corrupt data: no such field, "+fieldName;
		throw e;
	    } catch (Exception e) {
		logMessage = "Failed to corrupt data: "+e;
		throw e;
	    } finally {
		Log.info(logMessage);
	    }
	}
	else
	{
	    Log.info( "Unknown request" );
	    action = Action.NO_ACTION;
	}
    }


    /** 
     * Decypher the requested action from a SERVICE_CALL Invocation.
     *
     * @param invocation  all info necessary to carry out invocation
     *
     **/
    private void doServiceAction () 
	throws Exception
    {
	if( action == Action.MICROREBOOT )
        {
	    Log.info( "Microreboot scheduling not supported" );
	}
	else if( action == Action.INJECT_MEMLEAK )
	{
	    LeakedMem.add( (Object) (new int[LeakNumBytes/4]) );
	    Log.debug( "Injecting memory leak" );
	}
	else if( action == Action.INJECT_THROWABLE )
	{
	    Log.debug( "Injecting Exception" );
	    throw new Exception( ThrowableName );
	}
	else if( action == Action.DEADLOCK )
	{
	    Log.debug( "Injecting deadlock" );
	    getLock();
	}
	else if( action == Action.INFINITE_LOOP )
	{
	    Log.debug( "Injecting infinite loop" );
	    infiniteLoopProcess();
	}
	else
	{
	    Log.info( "Don't know what to do" );
	}
    }

    private void getLock() {
	deadlockCounter++;
	Log.debug(" counter number is " + deadlockCounter);
	if ( deadlockCounter >= DeadlockCounterThreashold ) {
	    if ( deadlockCounter == DeadlockCounterThreashold ) {
		synchronized (lock_b) {
		    Log.info("Got lock B. High probability to dead lock.");
		    locked = true;
		    try {
			Thread.sleep(SleepTime);
		    } catch (Exception e ) {
			//do nothing
		    }
		    synchronized (lock_a) {
			locked = false;
		    }
		}
	    } else {
		synchronized (lock_a) {
		    if( locked )
			Log.error("Deadlock!!");
		    synchronized (lock_b) {
		    }
		}
	    }
	} else {
	    //do nothing. getting other locks.
	}
	
    }

    private void infiniteLoopProcess(){
	System.out.println (" count is " + ILoopCount + " max is " + MaxILoop  );
	if (ILoopCount < MaxILoop) {
	    ILoopCount++;
	    System.out.println(" I'm in the loop. " );
	    while(true){}
	}
    }


    /**
     *  set null to the field of all ejb instances
     *
     *  @param fieldName   field name
     *  @throwns NoSuchFieldException specified field name doesn't mach existing filed of the target instance
     *  @return if target is stateless session bean and successfully injected data corruption, return true otherwise false
     */
    private boolean setFieldNull(String fieldName)
	throws Exception
    {
	String cname = container.getClass().getName();
	if ( cname.equals("org.jboss.ejb.StatelessSessionContainer") )
        {
	    AbstractInstancePool aip = (AbstractInstancePool)((StatelessSessionContainer)container).getInstancePool();

	    // 
	    // If there are no existing instances, then create one 
	    //
	    int poolOriginalSize = aip.getCurrentSize();
	    int poolSize = (poolOriginalSize == 0 ) ? 1 : poolOriginalSize;

	    EnterpriseContext[] ec = new EnterpriseContext[poolSize];
	    Object[] ejbInstances = new Object[poolSize];

	    //
	    // Extract all StatelessEnterpriseContexts and ejb instances
            //  from instance pool
	    //
	    for(int i=0;i<poolSize;i++) {
		ec[i] = aip.get();
		ejbInstances[i] = ec[i].getInstance();		
	    }

	    //
	    // Extract the target field object from class object.
	    // If the field doesn't exist, then NoSuchFieldException is thrown.
	    //
	    Class c = ejbInstances[0].getClass();
	    Field f = c.getDeclaredField(fieldName);
	    f.setAccessible(true);   // set accessible to private field

	    //
	    // Corrupt the value of the field. 
	    // Actually change its value into null.
	    //
	    for(int i=0;i<ejbInstances.length;i++){
		f.set(ejbInstances[i],null);
	    }

	    //
	    // free all instances ( restore to pool )
	    //
	    for(int i=0;i<poolSize;i++)
		aip.free(ec[i]);
	} 
	else 
	{
	    /*
	      Stateful session bean and entity bean versions haven't 
	      implemented yet. 
	      In these cases, both instance pool and instance cache 
	      seem to have proxy instance instead of ejb instance. 
	      Through such proxy instances we can't touch any field 
	      of its associated ejb instance.
	    */
	    return false;
	} 

	return true;
    }



    /**
     * Instantiates the interceptor (one instance of the interceptor
     * is created for each bean type, not one for each bean instance).
     **/
    public void create() 
	throws Exception 
    {
        super.start();
	LeakedMem = new LinkedList();
	threadKillList = Collections.synchronizedList( new LinkedList() );
	Log = Logger.getLogger(this.getClass().getName() + ", EJB=" + 
			       container.getBeanMetaData().getEjbName());
    }

    public void stop()
    {
	Iterator iter = threadKillList.iterator();
	while ( iter.hasNext() )
        {
	    Thread t = (Thread) iter.next();
	    if ( t == Thread.currentThread() )
		Log.error( "I am on the kill list" );
	    else
	    {
		Log.info( "Killing thread " + t );
		t.stop();
	    }
	}
    }


    /** 
     * Handle an invocation.
     *
     * @param invocation  all info necessary to carry out invocation
     *
     **/
    public Object invoke( Invocation invocation ) throws Exception
    {
	threadKillList.add( Thread.currentThread() );
	Object result = interceptInvocation( invocation );
	threadKillList.remove( Thread.currentThread() );
	return result;
    }


    /** 
     * Handle a home invocation (pass on to the next interceptor).
     *
     * @param invocation  all info necessary to carry out invocation
     *
     **/
    public Object invokeHome( Invocation invocation ) throws Exception 
    {
	threadKillList.add( Thread.currentThread() );
	Object result = getNext().invokeHome( invocation );
	threadKillList.remove( Thread.currentThread() );
	return result;
    }


    public void      setContainer(Container container) { this.container = container; }
    public Container getContainer()                    { return container; }
}
