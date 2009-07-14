package roc.loadgen.http;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import roc.loadgen.Request;

public class HttpRequest extends Request implements Serializable {

	URL url;
	Map headers;
	String postData;

	public HttpRequest(URL url, Map headers, String postData) {
		this.url = url;
		this.headers = headers;
		this.postData = postData;
	}

	public String toString() {
		return url.toString() + " posting: " + postData;
	}

	/**
	 * @return
	 */
	public Map getHeaders() {
		return headers;
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
