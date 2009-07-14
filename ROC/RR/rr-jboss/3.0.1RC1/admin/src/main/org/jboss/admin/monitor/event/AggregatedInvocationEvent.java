package org.jboss.admin.monitor.event;

/**
 * ...
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class AggregatedInvocationEvent extends GraphModelEvent {

    private int count   = 0;
    private double sum  = 0.0;
    
    public AggregatedInvocationEvent(Object source, double avgValue, double sum, int count) {
        super(source, avgValue);
        
        if (count <= 0)
            throw new IllegalArgumentException("count <= 0");
        
        this.sum   = sum;
        this.count = count;
    }

    public int getInvocationCount() {
        return count;
    }
    
    public double getSumValue() {
        return sum;
    }
    
}
