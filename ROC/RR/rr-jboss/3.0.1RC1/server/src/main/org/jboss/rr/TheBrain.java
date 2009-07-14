//
// $Id: TheBrain.java,v 1.23 2003/04/13 21:13:24 emrek Exp $
//

package org.jboss.RR;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.text.SimpleDateFormat;

// XML parsing classes
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;
import org.apache.crimson.tree.*;

// graph classes
import com.ibm.graph.*;
import com.ibm.graph.awt.*;
import com.ibm.graph.draw.*;
import com.ibm.graph.layout.*;
import com.ibm.research.util.Dict;
import com.ibm.research.util.KeyMissingException;

public class TheBrain 
{
    protected FNet fmap = null;
    protected TheBrainThread brainThread = null;
    protected int brainPort = 2374; // A-F-P-I on the telephone pad :-)
    
    // user/systemdict string defs  
    protected final static String numRebootKeyName = "numReboot";    
    protected final static String numPointFailKeyName = "numPointFail";
    protected final static String numEdgeFailKeyName = "numEdgeFail";

    // Used to send data out on port
    protected DatagramSocket restartAgentSocket;

    // PK Temporary hack variable for the 
    // restart agent port on the localhost
    protected int restartAgentPort = 1234;
    protected int delayProxyPort = 1313; // port to send pause/unpause messages to

    // for drawing the fmap
    protected Frame frame;

    // enabling/disabling sending message to restart agent
    protected boolean enableRestartMessage = true;

    // when false restarts only failed node, does not try to read
    // from fmap to see what other nodes need to be restarted
    protected boolean enableFMapRestart = true;

    // enable restarts without getting an end to end check (e.g., just
    //  after getting an associated failure from ExcMon)
    protected boolean enableEagerRestart = false;

    // list of restart lists that are waiting for the end to end check
    protected ArrayList queuedRestartLists = new ArrayList();
    protected ArrayList lastRestartEJBs = null; // list of EJBs restarted
                                                // because of last E2E error
                                                // (not necessarily in
                                                // dependence order)
    protected Date lastRestartTimeStamp = null;
    protected int identRBCount = 0; // number of consequtive reboots so far

    // after a failure, if another failure that affects a subset of the
    // components in the first failure presents itself within this many ms,
    // we ignore it 
    protected long noRestartBuffer_ms = 0;

    // options for running with Vanilla JBoss
    protected boolean vanilla = false;
    protected ArrayList appdirlist = null; // full paths
    protected int fullappRBthresh = 4;
    
    // how long to wait for undeployment, redeployment
    protected long waitUndeploy_ms = 7000;
    protected long waitRedeploy_ms = 15000;

    protected int waitVanilla_sec = 120; /* how long to wait to simulate human
                                          * intervention on a none self
                                          recovering setp */
 
    // default constructor
    public TheBrain()    
    {
	try
        {
	    restartAgentSocket = new DatagramSocket();
	}
        catch(SocketException e)
        {
            System.err.println("TheBrain: restartAgentSocket failed to bind to a UDP port!");            
	    e.printStackTrace();
	}
        fmap = new FNet();
    }

    // start listening to node failure reports
    public boolean StartBrainServices ()
    {
        try
        {
            brainThread = new TheBrainThread();
            brainThread.start();
            return true;
        }
        catch (SocketException e)
        {
            System.out.println("Could not bind to UDP port # " + brainPort);
            return false;
        }
    }

    // stop listening
    public void StopBrainServices ()
    {
        brainThread.stop = true;
        brainThread = null;
    }

    public void clearFMap()
    {
        fmap = new FNet(); // start a new FMap
    }

    // HACK Change ___EJB to The___
    // e.g. ClientControllerEJB becomes TheClientController
    private static String correctName(String node)
    {
        if(node.endsWith("EJB"))
        {
            System.out.println("HACK: Changing " + node + " to " +
                               ("The" + node.substring(0, node.length() - 3)));
            return ("The" + node.substring(0, node.length() - 3));
        }
        return node;
    }
    
    
    public void reportFailure (FailureReport report) 
    { 
        System.out.println("FAILURE REPORT RECEIVED, TIMESTAMP = " + 
                           (new SimpleDateFormat("HH:mm:ss,S")).format(report.timeStamp));

        if(report.failureType == FailureReport.failureTypePointNode) 
        {
            report.srcNode = correctName(report.srcNode);
            reportFailure(report.srcNode, report);
        }
        else if(report.failureType == FailureReport.failureTypeCorrelatedNodes) 
        {
            report.srcNode = correctName(report.srcNode);
            report.dstNode = correctName(report.dstNode);
            reportFailure(report.srcNode, report.dstNode, report);
	    if( enableEagerRestart ) {
		doRestart( report ); // this is to force failures on just ExcMon reported failures...
	    }
	}
        else if(report.failureType == FailureReport.failureTypeEndToEnd)
        {
            doRestart(report);
        }
        else {
            // invalid failure report (throw exception?)
        }
    }
    

