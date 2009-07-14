//
// $Id: FMap.java,v 1.1 2002/11/26 00:07:01 mikechen Exp $
//


//package org.jboss.RR;
package tracing;

//import org.jboss.system.ServiceMBeanSupport;
//import org.jboss.system.ServiceMBean;


import com.ibm.graph.Net;
import com.ibm.graph.Drawable;
import com.ibm.graph.Edge;
import com.ibm.graph.Graph;
import com.ibm.graph.NotDrawableException;
import com.ibm.graph.Vertex;
import com.ibm.graph.VertexMissingException;
import com.ibm.graph.draw.*;
import com.ibm.graph.layout.*;
import com.ibm.graph.awt.GraphCanvas;
import com.ibm.research.util.Dict;
import com.ibm.graph.draw.Draw3VertexRectangleKeyText;
import com.ibm.graph.draw.Draw2EdgeFromToRelativePolylines;
import com.ibm.graph.layout.LayoutDirectedAcyclicGraphAsTree;
import com.ibm.graph.*;
import com.ibm.graph.layout.LayoutGraphAsTree;
import com.ibm.graph.GraphLayout;
import com.ibm.graph.DirectedAcyclicGraph;
import com.ibm.graph.GraphLayoutManager;
import com.ibm.graph.awt.event.GraphCanvasEvent;
import com.ibm.graph.awt.event.GraphCanvasEventListener;


import com.ibm.research.util.Set;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.MouseEvent;



import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;
import java.lang.*;

import org.apache.crimson.tree.*;

public class FMap
{

   protected Document doc;
   protected NodeList nodes;
   protected FNet map;
   protected DirectedAcyclicGraph dag;
   protected Frame frame;
   protected ScrollPane scrollPane;
    LayoutGraphAsTree ldagat ;

   public FMap(){
      map = new FNet();
   }

    public static void main(String[] args) {
	try 
      {
	  FMap fmap = new FMap();
         fmap.loadFMap(new File(args[0]));
	 fmap.draw();
      }
      catch(IOException e)
      {
	  e.printStackTrace();
      }
    }


   public void populateGraph(){
      for(int i=0; i<nodes.getLength(); i++){

         Node node = nodes.item(i);
         if(node.getNodeType()==Node.ELEMENT_NODE){
            String nodeName = ((Element)node).getTagName();
            if(nodeName.equals("ejb")){
	       String ejbName = ((Element)node).getAttribute("name");
	       addEjb(ejbName);

               NodeList references = ((Element)node).getElementsByTagName("ejb-ref");
               for(int j=0; j<references.getLength(); j++){
                  Node ejbRefNode = references.item(j);
		  String ejbRefName = ((Element)ejbRefNode).getAttribute("name");
		  addEdge(ejbName, ejbRefName);
		}
	     }
	 }
       }
   }

   public void saveFMapToXML(File file){
	
      saveXML(file);

   }

   public void saveArchToXML(File file){
      saveXML(file);

   }

   public void saveXML(File file){
      try {
         Writer out = new OutputStreamWriter (new FileOutputStream(file));
         Document doc = createXML();

         ((XmlDocument)doc).write(out, "UTF-8");
	 out.flush();
         out.close();
      }
      catch (Exception e) {
         System.err.println("Save XML err:" + e);
      }

   }

   public Document createXML() {

      DocumentBuilderFactory dbf =
         DocumentBuilderFactory.newInstance();

      dbf.setValidating(false);
          
      DocumentBuilder db = null;
      try {
         db = dbf.newDocumentBuilder();
      } catch (ParserConfigurationException pce) {
         pce.printStackTrace();
      }
          
      Document doc  = db.newDocument();

      // Create the root node and add to the document
      ElementNode root = (ElementNode) doc.createElement("fmap");
      doc.appendChild(root);

      for( Enumeration vertices = map.enumerateVertices(); vertices.hasMoreElements(); ){
         MyVertex vertex = (MyVertex)vertices.nextElement();
         ElementNode node = createNode(doc, vertex);
         root.appendChild(node);
      }

      return(doc);
   }


   public ElementNode createNode(Document doc, MyVertex vertex){
      ElementNode vertexNode = (ElementNode) doc.createElement("ejb");
      vertexNode.setAttribute("name", vertex.getName());
      for( Enumeration refNodes = vertex.enumerateOutgoingDirectedNeighbors(); refNodes.hasMoreElements();){
         MyVertex tmp = (MyVertex)refNodes.nextElement();
         ElementNode tmpNode = (ElementNode) doc.createElement("ejb-ref");
         tmpNode.setAttribute("name", tmp.getName());
         vertexNode.appendChild(tmpNode);
      }
      return vertexNode;

   }


   public void addEjb(String ejbSName)
   {
      MyVertex ejbVertex = (MyVertex) map.findVertex(ejbSName.trim());

      if(ejbVertex == null)
      {
         ejbVertex = new MyVertex();
         ejbVertex.setName(ejbSName.trim());
         ejbVertex.systemdict.def( Vertex.strKeyName , ejbSName.trim() );
         map.add((Vertex)ejbVertex);
      }
   }
   
