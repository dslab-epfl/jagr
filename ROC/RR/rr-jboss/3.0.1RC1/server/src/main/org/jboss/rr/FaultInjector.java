//
// $Id: FaultInjector.java,v 1.10 2003/03/20 08:37:41 steveyz Exp $
//

package org.jboss.RR;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.ejb.Container;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.Category;

/**
 *
 * @jmx:mbean name="jboss:type=FaultInjector"
 *            extends="org.jboss.system.ServiceMBean"
 *            
 */
public class FaultInjector 
   extends ServiceMBeanSupport
   implements FaultInjectorMBean
{
    private static ExceptionInjector exceptionInjector = new ExceptionInjector();
    private String FaultMap;
    protected ArrayList ejbModules = new ArrayList();

    // keep track of time
    private static long startTime = 0;
    private static long endTime = -1;
    private static long duration = 0;

    private static boolean experimentComplete = false;  // tells when the
                                                    // experiment is done
    private static boolean timedTest = false; // timed test going on?
    private static boolean nonTimedTest = false; // non-timed (i.e. next fault
                                                 // injected right after
                                                 // reboot)
    private static long timedTestInterval = 10000; // 10 sec default
    private static Timer timer = null;

    public FaultInjector() 
    {
    }
    
    /**
     * @jmx:managed-operation
     */    
    public void save( String filename )
    {
        exceptionInjector.save(filename);
    }


    /**
     * @jmx:managed-operation
     */    
    public void load( String filename )
    {
        exceptionInjector.load(filename);
    }


    /**
     * @jmx:managed-operation
     */
    public int experimentStepsLeft()
    {
        int numExceptions = exceptionInjector.exceptionsLeft();
        log.info("There are " + numExceptions + " left.");
        return numExceptions;
    }
    

    /**
     * @jmx:managed-operation
     */
    public void startExceptionTest()
    {
        // Start the timer
        nonTimedTest = true;
        startTime = System.currentTimeMillis();
        nextInjection();    
    }

    // a one shot timer
    class InjectTask extends TimerTask 
    {
        public void run()
        {
            nextInjection();
            this.cancel();
        }
    }
            

    /**
     * @jmx:managed-operation
     */
    public void startTimedExceptionTest(long interval)
    {
        // really start a timer
        if(interval <= 0)
        {
            log.info("Invalid interval length for timed exception test: " + interval);
            return;
        }
        
        timedTest = true;
        timedTestInterval = interval; // in milliseconds
        log.info("Starting timed exception test with period of " + interval +
                 " milliseconds.");
        startTime = System.currentTimeMillis();
        nextInjection();    
    }    

    /**
     * @jmx:managed-operation
     */
    public void stopTimedExceptionTest()
    {
        // stop the timer
        timer.cancel();
        timedTest = false;
        log.info("Timed exception test stopped!");
    }   

    // called by RestartAgent with restart is done
    public void restartComplete()
    {
        if(nonTimedTest)
        {
            nextInjection();
        }       
        // if timedTest, wait for timer to expire before injecting next fault
    }

    /**
     * @jmx:managed-operation
     */
    public void nextInjection()
    {
        // If there are no exceptions left we stop the timer
        if ( experimentComplete )
        {
	    log.info("********************************************************");    
	    log.info("*** WARNING: The experiment has already completed");
	    log.info("*** but someone has restarted another component");
	    log.info("********************************************************");
            nonTimedTest = false;
            timedTest = false;
        }
        else if ( exceptionInjector.isDone() )
        {
	    // Get completion time and exit
	    endTime = System.currentTimeMillis();
	    duration = endTime - startTime;
	    log.info("-----------------------------------------------");
	    log.info("-----------------------------------------------");    
	    log.info("Fault injection test is done in " + duration + " ms");
	    log.info("-----------------------------------------------");
	    log.info("-----------------------------------------------"); 
	    // Set a flag showing that the experiment is complete
	    experimentComplete = true;
            nonTimedTest = false;
            timedTest = false;
        }
        else  // continue injecting the exceptions
        {
	    exceptionInjector.doInjection();
            if(timedTest)
            {
                timer = new Timer();
                timer.schedule(new InjectTask(), timedTestInterval);
            }
        }
    }


    /**
     * @jmx:managed-operation
     */
    public void injectExceptionIntoService( String serviceName, 
                                            String operationName, 
                                            String exceptionName )
    {
        server.injectException(serviceName, operationName, exceptionName);
    }


    /**
     * @jmx:managed-operation
     */
    public void startInjection()
    {
        exceptionInjector.doInjection();	
    }


    /**
     * @jmx:managed-operation
     */
    public void newContainer ( Container cont )
    {
        exceptionInjector.newContainer(cont);
    }
	

    /**
     * @jmx:managed-operation
     */
    public void saveToFile()
    {
        exceptionInjector.save();
    }

    /**
     * @jmx:managed-operation
     */
    public void loadFromFile()
    {   
        exceptionInjector.load();
    }


    /**
     * @jmx:managed-operation
     */
    public String getFaultMap()
    {
        return FaultMap;
    }

    /**
     * @jmx:managed-operation
     */
    public void setFaultMap( String newMap)
    {
        FaultMap = newMap;
    }


    public void start() 
        throws Exception
    {
        loadFromFile();
        super.start();
    }
    
    public void destroy()
    {
        saveToFile();	
        super.destroy();
    }
}
