package roc.loadgen.rubis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Properties;
import java.util.ArrayList;

/**
 * Configuration for the simulated RUBiS client.
 *
 * @version <tt>$Revision: 1.4 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public class Configuration
{
    private static Properties prop = null;

    public static String serverName;
    public static String workloadFile;
    public static int    nbOfOldItems;
    public static int    numUsersInDB;
    public static int    numItemsPerPage;
    public static int    totalActiveItems=0; // set in parseCategoryNamesAndSetTotalItems()
    public static int    commentMaxLength;
    public static int    itemMaxLength;
    public static int    maxItemQty;

    public static float  percentReservePrice;
    public static float  percentBuyNow;
    public static float  percentUniqueItems;

    public static Vector regions;
    public static Vector categories;

    /**
     * Constructor.
     *
     * @param configFile name of file containing the RUBiS configuration.
     */
    public Configuration( String configFile )
    {
	try
	{
	    prop = new Properties();
	    prop.load( new FileInputStream( configFile ) );

	    serverName = prop.getProperty( "httpd_hostname" );
	    workloadFile = fileName( prop.getProperty( "workload_transition_table" ));

	    numUsersInDB = Integer.parseInt( prop.getProperty( "database_number_of_users" ));
	    numItemsPerPage = Integer.parseInt( prop.getProperty( "workload_number_of_items_per_page" ));
	    commentMaxLength = Integer.parseInt( prop.getProperty( "database_comment_max_length" ));
	    itemMaxLength = Integer.parseInt( prop.getProperty( "database_item_description_length" ));
	    nbOfOldItems = Integer.parseInt( prop.getProperty( "database_number_of_old_items" ));
	    maxItemQty = Integer.parseInt( prop.getProperty( "database_max_quantity_for_multiple_items" ));
	    
	    percentReservePrice = Float.parseFloat( prop.getProperty( "database_percentage_of_items_with_reserve_price" ));
	    percentBuyNow       = Float.parseFloat( prop.getProperty( "database_percentage_of_buy_now_items" ));
	    percentUniqueItems  = Float.parseFloat( prop.getProperty( "database_percentage_of_unique_items" ));

	    regions = parseRegionNames();
	    categories = parseCategoryNamesAndSetTotalItems();
	}
	catch( Exception e )
	{
	    e.printStackTrace();
	    throw new RuntimeException( "Cannot process " + configFile );
	}
    }

    private Vector parseRegionNames()
	throws java.io.IOException
    {
	String regionsFile = fileName( prop.getProperty( "database_regions_file" ) );
	BufferedReader reader = new BufferedReader( new FileReader( regionsFile ));
	Vector regions = new Vector();

	while( reader.ready() )
	    regions.add( reader.readLine() );

	reader.close();
	return regions;
    }

    private Vector parseCategoryNamesAndSetTotalItems()
	throws java.io.IOException
    {
	String categoriesFile = fileName( prop.getProperty( "database_categories_file" ) );
	BufferedReader reader = new BufferedReader( new FileReader( categoriesFile ));
	Vector categories = new Vector();

	while( reader.ready() )
	{
	    String line = reader.readLine();

	    int openParen  = line.lastIndexOf('(');
	    int closeParen = line.lastIndexOf(')');
	    assert (openParen>0 && closeParen>openParen) : "Syntax error in categories file: " + line;
	    totalActiveItems += Integer.parseInt( line.substring( openParen+1, closeParen ) );

	    categories.add( line.substring( 0, openParen-1 ) );
	}

	reader.close();
	return categories;
    }


    private String fileName( String name )
    {
	return System.getProperty("ROC_TOP") + "/" + name;
    }

//   /**
//    * Check for all needed fields in rubis.properties and inialize corresponding values.
//    * This function returns the corresponding URLGenerator on success.
//    *
//    * @return returns null on any error or the URLGenerator corresponding to the configuration if everything was ok.
//    */
//   public URLGenerator checkPropertiesFileAndGetURLGenerator()
//   {
//     try
//     {
//       Integer foo  = new Integer(getProperty("httpd_port"));
//       webSitePort = foo.intValue();
//       report.println(webSitePort+"<br><br>");

//       ignoreFirstSessionOps = new Integer( getProperty("ignore_first_sess_ops") ).intValue();
//       report.println( "# of first session ops to ignore: " + ignoreFirstSessionOps );

