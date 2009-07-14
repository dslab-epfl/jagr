//package org.jboss.RR;
package tracing;

import com.ibm.graph.Edge;
import com.ibm.graph.Net;
import com.ibm.graph.Vertex;

import java.util.*;

class FNet extends Net{

	protected Hashtable vertices = new Hashtable();
	protected Hashtable edges = new Hashtable();

	public boolean add(Vertex vertex){

	   vertices.put(vertex.getName(), vertex);
	   return super.add(vertex);
	}

        public Vertex findVertex(String name){
	   return (Vertex)vertices.get(name);

	}

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

	public boolean add(Edge edge){
         
	  edges.put(edgeKey(edge),edge);
	  return super.add(edge);
	}

	public Edge findEdge(String fromVertex, String toVertex){
	  return (Edge)edges.get(fromVertex+toVertex);
	}
	  
	public String edgeKey(Edge edge){
	   return (edge.getFromVertex().getName() + edge.getToVertex().getName());

	}


}
