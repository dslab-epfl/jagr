package org.jboss.admin.monitor.event;

// standard imports
import java.util.EventObject;


/**
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class GraphModelEvent extends EventObject {

    /* initialized in the constructor */
    private double value;
    private int target = GraphModelListener.PLOT_POINT;
    
    public GraphModelEvent(Object source, double value) {
        super(source);
        
        this.value = value;
    }

    /*
     * @param target   target constant from GraphModelListener interface
     */
    public GraphModelEvent(Object source, double value, int target) {
        this(source, value);
        this.target = target;
    }
    
    public double getValue() {
        return value;
    }
    
    public int getTarget() {
        return target;
    }
    
}