//       // report.print("EJB Server            : ");
//       EJBServer  = getProperty("ejb_server");
//       // report.println(EJBServer+"<br>");
//       // report.print("EJB HTML files path   : ");
//       EJBHTMLPath  = getProperty("ejb_html_path");
//       // report.println(EJBHTMLPath+"<br>");
//       // report.print("EJB Script files path : ");
//       EJBScriptPath  = getProperty("ejb_script_path");
//       // report.println(EJBScriptPath+"<br><br>");

//       // report.print("Servlets server            : ");
//       ServletsServer  = getProperty("servlets_server");
//       // report.println(ServletsServer+"<br>");
//       // report.print("Servlets HTML files path   : ");
//       ServletsHTMLPath  = getProperty("servlets_html_path");
//       // report.println(ServletsHTMLPath+"<br>");
//       // report.print("Servlets Script files path : ");
//       ServletsScriptPath  = getProperty("servlets_script_path");
//       // report.println(ServletsScriptPath+"<br><br>");

//       // report.print("PHP HTML files path   : ");
//       PHPHTMLPath  = getProperty("php_html_path");
//       // report.println(PHPHTMLPath+"<br>");
//       // report.print("PHP Script files path : ");
//       PHPScriptPath  = getProperty("php_script_path");
//       // report.println(PHPScriptPath+"<br><br>");

//       // # Workload
//       report.println("\n<h3><br>### Workload ###</h3>");
//       report.print("Remote client nodes            : ");
//       StringTokenizer nodes = new StringTokenizer(getProperty("workload_remote_client_nodes"),",");
//       remoteClients = new Vector(nodes.countTokens());
//       while (nodes.hasMoreTokens())
//         remoteClients.add(nodes.nextToken().trim());
//       nbOfClients = remoteClients.size();
//       report.println(nbOfClients+"<br>");
//       report.print("Remote client command          : ");
//       remoteCommand  = getProperty("workload_remote_client_command");
//       report.println(remoteCommand+"<br>");
//       report.print("Number of clients              : ");
//       foo = new Integer(getProperty("workload_number_of_clients_per_node"));
//       nbOfClients = foo.intValue();
//       report.println(nbOfClients+"<br>");


//       report.print("Maximum number of transitions  : ");
//       foo = new Integer(getProperty("workload_maximum_number_of_transitions"));
//       maxNbOfTransitions = foo.intValue();
//       report.println(maxNbOfTransitions+"<br>");
//       report.print("Number of items per page       : ");
//       foo = new Integer(getProperty("workload_number_of_items_per_page"));
//       nbOfItemsPerPage = foo.intValue();
//       report.println(nbOfItemsPerPage+"<br>");

//       report.print("Session run time in ms         : ");
//       foo = new Integer(getProperty("workload_session_run_time_in_ms"));
//       sessionTime = foo.intValue();
//       report.println(sessionTime+"<br>");

//       report.print("<font color=red>Geo's bucket size              : ");
//       foo = new Integer(getProperty("geo_bucket_size"));
//       geo_bucketSize = foo.intValue();
//       report.println(geo_bucketSize+"</font><br>");

//       report.print("<font color=red>Geo's bucket dump multiple     : ");
//       foo = new Integer(getProperty("geo_dump_multiple"));
//       geo_dumpMultiple = foo.intValue();
//       report.println(geo_dumpMultiple + " (i.e., dump aprox. every " + (geo_dumpMultiple*geo_bucketSize/1000) + " sec)</font><br>");

//       report.print("<font color=red>Geo's traced session ID        : ");
//       foo = new Integer(getProperty("geo_trace_session"));
//       geo_tracedSession = foo.intValue();
//       report.println(geo_tracedSession+"</font><br>");


//       //
//       // # Retry policies
//       //
//       // All six retry-related properties are optional.
//       //
//       report.println("\n<h3><br>### Retry Policies ###</h3>");
      
//       report.print("Maximum number of consecutive network connection attempts       : ");
//       try {
// 	  foo = new Integer(getProperty("max_net_connect_attempts"));
// 	  maxNETConnectAttempts = foo.intValue();
// 	  if (maxNETConnectAttempts <= 0) {
// 	      System.err.println("Error: value for property max_net_connect_attempts must be a positive integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 1.
// 	  maxNETConnectAttempts = 1;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + maxNETConnectAttempts);
//       }
//       report.println(maxNETConnectAttempts+"<br>");

