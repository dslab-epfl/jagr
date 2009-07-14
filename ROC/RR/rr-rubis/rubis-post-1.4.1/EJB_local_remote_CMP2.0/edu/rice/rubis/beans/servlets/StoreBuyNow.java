package edu.rice.rubis.beans.servlets;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.rice.rubis.beans.SB_StoreBuyNow;
import edu.rice.rubis.beans.SB_StoreBuyNowHome;

import roc.rr.ssmutil.SSMException;

/** This servlet records a BuyNow in the database and display
 * the result of the transaction.
 * It must be called this way :
 * <pre>
 * http://..../StoreBuyNow?itemId=aa&userId=bb&minBuyNow=cc&maxQty=dd&BuyNow=ee&maxBuyNow=ff&qty=gg 
 *   where: aa is the item id 
 *          bb is the user id
 *          cc is the minimum acceptable BuyNow for this item
 *          dd is the maximum quantity available for this item
 *          ee is the user BuyNow
 *          ff is the maximum BuyNow the user wants
 *          gg is the quantity asked by the user
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class StoreBuyNow extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: StoreBuyNow");
    sp.printHTML("<h2>Your request has not been processed due to the following error :</h2><br>");
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
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doPost(request, response);
  }

  /**
   * Store the BuyNow to the database and display resulting message.
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
    Integer userId; // user id
    Integer itemId; // item id
    float   minBuyNow; // minimum acceptable BuyNow for this item
    float   BuyNow;    // user BuyNow
    float   maxBuyNow; // maximum BuyNow the user wants
    int     maxQty; // maximum quantity available for this item
    int     qty;    // quantity asked by the user
    String  value;

    sp = new ServletPrinter(request, response, "StoreBuyNow");

    /* Get and check all parameters */


    // extract user id from SSM
    try {
	userId = Session.getUserId(request);
    } catch (SSMException e) {
	printError("Cannot get user id from SSM: "+e,sp);
	return;
    }
    if ( userId == null || userId.intValue()<0 ) {
	//
	// go login instead of indicating error
	//  Feb/27/04 S.Kawamoto
	printError("<h3>ERROR: Your Session is no longer active, please login and re-enter your buy.<br></h3>",sp);
	return;
	//Session.goLogin(request,response,"/servlets/edu.rice.rubis.beans.servlets.StoreBuyNow");
    }
    
    // extract itemId from SSM
    try {
	value = Session.getItemId(request);
    } catch (SSMException e){
	printError("Cannot read item id from SSM: "+e,sp);
	return;
    }
    if (value == null || value.equals("")){
	printError("<h3>ERROR: Your Session is no longer active, please login and re-enter your buy.<br></h3>",sp);
	return;
    }
    itemId = new Integer(value);

    value = request.getParameter("maxQty");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a maximum quantity !<br></h3>", sp);
      return ;
    }
    else
    {
      Integer foo = new Integer(value);
      maxQty = foo.intValue();
    }

    value = request.getParameter("qty");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a quantity !<br></h3>", sp);
      return ;
    }
    else
    {
      Integer foo = new Integer(value);
      qty = foo.intValue();
    }

    /* Check for invalid values */

    if (qty > maxQty)
    {
      printError("<h3>You cannot request "+qty+" items because only "+maxQty+" are proposed !<br></h3>", sp);
      return ;
    }      

    try
    {
      initialContext = new InitialContext();
    } 
    catch (Exception e) 
    {
      printError("Cannot get initial context for JNDI: " + e+"<br>", sp);
      return ;
    }

    SB_StoreBuyNowHome buyHome;
    SB_StoreBuyNow buy;
    String jndiName = "SB_StoreBuyNowHome";
    Object jndiValue = null;

    try 
    {
	jndiValue = initialContext.lookup(jndiName);
	buyHome = (SB_StoreBuyNowHome)PortableRemoteObject.narrow(jndiValue,SB_StoreBuyNowHome.class);
	buy = buyHome.create();
    } 
    catch (ClassCastException e) {
        // Send service unavailable response to client
        // since microreboot of SB_ViewItem is in progress.
        sp.sendServiceUnavailable(jndiName, jndiValue);
        return;
    }
    catch (Exception e) {
	printError("Cannot lookup SB_StoreBuyNow: " +e+"<br>", sp);
	return ;
    }

    try 
    {
      buy.createBuyNow(itemId, userId, qty);
      sp.printHTMLheader("RUBiS: BuyNow result");
      if (qty == 1)
        sp.printHTML("<center><h2>Your have successfully bought this item.</h2></center>\n");
      else
        sp.printHTML("<center><h2>Your have successfully bought these items.</h2></center>\n");
    }
    catch (Exception e)
    {
      printError("Error while storing the BuyNow (got exception: " +e+")<br>", sp);
      return ;
    }
		
    sp.printHTMLfooter();
  }

}
