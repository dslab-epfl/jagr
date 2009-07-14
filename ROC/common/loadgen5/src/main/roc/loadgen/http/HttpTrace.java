package roc.loadgen.http;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import roc.loadgen.Request;
import roc.loadgen.Trace;

public class HttpTrace extends Trace {

    private String hostname;
    private int port;

    public HttpTrace(String filename)
    {
	this.filename = filename;
	//	LoadTrace();
    }

    public HttpTrace()
    {
	filename = null;
    }

    protected void loadTrace()
    {
	trace = new ArrayList();

	ArrayList stringTrace = LoadStrings();
	ParseTrace(stringTrace);
    }

    private ArrayList LoadStrings()
    {
	try {

	    ArrayList traces = new ArrayList();
	    
	    BufferedReader traceReader =
		new BufferedReader(new FileReader(filename));
	    
	    String line = null;
	    List currentTrace = new LinkedList();
	    
	    do {
		line = traceReader.readLine();
		if (line != null) {
		    line = line.trim();
		    if (line.length() > 0) {
			currentTrace.add(line);
		    }
		    else if (currentTrace.size() > 0) {
			traces.add(currentTrace);
			currentTrace = new LinkedList();
		    }
		}
	    }
	    while (line != null);
	    
	    return traces;
	}
	catch (IOException e) { e.printStackTrace(); System.err.println("Broke trace!"); }

	return null;
    }

    private void ParseTrace(ArrayList traces)
    {
        Map headers;
	List commands;
	Map sessionSpecific;

	String file;
	String postData;

	LinkedList oneRequest;
	Iterator iterWithinReq;
	Iterator traceIter = traces.iterator();

	boolean containsRequest;

	while(traceIter.hasNext())
	    {
		oneRequest = (LinkedList)traceIter.next();
		iterWithinReq = oneRequest.iterator();

		headers = new HashMap();
		commands = new LinkedList();
		sessionSpecific = new HashMap();

		file = null;
		postData = null;

		containsRequest = false;
		
		while (iterWithinReq.hasNext()) {

		    String line = (String)iterWithinReq.next();
		    
		    if (line.startsWith("$")) {
			commands.add(line);
		    }
		    else if (line.startsWith("GET")) {
			containsRequest = true;
			line = line.substring("GET".length()).trim();
			if (line.endsWith("HTTP/1.1")) {
			    line =
				line.substring(0, line.length() - "HTTP/1.1".length());
			}
			if(containsVariables(line))
			    sessionSpecific.put("file", line);
			file = line;
		    }
		    else if (line.startsWith("POST")) {
			containsRequest = true;
			line = line.substring("POST".length()).trim();
			//			post = true;
			if (line.endsWith("HTTP/1.1")) {
			    line =
				line.substring(0, line.length() - "HTTP/1.1".length());
			}
			if(containsVariables(line))
			    sessionSpecific.put("file", line);
			file = line;
		    }
		    else if (line.startsWith("Referer:")) {
			int idx = line.indexOf(":");
			headers.put(line.substring(0,idx),
					   line.substring(idx+1).trim());
		    }
		    else if (line.startsWith("Host:")) {
			int idx = line.indexOf(":");
			headers.put(line.substring(0,idx),
					   line.substring(idx+1).trim());
		    }
		    else if (line.startsWith("Content-Length:")) {
			//ignore this case
		    }
		    else if (line.startsWith("Cookie:")) {
			int idx = line.indexOf(":");
			/*sessionHeaders.put(line.substring(0,idx),
			  line.substring(idx+1).trim());*/
			sessionSpecific.put(line.substring(0,idx),
					   line.substring(idx+1).trim());
		    }
		    else if (line.startsWith("LG-POSTDATA")) {
			postData = "";
			line = line.substring( "LG-POSTDATA ".length()).trim();
			postData += line;
			if(containsVariables(line))
			    sessionSpecific.put("postData", postData);
		    }
		    else {
			int idx = line.indexOf(":");
			if (idx != -1) {
			    headers.put(
					line.substring(0, idx),
					line.substring(idx + 1).trim());
			}
		    }
		}   
		
		if (postData != null) {
		    headers.put("Content-Length", Integer.toString(postData.length()));
		}
		
		if(containsRequest)
		    trace.add(new HttpSessionAction(file, postData, headers, commands, sessionSpecific));
		else
		    trace.add(new HttpSessionAction(commands));
	    }
    }

    private boolean containsVariables(String str)
    {
	return (str.indexOf('$') != -1);
    }
}
