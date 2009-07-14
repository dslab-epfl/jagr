//
// $Id: SendTestReport.java,v 1.1 2003/03/09 05:38:39 steveyz Exp $
//

package org.jboss.RR;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;

public class SendTestReport
{
    public static void main(String[] args)
    {
        FailureReport report;

        if(args.length == 1)
        {
            report = new FailureReport(args[0], new Date());
        }
        else if(args.length == 2)
        {
            report = new FailureReport(args[0], args[1], new Date());
        }
        else
        {
            System.err.println("Usage: SendTestReport SrcNode [DstNode]");
            return;
        }

        try
        {
            ByteArrayOutputStream bArray_out = new ByteArrayOutputStream();
            ObjectOutputStream obj_out = new ObjectOutputStream(bArray_out);
            obj_out.writeObject(report);
            DatagramPacket packet = new DatagramPacket(bArray_out.toByteArray(), 
                                                       bArray_out.size(), 
                                                       InetAddress.getLocalHost(),
                                                       2374);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            System.out.println("FailureReport send to TheBrain: " + 
                report.toString());
        }
        catch (SocketException sockExp)
        {
            System.err.println("Failed to bind to a UDP port!");
        }
        catch (IOException ioExp)
        {
            System.err.println("Error sending FailureReport to TheBrain!");
        }
    }
}


    


        

