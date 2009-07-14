package edu.rice.rubis.beans.servlets;

import java.io.IOException;
import java.net.URLEncoder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.rice.rubis.beans.SB_SearchItemsByCategory;
import edu.rice.rubis.beans.SB_SearchItemsByCategoryHome;

/** This servlets displays a list of items belonging to a specific category.
 * It must be called this way :
 * <pre>
 * http://..../SearchItemsByCategory?category=xx&categoryName=yy 
 *    where xx is the category id
 *      and yy is the category name
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class SearchItemsByCategory extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: SearchItemsByCategory");
    sp.printHTML("<h2>We cannot process your request due to the following error :</h2><br>");
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
    Context initialContext = null;
    String categoryName;

    String  value = request.getParameter("category");
    Integer categoryId;
    Integer pageObject;
    Integer nbOfItemsObject;

    categoryName = request.getParameter("categoryName");
    sp = new ServletPrinter(request, response, "SearchItemsByCategory");
    if ((value == null) || (value.equals("")))
    {
      printError("You must provide a category identifier!<br>", sp);
      return ;
    }
    else
      categoryId = new Integer(value);

    value = request.getParameter("page");
    if ((value == null) || (value.equals("")))
      pageObject = new Integer(0);
    else
      pageObject = new Integer(value);

    value = request.getParameter("nbOfItems");
    if ((value == null) || (value.equals("")))
      nbOfItemsObject = new Integer(25);
    else
      nbOfItemsObject = new Integer(value);

    try
    {
      initialContext = new InitialContext();
    } 
    catch (Exception e) 
    {
      printError("Cannot get initial context for JNDI: " + e+"<br>", sp);
      return ;
    }

    // Connecting to Home thru JNDI
    SB_SearchItemsByCategoryHome home = null;
    SB_SearchItemsByCategory sb_SearchItemsByCategory = null;
    String jndiName  = "SB_SearchItemsByCategoryHome";
    Object jndiValue = null;
    String list = "";

    try {
	jndiValue = initialContext.lookup(jndiName);
	home = (SB_SearchItemsByCategoryHome)PortableRemoteObject.narrow(jndiValue, SB_SearchItemsByCategoryHome.class);
	sb_SearchItemsByCategory = home.create();
    } 
    catch (ClassCastException e) {
        // Send service unavailable response to client
        // since microreboot of SB_ViewItem is in progress.
        sp.sendServiceUnavailable(jndiName, jndiValue);
        return;
    }
    catch (Exception e) {
      printError("Cannot lookup SB_SearchItemsByCategory: " +e+"<br>", sp);
      return ;
    }


    if (categoryName == null)
    {
      sp.printHTMLheader("RUBiS: Missing category name");
      sp.printHTML("<h2>Items in this category</h2><br><br>");
    }
    else
    {
      sp.printHTMLheader("RUBiS: Items in category "+categoryName);
      sp.printHTML("<h2>Items in category "+categoryName+"</h2><br><br>");
    }

    int page = pageObject.intValue();
    int nbOfItems = nbOfItemsObject.intValue();

    try {
	list = sb_SearchItemsByCategory.getItems(categoryId, page, nbOfItems);
    } 
    catch (Exception e) {
	printError("Cannot get the list of items: " +e+"<br>", sp);
	return ;
    }

    try {
	if ((list != null) && (!list.equals(""))) {
	    sp.printItemHeader();
	    sp.printHTML(list); 
	    sp.printItemFooter();
	}
	else {
	    if (page == 0)
		sp.printHTML("<h2>Sorry, but there are no items available in this category !</h2>");
	    else {
		sp.printHTML("<h2>Sorry, but there are no more items available in this category !</h2>");
		// sp.printItemHeader();
		sp.printItemFooter("<a href=\"/ejb_rubis_web/servlet/edu.rice.rubis.beans.servlets.SearchItemsByCategory?category="+categoryId+
				   "&categoryName="+URLEncoder.encode(categoryName)+"&page="+(page-1)+"&nbOfItems="+nbOfItems+"\">Previous page</a>", "");
	    }
	    return ;
	}
	if (page == 0)
	    sp.printItemFooter("", "<a href=\"/ejb_rubis_web/servlet/edu.rice.rubis.beans.servlets.SearchItemsByCategory?category="+categoryId+
			       "&categoryName="+URLEncoder.encode(categoryName)+"&page="+(page+1)+"&nbOfItems="+nbOfItems+"\">Next page</a>");
	else
	    sp.printItemFooter("<a href=\"/ejb_rubis_web/servlet/edu.rice.rubis.beans.servlets.SearchItemsByCategory?category="+categoryId+
			       "&categoryName="+URLEncoder.encode(categoryName)+"&page="+(page-1)+"&nbOfItems="+nbOfItems+"\">Previous page</a>",
			       "<a href=\"/ejb_rubis_web/servlet/edu.rice.rubis.beans.servlets.SearchItemsByCategory?category="+categoryId+
			       "&categoryName="+URLEncoder.encode(categoryName)+"&page="+(page+1)+"&nbOfItems="+nbOfItems+"\">Next page</a>");
    } 
    catch (Exception e) {
	printError("Exception getting item list: " + e +"<br>", sp);
    }
		
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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }
}
