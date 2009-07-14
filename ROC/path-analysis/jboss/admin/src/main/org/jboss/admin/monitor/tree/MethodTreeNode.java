package org.jboss.admin.monitor.tree;

// standard imports
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

// non-standard class dependencies
import org.jboss.admin.monitor.graph.InvocationTimeGraphModel;
import org.jboss.admin.monitor.event.GraphModelListener;
import org.jboss.admin.monitor.InvocationTimeStatsView;

/**
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class MethodTreeNode extends DefaultMutableTreeNode {

    // superclass implements Serializable, Cloneable

    private final static boolean ALLOW_CHILDREN = false;

    /*
     * each node contains a reference to the graph model that visualizes
     * the data received as InvocationEntries from metrics invocation layer
     *
     * initialized in the constructor
     */
    private InvocationTimeGraphModel graphModel  =  null;
    
    private JComponent statsView = null;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    /*
     * constructs a node with method as user object
     * no children allowed
     */
    public MethodTreeNode(String method) {
        super(method, ALLOW_CHILDREN);

        // superclass creates children collection lazily but I want it
        // to be around right after construction to avoid making NPE checks
        children = new Vector();
        
        // initialize the graph model and stats view for this node
        graphModel = new InvocationTimeGraphModel();
        statsView  = new InvocationTimeStatsView();
        graphModel.setVerticalMax(100);
        
        // have the stats view listen to the model
        graphModel.addGraphModelListener((GraphModelListener)statsView);
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    /*
     * each method node contains a reference to a graph model that represents
     * the data from the metrics invocation layer
     */     
    public InvocationTimeGraphModel getGraphModel() {
       return graphModel; 
    }
    
    public JComponent getStatsView() {
        return statsView;
    }

/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */
 
    public int hashCode() {
        return getUserObject().hashCode();
    }
    
    public boolean equals(Object obj) {
     
        if (obj == null)
            return false;
            
        if (this == obj)
            return true;
            
        if (getClass().equals(obj.getClass())) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
            
            if (node.getUserObject().equals(this.getUserObject()))
                return true;
        }
        
        return false;
    }

}
