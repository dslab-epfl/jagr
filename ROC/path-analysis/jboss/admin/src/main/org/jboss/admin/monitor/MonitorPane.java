package org.jboss.admin.monitor;

// standard imports
import java.awt.GridLayout;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;

// non-standard class dependencies
import org.jboss.admin.monitor.graph.GraphModel;
import org.jboss.admin.monitor.event.GraphModelListener;
import org.gjt.lindfors.util.LocalizationSupport;

/**
 * ...
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */     
public class MonitorPane extends JPanel implements MonitorResourceConstants {

    private GraphView graph = null;
    private GraphModelListener stats = null;
    
    /**
     * Support class for localization. Used for loading language resource bundles
     * and retrieving localized info.  <p>
     *
     * Initialized in the constructor.
     */
    private transient LocalizationSupport lang = null;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */     
    
    public MonitorPane() {
        super(new GridLayout(0, 1, 0, 10));
        
        // load the language bundle
        // [TODO] fix this to use thelocale singleton
        this.lang = new LocalizationSupport(LANG_PKG, Locale.getDefault());
        
        init();
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     
 
    public void setGraphModel(GraphModel model) {
        graph.setModel(model);
    }
    
    public void setStatsView(GraphModelListener listener) {
        
        JComponent comp = (JComponent)listener;
        
        remove((JComponent)stats);
        //graph.getModel().removeGraphModelListener(stats);
        
        decorateStats(comp);
        stats = listener;
        
        //graph.getModel().addGraphModelListener(listener);
        add(comp);
    }
/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */     

    private void init() {

        // Create the initial graph view. This view's model will be changed
        // according to to the node selected in the application tree.
        graph = new GraphView();
        stats = new InvocationTimeStatsView();
        graph.getModel().addGraphModelListener(stats);
        
        EtchedBorder graphLines    = new EtchedBorder();
        TitledBorder graphTitle    = new TitledBorder(graphLines, "  Graph View  ");
        EmptyBorder  graphInsets   = new EmptyBorder(0, 5, 5, 5);
        CompoundBorder graphBorder = new CompoundBorder(graphTitle, graphInsets);
                                             
        EtchedBorder statsLines  = new EtchedBorder();
        TitledBorder statsBorder = new TitledBorder(statsLines, "  Statistics  ");
        
        graph.setBorder(graphBorder);
        ((JComponent)stats).setBorder(statsBorder);
        
        setBorder(new EmptyBorder(10, 10, 10, 10));
        add(graph);
        add((JComponent)stats);
    }    
    
    private void decorateStats(JComponent comp) {
        decorateStats(comp, "  Statistics  ");
    }
    
    private void decorateStats(JComponent comp, String title) {

        EtchedBorder statsLines  = new EtchedBorder();
        TitledBorder statsBorder = new TitledBorder(statsLines, title);
        
        comp.setBorder(statsBorder);        
    }
}
