package org.jboss.admin.monitor.tree;

// standard imports
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;


/*
 * Node representing the root of application tree.
 * This node accepts application nodes as children.
 * No duplicates are allowed.
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
class RootNode extends DefaultMutableTreeNode {

    // superclass implements Serializable, Cloneable

    private final static boolean ALLOW_CHILDREN = true;

    /*
     * model reference accessible to all node objects of the tree
     */
    private ApplicationTreeModel treeModel = null;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    /*
     * Default constructor. This is here just for javabean/serialization
     * compatibility. Note that using this constructor leaves the created
     * object in an unstable state (treeModel == null).
     */
     public RootNode() {
         this(null);
     }
     
    /*
     *
     */
    public RootNode(ApplicationTreeModel treeModel) {
        super("Root", ALLOW_CHILDREN);

        // superclass creates children collection lazily but I want it
        // to be around right after construction to avoid making NPE checks
        children = new Vector();
        
        // this acts as a link for all the nodes to the underlying model,
        // i.e.  someNode.getRoot().getModel();
        this.treeModel = treeModel;
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    /*
     * sets the tree model for the root node.
     */
     public void setTreeModel(ApplicationTreeModel treeModel) {
         this.treeModel = treeModel;
     }
     
    /*
     * Returns the tree model this root node is associated with.
     */
    public ApplicationTreeModel getTreeModel() {
        return treeModel;
    }
     
    /*
     * adds a new application
     * returns the node that represents this app
     */
    public ApplicationTreeNode addApplication(String applicationName) {
        ApplicationTreeNode node = new ApplicationTreeNode(applicationName);
        
        return addApplication(node);
    }
    
    /*
     * adds a new app node
     * returns a node that represents this app. if node already exists in 
     * the tree, returns the node from tree
     */
    public ApplicationTreeNode addApplication(ApplicationTreeNode node) {

        if (children.contains(node))
            return (ApplicationTreeNode)children.get(children.indexOf(node));
            
        add(node);
        
        ((RootNode)getRoot()).getTreeModel().nodesWereInserted(this, new int[] {getIndex(node)});

        return node;
    }
 

}
