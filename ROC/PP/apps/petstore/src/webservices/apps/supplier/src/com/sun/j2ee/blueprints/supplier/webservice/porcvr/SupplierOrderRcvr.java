package com.sun.j2ee.blueprints.supplier.webservice.porcvr;

import java.net.*;
import java.io.*;
import java.rmi.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.jms.*;

import com.sun.j2ee.blueprints.supplier.webservice.*;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.servicelocator.web.ServiceLocator;


public class SupplierOrderRcvr {
  public static final boolean TRACE = false;
  private TPASupplierOrderXDE supplierOrderXDE;
  private QueueHelper supplierOrderQueueHelper;


  public SupplierOrderRcvr() throws RemoteException {
    try {
          ServiceLocator serviceLocator = ServiceLocator.getInstance();
          QueueConnectionFactory queueFactory
        = serviceLocator.getQueueConnectionFactory(JNDINames.QUEUE_CONNECTION_FACTORY);
          Queue supplierOrderQueue = serviceLocator.getQueue(JNDINames.SUPPLIER_PURCHASE_ORDER_QUEUE);
          supplierOrderQueueHelper = new QueueHelper(queueFactory, supplierOrderQueue);
          supplierOrderXDE = new TPASupplierOrderXDE(serviceLocator.getUrl(JNDINames.XML_ENTITY_CATALOG_URL),
                                                 serviceLocator.getBoolean(JNDINames.XML_VALIDATION_SUPPLIER_ORDER),
                                                 serviceLocator.getBoolean(JNDINames.XML_XSD_VALIDATION_SUPPLIER_ORDER));
    } catch (Exception exception) {
          System.err.println(exception);
          throw new RemoteException("Service internal error.", exception);
    }
    return;
  }

  public String receive(Source supplierOrder) throws RemoteException, InvalidOrderException {
    try {
          supplierOrderXDE.setDocument(supplierOrder);
          String document = supplierOrderXDE.getDocumentAsString();
          if (TRACE) {
        System.err.println("SupplierOrderRcvr.receive, resending: " + document);
          }
          supplierOrderQueueHelper.sendMessage(document);
          return supplierOrderXDE.getOrderId();
    } catch (XMLDocumentException exception) {
          System.err.println(exception);
          throw new InvalidOrderException(exception.getMessage());
    } catch (Exception exception) {
          System.err.println(exception);
          throw new RemoteException("Service internal error.", exception);
    }
  }
}