//       report.print("Time to sleep between network connection attempts (in ms)       : ");
//       try {
// 	  foo = new Integer(getProperty("ms_between_net_connect_attempts"));
// 	  msBetweenNETConnectAttempts = foo.intValue();
// 	  if (msBetweenNETConnectAttempts < 0) {
// 	      System.err.println("Error: value for property ms_between_net_connect_attempts must be a non-negative integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 1000 ms.
// 	  msBetweenNETConnectAttempts = 1000;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + msBetweenNETConnectAttempts);
//       }
//       report.println(msBetweenNETConnectAttempts+"<br>");

//       report.print("Maximum number of consecutive HTTP read attempts                : ");
//       try {
// 	  foo = new Integer(getProperty("max_http_read_attempts"));
// 	  maxHTTPReadAttempts = foo.intValue();
// 	  if (maxHTTPReadAttempts <= 0) {
// 	      System.err.println("Error: value for property max_http_read_attempts must be a positive integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 1.
// 	  maxHTTPReadAttempts = 1;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + maxHTTPReadAttempts);
//       }
//       report.println(maxHTTPReadAttempts+"<br>");

//       report.print("Time to sleep between HTTP read attempts (in ms)                : ");
//       try {
// 	  foo = new Integer(getProperty("ms_between_http_read_attempts"));
// 	  msBetweenHTTPReadAttempts = foo.intValue();
// 	  if (msBetweenHTTPReadAttempts < 0) {
// 	      System.err.println("Error: value for property ms_between_http_read_attempts must be a non-negative integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 1000 ms.
// 	  msBetweenHTTPReadAttempts = 1000;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + msBetweenHTTPReadAttempts);
//       }
//       report.println(msBetweenHTTPReadAttempts+"<br>");

//       report.print("Maximum number of consecutive attempts to read failure-free HTML: ");
//       try {
// 	  foo = new Integer(getProperty("max_html_read_attempts"));
// 	  maxHTMLReadAttempts = foo.intValue();

// 	  if (maxHTMLReadAttempts <= 0) {
// 	      System.err.println("Error: value for property max_html_read_attempts must be a positive integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 1.
// 	  maxHTMLReadAttempts = 1;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + maxHTMLReadAttempts);
//       }
//       report.println(maxHTMLReadAttempts+"<br>");

//       report.print("Time to sleep between HTML read attempts (in ms)                : ");
//       try {
// 	  foo = new Integer(getProperty("ms_between_html_read_attempts"));
// 	  msBetweenHTMLReadAttempts = foo.intValue();
// 	  if (msBetweenHTMLReadAttempts < 0) {
// 	      System.err.println("Error: value for property ms_between_html_read_attempts must be a non-negative integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 1000 ms.
// 	  msBetweenHTMLReadAttempts = 1000;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + msBetweenHTMLReadAttempts);
//       }
//       report.println(msBetweenHTMLReadAttempts+"<br>");

//       report.print("Network connection timeout (in ms)       : ");
//       try {
// 	  foo = new Integer(getProperty("network_connect_timeout_in_ms"));
// 	  networkConnectTimeout = foo.intValue();
// 	  if (networkConnectTimeout < 0) {
// 	      System.err.println("Error: value for property network_connect_timeout_in_ms must be a non-negative integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 8000 ms
// 	  networkConnectTimeout = 8000;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + networkConnectTimeout);
//       }
//       report.println(networkConnectTimeout+"<br>");

//       report.print("Network read timeout (in ms)       : ");
//       try {
// 	  foo = new Integer(getProperty("network_read_timeout_in_ms"));
// 	  networkReadTimeout = foo.intValue();
// 	  if (networkReadTimeout < 0) {
// 	      System.err.println("Error: value for property network_read_timeout_in_ms must be a non-negative integer.");
// 	      return null;
// 	  }   
//       } catch (java.util.MissingResourceException e) {
// 	  // This property is not in the properties file. That's OK -- default it to 8000 ms
// 	  networkReadTimeout = 8000;
// 	  System.out.println("WARNING: " + e.getMessage() + "; defaulting to "
// 			     + networkReadTimeout);
//       }
//       report.println(networkReadTimeout+"<br>");