   public void addEjb(String ejbSName, Iterator ejbRefs, Iterator ejbLocalRefs){
	
      System.out.println("ADDING BEAN: " + ejbSName);

      MyVertex ejbVertex;
       
      ejbVertex = (MyVertex) map.findVertex(ejbSName);
      if(ejbVertex == null){
         ejbVertex = new MyVertex();
         ejbVertex.setName(ejbSName);
         ejbVertex.systemdict.def( Vertex.strKeyName , ejbSName );
         map.add((Vertex)ejbVertex);
      }
	
      Iterator references;
      if(ejbRefs.hasNext()) references = ejbRefs;
      else references = ejbLocalRefs;
                                 
      while(references.hasNext()){
	       
         MyVertex ejbRefVertex;
         String ejbRefSName = (String)references.next();
                        
         ejbRefVertex = (MyVertex) map.findVertex(ejbRefSName);
         if(ejbRefVertex == null){
            ejbRefVertex = new MyVertex();
            ejbRefVertex.setName(ejbRefSName);
            ejbRefVertex.systemdict.def( Vertex.strKeyName , ejbRefSName );
            map.add((Vertex)ejbRefVertex);
         }
              
         if(map.findEdge(ejbSName, ejbRefSName) == null){
            Edge refEdge  = new Edge( ejbVertex , ejbRefVertex , true  );
            map.add( refEdge);
         }
      }
   }

   public void removeEjb(String beanName){

      MyVertex ejbVertex;
                 
      ejbVertex = (MyVertex) map.findVertex(beanName);
      if(ejbVertex != null)
         map.remove(ejbVertex);
	


   }


   public void removeNode(String name){


   }


   public void addNode(String name){



   }

   public void addEdge(String fromName, String toName){
          MyVertex ejbRefVertex;
     
          ejbRefVertex = (MyVertex) map.findVertex(fromName.trim());
          if(ejbRefVertex == null){
             ejbRefVertex = new MyVertex();
             ejbRefVertex.setName(fromName.trim());
             ejbRefVertex.systemdict.def( Vertex.strKeyName , fromName.trim() );
             map.add((Vertex)ejbRefVertex);
          }
 
          MyVertex toVertex = (MyVertex) map.findVertex(toName.trim());   
          if(toVertex == null){
             toVertex = new MyVertex();
             toVertex.setName(toName.trim());   
             toVertex.systemdict.def( Vertex.strKeyName , toName.trim() );
             map.add((Vertex)toVertex);
          }
 
          
          if(map.findEdge(fromName.trim(), toName.trim()) == null){
             Edge refEdge  = new Edge( ejbRefVertex , toVertex , true  );
             map.add( refEdge);
          }

   }


   public void removeEdge(String fromName, String toName){



   }



   public void print(){
      map.print(System.out, 1);
	
   }


   public void draw(){

      Draw3VertexRectangleKeyText  vd
         = new Draw3VertexRectangleKeyText( Vertex.strKeyName , "Helvetica" , Font.PLAIN , 14 );
      
      // Drawing properties:
        
      vd.setAlignmentX( DrawVertex.ALIGNMENT_CENTER );
      vd.setAlignmentY( DrawVertex.ALIGNMENT_CENTER );
      vd.setMargins( 10 , 10 , 5 , 5 );
      vd.setColorFill( Color.yellow );

      dag = new DirectedAcyclicGraph();

      MyVertex vertex;

      for ( Enumeration vertices = map.enumerateVertices() ; vertices.hasMoreElements() ; )
      {
         vertex = (MyVertex)vertices.nextElement();
         vertex.setDrawable( vd );  // (It could also be done in the previous vertices loop above.)
         dag.add(vertex);
      }
    

      //    Drawable ed = new DrawEdgeDirectedArrow();
    
      Edge        edge;
      
      for ( Enumeration edges = map.enumerateEdges() ; edges.hasMoreElements() ; )
      {
         edge = (Edge)edges.nextElement();
         edge.setDrawable( new DrawEdgeDirectedArrow() ); 
         dag.add(edge);
      }
    

      // =========================================================================
      // Layout section
      // =========================================================================
    

      ldagat = new LayoutGraphAsTree();

      ldagat.setX0( 50 );
      ldagat.setY0( 50 );
      dag.setGraphLayoutManager( ldagat );
      
      dag.layout();
      //ldagat.layout(map);

      // ====================
      // APPLICATION SECTION:
      // ====================


      // Create and place the graph canvas
     
      frame = new Frame();
      scrollPane = new ScrollPane(); 

      GraphCanvas grfc = new GraphCanvas( dag );
      scrollPane.add(grfc);

      grfc.setLocation(20,140);
      grfc.setSize(500,600);


      
      // Listeners
      Dragging dragging = new Dragging();
      
      grfc.addGraphCanvasEventMouseReleasedListener
        (new FMapGraphCanvasEventListener(grfc));
      
      grfc.addGraphCanvasEventMouseDraggedListener
        (new FMapGraphCanvasEventMouseDraggedListener( dragging ));

      grfc.addGraphCanvasEventMouseReleasedListener
        (new FMapGraphCanvasEventMouseReleasedListener( dragging ));
      


    
      frame.setLayout(new BorderLayout());
      frame.add("Center",scrollPane);
      frame.setLocation(10,10);
      frame.setSize( new Dimension( 600 , 500 ) );
    
      frame.setVisible(true);
      
        frame.addWindowListener
        (
        new WindowAdapter()
        {
        public void windowClosing    ( WindowEvent wevent )
        {
        frame.setVisible(false);
        //    System.exit(0);
        }
        }
        ); 
      
   }


