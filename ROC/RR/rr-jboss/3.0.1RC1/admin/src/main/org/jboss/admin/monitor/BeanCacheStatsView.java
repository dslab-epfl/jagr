package org.jboss.admin.monitor;

// standard imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

// non-standard class dependencies
import org.jboss.admin.monitor.event.GraphModelListener;
import org.jboss.admin.monitor.event.GraphModelEvent;

import org.hs.jfc.FormPanel;
import org.gjt.lindfors.util.LocalizationSupport;


/**
 * ...
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class BeanCacheStatsView extends JPanel
        implements GraphModelListener, MonitorResourceConstants {

            // superclass implements Serializable
            
    /**
     * Support class for localization. Used for loading language resource bundles
     * and retrieving localized info.
     *
     * Initialized in the constructor.
     */
    private transient LocalizationSupport lang = null;
    
    private JLabel cacheSize     = new JLabel("0");
    private JLabel cacheCapacity = new JLabel("0");
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    /*
     * Default constructor
     */
    public BeanCacheStatsView() {
        super(new FlowLayout(FlowLayout.LEFT));
        
        //setLocale(getParent().getLocale());
        lang = new LocalizationSupport(LANG_PKG, Locale.getDefault());

        add(createLayout());        
    }
    
/*
 *************************************************************************
 *
 *      IMPLEMENTS GRAPH_MODEL_LISTENER INTERFACE
 *
 *************************************************************************
 */
 
    public void valueAppended(GraphModelEvent evt) {
    
        int value = (int)evt.getValue();

        cacheSize.setText(String.valueOf(value));
        cacheSize.repaint();
    }
    
    public void limitChanged(GraphModelEvent evt) {

        int value = (int)evt.getValue();

        if (evt.getTarget() == MAX_VERTICAL_LIMIT) {
            cacheCapacity.setText(String.valueOf(value));
            cacheCapacity.repaint();
        }
    }
    

/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */

    private JComponent createLayout() {

        FormPanel p = new FormPanel();
        
        JLabel size          = new JLabel(lang.getLabel(CACHE_SIZE) + ":");
        JLabel capacity      = new JLabel(lang.getLabel(CACHE_CAPACITY) + ":");
                
        p.add(size, cacheSize, 1, 1);           // row 1, col 1
        p.add(capacity, cacheCapacity, 2, 1);
        
        return p;
    }
        
}

