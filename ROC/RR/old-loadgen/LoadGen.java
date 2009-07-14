//
// $Id: LoadGen.java,v 1.13 2003/03/18 21:38:00 steveyz Exp $
//

// Based on 'ebe' written by Eugene Fratkin <fratkin@cs.stanford.edu>

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;


public class LoadGen {
   static int num = 1;
   static String webServer = "localhost";
   static int webPort = 6969;
   static final int time_elapsed=1000;
   static int number_of_requests=1;
   static int total_req_number=0;

   // PKEYANI these variables keep track of completed and failed requests
   static int completedRequestCount = 0;
   static Hashtable completedTable = new Hashtable();
   static int failedRequestCount = 0;
   static Hashtable failedTable = new Hashtable();
   static int unknownRequestCount = 0;
   static Hashtable unknownTable = new Hashtable();

   // PKEYANI this is used for resending requests in response to 404 HTTP headers
   static ArrayList currentCommandList;
   static int totalRetryCount = 0;
   static int totalFailedRetryCount = 0;
   static int currentRetryCount = 0;
   static final int MAX_RETRY = -1;

   // PKEYANI For writing to files
   // must add clientReader number and .html to each file name
   static String SUCCESS_FILENAME = "RESULTS_SUCCESS"; 
   static String FAILURE_FILENAME = "RESULTS_FAILED";

   // PKEYANI stores client readers so we can write their files in the output
   static  Thread[] clientThreadArray;   // for starting and stoping client reader threads
   static  ClientReader[] clientReaderArray; // for write data to files

   static String trace_file_name;
   static int no_of_times_to_repeat_workload = 1;

   static void printUsage()
      {
	 System.err.println("loadgen <repeat count> <trace file>");
      }

   public static void main (String args[]) throws Exception 
      {
	 if ( args.length != 2 )
	 {
	    printUsage();
	    System.exit(1);
	 }

	 try {
	    no_of_times_to_repeat_workload = Integer.parseInt(args[0]);
	    trace_file_name = args[1];
	 }
	 catch (Exception e)
	 {
	    printUsage();
	    System.exit(1);
	 }

	 // PKEYANI store the clientReader array so that at shutdown we can close their respective files
	 clientReaderArray = new ClientReader[num];

	 // Start the client threads that generate the workload
	 Thread clientThreadArray[] = new Thread[num];
	 for(int i = 0; i < num; i++) {
	    // PKEYANI changed this to store the clientReader as well
	    clientReaderArray[i] = new ClientReader(trace_file_name, 
						    no_of_times_to_repeat_workload,
						    SUCCESS_FILENAME+i+".html",
						    FAILURE_FILENAME+i+".html");
	    clientThreadArray[i] = new Thread(clientReaderArray[i]);
	    clientThreadArray[i].start();
	    
	    // PKEYANI first thing we do is write add the starting html 
	    // tags to fiels that we are interested in
	    writeToFile(clientReaderArray[i].getSuccessFilename(), "<html><body>\n");
	    writeToFile(clientReaderArray[i].getFailureFilename(), "<html><body>\n");
	    
	    // PKEYANI wait a while before creating the next threaded reader
	    try{
	       Thread.sleep(1000);
	    }
	    catch(Exception e){
	       System.out.println(e);
	    }
	    
	 }

	
	
	 // PKEYANI added shutdown hook to output results
	 // Had to hack this 
	 final ClientReader[] cra = clientReaderArray;
	 final Thread[] ta = clientThreadArray;
	 try{

	    Thread shutdown = new Thread(){
		  public void run(){
		     // Kill the threads
		     for(int i = 0 ; i < ta.length; i++){
			cra[i].die();
		     }

		     // Wait 5 seconds
		     try{
			for(int i = 1 ; i <= 5; i++){
			   Thread.sleep(1000);
			   System.out.print(i+" ");
			}
			System.out.println("");
		     }
		     catch(Exception e){
			System.out.println(e);
		     }
		     // Report statistics
		     System.out.println("****************************************");
		     System.out.println("************* Statistics ***************");
		     int total = LoadGen.completedRequestCount+LoadGen.failedRequestCount+LoadGen.unknownRequestCount;
		     System.out.println("*** Total requests: "+total);
		     System.out.println("*** Failed requests: "+LoadGen.failedRequestCount);
		     printHashtable(failedTable);
		     System.out.println("****************************************");
		     System.out.println("*** Completed request: "+LoadGen.completedRequestCount);
		     printHashtable(completedTable);
		     System.out.println("****************************************");
		     System.out.println("*** Unkown requests: "+LoadGen.unknownRequestCount);
		     printHashtable(unknownTable);
		     System.out.println("****************************************");
		     System.out.println("*** Retry requests: "+LoadGen.totalRetryCount);
		     System.out.println("*** Failed retry requests: "+LoadGen.totalFailedRetryCount);
			
		     // Write the results to file
		     for(int i = 0 ; i < ta.length; i++){
			writeToFile(cra[i].getSuccessFilename(), "</body></html>");
			writeToFile(cra[i].getFailureFilename(), "</body></html>");
		     }
		     System.out.println("Killed threads, reported statistics and wrote data to files");
		  }
	       };
	    // System.out.println("adding shutdown hook to report statistics");
	    Runtime.getRuntime().addShutdownHook(shutdown);
	 }
	 catch(Exception e){
	    System.out.println(e);
	 } // ENDOF PKEYANI added shutdown hook


	 // PKEYANI
	 System.out.println("Created "+num+" clients to talk to "+webServer+" on port "+webPort);

      } // ENDOF Main method

