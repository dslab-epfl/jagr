package org.jboss.test.security.interfaces;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

public interface IOSession extends EJBObject
{
    /** A method that returns its arg */
    public String read(String path) throws IOException, RemoteException;
    public void write(String path) throws IOException, RemoteException;
}
