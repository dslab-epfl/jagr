package org.jboss.admin.monitor.tree;

// standard imports
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

// non-standard class dependencies
import org.jboss.admin.monitor.graph.BeanCacheGraphModel;
import org.jboss.admin.monitor.BeanCacheStatsView;
import org.jboss.admin.monitor.event.GraphModelListener;

/**
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class BeanCacheTreeNode extends DefaultMutableTreeNode {

    // superclass implements Serializable, Cloneable

    private final static boolean ALLOW_CHILDREN = false;

    /*
     * ...
     *
     * initialized in the constructor
     */
    private BeanCacheGraphModel graphModel  =  null;
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
    public BeanCacheTreeNode(String name) {
        super(name, ALLOW_CHILDREN);

        // superclass creates children collection lazily but I want it
        // to be around right after construction to avoid making NPE checks
        children = new Vector();
        
        // initialize the graph model and stats view for this node
        graphModel = new BeanCacheGraphModel();
        statsView  = new BeanCacheStatsView();
        graphModel.setVerticalMax(50);
        
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
    public BeanCacheGraphModel getGraphModel() {
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

