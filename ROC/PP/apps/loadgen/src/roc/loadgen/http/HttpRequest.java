package roc.loadgen.http;

import java.io.Serializable;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest extends roc.loadgen.Request implements Serializable {

    String stringID;

    String server;
    int port;
    String path;

    Map headers;
    String postData;

    Map metadata;  // not used as part of the request, but may be read/written by interceptors
    
    public HttpRequest(String path, Map headers, String postData) {
	this(path,headers,postData, new HashMap(0));
    }
    
    public HttpRequest(String path, Map headers, String postData, Map metadata ) {
	this.path = path;
	this.headers = headers;
	this.postData = postData;
	this.metadata = metadata;
    }
	
    public void setStringID( String id ) {
	this.stringID = id;
    }

    public String getStringID() {
	return stringID;
    }
	
    public String toString() 
    {
	return "http://" + server + ":" + port + "/" + path;
    }

	/**
	 * @return
	 */
	public Map getHeaders() {
		return headers;
	}

	public Map getMetadata(){
	    return metadata;
	}
	
	/**
	 * @return
	 */
	public String getPostData() {
		return postData;
	}

    /**
     * @return
     */
    public URL getUrl() 
	throws MalformedURLException
    {
	assert path!=null;
	assert server!=null;
	assert port>0;

	return new URL( toString() );
    }

    public String getServer() { return server; }
    public int getPort() { return port; }
    public String getPath() {  return path;  }

	/**
	 * @param map
	 */
	public void setHeaders(Map map) {
		headers = map;
	}

	/**
	 * @param string
	 */
	public void setPostData(String string) {
		postData = string;
	}

    public void setDestination( String server ) 
    {
	assert path!=null;

	this.server = server;
	this.port = 8080;
    }

}
