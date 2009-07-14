/*
 *  SSMInitialize: SSM initializeation servlet 
 *
 *                                   Apr/1/2004 S.Kawamoto
 */

package edu.rice.rubis.beans.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import roc.rr.ssmutil.GlobalSSM;

public class SSMInitialize extends HttpServlet{

    public void doGet(HttpServletRequest request, 
		      HttpServletResponse response) 
	throws ServletException {
	
	ServletPrinter sp = new ServletPrinter(request, response, 
					       "SSMInitialize");
	try {
	    // force to initialize SSM
	    GlobalSSM.forceInitialize();
	} catch (Exception e){
	    sp.printHTML("<center><h1>RUBiS</h1></center>"); 
	    sp.printHTML("<center><h2> SSM initialization failed </h2></center>");
	    sp.printHTML(e.toString());
	}
	sp.printHTML("<center><h1>RUBiS</h1></center>"); 
	sp.printHTML("<center><h2>SSM initialization succeeded</h2></center>");
	sp.printHTML("<center><a href=\"/ejb_rubis_web/index.html\">go home</a></center>");

    }

  public void doPost(HttpServletRequest request, 
		     HttpServletResponse response)
      throws ServletException {

      doGet(request, response);
  }

    public void init() throws ServletException {
    }
}
