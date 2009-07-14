//
// $Id: DelayProxy.java,v 1.11 2003/04/12 08:28:29 steveyz Exp $
//

package org.jboss.RR;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

import org.jboss.RR.HTTPRequestHeader;
import org.jboss.RR.HTTPResponseHeader;

public class DelayProxy
{
    static boolean pause = false;
    static boolean error_inject = false;
    static int msglport = 1313; // default port to listen for pause/unpause messages
    static int weblport = 6969; // default port to listen for http requests
    static int webnumports = 1; // how many ports to listen on for http requests
                                // (will create sockets on weblport, weblport +
                                // 1, weblport + 2, ...)
    // default web server host name
    static String webaddr = null;
    static int webport = 8080; // default web server port
    static int polltimeout = 500; // when reading web server response, timeout
                                  // after this much time to check the quit variable
    static boolean multithread = false; // multiple threads at one time?
    public static int request_count = 0;
    public static boolean keep_trace = false; // write to log files

    public static int slidingWindow = 8; // requests being paused by the proxy are
                                  // killed after this many seconds

    public static Random rand = new Random(); // rand num generator

    public static void main(String[] args)
    {
        System.out.println("");
        
        try
        {
            webaddr = InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e)
        {
            webaddr = "localhost";
        }
        
        // process command line arguments
        for(int argindex = 0; argindex < args.length; argindex++)
        {
            if(args[argindex].equalsIgnoreCase("-h") ||
               args[argindex].equalsIgnoreCase("-help"))
            {   // print help message
                System.out.println("Usage: java org.jboss.RR.DelayProxy [options]");
                System.out.println("Available Options: ");
                System.out.println(" -h, -help          Displays this message");
                System.out.println(" -sw #secs          Sliding window (in seconds) (-1 = wait forever), (default = "
                                   + slidingWindow + " secs)");
                System.out.println(" -pause             Start out paused (default start unpaused)");
                System.out.println(" -nomt              Turns off multi-threading!");
                System.out.println(" -mt                Turns on multi-threading!");
                System.out.println(" -msglport port#    Listen for pause/unpause message on this port (default = "
                                   + msglport + ") ");
                System.out.println(" -weblport port#    Listen for web requests on port# (default = "     
                                   + weblport + ") ");
                System.out.println(" -webnumports num   How many ports to listen on (default = "
                                   + webnumports + ") ");
                System.out.println(" -webaddr name      Web server hostname (default = "
                                   + webaddr + ") ");
                System.out.println(" -webport port#     Web server port# (default = "
                                   + webport + ") ");
                System.out.println(" -polltimeout ms    Web server poll timeout, for checking quit variable (default = "
                                   + polltimeout + ") ");
                System.out.println(" -keeptrace         Write log files of for all sockets!");
                System.out.println(" -errorinject       Inject bit errors into stream");
                return;
            }
            else if(args[argindex].equalsIgnoreCase("-pause"))
            {
                pause = true;
            }
            else if(args[argindex].equalsIgnoreCase("-nomt"))
            {
                multithread = false;
            }
            else if(args[argindex].equalsIgnoreCase("-mt"))
            {
                multithread = true;
            }
            else if(args[argindex].equalsIgnoreCase("-sw"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("# of seconds required for -sw option!");
                    return;
                }
                try
                {
                    slidingWindow = Integer.parseInt(args[argindex]);
                    if(slidingWindow < -1)
                    {
                        System.err.println("Invalid sliding window interval: " + args[argindex]);
                        return;
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-msglport"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -msglport option!");
                    return;
                }
                try
                {
                    msglport = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-weblport"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -weblport option!");
                    return;
                }
                try
                {
                    weblport = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-webnumports"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Number of ports required for -webnumports option!");
                    return;
                }
                try
                {
                    webnumports = Integer.parseInt(args[argindex]);
                    if(webnumports < 1)
                    {
                        System.err.println("Cannot listen on less than 1 port!");
                        return;
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-webaddr"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Hostname required for -webaddr option!");
                    return;
                }

                webaddr = args[argindex];
            }
            else if(args[argindex].equalsIgnoreCase("-webport"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("Port # required for -webport option!");
                    return;
                }
                try
                {
                    webport = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid port number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-polltimeout"))
            {
                if(++argindex >= args.length)
                {
                    System.err.println("milliseconds required for -polltimeout option!");
                    return;
                }
                try
                {
                    polltimeout = Integer.parseInt(args[argindex]);
                }
                catch (NumberFormatException e)
                {
                    System.err.println(args[argindex] + " is not a valid number!");
                    return;
                }
            }
            else if(args[argindex].equalsIgnoreCase("-keeptrace"))
            {
                keep_trace = true;
            }
            else if(args[argindex].equalsIgnoreCase("-errorinject"))
            {
                error_inject = true;
            }
            else
            {
                System.err.println("Invalid option!  Use '-h' for help");
                return;
            }  
        }

        try
        {
            Thread dpthread = new DelayProxyThread();
            dpthread.start();
            System.out.println("DelayProxy listening for pause/unpause messages on port " 
                               + msglport);
        }
        catch(SocketException sockexp)
        {
            System.err.println("Could not bind to message listen port = " + msglport);
            System.exit(1);
        }

        System.out.println("");

        for(int curPort = weblport; curPort < (weblport + webnumports); curPort++)
        {
            try
            {
                (new ServerSocketThread(curPort, webaddr, webport)).start();
                System.out.println("A DelayProxy ServerSocket is now listening on port " + curPort);
            }
            catch (IOException e)
            {
                System.err.println(">>> Could not bind to web listen port = " + curPort);
                System.exit(1);
            }
        }

        System.out.println("\nWelcome to the DelayProxy!");
        System.out.println("---------------------");
        System.out.println("Number of web listen ports: " + webnumports);
        System.out.println("Webserver hostname: " + webaddr);
        System.out.println("Webserver port: " + webport);
        if(pause)
        {
            System.out.println("DelayProxy is currently PAUSED");
        }
        else
        {    
            System.out.println("DelayProxy is currently UNPAUSED");
        }
        if(keep_trace)
        {
            System.out.println("Tracing ENABLED!");
        }

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        while(true)
        {      
            System.out.println("\nPlease select from the following options:");
            if(pause)
            {
                System.out.println("1) Unpause Proxy!");
            }
            else
            {
                System.out.println("1) Pause Proxy!");
            }          
            if(multithread)
            {
                System.out.println("2) Turn OFF multi-threaded http request handling");
            }
            else
            {
                System.out.println("2) Turn ON multi-threaded http request handling");
            }
            System.out.println("3) Change web server poll timeout (current = " + polltimeout + ")");
            if(error_inject)
            {    
                System.out.println("4) Turn OFF error injection!");
            }
            else
            {
                System.out.println("4) Turn ON error injection!"); 
            }
            System.out.println("5) Change sliding window interval (current = " + slidingWindow + ")");
            System.out.println("6) Quit");
            System.out.print("Command> ");

            try
            {
                String cmd = stdin.readLine();
                if(Integer.parseInt(cmd) == 1)
                {
                    pause = !pause;
                    if(pause)
                    {
                        System.out.println("*** Proxy paused! ***");
                    }
                    else
                    {
                        System.out.println("*** Proxy unpaused! ***");
                    }
                }
                else if(Integer.parseInt(cmd) == 2)
                {
                    multithread = !multithread;
                    if(multithread)
                    {
                        System.out.println("*** Turned ON multi-threaded http request handling ***");
                    }
                    else
                    {
                        System.out.println("*** Turned OFF multi-threaded http request handling ***");
                    }
                }
                else if(Integer.parseInt(cmd) == 3)
                {
                    System.out.print("Please enter new poll timeout (in milliseconds): ");
                    String timestr = stdin.readLine();
                    try
                    {
                        polltimeout = Integer.parseInt(timestr);
                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println("Invalid poll timeout value! Timeout not changed");
                    }
                }
                else if(Integer.parseInt(cmd) == 4)
                {
                    error_inject = !error_inject;
                    if(error_inject)
                    {
                        System.out.println("*** Error injection activated! ***");
                    }
                    else
                    {
                        System.out.println("*** Error injection deactivated! ***");
                    }
                }
                else if(Integer.parseInt(cmd) == 5)
                {
                    System.out.print("Enter new sliding window in seconds (-1 for infinite): ");
                    String num = stdin.readLine();
                    try
                    {
                        if(Integer.parseInt(num) < -1) /* not allowed */
                        {
                            System.out.println("Error: Invalid sliding window value");
                        }
                        else
                        {
                            slidingWindow = Integer.parseInt(num);
                            System.out.println("*** Sliding window interval set to " + slidingWindow
                                               + " ***");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("Error: Invalid number: " + num);
                    }
                }
                else if(Integer.parseInt(cmd) == 6)
                {
                    break;
                }
                else
                {
                    System.out.println("Invalid option, please try again!");
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid option, please try again!");
            }
            catch(IOException e)
            {
                break; // error
            }
        }

        System.out.println("\nGoodbye!");
        System.exit(0);
    }

    private static class DelayProxyThread extends Thread
    {
        // this is to listen for pause/unpause message
        DatagramSocket socket = null;
        public DelayProxyThread() throws SocketException
        {
            socket = new DatagramSocket(msglport);
        }
        
        public void run()
        {
            while(true)
            {
                try
                {
                    byte[] buf = new byte[1];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    if(buf[0] == 'P') // pause
                    {
                        System.out.println("*** Received PAUSE message! ***");
                        pause = true;
                    }
                    else if(buf[0] == 'U') // unpause
                    {
                        System.out.println("*** Received UNPAUSE message! ***");
                        pause = false;
                    }
                }
                catch(Exception e)
                {
                    System.err.println("DelayProxy error receiving packet!");
                }
            }
        }
    }
}

class ServerSocketThread extends Thread
{
    ServerSocket ss = null;
    int listenPort;
    String webaddr;
    int webport;

    public ServerSocketThread(int lport, String addr, int port) throws IOException
    {
        listenPort = lport;
        webaddr = addr;
        webport = port;
        ss = new ServerSocket(listenPort);
    }
        
    public void run()
    {
	ProxyConnection thread = null;

        while(true)
        {
            try
            {
                Socket s = ss.accept();
		if(!DelayProxy.multithread && thread != null && thread.isAlive())
		{
                    thread.quit = true;
                    
                    /*
                    try
                    {
                        thread.quit = true;
                        thread.join();
                    }
                    catch (InterruptedException ie)
                    {
                        System.out.println(">>>> Interrupted waiting for ProxyConnection thread!");
                    } */
		   thread = null;
		}
                thread = new ProxyConnection(s, webaddr, webport);
                thread.start();      
            }
            catch (IOException e)
            { 
            }
        }
    }
}

class ProxyConnection extends Thread
{
    static final String _CRLF  = "\r\n";
    static final String _CRLF2 = "\r\n\r\n";
    static final String _LF    = "\n";
    static final String _LF2   = "\n\n";
    public boolean quit = false;

    // default settings
    int webport = 8080;
    String webaddr = "localhost";

    Socket sock_c;
    Socket sock_s;
    
    public ProxyConnection(Socket s)
    {
        sock_c = s;
    }

    public ProxyConnection(Socket s, String addr, int port)
    {
        sock_c = s;
        webaddr = addr;
        webport = port;
    }

    /**
     * close socket connection
     */
    private boolean closeSock(Socket s) 
    {
        try 
        { 
            s.close();    
        } 
        catch(IOException err) 
        { 
            return false; 
        }
        return true;
    }

    public void run()
    {
        FileOutputStream in_s_file = null; 
        FileOutputStream out_c_file = null;
        FileOutputStream out_s_file = null; 
        FileOutputStream in_c_file = null;

        if(DelayProxy.keep_trace)
        {
            try
            {
                in_s_file = new FileOutputStream("trace_" + 
                                                 DelayProxy.request_count + ".in_s.out");
                out_c_file = new FileOutputStream("trace_" + 
                                                  DelayProxy.request_count + ".out_c.out");
                in_c_file = new FileOutputStream("trace_" + 
                                                 DelayProxy.request_count + ".in_c.out");
                out_s_file = new FileOutputStream("trace_" + 
                                                  DelayProxy.request_count + ".out_s.out");
                DelayProxy.request_count++;
            }
            catch(FileNotFoundException e)
            {
                System.err.println(">>> Warning: Could not create trace files!");
                in_s_file = null;
                out_c_file = null;
                in_c_file = null;
                out_s_file = null;
            }
        }
        
        StringBuffer sb = new StringBuffer();
        InputStream in_c = null;
        InputStream in_s = null;
        OutputStream out_c = null;
        OutputStream out_s = null;
        HTTPRequestHeader  c_header  = null;
        HTTPResponseHeader s_header  = null;
        String buf = "";
        String key, value;
        URL url    = null;

        Date startTime = new Date();
        while(DelayProxy.pause)
        {
            try
            {
                sleep(200); // sleep for 200 sec at a time
            }
            catch(Exception e)
            {
            }
            if((DelayProxy.slidingWindow != -1) && 
               (System.currentTimeMillis() >= (startTime.getTime() + DelayProxy.slidingWindow*1000)))
            {
                // just close the socket
                closeSock(sock_c);
                return;
            }
        }

        try
        {
            in_c = sock_c.getInputStream();
            out_c = sock_c.getOutputStream();
        }
        catch (IOException e)
        {
            System.err.println("Error: Can't get client I/O Streams!");
            e.printStackTrace();
            closeSock(sock_c);
            return;
        }

        /*****************************************************
            Read Client Request From Socket InputStream in_c
         ******************************************************/
        byte onebyte[] = new byte[1];
        boolean EOH = false; // flag End Of Header
        try 
        {
            while(!EOH) 
            {
                if(in_c.read(onebyte) <= 0) 
                {
                    EOH = true;
                    continue;
                }
                else
                {
                    sb.append(new String(onebyte, "8859_1"));
                    if(in_c_file != null)
                    {
                        in_c_file.write(onebyte);
                        in_c_file.flush();
                    }
                }

                if(sb.toString().endsWith(_CRLF2) || sb.toString().endsWith(_LF2)) 
                {
                    EOH = true;
                }
            }
        } 
        catch (IOException ie) 
        {
            System.err.println("Error getting client request header!");
            ie.printStackTrace();
            closeSock(sock_c);
            return;
        }

        c_header = new HTTPRequestHeader(sb.toString());
        
        // DEBUGMSG
        //System.out.println(sb.toString());

        /*****************************************************
            Open a socket connection to remote server
         *****************************************************/
        byte resp[] = new byte[64*1024];
        int  content_len = 0;
        int  total_len   = 0;
        int  len = 0;
        content_len = c_header.getContentLength(); // get content length from
                                                   // client request
        EOH = false; // flag End Of Header

        try 
        {
            // open a socket to connect to webserver
            sock_s = new Socket(webaddr, webport);
            in_s  = sock_s.getInputStream();
            out_s = sock_s.getOutputStream();

            if(DelayProxy.error_inject)
            {
                /* swap 2 bytes in the header */
                int swap1 = DelayProxy.rand.nextInt(sb.length());
                int swap2 = DelayProxy.rand.nextInt(sb.length());
                char ch1 = sb.charAt(swap1);
                char ch2 = sb.charAt(swap2);
                sb.setCharAt(swap1, ch2);
                sb.setCharAt(swap2, ch1);
            }

            // write header message to server
            out_s.write(sb.toString().getBytes(), 0, sb.toString().length());
            out_s.flush();

            if(out_s_file != null)
            {
                out_s_file.write(sb.toString().getBytes(), 0, sb.toString().length());
                out_s_file.flush();
            }

            // write content body to server if there is any
            if(content_len > 0) 
            {
                while(content_len > total_len) 
                {
                    if((len = in_c.read(resp)) <= 0) break;
                    if(in_c_file != null)
                    {
                        in_c_file.write(resp,0,len);
                        in_c_file.flush();
                    }
                    total_len += len;

                    if(DelayProxy.error_inject)
                    {                
                        // swap 2 bytes in the body
                        int swap1 = DelayProxy.rand.nextInt(len);
                        int swap2 = DelayProxy.rand.nextInt(len);
                        byte tmp = resp[swap1];
                        resp[swap1] = resp[swap2];
                        resp[swap2] = tmp;
                    }
                    

                    out_s.write(resp, 0, len);
                    out_s.flush();
                    if(out_s_file != null)
                    {
                        out_s_file.write(resp,0,len);
                        out_s_file.flush();
                    }
                }
            }            

            sb = new StringBuffer(); // clear it out

            /*****************************************************
                Read Server Response
             *****************************************************/
            sock_s.setSoTimeout(DelayProxy.polltimeout);
            sock_s.setKeepAlive(true);
            while(!quit)
            {
                len = 0; // reset
                try
                {
                    len = in_s.read(resp);
                }
                catch(SocketTimeoutException timeexp)
                {
                    continue;
                }

                if(len < 0)
                {
                    break;
                }

                if(in_s_file != null)
                {                            
                    in_s_file.write(resp, 0, len);
                    in_s_file.flush();
                }

                if(DelayProxy.error_inject)
                {                
                    // swap 2 bytes in each chunk
                    int swap1 = DelayProxy.rand.nextInt(len);
                    int swap2 = DelayProxy.rand.nextInt(len);
                    byte tmp = resp[swap1];
                    resp[swap1] = resp[swap2];
                    resp[swap2] = tmp;
                }
                                
                out_c.write(resp, 0, len);
                out_c.flush();
                if(out_c_file != null)
                {   
                    out_c_file.write(resp, 0, len);
                    out_c_file.flush();
                }
            }
        }
        catch(SocketException se) 
        {
            System.err.println("======================================");
            System.err.println("Proxy Error: SocketException, " + c_header.URI);
            se.printStackTrace();
            System.err.println("======================================");
        }
        catch(IOException ie) 
        {
            System.err.println("======================================");
            System.err.println("Proxy Error: IOException, " + c_header.URI);
            ie.printStackTrace();
            System.err.println("======================================");
        }

        closeSock(sock_c);      // close connection to client
        closeSock(sock_s);      // close connection to web server
        try
        {            
            if(in_s_file != null)
            {
                in_s_file.close();
            }
            if(out_c_file != null)
            {
                out_c_file.close();
            }
            if(in_c_file != null)
            {
                in_c_file.close();
            }
            if(out_s_file != null)
            {
                out_s_file.close();
            }
        }
        catch(IOException e)
        {
        }
    } 
}
