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

import edu.rice.rubis.beans.SB_BuyNow;
import edu.rice.rubis.beans.SB_BuyNowHome;

import roc.rr.ssmutil.SSMException;


/** This servlets display the page allowing a user to buy an item
 * It must be called this way :
 * <pre>
 * http://..../BuyNow?itemId=xx&nickname=yy&password=zz
 *    where xx is the id of the item
 *          yy is the nick name of the user
 *          zz is the user password
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */


public class BuyNow extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: Buy Now");
    sp.printHTML("<h2>Your request has not been processed due to the following error :</h2><br>");
    sp.printHTML(errorMsg);
    sp.printHTMLfooter();
  }


  /**
   * Authenticate the user and end the display a buy now form
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      ServletPrinter sp = null;
      HttpSession session = request.getSession();
      String itemStr = request.getParameter("itemId");
      //String name = request.getParameter("nickname");
      //String pass = request.getParameter("password");
      sp = new ServletPrinter(request, response, "BuyNow");

      // If itemStr is null, it should be in SessionState object.
      // Else indicate error.
      //  Mar/3/04  S.Kawamoto
      if ((itemStr == null) || (itemStr.equals(""))){
	  try {
	      itemStr = Session.getItemId(request);
	  } catch (SSMException e) {
	      printError("Cannot read item id from SSM: "+e+"<br>",sp);
	      return;
	  }

	  if (itemStr==null || itemStr.equals("")){
	      printError("Item Id is required - Cannot process the request<br>", sp);
	      return;
	  }
      } else {
	  // store userId into SessionState
	  try {
	      Session.setItemId(request, response, itemStr);
	  } catch (SSMException e) {
	      printError("Cannot write item id to SSM: "+e+"<br>",sp);
	      return;
	  }
      }

      // if user hasn't loged in, go to login page
      //
      // Integer userId =(Integer)session.getAttribute("USERID");
      Integer userId;
      try {
	  userId = Session.getUserId(request);
      } catch (SSMException e) {
	  printError("Cannot read user id from SSM: "+e+"<br>",sp);
	  return;
      }

      if ((userId == null) || (userId.intValue()<0)) {
	  try {
	      Session.goLogin(request,response,
			      "/servlet/edu.rice.rubis.beans.servlets.BuyNow");
	  } catch (Exception e) {
	      printError("Cannot go to Login page: "+e+"<br>",sp);
	  }
	  return ;
      }

      Context initialContext = null;
      try {
	  initialContext = new InitialContext();
      } catch (Exception e) {
	  printError("Cannot get initial context for JNDI: " + e+"<br>", sp);
	  return ;
      }

      SB_BuyNowHome buyHome;
      SB_BuyNow buy;
      String jndiName  = "SB_BuyNowHome";
      Object jndiValue = null;

      try {
	  jndiValue = initialContext.lookup(jndiName);
	  buyHome = (SB_BuyNowHome)PortableRemoteObject.narrow(jndiValue, SB_BuyNowHome.class);
	  buy = buyHome.create();
      } 
      catch (ClassCastException e) {
	  // Send service unavailable response to client
	  // since microreboot of SB_ViewItem is in progress.
	  sp.sendServiceUnavailable(jndiName, jndiValue);
	  return;
      }
      catch (Exception e) {
	  printError("Cannot lookup SB_BuyNow: " +e+"<br>", sp);
	  return ;
      }

     Integer itemId = new Integer(itemStr);
     String html=null;
     try {

	 /*
	   At this point, user must have loged in yet !
	   So, the following code is replaced by the below code

	 if ( userId != null && userId.intValue() > 0 ) {
	     // case of already loged in 
	     html = buy.getBuyNowForm(itemId, userId.intValue());
	 } else {
	     // case of log in now 
	     Object[] r = buy.getBuyNowForm(itemId, name, pass);
	     Integer uid = (Integer)r[0];
	     if (uid != null && uid.intValue()>0) {
		 session.setAttribute("USERID",uid);
	     }
	     html = (String)r[1];
	 }
	 */
	 html = buy.getBuyNowForm(itemId,userId.intValue());
     } catch (Exception e) {
	 printError("Cannot get Buy Now form: " +e+"<br>", sp);
     } 
	
     sp.printHTMLheader("RUBiS: Buy now");
     sp.printHTML(html);
     sp.printHTMLfooter();
  }
    
  /**
   * Call the doGet method
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }
}