   //
   // Wait for the web server to come up
   //
   public static void waitForServer (){
      try{
	 File greenlight = new File("/tmp/loadgen.go");  // FIXME: this is a hack
	    
	 while (greenlight.exists() == false) {
	    System.out.println("Waiting for server...");
	    Thread.sleep(10000); // sleep for 10 seconds
	 }
	    
	 greenlight.delete();
	 System.out.println("Server is up; starting load!");
      }
      catch(Exception e){
	 System.out.println(e);
      }
   } // ENDOF Method waitForServer()

   public static void printHashtable(Hashtable ht){
      Enumeration e = ht.keys();
      while(e.hasMoreElements()){
	 Object key = e.nextElement();
	 Object value = ht.get(key);
	 System.out.println(value+" instances of "+key);
      }
   }

   public static void updateHashtable(Hashtable ht, Object o){
      Object value = ht.get(o);
      Integer i = null;
      if(value == null){
	 i = new Integer(1);
	    
      }
      else{
	 i = (Integer)value;
	 i = new Integer( (i.intValue())+1);
      }
      ht.put(o,i);
   }


   public static void writeToFile(String filename, String data){
      try {
	 // PKEYANI new way to serialize objects to file
	 FileWriter fw = new FileWriter(new File(filename), true);
	 fw.write(data+"\n");
	 fw.close();
      }
      catch (Exception e) {
	 e.printStackTrace();
      }  
   }

} // ENDOF Class LoadGen
//////////////////////////////////////////////////////////////////////////

/**
 * This class handles the details of generating a workload and collecting
 * the results.  Recently 03/10/2003 we added code to report statistics of
 * the different number of response types it could have
 *
 */
class ClientReader implements Runnable
{
   boolean error=false;

   // PKEYANI temporary variable to hold request
   String command;
   // PKEYANI this holds the basic html of a failed request
   String failedRequestPage;
    
   // PKEYANI filenames that get written to
   private String successFilename;
   private String failureFilename;

   // PKEYANI Keeps track of what message we are on
   int requestCount = 0;

   // PKEYANI used to reset the place counter of the mainLoop loop if
   // a 404 failure is encountered
   int retryPoint = 0;
    
   //PKEYANI had to move this variable outside of mainLoop() so 
   // that it could be reset in other methods
   int scriptIndex = 0;

   // PKEYANI added this variable so we have a safe way to stop this thread
   boolean die = false;
   String traceFile;
   int repeat_count;

   // PKEYANI added constructor so we can initialize elements
   public ClientReader(String traceFile, int repeat_count, String successFile, String failureFile){
      this.traceFile = traceFile;
      this.repeat_count = repeat_count;
      this.successFilename = successFile;
      this.failureFilename = failureFile;
   }

