package org.jboss.admin.monitor;

// standard imports
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;

// non-standard class dependencies
import org.jboss.admin.dataholder.InvocationEntry;
import org.jboss.admin.dataholder.BeanCacheEntry;
import org.jboss.admin.MetricsConnector;
import org.jboss.admin.monitor.graph.GraphModel;
import org.jboss.admin.monitor.graph.DefaultGraphModel;
import org.jboss.admin.monitor.tree.MethodTreeNode;
import org.jboss.admin.monitor.tree.BeanCacheTreeNode;
import org.jboss.admin.monitor.event.GraphModelListener;
import org.jboss.monitor.MetricsConstants;

import org.gjt.lindfors.util.LocalizationSupport;

/**
 * The main window of the monitoring tool application.
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class MonitorFrame extends JFrame implements TreeSelectionListener,
                                                    MetricsConstants,
                                                    MonitorResourceConstants {
    
            // superclass Frame implements Serializable
            
    /**
     * The tree element in the main display that presents the
     * application->bean->method hierarchy.   <p>
     *
     * Initialized in the constructor.
     */
    private ApplicationTree tree = null;

    /**
     * Support class for localization. Used for loading language resource bundles
     * and retrieving localized info.
     *
     * Initialized in the constructor.
     */
    private transient LocalizationSupport lang = null;
        
    private StatusBar statusBar = new StatusBar();
    private MonitorPane monitor = new MonitorPane();
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    /**
     * Constructs a new monitor frame. This constructor creates a frame with
     * a split layout where an application tree is placed on the left and
     * the graph and statistics are on the right. Constructing the frame
     * also attempts a connection to the JMS metrics topic (topic/metrics})
     * and registers itself as a subscriber to the topic.
     */
    public MonitorFrame() {
    
        // load the language bundle
        this.lang = new LocalizationSupport(LANG_PKG, getLocale());
    
        // create tbe basic layout
        Container c = getContentPane();
        
        // split pane with a app tree on the left and a graph on the right
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(createApplicationTree());
        split.setRightComponent(monitor);
        split.setDividerLocation(200);
        
        c.add(split, BorderLayout.CENTER);
        c.add(statusBar, BorderLayout.SOUTH);
        
        // add menu object
        setJMenuBar(new MonitorFrameMenu(this));
        
        // add window listener for close events
        addWindowListener(new WindowAdapter() {
                                
                                public void windowClosing(WindowEvent evt) {
                                    System.exit(0);    
                                }
                         });
        
        // set title and frame icon
        URL url = getClass().getResource(JBOSS_ICON);
        ImageIcon icon = new ImageIcon(url);
        setIconImage(icon.getImage());
        
        setTitle(lang.getLabel(MONITOR_FRAME));
        setSize(700, 450);
    }

    
/* 
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    // [TODO] get rid of this and create a locale singleton
    public LocalizationSupport getLanguagePackage() {
        return lang;
    }
    

/*
 *************************************************************************
 *
 *      TREE_SELECTION_LISTENER IMPLEMENTATION
 *
 *************************************************************************
 */

    /**
     * Handles the selection events in the application tree.
     *
     * @param   evt     tree selection event
     */
    public void valueChanged(TreeSelectionEvent evt) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                        tree.getLastSelectedPathComponent();
        
        if (node == null) 
            return;
            
        if (node.isLeaf() && (node instanceof MethodTreeNode)) {
         
            MethodTreeNode methodNode = (MethodTreeNode)node;
            monitor.setGraphModel(methodNode.getGraphModel());
            monitor.setStatsView((GraphModelListener)methodNode.getStatsView());
            monitor.repaint();
        }

        else if (node.isLeaf() && (node instanceof BeanCacheTreeNode)) {
         
            BeanCacheTreeNode cacheNode = (BeanCacheTreeNode)node;
            monitor.setGraphModel(cacheNode.getGraphModel());
            monitor.setStatsView((GraphModelListener)cacheNode.getStatsView());
            monitor.repaint();
        }
        
        // will do "summary" displays later
        else {
            // don't change view
            //monitor.setGraphModel(emptyGraphModel);
            //monitor.repaint();
        }
        
    }
    
    private GraphModel emptyGraphModel = new DefaultGraphModel();
    
/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */     

    public void setVisible(boolean b) {
        super.setVisible(b);
        
        Thread connectorThread = new Thread(new ConnectorThread());
        connectorThread.start();
    }
 
/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */

    /**
     * Creates the application tree component and wraps it in scroll pane.
     *
     * @return  component
     */
    private JComponent createApplicationTree() {

        tree = new ApplicationTree();
        
        // listen for the node activation/deactivation and update the graph
        // and stats models to match the selected item
        tree.addTreeSelectionListener(this);
        
        JScrollPane pane = new JScrollPane(tree);
        
        return pane;
    }
    
    
/*
 *************************************************************************
 *
 *      INNER CLASSES
 *
 *************************************************************************
 */     
 
    private class ConnectorThread implements Runnable, MessageListener {
        
        final String TOPIC      = "topic/metrics";
        final String SELECTOR   = null;
            //"JMSType = 'Invocation' OR JMSType = 'BeanCache'";
        
        public void run() {
            // connect to JMS
            try {
                statusBar.setMessage(lang.getString(CONNECTING_JNDI));
                
                MetricsConnector connector = new MetricsConnector();
                connector.setTopic(TOPIC);
                connector.setMessageSelector(SELECTOR);
                connector.connect(this);
                
                statusBar.clear();
            }
            catch (ServiceUnavailableException e) {
                statusBar.setAlert(lang.getString(JNDI_NOT_AVAILABLE));
            }
            catch (JMSException e) {
                System.err.println(e);
            }
            catch (NamingException e) {
                statusBar.setAlert(e.getMessage());
            }
        }
        
     
        /**
         * Receives the messages from JMS topic and passes them on to the
         * application tree for further processing.
         */
        public void onMessage(final Message msg) {

            SwingUtilities.invokeLater(new Runnable() {
                
                // adding invocation entries updates GUI, hence not thread safe
                public void run() {
                    try {    
                        Object msgType = msg.getJMSType();
                        
                        if (msgType.equals(INVOCATION_METRICS)) {
                            InvocationEntry entry = new InvocationEntry(msg);
                            tree.addEntry(entry);
                        }
                        else if (msgType.equals(BEANCACHE_METRICS)) {
                            BeanCacheEntry entry = new BeanCacheEntry(msg);
                            tree.addEntry(entry);
                        }
                        else {
                            // noop, we ignore unknown msg types
                            // these should get passed by msg selector anyways
                        }
                    }
                    catch (JMSException e) {
                        System.err.println(e);
                    }
                }
            });       
        }
    }
}

