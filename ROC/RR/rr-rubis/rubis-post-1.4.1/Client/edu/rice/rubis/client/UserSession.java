/* $Id: UserSession.java,v 1.30 2004/05/26 02:47:44 fjk Exp $ */

package edu.rice.rubis.client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class UserSession extends Thread
{
   public static int instanceDown = -1;
   public static ArrayList hostList = new ArrayList();

   private static FailureReporter reporter;
   private RUBiSProperties rubis = null;         // access to rubis.properties file
   private URLGenerator    urlGen = null;        // URL generator corresponding to the version to be used (PHP, EJB or Servlets)
   private TransitionTable transition = null;    // transition table user for this session
   private String          lastHTMLReply = null; // last HTML reply received from 
   private Random          userRand;          // a private random number generator for each user session
   private int             userId;               // User id for the current session
   private String          username = null;      // User name for the current session
   private String          password = null;      // User password for the current session
   private URL             lastURL = null;       // Last accessed URL
   private int             lastItemId = -1;      // This is to deal with back because the itemId cannot be retrieved from the current page
   private int             lastUserId = -1;      // This is to deal with back because the itemId cannot be retrieved 
   private int             debugLevel = 0;       // 0 = no debug message, 1 = just error messages, 2 = error messages+HTML pages, 3 = everything!
   private UserData        stats=null;        // each user session profiles the responses it gets from the server
   private long            userStartTime=0;
   private int             threadId = 0;
   private static PrintStream report=null;
   private static PrintStream trace=null;
   private boolean         loggedIn = false;     // Whether or not we are logged in (from the client's perspective). Only 
                                                 // meaningful if the property httpd_has_session_state is true in RUBiSProperties.  
   private CookieManager   cookieMgr = null;     // Manages cookies for this session
   private boolean         dropSessionStateRandomly = false; // If set to true, cookies are cleared with a certain probability
                                                             // at each step; this is to test how the client reacts when
                                                             // session state is unexpectedly lost.
   private int             ignoreCount=0;  // Don't count the first 'ignoreCount' operations in a session when doing stats
   private int             numUser=-1;     // an index showing how many users this thread has emulated so far (minus 1)

   public UserSession(int threadId,          // identifier for this particular user thread
                      URLGenerator URLGen,   // gives us a URL based on the state we want to go to
                      RUBiSProperties RUBiS, // the parameters for this experiment
                      UserData stats,        // place to collect statistics
                      long randSeed,         // seed for this user's random number generator
                      PrintStream report,    // place to dump report information
                      PrintStream trace)     // place to dump session trace information
   {
      super( "UserSession" + threadId );
      this.report = report;
      this.trace = trace;
      this.threadId = threadId;
      this.urlGen = URLGen;
      this.rubis  = RUBiS;
      this.reporter = new FailureReporter( this.rubis );
      this.stats = stats;
      this.userStartTime = System.currentTimeMillis();
      this.debugLevel = rubis.getMonitoringDebug();

      // Seed random number generator with controlled seed, so we can reproduce results later
      this.userRand = new Random(randSeed);


      transition = new TransitionTable(rubis.getNbOfColumns(), rubis.getNbOfRows(), rubis.useTPCWThinkTime(), randSeed, report);
      if (!transition.ReadExcelTextFile(rubis.getTransitionTable()))
         Runtime.getRuntime().exit(1);

      this.cookieMgr = new CookieManager(this);
   }

   /**
    * Get the number of times to attempt a NET request.
    *
    * @param  idempotent       true, if request is idempotent, false otherwise
    * @return <code>int</code> number of attempts
    *
    */
   private int getNumAttemptsNet( boolean idempotent )
   {
       if ( idempotent )
	   return rubis.getMaxNETConnectAttempts();
       else
	   return 1;
   }


   /**
    * Get the number of times to attempt an HTTP request.
    *
    * @param  idempotent       true, if request is idempotent, false otherwise
    * @return <code>int</code> number of attempts
    *
    */
   private int getNumAttemptsHttp( boolean idempotent )
   {
       if ( idempotent )
	   return rubis.getMaxHTTPReadAttempts();
       else
	   return 1;
   }


   /**
    * Get the number of times to attempt an HTML request.
    *
    * @param  idempotent       true, if request is idempotent, false otherwise
    * @return <code>int</code> number of attempts
    *
    */
   private int getNumAttemptsHtml( boolean idempotent )
   {
       if ( idempotent )
	   return rubis.getMaxHTMLReadAttempts();
       else
	   return 1;
   }


   /**
    * Call the HTTP Server according to the given URL and get the reply
    *
    * @param url        URL to access
    * @param idempotent true, if this request is idempotent, false otherwise
    * @return <code>String</code> containing the web server reply (HTML file). Return null if unable to connect or unable to read the http data.
    *
    */
   private String callHTTPServer ( URL url, boolean idempotent )
   {
      HttpURLConnection   conn;
      BufferedInputStream in = null;
      String              HTMLReply = "";

      int                 netConnectAttemptsLeft;  // # of remaining attempts to connect to a given URL before aborting user action
      int                 httpReadAttemptsLeft;    // # of remaining attempts to read http data from a given URL before aborting user action

      log( "callHTTPServer", "start", 0 );

      netConnectAttemptsLeft = getNumAttemptsNet( idempotent );
      httpReadAttemptsLeft   = getNumAttemptsHttp( idempotent );

      //
      // Try to connect to HTTP server and read the response
      //
      while (netConnectAttemptsLeft > 0 && httpReadAttemptsLeft > 0)
      {
         conn = null;
	 in = null;
         try
         {
	     conn = (HttpURLConnection)(url.openConnection());

	     // Open the connection to the HTTP server, sending & receiving cookies.
	     cookieMgr.connect(conn);

	     // Now call the actual URL
	     in = new BufferedInputStream(conn.getInputStream(), 4096); 

	     // Read the HTTP response
	     int    read;
	     byte[] buffer = new byte[4096];
	     while ((read = in.read(buffer, 0, buffer.length)) != -1)
             {
		 if (read > 0) 
		     HTMLReply = HTMLReply + new String(buffer, 0, read);
             }
	     break; // Successful connection and read; break out of retry while loop
         }
         catch (IOException ioe) 
         {
	     int       httpResponseCode;
	     int       timeToSleep;

	     // Get the response code in the http reply (if any)
	     try {
		 httpResponseCode = conn.getResponseCode();
	     } catch (IOException responseCodeIoe) {
		 httpResponseCode = -1;
	     }

	     if (conn == null || httpResponseCode < 300) {
		 // Either we couldn't connect, or we got a successful http response but then 
		 // got an error later. These are considered "network connection" failures.
		 netConnectAttemptsLeft--;
		 
		 // If we're going to try again, record this failed attempt.
		 if ( netConnectAttemptsLeft > 0  &&  ignoreCount <= 0 )
		     stats.recordFailedAttempt();

		 // Since this is a network error, reset our max consecutive HTTP attempts.
		 httpReadAttemptsLeft = getNumAttemptsHttp( idempotent );

		 log( "callHTTPServer", "Error: Network error accessing URL " + url + " (" + ioe.getMessage() + "); " + 
		      netConnectAttemptsLeft + " network connection attempts remaining", 0);

		 timeToSleep = rubis.getMsBetweenNETConnectAttempts();
	         if ( rubis.getProperty("send_netlevel_error").equals("yes") )
		     reporter.send( url, "NET" );
	     } else {
		 // We got an http response code of 300 or above; this is considered an http read failure.
		 httpReadAttemptsLeft--;

		 // If we're going to try again, record this failed attempt.
		 if ( httpReadAttemptsLeft > 0  &&  ignoreCount <= 0 )
		     stats.recordFailedAttempt();

		 // Since this is an http error, reset our max consecutive network connection attempts.
		 netConnectAttemptsLeft = getNumAttemptsNet( idempotent );

		 log( "callHTTPServer", "Error: Got bad HTTP response code " + httpResponseCode
		      + " accessing URL " + url + ". IO Exception text: \"" + ioe.getMessage() + "\". " 
		      + httpReadAttemptsLeft + " http read attempts remaining", 0);

		 timeToSleep = rubis.getMsBetweenHTTPReadAttempts();
	         if ( rubis.getProperty("send_httplevel_error").equals("yes") )
		     reporter.send( url, "HTTP" );
	     }

	     if (netConnectAttemptsLeft > 0 && httpReadAttemptsLeft > 0){
		 if (timeToSleep > 0) {
		     try {
			 Thread.currentThread().sleep(timeToSleep);
		     } catch (InterruptedException i) {
			 log( "callHTTPServer", "sleep interrupted, returning null", 1 );
			 return null;
		     }
		 }
	     }
	 }
	 finally {
	     // Close the input stream (if it's been opened)
	     try {
		 if (in != null)
		     in.close();
	     } catch (IOException ioe) {
		 if (debugLevel>0)
		     System.err.println("Thread "+this.getName()+": Unable to close URL "+url+" ("+ioe.getMessage()+")<br>");
	     }
	 }
      }

      if (netConnectAttemptsLeft <= 0) {
	  log( "callHTTPServer", "Attempted to connect to URL " + url + " "
	       + getNumAttemptsNet( idempotent )
	       + " times to no avail, returning null", 1);
	  return null;
      } else if (httpReadAttemptsLeft <= 0) {
	  log( "callHTTPServer", "Attempted to get good HTTP response (code >= 300) from URL " + url + " "
	       + getNumAttemptsHttp( idempotent )
	       + " times to no avail, returning null", 1);
	  return null;
      }

      // Look for any image to download
      Vector images = new Vector();
      int index = HTMLReply.indexOf("<IMG SRC=\"");
      while (index != -1)
      {
         int startQuote = index + 10; // 10 equals to length of <IMG SRC"
         int endQuote = HTMLReply.indexOf("\"", startQuote+1);
         images.add(HTMLReply.substring(startQuote, endQuote));
         index = HTMLReply.indexOf("<IMG SRC=\"", endQuote);
      }
    
      // Download all images
      byte[] buffer = new byte[4096];
      while (images.size() > 0)
      {
         URL imageURL = urlGen.genericHTMLFile((String)images.elementAt(0), url.getHost());
         try
         {
            BufferedInputStream inImage = new BufferedInputStream(imageURL.openStream(), 4096);
            while (inImage.read(buffer, 0, buffer.length) != -1); // Just download, skip data
            inImage.close();
         }          
         catch (IOException ioe) 
         {
            // At this point we don't count failed image reads as bad/failed responses
            log( "callHTTPServer", "Error while downloading image "+imageURL+" ("+ioe.getMessage()+")", 2);
         }
         images.removeElementAt(0);
      }

      log( "callHTTPServer", "returning OK", 0 );
      return HTMLReply;
   }

  /**
   * Rewrite specified request url with specified host address.
   *
   * @param url request url to be modifiedl
   * @param host host address which will override.
   *
   * @return returns modified url.
   */
   private URL rewriteURL(URL url, InetAddress host) throws MalformedURLException
   {
       String protocol = url.getProtocol();
       int port = url.getPort();
       String file = url.getFile();
       return new URL(protocol, host.getHostName(), port, file);
  }

  /**
   * if current target Host is during reboot, then choose another one.
   *
   * @param url original target url
   * @param doCast if true, direct request to failed node to dummy node.
   */
   private URL validateURL(URL url, boolean doCast)
   {
       URL retURL = null;
       
       if ( instanceDown >= 0 )
       {
	   int indexOfHost = 0;
	   indexOfHost = hostList.indexOf(urlGen.getHost());
	
	   if ( (instanceDown & (1 << indexOfHost) ) != 0 )
	   {
	       //do not count dummyHost.
	       int practicalHostListSize = hostList.size() - 1;
	       if (doCast) 
	       {
		   //index is dummyHost
		   indexOfHost = practicalHostListSize;
	       }
	       else
	       {
		   int maskBit = ( 1 <<  practicalHostListSize ) - 1;
		   int instanceUp = maskBit ^ instanceDown;
		   if ( instanceUp == 0 ) 
		   {
		       //all instance is down. index is dummyHost
		       indexOfHost = practicalHostListSize;
		   } 
		   else 
		   {
		       for ( int i = 0; i < practicalHostListSize; i++ ) 
		       {
			   indexOfHost++;
			   if ( indexOfHost == practicalHostListSize ) 
			       indexOfHost = 0;
			   if ( ( instanceDown & ( 1 << indexOfHost ) ) == 0 ) 
			       break;
		       }
		   }
	       }
	   }
	   
	   InetAddress host = (InetAddress) hostList.get(indexOfHost);
	   try
	   {
	       return rewriteURL(url, host);
	   }
	   catch (MalformedURLException e)
	   {
	       //Shouldn't happen.
	       System.out.println("malformed url exception");
	   }
       }
       
       //return as it was.
       return url;
   }

   /**
    * Internal method that returns the min between last_index 
    * and x if x is not equal to -1.
    *
    * @param last_index last_index value
    * @param x value to compare with last_index
    * @return x if (x<last_index and x!=-1) else last_index
    */
   private int isMin(int last_index, int x)
   {
         if (x == -1)
            return last_index;
         if (last_index<=x)
            return last_index;
         else
            return x;
      }

   /**
    * Extract an itemId from the last HTML reply. If several itemId entries
    * are found, one of them is picked up randomly.
    *
    * @return an item identifier or -1 on error
    */
   private int extractItemIdFromHTML()
   {
      if (lastHTMLReply == null)
      {
         if (debugLevel>0)
            System.err.println("Thread "+this.getName()+": There is no previous HTML reply<br>");
         return -1;
      }

      // Count number of itemId
      int count = 0;
      int keyIndex = lastHTMLReply.indexOf("itemId=");
      while (keyIndex != -1)
      {
         count++;
         keyIndex = lastHTMLReply.indexOf("itemId=", keyIndex+7); // 7 equals to itemId=
      }
      if (count == 0)
      {
         if (lastItemId >= 0)
            return lastItemId;

         log( "extractItemIdFromHTML", "Cannot find item id in last HTML reply", 1 );
         log( "extractItemIdFromHTML", "Last HTML reply is: <font color=black>" + lastHTMLReply + "</font>", 2 );

         return -1;
      }

      // Choose randomly an item
      count = userRand.nextInt(count)+1;
      keyIndex = -7;
      while (count > 0)
      {
         keyIndex = lastHTMLReply.indexOf("itemId=", keyIndex+7); // 7 equals to itemId=
         count--;
      }
      int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', keyIndex+7));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', keyIndex+7));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', keyIndex+7));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', keyIndex+7));
      Integer foo = new Integer(lastHTMLReply.substring(keyIndex+7, lastIndex));
      lastItemId = foo.intValue();
      return lastItemId;
   }


   /**
    * Extract a page value from the last HTML reply (used from BrowseCategories like functions)
    *
    * @return a page value
    */
   private int extractPageFromHTML()
   {
      if (lastHTMLReply == null)
         return 0;

      int firstPageIndex = lastHTMLReply.indexOf("&page=");
      if (firstPageIndex == -1)
         return 0;
      int secondPageIndex = lastHTMLReply.indexOf("&page=", firstPageIndex+6); // 6 equals to &page=
      int chosenIndex = 0;
      if (secondPageIndex == -1)
         chosenIndex = firstPageIndex; // First or last page => go to next or previous page
      else
      {  // Choose randomly a page (previous or next)
         if (userRand.nextInt(100000) < 50000)
            chosenIndex = firstPageIndex;
         else
            chosenIndex = secondPageIndex;
      }
      int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', chosenIndex+6));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', chosenIndex+6));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', chosenIndex+6));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', chosenIndex+6));
      Integer foo = new Integer(lastHTMLReply.substring(chosenIndex+6, lastIndex));
      return foo.intValue();
   }


   /**
    * Extract an int value corresponding to the given key
    * from the last HTML reply. Example : 
    * <pre>int userId = extractIdFromHTML("&userId=")</pre>
    *
    * @param key the pattern to look for
    * @return the <code>int</code> value or -1 on error.
    */
   private int extractIntFromHTML(String key)
   {
      if (lastHTMLReply == null)
      {
         if (debugLevel>0)
            System.err.println("Thread "+this.getName()+": There is no previous HTML reply");
         return -1;
      }

      // Look for the key
      int keyIndex = lastHTMLReply.indexOf(key);
      if (keyIndex == -1)
      {
         // Dirty hack here, ugly but convenient
         if ((key.compareTo("userId=") == 0) && (lastUserId >= 0))
            return lastUserId;
         log( "extractIntFromHTML", "Cannot find " + key + " in last HTML reply", 1 );
         log( "extractIntFromHTML", "Last HTML reply is: <font color=black>" + lastHTMLReply + "</font>", 2 );
         return -1;
      }
      int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', keyIndex+key.length()));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', keyIndex+key.length()));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', keyIndex+key.length()));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', keyIndex+key.length()));
      Integer foo = new Integer(lastHTMLReply.substring(keyIndex+key.length(), lastIndex));
      // Dirty hack again here, ugly but convenient
      if (key.compareTo("userId=") == 0)
         lastUserId = foo.intValue();
      return foo.intValue();
   }


   /**
    * Extract a float value corresponding to the given key
    * from the last HTML reply. Example : 
    * <pre>float minBid = extractFloatFromHTML("name=minBid value=")</pre>
    *
    * @param key the pattern to look for
    * @return the <code>float</code> value or -1 on error.
    */
   private float extractFloatFromHTML(String key)
   {
      if (lastHTMLReply == null)
      {
         if (debugLevel > 0)
            System.err.println("Thread "+this.getName()+": There is no previous HTML reply");
         return -1;
      }

      // Look for the key
      int keyIndex = lastHTMLReply.indexOf(key);
      if (keyIndex == -1)
      {
         log( "extractFloatFromHTML", "Cannot find " + key + " in last HTML reply", 1 );
         log( "extractFloatFromHTML", "Last HTML reply is: <font color=black>" + lastHTMLReply + "</font>", 2 );
         return -1;
      }
      int lastIndex = isMin(Integer.MAX_VALUE, lastHTMLReply.indexOf('\"', keyIndex+key.length()));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('?', keyIndex+key.length()));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('&', keyIndex+key.length()));
      lastIndex = isMin(lastIndex, lastHTMLReply.indexOf('>', keyIndex+key.length()));
      Float foo = new Float(lastHTMLReply.substring(keyIndex+key.length(), lastIndex));
      return foo.floatValue();
   }


   /**
    * Computes the URL to be accessed according to the given state.
    * If any parameter are needed, they are computed from last HTML reply.
    *
    * @param state current state
    * @return URL corresponding to the state
    */
   public UAType computeURLFromState(int state)
   {
      if (lastHTMLReply != null)
      {
         if (lastHTMLReply.indexOf("Sorry") != -1) // Nothing matched the request, we have to go back
            state = transition.backToPreviousState();
      }
      switch (state)
      {
      case -1: // reset to home page
         log( "computeURLFromState", "reset to Home (didn't find what was looking for)", 3 );
	 log( "computeURLFromState", "committing user action (because we're resetting to initial state)", 2 );
	 stats.commitUserAction();
	 resetToInitialState();
      case 0: // Home Page
         log( "computeURLFromState", "going to Home", 0 );
         // Generating the URL of the homepage means we are starting a new session
         return new UAType( false, urlGen.homePage(), true );
      case 1: // Register User Page
         log( "computeURLFromState", "going to Register", 0 );
         return new UAType( false, urlGen.register(), true );
      case 2: // Register the user in the database
      { // Choose a random nb over already known attributed ids
         int i = rubis.getNbOfUsers()+userRand.nextInt(1000000)+1; 
         String firstname = "Great"+i;
         String lastname = "User"+i;
         String nickname = "user"+i;
         String email = firstname+"."+lastname+"@rubis.com";
         String password = "password"+i;
         String regionName = (String)rubis.getRegions().elementAt(i % rubis.getNbOfRegions());
        
         log( "computeURLFromState", "going to RegisterUser in DB", 0 );
	 // this action is a COMMIT point
	 return new UAType( true, urlGen.registerUser(firstname, lastname, nickname, email, password, regionName), true );
      }
      case 3: // Browse Page
         log( "computeURLFromState", "going to Browse", 0 );
         return new UAType( false, urlGen.browse(), true );
      case 4: // Browse Categories
         log( "computeURLFromState", "going to BrowseCategories", 0 );
         return new UAType( false, urlGen.browseCategories(), true );
      case 5: // Browse items in a category
      { // We randomly pickup a category from the generated data instead of from the HTML page (faster)
         int    categoryId = userRand.nextInt(rubis.getNbOfCategories());
         String categoryName = (String)rubis.getCategories().elementAt(categoryId);
         log( "computeURLFromState", "going to SearchItemsInCategory", 0 );
         return new UAType( false, urlGen.browseItemsInCategory(categoryId, categoryName, extractPageFromHTML(), rubis.getNbOfItemsPerPage()), true );
      }
      case 6: // Browse Regions
         log( "computeURLFromState", "going to BrowseRegions", 0 );
         return new UAType( false, urlGen.browseRegions(), true );
      case 7: // Browse categories in a region
         String regionName = (String)rubis.getRegions().elementAt(userRand.nextInt(rubis.getNbOfRegions()));
         log( "computeURLFromState", "going to BrowseCategoriesInRegion", 0 );
         return new UAType( false, urlGen.browseCategoriesInRegion(regionName), true );
      case 8: // Browse items in a region for a given category
      { // We randomly pickup a category and a region from the generated data instead of from the HTML page (faster)
         int    categoryId = userRand.nextInt(rubis.getNbOfCategories());
         String categoryName = (String)rubis.getCategories().elementAt(categoryId);
         int    regionId = userRand.nextInt(rubis.getNbOfRegions());
         log( "computeURLFromState", "going to SearchItemsInRegion", 0 );
         return new UAType( false, urlGen.browseItemsInRegion(categoryId, categoryName, regionId, extractPageFromHTML(), rubis.getNbOfItemsPerPage()), true );
      }
      case 9: // View an item
      {
         int itemId = extractItemIdFromHTML();
         log( "computeURLFromState", "going to ViewItem", 0 );
         if (itemId == -1)
            computeURLFromState(transition.backToPreviousState()); // Nothing then go back
         else
	    return new UAType( false, urlGen.viewItem(itemId), true );
      }
      case 10: // View user information
      {
         int userId = extractIntFromHTML("userId=");
         log( "computeURLFromState", "going to ViewUserInfo", 0 );
         if (userId == -1)
            computeURLFromState(transition.backToPreviousState()); // Nothing then go back
         else
            return new UAType( false, urlGen.viewUserInformation(userId), true );
      }
      case 11: // View item bid history
         log( "computeURLFromState", "going to ViewBidHistory", 0 );
         return new UAType( false, urlGen.viewBidHistory(extractItemIdFromHTML()), true );
      case 12: // Buy Now Authentication
         log( "computeURLFromState", "going to BuyNowAuth", 0 );
         return new UAType( false, urlGen.buyNowAuth(extractItemIdFromHTML()), true );
      case 13: // Buy Now confirmation page
         log( "computeURLFromState", "going to BuyNow", 0 );
         return new UAType( false, urlGen.buyNow(extractItemIdFromHTML(), username, password), true );
      case 14: // Store Buy Now in the database
      {
         int maxQty = extractIntFromHTML("name=maxQty value=");
         if (maxQty < 1)
            maxQty = 1;
         int qty = userRand.nextInt(maxQty)+1;
         log( "computeURLFromState", "going to StoreBuyNow", 0 );
	 // this action is a COMMIT point
         return new UAType( true, urlGen.storeBuyNow(extractItemIdFromHTML(), userId, qty, maxQty), false );
      }
      case 15: // Bid Authentication
         log( "computeURLFromState", "going to PutBidAuth", 0 );
         return new UAType( false, urlGen.putBidAuth(extractItemIdFromHTML()), true );
      case 16: // Bid confirmation page
      {
         int itemId = extractItemIdFromHTML();
         log( "computeURLFromState", "going to PutBid", 0 );
         if (itemId == -1)
            computeURLFromState(transition.backToPreviousState()); // Nothing then go back
         else
	    return new UAType( false, urlGen.putBid(itemId, username, password), true );
      }
      case 17: // Store Bid in the database
      { /* Generate randomly the bid, maxBid and quantity values,
           all other values are retrieved from the last HTML reply */
         int maxQty = extractIntFromHTML("name=maxQty value=");
         if (maxQty < 1)
            maxQty = 1;
         int qty = userRand.nextInt(maxQty)+1;
         float minBid = extractFloatFromHTML("name=minBid value=");
         float addBid = userRand.nextInt(10)+1;
         float bid = minBid+addBid;
         float maxBid = minBid+addBid*2;
         log( "computeURLFromState", "going to StoreBid", 0 );
	 // this action is a COMMIT point
	 return new UAType( true, urlGen.storeBid(extractItemIdFromHTML(), userId, minBid, bid, maxBid, qty, maxQty), false );
      }
      case 18: // Comment Authentication page
         log( "computeURLFromState", "going to PutCommentAuth", 0 );
         return new UAType( false, urlGen.putCommentAuth(extractItemIdFromHTML(), extractIntFromHTML("to=")), true );
      case 19: // Comment confirmation page
         log( "computeURLFromState", "going to PutComment", 0 );
         return new UAType( false, urlGen.putComment(extractItemIdFromHTML(), extractIntFromHTML("to="), username, password), true );
      case 20: // Store Comment in the database
      { // Generate a random comment and rating
         String[] staticComment = { "This is a very bad comment. Stay away from this seller !!<br>",
                                    "This is a comment below average. I don't recommend this user !!<br>",
                                    "This is a neutral comment. It is neither a good or a bad seller !!<br>",
                                    "This is a comment above average. You can trust this seller even if it is not the best deal !!<br>",
                                    "This is an excellent comment. You can make really great deals with this seller !!<br>" };
         int[]    staticCommentLength = { staticComment[0].length(), staticComment[1].length(), staticComment[2].length(),
                                          staticComment[3].length(), staticComment[4].length()};
         int[]    ratingValue = { -5, -3, 0, 3, 5 };
         int      rating;
         String   comment;

         rating = userRand.nextInt(5);
         int commentLength = userRand.nextInt(rubis.getCommentMaxLength())+1;
         comment = "";
         while (staticCommentLength[rating] < commentLength)
         {
            comment = comment+staticComment[rating];
            commentLength -= staticCommentLength[rating];
         }
         comment = staticComment[rating].substring(0, commentLength);

         log( "computeURLFromState", "going to StoreComment", 0 );
	 // this action is a COMMIT point
         return new UAType( true, urlGen.storeComment(extractItemIdFromHTML(), extractIntFromHTML("name=to value="), userId, ratingValue[rating], comment), false );
      }
      case 21: // Sell page
         log( "computeURLFromState", "going to Sell", 0 );
         return new UAType( false, urlGen.sell(), true );
      case 22: // Select a category to sell item
         log( "computeURLFromState", "going to SelectCategoryToSellItem", 0 );
         return new UAType( false, urlGen.selectCategoryToSellItem(username, password), true );
      case 23:
      {
         int categoryId = userRand.nextInt(rubis.getNbOfCategories());
         log( "computeURLFromState", "going to SellItemForm", 0 );
         return new UAType( false, urlGen.sellItemForm(categoryId, userId), true );
      }
      case 24: // Store item in the database
      {
         String name;
         String description;
         float  initialPrice; 
         float  reservePrice;
         float  buyNow;
         int    duration;
         int    quantity;
         int    categoryId;
         String staticDescription = "This incredible item is exactly what you need !<br>It has a lot of very nice features including "+
            "a coffee option.<br>It comes with a free license for the free RUBiS software, that's really cool. But RUBiS even if it "+
            "is free, is <B>(C) Rice University/INRIA 2001</B>. It is really hard to write an interesting generic description for "+
            "automatically generated items, but who will really read this ?<br>You can also check some cool software available on "+
            "http://sci-serv.inrialpes.fr. There is a very cool DSM system called SciFS for SCI clusters, but you will need some "+
            "SCI adapters to be able to run it ! Else you can still try CART, the amazing 'Cluster Administration and Reservation "+
            "Tool'. All those software are open source, so don't hesitate ! If you have a SCI Cluster you can also try the Whoops! "+
            "clustered web server. Actually Whoops! stands for something ! Yes, it is a Web cache with tcp Handoff, On the fly "+
            "cOmpression, parallel Pull-based lru for Sci clusters !! Ok, that was a lot of fun but now it is starting to be quite late "+
            "and I'll have to go to bed very soon, so I think if you need more information, just go on <h1>http://sci-serv.inrialpes.fr</h1> "+
            "or you can even try http://www.cs.rice.edu and try to find where Emmanuel Cecchet or Julie Marguerite are and you will "+
            "maybe get fresh news about all that !!<br>";
         int    staticDescriptionLength = staticDescription.length();
         int    totalItems = rubis.getTotalActiveItems()+rubis.getNbOfOldItems();
         int    i = totalItems+userRand.nextInt(1000000)+1; 

         name = "RUBiS automatically generated item #"+i;
         int descriptionLength = userRand.nextInt(rubis.getItemDescriptionLength())+1;
         description = "";
         while (staticDescriptionLength < descriptionLength)
         {
            description = description+staticDescription;
            descriptionLength -= staticDescriptionLength;
         }
         description = staticDescription.substring(0, descriptionLength);
         initialPrice = userRand.nextInt(5000)+1;
         if (userRand.nextInt(totalItems) < rubis.getPercentReservePrice()*totalItems/100)
            reservePrice = userRand.nextInt(1000)+initialPrice;
         else
            reservePrice = 0;
         if (userRand.nextInt(totalItems) < rubis.getPercentBuyNow()*totalItems/100)
            buyNow = userRand.nextInt(1000)+initialPrice+reservePrice;
         else
            buyNow = 0;
         duration = userRand.nextInt(7)+1;
         if (userRand.nextInt(totalItems) < rubis.getPercentUniqueItems()*totalItems/100)
            quantity = 1;
         else
            quantity = userRand.nextInt(rubis.getMaxItemQty())+1;
         categoryId =  userRand.nextInt(rubis.getNbOfCategories());
         log( "computeURLFromState", "going to RegisterItem", 0 );
	 // this action is a COMMIT point
	 return new UAType( true, urlGen.registerItem(name, description, initialPrice, reservePrice, buyNow, duration, quantity, userId, categoryId), false );
      }
      case 25: // About Me authentification
         log( "computeURLFromState", "going to AboutMe (auth form)", 0 );
         return new UAType( false, urlGen.aboutMe(), true );
      case 26: // About Me information page
         log( "computeURLFromState", "going to AboutMe", 0 );
	 // this action is a COMMIT point
         return new UAType( true, urlGen.aboutMe(username, password), true );
      case 27: // Login page
         log( "computeURLFromState", "going to Login", 0 );
	 cookieMgr.emptyCookieJar(); // no need for cookies prior to logging in...
	 return new UAType( false, urlGen.login(), true );
      case 28: // Login user page
         this.loggedIn = true;
         log( "computeURLFromState", "going to LoginUser", 0 );	 
	 cookieMgr.emptyCookieJar(); // no need for cookies prior to logging in...
	 return new UAType( false, urlGen.loginUser(username, password), true );
      case 29: // Logout page
	 this.loggedIn = false;
         log( "computeURLFromState", "going to Logout", 0 );
	 // this action is a COMMIT point
	 return new UAType( true, urlGen.logout(), true );
      default:
         if (debugLevel > 0)
            System.err.println("Thread "+this.getName()+": This state is not supported ("+state+")<br>");
         return null;
      }
   }

  /**
   * Set the state machine back to the transition table's initial state (home page).
   * If we're running against an HTTP server that maintains session state, a logout
   * is performed as well (in case we're logged in).
   */

  public void resetToInitialState()
  {
      if ( rubis.getHttpdHasSessionState()  &&  this.loggedIn ) {
	  // Log out (in case we're currently logged in)
	  log("resetToInitialState", "calling logout servlet", 0);
	  callHTTPServer( urlGen.logout(), true ); // logout is idempotent
	  cookieMgr.emptyCookieJar();
	  this.loggedIn = false;
      }
      transition.resetToInitialState();
      ignoreCount = rubis.getIgnoreFirstSessionOps(); // we don't count the first HTML-level operations in a session
  }

  /**
   * Abort the current user action and reset the user session:
   *   - Log that an error occured,
   *   - Record the abort of the user action in our statistics,
   *   - Logout of the server (if needed)
   *   - Reset to home state
   */
  protected void abortUserAction(String errMsg)
  {
      if (errMsg == null) {
	  errMsg = "Aborting user action and resetting session";
      }
      log( "run", errMsg + ". URL: " + lastURL, 1 );
      stats.abortUserAction();

      // wait for TPCWThinkTime before going to logout
      transition.waitForTPCWThinkTime();

      // logout
      resetToInitialState();
  }

  /**
   * Return true iff we believe we're logged in, but the server thinks we're not.
   */
  protected boolean sessionStateApparentlyLost()
  {
      return (rubis.getHttpdHasSessionState()
	      && this.loggedIn
	      && lastHTMLReply != null
	      && (   (lastHTMLReply.indexOf("Log In!") != -1)
                  || (lastHTMLReply.indexOf("Session is no longer active") != -1)));
  }

  /**
   * Return true iff "html" has text indicating a failure occured
   */
  public boolean HTMLReplyHasFailure(String html)
  {
      return (html.indexOf("ERROR")!=-1
              || html.indexOf("rror")!=-1
              || html.indexOf("xception")!=-1 );
  }

   /**
    * Emulate a user session using the current transition table.
    */
   public void run()
      {
	  // Generate random #'s for taking probabilistic actions
         Random rand = new Random(); 
		
         int  nbOfTransitions=0;
         int  next=0;
         long begin=0, end=0;

	 int htmlReadAttemptsLeft;     // # of remaining attempts to read failure-free HTML from a given URL before aborting user action
	 boolean abortAction;          // Set to true if we need to abort the user action
	 String abortActionMsg = null; // If abortAction is true, this will be the associated error message.
	 boolean wait = true;
	 
         while (!ClientEmulator.isEndOfSimulation())
         {
            // Select a random user for this session
            userId = userRand.nextInt(rubis.getNbOfUsers());
            username = "user"+(userId+1);
            password = "password"+(userId+1);
	    numUser ++;
            nbOfTransitions = rubis.getMaxNbOfTransitions();
	    
	    if ( wait )
	    {
		int waittime = rand.nextInt(20000);
		try
		{
		    Thread.sleep(waittime);
		}
		catch (InterruptedException e)
		{
		    //do nothing.
		}
		wait = false;
	    }


            log( "run", "Starting a new user session for " + username, 3 );

            // Start from Home Page
            resetToInitialState();
            next = transition.getCurrentState();

            while (!ClientEmulator.isEndOfSimulation() && !transition.isEndOfSession() && (nbOfTransitions > 0))
            {
		if (dropSessionStateRandomly == true && rand.nextInt(10) == 1) {
		    // Randomly drop cookies to test detection of lost session state
		    cookieMgr.emptyCookieJar();
		    log("run", "Clearing cookies (and session state) for  " + username, 1);
		}

               // Compute next step (also measures time spent in server call)
	       UAType retVal      = computeURLFromState(next);
               lastURL            = retVal.url;
	       //if this is a new session, then validate URL.
	       if( rubis.getProperty("use_loadbalancer").equals("yes") ) {
		   String lbOption = rubis.getProperty("loadbalancer_option");
		   if ( lbOption.equals("a") ) {
		        lastURL = validateURL(lastURL, false);
	            } else if ( lbOption.equals("b") ) {
			if(next == 0 )
			    lastURL = validateURL(lastURL, false);
			if(next == 0 )
			    lastURL = validateURL(lastURL, true);
		    } else {
			//must be option c or something else. do nothing.
		    }

	       }

	       boolean commit     = retVal.commitUserAction;
	       boolean idempotent = retVal.idempotent;

	       // Start a new user operation
	       UserOperation thisOp = new UserOperation( threadId + "_" + numUser + "_" + userId, lastURL );

	       // Reset our html retry count and our indicator of an aborted session
	       htmlReadAttemptsLeft = getNumAttemptsHtml( idempotent );
	       abortAction = false;

	       // Call the URL, retrying up to a maximum number of times as specified by the 'max_html_read_attempts' property.
	       while (htmlReadAttemptsLeft > 0) {
		   lastHTMLReply = callHTTPServer( lastURL, idempotent );

		   if (lastHTMLReply == null)
		   {
		       // If lastHTMLReply is null, it means we couldn't connect (or we couldn't read 
		       // the http data), and we tried the maximum number of times.
		       //
		       // Note that we've already called reporter.send() and stats.recordFailedAttempt()
		       // for each failed attempt.

		       abortAction = true;
		       abortActionMsg =  "Aborting user action and resetting session, after failed attempts to connect or read http data";
		       break; // break out of while loop
		   } 
		   
                   if (sessionStateApparentlyLost() || HTMLReplyHasFailure(lastHTMLReply)) 
                   {		       
		       // There was some kind of error in the returned HTML.
		       htmlReadAttemptsLeft--;

		       // If we're going to try again, record this failed attempt.
		       if ( htmlReadAttemptsLeft > 0  &&  ignoreCount <= 0)
			   stats.recordFailedAttempt();

		       log( "run", "Error in HTML returned from access to " 
			    + lastURL + "; " + htmlReadAttemptsLeft 
			    + " attempts remaining", 0);
		       if (sessionStateApparentlyLost()) {
			   log("run", "Session state apparently lost. Unexpected login screen returned when already logged in", 0);
		       }

		       log( "run", "Unsuccessful HTML reply was: <font color=black>"
			    + (lastHTMLReply != null ? lastHTMLReply : "NULL")
			    + "</font>", 2 ); 
		       if(rubis.getProperty("send_htmllevel_error").equals("yes"))
			   reporter.send( lastURL, "HTML" );
		   
		       if ( htmlReadAttemptsLeft > 0 ) {
			   try {
			       Thread.currentThread().sleep(rubis.getMsBetweenHTMLReadAttempts());
			   } catch (InterruptedException i) {
			       log( "run", "sleep interrupted...waking up", 1 );
			   }
		       }
		   } 
                   else 
                   {
		       // Successful response!
		       abortAction = false;
		       abortActionMsg = null;
		       break; // break out of while loop
		   }
	       }
	       if (htmlReadAttemptsLeft <= 0) {
		   abortAction = true;
		   abortActionMsg = "Aborting user action and resetting session, after " + getNumAttemptsHtml( idempotent )
		       + " failed attempts to get failure-free HTML";
	       }

	       // Record the fact that we completed an operation
	       if ( ignoreCount <= 0 )
		   stats.recordOp();
	       else
		   ignoreCount--;

	       if (abortAction == true) {
		   thisOp.failed();
		   abortUserAction(abortActionMsg);
		   transition.waitForTPCWThinkTime();
		   next = transition.getCurrentState();
	       } else {
		   thisOp.succeeded();
		   if ( debugLevel > 0 ) {
		       log( "run", "<font color=green><b>GOOD</b></font> HTML reply was: <font color=black>" + lastHTMLReply + "</font>", 0 );
		   } else {
		       log( "run", "<font color=green><b>GOOD</b></font> HTML reply was: <font color=black> **omitted** </font>", 0 );
		   } 


		   if ( commit )
		   {
 		       log( "run", "committing user action (because commit point completed successfully)", 2 );
		       stats.commitUserAction();
		   }
		   next = transition.nextState();
	       }
	       nbOfTransitions--;
            }
	    if (transition.isEndOfSession() || nbOfTransitions == 0) {
		log( "run", "committing user action (because session is ending)", 2 );
		stats.commitUserAction();
		log( "run", "Session of "+username+" successfully ended<br>", 2);
	    }
	 } // end while (!isEndOfSimulation)
	 log ("run", "Session of "+username+" ended due to simulation termination<br>", 4);
      }


   /**
    * Log a message.
    */
   public void log( String location, String msg, int level )
   {
      if (debugLevel < level)
         return;

      String[] color = { "red", "red", "red", "blue", "green", "magenta", "yellow", "black" };
      String output = "<font color=" + color[level] + ">";
      //output timestamp in "M/d/yy HH:mm:ss,SSS" format
      SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss,SSS", Locale.US);
      Date date = new Date(System.currentTimeMillis());
      output += this.getName() + " [" + df.format(date) + "] ";

      output += " : " + location + " : " + this.username + " : ";
      output += msg;
      output += "<br></font>";

      trace.println( output );
   }


   private class UAType
   {
       public URL url;
       public boolean commitUserAction;
       public boolean idempotent;

       public UAType( boolean commit, URL url, boolean idempotent )
       {
	   this.url = url;
	   this.commitUserAction = commit;
	   this.idempotent = idempotent;
       }
   }
}
