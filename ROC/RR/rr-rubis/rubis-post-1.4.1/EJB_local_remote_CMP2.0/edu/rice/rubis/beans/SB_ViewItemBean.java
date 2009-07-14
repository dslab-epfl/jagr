package edu.rice.rubis.beans;

import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;
import java.io.Serializable;
import javax.transaction.UserTransaction;
import java.util.Collection;
import java.util.Iterator;
import java.net.URLEncoder;

/**
 * This is a stateless session bean used to get the information about an item.
 *  
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.1
 */

public class SB_ViewItemBean implements SessionBean 
{
  protected SessionContext sessionContext;
  protected Context initialContext = null;
  private UserTransaction utx = null;

  /**
   * Get the full description of an item and the bidding option if userId>0.
   *
   * @param item an <code>Item</code> value
   * @param userId an authenticated user id
   */
  public String getItemDescription(Integer itemId, int userId) throws RemoteException
  {
    StringBuffer html = new StringBuffer();
    ItemLocalHome     iHome = null;
    ItemLocal         item = null;

    try 
    {
      iHome = (ItemLocalHome)initialContext.lookup("java:comp/env/ejb/Item");
      item = iHome.findByPrimaryKey(new ItemPK(itemId));
    } 
    catch (Exception e)
    {
      throw new RemoteException("Cannot lookup Item: " +e);
    }
    try
    {
      float   buyNow   = 0;
      int     nbOfBids = item.getNbOfBids();
      int     qty      = item.getQuantity();
      String  firstBid;
      float   maxBid   = item.getMaxBid();
      String itemName = item.getName();

      if (maxBid == 0)
      {
        firstBid = "none";
        maxBid = item.getInitialPrice();
        buyNow = item.getBuyNow();
      }
      else
      {
        if (qty > 1)
        {
          Collection list;

          try 
          {
            /* Get the qty max first bids and parse bids in this order
                until qty is reached. The bid that reaches qty is the
                current minimum bid. */
            list = item.getItemQtyMaxBid(qty, itemId);
            Iterator it = list.iterator();
            int         numberOfItems = 0;
            BidLocal         bid;
            while (it.hasNext()) 
            {
              bid = (BidLocal)it.next();
              numberOfItems += bid.getQuantity();
              if (numberOfItems >= qty)
              {
                 maxBid = bid.getBid();
                 break;
              }
            }
          } 
          catch (Exception e)
          {
            throw new RemoteException("Problem while computing current bid: " +e);
          }
        }

        Float foo = new Float(maxBid);
        firstBid = foo.toString();
      }
     
      if (userId>0)
      {
        //header = printHTMLheader("RUBiS: Bidding\n");
        html.append(printHTMLHighlighted("You are ready to bid on: "+itemName));
      }
      else
      {
        //header = printHTMLheader("RUBiS: Viewing "+itemName+"\n");
        html.append(printHTMLHighlighted(itemName));
      }
      html.append("<TABLE>\n"+
                  "<TR><TD>Currently<TD><b><BIG>"+maxBid+"</BIG></b>\n");
      // Check if the reservePrice has been met (if any)
      float   reservePrice = item.getReservePrice();
      Integer sellerId = item.getSellerId();
      if (reservePrice > 0)
      { // Has the reserve price been met ?
        if (maxBid >= reservePrice)
          html.append("(The reserve price has been met)\n");
        else
          html.append("(The reserve price has NOT been met)\n");
      }
      html.append("<TR><TD>Quantity<TD><b><BIG>"+qty+"</BIG></b>\n"+
                  "<TR><TD>First bid<TD><b><BIG>"+firstBid+"</BIG></b>\n"+
                  "<TR><TD># of bids<TD><b><BIG>"+nbOfBids+"</BIG></b> (<a href=\""+BeanConfig.context+"/servlet/edu.rice.rubis.beans.servlets.ViewBidHistory?itemId="+itemId+"\">bid history</a>)\n"+
                  "<TR><TD>Seller<TD><a href=\""+BeanConfig.context
		  +"/servlet/edu.rice.rubis.beans.servlets.ViewUserInfo?userId="
		  +sellerId+"\">"+item.getSellerNickname()+"</a> (<a href=\""
		  +BeanConfig.context
		  //
		  // go PutComment instead of PutCommentAuth
		  //  Feb/27/04 S.Kawamoto
		  //+"/servlet/edu.rice.rubis.beans.servlets.PutCommentAuth?to="
		  +"/servlet/edu.rice.rubis.beans.servlets.PutComment?to="
		  +sellerId+"&itemId="
		  +itemId+"\">Leave a comment on this user</a>)\n"+
                  "<TR><TD>Started<TD>"+item.getStartDate()+"\n"+
                  "<TR><TD>Ends<TD>"+item.getEndDate()+"\n"+
                  "</TABLE>");
      // Can the user buy this item now ?
      if (buyNow > 0)
	  //
	  // go BuyNow instead of BuyNowAuth  Feb/27/04 S.Kawamoto
	  //html.append("<p><a href=\""+BeanConfig.context+"/servlet/edu.rice.rubis.beans.servlets.BuyNowAuth?itemId="+itemId+"\">"+
	  html.append("<p><a href=\""+BeanConfig.context+"/servlet/edu.rice.rubis.beans.servlets.BuyNow?itemId="+itemId+"\">"+
		      "<IMG SRC=\""+BeanConfig.context+"/buy_it_now.jpg\" height=22 width=150></a>"+
                    "  <BIG><b>You can buy this item right now for only $"+buyNow+"</b></BIG><br><p>\n");

      if (userId<=0)
      {
	  // putBidAuth is replace by putBid
	  //html.append("<a href=\""+BeanConfig.context+"/servlet/edu.rice.rubis.beans.servlets.PutBidAuth?itemId="+itemId+"\"><IMG SRC=\""+BeanConfig.context+"/bid_now.jpg\" height=22 width=90> on this item</a>\n");
        html.append("<a href=\""+BeanConfig.context+"/servlet/edu.rice.rubis.beans.servlets.PutBid?itemId="+itemId+"\"><IMG SRC=\""+BeanConfig.context+"/bid_now.jpg\" height=22 width=90> on this item</a>\n");
      }

      html.append(printHTMLHighlighted("Item description"));
      html.append(item.getDescription());
      html.append("<br><p>\n");

      if (userId>0)
      {
        html.append(printHTMLHighlighted("Bidding"));
        float minBid = maxBid+1;
        html.append("<form action=\""+BeanConfig.context+"/servlet/edu.rice.rubis.beans.servlets.StoreBid\" method=POST>\n"+
                  "<input type=hidden name=minBid value="+minBid+">\n"+
	       // "<input type=hidden name=userId value="+userId+">\n"+
                  "<input type=hidden name=itemId value="+itemId+">\n"+
                  "<input type=hidden name=maxQty value="+qty+">\n"+
                  "<center><table>\n"+
                  "<tr><td>Your bid (minimum bid is "+minBid+"):</td>\n"+
                  "<td><input type=text size=10 name=bid></td></tr>\n"+
                  "<tr><td>Your maximum bid:</td>\n"+
                  "<td><input type=text size=10 name=maxBid></td></tr>\n");
        if (qty > 1)
          html.append("<tr><td>Quantity:</td>\n"+
                    "<td><input type=text size=5 name=qty></td></tr>\n");
        else
          html.append("<input type=hidden name=qty value=1>\n");
        html.append("</table><p><input type=submit value=\"Bid now!\"></center><p>\n");
      }
    }
    catch (RemoteException re)
    {
      throw new RemoteException("Unable to print Item description (exception: "+re+")<br>\n");
    }
    return html.toString();
  }

