package org.jboss.admin.monitor.action;


// standard imports
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;

// non-standard class dependencies
import org.jboss.admin.monitor.MonitorResourceConstants;
import org.gjt.lindfors.util.LocalizationSupport;


/**
 * ...
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class GraphContinuousAction extends    AbstractAction 
                                   implements MonitorResourceConstants {

            // superclass implements Serializable
            
            
    public final static String ACTION_COMMAND = "GraphContinuousAction";
        
    // singleton
    private static Action defaultAction = null;
    
    // language pkg
    // init. in the constructor
    private transient LocalizationSupport lang = null;
    
    // help support
    // init. in the constructor
    private transient LocalizationSupport help = null;
    
    
/*
 *************************************************************************
 *
 *      CLASS METHODS
 *
 *************************************************************************
 */
 
    public static synchronized Action getInstance(Locale locale) {
        if (defaultAction == null) {
            defaultAction = new GraphContinuousAction(locale);
        }
        return defaultAction;
    }

    public static synchronized Action getInstance() {
        return getInstance(null);
    }
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    private GraphContinuousAction(Locale locale) {
    
        if (locale == null)
            locale = Locale.getDefault();
            
        // get language pkg and helps
        lang = new LocalizationSupport(LANG_PKG, locale);
        help = new LocalizationSupport(HELP_PKG, locale);
        
        // set name and properties
        putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND);    
        putValue(Action.NAME, lang.getLabel(GRAPH_CONTINUOUS));
        putValue(Action.MNEMONIC_KEY, lang.getMnemonic(GRAPH_CONTINUOUS));
        putValue(Action.ACCELERATOR_KEY, lang.getShortcut(GRAPH_CONTINUOUS));
        putValue(Action.SHORT_DESCRIPTION, lang.getTooltip(GRAPH_CONTINUOUS));
        
        putValue(Action.LONG_DESCRIPTION,  help.getContextHelp(GRAPH_CONTINUOUS));
    }
    
/*
 *************************************************************************
 *
 *      IMPLEMENTS ACTION_LISTENER INTERFACE
 *
 *************************************************************************
 */
 
    public void actionPerformed(ActionEvent evt) {
    
    
    }
}

