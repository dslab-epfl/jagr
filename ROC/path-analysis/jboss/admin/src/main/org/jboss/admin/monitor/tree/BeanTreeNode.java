package org.jboss.admin.monitor.tree;

// standard imports
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Vector;
import java.net.URL;

// non-standard class dependencies
import org.jboss.admin.monitor.MonitorResourceConstants;


/*
 * ejb bean node that takes methods as its children
 * duplicate methods are not allowed, and are ignored
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
class BeanTreeNode extends DefaultMutableTreeNode implements SwingConstants,
                                                             MonitorResourceConstants {

    // superclass implements Serializable, Cloneable

    private final static boolean ALLOW_CHILDREN = true;

   // private final static ImageIcon beanIcon =
   //         new ImageIcon(BeanTreeNode.class.getResource(BEAN_ICON_16));

/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    /*
     * constructs a node with bean name string as user object
     * all application nodes allow children
     */
    public BeanTreeNode(String beanName) {
        super();

        setUserObject(beanName);

        // allow child nodes to be added
        setAllowsChildren(true);
        
        URL url        = getClass().getResource(BEAN_ICON_16);
        ImageIcon icon = new ImageIcon(url);

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
     * adds a new method node
     * duplicates not allowed 
     */
    public MethodTreeNode addMethod(String method) {
        MethodTreeNode node = new MethodTreeNode(method);
        
        return addMethod(node);
    }
    
    public MethodTreeNode addMethod(MethodTreeNode node) {
        
        if (children.contains(node))
            return (MethodTreeNode)children.get(children.indexOf(node));
            
        add(node);
        
        // start continuous update
        node.getGraphModel().startContinuousUpdate();
        node.getGraphModel().setUpdateInterval(3000);

        // update the tree view        
        ((RootNode)getRoot()).getTreeModel().nodesWereInserted(this, new int[] {getIndex(node)});
        
        return node;
    }

    public BeanCacheTreeNode addBeanCache() {
        BeanCacheTreeNode node = new BeanCacheTreeNode("Bean Cache");
        
        return addBeanCache(node);
    }
    
    public BeanCacheTreeNode addBeanCache(BeanCacheTreeNode node) {
        
        if (children.contains(node))
            return (BeanCacheTreeNode)children.get(children.indexOf(node));
            
        // insert cache as first node
        insert(node, 0);
        
        // start continuous update
        node.getGraphModel().startContinuousUpdate();
        node.getGraphModel().setUpdateInterval(3000);
        
        // update the tree view
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
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
            
            if (node.getUserObject().equals(this.getUserObject()))
                return true;
        }
        
        return false;
    }
}