  /**
   * Construct a html highlighted string.
   * @param msg the message to display
   * @return a string in html format
   * @since 1.1
   */
  public String printHTMLHighlighted(String msg)
  {
    return "<TABLE width=\"100%\" bgcolor=\"#CCCCFF\">\n<TR><TD align=\"center\" width=\"100%\"><FONT size=\"4\" color=\"#000000\"><B>"+msg+"</B></FONT></TD></TR>\n</TABLE><p>\n";
  }


  // ======================== EJB related methods ============================

  /**
   * This method is empty for a stateless session bean
   */
  public void ejbCreate() throws CreateException, RemoteException
  {
  }

  /** This method is empty for a stateless session bean */
  public void ejbActivate() throws RemoteException {}
  /** This method is empty for a stateless session bean */
  public void ejbPassivate() throws RemoteException {}
  /** This method is empty for a stateless session bean */
  public void ejbRemove() throws RemoteException {}


  /** 
   * Sets the associated session context. The container calls this method 
   * after the instance creation. This method is called with no transaction context. 
   * We also retrieve the Home interfaces of all RUBiS's beans.
   *
   * @param sessionContext - A SessionContext interface for the instance. 
   * @exception RemoteException - Thrown if the instance could not perform the function 
   *            requested by the container because of a system-level error. 
   */
    public void setSessionContext(SessionContext sessionContext) 
	throws RemoteException {
	this.sessionContext = sessionContext;
	try {
	    initialContext = new InitialContext();  
	} catch (Exception e) {
	    throw new RemoteException("Cannot get JNDI InitialContext: "+e);
	}
    }
}
