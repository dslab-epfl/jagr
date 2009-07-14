package tracing;

import java.util.*;
import java.io.*;

/**
 * Generates unique IDs for requests.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: DependencyGraphs.java,v 1.1 2002/11/26 00:07:01 mikechen Exp $
 */ 


public class DependencyGraphs  {
    Map paths = new HashMap();

    FMap fmap;

    public DependencyGraphs() {
	fmap = new FMap();
    }

    public void addObservation(RequestObservation obs) {
	String id   = obs.requestId;
	int seqNum  = obs.seqNum;
	int returnSeqNum = obs.returnSeqNum;
	String name = obs.name.toLowerCase();
	long latency = obs.latency;
	
	RequestObservation[] path = (RequestObservation[])(paths.get(id));
	if (path == null) {
	    //// new path
	    path = new RequestObservation[1000];
	    paths.put(id, path);
	}
	// add to the path
	if (name.startsWith("class ")) {
	    obs.name = obs.name.substring(name.lastIndexOf(".")+1);
	}
	if (name.startsWith("select ")) {
	    obs.name = obs.name.substring(name.indexOf("from "), name.indexOf("where "));
	}
	if (name.startsWith("update ")) {
	    obs.name = obs.name.substring(0, name.indexOf("set "));
	}
	path[seqNum] = obs;
	if (returnSeqNum != -1)
	    path[returnSeqNum] = obs;

	if (seqNum == 0) {
	    if (latency >= 0) {
		int argsIndex = name.indexOf("?");
		if (argsIndex != -1) {
		    obs.name = name.substring(0,argsIndex);
		}
		//// end of a path, draw it!
		insertPath(id, path);
	    }
	}
    }

    void insertPath(String id, RequestObservation[] path) {
	System.out.println("inserting paths..");
	Stack stack = new Stack();
	for (int i = 0; i < path.length-1; i++) {
	    System.out.println("i=" + i );
	    //System.out.println(path[i]);
	    // stop if no more nodes in the call path
	    // should really do a sanity check here
	    /*
	    if (path[i+1] == null) {
		System.out.println("path[" + (i+1) + "] is null");
		break;
	    }
	    */
	    if (path[i] == null) {
		System.out.println("path[" + (i) + "] is null. done adding edges.");
		break;
	    }

	    // add an edge if
	    // 
	    /*
	    if (path[i+1].returnSeqNum != i+1   // if the next item is not a call return
		//|| path[i].seqNum == 0
		//|| (path[i].seqNum + 1 == path[i].returnSeqNum)
		) { 
	    }
	    */

	    RequestObservation curr = path[i];
	    if (curr.returnSeqNum != i || (i == 0 && curr.seqNum == 0)) {

		if (curr.seqNum != 0) {
		    RequestObservation prev = (RequestObservation)stack.peek();
		    fmap.addEjb(prev.name);
		    fmap.addEjb(curr.name);
		    fmap.addEdge(prev.name, curr.name);
		    System.out.println("adding " + prev.name + " --> " +  curr.name);
		}
		stack.push(curr);
	    }
	    else {//		
		//System.err.println("popping [" + i + "] seqNum=" + curr.seqNum + ", returnSeqNum=" + curr.returnSeqNum);
		if (curr.seqNum != curr.returnSeqNum)
		    stack.pop();
	    }
	}
	System.out.println("stack is: " + stack);
    }

    void draw() {
	fmap.draw();
    }

    void save() {
	fmap.saveFMapToXML(new File("paths-" + System.currentTimeMillis() + ".xml"));
    }

}
