// RMIntfTest.java - test the remote interface to Recovery Manager

package roc.rr.rm;

import java.rmi.*;

public class RMIntfTest 
{
    public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        if(args.length != 2)
        {
            System.out.println("Usage: RMIntfTest <rmiregistry host> <port>");
        }
        try
        {
            String name = "//" + args[0] + ":" + args[1] + "/RMIntf";
            RMIntf rm = (RMIntf) Naming.lookup(name);
            long ms = rm.timeSinceLastFault_ms();
            System.out.println("RMIntfTest: timeSinceLastFault_ms() returned " + ms);
        } catch (Exception e) {
            System.err.println("RMIntfTest: Error accessing remote interface: " +
                               e.getMessage());
            e.printStackTrace();
        }   
    }        
}
