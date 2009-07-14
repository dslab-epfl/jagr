package edu.rice.rubis.beans.servlets;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import edu.rice.rubis.beans.SB_AuthLocalHome;
import edu.rice.rubis.beans.SB_AuthLocal;
import edu.rice.rubis.beans.SB_AboutMeHome;
import edu.rice.rubis.beans.SB_AboutMe;

import roc.rr.ssmutil.SSMException;

/** This servlets authenticate user with nickname and password
 * It must be called this way :
 * <pre>
 * http://..../Login?nickname=yy&password=zz
 *    where yy is the nick name of the user
 *          zz is the user password
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class Logout extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: Logout");
    sp.printHTML("<h2>Your request has not been processed due to the following error :</h2><br>");
    sp.printHTML(errorMsg);
    sp.printHTMLfooter();
  }


  /**
   * Describe <code>doGet</code> method here.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
      ServletPrinter sp = null;
      sp = new ServletPrinter(request, response, "Logout");

      // check already loged in or not
      HttpSession session = request.getSession();
      if ( session != null ) {
	  try {
	      //  set null to SessionState instead of removing attribute
	      // Session.setUserId(request, response, null);

	      Session.resetSessionState(request,response);
	      session.invalidate();
      
	  } catch (Exception e){
	      printError("Cannot get or remove attribute: "+e,sp);
	  }
      }

      // send expired cookie to client
      Cookie[] cookies = request.getCookies();
      if (cookies != null){
	  Cookie cookie = null;
	  for(int i=0;i<cookies.length;i++){
	      cookie = cookies[i];
	      if (cookie.getName().equals("JSESSIONID")){
		  cookie.setMaxAge(0);
		  response.addCookie(cookie);
	      }
	  }
      }


      // Display the welcome message

      // Header must not check login state
      // sp.printHTMLheader("RUBiS: Login");
      sp.printLoginHTMLheader("RUBiS: Login");
      sp.printHTMLBody("/body-home.html");
      sp.printHTMLfooter();
  }

  /**
   * Call the <code>doGet</code> method.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      doGet(request, response);
  }
}