//       // # Database Information
//       report.println("\n<h3><br>### Database Information ###</h3>");
//       report.print("Database server                        : ");
//       dbServerName = getProperty("database_server");
//       report.println(dbServerName+"<br>");

//       // # Region & Category definition files
//       report.println("\n<h3><br>### Region & Category definition files ###</h3>");
//       report.print("Regions description file               : ");
//       report.println(numRegions+" regions found.<br>");

      
//       // # Items policy
//       report.println("\n<h3><br>### Items policy ###</h3>");
//       report.print("Number of old items                    : ");
//       report.println(nbOfOldItems+"<br>");
//       report.print("Percentage of unique items             : ");
//       report.println(percentUniqueItems+"%"+"<br>");
//       report.print("Percentage of items with reserve price : ");
//       report.println(percentReservePrice+"%"+"<br>");
//       report.print("Percentage of buy now items            : ");
//       report.println(percentBuyNow+"%"+"<br>");
//       report.print("Maximum quantity for multiple items    : ");
//       report.println(maxItemQty+"<br>");
//       report.print("Item description maximum lenth         : ");
//       report.println(itemDescriptionLength+"<br>");

//       // # Bids policy
//       report.println("\n<h3><br>### Bids policy ###</h3>");
//       report.print("Maximum number of bids per item        : ");
//       maxBidsPerItem        = new Integer(getProperty("database_max_bids_per_item"));
//       report.println(maxBidsPerItem+"<br>");

//       // # Comments policy
//       report.println("\n<h3><br>### Comments policy ###</h3>");
//       report.print("Maximum number of comments per user    : ");
//       maxCommentsPerUser    = new Integer(getProperty("database_max_comments_per_user"));
//       report.println(maxCommentsPerUser+"<br>");
//       report.print("Comment maximum length                 : ");
//       report.println(commentMaxLength+"<br>");

//       // # Monitoring Information
//       report.println("\n<h3><br>### Database Information ###</h3>");
//       report.print("Monitoring debugging level     : ");
//       monitoringDebug  = new Integer(getProperty("monitoring_debug_level"));
//       report.println(monitoringDebug+"<br>");
//       report.print("Monitoring program             : ");
//       monitoringProgram  = getProperty("monitoring_program");
//       report.println(monitoringProgram+"<br>");
//       report.print("Monitoring options             : ");
//       monitoringOptions  = getProperty("monitoring_options");
//       report.println(monitoringOptions+"<br>");
//       report.print("Monitoring sampling in seconds : ");
//       monitoringSampling = new Integer(getProperty("monitoring_sampling_in_seconds"));
//       report.println(monitoringSampling+"<br>");
//       report.print("Monitoring rsh                 : ");
//       monitoringRsh      = getProperty("monitoring_rsh");
//       report.println(monitoringRsh+"<br>");
//       report.print("Monitoring Gnuplot Terminal    : ");
//       monitoringGnuPlot  = getProperty("monitoring_gnuplot_terminal");
//       report.println(monitoringGnuPlot+"<br>");

//       // Create a new URLGenerator according to the version the user has chosen
//       report.println("\n");
//       useVersion = getProperty("httpd_use_version");
//       if (useVersion.compareTo("PHP") == 0)
//         urlGen = new URLGeneratorPHP(serverName, webSitePort, PHPHTMLPath, PHPScriptPath);
//       else if (useVersion.compareTo("EJB") == 0)
//         urlGen = new URLGeneratorEJB(serverName, webSitePort, EJBHTMLPath, EJBScriptPath);
//       else if (useVersion.compareTo("Servlets") == 0)
//         urlGen = new URLGeneratorServlets(serverName, webSitePort, ServletsHTMLPath, ServletsScriptPath);
//       else
//       {
//         System.err.println("Sorry but '"+useVersion+"' is not supported. Only PHP, EJB and Servlets are accepted.");
//         return null;
//       }
//       report.println("Using "+useVersion+" version.<br>");
//     }
//     catch (Exception e)
//     {
//       System.err.println("Error while checking rubis properties file: "+e.toString());
//       return null;
//     }
//     return urlGen;
//   }

