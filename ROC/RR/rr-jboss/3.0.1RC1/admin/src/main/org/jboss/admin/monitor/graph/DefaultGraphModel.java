package org.jboss.admin.monitor.graph;

// standard imports
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

// non-standard class dependencies
import org.jboss.admin.monitor.event.GraphModelListener;
import org.jboss.admin.monitor.event.GraphModelEvent;
import org.gjt.lindfors.util.BoundBuffer;


/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class DefaultGraphModel implements GraphModel {

    protected Collection xAxis = Collections.synchronizedCollection(new BoundBuffer(1000));
    protected EventListenerList listenerList = new EventListenerList();
    
    protected double max = 1.0;
    protected double min = 0.0;
    
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     
 
    /*
     * note that cannot max < min
     */
    public void setVerticalMax(double max) {
        this.max = max;
        
        fireLimitChanged(max, GraphModelListener.MAX_VERTICAL_LIMIT);
        
        if (max < min)
            setVerticalMin(max);
    }
    
    public double getVerticalMax() {
        return max;
    }
    
    /*
     * note that cannot min > max
     */
    public void setVerticalMin(double min) {
        this.min = min;
        
        fireLimitChanged(min, GraphModelListener.MIN_VERTICAL_LIMIT);
        
        if (min > max)
            setVerticalMax(min);
    }
    
    public double getVerticalMin() {
        return min;
    }
  
    public double getHorizontalMin() {
        throw new Error("NYI");
    }
    
    public double getHorizontalMax() {
        throw new Error("NYI");
    }
/*
 *************************************************************************
 *
 *      IMPLEMENTS GRAPH_MODEL INTERFACE
 *
 *************************************************************************
 */
 
    /*
     * append is thread safe
     * will automatically set max
     */
    public void append(Number number) {
        
        xAxis.add(number);        
        final double value = number.doubleValue();

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    append(value);
                }
            });
        }
        else
            append(value);
    }
        
    private void append(double value) {        
        if (value  > getVerticalMax())
            setVerticalMax(value);
            
        fireValueAppended(value);
    }
    
    /*
     */
    public Collection getPlotPoints() {
        Object[] points = xAxis.toArray();
        ArrayList plots = new ArrayList();
        
        for (int i = 0; i < points.length; ++i)
            plots.add(new Point(i, ((Number)points[i]).intValue()));
            
        return plots;
    }
    
    public void addGraphModelListener(GraphModelListener listener) {
        listenerList.add(GraphModelListener.class, listener);
    }
    
    public void removeGraphModelListener(GraphModelListener listener) {
        listenerList.remove(GraphModelListener.class, listener);
    }
    
/*
 *************************************************************************
 *
 *      PROTECTED METHODS
 *
 *************************************************************************
 */
 
    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.
     */
    protected void fireValueAppended(double value) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
     
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i >= 0; i -= 2) {
            
            if (listeners[i] == GraphModelListener.class) {
             
                GraphModelEvent evt = new GraphModelEvent(this, value);
                    
                ((GraphModelListener)listeners[i+1]).valueAppended(evt);
            }
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.
     */
    protected void fireLimitChanged(double value, int target) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
     
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i >= 0; i -= 2) {
            
            if (listeners[i] == GraphModelListener.class) {
             
                GraphModelEvent evt = new GraphModelEvent(this, value, target);
                    
                ((GraphModelListener)listeners[i+1]).limitChanged(evt);
            }
        }
    }
    
}

