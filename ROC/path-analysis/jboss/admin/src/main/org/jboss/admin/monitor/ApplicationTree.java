package org.jboss.admin.monitor;

// standard imports
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

// non-standard class dependencies
import org.jboss.admin.dataholder.InvocationEntry;
import org.jboss.admin.dataholder.BeanCacheEntry;
import org.jboss.admin.monitor.tree.ApplicationTreeModel;
import org.jboss.admin.monitor.tree.ApplicationTreeCellRenderer;


/**
 * ...
 * 
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class ApplicationTree extends JTree {


/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    public ApplicationTree() {
        super(new ApplicationTreeModel());
        
        // Set the selection model for this tree. Our selection model only
        // allows single selection.
        super.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // set the angled lines for the tree
        putClientProperty("JTree.lineStyle", "Angled");
        
        // set a custom cell renderer
        setCellRenderer(new ApplicationTreeCellRenderer());
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */

    public void addEntry(InvocationEntry entry) {
        ((ApplicationTreeModel)getModel()).addEntry(entry);
    }
    
    public void addEntry(BeanCacheEntry entry) {
        ((ApplicationTreeModel)getModel()).addEntry(entry);
    }
    
}
