package com.sun.j2ee.blueprints.opc.webservice;

import javax.xml.rpc.*;
import javax.xml.rpc.server.*;
import javax.xml.transform.Source;
import javax.jms.*;
import java.rmi.*;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.opc.webservice.invoicercvr.*;


public class OPCServiceImpl implements OPCService, ServiceLifecycle {


    public void init(Object context) throws JAXRPCException {}

    public void submitInvoice(Source invoiceDocument) throws RemoteException, InvalidInvoiceException {
        InvoiceRcvr invoiceRcvr = new InvoiceRcvr(); // Should ideally get it from a pool
        invoiceRcvr.receive(invoiceDocument);
        return;
    }

    public void destroy() {}
}

