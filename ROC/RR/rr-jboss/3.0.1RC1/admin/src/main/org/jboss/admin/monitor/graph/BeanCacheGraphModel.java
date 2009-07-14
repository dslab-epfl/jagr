package org.jboss.admin.monitor.graph;

// standard imports
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;

// non-standard class dependencies
import org.jboss.admin.dataholder.BeanCacheEntry;
import org.gjt.lindfors.util.BoundBuffer;


/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class BeanCacheGraphModel extends DefaultGraphModel
                                 implements  Runnable {
       
    private List resultList     = Collections.synchronizedList(new ArrayList());
    private Long previousValue  = new Long(0);
    private Thread updateThread = null;
    private boolean running     = true;
    private int delay           = 3000;

    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */     
    public BeanCacheGraphModel() {    } 
                     
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     
    public void appendBeanCacheEntry(BeanCacheEntry entry) {
        
        if (entry.getSize() > 0) {
            resultList.add(new Long(entry.getSize()));
        }
        
        if (entry.getCapacity() > 0) {
            setVerticalMax(entry.getCapacity());    
        }
    }

    
    public void startContinuousUpdate() {
        
        running = true;
        
        updateThread = new Thread(this);
        updateThread.setName("BeanCache Update Thread");
        updateThread.setDaemon(true);
        
        updateThread.start();
    }
    
    public void stopContinuousUpdate() {
        running = false;
        updateThread.interrupt();        
    }
    
    public int getUpdateInterva() {
        return delay;
    }
    
    public void setUpdateInterval(int delay) {
        
        if (delay < 0)
            throw new IllegalArgumentException("negative delay not allowed");
            
        this.delay = delay;
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
        
        synchronized (resultList) {
            
            if (resultList.size() == 0) {
                resultList.add(previousValue);
            }
                
            results = resultList.toArray();
            resultList.clear();
        }
        
        long sum = 0;
        
        for (int i = 0; i < results.length; ++i)
            sum += ((Long)results[i]).longValue();

        Long value = new Long(sum / results.length);
        
        append(value);
        
        previousValue = value;
    }
                                      
}