   // PKEYANI
   public String getSuccessFilename(){
      return successFilename;
   }
    
   // PKEYANI
   public String getFailureFilename(){
      return failureFilename;
   }
    
   // PKEYANI makes the thread die
   public void die(){
      die = true;
   }

   public void run() {
      // PKEYANI initialize any necassary starting data
      failedRequestPage = getFileData("BASE_FAILED_RETURN_PAGE");
	
      try  {
	 mainLoop();
      }
      catch (Exception e) {
	 System.err.println(e);
	 e.printStackTrace();
      }
	
      System.exit(0);
   }

   // PKEYANI method to read all the data of a file as a string
   public String getFileData(String filename){
      // return value
      String returnString = new String("");
      try{
	 RandomAccessFile raf = new RandomAccessFile(filename, "r");
	 String currentLine = raf.readLine();
	 while(currentLine != null){
	    if(currentLine.length() !=0){
	       currentLine = cleanseString(currentLine);
	       returnString = returnString.concat(currentLine);
	    }
	    currentLine = raf.readLine();
	 }
      }
      catch(Exception e){
	 e.printStackTrace();
      }
      return returnString;
   }

   /**
    * PKEYANI this method is used to remove spaces, tabs and endlines
    * from strings so as to make exact size comparison easier
    */
   public String cleanseString(String input){
      String ret = input;
      if(ret != null){
	 ret = ret.replaceAll(" ","");
	 ret = ret.replaceAll("\n","");
	 ret = ret.replaceAll("\r","");
      }
      return ret;
   }


   /**
    * PKEYANI this method is meant to modularize a set of requests
    * that are made in more than one place.  As of now I am only
    * using it in receive() for restarting when a 404 HTTP header is encountered
    */
   public void sendToServer(String s){
      //
      // Open connection to the web server 
      //
      try{
	 Socket webSocket= new Socket(LoadGen.webServer, LoadGen.webPort);
	 webSocket.setSoTimeout(30000);
	 // System.out.println("opened socket");
	    
	 OutputStream out = webSocket.getOutputStream();
	 PrintWriter socketOut = new PrintWriter(new BufferedOutputStream(out));
	 socketOut.println(s + "\r");
	 socketOut.flush();
      }
      catch(Exception e){
	 System.out.println("PKEYANI "+e);
      }

   }
   /**
    * PKEYANI this method stores the sent command so that the 
    * load generator can resend commands if they fail,  
    *
    */
   public void captureCommandForRetry(String s){

      if(s.startsWith("GET /") || s.startsWith("POST /") || s.startsWith("target_screen")){
	 LoadGen.currentCommandList = new ArrayList();  
	 // This will be used to reset the index if a failure is noticed later
	 // If we are advancing to the next command we reset the currentRetryCount
	 // to keep track of how many times we retry each command
	 if(retryPoint < scriptIndex){
	    LoadGen.currentRetryCount = 0;
	 }
	 // This will be used to reset the index if a failure is noticed later
	 retryPoint = scriptIndex;
      }
      if(!s.equals(""))
	 LoadGen.currentCommandList.add(new String(s));	
   }


   /**
    * PKEYANI this method contains the semantics for retrying a request
    *
    * Currently It does not have an upper bound on how many times it retries
    * This functionality is not required right now but may be needed later
    */
   public void retryLastRequest(int sleep){
      // If MAX_RETRY is a positive integer we only
      if(LoadGen.currentRetryCount != LoadGen.MAX_RETRY){
	 System.out.println("RETRY RETRY RETRY RETRY RETRY RETRY RETRY RETRY "+LoadGen.currentRetryCount);
	 // Increment the retry counts
	 LoadGen.totalRetryCount++;
	 LoadGen.currentRetryCount++;
	    
	 try{
	    for(int i =0 ; i < sleep; i++){
	       Thread.sleep(1000);
	       // System.out.println(i+" ");
	    }
	    System.out.println("");
	 } catch(Exception e){ 
	    System.out.println("PKEYANI "+e);
	 }
	 scriptIndex = retryPoint;
      }
      else if( LoadGen.currentRetryCount == LoadGen.MAX_RETRY){
	 LoadGen.totalFailedRetryCount++;
      }
      else{
	 // 
      }
   }


