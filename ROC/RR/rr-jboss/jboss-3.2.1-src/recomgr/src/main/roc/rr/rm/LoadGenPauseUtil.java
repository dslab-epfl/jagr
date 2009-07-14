//
// $Id: LoadGenPauseUtil.java,v 1.1 2003/09/24 05:57:47 steveyz Exp $
//

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class LoadGenPauseUtil
{
    protected static int port = 5623; /* port to listen on for start/stop msg */
    protected static String start_cmd = null;
    protected static String stop_cmd = null;

    public static void main(String[] args)
    {
        if(args.length != 2)
        {
            System.out.println("Usage: LoadGenPauseThread <start cmd> <stop cmd>");
            System.exit(1);
        }
        
        start_cmd = args[0];
        stop_cmd = args[1];

        try
        {
            Thread lgthread = new LoadGenPauseThread();
            lgthread.start();
            System.out.println("LoadGenPause listening on port " + port);
        }
        catch(SocketException sockexp)
        {
            System.err.println("Could not bind to port " + port);
            System.exit(1);
        }
    }

    private static class LoadGenPauseThread extends Thread
    {
        DatagramSocket socket = null;
        public LoadGenPauseThread() throws SocketException
        {
            socket = new DatagramSocket(port);
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
                    if(buf[0] == 'S') // start
                    {
                        System.out.println("*** Received START load message! ***");
                        System.out.println("Issuing command: " + start_cmd);
                        // issue the start command
                        Runtime.getRuntime().exec(start_cmd);
                    }
                    else if(buf[0] == 'T') // stop
                    {
                        System.out.println("*** Received STOP load message! ***");
                        System.out.println("Issuing command: " + stop_cmd);
                        // issue the stop command
                        Runtime.getRuntime().exec(stop_cmd);
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

