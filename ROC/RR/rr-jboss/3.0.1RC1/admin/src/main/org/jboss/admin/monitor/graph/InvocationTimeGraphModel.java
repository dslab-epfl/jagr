package org.jboss.admin.monitor.graph;

// standard imports
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

// non-standard class dependencies
import org.jboss.admin.dataholder.InvocationEntry;
import org.jboss.admin.monitor.event.GraphModelListener;
import org.jboss.admin.monitor.event.GraphModelEvent;
import org.jboss.admin.monitor.event.AggregatedInvocationEvent;
import org.gjt.lindfors.util.BoundBuffer;


/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class InvocationTimeGraphModel extends DefaultGraphModel
        implements  Runnable {
                                          

    public final static int CONTINUOUS_UPDATE       = 0x1;
    public final static int PER_INVOCATION_UPDATE   = 0x2;
    
    
    private final static int SECOND = 1000;

    private long delay = 1*SECOND;

    private Map  txMap      = Collections.synchronizedMap(new HashMap());
    private List resultList = Collections.synchronizedList(new ArrayList());
    
    private int mode = CONTINUOUS_UPDATE;
    
    private Thread updateThread = null;
    private boolean running = true;
    
    private int aggregateCount = 0;
    private double aggregateSum   = 0;
    
    private Object lock = new Object();
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    public InvocationTimeGraphModel() {
        // this(CONTINUOUS_UPDATE);
    }
    
//    public InvocationTimeGraphModel(int mode) {
//
//        this.mode = mode;
//        
//        // Just a little tweak. Add a few zeroes to the model so the 
//        // first actual entries don't look so lonely.
//        for (int i = 0; i < 10; ++i)
//            append(new Integer(0));
//            
//        switch (mode) {
//            
//            case CONTINUOUS_UPDATE:
//          
//                Thread t = new Thread(this);
//                t.start();
//                
//                break;
//                
//            case PER_INVOCATION_UPDATE:
//                
//                
//                break;
//                
//            default:
//                throw new IllegalArgumentException("Invalid mode: " + getModeAsString(mode));
//        }
//            
//    }

/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */

    public void appendInvocationEntry(InvocationEntry entry) {
        
        String txID = entry.getTxID();
        long   time = entry.getTime();
            
        if (entry.getCheckPoint().equalsIgnoreCase("START")) {
            txMap.put(txID, new Long(time));
        }
        
        else {
            Object value = txMap.remove(txID);
            
            if (value == null) {
                System.out.println("Hmm... this end point didnt have a pair: " + txID);
                return;
            }
            
            long start = ((Long)value).longValue();
            Long diff  = new Long(time-start);

            // deal with clock granularity problem, esp. winnt can't deal
            // correctly with anything that runs <10ms.
            if (diff.longValue() == 0)
                diff = new Long(1);
               
            //if (mode == PER_INVOCATION_UPDATE) 
            //    append(diff);
            //else if (mode == CONTINUOUS_UPDATE)
                resultList.add(diff);
            //else
            //    throw new InternalError("Unknown mode: " + mode);
        }            
    }

    public void setUpdateInterval(long ms) {
        if (ms <= 0) {
            System.err.println("negative or zero delay: " + ms);
            return;
        }
        
        this.delay = ms;
    }

    public long getUpdateInterval() {
        return delay;
    }

    public void startContinuousUpdate() {
        
        running = true;
        
        updateThread = new Thread(this);
        updateThread.setName("Invocation Time Update Thread");
        updateThread.setDaemon(true);
        
        updateThread.start();
    }
    
    public void stopContinuousUpdate() {   
        running = false;
        updateThread.interrupt();
    }

/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */     

    /*
     * Override the append to call our version of the fireValueAppended()
     * [TODO] this version is not thread safe
     */
    public void append(Number number) {
        xAxis.add(number);

        double avgValue = number.doubleValue();
        
        if (avgValue  > getVerticalMax())
            setVerticalMax(avgValue);

        int count  = 0;
        double sum = 0.0;
        
        // store the aggregate values set by the update thread
        // and release the thread from the wait status
        synchronized(lock) {
            count = aggregateCount;
            sum   = aggregateSum;
            
            aggregateCount = 0;
            aggregateSum   = 0.0;
            
            lock.notify();
        }
        
        fireValueAppended(avgValue, sum, count);
    }
    
    /*
     * 
     */
    protected void fireValueAppended(double avgValue, double sum, int count) {
                    
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
     
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i >= 0; i -= 2) {
            
            if (listeners[i] == GraphModelListener.class) {
             
                GraphModelEvent evt = new AggregatedInvocationEvent(this, avgValue, sum, count);
                    
                ((GraphModelListener)listeners[i+1]).valueAppended(evt);
            }
        }
    }
                                       
/*
 *************************************************************************
 *
 *      THREAD IMPLEMENTATION
 *
 *************************************************************************
 */
                                          
    /*
     * timer to update the graph within given delay
     */
    public void run() {
        
        while (running) {
            try {   
                updateModel();
                
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                running = false;
            }
        }
    }
    
    /*
     * This is just for the timer thread to call
     * other threads stay away
     */
    private void updateModel() {
        
        Object[] results = null;
        
        // lock the result list for atomic copy + clear
        synchronized (resultList) {
            
            if (resultList.size() == 0) {
                super.append(new Long(0));    // generates non-aggregated event
                return;
            }
                
            results = resultList.toArray();
            resultList.clear();
        }
        
        long sum = 0;
        
        for (int i = 0; i < results.length; ++i)
            sum += ((Long)results[i]).longValue();
        
        synchronized(lock) {
            // store the aggregate invocation size for event dispatch thread
            aggregateCount = results.length;
            aggregateSum   = sum;
            
            final Long average = new Long(sum / results.length);
            
            // ask the event dispatcher to add the new value to the model and
            // fire the required events
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    
                    // this is our overridden version of append
                    append(average); 
                }
            });
            
            // the event dispatcher thread will set this value to zero
            // once it has stored it for use in fireValueAppended() method
            while (aggregateCount != 0) {
                try {
                    lock.wait();
                }
                catch (InterruptedException e) {}
            }
        }
    }
    
    private String getModeAsString(int mode) {
        return "<NYI>";
    }
}

