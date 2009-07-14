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
import org.jboss.admin.monitor.event.AggregatedInvocationEvent;

import org.hs.jfc.FormPanel;
import org.gjt.lindfors.util.LocalizationSupport;


/**
 * ...
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class InvocationTimeStatsView extends JPanel
        implements GraphModelListener, MonitorResourceConstants {

            // superclass implements Serializable
            
    /**
     * Support class for localization. Used for loading language resource bundles
     * and retrieving localized info.
     *
     * Initialized in the constructor.
     */
    private transient LocalizationSupport lang = null;
    
    private int totalInvocations    = 0;
    private int totalInvocationTime = 0;

    private JLabel averageTime         = new JLabel("0");
    private JLabel numberOfInvocations = new JLabel("0");
    
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
    public InvocationTimeStatsView() {
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
    
        double value = evt.getValue();

        // Ignore zero additions; there will be lots of these if the
        // continuous graph update is being used, no need to count them
        if (value <= 0.0)
            return;
            
        // invocation graph model may send aggregated invocation event
        // in case of heavy load, they require slightly different handling
        if (evt instanceof AggregatedInvocationEvent) {
            AggregatedInvocationEvent event = (AggregatedInvocationEvent)evt;
            
            totalInvocations += event.getInvocationCount();
            totalInvocationTime += (int)event.getSumValue();
        }
        else {
            ++totalInvocations;
            totalInvocationTime += (int)value;
        }
        
        // [TODO] format
        averageTime.setText(String.valueOf(totalInvocationTime / totalInvocations));
        averageTime.repaint();
        
        numberOfInvocations.setText(String.valueOf(totalInvocations));
        numberOfInvocations.repaint();
    }
    
    public void limitChanged(GraphModelEvent evt) {
        
        // [TODO] graph listener adapter
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
        
        JLabel average      = new JLabel(lang.getLabel(AVG_INVOCATION_TIME) + ":");
        JLabel total        = new JLabel(lang.getLabel(TOTAL_INVOCATION_COUNT) + ":");
        JLabel millisecs    = new JLabel("ms");
        
        p.add(average, averageTime,       1, 1);  // row 1, col 1
        p.add(millisecs,                  1, 2);
        p.add(total, numberOfInvocations, 2, 1);
        
        return p;
    }
        
}