//  /**
//    * use in case that multiple httpd_hosts exist.
//    * Of course you can use this when only single httpd_host exists.
//    * At that time this method returns ArrayList that contains only
//    * one URLGenerator instance.
//    *
//    * @return ArrayList of URLGenerators. (Only differs in target
//    *         httpd host name.)
//    */
//   public ArrayList checkPropertiesFileAndGetURLGenerators()
//   {
//       ArrayList ret = new ArrayList();
//       URLGenerator urlGen = checkPropertiesFileAndGetURLGenerator();
//       if(urlGen == null)
// 	  Runtime.getRuntime().exit(1);
//       ret.add(urlGen);
      
//       for(int i=1; true; i++) 
//       {
// 	  try 
// 	  {
// 	      String hostName = getProperty("httpd_hostname" + i) ;
// 	      URLGenerator urlGenClone = (URLGenerator) urlGen.clone();
// 	      urlGenClone.setWebSiteName(hostName);
// 	      ret.add(urlGenClone);
// 	  } 
// 	  catch (java.util.MissingResourceException e) 
// 	  {
//             break;
// 	  }
//       }

//       return ret;
//   }


//   /**
//    * Get the total number of regions found in the region file given in the regions_file field
//    *
//    * @return total number of regions
//    */
//   public int getNbOfRegions()
//   {
//     return numRegions;
//   }


//   /**
//    * Get the total number of categories found in the categories file given in the categories_file field
//    *
//    * @return total number of categories
//    */
//   public int getNbOfCategories()
//   {
//     return nbOfCategories;
//   }

//   /**
//    * Get a vector of region names as found in the region file given in the regions_file field
//    *
//    * @return vector of region names
//    */
//   public Vector getRegions()
//   {
//     return regions;
//   }


//   /**
//    * Get a vector of category names as found in the categories file given in the categories_file field
//    *
//    * @return vector of category names
//    */
//   public Vector getCategories()
//   {
//     return categories;
//   }


//   /**
//    * Return an array of number of items per category as described in the categories file given in the categories_file field
//    *
//    * @return array of number of items per category
//    */
//   public int[] getItemsPerCategory()
//   {
//     return itemsPerCategory;
//   }


//   /**
//    * Get the total number of items computed from information found in the categories file given in the categories_file field
//    *
//    * @return total number of active items (auction date is not passed)
//    */
//   public int getTotalActiveItems()
//   {
//     return totalActiveItems;
//   }


//   /**
//    * Get the total number of old items (auction date is over) to be inserted in the database.
//    *
//    * @return total number of old items (auction date is over)
//    */
//   public int getNbOfOldItems()
//   {
//     return nbOfOldItems.intValue();
//   }


//   /**
//    * Get the percentage of unique items given in the percentage_of_unique_items field
//    *
//    * @return percentage of unique items
//    */
//   public float getPercentUniqueItems()
//   {
//     return percentUniqueItems.floatValue();
//   }


//   /**
//    * Get the percentage of items with a reserve price given in the percentage_of_items_with_reserve_price field
//    *
//    * @return percentage of items with a reserve price
//    */
//   public float getPercentReservePrice()
//   {
//     return percentReservePrice.floatValue();
//   }


//   /**
//    * Get the percentage of items that users can 'buy now' given in the percentage_of_buy_now_items field
//    *
//    * @return percentage of items that users can 'buy now' 
//    */
//   public float getPercentBuyNow()
//   {
//     return percentBuyNow.floatValue();
//   }


//   /**
//    * Get the maximum quantity for multiple items given in the max_quantity_for_multiple_items field
//    *
//    * @return maximum quantity for multiple items
//    */
//   public int getMaxItemQty()
//   {
//     return maxItemQty.intValue();
//   }

//   /**
//    * Get the maximum item description length given in the item_description_length field
//    *
//    * @return maximum item description length
//    */
//   public int getItemDescriptionLength()
//   {
//     return itemDescriptionLength.intValue();
//   }

//   /**
//    * Get the maximum number of bids per item given in the max_bids_per_item field
//    *
//    * @return maximum number of bids per item
//    */
//   public int getMaxBidsPerItem()
//   {
//     return maxBidsPerItem.intValue();
//   }

