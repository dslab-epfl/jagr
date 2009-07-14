package org.jboss.admin.monitor;

// standard imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

// non-standard class dependencies
import org.jboss.admin.monitor.graph.DefaultGraphModel;
import org.jboss.admin.monitor.graph.DefaultGraphRenderer;
import org.jboss.admin.monitor.graph.GraphModel;
import org.jboss.admin.monitor.graph.GraphRenderer;



/**
 *
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class GraphView extends JPanel {

            // superclass JComponent implements Serializable
            
    private GraphRenderer renderer   = new DefaultGraphRenderer();
    private GraphModel model         = new DefaultGraphModel();
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    public GraphView() {
        super(new BorderLayout());
    
//        add(renderer.getGraphRendererObject(this), BorderLayout.CENTER);
//        add(renderer.getVerticalControlRendererObject(this), BorderLayout.WEST);
//        add(renderer.getHorizontalControlRendererObject(this), BorderLayout.SOUTH);
        setGraphRenderer(renderer);
        model.addGraphModelListener(renderer);
    }
    
    public GraphView(GraphModel graphModel) {
        super(new BorderLayout());

        setModel(graphModel);
    }
        
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    public void setGraphBorder(Border border) {
        ((JComponent)renderer).setBorder(border);
    }
    
    public Border getGraphBorder() {
        return ((JComponent)renderer).getBorder();
    }
    
    public void setGraphRenderer(GraphRenderer graphRenderer) {

        if (graphRenderer == null)
            return;
            
        //remove((Component)renderer);
        removeAll();
        
        this.renderer = graphRenderer;
        
        add(renderer.getGraphRendererObject(this), BorderLayout.CENTER);
        add(renderer.getVerticalControlRendererObject(this), BorderLayout.WEST);
        add(renderer.getHorizontalControlRendererObject(this), BorderLayout.SOUTH);
        
        revalidate();
    }
    
    public GraphRenderer getGraphRenderer() {
        return renderer;
    }
    
    public void setModel(GraphModel graphModel) {

        if (model == null)
            return;
        
        model.removeGraphModelListener(renderer);
  
        this.model = graphModel;
        model.addGraphModelListener(renderer);

        setGraphRenderer(renderer);      
    }
    
    public GraphModel getModel() {
        return model;
    }
    
    public double getVerticalMax() {
        return getModel().getVerticalMax();
    }
    
    public double getVerticalMin() {
        return getModel().getVerticalMin();
    }
    
    public double getHorizontalMax() {
        return getModel().getHorizontalMax();
    }
    
    public double getHorizontalMin() {
        return getModel().getHorizontalMin();    
    }
    
}
