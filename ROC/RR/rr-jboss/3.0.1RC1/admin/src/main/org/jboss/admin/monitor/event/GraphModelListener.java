package org.jboss.admin.monitor.event;

// standard imports
import java.util.EventListener;


/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public interface GraphModelListener extends EventListener {

    // Constants --------------------------
    final static int MAX_VERTICAL_LIMIT   =  0x1;
    final static int MIN_VERTICAL_LIMIT   =  0x2;
    final static int MAX_HORIZONTAL_LIMIT =  0x4;
    final static int MIN_HORIZONTAL_LIMIT =  0x8;
    final static int PLOT_POINT           =  0x1000;
    
    void valueAppended(GraphModelEvent evt);

    void limitChanged(GraphModelEvent evt);
}
