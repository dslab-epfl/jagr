/*
 * SessionDemo is a test web application for SSM.
 *   Compile it with -classpath .:../../../ssm.jar:$JBOSS_HOME/server/default/lib/javax.servlet.jar
 *
 *    Mar/16/2004 S.Kawamoto
 */

import java.lang.Number;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Vector;
import roc.rr.ssm.*;
import roc.rr.ssmutil.*;

public class SessionDemo extends HttpServlet {
    private Stub st=null;

    public void init() throws ServletException {
	// initialize Stub
	GlobalSSM.initialize();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
	doPerform(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
	doPerform(request, response);
    }

    public void doPerform(HttpServletRequest request, 
			  HttpServletResponse response)
	throws ServletException, IOException {

	System.out.println("--- begin doPerform ---");

	int counter = 0;
	Cookie c=null;
	Cookie cookies[] = request.getCookies();

	// extract ssm cookie from http cookies
	if ( cookies != null ) {
	    for (int i=0; i<cookies.length;i++) {
		if (cookies[i].getName().equals("SSMID")) {
		    c = cookies[i];
		} 
	    }
	}

	if(c!=null){
	    String httpcookie = c.getValue();
	    System.out.println("--- http cookie: "+httpcookie);

	    // read data from SSM associated with ssm cookie
	    Integer i = (Integer)GlobalSSM.read(httpcookie);
	    if (i!=null){
		counter = i.intValue();
		counter++;
	    } else {
		System.out.println("failed to read");
		return;
	    }

	}

	
	Integer newvalue = new Integer(counter);
	String newssmcookie=null;

	// write updated data to SSM
	newssmcookie = GlobalSSM.write(newvalue);
	System.out.println("--- write ssmcookie: "+newssmcookie);
	if ( newssmcookie != null ){
	    // return ssm cookie to web client
	    Cookie newcookie = new Cookie("SSMID", newssmcookie);
	    response.addCookie(newcookie);
	    System.out.println("write succeed!");
	} else {
	    System.out.println("write failed");
	}

	String str = String.valueOf(counter);
	// 応答文字コードのセット
	response.setContentType("text/html; charset=Shift_JIS");
	// 出力ストリームの取得
	PrintWriter out = response.getWriter();
	out.println("<html><head>\n"
		    + "<meta http-equiv=\"content-type\" content=\"text/html; charset=Shift_JIS\" />\n"
		    + "<title>Session Demo</title>\n"
		    + "</head><body>\n"
		    + "<h1>Session Demo</h1>\n"
		    + "<p>" + str + "</p>\n"
		    + "<br><p> How to test </p>"
		    + "<br>1. Reload this page several times.\n"
		    + "<br>2. After that undeploy and deploy this web application.\n"
		    + "<br>3. Reload this page again.\n"
		    + "<br>4. Indicated number should be the successor of previous number if ssm is working properly.\n"
		    + "</body></html>\n");
	
    }
}
