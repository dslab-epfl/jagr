package roc.loadgen.rubis;

import roc.loadgen.*;
import roc.loadgen.http.HttpResponse;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * A request generator for simulated RUBiS clients.
 *
 * @version <tt>$Revision: 1.9 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public class RequestGenerator
{
    private static Logger log = Logger.getLogger( "RequestGenerator" );
    Random rand;

    // Transition function for getting the next state
    private TransitionFunction transFunc;

    // Name of the server
    private String hostname;

    private static final boolean END_ACTION = true;  // last req in an action = commit point
    private static final boolean MID_ACTION = false; // req in the middle of an action

    public static final RubisUserRequest REQ_BACK  = new RubisUserRequest( null, MID_ACTION, null );
    public static final RubisUserRequest REQ_ABORT = new RubisUserRequest( null, END_ACTION, null );

    /**
     * Constructor.
     *
     * @param filename name of the workload file
     * @param serverName name of server hosting the target application
     */
    public RequestGenerator( String filename, String serverName )
	throws FileNotFoundException, IOException, NoSuchElementException, NumberFormatException
    {
	assert filename != null;
	assert serverName != null;

	transFunc = new TransitionFunction( filename );
	hostname = serverName;
	rand = new Random();
    }

    /**
     * Compute the next request for this simulated user.  If the
     * response to the previous request indicates failure, we go
     * straight to the first request in session (note that this is not
     * necessarily associated with session termination).
     *
     * @param prevRequest the request that has just been submitted previously
     * @param rcvdResponse the server's response to 'prevRequest'
     */
    public RubisUserRequest nextRequest( RubisUserRequest prevRequest, Response rcvdResponse )
    {
	assert prevRequest  != null;
	assert rcvdResponse != null;

	HttpResponse response = (HttpResponse) rcvdResponse;
	if( response.isError() )
	{
	    log.warn( "Response had error; going to HOME" );
	    return firstRequestInSession();
	}

	RubisUserState nextState = transFunc.nextState( prevRequest.getState() );
	
	if( nextState == TransitionFunction.HOME )	  return genHome( nextState );
	if( nextState == TransitionFunction.REGISTER )	  return genRegister( nextState );
	if( nextState == TransitionFunction.REG_USER )    return genRegisterUser( nextState );
	if( nextState == TransitionFunction.BROWSE )	  return genBrowse( nextState );
	if( nextState == TransitionFunction.BROWSE_CAT )  return genBrowseCategories( nextState );
	if( nextState == TransitionFunction.SEARCH_CAT )  return genSearchItemsInCategory( nextState, response );
	if( nextState == TransitionFunction.BROWSE_REG )  return genBrowseRegions( nextState );
	if( nextState == TransitionFunction.BR_CAT_REG )  return genBrowseCategoriesInRegion( nextState );
	if( nextState == TransitionFunction.SEARCH_REG )  return genSearchItemsInRegion( nextState, response );
	if( nextState == TransitionFunction.VIEW_ITEM )	  return genViewItem( nextState, response );
	if( nextState == TransitionFunction.VIEW_USER )	  return genViewUserInfo( nextState, response );
	if( nextState == TransitionFunction.VIEW_HIST )	  return genViewBidHistory( nextState, response );
	if( nextState == TransitionFunction.BUY_NOW )	  return genBuyNow( nextState, response );
	if( nextState == TransitionFunction.STORE_BUY )	  return genStoreBuyNow( nextState, response );
	if( nextState == TransitionFunction.PUT_BID )	  return genPutBid( nextState, response );
	if( nextState == TransitionFunction.STORE_BID )	  return genStoreBid( nextState, response );
	if( nextState == TransitionFunction.PUT_COMMENT ) return genPutComment( nextState, response );
	if( nextState == TransitionFunction.STORE_COMM )  return genStoreComment( nextState, response );
	if( nextState == TransitionFunction.SELECT_CAT )  return genSelectCategorySellItem( nextState );
	if( nextState == TransitionFunction.SELL_ITEM )	  return genSellItemForm( nextState );
	if( nextState == TransitionFunction.REG_ITEM )	  return genRegisterItem( nextState );
	if( nextState == TransitionFunction.ABOUT_ME )	  return genAboutMe( nextState );
	if( nextState == TransitionFunction.LOGIN )       return genLogin( nextState );
	if( nextState == TransitionFunction.LOGIN_USR )	  return genLoginUser( nextState );
	if( nextState == TransitionFunction.LOGOUT )      return genLogout( nextState );
	if( nextState == TransitionFunction.BACK)	  return REQ_BACK;
	if( nextState == TransitionFunction.ABANDON )     return REQ_ABORT;

	throw new RuntimeException( "Should never arrive here" );
    }

    /**
     * Generate the first request in a session.
     */
    public RubisUserRequest firstRequestInSession()
    {
	return genHome( TransitionFunction.HOME );
    }

    /**
     * Transform a target servlet into a fully qualified URL.
     *
     * @param servletNameAndArgs the desired servlet along with all arguments
     */
    private URL genUrl( String servletNameAndArgs )
    {
	try {
	    return new URL( "http", hostname, 8080, "/ejb_rubis_web/" + servletNameAndArgs );
	}
	catch( Exception e )
	{
	    e.printStackTrace();
	    throw new RuntimeException( "URL creation should not fail" );
	}
    }


    /**
     * URL generator methods.
     */

    /*-- Home --*/
    private RubisUserRequest genHome( RubisUserState state )
    {
	URL url = genUrl( "index.html" );
	RubisUserRequest ret = new RubisUserRequest( url, MID_ACTION, state );

        // XXX: TODO:  this will cause the CookieManagerInterceptor to clear its
        // cookies.  If we ever decide that a User will visit the homepage without
        // wanting to clear its cookies, we'll have to be a bit smarter here...
        //   -- Emre
        ret.setNewSession( true );

        return ret;
    }

    /*-- Register --*/
    private RubisUserRequest genRegister( RubisUserState state )
    {
	URL url = genUrl( "register.html" );
	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- RegisterUser --*/
    private RubisUserRequest genRegisterUser( RubisUserState state )
    {
	// Generate userID such as to not overlap with preconfigured userIDs
	int userId = Configuration.numUsersInDB + rand.nextInt(9999999) + 1;
	String firstName = "Great" + userId;
	String lastName  = "User"  + userId;
	String nickName  = "user"  + userId;
	String email     = userId + "@ebay.zz";
	String password  = "password" + userId;
	Vector regVec    = Configuration.regions;
	String region    = (String) regVec.elementAt( userId % regVec.size() );

	URL url = genUrl( "edu.rice.rubis.beans.servlets.RegisterUser" +
			  "?firstname=" + firstName + 
			  "&lastname="  + lastName  +
			  "&nickname="  + nickName  +
			  "&email="     + email     +
			  "&password="  + password  +
			  "&region="    + region );

	return new RubisUserRequest( url, END_ACTION, state ); // commit point
    }

    /*-- Browse --*/
    private RubisUserRequest genBrowse( RubisUserState state )
    {
	URL url = genUrl( "browse.html" );
	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- BrowseCategories --*/
    private RubisUserRequest genBrowseCategories( RubisUserState state )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.BrowseCategories" );
	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- SearchItemsInCategory --*/
    private RubisUserRequest genSearchItemsInCategory( RubisUserState state, HttpResponse response )
    {
	int    categoryId = randomCategoryId();
	String categoryName = categoryName( categoryId );

	URL url = genUrl( "edu.rice.rubis.beans.servlets.SearchItemsByCategory" +
			  "?category=" + (1 + categoryId) + 
			  "&categoryName=" + categoryName + 
			  "&page=" + extractPageNumber( response ) +
			  "&nbOfItems=" + numItemsPerPage() );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- BrowseRegions --*/
    private RubisUserRequest genBrowseRegions( RubisUserState state )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.BrowseRegions" );
	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- BrowseCategoriesInRegion --*/
    private RubisUserRequest genBrowseCategoriesInRegion( RubisUserState state )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.BrowseCategories" +
			  "?region=" + randomRegion() );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- SearchItemsInRegion --*/
    private RubisUserRequest genSearchItemsInRegion( RubisUserState state, HttpResponse response )
    {
	int    categoryId = randomCategoryId();
	String categoryName = categoryName( categoryId );
	
	URL url = genUrl( "edu.rice.rubis.beans.servlets.SearchItemsByRegion" +
			  "?region=" + randomRegionId() + 
			  "&category=" + (1 + categoryId) + 
			  "&categoryName=" + categoryName + 
			  "&page=" + extractPageNumber( response ) + 
			  "&nbOfItems=" + numItemsPerPage() );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- ViewItem --*/
    private RubisUserRequest genViewItem( RubisUserState state, HttpResponse response )
    {
         int itemId = extractItemId( response );

         if( itemId == -1 )
	 {
	     log.debug( "genViewItem: no item ID found; clicking Back" );
	     return REQ_BACK;
	 }
	 
	 URL url = genUrl( "edu.rice.rubis.beans.servlets.ViewItem" +
			   "?itemId=" + itemId );
	 
	 return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- ViewUserInfo --*/
    private RubisUserRequest genViewUserInfo( RubisUserState state, HttpResponse response )
    {

	int userId = extractIntWithMarker( response, "userId=" );

	if( userId == -1 )
	{
	    log.debug( "genViewUserInfo: no user ID found; clicking Back" );
	    return REQ_BACK;
	}
	 
	URL url = genUrl( "edu.rice.rubis.beans.servlets.ViewUserInfo" +
			  "?userId=" + userId );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- ViewBidHistory --*/
    private RubisUserRequest genViewBidHistory( RubisUserState state, HttpResponse response )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.ViewBidHistory" +
			  "?itemId=" + extractItemId( response ) );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- BuyNow --*/
    private RubisUserRequest genBuyNow( RubisUserState state, HttpResponse response )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.BuyNow" +
			  "?itemId=" + extractItemId( response ) );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- StoreBuyNow --*/
    private RubisUserRequest genStoreBuyNow( RubisUserState state, HttpResponse response )
    {
	int maxQty = extractIntWithMarker( response, "name=maxQty value=" );
	if( maxQty < 1 )
            maxQty = 1;
	
	URL url = genUrl( "edu.rice.rubis.beans.servlets.StoreBuyNow" +
			  "?itemId=" + extractItemId( response ) +
			  "&qty=" + ( 1 + rand.nextInt(maxQty) ) +
			  "&maxQty=" + maxQty );
	
	return new RubisUserRequest( url, END_ACTION, state );
    }

    /*-- PutBid --*/
    private RubisUserRequest genPutBid( RubisUserState state, HttpResponse response )
    {
	int itemId = extractItemId( response );
	if( itemId == -1 )
	{
	    log.debug( "genPutBid: no item ID found; clicking Back" );
	    return REQ_BACK;
	}

	URL url = genUrl( "edu.rice.rubis.beans.servlets.PutBid" +
			  "?itemId=" + itemId );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- StoreBid --*/
    private RubisUserRequest genStoreBid( RubisUserState state, HttpResponse response )
    {
	int maxQty = extractIntWithMarker( response, "name=maxQty value=" );
	if( maxQty < 1 )
            maxQty = 1;

	float bidIncrement = rand.nextInt(10) + 1;
	float minBid = extractFloatWithMarker( response, "name=minBid value=" );
	float myBid  = minBid + bidIncrement;
	float maxBid = myBid + bidIncrement;

	URL url = genUrl( "edu.rice.rubis.beans.servlets.StoreBid" +
			  "?itemId=" + extractItemId( response ) +
			  "&minBid=" + convertFloatToStringDatabaseFormat( minBid ) + 
			  "&maxQty=" + maxQty + 
			  "&bid=" + convertFloatToStringDatabaseFormat( myBid ) + 
			  "&maxBid=" + convertFloatToStringDatabaseFormat( maxBid ) + 
			  "&qty=" + ( 1 + rand.nextInt(maxQty) ));

	return new RubisUserRequest( url, END_ACTION, state );
    }

    /*-- PutComment --*/
    private RubisUserRequest genPutComment( RubisUserState state, HttpResponse response )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.PutComment" +
			  "?itemId=" + extractItemId( response ) +
			  "&to=" + extractIntWithMarker( response, "to=" ) );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- StoreComment --*/
    private RubisUserRequest genStoreComment( RubisUserState state, HttpResponse response )
    {
	final String[] comments = { "User is horrible, stay away from him<br>",
				    "User is below average, I don't recommend him<br>",
				    "User is average, I don't have an opinion<br>",
				    "User is above average, you can trust him<br>",
				    "User is excellent, trustworthy and great deals<br>" };

	int[]    ratings = { -5, -3, 0, 3, 5 };

	int remainingChars = 1 + rand.nextInt( Configuration.commentMaxLength );

	int idx = rand.nextInt( 5 );
	String comment = "";

	while( comments[idx].length() < remainingChars )
	{
            comment += comments[idx];
            remainingChars -= comments[idx].length();
	}
	comment += comments[idx].substring( 0, remainingChars );

	URL url = genUrl( "edu.rice.rubis.beans.servlets.StoreComment" +
			  "?itemId=" + extractItemId( response ) +
			  "&to=" + extractIntWithMarker( response, "name=to value=" ) + 
			  "&rating=" + ratings[idx] + 
			  "&comment=" + comment );

	return new RubisUserRequest( url, END_ACTION, state );
    }

    /*-- SelectCategorySellItem --*/
    private RubisUserRequest genSelectCategorySellItem( RubisUserState state )
    {
	/*
	  Dummy nickname and password are required for BrowseCategories 
	  when it called for sell an item. 
	URL url = genUrl( "edu.rice.rubis.beans.servlets.BrowseCategories" ); 
	*/
	URL url = genUrl( "edu.rice.rubis.beans.servlets.BrowseCategories?nickname=&password=" ); 

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- SellItemForm --*/
    private RubisUserRequest genSellItemForm( RubisUserState state )
    {
	int categoryId = rand.nextInt( Configuration.categories.size() );
	 
	URL url = genUrl( "edu.rice.rubis.beans.servlets.SellItemForm" +
			  "?category=" + (1 + categoryId) );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- RegisterItem --*/
    private RubisUserRequest genRegisterItem( RubisUserState state )
    {
	 // construct the item name
         int    totalItems = Configuration.totalActiveItems + Configuration.nbOfOldItems;
         int    i = totalItems + rand.nextInt(1000000) + 1; 
	 String itemName = "RUBiS automatically generated item " + i;

	 // construct the item description
         String description;
         final String protoDesc = "This incredible item is exactly what you need;<br>it has a lot of very nice features.<br>";
         int remainingChars = 1 + rand.nextInt( Configuration.itemMaxLength );
         for( description="" ; protoDesc.length() < remainingChars ; remainingChars -= protoDesc.length() )
	 {
	     description += protoDesc;
	 }
         description += protoDesc.substring( 0, remainingChars );

	 // compute initial price
         float initialPrice = 1 +  rand.nextInt( 5000 );

	 // compute reserve price (if any)
         float reservePrice;
         if( rand.nextInt(totalItems) >= Configuration.percentReservePrice * totalItems / 100 )
            reservePrice = 0;
         else
            reservePrice = rand.nextInt( 1000 ) + initialPrice;

	 // compute buy-now price (if any)
         float buyNow;
         if( rand.nextInt(totalItems) >= Configuration.percentBuyNow * totalItems / 100 )
            buyNow = 0;
         else
            buyNow = rand.nextInt( 1000 ) + initialPrice + reservePrice;

	 // compute duration of auction (in days)
         int duration = rand.nextInt( 7 ) + 1;

	 // compute the number of items put up for auction under this listing
	 int quantity = 1;
         if( rand.nextInt(totalItems) >= Configuration.percentUniqueItems * totalItems / 100 )
	     quantity += rand.nextInt( Configuration.maxItemQty );

	 // compute the category under which we list this item
	 int categoryId = randomCategoryId();

	 URL url = genUrl( "edu.rice.rubis.beans.servlets.RegisterItem" +
			   "?name=" + itemName +
			   "&description=" + description +
			   "&initialPrice=" + convertFloatToStringDatabaseFormat(initialPrice) +
			   "&reservePrice=" + convertFloatToStringDatabaseFormat(reservePrice) +
			   "&buyNow=" + convertFloatToStringDatabaseFormat(buyNow) + 
			   "&duration=" + duration + 
			   "&quantity=" + quantity +
			   "&categoryId=" + categoryId );
	 
	 return new RubisUserRequest( url, END_ACTION, state );
    }
    
    /*-- AboutMe --*/
    private RubisUserRequest genAboutMe( RubisUserState state )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.AboutMe" );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- Login --*/
    private RubisUserRequest genLogin( RubisUserState state )
    {
	URL url = genUrl( "login.html" );
	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- LoginUser --*/
    private RubisUserRequest genLoginUser( RubisUserState state )
    {
	// this.loggedIn = true;
	// cookieMgr.emptyCookieJar(); // no need for cookies prior to logging in...

	// create a new userID that is in range
	int userId = 1 + (new Random()).nextInt( Configuration.numUsersInDB );

	URL url = genUrl( "edu.rice.rubis.beans.servlets.Login" +
			  "?nickname=" + ( "user" + userId ) +
			  "&password=" + ( "password" + userId ) );

	return new RubisUserRequest( url, MID_ACTION, state );
    }

    /*-- Logout --*/
    private RubisUserRequest genLogout( RubisUserState state )
    {
	URL url = genUrl( "edu.rice.rubis.beans.servlets.Logout" );
	return new RubisUserRequest( url, END_ACTION, state );
    }

    //=========================================================================
    
    /**
    * Extract a page number from the HTML of a server response.  For
    * long lists of items, RUBiS has at the bottom of the page a
    * "Previous page" and a "Next page" link; these links have strings
    * of the form "&page=x" in them, where x indicates which page is
    * targetted.  If we see two page links in the HTML, we randomly
    * choose between the two; otherwise we return the only one we
    * found.  If no page link exists, then we return 0.
    *
    * @param resp server's response to this user's last request
    */
    private int extractPageNumber( HttpResponse response )
    {
	String html = response.getRespStr();

	final String MARKER = new String("&page=");
	
	if( html == null )
	{
	    log.debug( "extractPageNumber: HTML reply is empty; returning 0" );
	    return 0;
	}
	
	// find the string offset where the page number starts
	int idx_prev_page = html.indexOf( MARKER );
	if( idx_prev_page < 0 )
        {
	    log.debug( "extractPageNumber: did not find any page number" );
	    return 0;
	}
	
	int idx_next_page = html.indexOf( MARKER, idx_prev_page + MARKER.length() );
	if( idx_next_page < 0 )
	{
	    log.debug( "extractPageNumber: only found a previous-page link" );
	    return extractIntWithIndex( html, idx_prev_page + MARKER.length() );
	}

	// we found both previous-page and next-page; choose one at random
	if( rand.nextInt(100000) < 50000 )
	    return extractIntWithIndex( html, idx_prev_page + MARKER.length() );
	else
	    return extractIntWithIndex( html, idx_next_page + MARKER.length() );
    }

   /**
    * Extract an item ID from the HTML of a server response. If
    * several itemId entries are found, one of them is picked up
    * randomly.  If none is found, return -1.
    *
    * @param resp server's response to this user's last request
    */
    private int extractItemId( HttpResponse response )
    {
	String html = response.getRespStr();
	final String MARKER = new String("itemId=");
	
	if( html == null )
	{
	    log.debug( "extractItemId: HTML reply is empty; returning -1" );
	    return -1;
	}

	// count how many items we have in the page
	int numItems=0;
	
	for( int idx = html.indexOf( MARKER ) ; idx > -1 ;  )
	{
	    numItems++;
	    idx = html.indexOf( MARKER, idx + MARKER.length() );
	}

	if( numItems == 0 )
	{
	    log.debug( "extractItemId: did not find any occurrence of " + MARKER );
	    return -1;
	}

	// randomly choose one of the occurrences 
	int idx_start = html.indexOf( MARKER );
	int count = rand.nextInt( numItems );
	log.debug( "extractItemId: returning occurrence #" + (1+count) );
	for( ; count > 0 ; count-- )
	{
	    idx_start = html.indexOf( MARKER, idx_start + MARKER.length() );
	}

	return extractIntWithIndex( html, idx_start + MARKER.length() );
   }



    private String region( int index )
    {
	Vector regVec    = Configuration.regions;
	return (String) regVec.elementAt( index % regVec.size() );
    }

    private String randomRegion()
    {
	Vector regVec    = Configuration.regions;
	return (String) regVec.elementAt( rand.nextInt( regVec.size() ));
    }

    private int randomRegionId()
    {
	return rand.nextInt( Configuration.regions.size() );
    }

    private int randomCategoryId()
    {
	return rand.nextInt( Configuration.categories.size() );
    }

    private String categoryName( int idx )
    {
	return (String) Configuration.categories.elementAt( idx );
    }

    private int numItemsPerPage()
    {
	return Configuration.numItemsPerPage;
    }

    private int extractIntWithMarker( HttpResponse response, String marker )
    {
	return ((Integer) extractNumberWithMarker( Integer.class, response, marker )).intValue();
    }

    private float extractFloatWithMarker( HttpResponse response, String marker )
    {
	return ((Float) extractNumberWithMarker( Float.class, response, marker )).floatValue();
    }

    private int extractIntWithIndex( String html, int idx_start )
    {
	return ((Integer) extractNumberWithIndex( Integer.class, html, idx_start )).intValue();
    }

    private float extractFloatWithIndex( String html, int idx_start )
    {
	return ((Float) extractNumberWithIndex( Float.class, html, idx_start )).floatValue();
    }


    private Object extractNumberWithMarker( Class type, HttpResponse response, String marker )
    {
	String html = response.getRespStr();
	assert html != null;

	// Look for the marker
	int idx = html.indexOf( marker );
	if( idx < 0 )
	{
	    log.debug( "extractIntWithMarker: could not find marker" );
	    if( type == Integer.class )
		return new Integer(-1);
	    else if( type == Float.class )
		return new Float(-1);
	    else
		throw new RuntimeException( "Cannot convert to " + type );
	}

	return extractNumberWithIndex( type, html, idx + marker.length() );
    }


    private Object extractNumberWithIndex( Class type, String html, int idx_start )
    {
	assert html != null;
	int idx_end;

	// find where the number ends
	idx_end = biasedMin( Integer.MAX_VALUE, html.indexOf('\"', idx_start) );
	idx_end = biasedMin( idx_end,           html.indexOf( '?', idx_start) );
	idx_end = biasedMin( idx_end,           html.indexOf( '&', idx_start) );
	idx_end = biasedMin( idx_end,           html.indexOf( '>', idx_start) );
	
	// extract and return the number
	if( type == Integer.class )
	    return (Object) new Integer( html.substring( idx_start, idx_end ));
	else if( type == Float.class )
	    return (Object) new Float( html.substring( idx_start, idx_end ));
	else
	    throw new RuntimeException( "Don't know how to convert to " + type );
    }
    
    /**
     * Returns the min, unless second arg is negative, in which case
     * the first one is returned (although it's greater).
     */
    private int biasedMin( int a, int b )
    {
	assert a >= 0;
	
	if( b < 0 ) return a;
	
	return a<=b ? a : b;
    }
    
    /**
     * Convert a float into a format understandable by the database.
     * TODO: doesn't work well for large numbers
     */
    private String convertFloatToStringDatabaseFormat( float f )
    {
	String result = Float.toString(f);
	int E = result.indexOf( 'E' );
	if( E > -1)
	{
	    /* Convert something like 1.2345E6 to 1.2345E+6 */    
	    result = result.substring( 0, E+1 ) + 
		     "+" + 
		     result.substring( E+1 );
	}
	return result;
    }


}