    // This version of the overloaded function reportFailure routine is used to report a 
    // failure where one node caused another node to fail
    protected void reportFailure (String srcNode, String dstNode, FailureReport report) 
    {       
        System.out.println("THE BRAIN RECEIVED NOTIFICATION OF ASSOCIATED FAILURE: " + srcNode + " --> " + dstNode);        

        // Record an edge in the f-map from srcNode to dstNode
        Edge edge = fmap.add(srcNode, dstNode);
	if(edge == null)
        {
            System.err.println("ERROR: Failed to add edge from " + srcNode + 
                               " --> " + dstNode);
            return;
        }
        else
        {
            fmap.incUserCount(edge, numEdgeFailKeyName);
        }

        // compare to restart list queue
        Iterator iter = queuedRestartLists.iterator();
        while(iter.hasNext())
        {
            ArrayList tmpList = (ArrayList)(iter.next());
            if((tmpList.indexOf(report.srcNode) != -1) &&
               (tmpList.indexOf(report.dstNode) != -1))
            {
                System.out.println("This failure is already on the restart queue.");
                return;
            }
        }

        ArrayList restartList = new ArrayList();

        if(enableFMapRestart)
        {            
            // trigger restart of all nodes reachable from srcNode (includes at
            // least srcNode and dstNode)
            Enumeration e = fmap.enumerateBreadthFirstTraversal(srcNode);
        
            while(e.hasMoreElements()) 
            {
                Vertex v = (Vertex) e.nextElement();
                restartList.add(0, v.getName()); // add to start of list
                fmap.incUserCount(v, numRebootKeyName); // increment reboot count
            }
        }
        else
        {
            System.out.println("Note: Using Simple Restarts (No consultation of f-map)!");
            restartList.add(0, edge.getFromVertex().getName()); 
            restartList.add(0, edge.getToVertex().getName());
            fmap.incUserCount(edge.getFromVertex(), numRebootKeyName); // increment reboot counts
            fmap.incUserCount(edge.getToVertex(), numRebootKeyName);
        }
        
        queueRestartMessage(restartList, report);
    }    
    
    // This version of the overloaded function reportFailure routine is used to report a 
    // single point failure
    protected void reportFailure (String node, FailureReport report)
    {
        System.out.println("THE BRAIN RECEIVED NOTIFICATION OF POINT FAILURE: " + node);

        // Add a vertex to f-map for this node, if not already present
        Vertex va = fmap.add(node);
        if(va == null)
        {
            System.err.println("ERROR: Failed to add node " + node + " to fmap!");
            return;
        }
        else
        {
            fmap.incUserCount(va, numPointFailKeyName);
        }

        // compare to restart list queue
        Iterator iter = queuedRestartLists.iterator();
        while(iter.hasNext())
        {
            ArrayList tmpList = (ArrayList)(iter.next());
            if(tmpList.indexOf(report.srcNode) != -1)
            {
		// TODO: EMK: CHECK IF THIS IS AN ASSOC. FAILURE.
		//       if so, then a point failure + a assoc. failure mean
		//       we restart this single component.
                System.out.println("This failure is already on the restart queue.");
                return;
            }
        }
        
        ArrayList restartList = new ArrayList();

        if(enableFMapRestart)
        {
            // trigger restart of all nodes reachable from srcNode (includes at
            // least srcNode itself)
            Enumeration e = fmap.enumerateBreadthFirstTraversal(node);
            while(e.hasMoreElements())
            {
                Vertex v = (Vertex) e.nextElement();
                restartList.add(0, v.getName()); // add to start of list
                fmap.incUserCount(v, numRebootKeyName); // increment reboot count
            }
        }
        else
        {
            System.out.println("Note: Using Simple Restarts (No consultation of f-map)!");
            restartList.add(0, va.getName()); // add to list (and only element of list)
            fmap.incUserCount(va, numRebootKeyName); // increment reboot count
        }
        
        queueRestartMessage(restartList, report);
    }

    /**
     * PK
     * This method sends a list of EJB names using a UDP port to a listening RestartAgent
     */
    private void queueRestartMessage(ArrayList restartList, FailureReport report)
    {
        // update the last restarts
        if(!vanilla)
        {
            // no need to keep track for vanilla JBoss
            queuedRestartLists.add(restartList);
        }
    }

    private ArrayList mergeLists(ArrayList listOfLists)
    {
        ArrayList mergedList = new ArrayList();
        Iterator outer = listOfLists.iterator();
        while(outer.hasNext())
        {
            ArrayList tmpList = (ArrayList)(outer.next());
            Iterator iter = tmpList.iterator();
            while(iter.hasNext())
            {
                Object element = iter.next();
                if(mergedList.indexOf(element) == -1)
                {
                    mergedList.add(element);
                }
            }
        }
        
        return mergedList;
    }

