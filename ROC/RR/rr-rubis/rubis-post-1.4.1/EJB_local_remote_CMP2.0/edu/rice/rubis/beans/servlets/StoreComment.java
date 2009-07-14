package edu.rice.rubis.beans.servlets;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.rice.rubis.beans.SB_StoreComment;
import edu.rice.rubis.beans.SB_StoreCommentHome;

import roc.rr.ssmutil.SSMException;

/** This servlets records a comment in the database and display
 * the result of the transaction.
 * It must be called this way :
 * <pre>
 * http://..../StoreComment?itemId=aa&userId=bb&minComment=cc&maxQty=dd&comment=ee&maxComment=ff&qty=gg 
 *   where: aa is the item id 
 *          bb is the user id
 *          cc is the minimum acceptable comment for this item
 *          dd is the maximum quantity available for this item
 *          ee is the user comment
 *          ff is the maximum comment the user wants
 *          gg is the quantity asked by the user
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class StoreComment extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: StoreComment");
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
   * Store the comment to the database and display the resulting message.
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

    Integer toId;    // to user id
    Integer fromId;  // from user id
    Integer itemId;  // item id
    String  comment; // user comment
    Integer rating;  // user rating
    String  value;

    sp = new ServletPrinter(request, response, "StoreComment");

    /* Get and check all parameters */

    // extract TO from SSM
    try {
	value = Session.getTo(request);
    } catch (SSMException e) {
	printError("Cannot get user id for comment: "+e,sp);
	return;
    }
    if (value==null || value.equals("")){
	printError("<h3>ERROR: Your Session is no longer active, please login and re-enter your comment.",sp);
	return;
    }
    toId = new Integer(value);

    // From is a user who is putting a comment on.
    // Extract userId from SSM
    try {
	fromId = Session.getUserId(request);
    } catch (SSMException e){
	printError("Cannot read user id from SSM: "+e,sp);
	return;
    }

    if (fromId == null || fromId.intValue()<0 ) {
      printError("<h3>ERROR: Your Session is no longer active, please login and re-enter your comment.<br></h3>", sp);
      return ;
    }
    
    // Extract itemId from SSM
    try {
	value = Session.getItemId(request);
    } catch (SSMException e){
	printError("Cannot read item id from SSM: "+e,sp);
	return;
    }
    if (value==null || value.equals("")){
	printError("<h3>ERROR: Your Session is no longer active, please login and re-enter your comment.</h3>",sp);
	return;
    }
    itemId = new Integer(value);


    // Extract rating from request parameter
    value = request.getParameter("rating");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a rating !<br></h3>", sp);
      return ;
    }
    else
      rating = new Integer(value);

    // Extract comment from request parameter
    comment = request.getParameter("comment");
    if ((comment == null) || (comment.equals("")))
    {
      printError("<h3>You must provide a comment !<br></h3>", sp);
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
    SB_StoreCommentHome scHome;
    SB_StoreComment sb_StoreComment;
    String jndiName  = "SB_StoreCommentHome";
    Object jndiValue = null;

    try 
    {
	jndiValue = initialContext.lookup(jndiName);
	scHome = (SB_StoreCommentHome)PortableRemoteObject.narrow(jndiValue,SB_StoreCommentHome.class);
	sb_StoreComment = scHome.create();
    } 
    catch (ClassCastException e) {
        // Send service unavailable response to client
        // since microreboot of SB_ViewItem is in progress.
        sp.sendServiceUnavailable(jndiName, jndiValue);
        return;
    }
    catch (Exception e) {
	printError("Cannot lookup SB_StoreComment: " +e+"<br>", sp);
	return ;
    }
    try
    {
      sb_StoreComment.createComment(fromId, toId, itemId, rating.intValue(), comment);
      sp.printHTMLheader("RUBiS: Comment posting");
      sp.printHTML("<center><h2>Your comment has been successfully posted.</h2></center>\n");
      sp.printHTMLfooter();
    }
    catch (Exception e)
    {
      printError("Error while storing the comment (got exception: " +e+")<br>", sp);
    }

  }

}
