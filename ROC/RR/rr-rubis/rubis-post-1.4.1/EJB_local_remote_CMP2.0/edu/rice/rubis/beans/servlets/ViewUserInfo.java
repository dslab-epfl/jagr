package edu.rice.rubis.beans.servlets;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.rice.rubis.beans.SB_ViewUserInfo;
import edu.rice.rubis.beans.SB_ViewUserInfoHome;

/** This servlets displays general information about a user.
 * It must be called this way :
 * <pre>
 * http://..../ViewUserInfo?userId=xx where xx is the id of the user
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class ViewUserInfo extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: View user info");
    sp.printHTML("<h2>We cannot process your request due to the following error :</h2><br>");
    sp.printHTML(errorMsg);
    sp.printHTMLfooter();
  }


  /**
   * Call the <code>doPost</code> method.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    doPost(request, response);
  }

  /**
   * Display information about a user.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
  {
    ServletPrinter sp = null;
    Context initialContext = null;

    String  value = request.getParameter("userId");
    Integer userId;
    
    sp = new ServletPrinter(request, response, "ViewUserInfo");

    if ((value == null) || (value.equals("")))
    {
      sp.printHTMLheader("RUBiS ERROR: View user information");
      sp.printHTML("<h3>You must provide a user identifier !<br></h3>");
      sp.printHTMLfooter();
      return ;
    }
    else
      userId = new Integer(value);

    try
    {
      initialContext = new InitialContext();
    } 
    catch (Exception e) 
    {
      printError("Cannot get initial context for JNDI: " + e+"<br>", sp);
      return ;
    }

    SB_ViewUserInfoHome viewUserInfoHome = null;
    SB_ViewUserInfo viewUserInfo = null;
    String jndiName = "SB_ViewUserInfoHome";
    Object jndiValue = null;

    try {
	jndiValue = initialContext.lookup(jndiName);
	viewUserInfoHome = (SB_ViewUserInfoHome)PortableRemoteObject.narrow(jndiValue, SB_ViewUserInfoHome.class);
	viewUserInfo = viewUserInfoHome.create();
    } 
    catch (ClassCastException e) {
        // Send service unavailable response to client
        // since microreboot of SB_ViewItem is in progress.
        sp.sendServiceUnavailable(jndiName, jndiValue);
        return;
    }
    catch (Exception e) {
	printError("Cannot lookup SB_ViewUserInfo: " +e+"<br>", sp);
	return ;
    }

    try {
	String html = viewUserInfo.getUserInfo(userId);
	sp.printHTMLheader("RUBiS: View user information");
	sp.printHTML(html);
	sp.printHTMLfooter();
    }
    catch (Exception e) {
      printError("Cannot get item description (got exception: " +e+")<br>", sp);
      return ;
    }

  }

}
