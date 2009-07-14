package edu.rice.rubis.client;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.String;

/**
 * Represents a (simplified) HTTP cookie. Used by the CookieManager class.
 *
 * @author Greg Friedman, ROC Research Group, Stanford University 
 */
public class Cookie
{
    /** The name (key) of the Cookie. */
    public String    Name;

    /** The value of the Cookie. */
    public String    Value;

    /** The cookie's expiration date */
    public Date      Expiration;

    /**
     * Create a cookie with the given name, value and expiration.
     *
     * @param name The name (key) of the cookie 
     * @param value The cookie's String value. 
     * @param expiration When the cookie expires. If null, the cookie does not expire.
     */
    public Cookie(String name, String value, Date expiration)
    {
	this.Name = name;
	this.Value = value;
	this.Expiration = expiration;
    }

    /** Create a Cookie from a text representation of a Cookie as generated 
     * by an HTTP server.
     *
     * @param cookieString Text representation of the cookie
     */
    public Cookie(String cookieString) throws java.text.ParseException
    {
	String fields[] = cookieString.split(";");
	int eqlIdx = fields[0].indexOf("=");

	// First field is cookie's name and value
	this.Name = fields[0].substring(0, eqlIdx);
	this.Value = fields[0].substring(eqlIdx + 1);
	
	// If expiration date is specified, use it.
	this.Expiration = null;
	for (int i = 1; i < fields.length; i++) {
	    if (fields[i].indexOf("xpires") != -1) {
		SimpleDateFormat df = new SimpleDateFormat("\"E, dd MMM yyyy HH:mm:ss Z\"");
		eqlIdx = fields[i].indexOf("=");
		String expireString = fields[i].substring(eqlIdx + 1);
		this.Expiration = df.parse(expireString);
	    }
	}
    }
}
