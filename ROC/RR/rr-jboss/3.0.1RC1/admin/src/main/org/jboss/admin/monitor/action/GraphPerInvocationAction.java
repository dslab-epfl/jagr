package org.jboss.admin.monitor.action;


// standard imports
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
public class GraphPerInvocationAction extends    AbstractAction
                                      implements MonitorResourceConstants {

            // superclass implements Serializable
            
            
    public final static String ACTION_COMMAND = "GraphPerInvocationAction";
    
    
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
            defaultAction = new GraphPerInvocationAction(locale);
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
 
    private GraphPerInvocationAction(Locale locale) {
    
        if (locale == null)
            locale = Locale.getDefault();
            
        // get language pkg and helps
        lang = new LocalizationSupport(LANG_PKG, locale);
        help = new LocalizationSupport(HELP_PKG, locale);
        
        // set name and properties
        putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND);
        putValue(Action.NAME, lang.getLabel(GRAPH_PER_INVOCATION));
        putValue(Action.MNEMONIC_KEY, lang.getMnemonic(GRAPH_PER_INVOCATION));
        putValue(Action.ACCELERATOR_KEY, lang.getShortcut(GRAPH_PER_INVOCATION));
        putValue(Action.SHORT_DESCRIPTION, lang.getTooltip(GRAPH_PER_INVOCATION));
        putValue(Action.LONG_DESCRIPTION, help.getContextHelp(GRAPH_PER_INVOCATION));
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

