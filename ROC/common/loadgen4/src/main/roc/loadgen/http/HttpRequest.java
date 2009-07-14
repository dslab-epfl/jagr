package roc.loadgen.http;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import roc.loadgen.Request;

public class HttpRequest extends Request implements Serializable {

    String stringID;

	URL url;
	Map headers;
	String postData;

	Map metadata;  // not used as part of the request, but may be read/written by interceptors

	public HttpRequest(URL url, Map headers, String postData) {
	    this(url,headers,postData, new HashMap(0));
	}

	public HttpRequest(URL url, Map headers, String postData, Map metadata ) {
	    this.url = url;
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
	
	public String toString() {
		return url.toString();
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
	public URL getUrl() {
		return url;
	}

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

	/**
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

}