//   /**
//    * @deprecated Comments are now generated per item and no more per user, so this
//    * function should not be used anymore.
//    *
//    * Get the maximum number of comments per user given in the max_comments_per_user field
//    *
//    * @return maximum number of comments per user
//    */
//   public int getMaxCommentsPerUser()
//   {
//     return maxCommentsPerUser.intValue();
//   }

//   /**
//    * Get the maximum comment length given in the comment_max_length field
//    *
//    * @return maximum comment length
//    */
//   public int getCommentMaxLength()
//   {
//     return commentMaxLength.intValue();
//   }


//   /**
//    * Get the transition table file name given in the transition_table field
//    *
//    * @return transition table file name
//    */
//   public String getTransitionTable()
//   {
//     return transitionTable;
//   }


//   /**
//    * Get the number of columns in the transition table
//    *
//    * @return number of columns
//    */
//   public int getNbOfColumns()
//   {
//     return nbOfColumns;
//   }


//   /**
//    * Get the number of rows in the transition table
//    *
//    * @return number of rows
//    */
//   public int getNbOfRows()
//   {
//     return nbOfRows;
//   }


//   /**
//    * Returns true if TPC-W compatible think time must be used,
//    * false if transition matrix think time has to be used.
//    *
//    * @return if think time should be TPC-W compatible
//    */
//   public boolean useTPCWThinkTime()
//   {
//     return useTPCWThinkTime;
//   }


//   /**
//    * Get the number of items to display per page (when browsing) given in the number_of_items_per_page field
//    *
//    * @return number of items to display per page
//    */
//   public int getNbOfItemsPerPage()
//   {
//     return nbOfItemsPerPage;
//   }


//   /**
//    * Get the total number of clients user sessions to launch in parallel
//    *
//    * @return total number of clients
//    */
//   public int getNbOfClients()
//   {
//     return nbOfClients;
//   }


//   /**
//    * Get a vector of remote node names to launch clients on
//    *
//    * @return vector of remote node names to launch clients on
//    */
//   public Vector getRemoteClients()
//   {
//     return remoteClients;
//   }


//   /**
//    * Get a vector of remote node names to launch clients on
//    *
//    * @return vector of remote node names to launch clients on
//    */
//   public String getClientsRemoteCommand()
//   {
//     return remoteCommand;
//   }


//   /**
//    * Get the maximum number of transitions a client may perform
//    *
//    * @return maximum number of transitions
//    */
//   public int getMaxNbOfTransitions()
//   {
//     return maxNbOfTransitions;
//   }


//   /**
//    * Get up ramp time in milliseconds
//    *
//    * @return up ramp time
//    */
//   public int getUpRampTime()
//   {
//     return upTime;
//   }


//   /**
//    * Get session time in milliseconds
//    *
//    * @return session time
//    */
//   public int getSessionTime()
//   {
//     return sessionTime;
//   }

//   /**
//    * Get bucket size in milliseconds
//    *
//    * @return session time
//    */
//   public int getBucketSize()
//   {
//     return geo_bucketSize;
//   }

//   /**
//    * Get multiple of buckets for checkpointing graph
//    *
//    * @return session time
//    */
//   public int getDumpMultiple()
//   {
//     return geo_dumpMultiple;
//   }

//   /**
//    * Get session to trace
//    *
//    * @return session time
//    */
//   public int getTracedSession()
//   {
//     return geo_tracedSession;
//   }

//   public int getIgnoreFirstSessionOps() { return ignoreFirstSessionOps; }

//   /**
//    * Get the maximum number of attempts to connect to HTTP server
//    * (per URL request) before giving up
//    *
//    * @return maximum connect attempts
//    */
//   public int getMaxNETConnectAttempts()
//   {
//       return maxNETConnectAttempts;
//   }

//   /**
//    * Get the maximum number of attempts to connect to read HTTP data
//    * (per URL request) before giving up
//    *
//    * @return maximum HTTP read attempts
//    */
//   public int getMaxHTTPReadAttempts()
//   {
//       return maxHTTPReadAttempts;
//   }

//   /**
//    * Get the maximum number of attempts to read error-free HTML
//    * (per URL request) before giving up
//    *
//    * @return maximum HTML read attempts
//    */
//   public int getMaxHTMLReadAttempts()
//   {
//       return maxHTMLReadAttempts;
//   }

