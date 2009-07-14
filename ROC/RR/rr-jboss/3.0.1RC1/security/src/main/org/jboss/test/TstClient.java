package org.jboss.test;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.jboss.security.srp.SRPClientSession;
import org.jboss.security.srp.SRPServerInterface;
import org.jboss.security.srp.SRPParameters;

/** A simple test client that looks up the SimpleSRPServer in the RMI
registry and attempts to validate the username and password passed
on the command line.

@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
*/
public class TstClient
{
    public static void main(String[] args) throws Exception
    {
        String username = args[0];
        char[] password = args[1].toCharArray();
        SRPServerInterface server = (SRPServerInterface) Naming.lookup("SimpleSRPServer");
        System.out.println("Found SRPServerInterface");
        SRPParameters params = server.getSRPParameters(username);
        System.out.println("Found params for username: "+username);
        SRPClientSession client = new SRPClientSession(username, password, params);
        byte[] A = client.exponential();
        byte[] B = server.init(username, A);
        System.out.println("Sent A public key, got B public key");
        byte[] M1 = client.response(B);
        byte[] M2 = server.verify(username, M1);
        System.out.println("Sent M1 challenge, got M2 challenge");
        if( client.verify(M2) == false )
            throw new SecurityException("Failed to validate server reply");
        System.out.println("Validation successful");
    }
}
