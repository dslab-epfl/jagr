package org.jboss.admin.monitor.tree;

// standard imports
import java.io.Serializable;
import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.event.TreeModelListener;

// non-standard class dependencies
import org.jboss.admin.dataholder.InvocationEntry;
import org.jboss.admin.dataholder.BeanCacheEntry;
import org.jboss.admin.monitor.graph.InvocationTimeGraphModel;
import org.jboss.admin.monitor.graph.BeanCacheGraphModel;


/**
 *
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class ApplicationTreeModel extends DefaultTreeModel {

        // superclass implements serializable

    /*
     * isLeaf() will ask if the leaf allows children and determine the
     * correct rendering even if the leaf doesn't currently have any children
     */
    private final static boolean ASKS_ALLOW_CHILDREN = true;

   
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    /*
     * Creates the default application tree model - non visible root, 
     * leaves allow children
     */
    public ApplicationTreeModel() {
        super(new DefaultMutableTreeNode(), ASKS_ALLOW_CHILDREN);
        
        RootNode root = new RootNode(this);
        setRoot(root);
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    /*
     * adds a new invocation entry to this model
     * will grab the relevant info from TxEntry
     */
    public void addEntry(InvocationEntry entry) {
        
        String applicationName = entry.getApplication();
        String beanName        = entry.getBean();
        String method          = entry.getMethod();
        
        RootNode root = (RootNode)getRoot();
 
        ApplicationTreeNode appNode = root.addApplication(applicationName);
        BeanTreeNode beanNode       = appNode.addBean(beanName);
        MethodTreeNode methodNode   = beanNode.addMethod(method);
        
        // Add the invocation entry to the graph model associated with 
        // the method tree node as well.
        InvocationTimeGraphModel graphModel = methodNode.getGraphModel();
        graphModel.appendInvocationEntry(entry);        
    }

    public void addEntry(BeanCacheEntry entry) {
        
        String applicationName = entry.getApplication();
        String beanName        = entry.getBean();
        
        RootNode root = (RootNode)getRoot();
        
        ApplicationTreeNode appNode = root.addApplication(applicationName);
        BeanTreeNode beanNode       = appNode.addBean(beanName);
        BeanCacheTreeNode cacheNode = beanNode.addBeanCache();
        
        // Add the bean cache entry to the graph model associated with
        // the bean cache node
        BeanCacheGraphModel graphModel = cacheNode.getGraphModel();
        graphModel.appendBeanCacheEntry(entry);
    }
    
}

