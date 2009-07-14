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

import edu.rice.rubis.beans.SB_PutComment;
import edu.rice.rubis.beans.SB_PutCommentHome;

import roc.rr.ssmutil.SSMException;

/** This servlets display the page allowing a user to put a comment
 * on an item.
 * It must be called this way :
 * <pre>
 * http://..../PutComment?to=ww&itemId=xx&nickname=yy&password=zz
 *    where ww is the id of the user that will receive the comment
 *          xx is the item id
 *          yy is the nick name of the user
 *          zz is the user password
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */


public class PutComment extends HttpServlet
{

  private void printError(String errorMsg, ServletPrinter sp)
  {
    sp.printHTMLheader("RUBiS ERROR: PutComment");
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
    ServletPrinter sp = null;
    String toStr = request.getParameter("to");
    String itemStr = request.getParameter("itemId");
    sp = new ServletPrinter(request, response, "PutComment");

    // If haven't loged in, then go login page 

    // If toStr is null then indicate error else store it in session object
    //  Mar/3/04 S.Kawamoto
    if (toStr==null || toStr.equals("")) {
	try {
	    toStr = Session.getTo(request);
	} catch (SSMException e) {
	    printError("Cannot read user id for comment from SSM: "+e,sp);
	    return;
	}
	if (toStr==null || toStr.equals("")) {
	    printError("To is required - Cannot process the request<br>", sp);
	    return ;
	}
    } else {
	// Store to into SessionState 
	try {
	    Session.setTo(request,response,toStr);
	} catch (SSMException e) {
	    printError("Cannot write user id for comment to SSM: "+e,sp);
	    return;
	}
    }

    // If itemStr is null then it should be stored in session object
    // Else indicate error.
    //  Mar/3/04 S.Kawamoto
    if (itemStr==null || itemStr.equals("")){
	try {
	    itemStr = Session.getItemId(request);
	} catch (SSMException e) {
	    printError("Cannot get item id from SSM: "+e,sp);
	    return;
	}
	if (itemStr==null || itemStr.equals("")){
	    printError("ItemId is required - Cannot process the request<br>", sp);
	    return;
	}
    } else {
	// Store itemStr into SessionState
	try {
	    Session.setItemId(request,response,itemStr);
	} catch (SSMException e) {
	    printError("Cannot set item id to SSM: "+e,sp);
	    return;
	}
    }


    // If user hasn't loged in yet then go to login page
    //  Mar/3/04 S.Kawamoto
    Integer userId;
    try {
	userId = Session.getUserId(request);
    } catch (SSMException e) {
	printError("Cannot get user id from SSM: "+e,sp);
	return;
    }
	
    if (userId==null || userId.intValue()<0){
	try {
	    Session.goLogin(request,response,"/servlet/edu.rice.rubis.beans.servlets.PutComment");
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

    // Connecting to Home thru JNDI
    SB_PutCommentHome home = null;
    SB_PutComment sb_PutComment = null;
    Integer toId = new Integer(toStr);
    Integer itemId = new Integer(itemStr);
    String jndiName = "SB_PutCommentHome";
    Object jndiValue = null;

    try {
	jndiValue = initialContext.lookup(jndiName);
	home = (SB_PutCommentHome)PortableRemoteObject.narrow(jndiValue,SB_PutCommentHome.class);
	sb_PutComment = home.create();
    } 
    catch (ClassCastException e) {
        // Send service unavailable response to client
        // since microreboot of SB_ViewItem is in progress.
        sp.sendServiceUnavailable(jndiName, jndiValue);
        return;
    }
    catch (Exception e) {
	printError("Cannot lookup SB_PutComment: " +e+"<br>", sp);
	return ;
    }

    String html;
    try {
	/*
	  At this point User must has loged in yet.
	  So the following code is replaced as below.
	*/
	html = sb_PutComment.getCommentForm(itemId, toId, userId.intValue());
    } catch (Exception e){
	printError("Cannot get the html form: " +e+"<br>", sp);
	return ;
    }
	
    // Display the comment form
    sp.printHTMLheader("RUBiS: Comment service");
    sp.printHTML(html);
    sp.printHTML("<tr><td><b>Rating</b>\n"+
		 "<td><SELECT name=rating>\n"+
		 "<OPTION value=\"5\">Excellent</OPTION>\n"+
		 "<OPTION value=\"3\">Average</OPTION>\n"+
		 "<OPTION selected value=\"0\">Neutral</OPTION>\n"+
		 "<OPTION value=\"-3\">Below average</OPTION>\n"+
		 "<OPTION value=\"-5\">Bad</OPTION>\n"+
		 "</SELECT></table><p><br>\n"+
		 "<TEXTAREA rows=\"20\" cols=\"80\" name=\"comment\">Write your comment here</TEXTAREA><br><p>\n"+
		 "<input type=submit value=\"Post this comment now!\"></center><p>\n");
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
