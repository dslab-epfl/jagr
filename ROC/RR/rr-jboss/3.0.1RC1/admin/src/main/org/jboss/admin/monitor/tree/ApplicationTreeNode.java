package org.jboss.admin.monitor.tree;

// standard imports
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

/*
 * application node that allows bean nodes as children
 * duplicate bean nodes are not allowed, and are ignored
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
class ApplicationTreeNode extends DefaultMutableTreeNode {

    // superclass implements Serializable, Cloneable

    private final static boolean ALLOW_CHILDREN = true;

    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    /*
     * constructs a node with application name string as user object
     * all application nodes allow children
     */
    public ApplicationTreeNode(String applicationName) {
        super(applicationName, ALLOW_CHILDREN);
       
        // superclass creates children collection lazily but I want it
        // to be around right after construction to avoid making NPE checks
        children = new Vector();
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    /*
     * adds new bean node
     */
    public BeanTreeNode addBean(String beanName) {
        BeanTreeNode node = new BeanTreeNode(beanName);

        return addBean(node);
    }
    
    public BeanTreeNode addBean(BeanTreeNode node) {
        
        if (children.contains(node))
            return (BeanTreeNode)children.get(children.indexOf(node));
            
        add(node);
        
        ((RootNode)getRoot()).getTreeModel().nodesWereInserted(this, new int[] {getIndex(node)});

        return node;
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
            ApplicationTreeNode node = (ApplicationTreeNode)obj;
            
            if (node.getUserObject().equals(this.getUserObject()))
                return true;
        }
        
        return false;
    }
}