   /**
    * This is the main method that generates the workload
    */
   public void mainLoop () throws Exception 
   {
      PrintWriter outputFileWriter = new PrintWriter(new FileWriter("outs.txt"));
      BufferedReader outputWrite = new BufferedReader(new FileReader(traceFile));
      Vector vec = new Vector();
      String line = outputWrite.readLine().trim();
      Timer timer = new Timer();
      timer.schedule(new Statistics(), 0, LoadGen.time_elapsed);
      boolean init = true;
	
      // Parse the recorded trace and store it in an in-memory vector
      while ( true ) {
	 if(line.length() > 0) {
	    vec.add(line);
	 }
	 else {
	    vec.add("");
	    //vec.add("\r\n");
	 }
	 line = outputWrite.readLine();
	 if( line == null )
	    break;
	 else 
	    line = line.trim();
      }
      vec.add("");
	
      int i = vec.size();
      System.out.println("Loaded " + vec.size() + " trace lines into memory");
	
      // PKEYANI Added variable to set the beginning of the last command
      // in the case that we have to retry, used to set j if failure noticed in receive()
      int retryPoint = 0;

      String s;
      String cookie="";
      boolean post=false;
      int requests=1;
      Random r= new Random();
      long name = r.nextInt();
      int reqno=1;
				    
      for ( ; repeat_count > 0 ; repeat_count--)
      {
	 while (scriptIndex < i-1)  	 // Loop for every line of the input file
	 {
	    int contentLength = 0;

	    // PKEYANI way to safely die
	    if(die){
	       break;
	    }
	    
	    // Get a new block of lines from the trace file; see if they
	    // correspond to a GET or a POST
	    s = (String) vec.get(scriptIndex);
	    if (s.startsWith("POST")) { // POST
	       post = true;
	    }

	    if (s.startsWith("POST") || s.startsWith("GET")) {
	       // PKEYANI added the prepended text
	       //System.out.println("PKEYANI +++ POST OR GET +++ "+s);
	       command = s;
	    }
	      
	    try {
	       int f=0;
	       Socket webSocket = null;
		  
	       //
	       // Open connection to the web server 
	       //
	       webSocket= new Socket(LoadGen.webServer, LoadGen.webPort);
	       webSocket.setSoTimeout(30000);
	       // System.out.println("opened socket");
		  
	       OutputStream out = webSocket.getOutputStream();
	       PrintWriter socketOut = new PrintWriter(new BufferedOutputStream(out));
		  
	       outputFileWriter.println(requests++);
	       outputFileWriter.flush();
		  
	       //
	       // Go through each line, until we meet an empty line
	       //
	       while( s.trim().length() != 0 ) {
		  //System.out.println("what is s? "+s);
		  //------------------------------
		  // GET requests
		  if (s.startsWith("GET")) {
		     // PKEYANNI  System.out.println("s starts with GET");
		     post = false;
		     if (s.indexOf("?") != -1) {
			StringTokenizer token= new StringTokenizer(s, "?");
			String new_s = token.nextToken() + "?";
			StringTokenizer tocken= new StringTokenizer(token.nextToken(), "&");
			while(tocken.hasMoreTokens()) {
			   String toc="";
			   toc=tocken.nextToken();
			   //System.out.println("toc = " + toc);
				  
			   if(toc.startsWith("user_name=") && (s.indexOf("createAccount")>-1)) {
			      name++;				      toc="user_name=u" + name;
			      while(toc.length() < 20) { // FIXME: verify the real length
				 name=name * 10;
				 toc="user_name=u" + name;
			      }
			      // System.out.println("name = " + name);
				      
			      // outputFileWriter.println("Workin On *********" + toc);
			      // outputFileWriter.flush();
			      toc="user_name=hi" + name;
			      // outputFileWriter.println("Workin On *********" + toc);
			      // outputFileWriter.flush();
                                //new_s += toc;
				      
			   }
			   if(toc.startsWith("user_name=") && (s.indexOf("updateAccount")>-1)) {
			      // outputFileWriter.println("Workin On *********" + toc);
			      // outputFileWriter.flush();
			      toc="user_name=hi" + name;
			      // outputFileWriter.println("Workin On *********" + toc);
			      // outputFileWriter.flush();
                                //new_s += toc;
			   }
			   if(toc.startsWith("j_username=")) {
			      // outputFileWriter.println("Workin On *********" + toc);
			      // outputFileWriter.flush();
			      toc="j_username=hi" + name;
			      // outputFileWriter.println("Workin On *********" + toc);
			      // outputFileWriter.flush();
                                //new_s += toc;
			   }
				  
			   if(tocken.hasMoreTokens()) {
			      new_s=new_s + toc + "&";
			   }
			   else {
			      new_s=new_s + toc;
			   }
			}
			//new_s += " HTTP/1.0";
			s = new_s;
			// System.out.println("\t" + s);
		     }
		  } /* end GET request */
		      
		      
		  //------------------------------
		  // Referer
		  if( s.startsWith("Referer:")) {
		     int coun1=0, coun2=0;
		     while(coun1 < 3) {
			if(s.charAt(coun2) == ':') {
			   coun1++;
			}
			coun2++;
		     }
		     s="Referer: http://" + LoadGen.webServer + ":" + s.substring(coun2);//+"\n";
			  
		  }
		      
		  //------------------------------
		  // Content-Length
		  if(s.startsWith("Content-Length:")) {
		     contentLength = Integer.parseInt(s.substring(16));
		     // System.out.println("content-length: " + contentLength + ", " + s);
		  }
		      
		  //------------------------------
		  // Host
		  if(s.startsWith("Host:")) {
		     s="Host: " + LoadGen.webServer + ":" + LoadGen.webPort;// +"\n";
		  }
		      
		  //------------------------------
		  // Cookie
		  if(s.startsWith("Cookie:")) {
		     s = "Cookie: " + cookie;// + "\n";
		     // System.out.println("Cookie (should NOT be empty): " + cookie);
		  }
		      
		  outputFileWriter.println(s + "\r");  // some debug information
		  outputFileWriter.flush();
		      
		  //
		  // Now send request to the web server
		  //
		      
		  // PKEYANI for retry
		  captureCommandForRetry(s);
		      
		  socketOut.println(s + "\r");
		  socketOut.flush();
		  scriptIndex++;
		      
		  //
		  // get the next entry in the vector
		  //
		  s = (String) vec.get(scriptIndex);
		      
	       } /* end while (s.trim().length() != 0) */
		  
	       outputFileWriter.println(s + "\r");
	       outputFileWriter.flush();
		  
	       // PKEYANI for retry 
	       captureCommandForRetry(s);
		      
	       socketOut.println(s + "\r");
	       socketOut.flush();
		  
	       //
	       // Deal with POSTs
	       //
	       if (post) {
		  //System.out.println("PKEYANI POST: "+s);
		  scriptIndex++;
		  s = (String) vec.get(scriptIndex);
		  StringTokenizer tocken= new StringTokenizer(s, "&");
		  String new_s ="";
		  while(tocken.hasMoreTokens()) {
		     String toc="";
		     toc=tocken.nextToken();
		     // System.out.println("toc = " + toc);
			  
		     //------------------------------
		     // A createAccount request
		     if(toc.startsWith("user_name=") && (s.indexOf("createAccount")>-1)) {
			// outputFileWriter.println("Workin On *********" + toc);
			// outputFileWriter.flush();
			toc="user_name=hi" + name;
			// outputFileWriter.println("Workin On *********" + toc);
			// outputFileWriter.flush();
			//new_s += toc;
			      
		     }
			  
		     //------------------------------
		     // An updateAccount request
		     if(toc.startsWith("user_name=") && (s.indexOf("updateAccount")>-1)) {
			// outputFileWriter.println("Workin On *********" + toc);
			// outputFileWriter.flush();
			toc="user_name=hi" + name;
			// outputFileWriter.println("Workin On *********" + toc);
			// outputFileWriter.flush();
			//new_s += toc;
		     }
			  
		     if(toc.startsWith("j_username=")) {
			// outputFileWriter.println("Workin On *********" + toc);
			// outputFileWriter.flush();
			toc="j_username=hi" + name;
			// outputFileWriter.println("Workin On *********" + toc);
			// outputFileWriter.flush();
			//new_s += toc;
		     }
			  
		     // Get the rest of the string ???
		     if(tocken.hasMoreTokens()) {
			new_s=new_s + toc + "&";
		     }
		     else {
			new_s=new_s + toc;
		     }
			  
		  }
		  s=new_s;
		  if (post)
		     while (s.length() < contentLength)
			s+="1";
		      
		  // System.out.println("new_s =" + s);
		  // System.out.println("new_s.length (shoudld be 73-76): " + new_s.length());
		      
		  outputFileWriter.print(s);
		  outputFileWriter.flush();
		      
		      
		  // PKEYANI for retry
		  captureCommandForRetry(s.trim());
		      
		  socketOut.print(s.trim());
		  socketOut.flush();
		  s = (String) vec.get(scriptIndex);
	       }
	       scriptIndex++;
		  
		  
	       //System.err.println("---PKEYANI--- Request: line = " + scriptIndex);
	       // System.out.println("Request: line = " + scriptIndex);
		  
//  		  if (scriptIndex >= 7300)
//  		      System.exit(0);
		  
	       cookie = Receive(webSocket,cookie,outputFileWriter,error);
	       // System.out.println("cookie = " + cookie);
	       //LoadGen.total_req_number++;
	       // System.err.println("Number of Req: " + LoadGen.number_of_requests + ":" + LoadGen.total_req_number);
	       System.out.print('.');
	    } 
	    // PKEYANI added finer granularity exception catching
	    catch(ConnectException e) {
	       System.out.println("PKEYANI Retrying because of connection failure "+e);
	       retryLastRequest(10);
	    }
	    catch (Exception e) {
	       e.printStackTrace();
	       // PKEYANI commented the next 2 lines out
	       // System.out.println("Server most likely down; exiting...");
	       //System.exit(1);
	    }
	    // PKEYANI added this so that we get the corrent request count
	    finally{
	       LoadGen.total_req_number++;
	    }

	    if(error) {
	       scriptIndex=0;
	       error=false;
	       System.err.println("Sleep! Sleep! Sleep!");
	       Thread.sleep(60000);  
	    }
	 }
      }
   }
    

