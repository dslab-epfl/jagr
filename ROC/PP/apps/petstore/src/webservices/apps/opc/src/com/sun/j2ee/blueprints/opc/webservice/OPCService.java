package com.sun.j2ee.blueprints.opc.webservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.xml.transform.Source;


public interface OPCService extends Remote {

    public void submitInvoice(Source invoiceDocument) throws RemoteException, InvalidInvoiceException;
}


