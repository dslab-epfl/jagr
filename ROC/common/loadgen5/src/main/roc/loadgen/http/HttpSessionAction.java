package roc.loadgen.http;

import java.net.URL;

import java.util.Map;
import java.util.List;

import java.net.MalformedURLException;

import roc.loadgen.Request;
import roc.loadgen.SessionAction;
import roc.loadgen.TraceReader;


public class HttpSessionAction extends SessionAction 
{
    private String file = null;
    private String postData = null;

    private Map headers = null;
    private List commands = null;
    private Map sessionSpecificData = null;

    boolean request = false;

    public String toString()
    {
	return "file: " + file + " postdata: ";
    }

    public HttpSessionAction(String file, String postData, Map headers, List commands, Map sessionSpecific)
    {
	this.file = file;
	this.postData = postData;
	this.headers = headers;
	this.commands = commands;
	this.sessionSpecificData = sessionSpecific;

	request = true;
    }

    public HttpSessionAction(List commands)
    {
	this.commands = commands; 
	request = false;
    }

    public HttpRequest getRequest(String server, int port)
    {
	URL url = null;

	try {
	    //	    System.out.println("Server: " + server + " port " + port + " file " + file);
	    url = new URL("http", server, port, file);
	}
	catch( MalformedURLException ex ) {
	    System.out.println( "ACK. Broken trace?");
	}

	//	System.out.println("New request: " + url + " headers: " + headers + "postdata: " + postData);

	return new HttpRequest(url, headers, postData);
    }

    public HttpRequest getRequest()
    {
	return getRequest("localhost", 8080);
    }

    public Map getSessionData()
    {
	return sessionSpecificData;
    }

    public Map getHeaders()
    {
	return headers;
    }

    public List getCommands()
    {
	return commands;
    }

    public boolean containsRequest()
    {
	return request;
    }

    public void setPostData(String postData)
    {
	this.postData = postData;
    }

    public void setFile(String file)
    {
	this.file = file;
    }

    public void addHeader(String key, Object value)
    {
	headers.put(key, value);
    }
}
