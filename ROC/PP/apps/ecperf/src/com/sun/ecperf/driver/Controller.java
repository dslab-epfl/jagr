/*
 * Copyright (c) 1999 by Sun Microsystems, Inc.
 *
 * $Id: Controller.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.driver;

import java.rmi.RemoteException;
import java.rmi.Remote;
import java.io.*;


/**
 * The methods in this interface are the public face of Controller
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
public interface Controller extends Remote {

    /**
     * register service with Controller
     * The service name is of the form <name>@<host>
     * For example, a CmdAgent will register itself as CmdAgent@<host>
     * so all CmdAgents on different machiens can be uniquely
     * identified by name.
     * @param public name of service 
     * @param Remote reference to service
     */
    public void register(String name, Remote service) throws RemoteException;

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
    public void register(String type, String name, Remote service) throws RemoteException;

    /**
     * unregister service from Controller
     * The controller removes this service from its list and clients
     * can no longer access it. This method is typically called when
     * the service exits.
     * @param public name of service 
     */
    public void unregister(String name) throws RemoteException;

    /**
           * unregister service from Controller
           * The controller removes this service from its list and clients
           * can no longer access it. This method is typically called when
           * the service exits.
           * @param type of service
           * @param public name of service
           */
    public void unregister(String type, String name) throws RemoteException;
    
    /**
     * get reference to service from Controller
     * The controller searches in its list of registered services
     * and returns a remote reference to the requested one.
     * The service name is of the form <name>@<host>
     * @param public name of service 
     * @return remote reference
     */
    public Remote getService(String name) throws RemoteException;

    /**
           * get reference to service from Controller
           * The controller searches in its list of registered services
           * and returns a remote reference to the requested one.
           * The service name is of the form <name>@<host>
           * @param type of service
           * @param public name of service
           * @return remote reference
           */
    public Remote getService(String type, String name) throws RemoteException;
    
    /**
           * get all references to a type of services from Controller
           * The controller searches in its list of registered services
           * and returns all  remote references to the requested type.
           * The service name is of the form <name>@<host>
           * @param type of service
           * @return remote references
           */
    public Remote[] getServices(String type) throws RemoteException;
    
    /**
        * Get the number of registered Services of a type
        * @param type of service
        * @return int number of registered services
        */
    public int getNumServices(String type) throws RemoteException;
 
}