    private void redeployFullApp(ArrayList pathlist) throws IOException, InterruptedException
    {
        Process p;
        String cmd;
        
        System.out.println("Undeploying application ...");
        for(int i = 0; i < pathlist.size(); i++)
        {                
            cmd = "/bin/mv " + pathlist.get(i) + " /tmp";
            //DEBUGMSG
            System.out.println("Undeploying file, cmd = \"" + cmd + "\"");
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor(); // wait for p to complete
            if(p.exitValue() != 0)
            {
                System.out.println("Undeploying file failed!, code = " + p.exitValue());
            }
        }
        
        Thread.sleep(waitUndeploy_ms); // wait for JBoss to undepoly app

        System.out.println("Redeploying application ...");
        for(int i = 0; i < pathlist.size(); i++)
        {
            String apppath = (String) pathlist.get(i);
            cmd = "/bin/mv /tmp/" + apppath.substring(apppath.lastIndexOf('/') + 1) +
                  " " + apppath;
            System.out.println("Redeploying file, cmd = \"" + cmd + "\"");
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor(); // wait for p to complete
            if(p.exitValue() != 0)
            {
                System.out.println("Redeploying file failed!, code = " + p.exitValue());
            }
        }
        
        Thread.sleep(waitRedeploy_ms); // wait for JBoss to redeploy app        
    }

