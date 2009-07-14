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

import edu.rice.rubis.beans.SB_BrowseCategories;
import edu.rice.rubis.beans.SB_BrowseCategoriesHome;

import roc.rr.ssmutil.SSMException;

/**
 * Builds the html page with the list of all categories and provides links to browse all
 * items in a category or items in a category for a given region
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class BrowseCategories extends HttpServlet
{
 
  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: Browse Categories");
    sp.printHTML("<h3>Your request has not been processed due to the following error :</h3><br>");
    sp.printHTML(errorMsg);
    sp.printHTMLfooter();
  }

  /**
   * Build the html page for the response
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    ServletPrinter sp = null;
    String  region;
    String  username=null, password=null;
    Context initialContext = null;
    int     uid=-1;

    sp = new ServletPrinter(request, response, "BrowseCategories");

    try
    {
      initialContext = new InitialContext();
    } 
    catch (Exception e) 
    {
      printError("Cannot get initial context for JNDI: " +e+"<br>", sp);
      return ;
    }

    region = request.getParameter("region");
    username = request.getParameter("nickname");
    password = request.getParameter("password");

    /* 
       BrowseCategories has two mode, browse and sell.
       If the parameters of BrowseCategories include username 
       and password, this request is a sell mode. If not, 
       it is a browse mode.

       In case of a sell mode, check login status and 
       if not loged in, go login screen 
   
       Mar/02/04 S.Kawamoto
    */

    if ( username != null && password != null ) {
	Integer userId;
	try {
	    userId = Session.getUserId(request);
	} catch (SSMException e) {
	    printError("Cannot read user id from SSM: "+e+"<br>",sp);
	    return;
	}
	if ( userId == null || userId.intValue() < 0 ){
	    try {
		Session.goLogin(request,response,
				"/servlet/edu.rice.rubis.beans.servlets.BrowseCategories?nickname=&password=");
	    } catch (Exception e) {
		printError("Cannot go to login page: "+e+"<br>",sp);
	    }
	    return;
	} else {
	    uid = userId.intValue();
	}
    }

    // Connecting to Home thru JNDI
    SB_BrowseCategoriesHome home = null;
    SB_BrowseCategories sb_browseCategories = null;
    String jndiName  = "SB_BrowseCategoriesHome";
    Object jndiValue = null;

    try {
	jndiValue =initialContext.lookup(jndiName);
	home = (SB_BrowseCategoriesHome)PortableRemoteObject.narrow(jndiValue, SB_BrowseCategoriesHome.class);
	sb_browseCategories = home.create();
    }
    catch (ClassCastException e) {
	// Send service unavailable response to client
	// since microreboot of SB_BrowseCategoriesHome is in progress.
	sp.sendServiceUnavailable(jndiName, jndiValue);
	return;
    }
    catch (Exception e) {
	sp.printHTMLheader("RUBiS available categories");
	sp.printHTML("<h2>Currently available categories</h2><br>");
	printError("Cannot lookup SB_BrowseCategories: " +e+"<br>", sp);
	return ;
    }

    String list;
    try {
	/* 
	   In case of sell mode, pass uid (integer value) to 
	   getCategories method instead of nickname and password.
	*/
	list = sb_browseCategories.getCategories(region, uid);
    } catch (Exception e) {
	sp.printHTMLheader("RUBiS available categories");
	sp.printHTML("<h2>Currently available categories</h2><br>");
	printError("Cannot get the list of categories: " +e+"<br>", sp);
	return ;
    }

    sp.printHTMLheader("RUBiS available categories");
    sp.printHTML("<h2>Currently available categories</h2><br>");
    sp.printHTML(list); 	
    sp.printHTMLfooter();
  }

  /**
   * Same as <code>doGet</code>.
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
