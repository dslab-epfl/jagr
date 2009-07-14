package org.jboss.admin.jndi;

/*
 * Class org.ejboss.admin.Browser
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
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import javax.naming.*;
   
// non-standard class dependencies


/**
 * Browser object contains a split view with a tree element representing the
 * EJBoss naming server contents on the left and a table for properties on the
 * right. 
 *
 * @author 	Juha Lindfors
 *
 * @version $Revision: 1.1.1.1 $
 * @since  	EJBoss 1.0DR2
 */
public class Browser extends JPanel {

       private JTable table         = null;
       private String[] columnNames = { "Name", "Value" };
       private JTree tree           = null;
       private DefaultMutableTreeNode top = null;
       
       
       /**
        * Reference to the initial context
        */
       private InitialContext initial  = null;
       
       
       
       /**
        * Constructs the naming tree view.
        *
        * @param    initial     JNDI initial context
        */
       public Browser(InitialContext initial) {
           this.initial = initial;
           
           setLayout(new BorderLayout());

           JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
           
           split.setLeftComponent(createNameContextTree());
           split.setRightComponent(createTargetAttributeTable());

           add("Center", split);
           split.setDividerLocation(250);
           
           iterateContext(initial, "/", null);
       }

       /**
        * Adds a node with a given name to the tree. If the <code>parent</code>
        * node is a <code>null</code> reference, the named node is added
        * directly under the top tree node. Otherwise the named node is added
        * as a child to its parent node.
        *
        * @param    parent      parent node
        * @param    name        name for the child node
        */
       public DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, String name) {
           DefaultMutableTreeNode reply = null;
           
           if (parent == null)    
                top.add(reply = new DefaultMutableTreeNode(name));
           else 
                parent.add(reply = new DefaultMutableTreeNode(name));
           
           return reply;
       }
       


      
       
       /**
        * This recursive method handles the tree traversal.
        *
        * @param    context     root context
        * @param    name        name of the context
        * @param    node        root node
        */
       private boolean iterateContext(Context context,
                                      String  name, 
                                      DefaultMutableTreeNode node) {            

            try {
                NamingEnumeration enum = context.list(name);
        
                while (enum.hasMore()) {
                    
                    NameClassPair pair = (NameClassPair)enum.next();

                    if (isContext(context, pair.getName())) {
                    
                        Context ctx = (Context)context.lookup(pair.getName());
                        String  str = ctx.composeName(pair.getName(), name);

                        iterateContext(ctx, str, addNode(node, pair.getName()));
                    }

                    else 
                        addNode(node, pair.getName());

                } 
            }
            catch (NamingException e) {
                System.err.println(e);
                return false;
            }
        
        return true;
    }
    
    private boolean isContext(Context ctx, String bindingName) 
                                           throws NamingException {
        try {
            ctx.list(bindingName);
        }
        catch(NotContextException e) {
            return false;
        }
        
        return true;
    }

    
    /**
     * Constructs the initial tree. This method will add a top node named
     * "Initial Context" to the tree.
     */
    private JComponent createNameContextTree() {
       
        top  = new DefaultMutableTreeNode(TOP_NODE_STR, true);
        
        tree = new JTree(top);
        tree.putClientProperty("JTree.lineStyle", "Angled");
       
        JScrollPane sp = new JScrollPane(tree);

        return sp;
    }

    /**
     * Creates the view table.
     */
    private JComponent createTargetAttributeTable() {
       
        Object[][] rows = {
            { null, null }
        };
       
        table = new JTable(rows, columnNames);
        JScrollPane sp = new JScrollPane(table);
 
        return sp;
    }

    /*
     * String constants
     */
    private final static String TOP_NODE_STR =
        "Initial Context";
        
}


