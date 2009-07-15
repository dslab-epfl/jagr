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
package roc.pinpoint.injection;

// marked for release 1.0

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import roc.pinpoint.tracing.ThreadedRequestTracer;

/**
 * This is a helper class that handles the details of deciding whether
 * or not to generate a fault.  See the EJB Tracer in
 * jboss-ext/roc.jboss.faultinjector.FaultInjectingInterceptor for
 * example usage.
 *
 */
public class FaultGenerator {

    // TODO: look up realistic numbers for all of these performance failures
    // TODO: read these numbers from the config file

    // TODO: add transient failures for exceptions, etc.

    public static long CONFIG_PERFORMANCE_GC_PERIOD = 5000;
    // sleep for this long
    public static long CONFIG_PERFORMANCE_GC_DURATION = 500;
    // then spin for this long

    public static long CONFIG_LEAK_MEM_PERIOD = 1000;
    // sleep for this long
    public static int CONFIG_LEAK_MEM_SIZE = 100000;
    // then grab an int[] of this size

    // todo move these configs to the faultConfig file
    public static long CONFIG_PERFORMANCE_CONSTANT = 50;
    public static long CONFIG_PERFORMANCE_CUMULATIVE_START = 0;
    public static long CONFIG_PERFORMANCE_STUTTER_MIN = 0;
    public static long CONFIG_PERFORMANCE_STUTTER_MAX = 100;
    public static double CONFIG_PERFORMANCE_INTERMITTENT_CHANCE = 0.5;
    public static long CONFIG_PERFORMANCE_INTERMITTENT = 100;

    // todo add config for intermittent fault

    private static FaultConfig faultConfig;
    private static Random random;
    private static long currentCumulativePerformance;
    // should technically be one counter per fault, but we're not injecting more than one fault at a time anyway...

    static {
	InitializeFaultTriggers();
	random = new Random();
	currentCumulativePerformance = CONFIG_PERFORMANCE_CUMULATIVE_START;
    }

    /**
     *  automatically called by static initializer.  Loads a fault
     *  configuration file from the location specified by the java property
     *  "roc.pinpoint.injection.FaultTriggerFile"
     */
    public static void InitializeFaultTriggers() {
        System.out.println(
            "Pinpoint.FaultGenerator: Initializing fault triggers...");
        try {
            String filename =
                System.getProperty("roc.pinpoint.injection.FaultTriggerFile");

            if (filename != null) {
                System.out.println(
                    "Pinpoint.FaultGenerator: Reading fault trigger "
                        + "information from file "
                        + filename);
                faultConfig = FaultConfig.ParseFaultConfig(new File(filename));
                System.out.println(
                    "Pinpoint.FaultGenerator: Done reading fault trigger file.");

                // special case: check for GC performance failure trigger and memory leak trigger
                Iterator iter = faultConfig.faultTriggers.iterator();
                while (iter.hasNext()) {
                    FaultTrigger ft = (FaultTrigger)iter.next();
                    if (ft.faultType == FaultTrigger.FT_PERFORMANCE_FAULT_GC) {
                        iter.remove();
                        Thread GC_thread =
                            new Thread(new GCPerformanceFailure());
                        GC_thread.setDaemon(true);
                        System.out.println(
                            "Pinpoint.FaultGenerator: spawning GC performance failure thread");
                        GC_thread.start();
                    }
		    else if( ft.faultType == FaultTrigger.FT_LEAK_MEMORY ) {
			iter.remove();
			Thread LEAK_MEMORY_thread =
			    new Thread( new LeakMemoryFailure() );
			LEAK_MEMORY_thread.setDaemon(true);
                        System.out.println(
                            "Pinpoint.FaultGenerator: spawning MEMORY LEAK failure thread");
			LEAK_MEMORY_thread.start();
		    }
                }

            }
            else {
                System.out.println(
                    "Pinpoint.FaultGenerator: No fault trigger file specified.");
            }
        }
        catch (Exception e) {
	    System.err.println( "Could not read fault config file: " + e.getMessage() );
        }
        System.out.println(
            "Pinpoint.FaultGenerator: Done initializing fault triggers.");
    }

    /**
     * returns the current fault configuration
     */
    public static FaultConfig getFaultConfig() {
	return faultConfig;
    }

    /**
     * sets the current faultconfiguration
     */
    public static void setFaultConfig( FaultConfig fc ) {
	faultConfig = fc;
	// TODO: need to check if faultconfig has a gc fault...
    }

    /**
     * this method decides whether or not to trigger a fault based on
     * a description of the current component.
     * @param currComponent attributes describing the current component
     * @return returns a FaultTrigger describing the fault that needs
     *         to be injected, or null if no fault should be injected.
     */
    public static FaultTrigger CheckFaultTriggers(Map currComponent) {
        if (faultConfig == null)
            return null;

        FaultTrigger ret = faultConfig.checkFaultTriggers(currComponent);

        if (ret != null ) {
            System.err.println(
                "PINPOINT FAULTGENERATOR: INJECTING "
		+ FaultTrigger.FAULT_TYPE_STRINGS[ret.faultType]
		+ "("
		+ ret
		+ ") into request id: "
		+  ThreadedRequestTracer.getRequestInfo().getRequestId() );
        }

        return ret;
    }

    /**
     * returns true if the FaultTrigger describes a fault that can be
     * automatically injected by the GenerateFault function.  (Other
     * faults, such as intercepting method calls, must be injected in a
     * system-specific manner).
     * @param ft describes the fault to be injected
     */
    public static boolean isAutomatableFault( FaultTrigger ft ) {
		return (ft.faultType > 0) && (ft.faultType <= FaultTrigger.IS_AUTOMATIC_FAULT);
    }

