/*
 * Class ApplicationTreeCellRenderer
 * Copyright (C) 2001  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * This package and its source code is available at www.jboss.org
 * $Id: ApplicationTreeCellRenderer.java,v 1.1.1.1 2002/10/03 21:06:52 candea Exp $
 */     
package org.jboss.admin.monitor.tree;

// standard imports
import java.net.URL;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

// non-standard class dependencies
import org.jboss.admin.monitor.MonitorResourceConstants;


/**
 * ...
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 * @version $Revision: 1.1.1.1 $
 */     
public class ApplicationTreeCellRenderer extends    DefaultTreeCellRenderer
                                         implements MonitorResourceConstants {

    private transient ImageIcon ejbIcon         = null;
    private transient ImageIcon cacheIcon       = null;
    private transient ImageIcon applicationIcon = null;
    private transient ImageIcon methodIcon      = null;
    private transient ImageIcon serverIcon      = null;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */ 
    
    public ApplicationTreeCellRenderer() {
        
        URL url = getClass().getResource(BEAN_ICON_16);
        if (url != null)
            ejbIcon = new ImageIcon(url);
        
        url = getClass().getResource(CACHE_ICON_16);
        if (url != null)
            cacheIcon = new ImageIcon(url);
        
        url = getClass().getResource(JAR_ICON_16);
        if (url != null)
            applicationIcon = new ImageIcon(url);
        
        url = getClass().getResource(METHOD_ICON_16);
        if (url != null)
            methodIcon = new ImageIcon(url);
            
        url = getClass().getResource(SERVER_ICON_16);
        if (url != null)
            serverIcon = new ImageIcon(url);
    }
    
/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */     

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);    
        
        if (value instanceof BeanTreeNode)
            setIcon(ejbIcon);
        else if (value instanceof BeanCacheTreeNode)
            setIcon(cacheIcon);
        else if (value instanceof MethodTreeNode)
            setIcon(methodIcon);
        else if (value instanceof ApplicationTreeNode)
            setIcon(applicationIcon);
        else if (value instanceof RootNode)
            setIcon(serverIcon);
       
        return this;
    }
}
