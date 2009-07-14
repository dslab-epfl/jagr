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
public class SaveAsCSVAction extends    AbstractAction 
                             implements MonitorResourceConstants {

            // superclass implements Serializable
            
            
    public final static String ACTION_COMMAND = "SaveAsCSVAction";
    
    
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
            defaultAction = new SaveAsCSVAction(locale);
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
 
    private SaveAsCSVAction(Locale locale) {
    
        if (locale == null)
            locale = Locale.getDefault();
            
        // get language pkg and helps
        lang = new LocalizationSupport(LANG_PKG, locale);
        help = new LocalizationSupport(HELP_PKG, locale);
        
        // set name and properties
        putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND);        
        putValue(Action.NAME, lang.getLabel(SAVE_AS_CSV));
        putValue(Action.MNEMONIC_KEY, lang.getMnemonic(SAVE_AS_CSV));
        putValue(Action.ACCELERATOR_KEY, lang.getShortcut(SAVE_AS_CSV));
        putValue(Action.SHORT_DESCRIPTION, lang.getTooltip(SAVE_AS_CSV));
        putValue(Action.LONG_DESCRIPTION, help.getContextHelp(SAVE_AS_CSV));
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