   public String Receive(Socket web, String cookie, PrintWriter outputFileWriter, boolean error)  {
      int count, len;
      long startTime = System.currentTimeMillis(), readTime1=0, readTime2=0;
       
      // PKEYANI this variable ensures we don't count failed requests more than once
      boolean failure = false;
       
      try{
	 BufferedReader answer = 
	    new BufferedReader(new InputStreamReader(web.getInputStream()));
	   
	 while (true) {
	    // System.out.println("-------------------- reading from server");
	    String s = "";
	    // Gets the first line of the header
	    try { 
	       s = answer.readLine();         /////////////////////////////// Read first line of server response 1
		   
	       if(s == null)
	       {
		  retryLastRequest(10);
		  LoadGen.failedRequestCount++;
		  LoadGen.updateHashtable(LoadGen.failedTable, "s is null");
		  return cookie;
	       } 

	       if(s.indexOf("HTTP/1.1 4") > -1 
		  ||s.indexOf("HTTP/1.1 5") > -1 ){
		  LoadGen.failedRequestCount++;
		  LoadGen.updateHashtable(LoadGen.failedTable, s);
		  failure = true;
		   
		  // PKEYANI retry code 
		  retryLastRequest(10);
		  return "RETRY COOKIE";
		       
	       }
	       else if(s.indexOf("HTTP/1.1 200 OK") > -1 
		       || s.indexOf("HTTP/1.1 3") > -1 
		       || s.indexOf("HTTP/1.1 1") > -1 ){
		  LoadGen.completedRequestCount++;
		  LoadGen.updateHashtable(LoadGen.completedTable, s);
	       }
	       else if(s.indexOf("HTTP/1.1") > -1 ){
		  System.out.println(s);
		  LoadGen.unknownRequestCount++;
		  LoadGen.updateHashtable(LoadGen.unknownTable, s);
	       }
	       else{
		  //		       LoadGen.unknownRequestCount++;
		  //		       LoadGen.updateHashtable(LoadGen.unknownTable, s);       
	       }
	    }
	    catch (Exception e) {
	       System.out.println("Got exception " + e + ". Exiting...");
	       System.exit(2);
	    }
	       
	    if (s == null  ||  s.equals(""))
	       break;
	       
	    // PKEYANI COMMENTED OUT System.err.println("Recv: |" + s + "|"); System.err.flush();
	       

	    if (s.startsWith("Content-Type:")) {
	       len = -1;
		   
	       // Search for Content-Length in the remainder of the header
	       try {
		  ///////////////////////////////////////////////////////// Read 2-n line of server response 2-n
		  for ( ; (s != null) && (s.equals("")==false) ; s = answer.readLine()) {   
		     // System.err.println("Recv: |" + s + "|");
		     if (s.startsWith("Content-Length:"))
			len = Integer.parseInt(s.substring("Content-Length: ".length()));
		     else if (s.equals("Transfer-Encoding: chunked")) {
			// Read in all of the HTML for the return page
			String message = new String("");
			while (len != 0) {
			   s = answer.readLine();  // skip over empty line
			   s = answer.readLine().trim(); // line with chunk length in it
			   len = Integer.parseInt(s, 16); // chunk length
			   // System.err.println(">>> Reading chunk of length " + len);
				   
                           if(len == 0)
                           {
                               // zero length chunk, quit
                               break;
                           }
			   // PKEYANI
			   char[] tempBuff = new char[len];
			   answer.read(tempBuff,0, len);
			   String tempString = new String(tempBuff);
			   message = message.concat(tempString);
			   // PKEYANI answer.skip(len);
			}

                        if(len == 0)
                        {
                            // zero length chunk, quit
                            break;
                        }

			/**
			 * The code below has the notion of message, fullMessage and cleansedMessage
			 * + message is the original message received from the server without the html 
			 * and body tags; used for outputting to a generated webpage
			 * + cleansedMessage is the message without spaces and such and has , used 
			 * for comparison with default error page and also has 
			 * + fullMessage is the original message untouched and is used for one of the comparisons
			 */

			// Use this string to test for "<body><html></html></body>"
			String fullMessage = message;
			       
			// only keep html between  <body*> and </body>
			boolean parsingWorked = true;
			int b1 = message.indexOf("<body");
			if(b1 >=0){
			   int b2 = message.indexOf(">",b1);
			   if(b2 >= b1){
			      int b3 = message.indexOf("</body>",b2);
			      if(b3 >= b2){
				 message = message.substring(b2+1, b3-1);
			      }
			      else{
				 //  parsingWorked = false;
			      }
			   }
			   else{
			      parsingWorked = false;
			   }
			}
			else{
			   parsingWorked = false;
			}
			// Check if parsing worked
			if(!parsingWorked){
			   System.out.println("===========PKEYANI  Error: return page not well formed");
			   System.out.println("Writing bad page source to ERROR.log");
			   LoadGen.writeToFile("ERROR.log",fullMessage);
			   System.exit(-1);
			}
			       

			// Clense the HTML of spaces and end lines
			String cleansedMessage = cleanseString(message);
			       
			// PKEYANI if we didn't see an http message failure we check the message body
			if(!failure) {
			   // Run this test on the original uncleaned
			   if(fullMessage.indexOf("<html><body></body></html>") >=0) {
			      message = message.concat("\n <h3>***** FAILURE simple page *****</h3>");
			      LoadGen.failedRequestCount++;
			      failure = true;
			   }
			   else if (cleansedMessage.indexOf(failedRequestPage) >=0) {
			      message = message.concat("\n <h3>***** FAILURE matches generic failure page *****</h3>");
			      LoadGen.failedRequestCount++;
			      failure = true;
			   }
			   else if (cleansedMessage.length() <= 100) {
			      message = message.concat("\n <h3>***** FAILURE return page too short *****</h3>");
			      LoadGen.failedRequestCount++;
			      failure = true;
			   }
			   else if(cleansedMessage.length() <= failedRequestPage.length()+50 
				   && cleansedMessage.length() >= failedRequestPage.length()-50 ){

			      // Check that the page is not a logout page
			      String logout = cleanseString("Please visit us again soon");
			      if(cleansedMessage.indexOf(logout) >= 0){
				 // Do nothing
			      }
			      else{
				 message = message.concat("\n <h3>***** FAILURE similiar size of generic failure page *****</h3>");
				 LoadGen.failedRequestCount++;
				 failure = true;
			      }
			   }
			   else if(cleansedMessage.indexOf("ERROR") >=0 
				   || cleansedMessage.indexOf("Error") >=0
				   || cleansedMessage.indexOf("error") >=0
				   || cleansedMessage.indexOf("EXCEPTION") >=0
				   || cleansedMessage.indexOf("Exception") >=0
				   || cleansedMessage.indexOf("exception") >=0){
			      // Exception found without help request
			      if(command.indexOf("/estore/control/help") == -1){
				 message = message.concat("\n <h3>***** FAILURE Explicit error message *****</h3>");
				 LoadGen.failedRequestCount++;
				 failure = true; 
				       
			      }
			   }
				   
			   else{
			      message = message.concat("\n <h3>***** NORMAL *****</h3>");
			   }
			}
			else{
			   message = message.concat("\n <h3>***** FAILURE http header error *****</h3>");
			}

			// Increment the counter
			requestCount++;
			// PKEYANI if this is a failure we write to file
			if(failure){
			   // Write the causing command to file
			   LoadGen.writeToFile(failureFilename,"<lb> \n command "+requestCount+": "+command+"\n\r");
			   // Write the message size to file
			   Integer iVal = new Integer(message.length());
			   String sVal =  iVal.toString();
			   LoadGen.writeToFile(failureFilename,"size "+sVal+"\n\r");
			   // Write the returning html page to file
			   int index = message.indexOf("</title>");
			   if(index >= 0)
			      message = message.substring(index, message.length()-1);
			   LoadGen.writeToFile(failureFilename,message+"\n");
			}
			else{
			   // Write the causing command to file
			   LoadGen.writeToFile(successFilename,"<lb> \n command "+requestCount+": "+command+"\n");
			   // Write the message size to file
			   Integer iVal = new Integer(message.length());
			   String sVal =  iVal.toString();
			   LoadGen.writeToFile(successFilename,"size "+sVal+"\n");
			   // Write the returning html page to file
			   int index = message.indexOf("</title>");
			   if(index >= 0)
			      message = message.substring(index, message.length()-1);
			   LoadGen.writeToFile(successFilename,message+"\n");
			}
		     }
		     else{
			       
		     }
		  }
	       }
	       catch (Exception e) {
		  // PKEYANI
		  LoadGen.failedRequestCount++;
		  LoadGen.updateHashtable(LoadGen.failedTable, "We got an exception "+e);
		       
      
		  System.out.println("Exception " + e + ". Exit now...");
		  System.exit(3);
	       }
		   
	       // Skip over the data we don't need
	       if (len < 0){
		  // PKEYANI ADDED following two lines and commneted out third
		  len = 0;
		  break;
		  //System.exit(-1);
	       }
	       answer.skip(len);  // skip over data
		       
	       break;
	    }
	       
	    if (s.startsWith("Set-Cookie:"))
	       cookie = s.substring(12, s.indexOf(';'));
	       
	    if(s.indexOf("Status: 404")>-1)
	       error = true;
		       
	 } /* end while */
	   
	 // System.err.println("closed socket");
	 web.close();
      } 
      catch (Exception e) { 
	 e.printStackTrace(); 
	 System.err.println("Error in the Receiver");
	 System.exit(-1);
      }
       
      // System.err.println("RECV: " + (System.currentTimeMillis() - startTime) + "= " + readTime1 + " + " + readTime2 + " msec");
       
      return cookie;
   }

   class Statistics extends TimerTask{
      PrintWriter statsWriter;
      public Statistics()
	 {
	    try{
	       statsWriter = new PrintWriter(new FileWriter("stats.txt"));
	    }
	    catch(Exception e){ e.printStackTrace(); }
	 }
      public void run(){
	 statsWriter.println((LoadGen.total_req_number/LoadGen.number_of_requests) + ":" + LoadGen.total_req_number);
	 statsWriter.flush();
	 LoadGen.number_of_requests++;
      }
   }
}