    /**
     * inject the fault specified by the fault trigger
     * @param trigger describes the fault to be injected
     */
    public static void GenerateFault(FaultTrigger trigger ) throws Exception {
		int ft = trigger.faultType;
		String faultArg = trigger.faultArg;

        try {

            if (ft == FaultTrigger.FT_NOFAULT) {
                System.out.println("INJECTED FAULT: no fault triggered");
                // do nothing
                return;
            }
            else if (ft == FaultTrigger.FT_THROWEXCEPTION ) {
				System.err.println( "INJECTED FAULT: THROW EXCEPTION: " + faultArg);
				Throwable t = null;
            	try {
            		Class cl = Class.forName( faultArg );
            		t = (Throwable)cl.newInstance();
            	}
            	catch( ClassNotFoundException ex ) {
            		System.err.println( "Tried to inject fault; but couldn't find class: " + faultArg );
            	}
            	catch( InstantiationException ex ) {
					System.err.println( "Tried to inject fault; but could not instantiate exception: " + faultArg );
            	}
            	catch( IllegalAccessException ex ) {
            		System.err.println( "Tried to inject fault; but got illegalacces while instantiating exception: " + faultArg );
            	}
            	
            	if( t instanceof Error ) {
            		throw (Error)t;
            	}
            	else if( t instanceof Exception ) {
	            	throw (Exception)t;
            	}
            	else {
            		System.err.println( "Tried to inject fault; but '" + t.toString() + " is not an exception, nor an error");
            	}
            }
            else if (ft == FaultTrigger.FT_THROWRUNTIMEEXCEPTION) {
                System.err.println("INJECTED FAULT: RuntimeException");
                throw new RuntimeException("ROC Fault System triggered Runtime Exception");
            }
            else if (ft == FaultTrigger.FT_INFINITELOOP) {
                System.err.println("INJECTED FAULT: Infinite-loop");
                while (true) {
                }
            }
            else if (ft == FaultTrigger.FT_HALTJVM) {
                System.err.println("INJECTED FAULT: Halt JVM");
                System.exit(-1);
            }
            /** GC failure is handled in a separate thread 
            else if( ft == FaultTrigger.FT_PERFORMANCE_GC ) {
            }
            **/
            else if (ft == FaultTrigger.FT_PERFORMANCE_FAULT_CONSTANT) {
                System.err.println(
                    "INJECTED FAULT: triggering constant performance failure");
                try {
                    Thread.sleep(CONFIG_PERFORMANCE_CONSTANT);
                }
                catch (InterruptedException wonthappen) {
                    wonthappen.printStackTrace();
                }
            }
            else if (ft == FaultTrigger.FT_PERFORMANCE_FAULT_CUMULATIVE) {
                System.err.println(
                    "INJECTED FAULT: triggering cumulative performance failure");
                try {
                    Thread.sleep(currentCumulativePerformance++);
                }
                catch (InterruptedException wonthappen) {
                    wonthappen.printStackTrace();
                }
            }
            else if (ft == FaultTrigger.FT_PERFORMANCE_FAULT_STUTTER) {
                System.err.println(
                    "INJECTED FAULT: triggering stutter performance stutter");

                long wait =
                    CONFIG_PERFORMANCE_STUTTER_MIN
                        + random.nextInt(
                            (int) (CONFIG_PERFORMANCE_STUTTER_MAX
                                - CONFIG_PERFORMANCE_STUTTER_MIN));
                try {
                    Thread.sleep(wait);
                }
                catch (InterruptedException wonthappen) {
                    wonthappen.printStackTrace();
                }
            }
	    else if (ft == FaultTrigger.FT_PERFORMANCE_FAULT_INTERMITTENT ) {
		System.err.println(
                    "INJECTED FAULT: triggering stutter performance stutter");

                double chance =
		    random.nextDouble();

		if( chance < CONFIG_PERFORMANCE_INTERMITTENT_CHANCE ) {
		    try {
			Thread.sleep(CONFIG_PERFORMANCE_INTERMITTENT);
		    }
		    catch (InterruptedException wonthappen) {
			wonthappen.printStackTrace();
		    }
		}
	    }
            else {
                System.err.println(
                    "Ooops! GenerateFault called with unrecognized Fault.  Are you sure you called 'isAutomatableFault()' first?");
                Exception e = new Exception();
                e.printStackTrace();
            }

        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}

class GCPerformanceFailure implements Runnable {

    public void run() {

        try {
            while (true) {
                Thread.sleep(FaultGenerator.CONFIG_PERFORMANCE_GC_PERIOD);
                long spinTillTime =
                    System.currentTimeMillis()
                        + FaultGenerator.CONFIG_PERFORMANCE_GC_DURATION;
                while (System.currentTimeMillis() < spinTillTime) {
                    // do nothing;
                }
            }
        }
        catch (InterruptedException e) {
            System.err.println(
                "Pinpoint.FaultGenerator.GCPerformanceFailure: interrupted!!! stopping GC-Fault Thread");
        }
    }

}


class LeakMemoryFailure implements Runnable {

    public void run() {

	ArrayList leak = new ArrayList();

        try {
            while (true) {
                Thread.sleep(FaultGenerator.CONFIG_LEAK_MEM_PERIOD);
		leak.add( new int[ FaultGenerator.CONFIG_LEAK_MEM_SIZE ] );
            }
        }
        catch (InterruptedException e) {
            System.err.println(
                "Pinpoint.FaultGenerator.GCPerformanceFailure: interrupted!!! stopping GC-Fault Thread");
        }
    }

}
