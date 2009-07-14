package org.jboss.admin.verifier;


// standard imports
import java.awt.Container;
import java.util.Locale;

import javax.swing.JFrame;


// non-standard class dependencies
import org.gjt.lindfors.util.LocalizationSupport;


/**
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors </a>
 */
public class VerifierFrame extends JFrame {

    /*
     * language package for this frame
     */
    private LocalizationSupport locale = 
            new LocalizationSupport("VerifierGUI", Locale.getDefault());


/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    /*
     * default bean constructor
     */
    public VerifierFrame() {

        initUI();
    }      

    /*
     * locale constr
     */
     public VerifierFrame(Locale locale) {
        
        if (locale != null)
            this.locale = new LocalizationSupport("VerifierGUI", locale);
            
        initUI();
     }
     
/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */
 
    /*
     * builds the gui components
     */
    private void initUI() {
    
        Container c = getContentPane();
        
        setJMenuBar(new VerifierMenu());
    }
}


