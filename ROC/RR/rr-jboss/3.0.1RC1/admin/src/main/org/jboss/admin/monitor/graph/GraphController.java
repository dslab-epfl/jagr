/*
 * Class GraphController
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
 * $Id: GraphController.java,v 1.1.1.1 2002/10/03 21:06:52 candea Exp $
 */     
package org.jboss.admin.monitor.graph;

// standard imports
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;

import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.DefaultMetalTheme;
 
/**
 * ...
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @version $Revision: 1.1.1.1 $
 */     
public class GraphController extends JPanel {

    /** Component orientation. */
    public final static int HORIZONTAL = 1;
    /** Component orientation. */
    public final static int VERTICAL   = 2;
    
    private int orientation = HORIZONTAL;
    
    private double vertMin  = 0.0;
    private double vertMax  = 100.0;
    private int vertGridGap = 10;
    private int horzGridGap = 10;
    
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */     
 
    /*
     * default constructor. default orientation = horiz.
     */
    public GraphController() {    }
    
    public GraphController(int orientation) {
        this();
        
        if (orientation == VERTICAL)
            this.orientation = VERTICAL;
        else 
            this.orientation = HORIZONTAL;
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     
 
    public void setVerticalMinValue(double d) {
        this.vertMin = d; 
    }
    
    public void setVerticalMaxValue(double d) {
        this.vertMax = d;
    }
    
    public void setVerticalGridGap(int gap) {
        this.vertGridGap = gap;    
    }
    
    public void setHorizontalGridGap(int gap) {
        
    }
 
/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */     

    public Dimension getMinimumSize() {
        return new Dimension(40, 20);
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(40, 20);
    }
    
    public void paintComponent(Graphics g) {
        
        // draw background first
        super.paintComponent(g);
        
        // get width, height and insets
        int width     = getWidth();
        int height    = getHeight();
        Insets insets = getInsets();
        
        // use work variables to take insets into account
        int w = width  - insets.left - insets.right;
        int h = height - insets.top  - insets.bottom;
        int x = insets.left;
        int y = insets.top;
        
        // check which area needs to be painted
        Rectangle clip = g.getClipBounds();
        
        // store the old clip state
        boolean wasNullClipArea = false;
        
        // no args repaint was called
        if (clip == null) {
            
            // leave insets out of the clip area
            g.setClip(x, y, width, height);
            
            wasNullClipArea = true;
        }
        
        // draw the grid line markers
        drawLineMarkers(g);

        // restore the old clip area    
        if (wasNullClipArea)
            g.setClip(null);
    }
    
    
/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */     
 
    private void drawLineMarkers(Graphics g) {
    
        if (orientation == VERTICAL)
            drawVerticalMarkers(g);
        else 
            drawHorizontalMarkers(g);            
    }
    
    private void drawVerticalMarkers(Graphics g) {

        Rectangle r = g.getClipBounds();
        MetalTheme theme = new DefaultMetalTheme();
        
        g.setColor(theme.getFocusColor());
        g.setFont(theme.getControlTextFont());
        
        int markerCount = 0;
        
        for (int y = r.height - 1; y >= 0; y -= vertGridGap, markerCount++) 
            g.drawLine(r.width - 5, y, r.width - 3, y);
            
        g.drawLine(r.width - 2, 0, r.width - 2, r.height);    
        
        double d        = 50.0 / vertGridGap;
        int labelSkip   = 1;
        
        if (d >= 1)
            labelSkip = (int)d;
            
        // [TODO] localize the format
        DecimalFormat format = new DecimalFormat("#.#");
        
        for (int i = 0; i < markerCount; ++i) {
            if (i % labelSkip == 0) {
                StringBuffer value = format.format((i*vertGridGap*vertMax)/r.height, new StringBuffer(), new FieldPosition(NumberFormat.FRACTION_FIELD));
                g.drawString(value.toString()/*String.valueOf((i*vertGridGap*vertMax)/r.height)*/,
                             0, r.height - 1 - i*vertGridGap + 5);
            }
        }
        
    }
    
    private void drawHorizontalMarkers(Graphics g) {
    
            
    }
}
 
 