   public void loadFMap ( File file ) throws IOException
   {
      try{
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setValidating(false);
         DocumentBuilder db = null;
         try{
            db = dbf.newDocumentBuilder();
         }catch(ParserConfigurationException pce){
            pce.printStackTrace();
         }
        
         doc = db.parse(file);
         Element root = doc.getDocumentElement();
         nodes = root.getElementsByTagName("ejb");
	 populateGraph();

      }catch(SAXException e){
         System.err.println("XML parse error:" + e.getMessage());
      }
	/*
	catch(IOException e){
         e.printStackTrace();
         System.err.println("IO err:" + e.getMessage());
         }
	*/
      



   }



 /* WAIT ON THESE METHODS
    public void increaseRobustness(String ejbName){
 
       MyVertex ejbVertex = (MyVertex) map.findVertex(ejbSName);
       if(ejbVertex != null){
               Color currentColor = ejbVertex.getDrawable().getColorFill();
 
       }
    }
 
 
    public void decreaseRobustness(String ejbName){
 
         MyVertex ejbVertex = (MyVertex) map.findVertex(ejbSName);
         if(ejbVertex != null){
                 Color currentColor = ejbVertex.getDrawable().getColorFill();
                       System.out.println("Color: " + currentColor.getName());
         }
 
 
    }
*/ 




}


class MyVertex extends Vertex
{
   public Set getAllDirectedEdges(){
      return allDirectedEdges();
   }

   public Set getDirectedIncomingEdges()
   {
      return allDirectedEdges().difference( allDirectedOutgoingEdges() );
   }
}

     
class FMapGraphCanvasEventListener implements GraphCanvasEventListener
{

 public FMapGraphCanvasEventListener( GraphCanvas graphCanvas){ }

 public void graphobjectSelected( GraphCanvasEvent gcevent )
  {
    if  ( gcevent.getGraphObject() instanceof Vertex )
    {
           
      Vertex vertex = (Vertex)gcevent.getGraphObject();
System.out.println("VERTEX: " + vertex.getName());
      MouseEvent mevent = (MouseEvent)gcevent.getAWTEvent();
         
      Point mlocation = mevent.getPoint();
      if(mevent.getButton() == MouseEvent.BUTTON3)
        System.out.println("RIGHT BUTTON");
      if(mevent.getButton() == MouseEvent.BUTTON1)
        System.out.println("LEFT BUTTON");
    }
  }
}
      
class FMapGraphCanvasEventMouseDraggedListener implements 
GraphCanvasEventListener
{
  public FMapGraphCanvasEventMouseDraggedListener( Dragging dragging )
  {
    _dragging = dragging;
  }
     
  public void graphobjectSelected( GraphCanvasEvent gcevent )
  {
    if  ( gcevent.getGraphObject() instanceof Vertex )
    {
      Vertex vertex = (Vertex)gcevent.getGraphObject();
 
      MouseEvent mevent = (MouseEvent)gcevent.getAWTEvent();
   
      Point mlocation = mevent.getPoint();
   
      if ( ! _dragging.zDragging )
      {
        _dragging.zDragging = true;
        try
        {
          _dragging.iXDelta = - mlocation.x + vertex.getLocation().x;
          _dragging.iYDelta = - mlocation.y + vertex.getLocation().y;
        }
        catch ( NotDrawableException ndexception )
        {
          /* Should not happen */
        }
      }
      mlocation.translate( _dragging.iXDelta , _dragging.iYDelta );
 
      //;;System.out.println( gcevent );
      Component component = mevent.getComponent();
      Graphics graphics = component.getGraphics();
      graphics.setXORMode( component.getBackground() );
   
      vertex.draw( graphics );
      vertex.setLocation( mlocation );
      vertex.draw( graphics );
    }
  }
  private Dragging _dragging;
}
      
class FMapGraphCanvasEventMouseReleasedListener implements 
GraphCanvasEventListener
{
  public FMapGraphCanvasEventMouseReleasedListener( Dragging dragging )
  {
    _dragging = dragging;
  }
       
  public void graphobjectSelected( GraphCanvasEvent gcevent )
  {
    if ( _dragging.zDragging )
    {
      MouseEvent mevent = (MouseEvent)gcevent.getAWTEvent();
      //;;System.out.println( mevent.getPoint() );
      Component component = mevent.getComponent();
      component.repaint();
      _dragging.zDragging = false;
    }
       
  }
  private Dragging _dragging;
}
      
      
class Dragging
{  
  public boolean zDragging = false;
  public int iXDelta = 0;
  public int iYDelta = 0;
}
   

