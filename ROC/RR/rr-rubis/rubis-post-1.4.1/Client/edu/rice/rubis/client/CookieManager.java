package edu.rice.rubis.client;

import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.util.Date;
import edu.rice.rubis.client.Cookie;

/**
 * The CookieManager sends and retreives cookies to/from an HTTP
 * server via a URLConnection. It keeps a running accumulation of all
 * the cookies sent in its private "cookie jar", and arranges to send
 * all such cookies when "setCookiesToSend" is called.
 *
 * Note: The CookieManager handles cookie expiration times, but does NOT currently handle
 * cookie filtering based on domain or any other "complex" cookie management functions.
 *
 * @author Greg Friedman, ROC Research Group, Stanford University 
 */
public class CookieManager
{
    private UserSession session; // Owning session, for logging trace messages.
    private HashMap cookieJar;

    /**
     * Create a new cookie manager, associated with the UserSession.
     * @param sess The associated UserSession, which may be used for logging.
     */
    public CookieManager(UserSession sess)
    {
	this.session = sess;
	cookieJar = new HashMap();
    }

    /**
     * Opens the connection to "conn", by calling conn.connect().
     *
     * Also sends the cookies in my cookie jar to the HTTP server on
     * the other end of "conn", and then merges the cookies sent back
     * from the server into my cookie jar.
     *
     * @param conn The connection on which we will call "connect()"
     */
    public void connect(URLConnection conn) throws IOException
    {
	this.setCookiesToSend(conn);
	conn.connect();
	this.getCookies(conn);
    }

    /**
     * Empty my cookie jar. Removes all the cookies I was managing.
     */
    public void emptyCookieJar()
    {
	cookieJar.clear();
    }

    /**
     * Retrieve the cookies sent by the HTTP server on the other side
     * of "conn", and store them in my cookie jar. If the server sends
     * a new value for a cookie that already exists in my jar, the old
     * value is replaced with the new value. 
     *
     * @param conn An open connection to the HTTP server. (NOTE:
     * getCookies should be called after conn.connect() is called). 
     */
    private void getCookies(URLConnection conn)
    {
        for (int i=0; ; i++) {
            String headerName = conn.getHeaderFieldKey(i);
            String headerValue = conn.getHeaderField(i);
    
            if (headerName == null && headerValue == null) {
                // No more headers
                break;
            }
            if ("Set-Cookie".equalsIgnoreCase(headerName)) {
		// this.session.log("Cookie Manager", "Reading cookie: " + headerValue, 4); 
		try {
		    Cookie c = new Cookie(headerValue);
		    cookieJar.put(c.Name, c);
		    /*
		    if (c.Expiration == null)
			this.session.log("Cookie Manager", "Cookie exp. date is null", 4);
		    else {
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat();
			this.session.log("Cookie Manager", "Cookie exp. date is " +
					 df.format(c.Expiration), 
					 4);
		    }
		    */
		} catch (java.text.ParseException e) {
   		    this.session.log("CookieManager", "Error parsing cookie from header returned from HTTP header: " + e.toString(), 2);
		}
	    }
	}
    }

    /**
     * Arrange for all the stored cookies in my cookie jar tbe sent to
     * the HTTP server on the other side of "conn". The cookies will
     * actually be sent when conn.connect() is called. Expired cookies are
     * not sent.
     *
     * @param conn An open connection to the HTTP server. (NOTE:
     * setCookiesToSend() should be called before conn.connect() is
     * called). 
     */
    private void setCookiesToSend(URLConnection conn)
    {
	if (cookieJar.size() > 0) {
	    conn.addRequestProperty("Cookie", generateCookieHeader());
	}
    }

    /**
     * Generate the string representing all the cookies in my cookie
     * jar; this String is in standard HTTP header format for cookies, so
     * it will be understood by an HTTP server. Expired cookies are
     * not included in this string.
     *
     * @returns A string representing all the unexpired cookies I am managing. 
     */
    private String generateCookieHeader() {
        String cookieLine = "";
	Date currentDate = null;

        Iterator iter = cookieJar.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
	    Cookie c = (Cookie)cookieJar.get(key);

	    if (c.Expiration != null) {
		// Check to see if cookie has expired

		// this.session.log("CookieManager", "Checking expiration date", 4);

		if (currentDate == null) {
		    currentDate = new Date();
		}

		if (currentDate.compareTo(c.Expiration) > 0) {
		    this.session.log("CookieManager",
				     "Cookie " + c.Name + " has expired; ignoring.",
				     3);
		    c = null;
		}		
	    }
	    if (c != null) {		
		cookieLine += c.Name + "=" + c.Value;
		if (iter.hasNext()) {
		    cookieLine += "; ";
		}
	    }
	}
	this.session.log("CookieManager", "Sending cookie(s): \"" + cookieLine + "\"", 3);
        return cookieLine;
    }
}