    private void doRestart(FailureReport report)
    {        
        if(enableRestartMessage && vanilla)
        {
            if(lastRestartTimeStamp != null)
            {
                if(report.timeStamp.getTime() < lastRestartTimeStamp.getTime())
                {
                    System.out.println("End to end failure occured before latest redeploy, ignored!");
                    return;
                }
            }

            // vanilla jboss, will only get end to end errors

            // wait for waitVanilla seconds
            if(waitVanilla_sec > 0)
            {
                System.out.println("Simulating human lag time ... sleeping for " +
                                   waitVanilla_sec + " seconds!");
                Date startWait = new Date();
                while(System.currentTimeMillis() < (startWait.getTime() + waitVanilla_sec*1000))
                {
                    try
                    {
                        // sleep for one second at a time
                        Thread.sleep(1000);
                        System.out.print(".");
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            // done waiting

            sendProxyMessage(true); // pause

            try
            {
                redeployFullApp(appdirlist);
            }
            catch(IOException ioexp)
            {
                System.out.println(">>> IOException while redeploying application, redeployment may have failed!");
            }
            catch(InterruptedException intexp)
            {
                System.out.println(">>> InterruptedException while redeploying application, redeployment may have failed!");
            }
            
            lastRestartTimeStamp = new Date();
            
            sendProxyMessage(false); // unpause
            return;
        }        

        if(enableRestartMessage)
        {   
	    //
	    // Don't send RST messages if we're within the "no restart" buffer
	    //
	    if ( lastRestartTimeStamp!=null  &&
		 System.currentTimeMillis() <= lastRestartTimeStamp.getTime() + noRestartBuffer_ms )
	    {
		System.out.println("Within NO-RESTART buffer; skipping...");
		return;
	    }

            if(queuedRestartLists.isEmpty())
            {
                System.err.println("No exception failures detected before the End to End failure!");
                return;
            }
            
            identRBCount++;
            ArrayList mergedList = mergeLists(queuedRestartLists);
            if(lastRestartEJBs != null)
            {
                if(lastRestartEJBs.size() == mergedList.size())
                {
                    Iterator iter = mergedList.iterator();
                    while(iter.hasNext())
                    {
                        if(lastRestartEJBs.indexOf(iter.next()) == -1)
                        {
                            // lists don't match
                            identRBCount = 0;
                            break;
                        }
                    }
                }
                else
                {
                    identRBCount = 0;
                }
            }
            else
            {
                identRBCount = 0;
            }
            if(appdirlist == null)
            {
                identRBCount = 0;
            }

            if(identRBCount >= fullappRBthresh)
            {   // need to do recursive restart, time to restart the whole app
                lastRestartEJBs = null; // clear list

                System.out.println("\nDoing the next level recursive restart ... redeploying whole app!");

                sendProxyMessage(true); // pause
                try
                {                
                    redeployFullApp(appdirlist);
                }
                catch(IOException ioexp)
                {
                    System.out.println(">>> IOException while redeploying application, redeployment may have failed!");
                }
                catch(InterruptedException intexp)
                {
                    System.out.println(">>> InterruptedException while redeploying application, redeployment may have failed!");
                }
                
                // record this time
                lastRestartTimeStamp = new Date();
                
                sendProxyMessage(false); // unpause
            }
            else
            {
                try
                {
                    Iterator outer = queuedRestartLists.iterator();
                    while(outer.hasNext())
                    {
                        ArrayList tmpList = (ArrayList)(outer.next());
                        Iterator iter = tmpList.iterator();
                        while(iter.hasNext())
                        {
                            System.out.println("THE BRAIN IS TRIGGERING A RESTART OF THE NODE: " + (String)iter.next()); 
                        }

                        ByteArrayOutputStream bArray_out = new ByteArrayOutputStream();	
                        ObjectOutputStream obj_Out = new ObjectOutputStream(bArray_out);
                        obj_Out.writeObject(tmpList);
	    
                        DatagramPacket packet = new DatagramPacket(bArray_out.toByteArray(), 
                                                                   bArray_out.size(),
                                                                   InetAddress.getLocalHost(), 
                                                                   restartAgentPort);

                        restartAgentSocket.send(packet);
                    }
                    // record this time
                    lastRestartTimeStamp = new Date();
                }
                catch(IOException e)
                {
                    System.err.println("TheBrain: Error sending restart list to Restart Agent!");
                    e.printStackTrace();
                    sendProxyMessage(false); // so it doesn't end up stuck forever
                }

                lastRestartEJBs = mergedList;            
            } // end else (no app restart)
        }
        else
        {
            System.out.println("Sending of restart messageInteger.parseInt(cmd) == 6) is disabled!");
        }

        // clear the list
        queuedRestartLists.clear();
    }

    // following methods handle saving to an XML file
    public boolean writeXML(File file)
    {
        try 
        {
            Writer out = new OutputStreamWriter (new FileOutputStream(file));
            Document doc = createXML();
            ((XmlDocument)doc).write(out, "UTF-8");
            out.close();
        }
        catch (Exception e) 
        {
            System.err.println("Failed to save to XML file " + file.getName());
            System.err.println("Error Message: " + e.getMessage());
            return false;
        }
        return true; // success
    }

    protected Document createXML() 
    {
        DocumentBuilderFactory dbf =
            DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
          
        DocumentBuilder db = null;
        try 
        {
            db = dbf.newDocumentBuilder();
        } 
        catch (ParserConfigurationException pce) 
        {
            pce.printStackTrace();
        }
          
        Document doc  = db.newDocument();

        // Create the root node and add to the document
        ElementNode root = (ElementNode) doc.createElement("fmap");
        doc.appendChild(root);

        for (Enumeration vertices = fmap.enumerateVertices(); vertices.hasMoreElements();)
        {
            Vertex vertex = (Vertex)vertices.nextElement();
            ElementNode node = createNode(doc, vertex);
            root.appendChild(node);
        }

        return(doc);
    }

    protected ElementNode createNode(Document doc, Vertex vertex)
    {
        ElementNode vertexNode = (ElementNode) doc.createElement("ejb");
        vertexNode.setAttribute("name", vertex.getName());
        try
        {
            vertexNode.setAttribute(numRebootKeyName, "" + vertex.userdict.getInteger(numRebootKeyName));
        }
        catch (KeyMissingException e)
        {
            vertexNode.setAttribute(numRebootKeyName, "" + 0);
        }
        try
        {
            vertexNode.setAttribute(numPointFailKeyName, "" + vertex.userdict.getInteger(numPointFailKeyName));
        }
        catch (KeyMissingException e)
        {
            vertexNode.setAttribute(numPointFailKeyName, "" + 0);
        }
        
        for(Enumeration refNodes = vertex.enumerateOutgoingDirectedEdges(); refNodes.hasMoreElements();)
        {
            Edge tmp = (Edge)refNodes.nextElement();
            ElementNode tmpNode = (ElementNode) doc.createElement("ejb-ref");
            tmpNode.setAttribute("name", tmp.getToVertex().getName());
            try
            {
                tmpNode.setAttribute(numEdgeFailKeyName, "" + tmp.userdict.getInteger(numEdgeFailKeyName));
            }
            catch (KeyMissingException e)
            {
                tmpNode.setAttribute(numEdgeFailKeyName, "" + 0);
            }
            
            vertexNode.appendChild(tmpNode);
        }
        return vertexNode;
    }
    
// the following methods handle restoring from an XML file
    public void readXML(File file) throws IOException
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = null;
            try
            {
                db = dbf.newDocumentBuilder();
            }
            catch(ParserConfigurationException pce)
            {
                pce.printStackTrace();
            }
        
            Document doc = db.parse(file);
            Element root = doc.getDocumentElement();
            populateGraph(root.getElementsByTagName("ejb"));
        }
        catch(SAXException e)
        {
            System.err.println("XML parse error:" + e.getMessage());
        }
    }

    protected void populateGraph(NodeList nodes)
    {
        clearFMap(); // reset graph
        for(int i=0; i<nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if(node.getNodeType()==Node.ELEMENT_NODE)
            {
                String nodeName = ((Element)node).getTagName();
                if(nodeName.equals("ejb"))
                {
                    String ejbName = ((Element)node).getAttribute("name");
                    Vertex v = fmap.add(ejbName);
                    try
                    {
                        if(((Element)node).getAttribute(numRebootKeyName) != "")
                        {    
                            v.userdict.def(numRebootKeyName, 
                                           Integer.parseInt(((Element)node).getAttribute(numRebootKeyName)));
                        }
                        if(((Element)node).getAttribute(numPointFailKeyName) != "")
                        {
                            v.userdict.def(numPointFailKeyName,
                                           Integer.parseInt(((Element)node).getAttribute(numPointFailKeyName)));
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("Warning: invalid counter attribute(s) for vertex: " + ejbName);
                    }
                    
                    NodeList references = ((Element)node).getElementsByTagName("ejb-ref");
                    for(int j=0; j<references.getLength(); j++)http://www.cnn.com/2003/TECH/internet/04/11/offbeat.minister.site.reut/index.html
                    {
                        Node ejbRefNode = references.item(j);
                        String ejbRefName = ((Element)ejbRefNode).getAttribute("name");
                        Edge edge = fmap.add(ejbName, ejbRefName);
                        try
                        {
                            if(((Element)ejbRefNode).getAttribute(numEdgeFailKeyName) != "")
                            {                                
                                edge.userdict.def(numEdgeFailKeyName,
                                                  Integer.parseInt(((Element)ejbRefNode).getAttribute(numEdgeFailKeyName)));
                            }   
                        }
                        catch (NumberFormatException e)
                        {
                            System.err.println("Warning: invalid counter attribute(s) for edge: " 
                                               + edge.getName());
                        }
                    }
                }
            }
        }
    }
    

// method to draw a graphical representation of our fmap
    public void draw()
    {

        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        Vertex vertex;

        for ( Enumeration vertices = fmap.enumerateVertices() ; vertices.hasMoreElements() ; )
        {
            vertex = (Vertex)vertices.nextElement();

            String numRebootStr;
            String numPointFailStr;
            try
            {
                numRebootStr = (new Integer(vertex.userdict.getInteger(numRebootKeyName))).toString();
            }
            catch (KeyMissingException e)
            {
                numRebootStr = "0";
            }
            try
            {
                numPointFailStr = (new Integer(vertex.userdict.getInteger(numPointFailKeyName))).toString();
            }
            catch (KeyMissingException e)
            {
                numPointFailStr = "0";
            }

            Draw3VertexRectangleText  vd
                = new Draw3VertexRectangleText(vertex.getName() + "(RB: " 
                                               + numRebootStr + " PF: " + 
                                               numPointFailStr + ")", 
                                               "Helvetica" , Font.PLAIN , 14 );
            // Drawing properties:
        
            vd.setAlignmentX( DrawVertex.ALIGNMENT_CENTER );
            vd.setAlignmentY( DrawVertex.ALIGNMENT_CENTER );
            vd.setMargins( 10 , 10 , 5 , 5 );
            vd.setColorFill( Color.yellow );

            vertex.setDrawable( vd );
            dag.add(vertex);
        }

        Edge edge;
      
        for(Enumeration edges = fmap.enumerateEdges();edges.hasMoreElements();)
        {
            DrawEdgeArrowThenText de = new DrawEdgeArrowThenText();
            edge = (Edge)edges.nextElement();
            try
            {
                de.setText("(" + edge.userdict.getInteger(numEdgeFailKeyName) + ")");
            }
            catch (KeyMissingException e)
            {
                de.setText("(0)");
            }
            
            edge.setDrawable(de); 
            dag.add(edge);
        }
    

        // =========================================================================
        // Layout section
        // =========================================================================

        LayoutGraphAsTree ldagat = new LayoutGraphAsTree();
        ldagat.setX0( 50 );
        ldagat.setY0( 50 );
        dag.setGraphLayoutManager( ldagat );
        dag.layout();

        // ====================
        // APPLICATION SECTION:
        // ====================


        // Create and place the graph canvas
        frame = new Frame();
        ScrollPane scrollPane = new ScrollPane(); 

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
        frame.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing    ( WindowEvent wevent )
                {
                    frame.setVisible(false);
                }
            });
    }
    
