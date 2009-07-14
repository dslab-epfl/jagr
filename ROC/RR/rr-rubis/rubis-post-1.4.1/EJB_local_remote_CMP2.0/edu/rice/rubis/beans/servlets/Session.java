package edu.rice.rubis.beans.servlets;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import roc.rr.ssmutil.*;

public class Session {
    private static final boolean useSSM = true;

    // setter and getter method for userId
    public static void setUserId(HttpServletRequest request,
				 HttpServletResponse response,
				 Integer userId)

	throws SSMException
    {
	// Extracts SessionState object from http session object.
	SessionState ss = getSessionState(request);
	// Set new user id.
	ss.setUserId(userId);
	try {
	    // Stores updated SessionState object into ssm. 
	    SSMstore(response, ss);
	} catch (SSMException e) {
	    // If ssm write failed, then invalidate cache
	    setSESSIONSTATENull(request);
	    throw e;
	}
    }

    public static Integer getUserId(HttpServletRequest request)
	throws SSMException 
    {	       
	SessionState ss = getSessionState(request);
	return ss.getUserId();
    }


    // setter and getter method for itemId

    public static void setItemId(HttpServletRequest request,
				 HttpServletResponse response,
				 String itemId)
	throws SSMException
    {
	SessionState ss = getSessionState(request);
	ss.setItemId(itemId);
	try {
	    SSMstore(response, ss);
	} catch (SSMException e) {
	    setSESSIONSTATENull(request);
	    throw e;
	}
    }

    public static String getItemId(HttpServletRequest request)
	throws SSMException
    {
	SessionState ss = getSessionState(request);
	return ss.getItemId();
    }


    // setter and getter method for to

    public static void setTo(HttpServletRequest request,
			     HttpServletResponse response,
			     String to)
	throws SSMException
    {
	SessionState ss = getSessionState(request);
	ss.setTo(to);
	try {
	    SSMstore(response, ss);
	} catch (SSMException e) {
	    setSESSIONSTATENull(request);
	    throw e;
	}
    }

    public static String getTo(HttpServletRequest request)
	throws SSMException
    {
	SessionState ss = getSessionState(request);
	return ss.getTo();
    }


    // setter and getter method for ReturnURL

    public static void setReturnURL(HttpServletRequest request, 
				    HttpServletResponse response,
				    String url)
	throws SSMException
    {
	SessionState ss = getSessionState(request);
	ss.setURL(url);
	try {
	    SSMstore(response, ss);
	} catch (SSMException e) {
	    setSESSIONSTATENull(request);
	    throw e;
	}
    }

    public static String getReturnURL(HttpServletRequest request)
	throws SSMException
    {
	SessionState ss = getSessionState(request);
	String url = ss.getURL();
	ss.setURL(null);
	return url;
    }

    public static void goLogin(HttpServletRequest request,
			       HttpServletResponse response,
			       String url)
	throws Exception
    {
	setReturnURL(request, response,url);
	request.getRequestDispatcher("/login.html").
	    forward(request,response);
    }

    /*	
     *  Store SessionState into SSM and 
     *    attache returned SSM cookie to response
     *         Mar/10/2004 S.Kawamoto
     */
    private static void SSMstore(HttpServletResponse response, 
				 SessionState ss)
	throws SSMException 
    {
	if (useSSM){
	    String[] states = ss.getStates();

	    // print states for debug
	    printStates("[SSMstore] store states: ",states);
	    String cookie = GlobalSSM.write(states);
	    Cookie co = new Cookie("SESSIONSTATE",cookie);
	    co.setPath("/ejb_rubis_web");
	    response.addCookie(co);
	}
    }


    /**
     *  Set a value to SESSIONSTATE attribute
     *
     *
     */
    private static void setSESSIONSTATENull(HttpServletRequest request)
    {
	HttpSession session = request.getSession();
	session.setAttribute("SESSIONSTATE",null);
    }

    /*
     * Extract SessionState from session object
     *    Mar/10/2004 S.Kawamoto
     */
    private static SessionState getSessionState(HttpServletRequest request)
	throws SSMException
    {
	HttpSession session = request.getSession();
	SessionState ss = (SessionState)session.getAttribute("SESSIONSTATE");
	Debug.println("[getSessionState] SESSIONSTATE attribute value: "+ss);
	if ( ss == null ){
	    /*
	     * If there is no SessionState object in Session object
	     *    then if SSM cookie came from the client 
	     *            then try to extract session states from SSM
	     *            else create new one 
             *         after that store it to session object
	     * 
	     *    Mar/10/2004 S.Kawamoto
	     */


	    ss = new SessionState();
	    Debug.println("[getSessionState] create new SessionState");

	    if (useSSM){
		// extract states from SSM
		Cookie[] cookies = request.getCookies();
		if ( cookies != null ){
		    for(int i=0;i<cookies.length;i++){
			Cookie cookie = cookies[i];
			if (cookie.getName().equals("SESSIONSTATE")) {
			    Debug.println("[getSessionState] SSMCookie exists, try to read it");
			    String ssmCookie = cookie.getValue();
			    String[] states = null;
			    states = (String[])GlobalSSM.read(ssmCookie);
			    // print states for debug
			    if ( states != null ){
				printStates("[getSessionState] states: ",
					    states);
				ss.setStates(states);
			    } else {
				Debug.println("[getSessionState] cannot read SSM");
			    }
			    break;
			}
		    }
		}
	    }
	    
	    // store SessionState into Http session object
	    session.setAttribute("SESSIONSTATE",ss);
	}

	// Corrupt session attribute
	SessionFaultInjector.corruptSessionAttribute(session,ss);

	// System.out.println("SessionState: "+ss);
	return ss;
    }

    // write empty session states into SSM
    public static void resetSessionState(HttpServletRequest request,
					 HttpServletResponse response)
	throws SSMException 
    {

	if (useSSM) {
	    HttpSession session = request.getSession();
	    SessionState ss = (SessionState)session.getAttribute("SESSIONSTATE");
	    String [] states = null;
	    if (ss != null){
		states = ss.getStates();
	    } else {
		states = new String[4];
	    }
	    // initialize all items
	    for(int i=0;i<4;i++){
		states[0]=null;
	    }
	
	    Debug.println("[resetSessionState] reset states");
	    String cookie = GlobalSSM.write(states);
	    Cookie co = new Cookie("SESSIONSTATE",cookie);
	    co.setMaxAge(0);
	    co.setPath("/ejb_rubis_web");
	    response.addCookie(co);
	}
    }	

    // return useSSM 
    public static boolean getUseSSM(){
	return useSSM;
    }

    // print states 
    private static void printStates(String title, String[] s){
	Debug.println(title
		      +" uid "+s[0]
		      +" item "+s[1]
		      +" to "+s[2]
		      +" url "+s[3]);
    }
}



