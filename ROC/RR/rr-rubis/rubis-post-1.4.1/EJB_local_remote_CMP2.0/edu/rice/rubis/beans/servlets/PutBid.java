package edu.rice.rubis.beans.servlets;

import java.io.IOException;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.rice.rubis.beans.SB_PutBid;
import edu.rice.rubis.beans.SB_PutBidHome;

import roc.rr.ssmutil.SSMException;

/** This servlets display the page allowing a user to put a bid
 * on an item.
 * It must be called this way :
 * <pre>
 * http://..../PutBid?itemId=xx&nickname=yy&password=zz
 *    where xx is the id of the item
 *          yy is the nick name of the user
 *          zz is the user password
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */


public class PutBid extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: PutBid");
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
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException, ServletException 
  {
      boolean authenticated = false;
      ServletPrinter sp = null;
      sp = new ServletPrinter(request, response, "PutBid");
      HttpSession session = request.getSession();

      String itemStr = request.getParameter("itemId");
      if ((itemStr == null) || (itemStr.equals(""))) {
	  // check itemId in session object
	  try {
	      itemStr = Session.getItemId(request);
	  } catch (SSMException e) {
	      printError("Cannot read item id from SSM: "+e,sp);
	      return;
	  }

	  if ( (itemStr == null)||(itemStr.equals("")) ) {
	      printError("Item id are required -- Cannot process the request<br>",sp);
	      return;
	  }
      } else {
	  // store itemId into SessionState
	  try {
	      Session.setItemId(request,response,itemStr);
	  } catch (SSMException e) {
	      printError("Cannot write item id to SSM: "+e,sp);
	      return;
	  }
      }
    
      Integer userId;
      try {
	  userId = Session.getUserId(request);
      } catch (SSMException e) {
	  printError("Cannot get user id from SSM: "+e,sp);
	  return;
      }
    
      if ( userId != null && userId.intValue() > 0 ) {
	  authenticated = true;
      } else {
	  // go to login page 
	  try {
	      Session.goLogin(request, response, "/servlet/edu.rice.rubis.beans.servlets.PutBid");
	  } catch (Exception e) {
	      printError("Cannot go to login page: "+e,sp);
	      return;
	  }
      }

    Context initialContext = null;
    try {
	initialContext = new InitialContext();
    } catch (Exception e) {
	printError("Cannot get initial context for JNDI: " + e+"<br>", sp);
	return ;
    }

    SB_PutBidHome putBidHome;
    SB_PutBid putBid ;
    String jndiName = "SB_PutBidHome";
    Object jndiValue = null;

    try {
	jndiValue = initialContext.lookup(jndiName);
	putBidHome = (SB_PutBidHome)PortableRemoteObject.narrow(jndiValue, SB_PutBidHome.class);
	putBid  = putBidHome.create();
    } 
    catch (ClassCastException e) {
        // Send service unavailable response to client
        // since microreboot of SB_ViewItem is in progress.
        sp.sendServiceUnavailable(jndiName, jndiValue);
        return;
    }
    catch (Exception e) {
	printError("Cannot lookup SB_PutBidNow: " +e+"<br>", sp);
	return ;
    }
    try {
	Integer itemId = new Integer(itemStr);
	String html = null;

	/*
	    At this point user already has loged in 
	*/

	html = putBid.getBiddingForm(itemId,userId.intValue());

	sp.printHTMLheader("RUBiS: PutBid");
	sp.printHTML(html);
	sp.printHTMLfooter();
    } catch (Exception e) {
	printError("This item does not exist (got exception: " +e+")<br>", sp);
	return ;
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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }
}