    private class TheBrainThread extends Thread 
    {
	DatagramSocket socket = null;
        boolean stop = false;
        
        public TheBrainThread() throws SocketException
        {
	    socket = new DatagramSocket(brainPort);
            try
            {
                String addr = InetAddress.getLocalHost().toString();
                System.out.println("THE BRAIN IS LISTENING ON UDP PORT #" + brainPort 
                                   + " of " + addr);
            }
            catch(UnknownHostException e)
            {
                System.out.println("THE BRAIN IS LISTENING ON UDP PORT #" + brainPort);		      	
            }      
	}
    
	private FailureReport getFailureReport() throws IOException, SocketException, ClassNotFoundException
        {
	    byte[] buf = new byte[1024];
	    DatagramPacket packet = new DatagramPacket(buf, buf.length);
	    socket.receive(packet);
	    ByteArrayInputStream bArray_in = new ByteArrayInputStream(buf);
	    ObjectInputStream obj_in = new ObjectInputStream(bArray_in);
	    FailureReport report = (FailureReport) obj_in.readObject();    
	    return report;
	}

        public void run() {
            // listen for failure messages
            while(stop == false) {
                // receive a packet
                FailureReport report;
                
                try 
                {
                    report = getFailureReport();
                    //System.out.println("THE BRAIN: DEBUG: Received Packet ... " + report.toString());
		}
                catch (Exception e)
                {
                    System.err.println("THE BRAIN: ERROR RECEIVING PACKET!");
                    continue;
		}

                reportFailure(report);
            }
        }
    }

