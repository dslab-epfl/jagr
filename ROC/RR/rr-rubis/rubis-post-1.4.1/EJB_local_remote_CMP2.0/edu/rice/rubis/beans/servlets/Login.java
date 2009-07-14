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

import roc.rr.ssmutil.GlobalSSM;
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

public class Login extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: Login");
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
      int uid = -1;
      ServletPrinter sp = null;
      String name = request.getParameter("nickname");
      String pass = request.getParameter("password");
      sp = new ServletPrinter(request, response, "Login");

      if ((name == null) || (name.equals(""))||
	  (pass == null) || (pass.equals(""))) {
	  printError("nickname and password are required - Cannot process the request<br>", sp);
	  return ;
      }

      // check already loged in or not
      Integer userId;
      try {
	  userId = Session.getUserId(request);
      } catch (SSMException e) {
	  printError("Cannot read user id from SSM: "+e,sp);
	  return;
      }

      if ( userId != null ){
	  String err = "You have already logged in.  Your ID: " + userId + ", your cookie(s): ";
	  Cookie[] cookies = request.getCookies();
	  for( int i=0 ; i < cookies.length ; i++ )
	      err += cookies[i].getName() + "=" + cookies[i].getValue() + " ";
	  printError( err ,sp);
	  return;
      }

      Context initialContext = null;
      try {
	  initialContext = new InitialContext();
      } catch (Exception e) {
	  printError("Cannot get initial context for JNDI: " + e+"<br>", sp);
	  return ;
      }
    
      SB_AuthLocalHome authHome = null;
      SB_AuthLocal auth = null;
      String jndiName = "SB_AuthHome";
      Object jndiValue = null;

      try {
	  //jndiValue = initialContext.lookup("java:comp/env/ejb/SB_AuthLocal");
	  jndiValue = initialContext.lookup(jndiName);
	  authHome = (SB_AuthLocalHome)jndiValue;
	  auth = authHome.create();
      } 
      catch (ClassCastException e) {
	  // Send service unavailable response to client
	  // since microreboot of SB_ViewItem is in progress.
	  sp.sendServiceUnavailable(jndiName, jndiValue);
      }
      catch (Exception e) {
	  printError("Cannot lookup SB_AuthLocalHome: " +e+"<br>", sp);
	  return;
      }

      try {
	  uid = auth.authenticate(name, pass);
      } catch (Exception e) {
	  printError("You don't have an account on RUBiS!<br>You have to register first.<br>",sp);
	  return ;
      }

      // password incorrect
      if ( uid == -1 ) {
	  sp.printHTMLheader("RUBiS: Login");
	  sp.printHTML("<center><h2>incorrect password!</2></center>");
	  sp.printHTML("<form method=\"post\" action=\"/ejb_rubis_web/login.html\"\n");
	  sp.printHTML("<center><input type=\"submit\" value=\"try again\"></center></form>");
	  sp.printHTMLfooter();
	  return;
      } 

      // add userId to SessionState
      try {
	  Integer io = new Integer(uid);
	  Session.setUserId(request, response, io);
      } catch (Exception e) {
	  printError("Cannot write user id to SSM: "+e+"<br>",sp);
	  return;
      }
	  
      // return to previous URL   Feb/27/04 S.Kawamoto
      String url;
      try {
	  url = Session.getReturnURL(request);
      } catch (Exception e) {
	  printError("Cannot read return URL from SSM: "+e,sp);
	  return;
      }

      if ( url != null ){
	  request.getRequestDispatcher(url).forward(request,response);
      } else {
	  // Display the welcome message
	  sp.printHTMLheader("RUBiS: Login");
	  sp.printHTML("<h2><center>Welcome, "
		       +name+" !</center></h2><br>");
	  sp.printHTMLfooter();
      }
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

    public void init() throws ServletException {
	// initialize SSM
	if (Session.getUseSSM()){
	    GlobalSSM.initialize();
	}
    }
}