//   /**
//    * Get the time to sleep between attempts to connect to a URL (when retrying)
//    *
//    * @return milliseconds to sleep between connect attempts
//    */
//   public int getMsBetweenNETConnectAttempts()
//   {
//       return msBetweenNETConnectAttempts;
//   }

//   /**
//    * Get the time to sleep between attempts to read http data when retrying
//    *
//    * @return milliseconds to sleep between HTTP read attempts
//    */
//   public int getMsBetweenHTTPReadAttempts()
//   {
//       return msBetweenHTTPReadAttempts;
//   }

//   /**
//    * Get the time to sleep between attempts to read error-free HTML when retrying
//    *
//    * @return milliseconds to sleep between HTML read attempts
//    */
//   public int getMsBetweenHTMLReadAttempts()
//   {
//       return msBetweenHTMLReadAttempts;
//   }

//   /**
//    * Get the timeout (in milliseconds) on attempts to perform network connections.
//    *
//    * @return timeout for network connection attempts
//    */
//   public int getNetworkConnectTimeout()
//   {
//       return networkConnectTimeout;
//   }

//   /**
//    * Get the timeout (in milliseconds) on attempts to perform reads from the network.
//    *
//    * @return timeout for network read attempts
//    */
//   public int getNetworkReadTimeout()
//   {
//       return networkReadTimeout;
//   }

//   /**
//    * Get the monitoring debug level. Level is defined as follow: <pre>
//    * 0 = no debug message
//    * 1 = just error messages
//    * 2 = error messages+HTML pages
//    * 3 = everything!
//    * </pre>
//    *
//    * @return monitoring program full path and name
//    */
//   public int getMonitoringDebug()
//   {
//     return monitoringDebug.intValue();
//   }


//     private String EJBServer;
//     private String EJBHTMLPath;
//     private String EJBScriptPath;
//     private String ServletsServer;
//     private String ServletsHTMLPath;
//     private String ServletsScriptPath;
//     private String PHPHTMLPath;
//     private String PHPScriptPath;
//     private String useVersion;

//     // Information about Workload
//     private Vector  remoteClients;
//     private String  remoteCommand;
//     private int     nbOfClients;
//     private String  transitionTable;
//     private int     maxNbOfTransitions;
//     private int     nbOfItemsPerPage;
//     private int     upTime;
//     private float   upSlowdown;
//     private int     sessionTime;
//     private int     geo_bucketSize;    // size of bucket to collect samples (in msec)
//     private int     geo_dumpMultiple;  // every geo_dumpMultiple filled buckets we plot the data
//     private int     geo_tracedSession; // ID of session we want to trace
//     private int     downTime;
//     private float   downSlowdown;
//     private int     maxNETConnectAttempts;       // Max # of consecutive attempts to connect to HTTP server before giving up
//     private int     msBetweenNETConnectAttempts; // # of ms to sleep before retrying connection attempt
//     private int     maxHTTPReadAttempts;         // Max # of consecutive attempts to read HTTP data before giving up
//     private int     msBetweenHTTPReadAttempts;   // # of ms to sleep before retrying http read attempt
//     private int     maxHTMLReadAttempts;         // Max # of consecutive attempts to read error-free HTML before giving up
//     private int     msBetweenHTMLReadAttempts;   // # of ms to sleep before retrying html read attempt
//     private int     networkConnectTimeout;       // # of ms before an attempt to connect to HTTP server times out
//     private int     networkReadTimeout;          // # of ms before an attempt to read from HTTP server times out

//     private int     ignoreFirstSessionOps;       // # of ops at beginning of session that should not be counted in stats

//     // Policy to generate database information
//     private String  dbServerName;
  

//     private int     nbOfCategories;
//     private Vector  categories;
//     private int[]   itemsPerCategory;

//     private Integer nbOfOldItems;
//     private Float   percentUniqueItems;
//     private Float   percentBuyNow;
//     private Integer maxItemQty;
//     private Integer itemDescriptionLength;

//     private Integer maxBidsPerItem;

//     private Integer maxCommentsPerUser;
//     private Integer commentMaxLength;


//     // Monitoring information
//     private Integer monitoringDebug;
//     private String  monitoringProgram;
//     private String  monitoringOptions;
//     private Integer monitoringSampling;
//     private String  monitoringRsh;
//     private String  monitoringGnuPlot;
  
//     private URLGenerator          urlGen = null;
//     private static PrintStream report=System.out;


}
