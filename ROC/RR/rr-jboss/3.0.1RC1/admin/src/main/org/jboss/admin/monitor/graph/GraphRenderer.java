package org.jboss.admin.monitor.graph;

// standard imports
import java.awt.Component;

// non-standard class dependencies
import org.jboss.admin.monitor.GraphView;
import org.jboss.admin.monitor.event.GraphModelListener;


/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public interface GraphRenderer extends GraphModelListener {

    Component getGraphRendererObject(GraphView graph);
    Component getVerticalControlRendererObject(GraphView graph);
    Component getHorizontalControlRendererObject(GraphView graph);
    
}

