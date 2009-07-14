//
// $Id: FNet.java,v 1.3 2003/03/14 07:40:05 steveyz Exp $
//

package org.jboss.RR;


import com.ibm.graph.Edge;
import com.ibm.graph.Net;
import com.ibm.graph.Vertex;
import com.ibm.graph.GraphObject;
import com.ibm.research.util.KeyMissingException;

import java.util.*;

class FNet extends Net{

    // hash table to store vertex name/vertex object mappings
    protected Hashtable vertices = new Hashtable();
    // hash table to store edge name/edge object mappings
    protected Hashtable edges = new Hashtable();

    public void incUserCount(GraphObject obj, String key)
    {
        incUserCount(obj, key, 1);
    }
    
    public void incUserCount(GraphObject obj, String key, int def)
    {
        try
        {
            obj.userdict.def(key, obj.userdict.getInteger(key) + 1);
        }
        catch (KeyMissingException e)
        {
            obj.userdict.def(key, def);
        }
    }
    

    // get edge hash key
    protected String edgeKey(Edge edge)
    {
        return (edgeKey(edge.getFromVertex().getName(),edge.getToVertex().getName()));
    }
    protected String edgeKey(String fromStr, String toStr)
    {
        return (fromStr + "-->" + toStr);
    }

    // add a vertex
    public boolean add(Vertex vertex)
    {
        // check for duplicates
        if(findVertex(vertex.getName()) != null)
        {
            return false;
        }
        vertices.put(vertex.getName(), vertex);
        return super.add(vertex);
    }    

    // add a vertex by name
    public Vertex add(String name)
    {
        Vertex vertex = findVertex(name);
        if(vertex == null)
        {
            vertex = new Vertex();
            vertex.setName(name);
            if(add(vertex) == false)
            {
                // failed to add vertex
                return null;
            }
        }
        return vertex; 
    }

    // remove a vertex (and all incident edges)
    public boolean remove(Vertex vertex){
        vertices.remove(vertex.getName());
        Enumeration vEdges = vertex.enumerateEdges();
        while(vEdges.hasMoreElements()){
            Edge e =  (Edge) vEdges.nextElement();
            edges.remove(edgeKey(e));
            super.remove(e);
        }
        return super.remove(vertex);
    }

    // remove a vertex (and all incident edges) by name
    public boolean remove(String name)
    {
        Vertex vertex = findVertex(name);
        if(vertex != null)
        {
            return(remove(vertex));
        }
        return false; /* vertex not found */
    }

    // add an edge
    public boolean add(Edge edge)
    {
        // check for duplicate edge name 
        if(edges.get(edgeKey(edge)) != null)
        {
            return false;
        }
        edges.put(edgeKey(edge),edge);
        return super.add(edge);
    }

    // add an edge by vertex names
    public Edge add(String fromVertex, String toVertex)
    {
        // add the two vertices first if needed
        Vertex fv = add(fromVertex);
        Vertex tv = add(toVertex);
        if((fv == null) || (tv == null))
        {   // if failed to create the vertices
            return null;    
        }
        
        Edge edge = findEdge(fromVertex, toVertex);
        if(edge == null)
        {
            // all edges must be directed
            edge = new Edge(fv, tv, true);
            edge.setName(edgeKey(edge));

            // edge has been encountered
            if(add(edge) == false)
            {
                return null;
            }
        }
        return edge;
    }

    // remove an edge
    public boolean remove(Edge edge)
    {
        edges.remove(edgeKey(edge));
        return super.remove(edge);
    }
    
    // remove an edge by name
    public boolean remove(String fromVertex, String toVertex)
    {
        Edge edge = findEdge(fromVertex, toVertex);
        if(edge != null)
        {
            return remove(edge);
        }
        return false; // doesn't exist
    }

    // find a vertex by name
    public Vertex findVertex(String name)
    {
        return (Vertex)vertices.get(name);
    }

    // find an edge by vertex names
    public Edge findEdge(String fromVertex, String toVertex)
    {
        return (Edge)edges.get(edgeKey(fromVertex, toVertex));
    }

    // get all enumeration of all nodes reachable from starting vertex name
    public Enumeration enumerateBreadthFirstTraversal(String start)
    {
        Vertex vertex = findVertex(start);
        if(vertex != null)
        {
            return super.enumerateBreadthFirstTraversal(vertex);
        }
        return null; // vertex doesn't exist
    }
    public Enumeration enumerateDepthFirstTraversal(String start)
    {
        Vertex vertex = findVertex(start);
        if(vertex != null)
        {
            return super.enumerateDepthFirstTraversal(vertex);
        }
        return null; // vertex doesn't exist
    }
}
