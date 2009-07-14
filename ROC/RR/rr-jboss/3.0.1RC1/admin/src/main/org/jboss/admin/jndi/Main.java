package org.jboss.admin.jndi;

/*
 * Class org.ejboss.admin.Main;
 * Copyright (C) 2000  Juha Lindfors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

// standard imports
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;

import javax.naming.*;


/**
 * ...
 *
 *
 * @author 	Juha Lindfors
 * @version $Revision: 1.1.1.1 $
 * @since  	EJBoss 1.0DR2
 */
public class Main extends JFrame {

    /**
     * Initial Naming Context
     */
    private InitialContext initialContext = null;
    
    /**
     * Reference to the browser component
     */
    private Browser browser        = null;
    
    /**
     * Reference to the shutdown button
     */
    private JButton shutdownButton = new JButton(SHUTDOWN_STR);
    
    /**
     * Output stream for the socket connecting to EJBoss admin port.
     */
    private Writer socketWriter    = null;
    
    /**
     * Tool properties.
     */
     private String initialContextFactory = null,
                    providerURL           = null,
                    pkgPrefixes           = null,
                    adminHost             = null;
     
    /**
     * Starts the app.
     */
    public static void main(String[] args) {
        Main app = new Main();
    }
     
    /**
     * Constructs the browser tool. This constructor reads the admin tool
     * property file, opens a socket connection to the admin server and
     * initializes a connection to JNDI server.
     */
    public Main() {
        super("JNDI Browser");

        initJNDI();

        Container c = getContentPane();
        c.add("Center", browser = new Browser(initialContext));
        c.add("South",  createButtonPanel());
        

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                dispose();
                System.exit(0);
            }
        });
        
        setSize(600, 400);
        setVisible(true);
    }
    
    


   /**
    * Initialize the JNDI connection.
    */
    private void initJNDI() {
    
        try {
            Properties props = new Properties();
            initialContext = new InitialContext();
        }
        catch (NamingException e) { 
            System.out.println("Unable to initialize JNDI " + e.getMessage());
        }
    }
    
   

    /**
     * Creates the panel for buttons. Shutdown button is tied to <code>ShutdownAction</code>
     *
     * @return  component containing a shutdown button
     */
    private JComponent createButtonPanel() {
        JPanel p = new JPanel(new FlowLayout());
        
        return p;
    }


    /*
     * String constants
     */
    private final static String SHUTDOWN_STR =
        "Server Shutdown";
    private final static String ADMIN_HOST   =
        "adminserver.host";
    private final static String ADMIN_PORT   =
        "adminserver.port";

    private final static char   SHUTDOWN_MNEMONIC = 'S';        
}


