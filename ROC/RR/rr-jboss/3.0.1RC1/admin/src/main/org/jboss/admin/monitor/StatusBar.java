package org.jboss.admin.monitor;

// standard imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.SoftBevelBorder;

/**
 * ...
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */     
public class StatusBar extends JPanel {
  
    private JLabel alert = new JLabel();
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */     
 
    public StatusBar() {
        init();
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     
 
    /**
     * <p>
     * This method is thread safe, although most Swing methods
     * are not.
     */
    public void setAlert(final String msg) {
    
        if (SwingUtilities.isEventDispatchThread())
            unsafeSetMessage(msg, Color.red);
        else 
            safe(new Runnable() {
                public void run() {
                    unsafeSetMessage(msg, Color.red);
                }
            });
    }
    
    /**
     * <p>
     * This method is thread safe, although most Swing methods
     * are not.
     */
    public void setMessage(final String msg) {
        
        if (SwingUtilities.isEventDispatchThread())
            unsafeSetMessage(msg, Color.black);
        else
            safe(new Runnable() {
                public void run() {
                    unsafeSetMessage(msg, Color.black);
                }
            });
    }
    
    /**
     * <p>
     * This method is thread safe, although most Swing methods
     * are not.
     */
    public void clear() {
        
        if (SwingUtilities.isEventDispatchThread())
            unsafeClear();
        else
            safe(new Runnable() {
                public void run() {
                    unsafeClear();
                }
            });
    }

/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */     

    private void safe(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }

    private void unsafeSetMessage(String msg, Color color) {

        if ( (msg == null) || (msg.equals("")) ) {
            unsafeClear();
            return;
        }
    
        alert.setText(msg);
        alert.setForeground(color);
        alert.repaint();
    }


    private void unsafeClear() {                    
        alert.setText("");
        alert.repaint();
    }
    
    private void init() {
    
        setLayout(new GridBagLayout());
        
        BevelBorder bevel    = new SoftBevelBorder(BevelBorder.LOWERED);
        EmptyBorder insets   = new EmptyBorder(0, 5, 0, 5);
        CompoundBorder combo = new CompoundBorder(bevel, insets);
        
        alert.setBorder(combo);
        alert.setMinimumSize(new Dimension(150, 20));
        alert.setPreferredSize(new Dimension(300, 20));
        alert.setMaximumSize(new Dimension(500, 20));
        alert.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // alert layout constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth  = 30;
        constraints.weightx    = 300;
        constraints.anchor     = GridBagConstraints.WEST;
        
        add(alert, constraints);
        
        JLabel bar = new JLabel();
        bar.setBorder(bevel);
        bar.setMinimumSize(new Dimension(20, 20));
        bar.setPreferredSize(new Dimension(20, 20));
        bar.setMaximumSize(new Dimension(20, 20));
        
        // constraints
        constraints = new GridBagConstraints();
        constraints.gridwidth   = 3;
        constraints.weightx     = 3;
        constraints.anchor      = GridBagConstraints.EAST;
        constraints.gridx       = GridBagConstraints.RELATIVE;
        constraints.gridy       = 0;
        
        add(bar, constraints);

        JLabel foo = new JLabel();
        foo.setBorder(bevel);
        foo.setMinimumSize(new Dimension(20, 20));
        foo.setPreferredSize(new Dimension(20, 20));
        foo.setMaximumSize(new Dimension(20, 20));
        
        // constraints
        constraints = new GridBagConstraints();
        constraints.gridwidth   = 3;
        constraints.weightx     = 3;
        constraints.anchor      = GridBagConstraints.EAST;
        constraints.gridx       = GridBagConstraints.RELATIVE;
        constraints.gridy       = 0;
        
        add(foo, constraints);
    }
}