    protected void sendProxyMessage(boolean pause)
    {
        try
        {
            byte[] buf = new byte[1];
            if(pause)
            {
                buf[0] = 'P';
            }
            else
            {
                buf[0] = 'U';
            }
            
            DatagramSocket s = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, 
                                                       InetAddress.getLocalHost(),
                                                       delayProxyPort);
            s.send(packet);
            if(pause)
                System.out.println("THE BRAIN: PAUSE MESSAGE SENT TO DELAYPROXY!");
            else
                System.out.println("THE BRAIN: UNPAUSE MESSAGE SENT TO DELAYPROXY!");
        }
        catch(Exception e)
        {
            if(pause)
                System.err.println("THE BRAIN: FAILED TO SEND PAUSE MESSAGE!");
            else
                System.err.println("THE BRAIN: FAILED TO SEND UNPAUSE MESSAGE!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        File savefile = null;
        File openfile = null;
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        TheBrain brain = new TheBrain();

        // process command line arguments
        int argindex;
        for(argindex = 0; argindex < args.length; argindex++)
        {
            if(args[argindex].equalsIgnoreCase("-h") ||
               args[argindex].equalsIgnoreCase("-help"))
            {   // print help message
                System.out.println("Usage: java org.jboss.RR.TheBrain [options] [app dirs ...]");
                System.out.println("[app dirs ...] points to application deployment files, needed for -vanilla option or recursive restarts!");
                System.out.println("Available Options: ");
                System.out.println(" -h, -help          Displays this message");
                System.out.println(" -o filename        Read f-map from filename at start");
                System.out.println(" -s filename        Saves f-map to filename at exit");
                System.out.println(" -bp port#          Has the brain listen on port# (default = "     
                                   + brain.brainPort + ") ");
                System.out.println(" -rp port#          Send restart message to port# (default = "
                                   + brain.restartAgentPort + ") ");
                System.out.println(" -dp port#          Send pause message to (delay proxy) port# (default = "
                                   + brain.delayProxyPort + ") ");                
                System.out.println(" -norstbuf ms       Send no restart bufffer to ms milliseconds (default = " 
                                   + brain.noRestartBuffer_ms + ") ");
                System.out.println(" -norestart         Starts with sending of restarted messages disabled");
                System.out.println(" -simplerestart     Restarts directly failed nodes only, does not consult fmap");
                System.out.println(" -vanilla           Run with unmodified jboss, needs path of app deploy file(s)");
                System.out.println(" -vanillawait secs  number of seconds to wait before doing restart, default = " + 
                                   brain.waitVanilla_sec + ")");
                System.out.println(" -redpwait ms       Redeployment wait time in milliseconds (default = "
                                   + brain.waitRedeploy_ms + ") ");
                System.out.println(" -undpwait ms       Undeployment wait time in milliseconds (default = "
                                   + brain.waitUndeploy_ms + ") ");
                System.out.println(" -rrthresh #        Number of conseq same EJB rbs before full app rb (default = "
                                   + brain.fullappRBthresh + ") ");
                return;
            }
            else if(args[argindex].equalsIgnoreCase("-o"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Filename required for -o option!");
                    return;
                }

                openfile = new File(args[argindex]);
            }
            else if(args[argindex].equalsIgnoreCase("-s"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Filename required for -s option!");
                    return;
                }
                savefile = new File(args[argindex]);
            }
            else if(args[argindex].equalsIgnoreCase("-bp"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -bp option!");
                    return;
                }
                try
                {
                    brain.brainPort = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-rp"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -rp option!");
                    return;
                }
                try
                {
                    brain.restartAgentPort = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-dp"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -dp option!");
                    return;
                }
                try
                {
                    brain.delayProxyPort = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-norstbuf"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Milliseconds required for -norstbuf option!");
                    return;
                }
                try
                {
                    brain.noRestartBuffer_ms = Long.parseLong(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-norestart"))
            {
                brain.enableRestartMessage = false;
            }
            else if(args[argindex].equalsIgnoreCase("-simplerestart"))
            {
                brain.enableFMapRestart = false;
            }
            else if(args[argindex].equalsIgnoreCase("-vanilla"))
            {
                brain.vanilla = true;
            }
            else if(args[argindex].equalsIgnoreCase("-vanillawait"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Seconds required for -vanillawait option!");
                    return;
                }
                try
                {
                    brain.waitVanilla_sec = Integer.parseInt(args[argindex]);
                    if(brain.waitVanilla_sec < 0)
                    {
                        System.err.println("Wait seconds parameter must be >= 0");
                        return;
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-redpwait"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Milliseconds required for -redpwait option!");
                    return;
                }
                try
                {
                    brain.waitRedeploy_ms = Long.parseLong(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-undpwait"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Milliseconds required for -undpwait option!");
                    return;
                }
                try
                {
                    brain.waitUndeploy_ms = Long.parseLong(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-rrthresh"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Integer required for -rrthresh option!");
                    return;
                }
                try
                {
                    brain.fullappRBthresh = Integer.parseInt(args[argindex]);
                    if(brain.fullappRBthresh <= 0)
                    {
                        System.err.println("-rrthresh parameter must be > 0");
                        return;
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid integer!");
                    return;
                }
            }
            else
            {
                if(args[argindex].startsWith("-"))
                {   
                    System.err.println("Invalid option!  Use '-h' for help");
                    return;
                }
                else
                {
                    break;
                }
            }    
        }

        // process appdir paths, if any
        for(; argindex < args.length; argindex++)
        {
            if(brain.appdirlist == null)
            {
                brain.appdirlist = new ArrayList();
            }

            String path = args[argindex];
            if(path.endsWith("/"))
            {
                // chop off ending /, if necessary
                path = path.substring(0, path.length() - 1);
            }
            brain.appdirlist.add(path);
        }

        if(brain.vanilla && (brain.appdirlist == null))
        {
            System.err.println("-vanilla option requires application path(s) to be specified!");
            return;
        }

        brain.StartBrainServices();
        System.out.println("Welcome to The Brain!");
        System.out.println("---------------------");
        System.out.println("Restart Agent Port: " + brain.restartAgentPort);
        System.out.println("Delay Proxy Message Port: " + brain.delayProxyPort);

        if(brain.enableRestartMessage)
        {
            System.out.println("Sending of restart message is currently ENABLED!");
        }
        else
        {
            System.out.println("Sending of restart message is currently DISABLED!");
        }
        if(!(brain.enableFMapRestart))
        {
            System.out.println("Note: Using simple restart (not consulting f-map)!");
        }        

        if(brain.appdirlist != null)
        {
            if(brain.vanilla)
            {
                System.out.println("***Interacting with Vanilla JBoss!***");
            }
            else
            {
                System.out.println("***Using 2 level RECURSIVE restart logic!***");
            }

            String paths = null;
            for(int index = 0; index < brain.appdirlist.size(); index++)
            {
                if(paths == null)
                {
                    paths = (String)brain.appdirlist.get(index);
                }
                else
                {
                    paths = paths + "; " + brain.appdirlist.get(index);
                }
            }
            
            System.out.println("   Application File Path(s): " + paths);
            System.out.println("   Undeploy wait time (ms): " + brain.waitUndeploy_ms);
            System.out.println("   Redeploy wait time (ms): " + brain.waitRedeploy_ms);   
        }
        else
        {
            System.out.println("*** Not using recursive restart logic ***");
        }

        if(openfile != null)
        {
            try
            {
                brain.readXML(openfile);
                System.out.println("f-map restored from file " + openfile.getName());
            }
            catch (IOException e)
            {
                System.err.println("Warning: Could not read from file " + openfile.getName());
            }
        }
        
        while(true)
        {
            System.out.println("\n\nPlease select from the following options:");
            System.out.println("1) Print current f-map");
            System.out.println("2) Inject Test Point Failure");
            System.out.println("3) Inject Test Correlated Failure");
            System.out.println("4) Save f-map to XML file");
            System.out.println("5) Restore f-map from XML file");
            System.out.println("6) Clear f-map");
            System.out.println("7) Draw f-map");
            if(brain.enableRestartMessage)
            {
                System.out.println("8) Disable sending of restart messages");
            }
            else
            {
                System.out.println("8) Enable sending of restart messages");
            }
            if(brain.enableFMapRestart)
            {
                System.out.println("9) Use simple restarts (do not consult f-map)");
            }
            else
            {
                System.out.println("9) Use regular f-map restarts");
            }
            System.out.println("10) Change no restart buffer (Current = " +
                               brain.noRestartBuffer_ms + " milliseconds)");
            System.out.println("11) Send Pause Message!");
            System.out.println("12) Send Unpause Message!");
            System.out.println("13) Inject End To End Failure (trigger restart!)");
            System.out.println("14) Set Full App RB Threshold (Current = " +
                               brain.fullappRBthresh + ")");
            System.out.println("15) Vanilla wait to RB time (Current = " +
                               brain.waitVanilla_sec + " secs)");            
	    System.out.println("16) " + 
			       (brain.enableEagerRestart?"Disable":"Enable") + 
			       " Restarts (don't wait for e2e failure" );
            System.out.println("17) Exit");
            System.out.print("Command> ");
            try
            {
                String cmd = stdin.readLine();
                if(Integer.parseInt(cmd) == 1)
                {
                    brain.fmap.print(System.out, 4);
                }
                else if(Integer.parseInt(cmd) == 2)
                {
                    System.out.println("*** Injecting test point failure ***");
                    System.out.print("Enter failure node name: ");
                    String node = stdin.readLine();
                    brain.reportFailure(new FailureReport(node, new Date()));
                }
                else if(Integer.parseInt(cmd) == 3)
                {
                    System.out.println("*** Injecting test correlated failure ***");
                    System.out.print("Enter src failure node name: ");
                    String srcNode = stdin.readLine();
                    System.out.print("Enter dst failure node name: ");
                    String dstNode = stdin.readLine();
                    brain.reportFailure(new FailureReport(srcNode, dstNode, new Date()));
                }                
                else if(Integer.parseInt(cmd) == 4)
                {
                    System.out.println("*** Saving f-map to XML File ***");
                    System.out.print("Save as file name: ");
                    String saveFile = stdin.readLine();
                    if(brain.writeXML(new File(saveFile)))
                    {
                        System.out.println("f-map successfully written to " + saveFile);
                    }
                }
                else if(Integer.parseInt(cmd) == 5)
                {
                    System.out.println("*** Restoring f-map from XML File ***");
                    System.out.print("Open file name: ");
                    String openFile = stdin.readLine();
                    try
                    {
                        brain.readXML(new File(openFile));
                        System.out.println("f-map restored from file " + openFile);
                    }
                    catch (IOException e)
                    {
                        System.err.println("Could not read from file " + openFile);
                    }
                }
                else if(Integer.parseInt(cmd) == 6)
                {
                    brain.clearFMap();
                    System.out.println("*** Deleted all nodes from the f-map! ***");
                }                
                else if(Integer.parseInt(cmd) == 7)
                {
                    brain.draw();
                }
                else if(Integer.parseInt(cmd) == 8)
                {
                    brain.enableRestartMessage = !brain.enableRestartMessage;
                    if(brain.enableRestartMessage)
                    {
                        System.out.println("*** Sending of restart messages now ENABLED ***");
                    }
                    else
                    {
                        System.out.println("*** Sending of restart messages now DISABLED ***");
                    }   
                }
                else if(Integer.parseInt(cmd) == 9)
                {
                    brain.enableFMapRestart = !brain.enableFMapRestart;
                    if(brain.enableFMapRestart)
                    {
                        System.out.println("*** Consulting f-map for restarts now ENABLED ***");
                    }
                    else
                    {
                        System.out.println("*** Consulting f-map for restarts now DISABLED (simple restarts)***");
                    }   
                }
                else if(Integer.parseInt(cmd) == 10)
                {                    
                    System.out.print("Enter new no restart buffer (ms): ");
                    String num = stdin.readLine();
                    try
                    {
                        brain.noRestartBuffer_ms = Long.parseLong(num);    
                        System.out.println("*** No restart buffer set to " + num
                                           + " milliseconds!");
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("Error: Invalid number: " + num);
                    }
                }
                else if(Integer.parseInt(cmd) == 11)
                {
                    brain.sendProxyMessage(true);
                }
                else if(Integer.parseInt(cmd) == 12)
                {
                    brain.sendProxyMessage(false);
                }
                else if(Integer.parseInt(cmd) == 13)
                {   // trigger end to end failure
                    brain.reportFailure(new FailureReport(new Date()));
                }
                else if(Integer.parseInt(cmd) == 14)
                {                    
                    System.out.print("Enter full app RB threshold: ");
                    String num = stdin.readLine();
                    try
                    {
                        if(Integer.parseInt(num) <= 0) /* not allowed */
                        {
                            System.out.println("Error: Threshold must be > 0");
                        }
                        else
                        {
                            brain.fullappRBthresh = Integer.parseInt(num);
                            System.out.println("*** Full app RB threshold set to " + brain.fullappRBthresh
                                + " ***");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("Error: Invalid number: " + num);
                    }
                }
                else if(Integer.parseInt(cmd) == 15)
                {                    
                    System.out.print("Enter vanilla wait to RB time (seconds): ");
                    String num = stdin.readLine();
                    try
                    {
                        if(Integer.parseInt(num) < 0) /* not allowed */
                        {
                            System.out.println("Error: Wait time must be >= 0 secs");
                        }
                        else
                        {
                            brain.waitVanilla_sec = Integer.parseInt(num);
                            System.out.println("*** Vanilla wait to RB time set to " + brain.waitVanilla_sec
                                + " secs ***");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("Error: Invalid number: " + num);
                    }
                }
		else if(Integer.parseInt(cmd) == 16) 
		{
		    brain.enableEagerRestart = !brain.enableEagerRestart;
		}
                else if(Integer.parseInt(cmd) == 17)
                {
                    break; // quit
                }
                else
                {
                    System.out.println("Invalid option, please try again!");
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid option, please try again!");
            }
            catch (IOException e)
            {
                break; // just quit
            }   
        }
        System.out.println("\nGoodbye!");
        brain.StopBrainServices();

        if(savefile != null) // save fmap to file
        {
            if(brain.writeXML(savefile))
            {
                System.out.println("f-map successfully written to " + savefile.getName());
            }
        }

        System.exit(0);
    }
}


    


        

