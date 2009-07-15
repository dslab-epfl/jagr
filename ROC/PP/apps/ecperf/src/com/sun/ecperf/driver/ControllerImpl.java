/**
 * Copyright (c) 1999 by Sun Microsystems, Inc.
 *
 * $Id: ControllerImpl.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */

package com.sun.ecperf.driver;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This class implements the Controller interface
 * The Controller is the single remote object that runs on the master
 * machine and with which all other instances of remote servers register.
 * A remote reference to any remote service is obtained by the GUI as
 * well as the Engine through the controller. Using the controller
 * eliminates the need to run rmiregistry on multiple machines. There
 * is only one remote server object (the Controller) that is known
 * by rmiregistry running on the master machine. Once a reference to
 * the Controller is obtained by the client, it should use the
 * 'getReference' method to obtain a reference to any type of
 * remote server.
 *
 * @author Shanti Subrmanyam
 */
public class ControllerImpl extends UnicastRemoteObject implements Controller
{
    private Hashtable servicesTable = new Hashtable();

    ControllerImpl() throws RemoteException
    {
        super();

        // This code was to check if AllByName returns all other names
        // for the machine
        /*      try {
                InetAddress[] allIPs = InetAddress.getAllByName("echodo");
                for (int i = 0; i < allIPs.length; i++) {
                String printHost = allIPs[i].getHostAddress();
                Debug.println("Host " + i + " = " + printHost);
                }
                }
                catch (java.net.UnknownHostException uhe) {
                Debug.println("ControllerImpl constructor: " + uhe.getMessage());
                System.exit(-1);
                } */
    }


    /**
           * register service with Controller
           * The service name is of the form <name>@<host>
           * For example, a CmdAgent will register itself as CmdAgent@<host>
           * so all CmdAgents on different machiens can be uniquely
           * identified by name.
           * @param public name of service
           * @param Remote reference to service
           */

    public void register(String name, Remote service)
    {
        // First check if service already exists
        Remote r = (Remote) servicesTable.get(name);
        if (r != null)
            unregister(name);
        Debug.println("Controller: Registering " + name +
                " on machine " + getCaller());
        servicesTable.put(name, service);
    }

    /**
           * register service with Controller
           * The service name is of the form <name>@<host>
           * For example, a CmdAgent will register itself as CmdAgent@<host>
           * so all CmdAgents on different machiens can be uniquely
           * identified by name.
           * @param type of service
           * @param name of service
           * @param Remote reference to service
           */

    public void register(String type, String name, Remote service)
    {
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null)
        {
            h = new Hashtable();
            servicesTable.put(type, h);
        }

        // check if service already exists
        Remote r = (Remote) h.get(name);
        if (r != null)
            unregister(type, name);
        Debug.println("Controller: Registering " + name +
                " on machine " + getCaller());
        h.put(name, service);
    }

    /**
           * unregister service from Controller
           * The controller removes this service from its list and clients
           * can no longer access it. This method is typically called when
           * the service exits.
           * @param public name of service
           */
    public void unregister(String name)
    {
        servicesTable.remove(name);
    }

    /**
           * unregister service from Controller
           * The controller removes this service from its list and clients
           * can no longer access it. This method is typically called when
           * the service exits.
           * @param type of service
           * @param public name of service
           */
    public void unregister(String type, String name)
    {
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null)
        {
            Debug.println(
                    "Controller.unregister : Cannot find Service type : " +
                    type);
        }
        else
        {
            h.remove(name);
        }
    }


    /**
           * get reference to service from Controller
           * The controller searches in its list of registered services
           * and returns a remote reference to the requested one.
           * The service name is of the form <name>@<host>
           * @param public name of service
           * @return remote reference
           */
    public Remote getService(String name)
    {
        Remote r = (Remote) servicesTable.get(name);
        return(r);
    }

    /**
           * get reference to service from Controller
           * The controller searches in its list of registered services
           * and returns a remote reference to the requested one.
           * The service name is of the form <name>@<host>
           * @param type of service
           * @param public name of service
           * @return remote reference
           */
    public Remote getService(String type, String name)
    {
        Remote r = null;
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null)
        {
            Debug.println(
                    "Controller.getService : Cannot find Service type : " +
                    type);
        }
        else
        {
            r = (Remote) h.get(name);
        }
        return(r);
    }

    /**
           * get all references to a type of services from Controller
           * The controller searches in its list of registered services
           * and returns all  remote references to the requested type.
           * The service name is of the form <name>@<host>
           * @param type of service
           * @return remote references
           */
    public Remote[] getServices(String type)
    {
        Remote[] r = null;
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);

        if (h == null)
        {
            Debug.println(
                    "Controller.getServices : Cannot find Service type : " +
                    type);
        }
        else
        {
            r = new Remote[h.size()];
            Enumeration enum = h.elements();
            int i = 0;
            while (enum.hasMoreElements())
                r[i++] = (Remote) enum.nextElement();
        }
        return(r);
    }

    /**
        * Get the number of registered Services of a type
        * @param type of service
        * @return int number of registered services
        */
    public int getNumServices(String type)
    {
        // First check if the type of service exists
        Hashtable h = (Hashtable) servicesTable.get(type);
        int i = 0;
        if (h == null)
        {
            Debug.println(
                    "Controller.getNumServices : Cannot find Service type : " +
                    type);
        }
        else
        {
            i = h.size(); 
        }
        return i;
    }

    // Get the caller
    private String getCaller()
    {
        String s = null;

        try
        {
            s = getClientHost(); 
        }
        catch (Exception e)
        {
            Debug.println(e.getMessage());
        }

        return s;
    }
    
    /**
        * Registration for RMI serving
        */

    public static void main(String [] argv)
    {

        String s = null;
        System.setSecurityManager (new RMISecurityManager());

        try
        {
            Controller c = new ControllerImpl();
            String host = (InetAddress.getLocalHost()).getHostName();
            s = "//" + host + "/" + "Controller";
            Naming.bind(s , c);
            Debug.println("Binding controller to " + s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                Naming.unbind(s);
            }
            catch (Exception ei)
            { }
            System.exit(-1);
        }
    }
}



