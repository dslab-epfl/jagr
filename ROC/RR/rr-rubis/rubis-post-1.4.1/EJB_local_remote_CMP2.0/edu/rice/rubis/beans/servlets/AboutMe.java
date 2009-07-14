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

import edu.rice.rubis.beans.SB_AboutMe;
import edu.rice.rubis.beans.SB_AboutMeHome;
import roc.rr.ssmutil.SSMException;

/**
 * This servlets displays general information about the user loged in
 * and about his current bids or items to sell.
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class AboutMe extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: AboutMe");
    sp.printHTML("<h3>Your request has not been processed due to the following error :</h3><br>");
    sp.printHTML(errorMsg);
    sp.printHTMLfooter();
  }

  /**
   * Call <code>doPost</code> method.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doPost(request, response);
  }


  /** 
   * Check username and password and build the web page that display the information about
   * the loged in user.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
      ServletPrinter sp = null;
      Context initialContext = null;

      //String  password=null, username=null; 
      Integer userId=null;
    
      sp = new ServletPrinter(request, response, "AboutMe");


      // check login status first
      // if not loged in, go to login page, after that execute here again 
      //   Mar/01/04  S.Kawamoto
      try {
	  userId = Session.getUserId(request);
      } catch (SSMException e) {
	  printError("Cannot read session data from SSM: "+e+"<br>",sp);
	  return;
      }
      if ( userId == null || userId.intValue() < 0 ) {
	  try {
	      Session.goLogin(request,response,"/servlet/edu.rice.rubis.beans.servlets.AboutMe");
	  } catch (Exception e) {
	      printError("Cannnot go to Login page: "+e+"<br>",sp);
	  }
	  return;
      }

      try {
	  initialContext = new InitialContext();
      } catch (Exception e) {
	  printError("Cannot get initial context for JNDI: " + e+"<br>", sp);
	  return ;
      }
         
      SB_AboutMeHome aboutMeHome;
      SB_AboutMe aboutMe;
      String jndiName  = "SB_AboutMeHome";
      Object jndiValue = null;

      try {
	  jndiValue = initialContext.lookup(jndiName);
	  aboutMeHome = (SB_AboutMeHome)PortableRemoteObject.narrow(jndiValue,SB_AboutMeHome.class);
	  aboutMe = aboutMeHome.create();
      } 
      catch (ClassCastException e) {
	  // Send service unavailable response to client 
	  // since microreboot of SB_AboutMe is in progress.
	  sp.sendServiceUnavailable(jndiName, jndiValue);
	  return;
      } 
      catch (Exception e) {
	  printError("Cannot lookup SB_AboutMe: " +e+"<br>", sp);
	  return ;
      }

      String html;
      try {
	  html = aboutMe.getAboutMe(userId);
      } catch (Exception e) {
	  printError("Cannot retrieve information about you: " +e+"<br>", sp);
	  return ;
      }

      sp.printHTMLheader("RUBiS: About Me");
      sp.printHTML(html);
      sp.printHTMLfooter();
  }

}
