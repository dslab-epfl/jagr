package com.sun.j2ee.blueprints.supplier.webservice.invoicesender;

import java.io.*;
import java.net.*;
import java.rmi.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.rpc.*;

import com.sun.j2ee.blueprints.supplier.webservice.opcclient.*;
import com.sun.j2ee.blueprints.xmldocuments.*;


public class InvoiceSender {
  public static final boolean TRACE = false;
  private OPCService_Stub opcService;


  public InvoiceSender(URL serviceEndPointURL) {
    opcService = (OPCService_Stub) new OPCWebService_Impl().getOPCServicePort();
    opcService._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, serviceEndPointURL.toString());
    return;
  }

  public void submitInvoice(String invoiceDocument) throws RemoteException, InvalidInvoiceException {
    if (TRACE) {
          System.err.println("InvoiceSender.order: \n" + invoiceDocument);
    }
    submitInvoice(new StreamSource(new StringReader(invoiceDocument)));
    return;
  }

  public void submitInvoice(Source invoiceDocument) throws RemoteException, InvalidInvoiceException {
    opcService.submitInvoice(invoiceDocument);
    return;
  }
}
