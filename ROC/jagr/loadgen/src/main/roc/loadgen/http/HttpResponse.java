package roc.loadgen.http;

import java.util.Map;

import roc.loadgen.*;

public class HttpResponse implements Response {

	Map headers;
	int respCode;
	String contentType;
	byte[] respBuf;
	String respStr;

	Throwable ex;
    boolean forceError;


	public HttpResponse(
		Map headers,
		int respCode,
		String contentType,
		byte[] respBuf,
		String respStr) {
			
		this.headers = headers;
		this.respCode = respCode;
		this.contentType = contentType;
		this.respBuf = respBuf;
		this.respStr = respStr;
		forceError = false;
	}

	public HttpResponse(Throwable ex) {
		this.ex = ex;
		this.respCode=-1;
		forceError = false;
	}

    public String toString() {
	return respCode + ", " + contentType + ", " +
	    ((respBuf==null)
	     ?("[NULL]")
	     :(Integer.toString(respBuf.length))) + ", " +
	    ((ex==null)
	     ?("[NOEXC]")
	     :(ex.getMessage()));
    }

	public boolean isOK() {
		return (!forceError) && (ex == null) && (respCode >= 200) && (respCode < 300);
	}

	public boolean isError() {
		return (forceError) || (ex != null) || (respCode >= 400);
	}

    public void setIsError() {
	forceError = true;
    }

	public Throwable getThrowable() {
		return ex;
	}
	
	public void setThrowable( Throwable ex ) {
		this.ex = ex;
	}

	/**
	 * @return
	 */
	public String getContentType() {
		return contentType;
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
	public byte[] getRespBuf() {
		return respBuf;
	}

	/**
	 * @return
	 */
	public int getRespCode() {
		return respCode;
	}

	/**
	 * @return
	 */
	public String getRespStr() {
		return respStr;
	}

	/**
	 * @param string
	 */
	public void setContentType(String string) {
		contentType = string;
	}

	/**
	 * @param map
	 */
	public void setHeaders(Map map) {
		headers = map;
	}

	/**
	 * @param bs
	 */
	public void setRespBuf(byte[] bs) {
		respBuf = bs;
	}

	/**
	 * @param i
	 */
	public void setRespCode(int i) {
		respCode = i;
	}

	/**
	 * @param string
	 */
	public void setRespStr(String string) {
		respStr = string;
	}

}
