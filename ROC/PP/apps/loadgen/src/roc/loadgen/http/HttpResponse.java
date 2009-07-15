package roc.loadgen.http;

import java.util.Map;

import roc.loadgen.*;

public class HttpResponse implements Response {

    private HttpRequest request=null;

    Map headers;
    int respCode;
    String contentType;
    byte[] respBuf;
    String respStr;
    Throwable ex;
    boolean forceError;
    long respTime;


    // The known-good response that this response differed from
    HttpResponse referenceResponse=null;

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

        public HttpResponse( HttpRequest req ) 
        {
	    request = req;
	    headers = null;
	    respCode = 0;
	    contentType = null;
	    respBuf = null;
	    respStr = null;
	    respTime = 0;
	}

    public HttpRequest getRequest() {  return request;  }

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

    public boolean isServiceUnavailableError() {
	return (respCode == 503);
    }

    public HttpResponse getReferenceResponse() { 
	return referenceResponse; 
    }

    public void setReferenceResponse( HttpResponse resp ) { 
	referenceResponse = resp; 
    }

    public void setIsError() {
	forceError = true;
    }

    public Throwable getThrowable() {
	return ex;
    }
	
    public void setThrowable( Throwable ex ) {
	this.ex = ex;
	this.respCode=-1;
	forceError = false;
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

    /**
     * @param long
     */
    public void setRespTime(long respTime){
	this.respTime = respTime;
    }

    public long getRespTime(){
	return respTime;
    }
}
