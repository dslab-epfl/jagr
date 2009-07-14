package org.jboss.admin.monitor.graph;

// standard imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.DefaultMetalTheme;

// non-standard class dependencies
import org.jboss.admin.monitor.GraphView;
import org.jboss.admin.monitor.event.GraphModelListener;
import org.jboss.admin.monitor.event.GraphModelEvent;


/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class DefaultGraphRenderer extends JPanel implements GraphRenderer {

        // superclass JComponent implements Serializable

    /** Horizontal "step" on the grid between plot points, in pixels */
    private int horzPlotStep    = 4;
    
    /** Gap between the vertical grid lines, in pixels */
    private int gridGapWidth    = 10;
    /** Gap between the horizontal grid lines, in pixels */
    private int gridGapHeight   = 10;
    
    /** The color used for drawing the background grid */
    private Color gridLineColor = new Color(0x007700);
    /** The color used for drawing the plot lines */
    private Color plotColor     = Color.green;
     
    /**
     * The current maximum value plotted on the grid. This value is the
     * non-scaled value of a plot point at the top of the grid.
     */
    private double max          = 1.0d;

    private List points    = new ArrayList();

    private boolean autoScale   = true;
       
    private GraphController controller = new GraphController(GraphController.VERTICAL);
        
    private GraphView parent = null;    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    public DefaultGraphRenderer() {
    
        // show default background color
        setOpaque(true);
        
        // set bevel border around the graph (not the controls though)
        setBorder(new BevelBorder(BevelBorder.LOWERED)); 
        
        // enable auto-scaling
        //setAutoScaleEnabled(true);
    }   
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */     
 
//    public void setAutoScaleEnabled(boolean autoScale) {
//        this.autoScale = autoScale;
//    }
//    
//    public boolean isAutoScaled() {
//        return autoScale;
//    }
    
/*
 *************************************************************************
 *
 *      IMPLEMENTS GRAPH_RENDERER INTERFACE
 *
 *************************************************************************
 */

    public Component getGraphRendererObject(GraphView graph) {

        try {            
            //Point maxPoint = (Point)Collections.max(points, new PointComparator());
            
            //if (isAutoScaled())
            //    if (!isWithinScale(maxPoint.getY()))
            //        rescale(maxPoint.getY());
            this.parent = graph;
            this.max    = graph.getModel().getVerticalMax();

            points = new ArrayList(graph.getModel().getPlotPoints());
            Collections.reverse(points);
        }
        catch (NoSuchElementException ignored) {
            // collection was empty, just ignore
        }
//        catch (ClassCastException e) {
//            System.err.println(e);
//            e.printStackTrace();
//        }
        
        return this;
    }
    
    public Component getHorizontalControlRendererObject(GraphView graph) {
     
        return new GraphController(GraphController.HORIZONTAL);
    }
    
    public Component getVerticalControlRendererObject(GraphView graph) {
        
        controller.setVerticalGridGap(gridGapWidth);
        controller.setVerticalMinValue(0);
        controller.setVerticalMaxValue(graph.getVerticalMax());
        
        return controller;
    }

    public void valueAppended(GraphModelEvent evt) {   

        double value = evt.getValue();
        points.add(0, new Point(points.size(), (int)value));

        //if (isAutoScaled())
        //    if (!isWithinScale(value))
        //        rescale(value);
            
        repaint();
    }
    
    public void limitChanged(GraphModelEvent evt) {
        
        // just checking this one limit for now
        if (evt.getTarget() == MAX_VERTICAL_LIMIT)
            max = evt.getValue();
            // [TODO] these should tweak the controller objects
            
    }
/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */
 
    
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
        
        // draw the graph background if we're opaque
        if (isOpaque())
            drawBackground(g);
            
        // draw the grid
        drawGrid(g);

        // draw plot points
        plot(g);
        
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
 
    /*
     * draws the background rectangle
     * should only be called if component is opaque
     * graphics clipped to protect insets
     * honors clip bounds
     */
    private void drawBackground(Graphics g) {
        
        MetalTheme theme = new DefaultMetalTheme();
        g.setColor(Color.black);
        
        Rectangle rect = g.getClipBounds();
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }
    
    /*
     * draws horizontal and vertical grid lines
     * graphics clipped to protect insets
     * draws thrugh the entire component
     */
    private void drawGrid(Graphics g) {
        g.setColor(gridLineColor);
        
        // vertical lines, left to right
        for (int x = gridGapWidth; x < getWidth(); x += gridGapWidth)
            g.drawLine(x, 0, x, getHeight());
            
        // horizontal lines. bottom to top (the origo is at the bottom left
        // corner whereas the graphics system's origo is at the top left corner)
        for (int y = getHeight() - gridGapHeight; y >= 0; y -= gridGapHeight)
            g.drawLine(0, y, getWidth(), y); 
    }
    
    /*
     * plots the points
     * graphics clipped to protect insets
     * draws through the entire component
     */
    private void plot(Graphics g) {

        // get width, height and insets
        int width     = getWidth();
        int height    = getHeight();
        
        // set drawing color
        g.setColor(plotColor);

        int x = width  - 1;
       
        Point point = (points.size() > 0) ? (Point)points.get(0)
                                          : new Point(0, 0);
                                          
        int y = height - (int)(point.y * height / parent.getVerticalMax());
        //int y = height - 1;
        
        Iterator it = points.iterator();
        
        while ((it.hasNext()) && (x >= 0)) {
            Point p = (Point)it.next();
            g.drawLine(x, y, (x -= horzPlotStep), (y = height - (int)(p.y * height / parent.getVerticalMax())));
            --x;
        }
    }
    
    
//    private boolean isWithinScale(Double value) {
//        return isWithinScale(value.doubleValue());
//    }
//    
//    private boolean isWithinScale(double value) {
//    
//        if (value < 0.9 * parent.getVerticalMax())
//            return true;
//            
//        return false;
//    }
//    
//    private void rescale(double value) {
//        
//        max     = 1.4 * value;
//        
//    }
    
/*
 *************************************************************************
 *
 *      INNER CLASSES
 *
 *************************************************************************
 */
 
    private class PointComparator implements Comparator, Serializable {
     
        /**
         * ...
         *
         * @exception ClassCastException - if the arguments' types prevent them
         *            from being compared by this Comparator.
         */
        public int compare(Object a, Object b) {
            Point pa = (Point)a;
            Point pb = (Point)b;

            if (pa.getY() == pb.getY())
                return 0;
                
            else if (pa.getY() < pb.getY())
                return -1;
                
            else return 1;
        }
    }
    
}

