package com.sun.j2ee.blueprints.opc.webservice.posender;

import java.io.*;
import java.net.*;
import java.rmi.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.rpc.*;

import com.sun.j2ee.blueprints.opc.webservice.supplierclient.*;
import com.sun.j2ee.blueprints.xmldocuments.*;


public class SupplierOrderSender {
  public static final boolean TRACE = false;
  private SupplierService_Stub supplierService;


  public SupplierOrderSender(URL serviceEndPointURL) {
        supplierService = (SupplierService_Stub) new SupplierWebService_Impl().getSupplierServicePort();
        supplierService._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, serviceEndPointURL.toString());
        return;
  }

  public String submitOrder(String supplierOrder) throws RemoteException, InvalidOrderException {
        if (TRACE) {
      System.err.println("SupplierOrderSender.submitOrder: \n" + supplierOrder);
        }
        return submitOrder(new StreamSource(new StringReader(supplierOrder)));
  }

  public String submitOrder(Source supplierOrder) throws RemoteException, InvalidOrderException {
        String trackingNumber = supplierService.submitOrder(supplierOrder);
        if (TRACE) {
      System.err.println("SupplierOrderSender.submitOrder, trackingNumber=" + trackingNumber);
        }
        return trackingNumber;
  }
}
