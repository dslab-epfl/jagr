package org.jboss.admin.monitor;

// standard imports
import java.util.Locale;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

// non-standard class dependencies
import org.jboss.admin.monitor.action.AboutAction;
import org.jboss.admin.monitor.action.SaveAsCSVAction;
import org.jboss.admin.monitor.action.GraphContinuousAction;
import org.jboss.admin.monitor.action.GraphPerInvocationAction;
import org.jboss.admin.monitor.action.LaunchJNDIBrowserAction;

import org.gjt.lindfors.util.LocalizationSupport;


/**
 * ...
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class MonitorFrameMenu extends    JMenuBar 
                              implements MonitorResourceConstants {

            // superclass JComponent implements Serializable
            
    // this menu's parent component
    // initialized in the constructor
    private MonitorFrame frame     = null;
    
    // language pkg
    // init. in the constructor
    private transient LocalizationSupport lang = null;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    public MonitorFrameMenu(MonitorFrame frame) {
        
        // preconditions
        if (frame == null)
            throw new IllegalArgumentException("null frame");
            
        // store parent frame ref.
        this.frame = frame;
        
        // set up localization
        lang = frame.getLanguagePackage();
        
        // create menus
        add(createFileMenu());
        add(createEditMenu());
        add(createViewMenu());
        add(createToolsMenu());
        add(createHelpMenu());
    }


/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */
 
    private JMenu createFileMenu() {
    
        JMenu file = new JMenu(lang.getLabel(FILE_MENU));
        
        file.add(SaveAsCSVAction.getInstance(frame.getLocale()));
        file.add(new JSeparator());
        file.add("Exit" /*ExitAction.getInstance()*/);
        
        return file;
    }
    
    private JMenu createEditMenu() {
        
        JMenu edit = new JMenu(lang.getLabel(EDIT_MENU));
        
        edit.add("Cut"  /*CutAction.getInstance()*/);
        edit.add("Copy" /*CopyAction.getInstance()*/);
        edit.add("Paste" /*PasteAction.getInstance()*/);
        
        return edit;
    }
    
    private JMenu createViewMenu() {
        
        JMenu view = new JMenu(lang.getLabel(VIEW_MENU));
        
        view.add(GraphPerInvocationAction.getInstance(frame.getLocale()));
        view.add(GraphContinuousAction.getInstance(frame.getLocale()));
        
        return view;
    }
    
    private JMenu createToolsMenu() {
        
        JMenu tools = new JMenu(lang.getLabel(TOOLS_MENU));
        
        tools.add("Launch EJX...");
        tools.add(LaunchJNDIBrowserAction.getInstance(frame.getLocale()));
        tools.add("Launch EJB Verifier...");
        
        return tools;
    }
    
    private JMenu createHelpMenu() {
     
        JMenu help = new JMenu(lang.getLabel(HELP_MENU));
        
        help.add(AboutAction.getInstance(frame.getLocale()));
        
        return help;
    }
    
}
