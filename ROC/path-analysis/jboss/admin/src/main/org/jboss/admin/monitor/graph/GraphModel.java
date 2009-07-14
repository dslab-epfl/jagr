package org.jboss.admin.monitor.graph;

// standard imports
import java.util.Collection;

// non-standard class dependencies
import org.jboss.admin.monitor.event.GraphModelListener;


/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public interface GraphModel {

    public Collection getPlotPoints();
    public void append(Number number);
    public double getVerticalMax();
    public double getVerticalMin();
    public double getHorizontalMax();
    public double getHorizontalMin();    
    public void addGraphModelListener(GraphModelListener listener);
    public void removeGraphModelListener(GraphModelListener listener);
    
}

